package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Project Entity - 科技成果信息表
 */
@Data
@TableName("project")
public class Project {
    @TableId(value = "project_id", type = IdType.AUTO)
    private Integer projectId;

    private String projectName; // 成果名称

    private String techDomain; // 成果领域（多选，JSON格式存储）

    private String description; // 成果介绍（包括成果背景、痛点问题、技术解决方案、竞争优势分析、获奖情况、创新点等）

    private String applicationScenario; // 应用场景及案例

    private String cooperationNeed; // 合作需求（多选，JSON格式：技术授权/联合研发/技术咨询/人才联合培养）

    private String intellectualProperty; // 知识产权情况（专利、软著、核心论文等）

    private String attachmentUrl; // 附件文件路径/URL（技术展示材料、补充材料、技术白皮书等）

    private String otherSupplement; // 其他补充信息

    private BigDecimal budget; // 预期经费(万元)

    private String rejectReason; // 驳回理由

    private LocalDateTime auditTime; // 管理员审核时间

    /**
     * 0-Draft, 1-Pending Review, 2-In Library, 9-Rejected, 3-Review Ended/Pending Final
     */
    private Integer status;

    private Integer applicantId;
    
    private java.math.BigDecimal finalScore; // 评审最终平均分

    private LocalDateTime createdAt;
}
