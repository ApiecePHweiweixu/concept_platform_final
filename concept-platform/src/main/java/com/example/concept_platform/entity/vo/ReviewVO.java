package com.example.concept_platform.entity.vo;

import lombok.Data;

@Data
public class ReviewVO {
    private Integer reviewId;
    private Integer projectId;
    private String projectName;
    private String applicantName;
    private String techDomain;
    private String attachmentUrl; // Added attachment URL
    private Integer status; // 0-Not Reviewed, 1-Reviewed
    private Integer score;
    private String comments;
}
