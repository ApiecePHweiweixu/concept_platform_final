package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 孵化里程碑进展报告
 */
@Data
@TableName("incubation_milestone_report")
public class IncubationMilestoneReport {

    @TableId(value = "report_id", type = IdType.AUTO)
    private Integer reportId;

    private Integer milestoneId;

    private Integer projectId;

    private Integer submitterId;

    private String content;

    /**
     * 项目描述（申报者填写）
     */
    @TableField("project_description")
    private String projectDescription;

    /**
     * 附件列表 JSON 字符串
     */
    private String attachments;

    /**
     * 状态: 0-待审核, 1-已通过, 2-需修订
     */
    private Integer status;

    @TableField("mentor_feedback")
    private String mentorFeedback;

    /**
     * 专家评分(0-100)
     */
    @TableField("expert_score")
    private Integer expertScore;

    /**
     * 专家反馈意见
     */
    @TableField("expert_feedback")
    private String expertFeedback;

    /**
     * 管理员是否已审批: 0-未审批, 1-已审批通过, 2-已审批拒绝
     */
    @TableField("admin_approved")
    private Integer adminApproved;

    /**
     * 管理员审批意见
     */
    @TableField("admin_feedback")
    private String adminFeedback;

    /**
     * 管理员是否已通知: 0-未通知, 1-已通知
     */
    @TableField("admin_notified")
    private Integer adminNotified;

    /**
     * 管理员通知时间
     */
    @TableField("admin_notify_time")
    private LocalDateTime adminNotifyTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}


