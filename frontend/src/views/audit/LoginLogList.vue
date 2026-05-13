<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">登录日志</h2>
      <p class="page-desc">记录所有用户的登录尝试</p>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline size="default">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="输入用户名" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="IP 地址">
          <el-input v-model="query.ip" placeholder="输入 IP" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:140px">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width:360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="loginIp" label="登录 IP" width="160" />
        <el-table-column prop="loginTime" label="登录时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" min-width="160">
          <template #default="{ row }">
            <span v-if="row.failReason" class="fail-reason">{{ row.failReason }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 15, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="登录日志详情" width="560px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ detail.userId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="登录 IP">{{ detail.loginIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 'success' ? 'success' : 'danger'" size="small">
            {{ detail.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="失败原因">{{ detail.failReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="登录时间" :span="2">{{ formatTime(detail.loginTime) }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent" :span="2">{{ detail.userAgent || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { getLoginLogs, getLoginLogDetail, type LoginLog, type LoginLogQuery } from '@/api/audit'

const loading = ref(false)
const tableData = ref<LoginLog[]>([])
const total = ref(0)
const timeRange = ref<[string, string] | null>(null)

const query = reactive<LoginLogQuery & { page: number; pageSize: number }>({
  page: 1,
  pageSize: 15,
  username: '',
  ip: '',
  status: ''
})

async function fetchData() {
  loading.value = true
  try {
    if (timeRange.value) {
      query.startTime = timeRange.value[0]
      query.endTime = timeRange.value[1]
    } else {
      query.startTime = undefined
      query.endTime = undefined
    }
    const res = await getLoginLogs(query)
    const data = res.data.data
    tableData.value = data.records
    total.value = data.total
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchData()
}

function handleReset() {
  query.username = ''
  query.ip = ''
  query.status = ''
  timeRange.value = null
  query.page = 1
  fetchData()
}

const detailVisible = ref(false)
const detail = ref<LoginLog | null>(null)

async function showDetail(id: number) {
  try {
    const res = await getLoginLogDetail(id)
    detail.value = res.data.data
    detailVisible.value = true
  } catch {
    detail.value = null
  }
}

function formatTime(str: string) {
  if (!str) return '-'
  const d = new Date(str)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

fetchData()
</script>

<style scoped lang="scss">
.search-card {
  margin-bottom: 16px;
}

.table-card {
  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}

.fail-reason {
  color: var(--el-color-danger);
}
</style>
