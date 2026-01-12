-- 1. 为 project 表增加 final_score 字段
ALTER TABLE `project` ADD COLUMN `final_score` DECIMAL(5,2) DEFAULT NULL COMMENT '评审最终平均分';

-- 2. 注入更多专家数据（每个领域 5 名专家）
-- 人工智能领域
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `company`, `field`) VALUES 
('ai_expert1', '123456', '人工智能专家1', 'EXPERT', '清华大学', '人工智能'),
('ai_expert2', '123456', '人工智能专家2', 'EXPERT', '北京大学', '人工智能'),
('ai_expert3', '123456', '人工智能专家3', 'EXPERT', '中科院自动化所', '人工智能'),
('ai_expert4', '123456', '人工智能专家4', 'EXPERT', '商汤科技', '人工智能'),
('ai_expert5', '123456', '人工智能专家5', 'EXPERT', '旷视科技', '人工智能');

-- 大数据与云计算领域
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `company`, `field`) VALUES 
('bigdata_expert1', '123456', '大数据专家1', 'EXPERT', '阿里云', '大数据与云计算'),
('bigdata_expert2', '123456', '大数据专家2', 'EXPERT', '腾讯云', '大数据与云计算'),
('bigdata_expert3', '123456', '大数据专家3', 'EXPERT', '华为云', '大数据与云计算'),
('bigdata_expert4', '123456', '大数据专家4', 'EXPERT', '百度智能云', '大数据与云计算'),
('bigdata_expert5', '123456', '大数据专家5', 'EXPERT', '京东云', '大数据与云计算');

-- 信息安全领域
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `company`, `field`) VALUES 
('sec_expert1', '123456', '信息安全专家1', 'EXPERT', '奇安信', '信息安全'),
('sec_expert2', '123456', '信息安全专家2', 'EXPERT', '深信服', '信息安全'),
('sec_expert3', '123456', '信息安全专家3', 'EXPERT', '启明星辰', '信息安全'),
('sec_expert4', '123456', '信息安全专家4', 'EXPERT', '三六零', '信息安全'),
('sec_expert5', '123456', '信息安全专家5', 'EXPERT', '绿盟科技', '信息安全');

-- 物联网与嵌入式技术领域
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `company`, `field`) VALUES 
('iot_expert1', '123456', '物联网专家1', 'EXPERT', '北京航空航天大学', '物联网与嵌入式技术'),
('iot_expert2', '123456', '物联网专家2', 'EXPERT', '北京邮电大学', '物联网与嵌入式技术'),
('iot_expert3', '123456', '物联网专家3', 'EXPERT', '小米公司', '物联网与嵌入式技术'),
('iot_expert4', '123456', '物联网专家4', 'EXPERT', '涂鸦智能', '物联网与嵌入式技术'),
('iot_expert5', '123456', '物联网专家5', 'EXPERT', '海尔智家', '物联网与嵌入式技术');

-- 数字人文技术领域
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `company`, `field`) VALUES 
('dh_expert1', '123456', '数字人文专家1', 'EXPERT', '北京师范大学', '数字人文技术'),
('dh_expert2', '123456', '数字人文专家2', 'EXPERT', '中国人民大学', '数字人文技术'),
('dh_expert3', '123456', '数字人文专家3', 'EXPERT', '国家图书馆', '数字人文技术'),
('dh_expert4', '123456', '数字人文专家4', 'EXPERT', '故宫博物院', '数字人文技术'),
('dh_expert5', '123456', '数字人文专家5', 'EXPERT', '敦煌研究院', '数字人文技术');

-- 其他领域
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `company`, `field`) VALUES 
('other_expert1', '123456', '综合专家1', 'EXPERT', '投资机构A', '其他'),
('other_expert2', '123456', '综合专家2', 'EXPERT', '孵化器B', '其他'),
('other_expert3', '123456', '综合专家3', 'EXPERT', '产业园C', '其他'),
('other_expert4', '123456', '综合专家4', 'EXPERT', '科技局D', '其他'),
('other_expert5', '123456', '综合专家5', 'EXPERT', '大学管理处E', '其他');

