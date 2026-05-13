import { post, get } from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginVo {
  token: string
  userId: number
  username: string
  realName: string
  avatar: string
  roles: string[]
  permissions: string[]
}

export interface UserInfo {
  userId: number
  username: string
  realName: string
  avatar: string
  email: string
  phone: string
  deptId: number
  roles: string[]
  permissions: string[]
}

export function login(data: LoginRequest) {
  return post<LoginVo>('/auth/login', data)
}

export function logout() {
  return post('/auth/logout')
}

export function getCurrentUser() {
  return get<UserInfo>('/auth/me')
}
