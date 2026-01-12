package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资源库存台账实体类
 */
@Data
@TableName("resource_inventory")
public class ResourceInventory {
    @TableId(value = "resource_id", type = IdType.AUTO)
    private Integer resourceId;

    private String resourceName;

    /**
     * FUND, COMPUTING, SPACE, SERVICE
     */
    private String resourceType;

    private BigDecimal totalQuota;

    private BigDecimal remainingQuota;

    private String unit;

    private String description;

    private Integer status;

    private LocalDateTime createdAt;
}

