<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">{{ isEdit ? '编辑采购申请' : '创建采购申请' }}</h2>
      <p class="page-desc">填写采购基本信息</p>
    </div>
    <el-card shadow="never" class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 640px">
        <el-form-item label="采购标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入采购标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="采购金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1000" placeholder="请输入采购金额" style="width: 100%" />
        </el-form-item>
        <el-form-item label="采购品类" prop="category">
          <el-select v-model="form.category" placeholder="请选择采购品类" style="width: 100%">
            <el-option label="办公设备" value="办公设备" />
            <el-option label="IT设备" value="IT设备" />
            <el-option label="软件服务" value="软件服务" />
            <el-option label="家具" value="家具" />
            <el-option label="耗材" value="耗材" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="采购说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入采购说明（选填）" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createRequest, updateRequest, getRequestById } from '@/api/procurement'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  amount: 0,
  category: '',
  description: ''
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入采购标题', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入采购金额', trigger: 'blur' }]
}

onMounted(async () => {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    const res = await getRequestById(Number(id))
    const d = res.data.data
    form.title = d.title
    form.amount = d.amount
    form.category = d.category || ''
    form.description = d.description || ''
  }
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = {
      title: form.title,
      amount: form.amount,
      category: form.category || undefined,
      description: form.description || undefined
    }
    if (isEdit.value) {
      const id = Number(route.params.id)
      await updateRequest(id, data)
      ElMessage.success('更新成功')
    } else {
      await createRequest(data)
      ElMessage.success('创建成功')
    }
    router.push('/procurement/requests')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/procurement/requests')
}
</script>

<style scoped lang="scss">
.form-card {
  :deep(.el-card__body) { padding: 32px; }
}
</style>
