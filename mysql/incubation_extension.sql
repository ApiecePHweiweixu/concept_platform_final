/*
 * 孵化阶段相关表结构与评审多维度扩展
 * 在现有 concept_platform 库基础上执行
 */

USE concept_platform;

-- 1. 扩展 review 表为多维度评分
-- 总分 = 可行性 * 0.4 + 深度 * 0.3 + 拓展度 * 0.3

ALTER TABLE `review`
  ADD COLUMN `score_feasibility` int DEFAULT NULL COMMENT '可行性评分(0-100)' AFTER `score`,
  ADD COLUMN `score_depth` int DEFAULT NULL COMMENT '技术/研究深度评分(0-100)' AFTER `score_feasibility`,
  ADD COLUMN `score_extension` int DEFAULT NULL COMMENT '拓展度/应用前景评分(0-100)' AFTER `score_depth`,
  ADD COLUMN `total_score` int DEFAULT NULL COMMENT '加权总分(0-100)' AFTER `score_extension`;

-- 2. 为项目状态增加孵化阶段：4-待入孵, 5-孵化中, 6-已毕业
ALTER TABLE `project`
  MODIFY `status` int DEFAULT '0' COMMENT '状态: 0-草稿, 1-待初审, 2-评审中, 3-已入库, 4-待入孵, 5-孵化中, 6-已毕业, 9-已驳回';

-- 3. 孵化项目主表（孵化工作台）
DROP TABLE IF EXISTS `incubation_project`;
CREATE TABLE `incubation_project` (
  `incubation_id` int NOT NULL AUTO_INCREMENT COMMENT '孵化项目ID',
  `project_id` int NOT NULL COMMENT '关联概念验证项目ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '孵化状态: 0-待签署协议, 1-孵化中, 2-已毕业, 3-终止',
  `agreement_url` varchar(500) DEFAULT NULL COMMENT '孵化服务协议附件URL',
  `info_form_json` json DEFAULT NULL COMMENT '入孵信息表（团队、资源需求等，JSON存储）',
  `project_manager_id` int DEFAULT NULL COMMENT '项目经理用户ID(ADMIN)',
  `mentor_ids` varchar(255) DEFAULT NULL COMMENT '导师ID列表，逗号分隔',
  `graduate_status` int NOT NULL DEFAULT 0 COMMENT '毕业状态: 0-未申请, 1-待评审, 2-已通过, 3-未通过',
  `graduate_package_url` varchar(500) DEFAULT NULL COMMENT '毕业材料包附件URL',
  `graduate_desc` text COMMENT '毕业申请说明/最终产出摘要',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '入孵创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近更新时间',
  PRIMARY KEY (`incubation_id`),
  UNIQUE KEY `uk_incubation_project` (`project_id`),
  CONSTRAINT `fk_incubation_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孵化项目主表';

-- 4. 孵化里程碑表
DROP TABLE IF EXISTS `incubation_milestone`;
CREATE TABLE `incubation_milestone` (
  `milestone_id` int NOT NULL AUTO_INCREMENT COMMENT '里程碑ID',
  `incubation_id` int NOT NULL COMMENT '孵化项目ID',
  `name` varchar(200) NOT NULL COMMENT '里程碑名称',
  `description` text COMMENT '目标描述',
  `deliverables` text COMMENT '需提交的交付物清单（文本或JSON说明）',
  `deadline` datetime DEFAULT NULL COMMENT '截止日期',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态: 0-未开始, 1-进行中, 2-待审核, 3-通过, 4-需修订',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`milestone_id`),
  KEY `idx_incubation` (`incubation_id`),
  CONSTRAINT `fk_milestone_incubation` FOREIGN KEY (`incubation_id`) REFERENCES `incubation_project` (`incubation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孵化项目里程碑';

-- 5. 里程碑交付物与进展报告表
DROP TABLE IF EXISTS `incubation_milestone_report`;
CREATE TABLE `incubation_milestone_report` (
  `report_id` int NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `milestone_id` int NOT NULL COMMENT '里程碑ID',
  `project_id` int NOT NULL COMMENT '项目ID冗余，便于查询',
  `submitter_id` int NOT NULL COMMENT '提交人(创业团队成员/申报人)',
  `content` text COMMENT '进展报告正文',
  `attachments` text COMMENT '附件列表(JSON存储文件URL、代码仓链接等)',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已通过, 2-需修订',
  `mentor_feedback` text COMMENT '导师/项目经理反馈（文本）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`),
  KEY `idx_milestone` (`milestone_id`),
  CONSTRAINT `fk_report_milestone` FOREIGN KEY (`milestone_id`) REFERENCES `incubation_milestone` (`milestone_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孵化里程碑进展报告';

-- 6. 资源申请工单表
DROP TABLE IF EXISTS `incubation_resource_request`;
CREATE TABLE `incubation_resource_request` (
  `request_id` int NOT NULL AUTO_INCREMENT COMMENT '资源申请ID',
  `incubation_id` int NOT NULL COMMENT '孵化项目ID',
  `project_id` int NOT NULL COMMENT '项目ID冗余，便于前端展示',
  `requester_id` int NOT NULL COMMENT '申请人（创业者）',
  `type` varchar(50) NOT NULL COMMENT '资源类型: TECH, IP, MARKET, FUND 等',
  `title` varchar(200) NOT NULL COMMENT '申请标题',
  `description` text COMMENT '申请详情',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已完成, 3-已拒绝',
  `handler_id` int DEFAULT NULL COMMENT '当前处理人（项目经理或资源负责人）',
  `history_json` json DEFAULT NULL COMMENT '处理过程与沟通记录(JSON数组)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`),
  KEY `idx_incubation` (`incubation_id`),
  CONSTRAINT `fk_request_incubation` FOREIGN KEY (`incubation_id`) REFERENCES `incubation_project` (`incubation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孵化阶段资源申请工单';


