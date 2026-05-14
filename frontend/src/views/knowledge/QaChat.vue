<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">智能问答</h2>
      <p class="page-desc">基于知识库制度文件进行自然语言问答，回答附带引用来源</p>
    </div>

    <div class="qa-layout">
      <div class="qa-sidebar">
        <el-card shadow="never" class="sidebar-card">
          <template #header>
            <div class="sidebar-title">
              <el-icon><Folder /></el-icon>
              <span>知识库文档</span>
            </div>
          </template>
          <div v-loading="docsLoading" class="doc-list">
            <div v-if="documents.length === 0 && !docsLoading" class="doc-empty">暂无文档</div>
            <div v-for="doc in documents" :key="doc.id" class="doc-item">
              <div class="doc-name">{{ doc.title }}</div>
              <div class="doc-meta">{{ doc.fileType }} · {{ doc.chunkCount }} 片段</div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="qa-main">
        <el-card shadow="never" class="chat-card">
          <div class="chat-messages" ref="chatMessagesRef">
            <div v-if="messages.length === 0" class="chat-welcome">
              <el-icon :size="48" class="welcome-icon"><ChatDotRound /></el-icon>
              <h3>知识库智能问答</h3>
              <p>输入您的问题，系统将在已上传的制度文件中检索相关内容并给出回答</p>
              <div class="example-questions">
                <span class="example-label">试试问：</span>
                <el-tag
                  v-for="q in exampleQuestions" :key="q"
                  class="example-tag"
                  @click="sendExample(q)"
                >{{ q }}</el-tag>
              </div>
            </div>

            <div v-for="(msg, idx) in messages" :key="idx" :class="['message-item', msg.role]">
              <div class="message-avatar">
                <el-icon v-if="msg.role === 'user'" :size="20"><User /></el-icon>
                <el-icon v-else :size="20"><Cpu /></el-icon>
              </div>
              <div class="message-body">
                <div class="message-content">{{ msg.content }}</div>

                <div v-if="msg.sources && msg.sources.length > 0" class="message-sources">
                  <div class="sources-title">引用来源：</div>
                  <div v-for="(src, si) in msg.sources" :key="si" class="source-item">
                    <el-tag size="small" type="warning">{{ src.documentTitle }}</el-tag>
                    <span class="source-meta">第 {{ src.chunkIndex }} 段</span>
                    <p class="source-snippet">{{ src.snippet }}</p>
                  </div>
                </div>

                <div v-if="msg.status === 'no_match'" class="message-no-match">
                  <el-icon><Warning /></el-icon>
                  <span>当前知识库未找到足够依据，无法回答该问题</span>
                </div>

                <div v-if="msg.confidence !== undefined" class="message-meta">
                  <span>置信度：{{ (msg.confidence * 100).toFixed(1) }}%</span>
                  <span class="meta-div">|</span>
                  <span>耗时：{{ msg.costMs }}ms</span>
                </div>
              </div>
            </div>

            <div v-if="thinking" class="message-item bot">
              <div class="message-avatar">
                <el-icon :size="20"><Cpu /></el-icon>
              </div>
              <div class="message-body">
                <div class="thinking-dots">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="chat-input">
            <el-input
              v-model="question"
              type="textarea"
              :rows="2"
              placeholder="请输入您的问题..."
              @keyup.enter.exact.native="handleAsk"
            />
            <el-button
              type="primary"
              :loading="thinking"
              :disabled="!question.trim()"
              @click="handleAsk"
              style="margin-top: 10px;"
            >
              <el-icon style="margin-right:6px"><Promotion /></el-icon>发送
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Cpu, Folder, Promotion, Warning } from '@element-plus/icons-vue'
import { askQuestion, getDocumentList, type KnowledgeDocument, type SourceVo } from '@/api/knowledge'

interface Message {
  role: 'user' | 'bot'
  content: string
  sources?: SourceVo[]
  confidence?: number
  costMs?: number
  status?: string
}

