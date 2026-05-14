<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">IP 黑名单</h2>
      <p class="page-desc">管理黑名单 IP，封禁可疑来源</p>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline size="default">
        <el-form-item label="IP 地址">
          <el-input v-model="query.ipAddress" placeholder="输入 IP" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" @click="showAdd">新增黑名单</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="ipAddress" label="IP 地址" width="170">
          <template #default="{ row }">
            <code class="ip-code">{{ row.ipAddress }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="加黑原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'info'" size="small">
              {{ row.status === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expiresAt" label="过期时间" width="170">
          <template #default="{ row }">
            <span v-if="row.expiresAt">{{ formatTime(row.expiresAt) }}</span>
            <span v-else class="text-muted">永久有效</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="创建人" width="110">
          <template #default="{ row }">
            {{ row.createdBy || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showEdit(row.id)">编辑</el-button>
            <el-popconfirm title="确定删除该黑名单条目？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'add' ? '新增黑名单' : '编辑黑名单'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="IP 地址" prop="ipAddress">
          <el-input v-model="form.ipAddress" placeholder="输入 IP 地址，如 192.168.1.100" />
        </el-form-item>
        <el-form-item label="加黑原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="2"
            placeholder="输入加黑原因"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expiresAt"
            type="datetime"
            placeholder="选择过期时间（留空表示永不过期）"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ formMode === 'add' ? '新增' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import {
  getBlacklist, addBlacklist, updateBlacklist, deleteBlacklist,
  type IpBlacklist, type BlacklistQuery, type BlacklistForm
} from '@/api/audit'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref<IpBlacklist[]>([])
const total = ref(0)

const query = reactive<BlacklistQuery & { page: number; pageSize: number }>({
  page: 1,
  pageSize: 15,
  ipAddress: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getBlacklist(query)
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
  query.ipAddress = ''
  query.status = undefined
  query.page = 1
  fetchData()
}

const formVisible = ref(false)
const formMode = ref<'add' | 'edit'>('add')
const editId = ref(0)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<BlacklistForm>({
  ipAddress: '',
  reason: '',
  status: 1,
  expiresAt: undefined
})

const formRules: FormRules = {
  ipAddress: [
    { required: true, message: 'IP 地址不能为空', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '加黑原因不能为空', trigger: 'blur' }
  ]
}

function resetForm() {
  form.ipAddress = ''
  form.reason = ''
  form.status = 1
  form.expiresAt = undefined
}

function showAdd() {
  formMode.value = 'add'
  resetForm()
  formVisible.value = true
}

async function showEdit(id: number) {
  formMode.value = 'edit'
  editId.value = id
  try {
    // 使用 getBlacklist 分页接口不便获取单个，这里用 alert detail 类似调用
    // 改为通过 getBlacklist 列表中的记录
    const item = tableData.value.find(r => r.id === id)
    if (item) {
      form.ipAddress = item.ipAddress
      form.reason = item.reason
      form.status = item.status
      form.expiresAt = item.expiresAt || undefined
    }
    // 如果当前页没有该记录，先获取单条
    if (!item) {
      // 直接调用 API 获取单条（复用 alert 的 pattern，实际上黑名单没有单独 GET 接口）
      // 这里手动构造即可，因为黑名单数据简单
      form.ipAddress = ''
      form.reason = ''
      form.status = 1
      form.expiresAt = undefined
    }
  } catch {
    resetForm()
  }
  formVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (formMode.value === 'add') {
      await addBlacklist(form)
    } else {
      await updateBlacklist(editId.value, form)
    }
    formVisible.value = false
    fetchData()
  } catch {
    // 操作失败
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteBlacklist(id)
    fetchData()
  } catch {
    // 删除失败
  }
}

function formatTime(str: string): string {
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
  .table-toolbar {
    margin-bottom: 16px;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}

.ip-code {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 3px;
  color: var(--text-primary);
}

.text-muted {
  color: var(--text-placeholder);
  font-size: 12px;
}
</style>
