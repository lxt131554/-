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
      ElMessage.error(res.data.message || '请求失败')
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      ElMessage.error('请先登录')
    } else if (err.response?.status === 403) {
      ElMessage.error('没有权限')
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(err)
  }
)

// Enable mock mode for demo deployment (no backend needed)
// When the backend is unreachable, all API calls return realistic mock data.
// When the backend IS running, real requests pass through normally.
setupMock(request)

export default request
