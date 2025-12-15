package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.Project;
import com.example.concept_platform.entity.Review;
import com.example.concept_platform.entity.vo.ReviewVO;
import com.example.concept_platform.mapper.ProjectMapper;
import com.example.concept_platform.mapper.ReviewMapper;
import com.example.concept_platform.service.IReviewService;
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

    @Override
    public List<ReviewVO> getReviewListByExpert(Integer expertId) {
        return reviewMapper.selectReviewVOList(expertId);
    }

    @Override
    @Transactional
    public boolean submitReview(Integer reviewId, Integer score, String comments) {
        // 1. Update Review Record
        Review review = this.getById(reviewId);
        if (review == null) {
            return false;
        }
        review.setScore(score);
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
                // Calculate average score
                OptionalDouble avg = allReviews.stream()
                        .filter(r -> r.getScore() != null)
                        .mapToInt(Review::getScore)
                        .average();

                double avgScore = avg.orElse(0.0);

                Project project = projectMapper.selectById(review.getProjectId());
                if (project != null) {
                    if (avgScore >= 60) {
                        project.setStatus(3); // Incubating/In Library
                    } else {
                        project.setStatus(9); // Rejected
                        project.setRejectReason("Average score below 60: " + String.format("%.2f", avgScore));
                    }
                    projectMapper.updateById(project);
                }
            }
        }
        return updateResult;
    }
}
