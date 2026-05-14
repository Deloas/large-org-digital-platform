<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">采购申请详情</h2>
      <p class="page-desc">{{ request.requestNo }}</p>
    </div>
    <el-card v-loading="loading" shadow="never" class="detail-card">
      <template v-if="request.id">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请编号">{{ request.requestNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(request.status)" size="small">{{ statusLabel(request.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="采购标题">{{ request.title }}</el-descriptions-item>
          <el-descriptions-item label="采购品类">{{ request.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采购金额">
            {{ formatMoney(request.amount) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="审批进度">
            {{ request.totalSteps > 0 ? `${request.currentStep} / ${request.totalSteps}` : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="采购说明" :span="2">
            {{ request.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ request.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ request.updatedAt }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">审批记录</el-divider>
        <div v-if="approvals.length === 0" class="empty-hint">暂无审批记录</div>
        <el-timeline v-else class="approval-timeline">
          <el-timeline-item
            v-for="item in approvals"
            :key="item.id"
            :type="approvalTimelineType(item.status)"
            :timestamp="item.approvedAt || item.createdAt"
            placement="top"
          >
            <div class="approval-item">
              <span class="approval-step">第{{ item.stepOrder }}步 — {{ expectedRoleLabel(item.expectedRole) }}</span>
              <el-tag :type="approvalStatusType(item.status)" size="small" style="margin-left: 8px">
                {{ item.status === 'pending' ? '待审批' : item.status === 'approved' ? '已通过' : '已驳回' }}
              </el-tag>
              <p v-if="item.comment" class="approval-comment">{{ item.comment }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </template>
      <div class="detail-footer">
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRequestById, getRequestApprovals, type ProcurementRequest, type ProcurementApproval } from '@/api/procurement'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const request = ref<ProcurementRequest>({} as ProcurementRequest)
const approvals = ref<ProcurementApproval[]>([])

function formatMoney(val: number) {
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function statusType(status: string) {
  const map: Record<string, string> = { draft: 'info', pending: 'warning', approved: 'success', rejected: 'danger', withdrawn: 'info' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { draft: '草稿', pending: '审批中', approved: '已通过', rejected: '已驳回', withdrawn: '已撤回' }
  return map[status] || status
}

function approvalTimelineType(status: string) {
  return status === 'approved' ? 'success' : status === 'rejected' ? 'danger' : 'info'
}

function approvalStatusType(status: string) {
  return status === 'approved' ? 'success' : status === 'rejected' ? 'danger' : 'warning'
}

function expectedRoleLabel(role: string) {
  const map: Record<string, string> = { dept_manager: '部门负责人', finance: '财务负责人', procurement: '采购管理员' }
  return map[role] || role
}

async function fetchData() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const [reqRes, appRes] = await Promise.all([getRequestById(id), getRequestApprovals(id)])
    request.value = reqRes.data.data
    approvals.value = appRes.data.data
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/procurement/requests')
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.detail-card {
  :deep(.el-card__body) { padding: 24px; }
}

.approval-timeline {
  padding: 0 16px;
}

.approval-item {
  .approval-step { font-size: 14px; color: var(--text-primary); }
  .approval-comment { margin-top: 6px; font-size: 13px; color: var(--text-secondary); }
}

.empty-hint {
  text-align: center;
  color: var(--text-secondary);
  padding: 24px 0;
}

.detail-footer {
  margin-top: 24px;
  text-align: right;
}
</style>
