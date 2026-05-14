<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="layout-aside">
      <div class="aside-logo">
        <div class="logo-icon">
          <el-icon :size="24"><Platform /></el-icon>
        </div>
        <div class="logo-text">
          <span class="logo-title">一体化办公平台</span>
          <span class="logo-sub">数字化办公与安全审计</span>
        </div>
      </div>

      <div class="aside-menu-wrap">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="transparent"
          text-color="var(--sidebar-text)"
          active-text-color="var(--sidebar-active-text)"
          class="aside-menu"
        >
          <template v-for="item in userStore.menus" :key="item.name">
            <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
              <template #title>
                <el-icon v-if="item.meta?.icon"><component :is="iconMap[item.meta.icon]" /></el-icon>
                <span>{{ item.meta?.title || item.name }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.name"
                :index="child.path"
              >
                <el-icon v-if="child.meta?.icon"><component :is="iconMap[child.meta.icon]" /></el-icon>
                <span>{{ child.meta?.title || child.name }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="item.path">
              <el-icon v-if="item.meta?.icon"><component :is="iconMap[item.meta.icon]" /></el-icon>
              <span>{{ item.meta?.title || item.name }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>

      <div class="aside-footer">
        <span>v1.0.0</span>
      </div>
    </el-aside>

    <el-container class="layout-main">
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="pageTitle !== '工作台'">{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip content="系统运行中" placement="bottom">
            <el-tag type="success" size="small" effect="plain">运行中</el-tag>
          </el-tooltip>
          <span class="header-divider"></span>
          <el-avatar :size="32" icon="UserFilled" />
          <span class="header-user">{{ userStore.realName || userStore.username || '未登录' }}</span>
          <el-button v-if="!userStore.token" type="primary" size="small" @click="goLogin">登录</el-button>
          <el-button v-else type="default" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Monitor, Document, ShoppingCart, Lock, Setting,
  Platform, User, Avatar, Menu, Grid, Notebook, Tickets,
  Folder, ChatDotRound
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  if (userStore.token && !userStore.menusLoaded) {
    await Promise.all([userStore.fetchUserInfo(), userStore.fetchMenus()])
  }
})

const iconMap: Record<string, any> = {
  Monitor, Document, ShoppingCart, Lock, Setting, User, Avatar, Menu, Grid, Notebook, Tickets, Folder, ChatDotRound
}

const activeMenu = computed(() => route.path)
const sidebarWidth = '240px'

const pageTitle = computed(() => {
  const meta = route.meta
  return (meta?.title as string) || ''
})

function goLogin() {
  router.push('/login')
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.aside-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  gap: 12px;

  .logo-icon {
    color: var(--color-primary-hover);
    flex-shrink: 0;
  }

  .logo-text {
    display: flex;
    flex-direction: column;
    line-height: 1.3;
  }

  .logo-title {
    color: #fff;
    font-size: 15px;
    font-weight: 600;
    white-space: nowrap;
  }

  .logo-sub {
    color: var(--sidebar-text);
    font-size: 11px;
    white-space: nowrap;
  }
}

.aside-menu-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.aside-menu {
  border-right: none !important;

  :deep(.el-menu-item) {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
    border-radius: 6px;
    color: var(--sidebar-text);
    font-size: 14px;

    &:hover {
      color: var(--sidebar-text-hover);
      background-color: rgba(255, 255, 255, 0.06);
    }

    &.is-active {
      color: var(--sidebar-active-text);
      background-color: var(--sidebar-active-bg);
    }

    .el-icon {
      font-size: 18px;
    }
  }

  :deep(.el-sub-menu__title) {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
    border-radius: 6px;
    color: var(--sidebar-text);
    font-size: 14px;

    &:hover {
      color: var(--sidebar-text-hover);
      background-color: rgba(255, 255, 255, 0.06);
    }

    .el-icon {
      font-size: 18px;
    }
  }

  :deep(.el-sub-menu) {
    .el-menu {
      background-color: rgba(0, 0, 0, 0.15);

      .el-menu-item {
        padding-left: 56px !important;
      }
    }
  }
}

.aside-footer {
  padding: 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);

  span {
    font-size: 11px;
    color: var(--sidebar-text);
  }
}

.layout-header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--header-bg);
  box-shadow: var(--header-shadow);
  padding: 0 24px;
  position: relative;
  z-index: 1;
}

.header-left {
  :deep(.el-breadcrumb__inner) {
    color: var(--text-secondary);
    font-size: 13px;
  }

  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: var(--text-primary);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-divider {
  width: 1px;
  height: 20px;
  background-color: var(--border-color);
}

.header-user {
  font-size: 13px;
  color: var(--text-secondary);
}

.layout-content {
  background-color: var(--content-bg);
  padding: var(--content-padding);
  overflow-y: auto;
}
</style>
