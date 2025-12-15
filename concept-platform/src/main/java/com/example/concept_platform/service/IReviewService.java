package com.example.concept_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.concept_platform.entity.Review;
import com.example.concept_platform.entity.vo.ReviewVO;

import java.util.List;

public interface IReviewService extends IService<Review> {
    List<ReviewVO> getReviewListByExpert(Integer expertId);
    boolean submitReview(Integer reviewId, Integer score, String comments);
}
