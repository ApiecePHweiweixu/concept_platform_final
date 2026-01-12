# 🚀 概念验证与项目孵化平台 (Concept Verification & Incubation Platform)

> 数据库系统概论课程大作业 - 2025（深度优化版）

## 📖 项目简介

本项目是一个基于 B/S 架构的**全生命周期管理平台**，不仅管理科技项目的申报与孵化，更引入了 **AI 辅助决策**、**多专家同行评议**以及**实物资源台账**等工业级功能。

### 核心功能亮点
1.  **AI 智能初筛**：集成 **智谱 AI (GLM-4.7)**，项目提交后异步生成创新性、可行性、市场潜力评分及风险提示。
2.  **严谨同行评议**：支持管理员按**技术领域精准指派** 1-3 名专家评分，系统自动计算加权平均分进行入库决策。
3.  **资源实体化管理**：建立资金、算力、场地实物台账。支持“申请-匹配库存-专家划拨-自动扣减”的资产管理闭环。
4.  **全流程状态机**：涵盖申报、AI评估、多专家评审、入库、协议签署、里程碑审核、资源调度、落成结项、成功案例展示。

---

## 🛠 技术栈 (Tech Stack)

### 前端 (Frontend)
- **核心**: Vue 3 + Vite
- **UI 库**: Element Plus (支持响应式与现代化交互)
- **图表**: ECharts (项目状态与资源分布可视化)

### 后端 (Backend)
- **框架**: Spring Boot 3.4.x (Java 21)
- **ORM**: MyBatis-Plus
- **异步**: Spring @Async (用于处理耗时 AI 任务)
- **AI 集成**: Zhipu AI (GLM-4.7) API

---

## 📦 数据库初始化 (Database Setup)

**重要**：请按照以下顺序执行 SQL 脚本，以确保系统所有高级功能（AI、资源、多专家）正常运行。

### 步骤 1：基础结构初始化
依次执行以下脚本（位于 `mysql/` 目录）：
1. `db_init.sql`：创建核心用户与项目表。
2. `incubation_extension.sql`：创建孵化相关表。
3. `success_case_extension.sql`：创建成功案例表。

### 步骤 2：高级功能扩展（必须执行）
1.  **多专家与领域注入**：执行 `mysql/update_experts_and_fields.sql`。
    *   为项目增加 `final_score` 字段。
    *   注入 30 名涵盖 AI、大数据、安全等领域的专业专家数据。
2.  **AI 审核扩展**：执行 `mysql/create_ai_analysis_table.sql`。
    *   创建 `project_ai_analysis` 表，存储大模型分析结果。
3.  **资源实体化扩展**：执行 `mysql/resource_physical_extension.sql`。
    *   创建 `resource_inventory` 实物资产台账表。
    *   建立资源申请与实物库存的关联关系。

---

## 🔧 环境配置与启动

### 1. 后端 API Key 配置
打开 `src/main/resources/application.properties`，配置你的智谱 AI 密钥：
```properties
# 修改为你真实的 API Key
zhipu.ai.api-key=你的智谱API密钥
zhipu.ai.model=glm-4.7
```

### 2. 启动服务
- **后端**：使用 IDEA 运行 `ConceptPlatformApplication.java` 或执行 `mvn spring-boot:run`。
- **前端**：进入 `frontend` 目录，执行 `npm install` 后运行 `npm run dev`。

---

## 🔄 核心业务流程说明

### 1. 申报与 AI 决策流
```
申请人提交 
  → [异步] AI 自动分析 (创新性/可行性/风险) 
  → 管理员根据领域指派 1-3 名匹配专家 
  → 专家独立评分 
  → 系统自动汇总均分 
  → 分数 >= 60 自动入库并开启孵化
```

### 2. 实物资源调度流
```
申请人提交资源申请 (如: 算力) 
  → 指导老师查看对应类型资源库存 (如: A100算力集群) 
  → 老师输入分配数额并完成 
  → 系统自动扣减剩余额度 
  → 申请人“我的资产”实时入账
```

---

## 🔑 测试账号

| 角色 | 账号 | 密码 | 说明 |
| :--- | :--- | :--- | :--- |
| **管理员** | admin | 123456 | 拥有全局审核、指派、资源维护权限 |
| **人工智能专家** | ai_expert1 | 123456 | 被系统自动匹配至 AI 领域项目 |
| **大数据专家** | bigdata_expert1 | 123456 | 负责云计算相关资源的划拨 |
| **申报人** | student | 123456 | 模拟提交项目并查看资产持有情况 |

---

## ✅ 启动检查清单
- [ ] 数据库 `project` 表已包含 `final_score` 字段。
- [ ] 数据库 `resource_inventory` 表已注入 10 条以上测试资产。
- [ ] 后端 `application.properties` 中 API Key 已填入。
- [ ] 访问 http://localhost:5173 登录管理员账号查看“项目审核”页面。

---
*本项目作为数据库系统概论课程作业，旨在展示 AI 驱动的现代化信息管理系统设计。*
