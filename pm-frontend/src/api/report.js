import request from './index'

export function getReports(stageId) {
  return request.get(`/stages/${stageId}/reports`)
}

export function submitReport(stageId, data) {
  return request.post(`/stages/${stageId}/reports`, data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getPendingReviews() {
  return request.get('/reports/pending')
}

export function reviewReport(reportId, data) {
  return request.post(`/reports/${reportId}/review`, data)
}
