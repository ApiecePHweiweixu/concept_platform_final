package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Review Entity
 */
@Data
@TableName("review")
public class Review {
    @TableId(value = "review_id", type = IdType.AUTO)
    private Integer reviewId;

    private Integer projectId;

    private Integer expertId;

    /**
     * 单维旧总分（兼容历史数据），保留字段
     */
    private Integer score;

    /**
     * 多维度评分：可行性、深度、拓展度
     */
    private Integer scoreFeasibility;

    private Integer scoreDepth;

    private Integer scoreExtension;

    /**
     * 加权总分，后端根据多维分自动计算
     */
    private Integer totalScore;

    private String comments;

    private LocalDateTime reviewTime;

    /**
     * 0-Not Reviewed, 1-Reviewed
     */
    private Integer status;
}

