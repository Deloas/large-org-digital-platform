<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">知识库</h2>
      <p class="page-desc">制度文件管理、文档解析、智能问答与溯源</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon doc-icon"><el-icon :size="28"><Folder /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ stats.documentCount }}</div>
            <div class="stat-label">文档总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon chunk-icon"><el-icon :size="28"><Grid /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ stats.chunkCount }}</div>
            <div class="stat-label">文本片段</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon qa-icon"><el-icon :size="28"><ChatDotRound /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ stats.qaCount }}</div>
            <div class="stat-label">问答次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon ready-icon"><el-icon :size="28"><CircleCheck /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ stats.readyCount }}</div>
            <div class="stat-label">已就绪文档</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="nav-row">
      <el-col :span="8">
        <el-card shadow="never" class="nav-card" @click="$router.push('/knowledge/documents')">
          <el-icon :size="40" class="nav-icon"><Folder /></el-icon>
          <h3 class="nav-title">文档管理</h3>
          <p class="nav-desc">上传制度文件，支持 PDF / Word / TXT，自动解析文本并生成检索片段</p>
          <el-tag type="info" size="small">上传</el-tag>
          <el-tag type="info" size="small" style="margin-left:6px">查阅</el-tag>
          <el-tag type="info" size="small" style="margin-left:6px">删除</el-tag>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="nav-card" @click="$router.push('/knowledge/qa')">
          <el-icon :size="40" class="nav-icon"><ChatDotRound /></el-icon>
          <h3 class="nav-title">智能问答</h3>
          <p class="nav-desc">基于知识库制度文件进行自然语言问答，回答附带引用来源和溯源信息</p>
          <el-tag type="primary" size="small">提问</el-tag>
          <el-tag type="primary" size="small" style="margin-left:6px">溯源</el-tag>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="nav-card" @click="$router.push('/knowledge/qa/logs')">
          <el-icon :size="40" class="nav-icon"><Tickets /></el-icon>
          <h3 class="nav-title">问答日志</h3>
          <p class="nav-desc">查看历史问答记录，回顾问题与回答，追踪知识库使用情况</p>
          <el-tag type="warning" size="small">查询</el-tag>
          <el-tag type="warning" size="small" style="margin-left:6px">审计</el-tag>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Folder, Grid, ChatDotRound, CircleCheck, Tickets } from '@element-plus/icons-vue'
import { getDocumentList, getQaLogs } from '@/api/knowledge'

const stats = ref({
  documentCount: 0,
  chunkCount: 0,
  qaCount: 0,
  readyCount: 0
})

onMounted(async () => {
  try {
    const docRes = await getDocumentList({ pageNum: 1, pageSize: 1 })
    const docs = docRes.data.data
    stats.value.documentCount = docs.total || 0

    // 统计就绪文档
    const readyRes = await getDocumentList({ pageNum: 1, pageSize: 1, status: 'ready' })
    stats.value.readyCount = readyRes.data.data.total || 0

    // 统计问答次数
    const qaRes = await getQaLogs({ page: 1, pageSize: 1 })
    stats.value.qaCount = qaRes.data.data.total || 0

    // 统计 chunk（遍历文档）
    const allDocs = await getDocumentList({ pageNum: 1, pageSize: 100 })
    let totalChunks = 0
    if (allDocs.data.data.records) {
      for (const doc of allDocs.data.data.records) {
        totalChunks += doc.chunkCount || 0
      }
    }
    stats.value.chunkCount = totalChunks
  } catch {
    // 统计数据获取失败时保持默认值
  }
})
</script>

<style scoped lang="scss">
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  cursor: default;
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.doc-icon { background: rgba(64, 158, 255, 0.1); color: #409eff; }
  &.chunk-icon { background: rgba(103, 194, 58, 0.1); color: #67c23a; }
  &.qa-icon { background: rgba(230, 162, 60, 0.1); color: #e6a23c; }
  &.ready-icon { background: rgba(64, 158, 255, 0.1); color: #409eff; }
}

.stat-body {
  flex: 1;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.nav-card {
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  :deep(.el-card__body) {
    padding: 28px 24px;
    text-align: center;
  }
}

.nav-icon {
  color: var(--color-primary);
  margin-bottom: 12px;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px;
}

.nav-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 14px;
}
</style>
