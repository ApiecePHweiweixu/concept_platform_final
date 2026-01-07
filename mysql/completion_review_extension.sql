/*
 * 落成审批流程扩展
 * 增加 expert 终审打分环节
 */

USE concept_platform;

-- 为 success_case 表添加专家评分字段（可选，用于记录终审评分）
SET @dbname = DATABASE();
SET @tablename = 'success_case';

-- 添加 expert_score 字段
SET @columnname = 'expert_score';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `expert_score` int DEFAULT NULL COMMENT ''专家终审评分(0-100)'' AFTER `view_count`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加 expert_feedback 字段
SET @columnname = 'expert_feedback';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `expert_feedback` text COMMENT ''专家终审反馈意见'' AFTER `expert_score`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 为 incubation_project 表修改落成状态字段注释（细化审批流程）
-- 0-未申请, 1-已申请待admin初审, 2-admin已通过待expert终审, 3-expert已通过(已落成), 4-admin拒绝, 5-expert拒绝
ALTER TABLE `incubation_project`
  MODIFY COLUMN `completion_status` int DEFAULT 0 COMMENT '落成状态: 0-未申请, 1-已申请待admin初审, 2-admin已通过待expert终审, 3-expert已通过(已落成), 4-admin拒绝, 5-expert拒绝';

