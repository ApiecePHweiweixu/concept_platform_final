# 完整项目生命周期数据流程文档

## 概述

本文档详细说明从**申报人在概念验证平台提交项目**开始，到**项目最终进入成功案例库**的完整数据流转过程，包括申报人、管理员（Admin）、专家（Expert）三个角色的操作路径和处理逻辑。

---

## 一、概念验证阶段（Concept Verification）

### 1.1 申报人视角：项目提交与状态跟踪

**操作路径：** `我的项目` → `新建申报` / `项目表单`

**数据流转：**

1. **项目创建**
   - 申报人填写项目基本信息（项目名称、技术领域、应用场景、知识产权、合作需求等）
   - 调用接口：`POST /api/project/submit`
   - **数据库操作：**
     - 在 `project` 表中插入新记录
     - `project.status = 0`（草稿）或 `1`（待初审）
     - `project.applicant_id` = 当前登录用户ID

2. **项目状态查看**
   - 申报人在 `我的项目` 页面查看项目列表
   - 调用接口：`GET /api/project/my?applicantId={id}`
   - **状态含义：**
     - `0`: 草稿
     - `1`: 待初审
     - `2`: 评审中（已分配给专家）
     - `3`: 已入库（通过概念验证）
     - `4`: 待入孵（管理员已标记）
     - `9`: 已驳回

### 1.2 管理员视角：项目审核与分配

**操作路径：** `项目审核` → 选择项目 → `分配专家`

**数据流转：**

1. **查看待审核项目**
   - 调用接口：`GET /api/project/admin/list`
   - 筛选条件：`project.status = 1`（待初审）

2. **分配专家进行评审**
   - 管理员为项目分配 2-3 名专家
   - 调用接口：`POST /api/project/assign`
   - **数据库操作：**
     - 更新 `project.status = 2`（评审中）
     - 在 `review` 表中为每个分配的专家创建评审记录
     - `review.project_id` = 项目ID
     - `review.expert_id` = 专家ID
     - `review.status = 0`（待评审）

### 1.3 专家视角：多维度评分

**操作路径：** `评审任务` → 选择项目 → `提交评审`

**数据流转：**

1. **查看待评审项目**
   - 调用接口：`GET /api/review/pending?expertId={id}`
   - 筛选条件：`review.expert_id = {expertId}` AND `review.status = 0`

2. **提交多维度评分**
   - 专家填写：
     - **可行性评分**（`score_feasibility`，0-100分，权重0.4）
     - **深度评分**（`score_depth`，0-100分，权重0.3）
     - **拓展度评分**（`score_extension`，0-100分，权重0.3）
   - 调用接口：`POST /api/review/submit`
   - **数据库操作：**
     - 更新 `review` 表：
       - `review.score_feasibility` = 可行性评分
       - `review.score_depth` = 深度评分
       - `review.score_extension` = 拓展度评分
       - `review.total_score` = `score_feasibility * 0.4 + score_depth * 0.3 + score_extension * 0.3`
       - `review.status = 1`（已评审）

3. **自动判断是否通过概念验证**
   - **后端逻辑（`ReviewServiceImpl.submitReview`）：**
     - 当所有分配的专家都完成评审后（`review.status = 1`），计算平均总分
     - 如果 `avg(total_score) >= 60`：
       - 更新 `project.status = 3`（已入库）
       - **自动创建孵化项目记录：**
         - 在 `incubation_project` 表中插入新记录
         - `incubation_project.project_id` = 项目ID
         - `incubation_project.status = 0`（待签署协议）
       - **自动创建两个默认里程碑：**
         - `incubation_milestone` 表插入两条记录：
           - 里程碑1：`name = "中期审核"`，`status = 1`（进行中）
           - 里程碑2：`name = "最终定型"`，`status = 0`（未开始）
     - 如果 `avg(total_score) < 60`：
       - 更新 `project.status = 9`（已驳回）

---

## 二、孵化准备阶段（Incubation Preparation）

### 2.1 申报人视角：签署协议与信息表

**操作路径：** `我的孵化项目` → 选择项目 → `提交入孵协议`

