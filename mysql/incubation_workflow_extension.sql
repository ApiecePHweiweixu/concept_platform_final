/*
 * 孵化工作流扩展
 * 完善审批机制：admin通知 -> 申报者提交 -> admin初审 -> expert打分 -> 落成展示
 */

USE concept_platform;

-- 为里程碑报告表添加项目描述字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'incubation_milestone_report';

-- 添加 project_description 字段
SET @columnname = 'project_description';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `project_description` text COMMENT ''项目描述（申报者填写）'' AFTER `content`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加 admin_notified 字段
SET @columnname = 'admin_notified';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `admin_notified` int NOT NULL DEFAULT 0 COMMENT ''管理员是否已通知: 0-未通知, 1-已通知'' AFTER `admin_feedback`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加 admin_notify_time 字段
SET @columnname = 'admin_notify_time';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `admin_notify_time` datetime DEFAULT NULL COMMENT ''管理员通知时间'' AFTER `admin_notified`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 修改孵化项目表，将"毕业"改为"落成"（如果字段存在）
SET @tablename = 'incubation_project';

-- 检查并重命名 graduate_status
SET @columnname = 'graduate_status';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' CHANGE COLUMN `graduate_status` `completion_status` int DEFAULT 0 COMMENT ''落成状态: 0-未申请, 1-已申请, 2-已落成, 3-已拒绝'';'),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

-- 检查并重命名 graduate_package_url
SET @columnname = 'graduate_package_url';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' CHANGE COLUMN `graduate_package_url` `completion_package_url` varchar(500) DEFAULT NULL COMMENT ''落成材料包URL'';'),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

-- 检查并重命名 graduate_desc
SET @columnname = 'graduate_desc';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' CHANGE COLUMN `graduate_desc` `completion_desc` text COMMENT ''落成描述'';'),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

-- 更新项目状态枚举（如果需要）
-- 注意：如果 project 表的状态字段需要修改，需要单独处理

