import { get, post, put, del } from './request'

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

// ---- 安全审计增强模块 ----

export interface SecurityAlert {
  id: number
  alertType: string
  severity: string
  title: string
  detail: string
  relatedUser: string
  relatedIp: string
  status: string
  handler: string
  handleNote: string
  duplicateCount: number
  firstTime: string
  lastTime: string
  createdAt: string
  updatedAt: string
}

export interface IpBlacklist {
  id: number
  ipAddress: string
  reason: string
  status: number
  expiresAt: string
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface DashboardData {
  totalAlertsUnread: number
  highAlertsToday: number
  totalAlertsToday: number
  blacklistCount: number
  alertTrend: { date: string; count: number }[]
  alertByType: { alertType: string; count: number }[]
  alertBySeverity: { severity: string; count: number }[]
  recentHighAlerts: SecurityAlert[]
  topFailedAccounts: { username: string; count: number }[]
}

export interface AlertQuery {
  page?: number
  pageSize?: number
  alertType?: string
  severity?: string
  status?: string
  relatedUser?: string
  relatedIp?: string
  startTime?: string
  endTime?: string
}

export interface BlacklistQuery {
  page?: number
  pageSize?: number
  ipAddress?: string
  status?: number
}

export interface BlacklistForm {
  ipAddress: string
  reason: string
  status?: number
  expiresAt?: string
}

export function getDashboard() {
  return get<DashboardData>('/audit/security/dashboard')
}

export function getAlerts(params: AlertQuery) {
  return get<PageResult<SecurityAlert>>('/audit/security/alerts', params)
}

export function getAlertDetail(id: number) {
  return get<SecurityAlert>(`/audit/security/alerts/${id}`)
}

export function updateAlertStatus(id: number, status: string, note?: string) {
  return put<void>(`/audit/security/alerts/${id}/status`, { status, note })
}

export function getBlacklist(params: BlacklistQuery) {
  return get<PageResult<IpBlacklist>>('/audit/security/blacklist', params)
}

export function addBlacklist(data: BlacklistForm) {
  return post<void>('/audit/security/blacklist', data)
}

export function updateBlacklist(id: number, data: BlacklistForm) {
  return put<void>(`/audit/security/blacklist/${id}`, data)
}

export function deleteBlacklist(id: number) {
  return del<void>(`/audit/security/blacklist/${id}`)
}