**数据流转：**

1. **查看孵化项目列表**
   - 调用接口：`GET /api/incubation/my?applicantId={id}`
   - **后端逻辑：**
     - 查找该申报人的所有项目（`project.applicant_id = {applicantId}`）
     - 对于 `project.status >= 3` 且 `!= 9` 的项目，如果不存在对应的 `incubation_project` 记录，则自动创建（`status = 0`）
   - **返回数据：**
     - `incubation_id`、`project_id`、`project_name`、`status`、`completion_status`

2. **提交入孵协议与信息表**
   - 申报人填写：
     - 协议附件URL（`agreementUrl`）
     - 入孵信息表JSON（`infoFormJson`，如团队规模、需求等）
   - 调用接口：`POST /api/incubation/submit-agreement`
   - **数据库操作：**
     - 更新 `incubation_project`：
       - `incubation_project.agreement_url` = 协议URL
       - `incubation_project.info_form_json` = 信息表JSON
       - `incubation_project.status = 1`（孵化中）
     - 更新 `project.status = 5`（孵化中）

### 2.2 管理员视角：指派项目经理与导师

**操作路径：** `孵化项目管理` → 选择项目 → `指派经理与导师`

**数据流转：**

1. **查看孵化项目列表**
   - 调用接口：`GET /api/incubation/admin/list`
   - 返回所有 `incubation_project` 记录，包含项目名称和状态

2. **指派项目经理与导师**
   - 管理员填写：
     - 项目经理ID（`projectManagerId`）
     - 导师ID列表（`mentorIds`，逗号分隔，如 "3,4,5"）
   - 调用接口：`POST /api/incubation/assign`
   - **数据库操作：**
     - 更新 `incubation_project`：
       - `incubation_project.project_manager_id` = 项目经理ID
       - `incubation_project.mentor_ids` = 导师ID列表
     - **如果该孵化项目还没有里程碑，自动创建两个默认里程碑：**
       - `incubation_milestone` 表插入：
         - 里程碑1：`name = "中期审核"`，`status = 1`（进行中）
         - 里程碑2：`name = "最终定型"`，`status = 0`（未开始）

---

## 三、孵化执行阶段（Incubation Execution）

### 3.1 里程碑报告流程（两轮：中期审核 + 最终定型）

#### 3.1.1 管理员：通知申报人提交报告

**操作路径：** `孵化项目管理` → 选择项目 → `审批里程碑报告` → 选择里程碑 → `通知申报人`

**数据流转：**

1. **通知申报人**
   - 调用接口：`POST /api/incubation/milestone/admin-notify`
   - **数据库操作：**
     - 在 `incubation_milestone_report` 表中创建或更新报告记录：
       - `incubation_milestone_report.milestone_id` = 里程碑ID
       - `incubation_milestone_report.admin_notified = 1`（已通知）
       - `incubation_milestone_report.admin_notify_time` = 当前时间
       - `incubation_milestone_report.status = 0`（待提交）
     - 更新 `incubation_milestone.status = 1`（进行中）

#### 3.1.2 申报人：提交里程碑报告

**操作路径：** `我的孵化项目` → 选择项目 → `里程碑与汇报` → `提交/修改报告`

**数据流转：**

1. **查看里程碑列表**
   - 调用接口：`GET /api/incubation/{incubationId}/milestones`
   - 返回该孵化项目的所有里程碑

2. **提交报告**
   - 申报人填写：
     - **项目描述**（`projectDescription`）
     - **进展内容**（`content`）
     - **附件**（`attachments`，JSON格式）
   - 调用接口：`POST /api/incubation/milestone/submit-report`
   - **数据库操作：**
     - 更新 `incubation_milestone_report`：
       - `incubation_milestone_report.project_description` = 项目描述
       - `incubation_milestone_report.content` = 进展内容
       - `incubation_milestone_report.attachments` = 附件JSON
       - `incubation_milestone_report.submitter_id` = 申报人ID
       - `incubation_milestone_report.status = 0`（待审核）
       - `incubation_milestone_report.admin_approved = 0`（待管理员审批）
     - 更新 `incubation_milestone.status = 2`（待审核）

