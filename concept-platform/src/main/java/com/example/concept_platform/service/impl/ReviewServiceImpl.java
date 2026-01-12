package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.Project;
import com.example.concept_platform.entity.Review;
import com.example.concept_platform.entity.IncubationProject;
import com.example.concept_platform.entity.IncubationMilestone;
import com.example.concept_platform.entity.vo.ReviewVO;
import com.example.concept_platform.mapper.ProjectMapper;
import com.example.concept_platform.mapper.ReviewMapper;
import com.example.concept_platform.service.IReviewService;
import com.example.concept_platform.service.IIncubationProjectService;
import com.example.concept_platform.service.IIncubationMilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements IReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private IIncubationProjectService incubationProjectService;

    @Autowired
    private IIncubationMilestoneService incubationMilestoneService;

    @Override
    public List<ReviewVO> getReviewListByExpert(Integer expertId) {
        return reviewMapper.selectReviewVOList(expertId);
    }

    @Override
    @Transactional
    public boolean submitReview(Integer reviewId,
                                Integer feasibilityScore,
                                Integer depthScore,
                                Integer extensionScore,
                                String comments) {
        // 1. Update Review Record
        Review review = this.getById(reviewId);
        if (review == null) {
            return false;
        }

        // 旧字段 score 兼容：取加权总分或简单平均
        // 权重：可行性 0.4，深度 0.3，拓展度 0.3
        int f = feasibilityScore == null ? 0 : feasibilityScore;
        int d = depthScore == null ? 0 : depthScore;
        int e = extensionScore == null ? 0 : extensionScore;
        double total = f * 0.4 + d * 0.3 + e * 0.3;
        int totalInt = (int) Math.round(total);

        review.setScoreFeasibility(feasibilityScore);
        review.setScoreDepth(depthScore);
        review.setScoreExtension(extensionScore);
        review.setTotalScore(totalInt);
        review.setScore(totalInt);
        review.setComments(comments);
        review.setStatus(1); // Reviewed
        review.setReviewTime(LocalDateTime.now()); // Fix: Force update time
        boolean updateResult = this.updateById(review);

        if (updateResult) {
            // 2. Check if all reviews for this project are completed
            QueryWrapper<Review> query = new QueryWrapper<>();
            query.eq("project_id", review.getProjectId());
            List<Review> allReviews = this.list(query);

            boolean allCompleted = allReviews.stream().allMatch(r -> r.getStatus() == 1);
            
            if (allCompleted) {
                // Calculate average total score（基于 totalScore，如果为空则退回到 score）
                OptionalDouble avg = allReviews.stream()
                        .filter(r -> r.getScore() != null)
                        .mapToInt(r -> {
                            if (r.getTotalScore() != null) {
                                return r.getTotalScore();
                            }
                            return r.getScore();
                        })
                        .average();

                double avgScore = avg.orElse(0.0);

                Project project = projectMapper.selectById(review.getProjectId());
                if (project != null) {
                    // 保存最终得分
                    project.setFinalScore(java.math.BigDecimal.valueOf(avgScore));
                    
                    if (avgScore >= 60) {
                        // 概念验证通过：项目进入“已入库”，并自动创建孵化项目与两阶段里程碑
                        project.setStatus(3);
                        projectMapper.updateById(project);

                        // 1. 创建孵化项目记录（如不存在）
                        IncubationProject incubation = ensureIncubationProject(project.getProjectId());
                        // 2. 自动生成两阶段里程碑：中期评审 & 最终定型
                        ensureDefaultMilestones(incubation.getIncubationId());
                    } else {
                        project.setStatus(9); // Rejected
                        project.setRejectReason("Average score below 60: " + String.format("%.2f", avgScore));
                        projectMapper.updateById(project);
                    }
                }
            }
        }
        return updateResult;
    }

    /**
     * 如果还没有孵化项目记录，则创建一条待签署协议的记录
     */
    private IncubationProject ensureIncubationProject(Integer projectId) {
        QueryWrapper<IncubationProject> query = new QueryWrapper<>();
        query.eq("project_id", projectId);
        IncubationProject existing = incubationProjectService.getOne(query);
        if (existing != null) {
            return existing;
        }
        IncubationProject incubation = new IncubationProject();
        incubation.setProjectId(projectId);
        // 0-待签署协议：此时即可在孵化视图中看到项目
        incubation.setStatus(0);
        incubationProjectService.save(incubation);
        return incubation;
    }

    /**
     * 为孵化项目创建默认的两阶段里程碑：中期审核、最终定型
     * 若已经存在里程碑则不重复创建
     */
    private void ensureDefaultMilestones(Integer incubationId) {
        QueryWrapper<IncubationMilestone> query = new QueryWrapper<>();
        query.eq("incubation_id", incubationId);
        long count = incubationMilestoneService.count(query);
        if (count > 0) {
            return;
        }
        // M1: 中期审核
        IncubationMilestone mid = new IncubationMilestone();
        mid.setIncubationId(incubationId);
        mid.setName("中期审核");
        mid.setDescription("提交中期进展报告，专家开展中期评审与资源调整。");
        mid.setDeliverables("中期进展报告、阶段成果材料");
        mid.setStatus(1); // 进行中
        mid.setCreatedAt(LocalDateTime.now());
        incubationMilestoneService.save(mid);

        // M2: 最终定型
        IncubationMilestone finalM = new IncubationMilestone();
        finalM.setIncubationId(incubationId);
        finalM.setName("最终定型");
        finalM.setDescription("提交最终成果材料，专家进行结项/毕业评审。");
        finalM.setDeliverables("完整商业计划书、产品/服务说明、知识产权证明等");
        finalM.setStatus(0); // 未开始，由中期通过后人工激活亦可
        finalM.setCreatedAt(LocalDateTime.now());
        incubationMilestoneService.save(finalM);
    }
}
