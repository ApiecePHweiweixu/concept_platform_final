# 🚀 概念验证与孵化平台 (Concept Verification & Incubation Platform)

> 数据库系统概论课程大作业 - 2025

## 📖 项目简介

本项目是一个基于 B/S 架构的**概念验证与孵化平台**，用于管理科技项目的**申报、评审、孵化**全流程。

### 核心功能

1. **概念验证阶段**
   - 项目申报：支持多领域、多合作需求的项目提交
   - 多维度评审：专家从可行性、深度、拓展度三个维度进行加权评分
   - 自动入库：评分通过的项目自动进入孵化平台

2. **孵化平台阶段**
   - 入孵管理：管理员指派项目经理和导师，自动创建里程碑
   - 里程碑管理：中期审核和最终定型两个关键节点
   - 审批流程：申报人提交 → 管理员审批 → 专家打分
   - 资源申请：支持技术咨询、知识产权、市场渠道、融资、拨款等多种资源
   - 成功案例：完成孵化的项目进入公共展示和招标界面

3. **角色权限**
   - **申报人**：提交项目、查看我的项目、提交孵化材料、申请资源
   - **专家**：评审打分、查看辅导项目、处理资源申请、专家打分
   - **管理员**：审核项目、管理孵化项目、审批里程碑报告、指派资源

---

## 🛠 技术栈 (Tech Stack)

### 前端 (Frontend)
- **框架**: Vue 3 + Vite
- **UI 库**: Element Plus
- **网络请求**: Axios
- **图表**: ECharts
- **开发环境**: Node.js v18+ (推荐 v20+)

### 后端 (Backend)
- **核心框架**: Spring Boot 3.4.x
- **语言**: Java (JDK 21)
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.0+
- **构建工具**: Maven

---

## ⚡️ 快速开始 (Quick Start)

### 前置要求

- **MySQL 8.0+**：确保已安装并运行
- **JDK 21**：确保已安装并配置环境变量
- **Node.js v18+**：推荐使用 v20+
- **Maven 3.6+**：用于后端依赖管理
- **IntelliJ IDEA**：推荐用于后端开发（或 VS Code）

---

## 📦 数据库初始化 (Database Setup)

**重要**：请按照以下顺序执行 SQL 脚本，确保数据库结构完整。

> **路径说明**：以下示例中的路径 `D:\githubprojects\Concept_Platform` 为示例路径，请根据你的实际项目路径进行修改。
> - Windows 系统：通常为 `D:\githubprojects\Concept_Platform` 或 `C:\Users\你的用户名\Concept_Platform`
> - Linux/Mac 系统：通常为 `/home/你的用户名/Concept_Platform` 或 `/Users/你的用户名/Concept_Platform`
> 
> **快速获取项目路径**：
> - Windows：在项目根目录打开 PowerShell，执行 `pwd` 或 `Get-Location`
> - Linux/Mac：在项目根目录打开终端，执行 `pwd`

### 步骤 1：基础数据库初始化

在 MySQL 中执行 `mysql/db_init.sql`，这将创建：
- 数据库 `concept_platform`
- 基础表结构（用户表、项目表、评审表等）
- 初始化测试数据

**方式一：在 MySQL 客户端中使用绝对路径执行**

```sql
-- Windows 系统（假设项目在 D:\githubprojects\Concept_Platform）
SOURCE D:/githubprojects/Concept_Platform/mysql/db_init.sql;

-- Linux/Mac 系统（假设项目在 /home/user/Concept_Platform）
-- SOURCE /home/user/Concept_Platform/mysql/db_init.sql;
```

**方式二：使用命令行执行（推荐）**

```bash
# Windows 系统（在项目根目录执行）
mysql -u root -p < D:\githubprojects\Concept_Platform\mysql\db_init.sql

# Linux/Mac 系统
# mysql -u root -p < /home/user/Concept_Platform/mysql/db_init.sql
```

**方式三：在 Navicat 等工具中**
- 打开 SQL 文件：`D:\githubprojects\Concept_Platform\mysql\db_init.sql`
- 复制文件内容，在查询窗口中执行

### 步骤 2：孵化平台扩展

