package com.example.concept_platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.concept_platform.common.Result;
import com.example.concept_platform.entity.Project;
import com.example.concept_platform.entity.Review;
import com.example.concept_platform.entity.vo.ProjectResultVO;
import com.example.concept_platform.service.IProjectService;
import com.example.concept_platform.service.IReviewService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

    @Autowired
    private IReviewService reviewService;

    @Data
    public static class StatItem {
        private String name;
        private Long value;
    }

    // Get List
    @GetMapping("/list")
    public Result<List<Project>> list() {
        return Result.success(projectService.list());
    }

    // Get One by ID
    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Integer id) {
        return Result.success(projectService.getById(id));
    }

    // Add (Simplified, using entity directly)
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Project project) {
        return Result.success(projectService.save(project));
    }

    // Update
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Project project) {
        return Result.success(projectService.updateById(project));
    }

    // Delete
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(projectService.removeById(id));
    }
    
    @GetMapping("/list/my")
    public Result<List<Project>> listMy(@RequestParam Integer applicantId) {
        QueryWrapper<Project> query = new QueryWrapper<>();
        query.eq("applicant_id", applicantId);
        query.orderByDesc("created_at"); // 按时间倒序
        return Result.success(projectService.list(query));
    }

    // Get Project Result
    @GetMapping("/result/{projectId}")
    public Result<ProjectResultVO> getResult(@PathVariable Integer projectId) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            return Result.error("Project not found");
        }

        ProjectResultVO resultVO = new ProjectResultVO();
        resultVO.setProjectId(project.getProjectId());
        resultVO.setProjectName(project.getProjectName());
        resultVO.setStatus(project.getStatus());
        
        // If rejected, set reason
        if (project.getStatus() == 9) {
            resultVO.setRejectReason(project.getRejectReason());
        }

        // Get Reviews
        QueryWrapper<Review> reviewQuery = new QueryWrapper<>();
        reviewQuery.eq("project_id", projectId);
        reviewQuery.eq("status", 1); // Only finished reviews
        List<Review> reviews = reviewService.list(reviewQuery);

        // Convert to SimpleReviewVO (Hide Expert Info)
        List<ProjectResultVO.SimpleReviewVO> simpleReviews = reviews.stream().map(r -> {
            ProjectResultVO.SimpleReviewVO vo = new ProjectResultVO.SimpleReviewVO();
            vo.setExpertName("评审专家"); // Anonymized
            vo.setScore(r.getScore());
            vo.setComments(r.getComments());
            return vo;
        }).collect(Collectors.toList());

        resultVO.setReviews(simpleReviews);
        
        return Result.success(resultVO);
    }

    /**
     * 统计各技术领域项目数量（用于饼图）
     */
    @GetMapping("/stats/domain")
    public Result<List<StatItem>> getDomainStats() {
        List<Project> projects = projectService.list();
        List<StatItem> stats = projects.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getTechDomain() == null || p.getTechDomain().isBlank() ? "未分类" : p.getTechDomain(),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> {
                    StatItem item = new StatItem();
                    item.setName(e.getKey());
                    item.setValue(e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        return Result.success(stats);
    }

    /**
     * 统计项目状态数量（用于柱状图）
     */
    @GetMapping("/stats/status")
    public Result<List<StatItem>> getStatusStats() {
        List<Project> projects = projectService.list();
        List<StatItem> stats = projects.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus() == null ? -1 : p.getStatus(),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> {
                    StatItem item = new StatItem();
                    String name;
                    switch (e.getKey()) {
                        case 1: name = "待审核"; break;
                        case 2: name = "评审中"; break;
                        case 3: name = "已入库"; break;
                        case 9: name = "已驳回"; break;
                        case 0: name = "草稿"; break;
                        default: name = "未知"; break;
                    }
                    item.setName(name);
                    item.setValue(e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        return Result.success(stats);
    }

    // Assign Experts
    @PostMapping("/assign")
    public Result<Boolean> assign(@RequestBody AssignDto assignDto) {
        if (assignDto.getProjectId() == null || assignDto.getExpertIds() == null || assignDto.getExpertIds().isEmpty()) {
            return Result.error("Project ID and Expert IDs are required");
        }

        // 1. Update Project Status to 2 (Reviewing)
        Project project = new Project();
        project.setProjectId(assignDto.getProjectId());
        project.setStatus(2); 
        projectService.updateById(project);

        // 2. Create Review Records
        List<Review> reviews = new ArrayList<>();
        for (Integer expertId : assignDto.getExpertIds()) {
            Review review = new Review();
            review.setProjectId(assignDto.getProjectId());
            review.setExpertId(expertId);
            review.setStatus(0); // Not reviewed yet
            reviews.add(review);
        }
        
        return Result.success(reviewService.saveBatch(reviews));
    }

    // Audit Project (New)
    @PostMapping("/audit")
    public Result<Boolean> audit(@RequestBody AuditDto auditDto) {
        if (auditDto.getProjectId() == null || auditDto.getPass() == null) {
            return Result.error("Project ID and Pass status are required");
        }

        Project project = new Project();
        project.setProjectId(auditDto.getProjectId());
        project.setAuditTime(LocalDateTime.now()); // Set Audit Time

        if (auditDto.getPass()) {
            // Check if experts are assigned
            if (auditDto.getExpertIds() == null || auditDto.getExpertIds().isEmpty()) {
                return Result.error("请至少选择一名专家");
            }
            project.setStatus(2); // Passed initial review, ready for expert review
            projectService.updateById(project);

            // Assign experts
            List<Review> reviews = new ArrayList<>();
            for (Integer expertId : auditDto.getExpertIds()) {
                Review review = new Review();
                review.setProjectId(auditDto.getProjectId());
                review.setExpertId(expertId);
                review.setStatus(0); // Not reviewed yet
                reviews.add(review);
            }
            reviewService.saveBatch(reviews);
        } else {
            project.setStatus(9); // Rejected
            project.setRejectReason(auditDto.getRejectReason());
            projectService.updateById(project);
        }

        return Result.success(true);
    }

    @Data
    public static class AssignDto {
        private Integer projectId;
        private List<Integer> expertIds;
    }

    @Data
    public static class AuditDto {
        private Integer projectId;
        private Boolean pass;
        private String rejectReason;
        private List<Integer> expertIds;
    }
}
