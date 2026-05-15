<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">文档管理</h2>
      <p class="page-desc">上传制度文件，系统自动解析文本并生成检索片段</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="searchKeyword" placeholder="搜索文档标题或文件名" clearable style="width: 260px" @clear="fetchList" @keyup.enter="fetchList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterType" placeholder="文件类型" clearable style="width: 130px; margin-left: 12px" @change="fetchList">
            <el-option label="PDF" value="PDF" />
            <el-option label="DOCX" value="DOCX" />
            <el-option label="TXT" value="TXT" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 130px; margin-left: 12px" @change="fetchList">
            <el-option label="已就绪" value="ready" />
            <el-option label="处理中" value="processing" />
            <el-option label="失败" value="failed" />
          </el-select>
          <el-button style="margin-left: 12px" @click="fetchList">查询</el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" @click="showUpload = true">
            <el-icon style="margin-right:6px"><Upload /></el-icon>上传文档
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="文档标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="80" />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="片段数" width="80" align="center" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'ready'" type="success" size="small">已就绪</el-tag>
            <el-tag v-else-if="row.status === 'processing'" type="warning" size="small">处理中</el-tag>
            <el-tag v-else type="danger" size="small">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadUsername" label="上传人" width="100" />
        <el-table-column label="上传时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 上传对话框 -->
    <el-dialog v-model="showUpload" title="上传文档" width="480px" destroy-on-close>
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="文档标题" required>
          <el-input v-model="uploadForm.title" placeholder="请输入文档标题（可选，默认使用文件名）" />
        </el-form-item>
        <el-form-item label="选择文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeUpload"
            accept=".pdf,.docx,.txt"
          >
            <el-button type="primary" plain>
              <el-icon style="margin-right:6px"><Upload /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">支持 PDF / DOCX / TXT，单文件不超过 10MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">确认上传</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="文档详情" width="720px">
      <template v-if="detailDoc">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="标题">{{ detailDoc.title }}</el-descriptions-item>
          <el-descriptions-item label="文件名">{{ detailDoc.fileName }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detailDoc.fileType }}</el-descriptions-item>
          <el-descriptions-item label="大小">{{ formatSize(detailDoc.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="detailDoc.status === 'ready'" type="success" size="small">已就绪</el-tag>
            <el-tag v-else-if="detailDoc.status === 'processing'" type="warning" size="small">处理中</el-tag>
            <el-tag v-else type="danger" size="small">失败</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="片段数">{{ detailDoc.chunkCount }}</el-descriptions-item>
          <el-descriptions-item label="上传人">{{ detailDoc.uploadUsername }}</el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ formatTime(detailDoc.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 12px; font-size: 15px;">文本片段列表</h4>
        <div v-loading="chunksLoading">
          <div v-if="chunks.length === 0 && !chunksLoading" class="empty-chunks">暂无片段数据</div>
          <div v-for="chunk in chunks" :key="chunk.id" class="chunk-item">
            <div class="chunk-header">
              <el-tag size="small" type="info">第 {{ chunk.chunkIndex }} 段</el-tag>
              <span class="chunk-len">{{ chunk.charCount }} 字符</span>
            </div>
            <div class="chunk-content">{{ chunk.content }}</div>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Upload } from '@element-plus/icons-vue'
import {
  getDocumentList, getDocumentById, getDocumentChunks,
  uploadDocument, deleteDocument,
  type KnowledgeDocument, type ChunkVo
} from '@/api/knowledge'

const searchKeyword = ref('')
const filterType = ref('')
const filterStatus = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref<KnowledgeDocument[]>([])
const loading = ref(false)

// 上传相关
const MAX_FILE_SIZE = 10 * 1024 * 1024

const showUpload = ref(false)
const uploading = ref(false)
const uploadForm = ref({ title: '' })
const selectedFile = ref<File | null>(null)
const uploadRef = ref<any>(null)

// 详情相关
const showDetailDialog = ref(false)
const detailDoc = ref<KnowledgeDocument | null>(null)
const chunks = ref<ChunkVo[]>([])
const chunksLoading = ref(false)

onMounted(() => { fetchList() })

async function fetchList() {
  loading.value = true
  try {
    const res = await getDocumentList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      fileType: filterType.value || undefined,
      status: filterStatus.value || undefined
    })
    const data = res.data.data
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    loading.value = false
  }
}

function handleFileChange(file: any) {
  const raw = file.raw as File
  if (!raw) return

  const ext = raw.name.split('.').pop()?.toUpperCase()
  const validExts = ['PDF', 'DOCX', 'TXT']
  if (!validExts.includes(ext || '')) {
    ElMessage.error('仅支持 PDF / DOCX / TXT 格式')
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    return
  }
  if (raw.size > MAX_FILE_SIZE) {
    ElMessage.error('文件大小不能超过 10MB')
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    return
  }
  selectedFile.value = raw
}

function handleFileRemove() {
  selectedFile.value = null
}

function beforeUpload(file: File) {
  const validTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain']
  const ext = file.name.split('.').pop()?.toUpperCase()
  const validExts = ['PDF', 'DOCX', 'TXT']
  if (!validExts.includes(ext || '')) {
    ElMessage.error('仅支持 PDF / DOCX / TXT 格式')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  if (selectedFile.value.size > MAX_FILE_SIZE) {
    ElMessage.error('文件大小不能超过 10MB')
    selectedFile.value = null
    uploadRef.value?.clearFiles()
    return
  }
  uploading.value = true
  try {
    await uploadDocument(selectedFile.value, uploadForm.value.title)
    ElMessage.success('上传成功，文档正在处理中')
    showUpload.value = false
    uploadForm.value.title = ''
    selectedFile.value = null
    fetchList()
  } catch {
    // 错误已处理
  } finally {
    uploading.value = false
  }
}

async function showDetail(row: KnowledgeDocument) {
  detailDoc.value = row
  showDetailDialog.value = true
  chunksLoading.value = true
  try {
    const res = await getDocumentChunks(row.id)
    chunks.value = res.data.data || []
  } catch {
    chunks.value = []
  } finally {
    chunksLoading.value = false
  }
}

async function handleDelete(row: KnowledgeDocument) {
  try {
    await ElMessageBox.confirm(`确定要删除文档「${row.title}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteDocument(row.id)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 取消删除
  }
}

function formatSize(bytes: number): string {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

function formatTime(time: string): string {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped lang="scss">
.toolbar-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.empty-chunks {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.chunk-item {
  margin-bottom: 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  overflow: hidden;
}

.chunk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: var(--fill-color-light);
}

.chunk-len {
  font-size: 12px;
  color: var(--text-secondary);
}

.chunk-content {
  padding: 12px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