执行 `mysql/incubation_extension.sql`，创建孵化相关表：
- `incubation_project`：孵化项目表
- `incubation_milestone`：里程碑表
- `incubation_milestone_report`：里程碑报告表
- `incubation_resource_request`：资源申请表

**方式一：在 MySQL 客户端中使用绝对路径执行**

```sql
-- Windows 系统
SOURCE D:/githubprojects/Concept_Platform/mysql/incubation_extension.sql;

-- Linux/Mac 系统
-- SOURCE /home/user/Concept_Platform/mysql/incubation_extension.sql;
```

**方式二：使用命令行执行（推荐）**

```bash
# Windows 系统（在项目根目录执行）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\incubation_extension.sql

# Linux/Mac 系统
# mysql -u root -p concept_platform < /home/user/Concept_Platform/mysql/incubation_extension.sql
```

### 步骤 3：审批流程扩展

执行 `mysql/incubation_approval_extension.sql`，添加审批流程字段：
- 为 `incubation_milestone_report` 表添加 `admin_approved`、`expert_score` 等字段

**方式一：在 MySQL 客户端中使用绝对路径执行**

```sql
-- Windows 系统
SOURCE D:/githubprojects/Concept_Platform/mysql/incubation_approval_extension.sql;

-- Linux/Mac 系统
-- SOURCE /home/user/Concept_Platform/mysql/incubation_approval_extension.sql;
```

**方式二：使用命令行执行（推荐）**

```bash
# Windows 系统（在项目根目录执行）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\incubation_approval_extension.sql

# Linux/Mac 系统
# mysql -u root -p concept_platform < /home/user/Concept_Platform/mysql/incubation_approval_extension.sql
```

### 步骤 4：成功案例扩展

执行 `mysql/success_case_extension.sql`，创建成功案例表：
- `success_case`：成功案例展示表

**方式一：在 MySQL 客户端中使用绝对路径执行**

```sql
-- Windows 系统
SOURCE D:/githubprojects/Concept_Platform/mysql/success_case_extension.sql;

-- Linux/Mac 系统
-- SOURCE /home/user/Concept_Platform/mysql/success_case_extension.sql;
```

**方式二：使用命令行执行（推荐）**

```bash
# Windows 系统（在项目根目录执行）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\success_case_extension.sql

# Linux/Mac 系统
# mysql -u root -p concept_platform < /home/user/Concept_Platform/mysql/success_case_extension.sql
```

### 步骤 5：工作流扩展（重要！）

执行 `mysql/incubation_workflow_extension.sql`，完善审批机制：
- 为里程碑报告表添加项目描述字段
- 添加管理员通知相关字段
- 将"毕业"相关字段改为"落成"

**方式一：在 MySQL 客户端中使用绝对路径执行**

```sql
-- Windows 系统
SOURCE D:/githubprojects/Concept_Platform/mysql/incubation_workflow_extension.sql;

-- Linux/Mac 系统
-- SOURCE /home/user/Concept_Platform/mysql/incubation_workflow_extension.sql;
```

**方式二：使用命令行执行（推荐）**

```bash
# Windows 系统（在项目根目录执行）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\incubation_workflow_extension.sql

# Linux/Mac 系统
# mysql -u root -p concept_platform < /home/user/Concept_Platform/mysql/incubation_workflow_extension.sql
```

### 步骤 6：落成审批流程扩展（重要！）

执行 `mysql/completion_review_extension.sql`，完善落成审批流程：
- 为成功案例表添加专家评分字段
- 细化落成状态枚举（支持 admin 初审 + expert 终审两阶段）

**方式一：在 MySQL 客户端中使用绝对路径执行**

```sql
-- Windows 系统
SOURCE D:/githubprojects/Concept_Platform/mysql/completion_review_extension.sql;

-- Linux/Mac 系统
-- SOURCE /home/user/Concept_Platform/mysql/completion_review_extension.sql;
```

**方式二：使用命令行执行（推荐）**

```bash
# Windows 系统（在项目根目录执行）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\completion_review_extension.sql

# Linux/Mac 系统
# mysql -u root -p concept_platform < /home/user/Concept_Platform/mysql/completion_review_extension.sql
```

### 步骤 7：其他扩展（如需要）

