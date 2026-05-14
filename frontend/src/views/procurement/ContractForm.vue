<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">{{ isEdit ? '编辑合同' : '创建合同' }}</h2>
      <p class="page-desc">填写合同基本信息</p>
    </div>
    <el-card shadow="never" class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 640px">
        <el-form-item label="关联采购申请" prop="requestId">
          <el-input-number v-model="form.requestId" :min="1" placeholder="请输入已审批通过的申请ID" :disabled="isEdit" style="width: 100%" />
        </el-form-item>
        <el-form-item label="关联供应商" prop="supplierId">
          <el-input-number v-model="form.supplierId" :min="1" placeholder="请输入供应商ID" :disabled="isEdit" style="width: 100%" />
        </el-form-item>
        <el-form-item label="合同标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入合同标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="合同金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1000" placeholder="请输入合同金额" style="width: 100%" />
        </el-form-item>
        <el-form-item label="签订日期" prop="signedDate">
          <el-date-picker v-model="form.signedDate" type="date" placeholder="请选择签订日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="到期日期" prop="expiryDate">
          <el-date-picker v-model="form.expiryDate" type="date" placeholder="请选择到期日期" style="width: 100%" />
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
import { createContract, updateContract, getContractById } from '@/api/procurement'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  requestId: 0,
  supplierId: 0,
  title: '',
  amount: 0,
  signedDate: '',
  expiryDate: ''
})

const rules: FormRules = {
  requestId: [{ required: true, message: '请输入关联采购申请ID', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请输入关联供应商ID', trigger: 'blur' }],
  title: [{ required: true, message: '请输入合同标题', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }]
}

onMounted(async () => {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    const res = await getContractById(Number(id))
    const d = res.data.data
    form.requestId = d.requestId
    form.supplierId = d.supplierId
    form.title = d.title
    form.amount = d.amount
    form.signedDate = d.signedDate || ''
    form.expiryDate = d.expiryDate || ''
  }
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data: any = {
      requestId: form.requestId,
      supplierId: form.supplierId,
      title: form.title,
      amount: form.amount,
      signedDate: form.signedDate || undefined,
      expiryDate: form.expiryDate || undefined
    }
    if (isEdit.value) {
      const id = Number(route.params.id)
      const { requestId, supplierId, ...updateData } = data
      await updateContract(id, updateData)
      ElMessage.success('更新成功')
    } else {
      await createContract(data)
      ElMessage.success('创建成功')
    }
    router.push('/procurement/contracts')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/procurement/contracts')
}
</script>

<style scoped lang="scss">
.form-card {
  :deep(.el-card__body) { padding: 32px; }
}
</style>
