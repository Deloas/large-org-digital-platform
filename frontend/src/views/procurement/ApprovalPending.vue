<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">待办审批</h2>
      <p class="page-desc">查看和处理待您审批的采购申请</p>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="tableLoading" border stripe>
        <el-table-column label="审批编号" width="80" prop="id" />
        <el-table-column label="步骤" width="100" align="center">
          <template #default="{ row }">
            第{{ row.stepOrder }}步
          </template>
        </el-table-column>
        <el-table-column label="审批角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ expectedRoleLabel(row.expectedRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">待审批</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" prop="createdAt" />
        <el-table-column label="操作" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text type="success" size="small" @click="openApprove(row)">通过</el-button>
            <el-button text type="danger" size="small" @click="openReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :close-on-click-modal="false" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="comment" type="textarea" :rows="3" placeholder="请输入审批意见（选填）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingApprovals, approveApproval, rejectApproval, type ProcurementApproval } from '@/api/procurement'

const tableData = ref<ProcurementApproval[]>([])
const tableLoading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const comment = ref('')
const currentApproval = ref<ProcurementApproval | null>(null)
const actionType = ref<'approve' | 'reject'>('approve')
const submitting = ref(false)

function expectedRoleLabel(role: string) {
  const map: Record<string, string> = { dept_manager: '部门负责人', finance: '财务负责人', procurement: '采购管理员' }
  return map[role] || role
}

async function fetchData() {
  tableLoading.value = true
  try {
    const res = await getPendingApprovals({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    tableLoading.value = false
  }
}

function openApprove(row: ProcurementApproval) {
  currentApproval.value = row
  actionType.value = 'approve'
  dialogTitle.value = '审批通过'
  comment.value = ''
  dialogVisible.value = true
}

function openReject(row: ProcurementApproval) {
  currentApproval.value = row
  actionType.value = 'reject'
  dialogTitle.value = '审批驳回'
  comment.value = ''
  dialogVisible.value = true
}

async function confirmAction() {
  if (!currentApproval.value) return
  submitting.value = true
  try {
    if (actionType.value === 'approve') {
      await approveApproval(currentApproval.value.id, comment.value || undefined)
      ElMessage.success('审批通过')
    } else {
      await rejectApproval(currentApproval.value.id, comment.value || undefined)
      ElMessage.success('已驳回')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