如果遇到字段缺失问题，可以执行：
- `mysql/add_missing_columns.sql`：补充缺失字段
- `mysql/incubation_report_extension.sql`：报告扩展字段

### 完整初始化脚本（推荐）

如果数据库是全新的，可以按顺序执行以下脚本：

**Windows 系统（在项目根目录执行）**

```bash
# 1. 基础数据库（创建数据库和基础表）
mysql -u root -p < D:\githubprojects\Concept_Platform\mysql\db_init.sql

# 2. 孵化平台扩展（创建孵化相关表）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\incubation_extension.sql

# 3. 审批流程扩展（添加审批字段）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\incubation_approval_extension.sql

# 4. 成功案例扩展（创建成功案例表）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\success_case_extension.sql

# 5. 工作流扩展（完善审批机制）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\incubation_workflow_extension.sql

# 6. 落成审批流程扩展（admin初审 + expert终审）
mysql -u root -p concept_platform < D:\githubprojects\Concept_Platform\mysql\completion_review_extension.sql
```

**Linux/Mac 系统（请根据实际路径修改）**

```bash
# 1. 基础数据库（创建数据库和基础表）
mysql -u root -p < /home/user/Concept_Platform/mysql/db_init.sql

# 2. 孵化平台扩展（创建孵化相关表）
mysql -u root -p concept_platform < /home/user/Concept_Platform/mysql/incubation_extension.sql

# 3. 审批流程扩展（添加审批字段）
mysql -u root -p concept_platform < /home/user/Concept_Platform/mysql/incubation_approval_extension.sql

# 4. 成功案例扩展（创建成功案例表）
mysql -u root -p concept_platform < /home/user/Concept_Platform/mysql/success_case_extension.sql
```

> **注意**：请将路径中的 `D:\githubprojects\Concept_Platform` 或 `/home/user/Concept_Platform` 替换为你的实际项目路径。

### 数据库初始化检查清单

执行完所有脚本后，可以使用以下 SQL 检查表是否创建成功：

```sql
USE concept_platform;

-- 检查核心表是否存在
SHOW TABLES LIKE 'sys_user';
SHOW TABLES LIKE 'project';
SHOW TABLES LIKE 'review';
SHOW TABLES LIKE 'incubation_project';
SHOW TABLES LIKE 'incubation_milestone';
SHOW TABLES LIKE 'incubation_milestone_report';
SHOW TABLES LIKE 'incubation_resource_request';
SHOW TABLES LIKE 'success_case';

-- 检查孵化报告表是否有审批字段
DESC incubation_milestone_report;
-- 应该看到：admin_approved, expert_score, expert_feedback, admin_feedback 等字段
```

---

## 🔧 后端启动 (Backend Setup)

### 1. 打开项目

使用 **IntelliJ IDEA** 打开 `concept-platform` 文件夹。

### 2. 配置数据库连接

打开 `src/main/resources/application.properties`，修改数据库配置：

```properties
# 修改为你的 MySQL 密码
spring.datasource.password=你的MySQL密码

# 如果 MySQL 端口不是 3306，也需要修改
spring.datasource.url=jdbc:mysql://localhost:3306/concept_platform?...
```

### 3. 下载依赖

- 在 IntelliJ IDEA 右侧边栏找到 **Maven** 工具窗口
- 点击刷新按钮（🔄）下载所有依赖
- 或使用命令行：`mvn clean install`

### 4. 启动后端服务

- 找到 `src/main/java/com/example/concept_platform/ConceptPlatformApplication.java`
- 右键选择 **Run 'ConceptPlatformApplication'**
- 或使用命令行：`mvn spring-boot:run`

### 5. 验证启动成功

看到以下日志表示启动成功：
```
Started ConceptPlatformApplication in x.xx seconds
```

后端服务默认运行在：**http://localhost:8080**

---

## 🎨 前端启动 (Frontend Setup)

### 1. 进入前端目录

```bash
cd concept-platform-frontend/frontend
```

### 2. 安装依赖（仅第一次需要）

```bash
npm install
```

如果遇到网络问题，可以使用国内镜像：
```bash
npm install --registry=https://registry.npmmirror.com
```

