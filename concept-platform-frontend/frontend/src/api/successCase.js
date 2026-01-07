import request from '@/utils/request'

// 管理员：创建成功案例
export function createSuccessCase(data) {
  return request({
    url: '/api/success-case/create',
    method: 'post',
    data
  })
}

// 公共展示：获取成功案例列表
export function getPublicSuccessCases() {
  return request({
    url: '/api/success-case/public/list',
    method: 'get'
  })
}

// 公共展示：获取案例详情
export function getCaseDetail(caseId) {
  return request({
    url: `/api/success-case/public/${caseId}`,
    method: 'get'
  })
}

// 统计：技术领域分布
export function getCaseDomainStats() {
  return request({
    url: '/api/success-case/stats/domain',
    method: 'get'
  })
}

// 统计：数量趋势
export function getCaseTrendStats() {
  return request({
    url: '/api/success-case/stats/trend',
    method: 'get'
  })
}

