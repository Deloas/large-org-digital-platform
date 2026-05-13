import { get, post, put, del } from './request'

// ---- 用户 ----
export interface SysUser {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  deptId: number
  avatar: string
  status: number
  createdAt: string
}

export interface UserCreateRequest {
  username: string
  password: string
  realName: string
  email?: string
  phone?: string
  deptId?: number
  roleId?: number
}

export interface UserUpdateRequest {
  realName?: string
  email?: string
  phone?: string
  deptId?: number
  roleId?: number
}

export function getUserList(params: any) {
  return get<{ records: SysUser[]; total: number }>('/system/users', params)
}

export function getUserById(id: number) {
  return get<SysUser>(`/system/users/${id}`)
}

export function createUser(data: UserCreateRequest) {
  return post('/system/users', data)
}

export function updateUser(id: number, data: UserUpdateRequest) {
  return put(`/system/users/${id}`, data)
}

export function updateUserStatus(id: number, status: number) {
  return put(`/system/users/${id}/status`, null, { params: { status } })
}

export function resetUserPassword(id: number) {
  return put(`/system/users/${id}/reset-password`)
}

// ---- 角色 ----
export interface SysRole {
  id: number
  roleCode: string
  roleName: string
  description: string
  sortOrder: number
  status: number
}

export interface RoleCreateRequest {
  roleCode: string
  roleName: string
  description?: string
  sortOrder?: number
}

export interface RoleUpdateRequest {
  roleName?: string
  description?: string
  sortOrder?: number
}

export function getRoleList() {
  return get<SysRole[]>('/system/roles')
}

export function createRole(data: RoleCreateRequest) {
  return post('/system/roles', data)
}

export function updateRole(id: number, data: RoleUpdateRequest) {
  return put(`/system/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return del(`/system/roles/${id}`)
}

export function getRoleMenuIds(id: number) {
  return get<number[]>(`/system/roles/${id}/menus`)
}

export function assignRoleMenus(id: number, menuIds: number[]) {
  return put(`/system/roles/${id}/menus`, { menuIds })
}

// ---- 部门 ----
export function getDeptTree() {
  return get<any[]>('/system/depts')
}

export function createDept(data: any) {
  return post('/system/depts', data)
}

export function updateDept(id: number, data: any) {
  return put(`/system/depts/${id}`, data)
}

export function deleteDept(id: number) {
  return del(`/system/depts/${id}`)
}

// ---- 菜单 ----
export function getMenuTree() {
  return get<any[]>('/system/menus')
}

export function getMenuRouters() {
  return get<any[]>('/system/menus/routers')
}

export function createMenu(data: any) {
  return post('/system/menus', data)
}

export function updateMenu(id: number, data: any) {
  return put(`/system/menus/${id}`, data)
}

export function deleteMenu(id: number) {
  return del(`/system/menus/${id}`)
}
