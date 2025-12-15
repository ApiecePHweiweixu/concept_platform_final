package com.example.concept_platform.entity.vo;

import lombok.Data;
import java.util.List;

@Data
public class ProjectResultVO {
    private Integer projectId;
    private String projectName;
    private Integer status;
    private String rejectReason;
    private List<SimpleReviewVO> reviews;

    @Data
    public static class SimpleReviewVO {
        private String expertName; // Fixed as "Review Expert"
        private Integer score;
        private String comments;
    }
}

