package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 成功案例/公共展示招标表实体
 */
@Data
@TableName("success_case")
public class SuccessCase {
    @TableId(value = "case_id", type = IdType.AUTO)
    private Integer caseId;

    private Integer projectId;

    private Integer incubationId;

    private String caseName;

    private String caseDescription;

    private String techDomain;

    private String applicationScenario;

    private String intellectualProperty;

    private String cooperationMode;

    private String contactInfo;

    /**
     * 展示状态: 1-公开展示, 0-隐藏
     */
    private Integer displayStatus;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