### 3. 启动开发服务器

```bash
npm run dev
```

### 4. 访问前端

打开浏览器访问：**http://localhost:5173**

> **注意**：前端配置了 API 代理，但当前 `request.js` 中直接使用 `baseURL: 'http://localhost:8080'`。如果遇到 CORS 跨域问题，可以：
> 1. 修改 `src/utils/request.js`，将 `baseURL` 改为 `/api`
> 2. 或者确保后端已配置 CORS（`CorsConfig.java` 已处理）

---

## 🔑 测试账号 (Test Accounts)

| 角色 | 账号 | 密码 | 权限说明 |
| :--- | :--- | :--- | :--- |
| **申报人** | student | 123456 | 提交项目，查看我的项目，提交孵化材料 |
| **申报人** | applicant1 | 123456 | 提交项目，查看我的项目 |
| **申报人** | applicant2 | 123456 | 提交项目，查看我的项目 |
| **申报人** | applicant15 | 123456 | 提交项目，查看我的项目 |
| **管理员** | admin | 123456 | 审核项目，管理孵化项目，审批里程碑报告 |
| **专家** | expert1 | 123456 | 评审打分，查看辅导项目，处理资源申请 |
| **专家** | expert2 | 123456 | 评审打分，查看辅导项目 |
| **专家** | expert3 | 123456 | 评审打分，查看辅导项目 |

> **注意**：申报人账号从 applicant1 到 applicant15 都有，密码均为 123456

---

## 📊 项目状态说明

### 概念验证阶段状态
- **0** - 草稿
- **1** - 待初审
- **2** - 评审中
- **3** - 已入库（通过评审，进入孵化平台）
- **9** - 已驳回

### 孵化阶段状态
- **4** - 待入孵（已通过评审，等待管理员准予入孵）
- **5** - 孵化中（已签署协议，正在进行孵化）
- **6** - 已毕业（完成孵化，进入成功案例库）

---

## 🗂 目录结构说明

```
Concept_Platform/
├── mysql/                          # 数据库脚本目录
│   ├── db_init.sql                 # 基础数据库初始化（必执行）
│   ├── incubation_extension.sql    # 孵化平台扩展（必执行）
│   ├── incubation_approval_extension.sql  # 审批流程扩展（必执行）
│   ├── success_case_extension.sql  # 成功案例扩展（必执行）
│   └── ...                         # 其他扩展脚本
│
├── concept-platform/               # 后端项目
│   ├── src/main/java/
│   │   └── com/example/concept_platform/
│   │       ├── controller/         # 接口层 (API)
│   │       ├── service/            # 业务逻辑层
│   │       ├── mapper/             # 数据库操作层
│   │       ├── entity/             # 数据库实体类
│   │       └── ConceptPlatformApplication.java  # 启动类
│   └── src/main/resources/
│       └── application.properties  # 配置文件（需修改数据库密码）
│
└── concept-platform-frontend/      # 前端项目
    └── frontend/
        ├── src/
        │   ├── api/                 # 后端接口定义
        │   ├── views/               # 页面文件
        │   │   ├── LoginView.vue    # 登录页
        │   │   ├── MyProjects.vue   # 我的项目（申报人）
        │   │   ├── ExpertReviewView.vue  # 专家评审页
        │   │   ├── MyIncubationProjects.vue  # 我的孵化项目（申报人）
        │   │   ├── IncubationManageView.vue  # 孵化项目管理（管理员）
        │   │   ├── MentorProjectsView.vue    # 我的辅导项目（专家）
        │   │   └── SuccessCaseView.vue        # 成功案例库
        │   ├── router/              # 路由配置
        │   ├── layout/              # 布局组件
        │   └── utils/               # 工具类（Axios封装等）
        ├── package.json             # 依赖配置
        └── vite.config.js           # Vite 配置
```

---

## 🔄 功能流程说明

### 1. 概念验证流程

```
申报人提交项目 
  → 管理员初审 
  → 专家多维度评审（可行性、深度、拓展度）
  → 自动计算总分（加权平均）
  → 平均分≥60分 → 项目状态变为"已入库"
  → 自动创建孵化项目记录和默认里程碑
```

### 2. 孵化流程

