package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 孵化项目主表实体，对应 incubation_project
 */
@Data
@TableName("incubation_project")
public class IncubationProject {

    @TableId(value = "incubation_id", type = IdType.AUTO)
    private Integer incubationId;

    private Integer projectId;

    /**
     * 孵化状态: 0-待签署协议, 1-孵化中, 2-已落成, 3-终止
     */
    private Integer status;

    private String agreementUrl;

    /**
     * 入孵信息表 JSON 字符串
     */
    private String infoFormJson;

    private Integer projectManagerId;

    /**
     * 导师ID列表，逗号分隔
     */
    private String mentorIds;

    /**
     * 落成状态（两段式审批流程）:
     * 0-未申请
     * 1-待平台初审（管理员）
     * 2-平台初审通过，待专家终审
     * 3-已落成（专家终审通过）
     * 4-平台初审拒绝
     * 5-专家终审拒绝
     */
    private Integer completionStatus;

    private String completionPackageUrl;

    private String completionDesc;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}


