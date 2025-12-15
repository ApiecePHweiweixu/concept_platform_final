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

    private Integer score;

    private String comments;

    private LocalDateTime reviewTime;

    /**
     * 0-Not Reviewed, 1-Reviewed
     */
    private Integer status;
}