```
管理员准予入孵 
  → 指派项目经理和导师（自动创建中期审核、最终定型里程碑）
  → 申报人签署协议并填写信息表
  → 申报人提交中期进展报告
  → 管理员审批报告
  → 专家打分（0-100分）
  → 申报人提交最终成果材料
  → 管理员审批最终报告
  → 专家打分
  → 项目毕业，进入成功案例库
```

### 3. 资源申请流程

```
申报人申请资源（技术咨询、知识产权、市场渠道、融资等）
  → 项目经理/导师处理申请
  → 更新处理状态和说明
  → 申报人查看处理结果
```

---

## 🐛 常见问题 (Troubleshooting)

### 数据库连接失败

1. 检查 MySQL 服务是否启动
2. 检查 `application.properties` 中的数据库配置
3. 确认数据库密码是否正确
4. 确认数据库 `concept_platform` 是否已创建

### 前端无法访问后端 API

1. 确认后端服务已启动（http://localhost:8080）
2. 检查浏览器控制台是否有 CORS 错误
   - 如果出现 CORS 错误，检查后端 `CorsConfig.java` 是否已正确配置
   - 或修改前端 `src/utils/request.js`，将 `baseURL` 改为 `/api` 使用代理
3. 检查网络请求的 URL 是否正确（应为 `http://localhost:8080/api/xxx`）

### 依赖下载失败

**后端（Maven）**：
- 检查网络连接
- 尝试使用国内镜像源（在 `pom.xml` 或 `settings.xml` 中配置）

**前端（npm）**：
```bash
npm install --registry=https://registry.npmmirror.com
```

### 里程碑为空

1. 确认已执行 `incubation_extension.sql`
2. 确认管理员已指派项目经理和导师（会自动创建里程碑）
3. 检查 `incubation_milestone` 表中是否有数据

### 专家无法看到孵化项目

1. 确认专家已被指派为导师（在管理员界面指派）
2. 确认项目状态为"孵化中"（status=5）
3. 检查专家账号的 `field` 字段是否与项目的 `tech_domain` 匹配

---

## 📝 开发注意事项

1. **数据库修改**：如果修改了数据库结构，记得更新对应的 SQL 脚本和实体类
2. **API 接口**：新增接口时，记得在前端 `api/` 目录下添加对应的调用函数
3. **状态管理**：项目状态变更时，确保相关表的状态字段同步更新
4. **权限控制**：确保不同角色只能访问对应的功能页面

---

## 📄 许可证

本项目为课程作业项目，仅供学习使用。

---

## 👥 贡献者

- 数据库系统概论课程小组

---

## ✅ 启动检查清单

在开始使用前，请确认以下事项：

### 数据库
- [ ] MySQL 服务已启动
- [ ] 已执行 `db_init.sql` 创建基础数据库
- [ ] 已执行 `incubation_extension.sql` 创建孵化表
- [ ] 已执行 `incubation_approval_extension.sql` 添加审批字段
- [ ] 已执行 `success_case_extension.sql` 创建成功案例表
- [ ] 数据库 `concept_platform` 存在且包含所有表

### 后端
- [ ] JDK 21 已安装并配置环境变量
- [ ] Maven 依赖已下载完成
- [ ] `application.properties` 中的数据库密码已修改
- [ ] 后端服务成功启动（http://localhost:8080）

### 前端
- [ ] Node.js v18+ 已安装
- [ ] 已执行 `npm install` 安装依赖
- [ ] 前端服务成功启动（http://localhost:5173）
- [ ] 可以正常访问登录页面

### 功能测试
- [ ] 可以使用测试账号登录
- [ ] 申报人可以提交项目
- [ ] 专家可以评审项目
- [ ] 管理员可以审核项目
- [ ] 通过评审的项目出现在孵化平台
- [ ] 管理员可以指派项目经理和导师
- [ ] 里程碑自动创建成功

---

## 📞 联系方式

如有问题，请通过以下方式联系：
- 提交 Issue
- 联系项目维护者

---

## 📚 相关文档

- [前端 README](concept-platform-frontend/frontend/README.md)
- [数据库脚本说明](mysql/)

---

**最后更新**：2026-01-07
