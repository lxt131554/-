import axios from 'axios'
import { ElMessage } from 'element-plus'
import { setupMock } from '../mock/index'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true
})

request.interceptors.response.use(
  res => {
    if (res.data.code !== 200) {
      // Don't show toast for auth errors (401) — handled by router
      if (res.data.code !== 401) {
        ElMessage.error(res.data.message || '请求失败')
      }
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => {
    // Don't show toast for auth errors — handled by router
    if (err.response?.status === 401) {
      // silent
    } else if (err.response?.status === 403) {
      ElMessage.error('没有权限')
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(err)
  }
)

// Mock mode: only enabled when VITE_MOCK=true (Netlify demo deployment)
// Local dev uses real SpringBoot backend at localhost:8080
if (import.meta.env.VITE_MOCK === 'true') {
  setupMock(request)
}

export default request
