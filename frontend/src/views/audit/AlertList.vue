<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">安全告警</h2>
      <p class="page-desc">查看和处理系统安全告警</p>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline size="default">
        <el-form-item label="告警类型">
          <el-select v-model="query.alertType" placeholder="全部" clearable style="width:180px">
            <el-option label="暴力破解" value="brute_force" />
            <el-option label="撞库风险" value="credential_stuffing" />
            <el-option label="非工作时间管理员登录" value="off_hours_admin" />
            <el-option label="多 IP 登录" value="multi_ip" />
            <el-option label="黑名单 IP 登录" value="blacklisted_ip" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重级别">
          <el-select v-model="query.severity" placeholder="全部" clearable style="width:120px">
            <el-option label="高危" value="high" />
            <el-option label="中危" value="medium" />
            <el-option label="低危" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="未读" value="unread" />
            <el-option label="已读" value="read" />
            <el-option label="已处理" value="resolved" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联用户">
          <el-input v-model="query.relatedUser" placeholder="输入用户名" clearable style="width:160px" />
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
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="告警标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="alertType" label="类型" width="140">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.alertType)" size="small">{{ typeLabel(row.alertType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="sevTag(row.severity)" size="small" effect="dark">{{ sevLabel(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedUser" label="关联用户" width="120">
          <template #default="{ row }">
            {{ row.relatedUser || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="relatedIp" label="关联 IP" width="150">
          <template #default="{ row }">
            {{ row.relatedIp || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duplicateCount" label="重复次数" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.duplicateCount > 1" class="dup-count">{{ row.duplicateCount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastTime" label="最近时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.lastTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row.id)">详情</el-button>
            <el-button
              v-if="row.status === 'unread'"
              type="warning" link size="small"
              @click="changeStatus(row.id, 'read')"
            >标为已读</el-button>
            <el-button
              v-if="row.status === 'unread' || row.status === 'read'"
              type="success" link size="small"
              @click="showResolve(row.id)"
            >处理</el-button>
            <el-button
              v-if="row.status === 'unread' || row.status === 'read'"
              type="info" link size="small"
              @click="changeStatus(row.id, 'ignored')"
            >忽略</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无告警" />

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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="告警详情" width="620px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="告警标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="告警类型">
          <el-tag :type="typeTag(detail.alertType)" size="small">{{ typeLabel(detail.alertType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="严重级别">
          <el-tag :type="sevTag(detail.severity)" size="small" effect="dark">{{ sevLabel(detail.severity) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联用户">{{ detail.relatedUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联 IP">{{ detail.relatedIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.status)" size="small">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="重复次数">{{ detail.duplicateCount }}</el-descriptions-item>
        <el-descriptions-item label="首次检测">{{ formatTime(detail.firstTime) }}</el-descriptions-item>
        <el-descriptions-item label="最近检测">{{ formatTime(detail.lastTime) }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detail.handler || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理备注">{{ detail.handleNote || '-' }}</el-descriptions-item>
        <el-descriptions-item label="证据数据" :span="2">
          <pre class="detail-json">{{ formatJson(detail.detail) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button
          v-if="detail && (detail.status === 'unread' || detail.status === 'read')"
          type="primary"
          @click="showResolveFromDetail()"
        >处理</el-button>
        <el-button
          v-if="detail && (detail.status === 'unread' || detail.status === 'read')"
          @click="changeStatusFromDetail('ignored')"
        >忽略</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 处理弹窗 -->
    <el-dialog v-model="resolveVisible" title="处理告警" width="440px">
      <el-form label-width="80px">
        <el-form-item label="处理备注">
          <el-input
            v-model="resolveNote"
            type="textarea"
            :rows="3"
            placeholder="输入处理备注（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResolve">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { getAlerts, getAlertDetail, updateAlertStatus, type SecurityAlert, type AlertQuery } from '@/api/audit'

const loading = ref(false)
const tableData = ref<SecurityAlert[]>([])
const total = ref(0)
const timeRange = ref<[string, string] | null>(null)

const query = reactive<AlertQuery & { page: number; pageSize: number }>({
  page: 1,
  pageSize: 15,
  alertType: '',
  severity: '',
  status: '',
  relatedUser: ''
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
    const res = await getAlerts(query)
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
  query.alertType = ''
  query.severity = ''
  query.status = ''
  query.relatedUser = ''
  timeRange.value = null
  query.page = 1
  fetchData()
}

const detailVisible = ref(false)
const detail = ref<SecurityAlert | null>(null)

async function showDetail(id: number) {
  try {
    const res = await getAlertDetail(id)
    detail.value = res.data.data
    detailVisible.value = true
  } catch {
    detail.value = null
  }
}

const resolveVisible = ref(false)
const resolveTargetId = ref(0)
const resolveNote = ref('')

function showResolve(id: number) {
  resolveTargetId.value = id
  resolveNote.value = ''
  resolveVisible.value = true
}

function showResolveFromDetail() {
  detailVisible.value = false
  if (detail.value) {
    resolveTargetId.value = detail.value.id
    resolveNote.value = ''
    resolveVisible.value = true
  }
}

async function confirmResolve() {
  await changeStatus(resolveTargetId.value, 'resolved', resolveNote.value || undefined)
  resolveVisible.value = false
}

async function changeStatus(id: number, status: string, note?: string) {
  try {
    await updateAlertStatus(id, status, note)
    fetchData()
    if (detail.value && detail.value.id === id) {
      detail.value = null
    }
  } catch {
    // 操作失败
  }
}

async function changeStatusFromDetail(status: string) {
  if (!detail.value) return
  await changeStatus(detail.value.id, status)
  detailVisible.value = false
}

function typeLabel(t: string): string {
  const map: Record<string, string> = {
    brute_force: '暴力破解',
    credential_stuffing: '撞库风险',
    off_hours_admin: '非工作时间管理员登录',
    multi_ip: '多 IP 登录',
    blacklisted_ip: '黑名单 IP 登录'
  }
  return map[t] || t
}

function typeTag(t: string): string {
  const map: Record<string, string> = {
    brute_force: 'danger',
    credential_stuffing: 'warning',
    off_hours_admin: 'info',
    multi_ip: '',
    blacklisted_ip: 'danger'
  }
  return map[t] || 'info'
}

function sevLabel(s: string): string {
  if (s === 'high') return '高危'
  if (s === 'medium') return '中危'
  return '低危'
}

function sevTag(s: string): string {
  if (s === 'high') return 'danger'
  if (s === 'medium') return 'warning'
  return 'info'
}

function statusLabel(s: string): string {
  const map: Record<string, string> = {
    unread: '未读',
    read: '已读',
    resolved: '已处理',
    ignored: '已忽略'
  }
  return map[s] || s
}

function statusTag(s: string): string {
  const map: Record<string, string> = {
    unread: 'danger',
    read: 'warning',
    resolved: 'success',
    ignored: 'info'
  }
  return map[s] || 'info'
}

function formatTime(str: string): string {
  if (!str) return '-'
  const d = new Date(str)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function formatJson(str: string): string {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
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

.dup-count {
  display: inline-block;
  background: var(--color-danger);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
  padding: 1px 7px;
  min-width: 22px;
  text-align: center;
}

.detail-json {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  color: var(--text-secondary);
  background: #f8fafc;
  padding: 10px;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
