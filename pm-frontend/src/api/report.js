import request from './index'

export function getReports(stageId) {
  return request.get(`/stages/${stageId}/reports`)
}

export function submitReport(stageId, data) {
  return request.post(`/stages/${stageId}/reports`, data)
}

export function getPendingReviews(params) {
  return request.get('/reports/pending', { params })
}

export function reviewReport(reportId, data) {
  return request.post(`/reports/${reportId}/review`, data)
}
