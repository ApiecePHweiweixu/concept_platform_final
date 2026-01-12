package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_ai_analysis")
public class ProjectAiAnalysis {
    @TableId(value = "analysis_id", type = IdType.AUTO)
    private Integer analysisId;
    
    private Integer projectId;
    
    private Integer innovationScore;
    
    private Integer feasibilityScore;
    
    private Integer marketScore;
    
    private String analysisSummary;
    
    private String riskWarning;
    
    private String rawResponse;
    
    /**
     * 0-InProgress, 1-Success, 2-Failed
     */
    private Integer status;
    
    private LocalDateTime createdAt;
}

