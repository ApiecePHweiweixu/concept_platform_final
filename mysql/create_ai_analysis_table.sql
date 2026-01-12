-- 创建 AI 辅助分析结果表
CREATE TABLE IF NOT EXISTS `project_ai_analysis` (
  `analysis_id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id` int NOT NULL COMMENT '关联项目ID',
  `innovation_score` int COMMENT '创新性评分(0-100)',
  `feasibility_score` int COMMENT '技术可行性评分(0-100)',
  `market_score` int COMMENT '市场潜力评分(0-100)',
  `analysis_summary` text COMMENT '综合评价',
  `risk_warning` text COMMENT '风险提示',
  `raw_response` text COMMENT '大模型原始JSON结果',
  `status` int DEFAULT 0 COMMENT '状态: 0-进行中, 1-成功, 2-失败',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_project_id` (`project_id`),
  CONSTRAINT `fk_ai_analysis_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI辅助分析结果表';

