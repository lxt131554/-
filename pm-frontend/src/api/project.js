import request from './index'

export function getProjects(params) {
  return request.get('/projects', { params })
}

export function getProjectDetail(id) {
  return request.get(`/projects/${id}`)
}

export function createProject(data) {
  return request.post('/projects', data)
}

export function updateProject(id, data) {
  return request.put(`/projects/${id}`, data)
}

export function deleteProject(id) {
  return request.delete(`/projects/${id}`)
}

export function importProjectsFromOa(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/projects/import/oa', formData, {
    timeout: 60000
  })
}

export function getProjectMembers(id) {
  return request.get(`/projects/${id}/members`)
}

export function addProjectMember(id, data) {
  return request.post(`/projects/${id}/members`, data)
}

export function removeProjectMember(projectId, memberId) {
  return request.delete(`/projects/${projectId}/members/${memberId}`)
}
