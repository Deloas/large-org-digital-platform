import { get } from './request'

export interface LoginLog {
  id: number
  userId: number
  username: string
  loginIp: string
  userAgent: string
  status: string
  failReason: string
  loginTime: string
  createdAt: string
}

export interface OperationLog {
  id: number
  userId: number
  username: string
  module: string
  action: string
  requestPath: string
  requestMethod: string
  requestParams: string
  result: string
  errorMsg: string
  costMs: number
  ip: string
  userAgent: string
  createdAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface LoginLogQuery {
  page?: number
  pageSize?: number
  username?: string
  ip?: string
  status?: string
  startTime?: string
  endTime?: string
}

export interface OperationLogQuery {
  page?: number
  pageSize?: number
  username?: string
  module?: string
  action?: string
  requestPath?: string
  result?: string
  startTime?: string
  endTime?: string
}

export function getLoginLogs(params: LoginLogQuery) {
  return get<PageResult<LoginLog>>('/audit/login-logs', params)
}

export function getLoginLogDetail(id: number) {
  return get<LoginLog>(`/audit/login-logs/${id}`)
}

export function getOperationLogs(params: OperationLogQuery) {
  return get<PageResult<OperationLog>>('/audit/operation-logs', params)
}

export function getOperationLogDetail(id: number) {
  return get<OperationLog>(`/audit/operation-logs/${id}`)
}
