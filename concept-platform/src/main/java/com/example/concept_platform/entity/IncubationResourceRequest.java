package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 孵化阶段资源申请工单
 */
@Data
@TableName("incubation_resource_request")
public class IncubationResourceRequest {

    @TableId(value = "request_id", type = IdType.AUTO)
    private Integer requestId;

    private Integer incubationId;

    private Integer projectId;

    private Integer requesterId;

    /**
     * 资源类型: TECH/IP/MARKET/FUND 等
     */
    private String type;

    private String title;

    private String description;

    /**
     * 状态: 0-待处理, 1-处理中, 2-已完成, 3-已拒绝
     */
    private Integer status;

    private Integer handlerId;

    /**
     * 处理历史 JSON 字符串
     */
    private String historyJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer allocatedResourceId;

    private java.math.BigDecimal allocatedAmount;
}


