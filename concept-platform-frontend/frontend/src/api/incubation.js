import request from '@/utils/request'

// 管理员：将项目标记为准予入孵并创建孵化记录
export function approveIncubation(data) {
  return request({
    url: '/api/incubation/approve',
    method: 'post',
    data
  })
}

// 管理员：孵化项目列表
export function getAdminIncubationList() {
  return request({
    url: '/api/incubation/admin/list',
    method: 'get'
  })
}

// 管理员：指派项目经理和导师、创建里程碑
export function assignIncubation(data) {
  return request({
    url: '/api/incubation/assign',
    method: 'post',
    data
  })
}

// 申报人：我的孵化项目
export function getMyIncubationProjects(params) {
  return request({
    url: '/api/incubation/my',
    method: 'get',
    params
  })
}

// 申报人：提交入孵协议与信息表
export function submitAgreement(data) {
  return request({
    url: '/api/incubation/submit-agreement',
    method: 'post',
    data
  })
}

// 里程碑列表
export function getMilestones(incubationId) {
  return request({
    url: `/api/incubation/${incubationId}/milestones`,
    method: 'get'
  })
}

// 提交里程碑报告
export function submitMilestoneReport(data) {
  return request({
    url: '/api/incubation/milestone/submit-report',
    method: 'post',
    data
  })
}

// 审核里程碑报告
export function reviewMilestoneReport(data) {
  return request({
    url: '/api/incubation/milestone/review-report',
    method: 'post',
    data
  })
}

// 管理员：审批里程碑报告
export function adminApproveMilestoneReport(data) {
  return request({
    url: '/api/incubation/milestone/admin-approve',
    method: 'post',
    data
  })
}

// 专家：为里程碑报告打分
export function expertScoreMilestoneReport(data) {
  return request({
    url: '/api/incubation/milestone/expert-score',
    method: 'post',
    data
  })
}

// 里程碑报告列表
export function getMilestoneReports(milestoneId) {
  return request({
    url: `/api/incubation/milestone/${milestoneId}/reports`,
    method: 'get'
  })
}

// 资源申请
export function applyResource(data) {
  return request({
    url: '/api/incubation/resource/apply',
    method: 'post',
    data
  })
}

// 处理资源申请
export function handleResource(data) {
  return request({
    url: '/api/incubation/resource/handle',
    method: 'post',
    data
  })
}

// 管理员：通知申报人提交里程碑报告
export function adminNotifyMilestone(data) {
  return request({
    url: '/api/incubation/milestone/admin-notify',
    method: 'post',
    data
  })
}

// 落成申请（原毕业申请）
export function applyCompletion(data) {
  return request({
    url: '/api/incubation/completion/apply',
    method: 'post',
    data
  })
}

// 管理员：落成初审
export function adminReviewCompletion(data) {
  return request({
    url: '/api/incubation/completion/admin-review',
    method: 'post',
    data
  })
}

// 专家：落成终审打分
export function expertReviewCompletion(data) {
  return request({
    url: '/api/incubation/completion/expert-review',
    method: 'post',
    data
  })
}

// 专家：查看待终审的落成申请列表
export function getPendingCompletionReviews(params) {
  return request({
    url: '/api/incubation/completion/expert/pending',
    method: 'get',
    params
  })
}

// 导师：我的孵化项目
export function getMentorIncubationProjects(params) {
  return request({
    url: '/api/incubation/mentor/list',
    method: 'get',
    params
  })
}

// 待提交提醒（弹窗使用）
export function getIncubationReminders(params) {
  return request({
    url: '/api/incubation/reminders',
    method: 'get',
    params
  })
}

// 专家：查看我负责的孵化项目的资源申请列表
export function getMentorResourceList(params) {
  return request({
    url: '/api/incubation/resource/mentor/list',
    method: 'get',
    params
  })
}

// 申报人：查看自己的资源申请及处理结果
export function getApplicantResourceList(params) {
  return request({
    url: '/api/incubation/resource/applicant/list',
    method: 'get',
    params
  })
}


