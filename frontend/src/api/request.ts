import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

interface Result<T = any> {
  code: number
  message: string
  data: T
}

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.satoken = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

instance.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
  return instance.get<Result<T>>(url, { params, ...config })
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
  return instance.post<Result<T>>(url, data, config)
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
  return instance.put<Result<T>>(url, data, config)
}

export function del<T = any>(url: string, config?: AxiosRequestConfig) {
  return instance.delete<Result<T>>(url, config)
}

export default instance
