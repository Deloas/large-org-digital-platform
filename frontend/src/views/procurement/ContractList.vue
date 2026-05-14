<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">合同管理</h2>
      <p class="page-desc">合同创建与归档管理</p>
    </div>
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="标题/编号" clearable style="width: 200px" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="生效中" value="active" />
              <el-option label="已完成" value="completed" />
              <el-option label="已终止" value="terminated" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="openCreate">创建合同</el-button>
      </div>
      <el-table :data="tableData" v-loading="tableLoading" border stripe>
        <el-table-column prop="contractNo" label="合同编号" width="180" />
        <el-table-column prop="title" label="合同标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额(元)" width="140" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="signedDate" label="签订日期" width="120" />
        <el-table-column prop="expiryDate" label="到期日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDetail(row.id)">详情</el-button>
            <el-button text type="primary" size="small" @click="openEdit(row.id)">编辑</el-button>
            <el-button v-if="row.status === 'active'" text type="warning" size="small" @click="handleStatus(row.id, 'completed')">完成</el-button>
            <el-button v-if="row.status === 'active'" text type="danger" size="small" @click="handleStatus(row.id, 'terminated')">终止</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContractList, updateContractStatus } from '@/api/procurement'
import type { Contract } from '@/api/procurement'

const router = useRouter()
const tableData = ref<Contract[]>([])
const tableLoading = ref(false)
const total = ref(0)
const pagination = reactive({ pageNum: 1, pageSize: 10 })
const searchForm = reactive({ keyword: '', status: '' })

function formatMoney(val: number) {
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function statusType(status: string) {
  const map: Record<string, string> = { active: 'success', completed: 'info', terminated: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { active: '生效中', completed: '已完成', terminated: '已终止' }
  return map[status] || status
}

async function fetchData() {
  tableLoading.value = true
  try {
    const params: any = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.status) params.status = searchForm.status
    const res = await getContractList(params)
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() { pagination.pageNum = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.status = ''; handleSearch() }

function openCreate() { router.push('/procurement/contracts/create') }
function openEdit(id: number) { router.push(`/procurement/contracts/${id}/edit`) }
function openDetail(id: number) { router.push(`/procurement/contracts/${id}`) }

async function handleStatus(id: number, status: string) {
  const label = status === 'completed' ? '完成' : '终止'
  try {
    await ElMessageBox.confirm(`确认将合同标记为"${label}"？`, `确认${label}`)
    await updateContractStatus(id, status)
    ElMessage.success(`合同已${label}`)
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
