-- 快速修复：直接添加缺失的字段（如果已存在会报错，可以忽略）
USE concept_platform;

-- 添加 application_scenario 字段
ALTER TABLE `project` ADD COLUMN IF NOT EXISTS `application_scenario` text COMMENT '应用场景及案例（技术过往实际应用场景）';

-- 添加 cooperation_need 字段  
ALTER TABLE `project` ADD COLUMN IF NOT EXISTS `cooperation_need` varchar(200) DEFAULT NULL COMMENT '合作需求（多选，JSON格式）';

-- 添加 intellectual_property 字段
ALTER TABLE `project` ADD COLUMN IF NOT EXISTS `intellectual_property` text COMMENT '知识产权情况';

-- 添加 other_supplement 字段
ALTER TABLE `project` ADD COLUMN IF NOT EXISTS `other_supplement` text COMMENT '其他补充信息';

-- 添加 budget 字段
ALTER TABLE `project` ADD COLUMN IF NOT EXISTS `budget` decimal(10,2) DEFAULT NULL COMMENT '预期经费(万元)';

