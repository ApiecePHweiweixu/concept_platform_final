import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

export function getExperts() {
  return request({
    url: '/api/user/experts', // 假设获取专家列表的接口
    method: 'get'
  })
}
