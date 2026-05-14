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
        redirect: '/procurement/requests',
        meta: { title: '采购管理', icon: 'ShoppingCart' },
        children: [
          {
            path: 'requests',
            name: 'ProcurementRequests',
            component: () => import('@/views/procurement/RequestList.vue'),
            meta: { title: '采购申请' }
          },
          {
            path: 'requests/create',
            name: 'ProcurementRequestCreate',
            component: () => import('@/views/procurement/RequestForm.vue'),
            meta: { title: '创建采购申请' }
          },
          {
            path: 'requests/:id/edit',
            name: 'ProcurementRequestEdit',
            component: () => import('@/views/procurement/RequestForm.vue'),
            meta: { title: '编辑采购申请' }
          },
          {
            path: 'requests/:id',
            name: 'ProcurementRequestDetail',
            component: () => import('@/views/procurement/RequestDetail.vue'),
            meta: { title: '采购申请详情' }
          },
          {
            path: 'approvals',
            name: 'ProcurementApprovals',
            component: () => import('@/views/procurement/ApprovalPending.vue'),
            meta: { title: '待办审批' }
          },
          {
            path: 'suppliers',
            name: 'ProcurementSuppliers',
            component: () => import('@/views/procurement/SupplierList.vue'),
            meta: { title: '供应商管理' }
          },
          {
            path: 'contracts',
            name: 'ProcurementContracts',
            component: () => import('@/views/procurement/ContractList.vue'),
            meta: { title: '合同管理' }
          },
          {
            path: 'contracts/create',
            name: 'ProcurementContractCreate',
            component: () => import('@/views/procurement/ContractForm.vue'),
            meta: { title: '创建合同' }
          },
          {
            path: 'contracts/:id/edit',
            name: 'ProcurementContractEdit',
            component: () => import('@/views/procurement/ContractForm.vue'),
            meta: { title: '编辑合同' }
          },
          {
            path: 'contracts/:id',
            name: 'ProcurementContractDetail',
            component: () => import('@/views/procurement/ContractDetail.vue'),
            meta: { title: '合同详情' }
          },
          {
            path: 'payments',
            name: 'ProcurementPayments',
            component: () => import('@/views/procurement/PaymentManagement.vue'),
            meta: { title: '付款管理' }
          }
        ]
      },
      {
        path: 'audit',
        name: 'Audit',
        component: () => import('@/views/audit/AuditView.vue'),
        redirect: '/audit/login-logs',
        meta: { title: '安全审计', icon: 'Lock' },
        children: [
          {
            path: 'login-logs',
            name: 'AuditLoginLogs',
            component: () => import('@/views/audit/LoginLogList.vue'),
            meta: { title: '登录日志' }
          },
          {
            path: 'operation-logs',
            name: 'AuditOperationLogs',
            component: () => import('@/views/audit/OperationLogList.vue'),
            meta: { title: '操作日志' }
          }
        ]
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
