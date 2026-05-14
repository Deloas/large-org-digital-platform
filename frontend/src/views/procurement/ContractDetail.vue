<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">合同详情</h2>
      <p class="page-desc">{{ contract.contractNo }}</p>
    </div>
    <el-card v-loading="loading" shadow="never" class="detail-card">
      <template v-if="contract.id">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合同编号">{{ contract.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(contract.status)" size="small">{{ statusLabel(contract.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="合同标题">{{ contract.title }}</el-descriptions-item>
          <el-descriptions-item label="合同金额">
            {{ formatMoney(contract.amount) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="关联申请ID">{{ contract.requestId }}</el-descriptions-item>
          <el-descriptions-item label="关联供应商ID">{{ contract.supplierId }}</el-descriptions-item>
          <el-descriptions-item label="签订日期">{{ contract.signedDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="到期日期">{{ contract.expiryDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ contract.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ contract.updatedAt || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">付款节点</el-divider>
        <el-button type="primary" size="small" @click="openPaymentCreate" style="margin-bottom: 12px">新增付款节点</el-button>
        <el-table :data="payments" border stripe>
          <el-table-column prop="nodeName" label="节点名称" width="140" />
          <el-table-column prop="amount" label="金额(元)" width="140" align="right">
            <template #default="{ row }">
              {{ formatMoney(row.amount) }}
            </template>
          </el-table-column>
          <el-table-column prop="ratio" label="比例(%)" width="100" align="center">
            <template #default="{ row }">
              {{ row.ratio || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="plannedDate" label="计划付款日" width="130" />
          <el-table-column prop="actualDate" label="实际付款日" width="130">
            <template #default="{ row }">
              {{ row.actualDate || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'paid' ? 'success' : 'warning'" size="small">
                {{ row.status === 'paid' ? '已付款' : '待付款' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 'pending'" text type="success" size="small" @click="handlePay(row.id)">确认付款</el-button>
              <el-button v-if="row.status === 'pending'" text type="danger" size="small" @click="handleDeletePayment(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <div class="detail-footer">
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </el-card>

    <el-dialog v-model="paymentDialogVisible" title="新增付款节点" :close-on-click-modal="false" width="480px">
      <el-form ref="paymentFormRef" :model="paymentForm" :rules="paymentRules" label-width="100px">
        <el-form-item label="节点名称" prop="nodeName">
          <el-select v-model="paymentForm.nodeName" placeholder="请选择" style="width: 100%">
            <el-option label="预付款" value="预付款" />
            <el-option label="到货款" value="到货款" />
            <el-option label="验收款" value="验收款" />
            <el-option label="质保金" value="质保金" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款金额" prop="amount">
          <el-input-number v-model="paymentForm.amount" :min="0" :precision="2" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款比例">
          <el-input-number v-model="paymentForm.ratio" :min="0" :max="100" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="paymentForm.plannedDate" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paymentSubmitting" @click="savePayment">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getContractById, getPaymentNodes, createPayment, deletePayment, confirmPayment } from '@/api/procurement'
import type { Contract, PaymentNode } from '@/api/procurement'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const contract = ref<Contract>({} as Contract)
const payments = ref<PaymentNode[]>([])

const paymentDialogVisible = ref(false)
const paymentSubmitting = ref(false)
const paymentFormRef = ref<FormInstance>()
const paymentForm = reactive({
  nodeName: '',
  amount: 0,
  ratio: undefined as number | undefined,
  plannedDate: ''
})
const paymentRules: FormRules = {
  nodeName: [{ required: true, message: '请选择节点名称', trigger: 'change' }],
  amount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }]
}

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
  loading.value = true
  try {
    const id = Number(route.params.id)
    const [conRes, payRes] = await Promise.all([getContractById(id), getPaymentNodes(id)])
    contract.value = conRes.data.data
    payments.value = payRes.data.data
  } finally {
    loading.value = false
  }
}

function openPaymentCreate() {
  paymentForm.nodeName = ''
  paymentForm.amount = 0
  paymentForm.ratio = undefined
  paymentForm.plannedDate = ''
  paymentDialogVisible.value = true
}

async function savePayment() {
  const valid = await paymentFormRef.value?.validate().catch(() => false)
  if (!valid) return
  paymentSubmitting.value = true
  try {
    await createPayment({
      contractId: contract.value.id,
      nodeName: paymentForm.nodeName,
      amount: paymentForm.amount,
      ratio: paymentForm.ratio,
      plannedDate: paymentForm.plannedDate || undefined
    })
    ElMessage.success('付款节点创建成功')
    paymentDialogVisible.value = false
    fetchData()
  } finally {
    paymentSubmitting.value = false
  }
}

async function handlePay(id: number) {
  try {
    await ElMessageBox.confirm('确认此付款节点已付款？', '确认付款')
    await confirmPayment(id)
    ElMessage.success('付款确认成功')
    fetchData()
  } catch { /* cancelled */ }
}

async function handleDeletePayment(id: number) {
  try {
    await ElMessageBox.confirm('确认删除该付款节点？', '确认删除', { type: 'warning' })
    await deletePayment(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

function goBack() {
  router.push('/procurement/contracts')
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.detail-card {
  :deep(.el-card__body) { padding: 24px; }
}
.detail-footer {
  margin-top: 24px;
  text-align: right;
}
</style>
