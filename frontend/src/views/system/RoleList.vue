<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">角色管理</h2>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <div></div>
        <el-button type="primary" @click="openCreate">新增角色</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleCode" label="角色编码" width="140" />
        <el-table-column prop="roleName" label="角色名称" width="140" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="openMenuDialog(row)">分配菜单</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
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

    <el-dialog
      v-model="menuDialogVisible"
      title="分配菜单权限"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        show-checkbox
        :default-checked-keys="checkedMenuIds"
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSubmitLoading" @click="handleMenuSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'
import {
  getRoleList, createRole, updateRole, deleteRole, getRoleMenuIds, assignRoleMenus,
  getMenuTree,
  type SysRole
} from '@/api/system'

const tableData = ref<SysRole[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: 0,
  roleCode: '',
  roleName: '',
  description: '',
  sortOrder: 0
})

const rules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const menuDialogVisible = ref(false)
const menuSubmitLoading = ref(false)
const menuTree = ref<any[]>([])
const checkedMenuIds = ref<number[]>([])
const currentRoleId = ref(0)
const menuTreeRef = ref<InstanceType<typeof ElTree>>()

async function fetchList() {
  loading.value = true
  try {
    const res = await getRoleList()
    tableData.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  const res = await getMenuTree()
  menuTree.value = res.data.data || []
}

function openCreate() {
  isEdit.value = false
  form.id = 0
  form.roleCode = ''
  form.roleName = ''
  form.description = ''
  form.sortOrder = 0
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row: SysRole) {
  isEdit.value = true
  form.id = row.id
  form.roleCode = row.roleCode
  form.roleName = row.roleName
  form.description = row.description
  form.sortOrder = row.sortOrder
  formRef.value?.resetFields()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateRole(form.id, {
        roleName: form.roleName,
        description: form.description,
        sortOrder: form.sortOrder
      })
      ElMessage.success('更新成功')
    } else {
      await createRole({
        roleCode: form.roleCode,
        roleName: form.roleName,
        description: form.description,
        sortOrder: form.sortOrder
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: SysRole) {
  try {
    await ElMessageBox.confirm(`确定要删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* cancelled */ }
}

async function openMenuDialog(row: SysRole) {
  currentRoleId.value = row.id
  const res = await getRoleMenuIds(row.id)
  checkedMenuIds.value = res.data.data || []
  menuDialogVisible.value = true
}

async function handleMenuSubmit() {
  menuSubmitLoading.value = true
  try {
    const keys = menuTreeRef.value?.getCheckedKeys() as number[] || []
    const halfKeys = menuTreeRef.value?.getHalfCheckedKeys() as number[] || []
    await assignRoleMenus(currentRoleId.value, [...keys, ...halfKeys])
    ElMessage.success('菜单分配成功')
    menuDialogVisible.value = false
  } finally {
    menuSubmitLoading.value = false
  }
}

onMounted(() => {
  fetchList()
  loadMenuTree()
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
