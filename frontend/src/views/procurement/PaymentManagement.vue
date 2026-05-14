<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">付款管理</h2>
      <p class="page-desc">选择合同查看和管理付款节点</p>
    </div>
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-form :inline="true">
          <el-form-item label="合同编号">
            <el-input v-model="contractSearch" placeholder="请输入合同编号" clearable style="width: 240px" @keyup.enter="searchContract" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchContract">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div v-if="contractNotFound" class="empty-hint">未找到该合同，请确认合同编号</div>
      <template v-if="contract.id">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="合同编号">{{ contract.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="合同标题">{{ contract.title }}</el-descriptions-item>
          <el-descriptions-item label="金额">{{ formatMoney(contract.amount) }} 元</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">付款节点</el-divider>
        <el-button type="primary" size="small" @click="openCreate" style="margin-bottom: 12px">新增付款节点</el-button>
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
              <el-button v-if="row.status === 'pending'" text type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="payments.length === 0" class="empty-hint">暂无付款节点</div>
      </template>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增付款节点" :close-on-click-modal="false" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="节点名称" prop="nodeName">
          <el-select v-model="form.nodeName" placeholder="请选择" style="width: 100%">
            <el-option label="预付款" value="预付款" />
            <el-option label="到货款" value="到货款" />
            <el-option label="验收款" value="验收款" />
            <el-option label="质保金" value="质保金" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款比例">
          <el-input-number v-model="form.ratio" :min="0" :max="100" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="form.plannedDate" type="date" style="width: 100%" />
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getContractList, getPaymentNodes, createPayment, deletePayment, confirmPayment } from '@/api/procurement'
import type { Contract, PaymentNode } from '@/api/procurement'

const contractSearch = ref('')
const contract = ref<Contract>({} as Contract)
const contractNotFound = ref(false)
const payments = ref<PaymentNode[]>([])

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  nodeName: '',
  amount: 0,
  ratio: undefined as number | undefined,
  plannedDate: ''
})
const rules: FormRules = {
  nodeName: [{ required: true, message: '请选择节点名称', trigger: 'change' }],
  amount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }]
}

function formatMoney(val: number) {
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function searchContract() {
  if (!contractSearch.value) return
  const res = await getContractList({ pageNum: 1, pageSize: 1, keyword: contractSearch.value })
  const records = res.data.data.records
  if (records.length > 0) {
    contract.value = records[0]
    contractNotFound.value = false
    const payRes = await getPaymentNodes(contract.value.id)
    payments.value = payRes.data.data
  } else {
    contract.value = {} as Contract
    payments.value = []
    contractNotFound.value = true
  }
}

function openCreate() {
  form.nodeName = ''
  form.amount = 0
  form.ratio = undefined
  form.plannedDate = ''
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createPayment({
      contractId: contract.value.id,
      nodeName: form.nodeName,
      amount: form.amount,
      ratio: form.ratio,
      plannedDate: form.plannedDate || undefined
    })
    ElMessage.success('付款节点创建成功')
    dialogVisible.value = false
    searchContract()
  } finally {
    submitting.value = false
  }
}

async function handlePay(id: number) {
  try {
    await ElMessageBox.confirm('确认此付款节点已付款？', '确认付款')
    await confirmPayment(id)
    ElMessage.success('付款确认成功')
    searchContract()
  } catch { /* cancelled */ }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除该付款节点？', '确认删除', { type: 'warning' })
    await deletePayment(id)
    ElMessage.success('删除成功')
    searchContract()
  } catch { /* cancelled */ }
}
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
.empty-hint {
  text-align: center;
  color: var(--text-secondary);
  padding: 24px 0;
}
</style>
