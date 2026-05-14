<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">问答日志</h2>
      <p class="page-desc">查看历史问答记录，回顾问题与回答详情</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="filterUsername" placeholder="搜索用户名" clearable style="width: 200px" @clear="fetchList" @keyup.enter="fetchList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 130px; margin-left: 12px" @change="fetchList">
            <el-option label="已回答" value="answered" />
            <el-option label="未匹配" value="no_match" />
          </el-select>
          <el-date-picker
            v-model="filterDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="margin-left: 12px"
            @change="fetchList"
          />
          <el-button style="margin-left: 12px" @click="fetchList">查询</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column label="问题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ truncate(row.question, 60) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'answered'" type="success" size="small">已回答</el-tag>
            <el-tag v-else type="warning" size="small">未匹配</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置信度" width="90" align="center">
          <template #default="{ row }">
            {{ row.confidence ? (row.confidence * 100).toFixed(1) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="80" align="center">
          <template #default="{ row }">{{ row.costMs }}ms</template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="问答详情" width="680px">
      <template v-if="detailLog">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="用户">{{ detailLog.username }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="detailLog.status === 'answered'" type="success" size="small">已回答</el-tag>
            <el-tag v-else type="warning" size="small">未匹配</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="置信度">
            {{ detailLog.confidence ? (detailLog.confidence * 100).toFixed(1) + '%' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="耗时">{{ detailLog.costMs }}ms</el-descriptions-item>
          <el-descriptions-item label="时间" :span="2">{{ formatTime(detailLog.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 8px; font-size: 15px;">问题</h4>
        <div class="qa-detail-text">{{ detailLog.question }}</div>

        <h4 style="margin: 20px 0 8px; font-size: 15px;">回答</h4>
        <div class="qa-detail-text">{{ detailLog.answer || '（未找到匹配内容）' }}</div>

        <div v-if="detailLog.sources && detailLog.sources.length > 0" style="margin-top: 20px;">
          <h4 style="margin: 0 0 8px; font-size: 15px;">引用来源</h4>
          <div v-for="(src, si) in detailLog.sources" :key="si" class="source-item">
            <el-tag size="small" type="warning">{{ src.documentTitle }}</el-tag>
            <span class="source-meta">第 {{ src.chunkIndex }} 段</span>
            <p class="source-snippet">{{ src.snippet }}</p>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getQaLogs, type QaLogVo, type QaLogQuery } from '@/api/knowledge'

const filterUsername = ref('')
const filterStatus = ref('')
const filterDateRange = ref<string[] | null>(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref<QaLogVo[]>([])
const loading = ref(false)

const showDetailDialog = ref(false)
const detailLog = ref<QaLogVo | null>(null)

onMounted(() => { fetchList() })

async function fetchList() {
  loading.value = true
  try {
    const params: QaLogQuery = {
      page: pageNum.value,
      pageSize: pageSize.value,
      username: filterUsername.value || undefined,
      status: filterStatus.value || undefined
    }
    if (filterDateRange.value && filterDateRange.value.length === 2) {
      params.startTime = filterDateRange.value[0]
      params.endTime = filterDateRange.value[1]
    }
    const res = await getQaLogs(params)
    const data = res.data.data
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

function showDetail(row: QaLogVo) {
  detailLog.value = row
  showDetailDialog.value = true
}

function truncate(text: string, maxLen: number): string {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

function formatTime(time: string): string {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped lang="scss">
.toolbar-card { margin-bottom: 16px; }
.toolbar { display: flex; align-items: center; }
.toolbar-left { display: flex; align-items: center; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }

.qa-detail-text {
  padding: 12px 16px;
  background: var(--fill-color-light);
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}

.source-item {
  margin-bottom: 8px;
  padding: 10px 12px;
  background: var(--fill-color-light);
  border-radius: 6px;
  border-left: 3px solid var(--color-warning);
  &:last-child { margin-bottom: 0; }
}

.source-meta {
  font-size: 11px;
  color: var(--text-secondary);
  margin-left: 8px;
}

.source-snippet {
  font-size: 12px;
  color: var(--text-regular);
  margin: 4px 0 0;
  line-height: 1.5;
}
</style>
