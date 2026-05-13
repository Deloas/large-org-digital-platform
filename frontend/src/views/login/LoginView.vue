<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <el-icon :size="36" class="brand-icon"><Platform /></el-icon>
        <h1>大型组织一体化办公平台</h1>
        <p class="brand-sub">数字化办公与安全审计</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="errorMsg" class="login-error">
        <el-alert :title="errorMsg" type="error" :closable="false" show-icon />
      </div>

      <div class="login-hint">
        <el-alert
          title="测试账号：admin / Admin@123456"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </div>

    <p class="login-footer">
      学生工程实践项目 · 不使用真实企业内部数据
    </p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Platform } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMsg.value = ''
  try {
    await userStore.login({ username: form.username, password: form.password })
    router.push('/dashboard')
  } catch (e: any) {
    errorMsg.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: var(--content-bg);
}

.login-card {
  width: 420px;
  padding: 48px 40px 36px;
  background: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.login-brand {
  text-align: center;
  margin-bottom: 32px;

  .brand-icon {
    color: var(--color-primary);
    margin-bottom: 12px;
  }

  h1 {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 6px;
  }

  .brand-sub {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
}

.login-error {
  margin-bottom: 16px;
}

.login-hint {
  margin-top: 16px;
}

.login-footer {
  margin-top: 24px;
  font-size: 12px;
  color: var(--text-placeholder);
}
</style>
