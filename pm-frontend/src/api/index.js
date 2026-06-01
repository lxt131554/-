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
      if (res.data.code !== 401) {
        ElMessage.error(res.data.message || '请求失败')
      }
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      // silent - auth errors handled by router
    } else if (err.response?.status === 403) {
      ElMessage.error('没有权限')
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
