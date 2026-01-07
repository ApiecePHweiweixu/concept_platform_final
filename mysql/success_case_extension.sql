/*
 * 成功案例/公共展示招标界面扩展
 * 在现有孵化表基础上执行
 */

USE concept_platform;

-- 1. 成功案例表（已完成的孵化项目，可投入使用）
DROP TABLE IF EXISTS `success_case`;
CREATE TABLE `success_case` (
  `case_id` int NOT NULL AUTO_INCREMENT COMMENT '案例ID',
  `project_id` int NOT NULL COMMENT '关联项目ID',
  `incubation_id` int NOT NULL COMMENT '关联孵化项目ID',
  `case_name` varchar(200) NOT NULL COMMENT '案例名称（可脱敏）',
  `case_description` text COMMENT '案例描述（脱敏后的成果介绍）',
  `tech_domain` varchar(200) DEFAULT NULL COMMENT '技术领域（JSON格式）',
  `application_scenario` text COMMENT '应用场景',
  `intellectual_property` text COMMENT '知识产权情况',
  `cooperation_mode` varchar(200) DEFAULT NULL COMMENT '合作模式（技术授权/联合研发等，JSON格式）',
  `contact_info` varchar(500) DEFAULT NULL COMMENT '联系方式（脱敏后）',
  `display_status` int NOT NULL DEFAULT 1 COMMENT '展示状态: 1-公开展示, 0-隐藏',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '案例创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`case_id`),
  UNIQUE KEY `uk_case_project` (`project_id`),
  UNIQUE KEY `uk_case_incubation` (`incubation_id`),
  CONSTRAINT `fk_case_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_case_incubation` FOREIGN KEY (`incubation_id`) REFERENCES `incubation_project` (`incubation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成功案例/公共展示招标表';

-- 2. 为孵化项目表增加里程碑提醒日期字段（用于中期和终期提醒）
ALTER TABLE `incubation_milestone`
  ADD COLUMN `reminder_sent` tinyint(1) DEFAULT 0 COMMENT '是否已发送提醒' AFTER `deadline`;

