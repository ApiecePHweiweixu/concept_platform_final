import request from '@/utils/request'

export function getList(params) {
  return request({
    url: '/api/project/list',
    method: 'get',
    params
  })
}

export function getMyProjectList(params) {
  return request({
    url: '/api/project/list/my',
    method: 'get',
    params
  })
}

export function addProject(data) {
  return request({
    url: '/api/project/add',
    method: 'post',
    data
  })
}

export function auditProject(data) {
  return request({
    url: '/api/project/audit', // 假设审核接口
    method: 'post',
    data
  })
}