#### 3.1.3 管理员：初审里程碑报告

**操作路径：** `孵化项目管理` → 选择项目 → `审批里程碑报告` → 选择里程碑 → `通过/拒绝`

**数据流转：**

1. **查看待审核报告**
   - 调用接口：`GET /api/incubation/milestone/{milestoneId}/reports`
   - 筛选条件：`incubation_milestone_report.status = 0`（待审核）

2. **管理员初审**
   - 管理员选择：通过 / 拒绝
   - 调用接口：`POST /api/incubation/milestone/admin-approve`
   - **数据库操作：**
     - 如果通过：
       - `incubation_milestone_report.admin_approved = 1`（已审批通过）
       - `incubation_milestone_report.admin_feedback` = 审批意见
       - `incubation_milestone_report.status = 0`（保持待审核，等待专家打分）
       - `incubation_milestone.status = 2`（待专家审核）
     - 如果拒绝：
       - `incubation_milestone_report.admin_approved = 2`（已审批拒绝）
       - `incubation_milestone_report.admin_feedback` = 拒绝原因
       - `incubation_milestone_report.status = 2`（需修订）
       - `incubation_milestone.status = 4`（需修订）

#### 3.1.4 专家：终审打分

**操作路径：** `我的辅导项目` → 选择项目 → `查看里程碑` → `专家打分`

**数据流转：**

1. **查看待打分报告**
   - 专家在 `我的辅导项目` 中查看分配给自己的孵化项目
   - 调用接口：`GET /api/incubation/mentor/list?mentorId={id}`
   - **筛选规则：**
     - 通过 `incubation_project.mentor_ids` 字段匹配（使用 `FIND_IN_SET`）
     - 或通过 `project.tech_domain` 与 `expert.field` 匹配

2. **专家打分**
   - 专家填写：
     - **评分**（`score`，0-100分）
     - **反馈意见**（`feedback`）
   - 调用接口：`POST /api/incubation/milestone/expert-score`
   - **前置条件检查：**
     - `incubation_milestone_report.admin_approved = 1`（必须已通过管理员审批）
   - **数据库操作：**
     - 更新 `incubation_milestone_report`：
       - `incubation_milestone_report.expert_score` = 评分
       - `incubation_milestone_report.expert_feedback` = 反馈意见
       - `incubation_milestone_report.status = 1`（已通过）
     - 更新 `incubation_milestone.status = 3`（已通过）

**重复上述流程两次：**
- **第一轮：中期审核**
- **第二轮：最终定型**

### 3.2 资源申请流程（随时可申请）

#### 3.2.1 申报人：申请资源

**操作路径：** `我的孵化项目` → 选择项目 → `申请资源`

**数据流转：**

1. **提交资源申请**
   - 申报人填写：
     - 资源类型（`type`）：TECH、IP、MARKET、FUND、FUNDING、SERVER、SUPPORT、INTERNSHIP、MENTOR
     - 标题（`title`）
     - 详情（`description`）
   - 调用接口：`POST /api/incubation/resource/apply`
   - **数据库操作：**
     - 在 `incubation_resource_request` 表中插入新记录：
       - `incubation_resource_request.incubation_id` = 孵化项目ID
       - `incubation_resource_request.project_id` = 项目ID
       - `incubation_resource_request.requester_id` = 申报人ID
       - `incubation_resource_request.type` = 资源类型
       - `incubation_resource_request.title` = 标题
       - `incubation_resource_request.description` = 详情
       - `incubation_resource_request.status = 0`（待处理）

#### 3.2.2 专家/项目经理：处理资源申请

**操作路径：** `我的辅导项目` → 选择项目 → `资源申请` → `处理`

**数据流转：**

1. **查看资源申请列表**
   - 调用接口：`GET /api/incubation/resource/mentor/list?mentorId={id}`
   - 返回该专家负责的所有孵化项目的资源申请

