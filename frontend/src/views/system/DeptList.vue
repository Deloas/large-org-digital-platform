<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">部门管理</h2>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <div></div>
        <el-button type="primary" @click="openCreate(0)">新增部门</el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="id"
        border
        stripe
        default-expand-all
      >
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column prop="leaderName" label="负责人" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openCreate(row.id)">添加子部门</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑部门' : '新增部门'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="tableData"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="无（顶级部门）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leaderName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getDeptTree, createDept, updateDept, deleteDept } from '@/api/system'

const tableData = ref<any[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: 0,
  parentId: undefined as number | undefined,
  deptName: '',
  leaderName: '',
  phone: '',
  sortOrder: 0
})

const rules: FormRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

async function fetchTree() {
  loading.value = true
  try {
    const res = await getDeptTree()
    tableData.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

function openCreate(parentId: number) {
  isEdit.value = false
  form.id = 0
  form.parentId = parentId || undefined
  form.deptName = ''
  form.leaderName = ''
  form.phone = ''
  form.sortOrder = 0
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  form.id = row.id
  form.parentId = row.parentId || undefined
  form.deptName = row.deptName
  form.leaderName = row.leaderName || ''
  form.phone = row.phone || ''
  form.sortOrder = row.sortOrder || 0
  formRef.value?.resetFields()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      parentId: form.parentId || 0,
      deptName: form.deptName,
      leaderName: form.leaderName || undefined,
      phone: form.phone || undefined,
      sortOrder: form.sortOrder
    }
    if (isEdit.value) {
      await updateDept(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await createDept(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchTree()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除部门「${row.deptName}」吗？如有子部门将一并删除。`, '提示', { type: 'warning' })
    await deleteDept(row.id)
    ElMessage.success('删除成功')
    fetchTree()
  } catch { /* cancelled */ }
}

onMounted(() => {
  fetchTree()
})
</script>

<style scoped lang="scss">
.table-card {
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
