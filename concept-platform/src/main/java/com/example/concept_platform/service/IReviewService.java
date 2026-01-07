package com.example.concept_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.concept_platform.entity.Review;
import com.example.concept_platform.entity.vo.ReviewVO;

import java.util.List;

public interface IReviewService extends IService<Review> {
    List<ReviewVO> getReviewListByExpert(Integer expertId);

    /**
     * 多维度提交评审结果，并自动计算加权总分与更新项目状态
     */
    boolean submitReview(Integer reviewId,
                         Integer feasibilityScore,
                         Integer depthScore,
                         Integer extensionScore,
                         String comments);
}