2. **处理资源申请**
   - 专家填写处理说明（`historyJson`）
   - 调用接口：`POST /api/incubation/resource/handle`
   - **数据库操作：**
     - 更新 `incubation_resource_request`：
       - `incubation_resource_request.status = 2`（已完成，直接完成分配）
       - `incubation_resource_request.handler_id` = 处理人ID
       - `incubation_resource_request.history_json` = 处理说明（追加到历史记录）

3. **申报人查看资源分配结果**
   - 调用接口：`GET /api/incubation/resource/applicant/list?applicantId={id}`
   - 在首页弹窗中展示状态为"已完成"的资源申请

---

## 四、落成审批阶段（Completion Review）

### 4.1 申报人：提交落成申请

**操作路径：** `我的孵化项目` → 选择项目 → `提交落成申请`

**前置条件：**
- 必须完成至少两轮里程碑审核（`incubation_milestone.status = 3` 的记录数 >= 2）

**数据流转：**

1. **提交落成申请**
   - 申报人填写：
     - 材料包URL（`completionPackageUrl`）
     - 落成说明（`completionDesc`）
   - 调用接口：`POST /api/incubation/completion/apply`
   - **数据库操作：**
     - 更新 `incubation_project`：
       - `incubation_project.completion_status = 1`（已申请待admin初审）
       - `incubation_project.completion_package_url` = 材料包URL
       - `incubation_project.completion_desc` = 落成说明

### 4.2 管理员：落成初审

**操作路径：** `孵化项目管理` → 选择项目 → `落成初审`

**数据流转：**

1. **查看待初审的落成申请**
   - 筛选条件：`incubation_project.status = 1`（孵化中）AND `incubation_project.completion_status = 1`（已申请待admin初审）

2. **管理员初审**
   - 管理员选择：通过 / 拒绝
   - 调用接口：`POST /api/incubation/completion/admin-review`
   - **数据库操作：**
     - 如果通过：
       - `incubation_project.completion_status = 2`（admin已通过待expert终审）
     - 如果拒绝：
       - `incubation_project.completion_status = 4`（admin拒绝）

### 4.3 专家：落成终审打分

**操作路径：** `我的辅导项目` → 选择项目 → `落成终审`

**数据流转：**

1. **查看待终审的落成申请**
   - 筛选条件：`incubation_project.completion_status = 2`（待expert终审）
   - 调用接口：`GET /api/incubation/completion/expert/pending?expertId={id}`（可选，或直接在项目列表中显示）

2. **专家终审打分**
   - 专家填写：
     - **评分**（`score`，0-100分）
     - **反馈意见**（`feedback`）
   - 调用接口：`POST /api/incubation/completion/expert-review`
   - **数据库操作：**
     - 如果评分 >= 60：
       - `incubation_project.completion_status = 3`（expert已通过，已落成）
       - `incubation_project.status = 2`（孵化项目状态：已落成）
       - `project.status = 6`（项目整体状态：已落成）
       - **自动创建成功案例记录：**
         - 在 `success_case` 表中插入新记录：
           - `success_case.project_id` = 项目ID
           - `success_case.incubation_id` = 孵化项目ID
           - `success_case.case_name` = 项目名称
           - `success_case.case_description` = 项目描述
           - `success_case.tech_domain` = 技术领域
           - `success_case.application_scenario` = 应用场景
           - `success_case.intellectual_property` = 知识产权
           - `success_case.cooperation_mode` = 合作模式
           - `success_case.display_status = 1`（公开展示）
           - `success_case.view_count = 0`
     - 如果评分 < 60：
       - `incubation_project.completion_status = 5`（expert拒绝）

---

## 五、成功案例展示阶段（Success Case Display）

### 5.1 公共展示：成功案例库

**操作路径：** `成功案例库`（所有角色可见）

**数据流转：**

1. **查看成功案例列表**
   - 调用接口：`GET /api/success-case/public/list`
   - 筛选条件：`success_case.display_status = 1`
   - 返回所有公开的成功案例

2. **查看案例详情**
   - 调用接口：`GET /api/success-case/public/{caseId}`
   - **数据库操作：**
     - `success_case.view_count` += 1（增加浏览次数）

