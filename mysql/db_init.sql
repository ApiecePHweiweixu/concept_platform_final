/*
 Navicat MySQL Data Transfer
 Source Database       : concept_platform
 Project Name          : 概念验证平台数据库初始化
 Date                  : 2025-12-15 (Final Version)
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 创建并使用数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `concept_platform` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `concept_platform`;

-- ----------------------------
-- 2. 表结构：用户表 (sys_user)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '密码 (默认123456)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role` varchar(20) NOT NULL COMMENT '角色: APPLICANT(申报人), EXPERT(专家), ADMIN(管理员)',
  `field` varchar(50) DEFAULT NULL COMMENT '擅长领域/职称 (仅专家需要)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 3. 表结构：项目申报表 (project)
-- ----------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `project_id` int NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_name` varchar(200) NOT NULL COMMENT '项目名称',
  `tech_domain` varchar(50) DEFAULT NULL COMMENT '技术领域 (如: 人工智能)',
  `budget` decimal(10,2) DEFAULT NULL COMMENT '预期经费(万元)',
  `description` text COMMENT '项目简介',
  `attachment_url` varchar(255) DEFAULT NULL COMMENT '附件文件路径/URL',
  `status` int DEFAULT '0' COMMENT '状态: 0-草稿, 1-待初审, 2-评审中, 3-已入库, 9-已驳回',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回理由',
  `audit_time` datetime DEFAULT NULL COMMENT '管理员审核时间',
  `applicant_id` int NOT NULL COMMENT '关联申报人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`project_id`),
  KEY `idx_applicant` (`applicant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='项目信息表';

-- ----------------------------
-- 4. 表结构：专家评审表 (review)
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `review_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` int NOT NULL COMMENT '项目ID',
  `expert_id` int NOT NULL COMMENT '专家ID',
  `score` int DEFAULT NULL COMMENT '评分 (0-100)',
  `comments` text COMMENT '专家评语',
  `review_time` datetime DEFAULT NULL COMMENT '评审时间',
  `status` int DEFAULT '0' COMMENT '状态: 0-未评, 1-已评',
  PRIMARY KEY (`review_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_expert` (`expert_id`),
  CONSTRAINT `fk_review_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_expert` FOREIGN KEY (`expert_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5000 DEFAULT CHARSET=utf8mb4 COMMENT='专家评审记录表';

-- ----------------------------
-- 5. 初始化数据：用户
-- ----------------------------
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `real_name`, `role`, `field`) VALUES 
(1, 'student', '123456', '张同学', 'APPLICANT', NULL),
(2, 'admin', '123456', '王管理员', 'ADMIN', NULL),
(3, 'expert1', '123456', '李教授', 'EXPERT', '人工智能'),
(4, 'expert2', '123456', '张博士', 'EXPERT', '大数据分析'),
(5, 'expert3', '123456', '陈专家', 'EXPERT', '生物医药');

-- ----------------------------
-- 6. 初始化数据：示例项目
-- ----------------------------
INSERT INTO `project` (`project_id`, `project_name`, `tech_domain`, `description`, `status`, `applicant_id`) VALUES 
(1001, '基于大模型的代码辅助工具', '人工智能', '本项目旨在开发一款IDE插件...', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
