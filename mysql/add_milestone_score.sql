-- 为里程碑报告添加专家评分字段
USE concept_platform;

ALTER TABLE `incubation_milestone_report`
  ADD COLUMN `expert_score` int DEFAULT NULL COMMENT '专家评分(0-100)' AFTER `mentor_feedback`,
  ADD COLUMN `expert_feedback` text COMMENT '专家反馈意见' AFTER `expert_score`,
  ADD COLUMN `admin_approved` tinyint(1) DEFAULT 0 COMMENT '管理员是否已审批: 0-未审批, 1-已审批通过, 2-已审批拒绝' AFTER `expert_feedback`,
  ADD COLUMN `admin_feedback` text COMMENT '管理员审批意见' AFTER `admin_approved`;

