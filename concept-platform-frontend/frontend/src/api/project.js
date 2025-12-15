import request from '@/utils/request'

export function getList(params) {
  return request({
    url: '/api/project/list',
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

