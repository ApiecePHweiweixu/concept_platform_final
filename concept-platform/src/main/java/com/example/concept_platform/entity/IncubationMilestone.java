package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 孵化里程碑实体
 */
@Data
@TableName("incubation_milestone")
public class IncubationMilestone {

    @TableId(value = "milestone_id", type = IdType.AUTO)
    private Integer milestoneId;

    private Integer incubationId;

    private String name;

    private String description;

    private String deliverables;

    private LocalDateTime deadline;

    /**
     * 状态: 0-未开始, 1-进行中, 2-待审核, 3-通过, 4-需修订
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}


