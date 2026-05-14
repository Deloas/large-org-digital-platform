<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">供应商管理</h2>
      <p class="page-desc">管理与维护供应商信息</p>
    </div>
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="名称/编号/联系人" clearable style="width: 240px" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="openCreate">新增供应商</el-button>
      </div>
      <el-table :data="tableData" v-loading="tableLoading" border stripe>
        <el-table-column prop="supplierNo" label="供应商编号" width="180" />
        <el-table-column prop="name" label="供应商名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="160" show-overflow-tooltip />
        <el-table-column prop="qualification" label="资质" width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :close-on-click-modal="false" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="30" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="100" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" maxlength="300" />
        </el-form-item>
        <el-form-item label="资质" prop="qualification">
          <el-input v-model="form.qualification" type="textarea" :rows="2" placeholder="请输入资质描述" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getSupplierList, createSupplier, updateSupplier, deleteSupplier, updateSupplierStatus } from '@/api/procurement'
import type { Supplier } from '@/api/procurement'

const tableData = ref<Supplier[]>([])
const tableLoading = ref(false)
const total = ref(0)
const pagination = reactive({ pageNum: 1, pageSize: 10 })
const searchForm = reactive({ keyword: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref(0)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const emptyForm = () => ({
  name: '',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: '',
  qualification: ''
})
const form = reactive(emptyForm())
const rules: FormRules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }]
}

async function fetchData() {
  tableLoading.value = true
  try {
    const params: any = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    const res = await getSupplierList(params)
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() { pagination.pageNum = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; handleSearch() }

function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新增供应商'
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row: Supplier) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑供应商'
  form.name = row.name
  form.contactPerson = row.contactPerson || ''
  form.contactPhone = row.contactPhone || ''
  form.email = row.email || ''
  form.address = row.address || ''
  form.qualification = row.qualification || ''
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = {
      name: form.name,
      contactPerson: form.contactPerson || undefined,
      contactPhone: form.contactPhone || undefined,
      email: form.email || undefined,
      address: form.address || undefined,
      qualification: form.qualification || undefined
    }
    if (isEdit.value) {
      await updateSupplier(editId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createSupplier(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row: Supplier) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该供应商？`, `确认${action}`)
    await updateSupplierStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch { /* cancelled */ }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除该供应商？', '确认删除', { type: 'warning' })
    await deleteSupplier(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.search-form {
  :deep(.el-form-item) { margin-bottom: 0; }
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
