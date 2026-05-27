import request from './index'

export function getStages(projectId) {
  return request.get(`/projects/${projectId}/stages`)
}

export function addStage(projectId, data) {
  return request.post(`/projects/${projectId}/stages`, data)
}

export function updateStage(projectId, stageId, data) {
  return request.put(`/projects/${projectId}/stages/${stageId}`, data)
}

export function deleteStage(projectId, stageId) {
  return request.delete(`/projects/${projectId}/stages/${stageId}`)
}

export function getMyTasks() {
  return request.get('/stages/my-tasks')
}
