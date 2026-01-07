package com.example.concept_platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.concept_platform.common.Result;
import com.example.concept_platform.entity.Review;
import com.example.concept_platform.entity.vo.ReviewVO;
import com.example.concept_platform.service.IReviewService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private IReviewService reviewService;

    // List reviews for a project
    @GetMapping("/project/{projectId}")
    public Result<List<Review>> listByProject(@PathVariable Integer projectId) {
        QueryWrapper<Review> query = new QueryWrapper<>();
        query.eq("project_id", projectId);
        return Result.success(reviewService.list(query));
    }

    // List reviews by an expert
    @GetMapping("/expert/{expertId}")
    public Result<List<Review>> listByExpert(@PathVariable Integer expertId) {
        // Simple list, deprecated by /list endpoint for detailed VO
        QueryWrapper<Review> query = new QueryWrapper<>();
        query.eq("expert_id", expertId);
        return Result.success(reviewService.list(query));
    }

    // Get My Reviews (Expert) - with Project Details
    @GetMapping("/list")
    public Result<List<ReviewVO>> listMyReviews(@RequestParam Integer expertId) {
        return Result.success(reviewService.getReviewListByExpert(expertId));
    }

    // Get One by ID
    @GetMapping("/{id}")
    public Result<Review> getById(@PathVariable Integer id) {
        return Result.success(reviewService.getById(id));
    }

    // Add Review (Initialize)
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Review review) {
        if (review.getStatus() == null) {
            review.setStatus(0); // Default to not reviewed
        }
        return Result.success(reviewService.save(review));
    }

    // Submit Review
    @PostMapping("/submit")
    public Result<Boolean> submit(@RequestBody SubmitReviewDto dto) {
        if (dto.getReviewId() == null
                || dto.getFeasibilityScore() == null
                || dto.getDepthScore() == null
                || dto.getExtensionScore() == null) {
            return Result.error("评分维度不能为空");
        }
        // Ensure comments is passed
        return Result.success(reviewService.submitReview(
                dto.getReviewId(),
                dto.getFeasibilityScore(),
                dto.getDepthScore(),
                dto.getExtensionScore(),
                dto.getComments()
        ));
    }

    // Update / Submit Review (Old generic update)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Review review) {
        if (review.getScore() != null && review.getComments() != null && review.getStatus() != null && review.getStatus() == 1) {
             if (review.getReviewTime() == null) {
                 review.setReviewTime(LocalDateTime.now());
             }
        }
        return Result.success(reviewService.updateById(review));
    }

    // Delete
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(reviewService.removeById(id));
    }

    @Data
    public static class SubmitReviewDto {
        private Integer reviewId;
        // 多维度评分
        private Integer feasibilityScore;
        private Integer depthScore;
        private Integer extensionScore;
        private String comments; // Confirmed field name is comments
    }
}
