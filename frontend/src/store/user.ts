import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import type { LoginRequest } from '@/api/auth'
import { getMenuRouters } from '@/api/system'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userId = ref<number>(0)
  const username = ref('')
  const realName = ref('')
  const avatar = ref('')
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])
  const menus = ref<any[]>([])
  const menusLoaded = ref(false)

  function resetState() {
    token.value = ''
    userId.value = 0
    username.value = ''
    realName.value = ''
    avatar.value = ''
    roles.value = []
    permissions.value = []
    menus.value = []
    menusLoaded.value = false
    localStorage.removeItem('token')
  }

  async function login(data: LoginRequest) {
    resetState()
    const res = await loginApi(data)
    const d = res.data.data
    token.value = d.token
    userId.value = d.userId
    username.value = d.username
    realName.value = d.realName
    avatar.value = d.avatar
    roles.value = d.roles
    permissions.value = d.permissions
    localStorage.setItem('token', d.token)
    await fetchMenus()
  }

  async function fetchUserInfo() {
    const res = await getCurrentUser()
    const d = res.data.data
    userId.value = d.userId
    username.value = d.username
    realName.value = d.realName
    avatar.value = d.avatar
    roles.value = d.roles
    permissions.value = d.permissions
  }

  async function fetchMenus() {
    try {
      const res = await getMenuRouters()
      menus.value = res.data.data || []
    } catch {
      menus.value = []
    } finally {
      menusLoaded.value = true
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      resetState()
    }
  }

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm)
  }

  function hasRole(role: string): boolean {
    return roles.value.includes(role)
  }

  function flattenMenuPaths(): string[] {
    const paths: string[] = []
    function walk(items: any[]) {
      for (const item of items) {
        if (item.path) paths.push(item.path)
        if (item.children && item.children.length) {
          walk(item.children)
        }
      }
    }
    walk(menus.value)
    return paths
  }

  return {
    token, userId, username, realName, avatar,
    roles, permissions, menus, menusLoaded,
    login, fetchUserInfo, fetchMenus, logout,
    hasPermission, hasRole, resetState, flattenMenuPaths
  }
})
