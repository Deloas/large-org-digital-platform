import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/ForbiddenView.vue'),
    meta: { title: '无权限' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台', icon: 'Monitor' }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/KnowledgeView.vue'),
        meta: { title: '知识库', icon: 'Document' }
      },
      {
        path: 'procurement',
        name: 'Procurement',
        component: () => import('@/views/procurement/ProcurementView.vue'),
        meta: { title: '采购管理', icon: 'ShoppingCart' }
      },
      {
        path: 'audit',
        name: 'Audit',
        component: () => import('@/views/audit/AuditView.vue'),
        meta: { title: '安全审计', icon: 'Lock' }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/views/system/SystemView.vue'),
        redirect: '/system/users',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'users',
            name: 'SystemUsers',
            component: () => import('@/views/system/UserList.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'roles',
            name: 'SystemRoles',
            component: () => import('@/views/system/RoleList.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: 'depts',
            name: 'SystemDepts',
            component: () => import('@/views/system/DeptList.vue'),
            meta: { title: '部门管理' }
          },
          {
            path: 'menus',
            name: 'SystemMenus',
            component: () => import('@/views/system/MenuList.vue'),
            meta: { title: '菜单管理' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from) => {
  const token = localStorage.getItem('token')

  if (to.path === '/403') {
    return
  }

  if (to.path !== '/login' && !token) {
    return '/login'
  }

  if (token && to.path !== '/login' && to.path !== '/dashboard') {
    const userStore = useUserStore()
    if (!userStore.menusLoaded) {
      await Promise.all([userStore.fetchUserInfo(), userStore.fetchMenus()])
    }
    const paths = userStore.flattenMenuPaths()
    if (paths.length > 0 && !paths.includes(to.path)) {
      return '/403'
    }
  }
})

export default router
