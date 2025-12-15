package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Project Entity
 */
@Data
@TableName("project")
public class Project {
    @TableId(value = "project_id", type = IdType.AUTO)
    private Integer projectId;

    private String projectName;

    private String description;

    private String techDomain;

    private BigDecimal budget;

    private String attachmentUrl;

    private String rejectReason;

    private LocalDateTime auditTime;

    /**
     * 0-Draft, 1-Pending Review, 2-In Library, 9-Rejected, 3-Review Ended/Pending Final
     */
    private Integer status;

    private Integer applicantId;

    private LocalDateTime createdAt;
}
