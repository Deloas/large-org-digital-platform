<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
      <p class="page-desc">记录系统管理类的所有写入操作</p>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline size="default">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="输入用户名" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="query.module" placeholder="全部" clearable style="width:140px">
            <el-option label="用户管理" value="用户管理" />
            <el-option label="角色管理" value="角色管理" />
            <el-option label="部门管理" value="部门管理" />
            <el-option label="菜单管理" value="菜单管理" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作动作">
          <el-input v-model="query.action" placeholder="输入动作" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="query.result" placeholder="全部" clearable style="width:120px">
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
        <el-table-column prop="module" label="操作模块" width="110" />
        <el-table-column prop="action" label="操作动作" width="120" />
        <el-table-column prop="requestPath" label="请求路径" width="200" />
        <el-table-column prop="costMs" label="耗时" width="80">
          <template #default="{ row }">{{ row.costMs }}ms</template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.result === 'success' ? 'success' : 'danger'" size="small">
              {{ row.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
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

    <el-dialog v-model="detailVisible" title="操作日志详情" width="620px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ detail.module }}</el-descriptions-item>
        <el-descriptions-item label="操作动作">{{ detail.action }}</el-descriptions-item>
        <el-descriptions-item label="请求路径">{{ detail.requestPath || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detail.requestMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costMs }}ms</el-descriptions-item>
        <el-descriptions-item label="操作 IP">{{ detail.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="detail.result === 'success' ? 'success' : 'danger'" size="small">
            {{ detail.result === 'success' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="params-text">{{ detail.requestParams || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="异常信息" :span="2" v-if="detail.errorMsg">
          <span class="error-text">{{ detail.errorMsg }}</span>
        </el-descriptions-item>
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
import { getOperationLogs, getOperationLogDetail, type OperationLog, type OperationLogQuery } from '@/api/audit'

const loading = ref(false)
const tableData = ref<OperationLog[]>([])
const total = ref(0)
const timeRange = ref<[string, string] | null>(null)

const query = reactive<OperationLogQuery & { page: number; pageSize: number }>({
  page: 1,
  pageSize: 15,
  username: '',
  module: '',
  action: '',
  result: ''
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
    const res = await getOperationLogs(query)
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
  query.module = ''
  query.action = ''
  query.result = ''
  timeRange.value = null
  query.page = 1
  fetchData()
}

const detailVisible = ref(false)
const detail = ref<OperationLog | null>(null)

async function showDetail(id: number) {
  try {
    const res = await getOperationLogDetail(id)
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

.params-text {
  word-break: break-all;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  max-height: 200px;
  overflow-y: auto;
}

.error-text {
  color: var(--el-color-danger);
}
</style>
