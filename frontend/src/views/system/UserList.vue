<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm" size="default">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="用户名" clearable />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="searchForm.realName" placeholder="姓名" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchList">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </div>

      <el-table :data="tableData" v-loading="tableLoading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="info" @click="resetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchList"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择部门"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" placeholder="请选择角色" clearable style="width: 100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
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
import {
  getUserList, createUser, updateUser, updateUserStatus, resetUserPassword,
  getDeptTree, getRoleList,
  type SysUser
} from '@/api/system'

const tableData = ref<SysUser[]>([])
const tableLoading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ username: '', realName: '', status: undefined as number | undefined })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: 0,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  deptId: undefined as number | undefined,
  roleId: undefined as number | undefined
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const deptTree = ref<any[]>([])
const roleList = ref<any[]>([])

async function fetchList() {
  tableLoading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.realName) params.realName = searchForm.realName
    if (searchForm.status !== undefined) params.status = searchForm.status
    const res = await getUserList(params)
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    tableLoading.value = false
  }
}

function resetSearch() {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.status = undefined
  fetchList()
}

async function loadOptions() {
  const [deptRes, roleRes] = await Promise.all([getDeptTree(), getRoleList()])
  deptTree.value = deptRes.data.data || []
  roleList.value = roleRes.data.data || []
}

function openCreate() {
  isEdit.value = false
  form.id = 0
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.phone = ''
  form.deptId = undefined
  form.roleId = undefined
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row: SysUser) {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.password = ''
  form.realName = row.realName
  form.email = row.email
  form.phone = row.phone
  form.deptId = row.deptId
  form.roleId = undefined
  formRef.value?.resetFields()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUser(form.id, {
        realName: form.realName,
        email: form.email || undefined,
        phone: form.phone || undefined,
        deptId: form.deptId,
        roleId: form.roleId
      })
      ElMessage.success('更新成功')
    } else {
      await createUser({
        username: form.username,
        password: form.password,
        realName: form.realName,
        email: form.email || undefined,
        phone: form.phone || undefined,
        deptId: form.deptId,
        roleId: form.roleId
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function toggleStatus(row: SysUser) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${label}用户「${row.username}」吗？`, '提示', { type: 'warning' })
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${label}成功`)
    fetchList()
  } catch { /* cancelled */ }
}

async function resetPassword(row: SysUser) {
  try {
    await ElMessageBox.confirm(`确定要重置用户「${row.username}」的密码吗？密码将被重置为 User@123456`, '提示', { type: 'warning' })
    await resetUserPassword(row.id)
    ElMessage.success('密码已重置为 User@123456')
  } catch { /* cancelled */ }
}

onMounted(() => {
  fetchList()
  loadOptions()
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
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
</style>
