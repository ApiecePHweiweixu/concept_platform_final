-- 1. 创建资源库存台账表
CREATE TABLE IF NOT EXISTS `resource_inventory` (
  `resource_id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `resource_name` varchar(100) NOT NULL COMMENT '资源名称（如：A100算力、孵化启动金、中关村办公室）',
  `resource_type` varchar(50) NOT NULL COMMENT '类型：FUND(资金), COMPUTING(算力), SPACE(场地), SERVICE(服务)',
  `total_quota` decimal(15,2) COMMENT '总额度/总量',
  `remaining_quota` decimal(15,2) COMMENT '剩余额度/可用量',
  `unit` varchar(20) COMMENT '单位（如：万元, 核, 平方米, 项）',
  `description` varchar(255) COMMENT '资源详情描述',
  `status` int DEFAULT 1 COMMENT '1-启用, 0-禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台资源库存表';

-- 2. 扩展资源申请表，支持实体关联
-- 增加 allocated_resource_id (分配的资源ID) 和 allocated_amount (实际分配数量)
ALTER TABLE `incubation_resource_request` 
ADD COLUMN `allocated_resource_id` int DEFAULT NULL COMMENT '关联库存资源ID',
ADD COLUMN `allocated_amount` decimal(15,2) DEFAULT NULL COMMENT '实际分配数额';

-- 3. 初始化平台初始资源数据（管理员预置）
INSERT INTO `resource_inventory` (`resource_name`, `resource_type`, `total_quota`, `remaining_quota`, `unit`, `description`) VALUES 
('概念验证专项启动金', 'FUND', 500.00, 500.00, '万元', '用于支持入孵项目的初期研发与办公开支'),
('阿里云 A100 算力集群', 'COMPUTING', 128.00, 128.00, '核', '高性能计算资源，单节点支持8路A100'),
('图灵数智空间-独立办公室', 'SPACE', 20.00, 20.00, '间', '位于海淀区中关村，精装修带办公家具'),
('图灵数智空间-共享工位', 'SPACE', 100.00, 100.00, '个', '开放式办公区域，适合初创团队'),
('知识产权申请全流程服务', 'SERVICE', 50.00, 50.00, '项', '包含专利、软著代理及加急服务');

