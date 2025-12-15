import request from '@/utils/request'

export function getReviewList(params) {
  return request({
    url: '/api/review/list',
    method: 'get',
    params
  })
}

// 获取项目的所有评审结果
export function getProjectReviews(projectId) {
  return request({
    url: `/api/review/project/${projectId}`,
    method: 'get'
  })
}

export function submitReview(data) {
  return request({
    url: '/api/review/submit',
    method: 'post',
    data
  })
}