### 5.2 统计图表展示

**数据流转：**

1. **技术领域分布统计**
   - 调用接口：`GET /api/success-case/stats/domain`
   - **统计逻辑：**
     - 查询所有 `success_case.display_status = 1` 的记录
     - 按 `tech_domain` 字段分组统计
     - 标准化领域名称（人工智能、大数据与云计算、信息安全、物联网与嵌入式技术、数字人文技术、其他）
   - **返回格式：** `[{name: "人工智能", value: 5}, ...]`
   - **用途：** 饼图展示

2. **成功案例数量趋势**
   - 调用接口：`GET /api/success-case/stats/trend`
   - **统计逻辑：**
     - 查询所有 `success_case.display_status = 1` 的记录
     - 按 `created_at` 字段的年份分组统计
   - **返回格式：** `[{name: "2024年", value: 10}, ...]`
   - **用途：** 柱状图展示

---

## 六、完整状态流转图

```
概念验证阶段：
project.status: 0(草稿) → 1(待初审) → 2(评审中) → 3(已入库) / 9(已驳回)

孵化准备阶段：
incubation_project.status: 0(待签署协议) → 1(孵化中)
project.status: 3(已入库) → 4(待入孵) → 5(孵化中)

孵化执行阶段（里程碑）：
incubation_milestone.status: 0(未开始) → 1(进行中) → 2(待审核) → 3(已通过) / 4(需修订)
incubation_milestone_report.status: 0(待审核) → 1(已通过) / 2(需修订)
incubation_milestone_report.admin_approved: 0(未审批) → 1(已审批通过) / 2(已审批拒绝)

资源申请：
incubation_resource_request.status: 0(待处理) → 2(已完成)

落成审批阶段：
incubation_project.completion_status: 0(未申请) → 1(已申请待admin初审) → 2(admin已通过待expert终审) → 3(已落成) / 4(admin拒绝) / 5(expert拒绝)
project.status: 5(孵化中) → 6(已落成)
incubation_project.status: 1(孵化中) → 2(已落成)

成功案例：
success_case.display_status: 1(公开展示)
```

---

## 七、关键数据表关系

```
project (项目主表)
  ├── applicant_id → sys_user (申报人)
  ├── status (项目状态)
  └── tech_domain (技术领域)

review (评审记录表)
  ├── project_id → project
  ├── expert_id → sys_user (专家)
  ├── score_feasibility, score_depth, score_extension, total_score (多维度评分)
  └── status (评审状态)

incubation_project (孵化项目表)
  ├── project_id → project
  ├── project_manager_id → sys_user (项目经理)
  ├── mentor_ids (导师ID列表，逗号分隔)
  ├── status (孵化状态)
  └── completion_status (落成状态)

incubation_milestone (里程碑表)
  └── incubation_id → incubation_project

incubation_milestone_report (里程碑报告表)
  ├── milestone_id → incubation_milestone
  ├── project_id → project
  ├── submitter_id → sys_user (申报人)
  ├── admin_approved (管理员审批状态)
  ├── expert_score, expert_feedback (专家评分和反馈)
  └── status (报告状态)

incubation_resource_request (资源申请表)
  ├── incubation_id → incubation_project
  ├── project_id → project
  ├── requester_id → sys_user (申报人)
  └── handler_id → sys_user (处理人)

success_case (成功案例表)
  ├── project_id → project
  ├── incubation_id → incubation_project
  └── display_status (展示状态)
```

---

## 八、总结

**完整流程路径：**

1. **申报人**：提交项目 → 签署协议 → 提交里程碑报告（两轮）→ 申请资源 → 提交落成申请
2. **管理员**：分配专家 → 指派项目经理与导师 → 通知提交报告 → 初审里程碑报告 → 初审落成申请
3. **专家**：多维度评分 → 终审里程碑报告 → 处理资源申请 → 终审落成申请（打分）

**最终结果：**
- 通过专家终审（评分≥60）的落成申请，自动创建成功案例记录
- 成功案例在"成功案例库"页面公开展示
- 统计图表自动更新，展示技术领域分布和数量趋势