const exampleQuestions = [
  '公司的考勤制度是什么？',
  '员工出差报销流程是怎样的？',
  '采购审批需要经过哪些环节？'
]

const question = ref('')
const messages = ref<Message[]>([])
const thinking = ref(false)
const chatMessagesRef = ref<HTMLElement | null>(null)

const documents = ref<KnowledgeDocument[]>([])
const docsLoading = ref(false)

onMounted(() => { fetchDocuments() })

async function fetchDocuments() {
  docsLoading.value = true
  try {
    const res = await getDocumentList({ pageNum: 1, pageSize: 50, status: 'ready' })
    documents.value = res.data.data.records || []
  } catch {
    documents.value = []
  } finally {
    docsLoading.value = false
  }
}

function sendExample(q: string) {
  question.value = q
  handleAsk()
}

async function handleAsk() {
  const q = question.value.trim()
  if (!q || thinking.value) return

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  thinking.value = true

  await nextTick()
  scrollToBottom()

  try {
    const res = await askQuestion(q)
    const data = res.data.data

    if (data.status === 'no_match') {
      messages.value.push({
        role: 'bot',
        content: '',
        status: 'no_match',
        confidence: 0,
        costMs: data.costMs
      })
    } else {
      messages.value.push({
        role: 'bot',
        content: data.answer,
        sources: data.sources,
        confidence: data.confidence,
        costMs: data.costMs,
        status: 'answered'
      })
    }
    // 刷新左侧文档列表
    fetchDocuments()
  } catch {
    // 错误已处理
  } finally {
    thinking.value = false
    await nextTick()
    scrollToBottom()
  }
}

function scrollToBottom() {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}
</script>

<style scoped lang="scss">
.qa-layout {
  display: flex;
  gap: 16px;
  height: calc(100vh - 180px);
  min-height: 500px;
}

.qa-sidebar {
  width: 260px;
  flex-shrink: 0;

  .sidebar-card {
    height: 100%;
    :deep(.el-card__body) { padding: 0; }
  }
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.doc-list {
  padding: 8px 12px;
  overflow-y: auto;
  max-height: calc(100% - 50px);
}

.doc-empty {
  text-align: center;
  padding: 32px 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.doc-item {
  padding: 10px 12px;
  border-radius: 6px;
  margin-bottom: 6px;
  transition: background 0.2s;

  &:hover { background: var(--fill-color-light); }
}

.doc-name {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-meta {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 3px;
}

.qa-main {
  flex: 1;
  min-width: 0;

  .chat-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 0;
    }
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-welcome {
  text-align: center;
  padding: 60px 20px;

  .welcome-icon { color: var(--color-primary); margin-bottom: 16px; }

  h3 { font-size: 18px; color: var(--text-primary); margin: 0 0 8px; font-weight: 600; }

  p { color: var(--text-secondary); font-size: 14px; margin: 0 0 20px; }
}

.example-questions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.example-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.example-tag {
  cursor: pointer;
  transition: all 0.2s;
  &:hover { opacity: 0.8; }
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.bot { .message-content { background: var(--fill-color-light); } }
  &.user {
    flex-direction: row-reverse;
    .message-content { background: var(--color-primary); color: #fff; }
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--fill-color);
  color: var(--text-secondary);

  .user & { background: var(--color-primary); color: #fff; }
}

.message-body {
  max-width: 75%;
}

.message-content {
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-sources {
  margin-top: 10px;
  padding: 12px;
  background: var(--fill-color);
  border-radius: 8px;
  border-left: 3px solid var(--color-warning);
}

.sources-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.source-item {
  margin-bottom: 8px;
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

.message-no-match {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(230, 162, 60, 0.1);
  border-radius: 8px;
  color: #b88230;
  font-size: 14px;
}

.message-meta {
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-placeholder);

  .meta-div { margin: 0 8px; }
}

.thinking-dots {
  display: flex;
  gap: 6px;
  padding: 12px 16px;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--text-placeholder);
    animation: dot-bounce 1.4s infinite ease-in-out both;
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input {
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
}
</style>
