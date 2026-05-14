import { get, post, del } from './request'

// ---- 文档 ----
export interface KnowledgeDocument {
  id: number
  title: string
  fileName: string
  fileType: string
  fileSize: number
  contentText: string
  chunkCount: number
  status: string
  uploadUserId: number
  uploadUsername: string
  createdAt: string
  updatedAt: string
}

export interface ChunkVo {
  id: number
  documentId: number
  chunkIndex: number
  content: string
  charCount: number
}

export interface SourceVo {
  documentId: number
  documentTitle: string
  chunkIndex: number
  snippet: string
}

export interface QaResultVo {
  question: string
  answer: string
  confidence: number
  status: string
  costMs: number
  sources: SourceVo[]
  disclaimer: string
}

export interface QaLogVo {
  id: number
  userId: number
  username: string
  question: string
  answer: string
  sources: SourceVo[]
  confidence: number
  status: string
  costMs: number
  createdAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface QaLogQuery {
  page?: number
  pageSize?: number
  username?: string
  status?: string
  startTime?: string
  endTime?: string
}

// 文档接口
export function uploadDocument(file: File, title: string) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('title', title)
  return post<KnowledgeDocument>('/knowledge/documents', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getDocumentList(params: any) {
  return get<PageResult<KnowledgeDocument>>('/knowledge/documents', params)
}

export function getDocumentById(id: number) {
  return get<KnowledgeDocument>(`/knowledge/documents/${id}`)
}

export function deleteDocument(id: number) {
  return del(`/knowledge/documents/${id}`)
}

export function getDocumentChunks(id: number) {
  return get<ChunkVo[]>(`/knowledge/documents/${id}/chunks`)
}

// 问答接口
export function askQuestion(question: string) {
  return post<QaResultVo>('/knowledge/qa/ask', { question })
}

// 问答日志接口
export function getQaLogs(params: QaLogQuery) {
  return get<PageResult<QaLogVo>>('/knowledge/qa/logs', params)
}

export function getQaLogById(id: number) {
  return get<QaLogVo>(`/knowledge/qa/logs/${id}`)
}
