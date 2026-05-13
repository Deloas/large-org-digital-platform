<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">菜单管理</h2>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <div></div>
        <el-button type="primary" @click="openCreate(0)">新增菜单</el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="id"
        border
        stripe
        default-expand-all
      >
        <el-table-column prop="name" label="菜单名称" min-width="160" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.type === 'directory' ? '' : row.type === 'menu' ? 'success' : 'info'">
              {{ row.type === 'directory' ? '目录' : row.type === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" width="160" />
        <el-table-column prop="component" label="组件" width="200" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="permission" label="权限标识" width="160" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openCreate(row.id)">添加子级</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="tableData"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="无（顶级菜单）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="directory">目录</el-radio>
            <el-radio value="menu">菜单</el-radio>
            <el-radio value="button">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路径" />
        </el-form-item>
        <el-form-item v-if="form.type === 'menu'" label="组件">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item v-if="form.type !== 'directory'" label="权限标识">
          <el-input v-model="form.permission" placeholder="请输入权限标识" />
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
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/system'

const tableData = ref<any[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: 0,
  parentId: undefined as number | undefined,
  type: 'menu' as string,
  name: '',
  path: '',
  component: '',
  icon: '',
  permission: '',
  sortOrder: 0
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路径', trigger: 'blur' }]
}

async function fetchTree() {
  loading.value = true
  try {
    const res = await getMenuTree()
    tableData.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

function openCreate(parentId: number) {
  isEdit.value = false
  form.id = 0
  form.parentId = parentId || undefined
  form.type = 'menu'
  form.name = ''
  form.path = ''
  form.component = ''
  form.icon = ''
  form.permission = ''
  form.sortOrder = 0
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  form.id = row.id
  form.parentId = row.parentId || undefined
  form.type = row.type
  form.name = row.name
  form.path = row.path || ''
  form.component = row.component || ''
  form.icon = row.icon || ''
  form.permission = row.permission || ''
  form.sortOrder = row.sortOrder || 0
  formRef.value?.resetFields()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data: any = {
      parentId: form.parentId || 0,
      type: form.type,
      name: form.name,
      path: form.path,
      icon: form.icon || undefined,
      sortOrder: form.sortOrder
    }
    if (form.type === 'menu') {
      data.component = form.component || undefined
    }
    if (form.type !== 'directory') {
      data.permission = form.permission || undefined
    }
    if (isEdit.value) {
      await updateMenu(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await createMenu(data)
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
    await ElMessageBox.confirm(`确定要删除菜单「${row.name}」吗？如有子菜单将一并删除。`, '提示', { type: 'warning' })
    await deleteMenu(row.id)
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
