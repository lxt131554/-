import axios from 'axios'
import { ElMessage } from 'element-plus'
import { setupMock } from '../mock/index'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  withCredentials: true
})

function withRequestId(message, data) {
  return data?.requestId ? `${message} [requestId: ${data.requestId}]` : message
}

request.interceptors.response.use(
  res => {
    if (res.data.code !== 200) {
      if (res.data.code !== 401) {
        ElMessage.error(withRequestId(res.data.message || '请求失败', res.data))
      }
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      // silent - auth errors handled by router
    } else if (err.response?.status === 403) {
      ElMessage.error(err.response?.data?.message || '没有权限')
    } else if (err.response?.status === 409) {
      ElMessage.error(err.response?.data?.message || '数据冲突，请检查是否重复操作')
    } else if (err.response?.data?.message) {
      ElMessage.error(withRequestId(err.response.data.message, err.response.data))
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(err)
  }
)

// Mock mode: only enabled when VITE_MOCK=true (Netlify demo deployment)
if (import.meta.env.VITE_MOCK === 'true') {
  setupMock(request)
}

export default request
