package com.example.concept_platform.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Integer reviewId;
    private Integer projectId;
    private String projectName;
    private String projectDescription; // Added project description
    private String applicantName;
    private String techDomain;
    private String attachmentUrl;
    private Integer status; // 0-Not Reviewed, 1-Reviewed
    private Integer score;
    private String comments;
    private LocalDateTime reviewTime;
}
