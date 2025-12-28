-- 快速修复：添加缺失的字段到 project 表
-- 如果字段已存在会报错，可以忽略错误继续执行

USE concept_platform;

-- 添加 application_scenario 字段
ALTER TABLE `project` ADD COLUMN `application_scenario` text COMMENT '应用场景及案例（技术过往实际应用场景）';

-- 添加 cooperation_need 字段（如果上面报错说字段已存在，注释掉这行再执行）
-- ALTER TABLE `project` ADD COLUMN `cooperation_need` varchar(200) DEFAULT NULL COMMENT '合作需求（多选，JSON格式）';

-- 添加 intellectual_property 字段
-- ALTER TABLE `project` ADD COLUMN `intellectual_property` text COMMENT '知识产权情况';

-- 添加 other_supplement 字段
-- ALTER TABLE `project` ADD COLUMN `other_supplement` text COMMENT '其他补充信息';

-- 添加 budget 字段
-- ALTER TABLE `project` ADD COLUMN `budget` decimal(10,2) DEFAULT NULL COMMENT '预期经费(万元)';

