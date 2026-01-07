# 孵化平台工作流程说明

## 完整审批流程

### 第一阶段：中期审核

1. **管理员通知** (`/api/incubation/milestone/admin-notify`)
   - 管理员在里程碑管理界面点击"通知申报人"
   - 系统创建报告记录，标记为"已通知"
   - 里程碑状态变为"进行中"

2. **申报人提交** (`/api/incubation/milestone/submit-report`)
   - 申报人收到通知后，填写：
     - 项目描述（projectDescription）
     - 进展内容（content）
     - 附件（attachments）
   - 提交后，报告状态为"待审核"
   - 里程碑状态变为"待审核"

3. **管理员初审** (`/api/incubation/milestone/admin-approve`)
   - 管理员检查报告是否有误
   - 如果通过：推送给专家（adminApproved=1）
   - 如果拒绝：需要申报人修改（adminApproved=2, status=2）

4. **专家打分** (`/api/incubation/milestone/expert-score`)
   - 只有管理员审批通过的报告才能打分
   - 专家填写：
     - 评分（0-100分）
     - 反馈意见
   - 打分后，报告状态为"已通过"
   - 里程碑状态变为"已通过"

### 第二阶段：最终定型

重复上述流程（中期审核 → 最终定型）

### 第三阶段：落成申请

1. **申报人申请落成** (`/api/incubation/completion/apply`)
   - 完成两轮里程碑审核后
   - 提交落成材料包和描述

2. **管理员落成评审** (`/api/incubation/completion/review`)
   - 管理员审核落成申请
   - 通过后，项目进入成功案例库

## 资源申请流程

1. **申报人申请资源** (`/api/incubation/resource/apply`)
   - 随时可以申请资源

2. **专家/项目经理处理** (`/api/incubation/resource/handle`)
   - 更新处理状态
   - 填写处理说明（追加到历史记录）

## 数据库字段说明

### incubation_milestone_report 表新增字段：
- `project_description`: 项目描述（申报者填写）
- `admin_notified`: 管理员是否已通知（0-未通知, 1-已通知）
- `admin_notify_time`: 管理员通知时间

### incubation_project 表字段变更：
- `graduate_status` → `completion_status`: 落成状态
- `graduate_package_url` → `completion_package_url`: 落成材料包URL
- `graduate_desc` → `completion_desc`: 落成描述

## 里程碑状态说明

- **0**: 未开始
- **1**: 进行中（管理员已通知，等待申报人提交）
- **2**: 待审核（申报人已提交，等待管理员审批）
- **3**: 已通过（专家已打分）
- **4**: 需修订（管理员拒绝或专家要求修改）

## 报告状态说明

- **0**: 待审核（申报人已提交，等待管理员审批）
- **1**: 已通过（专家已打分）
- **2**: 需修订（管理员拒绝）

## 管理员审批状态说明

- **0**: 未审批
- **1**: 已审批通过（推送给专家）
- **2**: 已审批拒绝（需修改）

