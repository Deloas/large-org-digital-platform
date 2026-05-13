<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">知识库</h2>
      <p class="page-desc">制度文件管理、文档解析、智能问答与溯源</p>
    </div>

    <el-card shadow="never" class="placeholder-card">
      <el-icon :size="56" class="placeholder-icon"><Document /></el-icon>
      <h3>知识库功能将在后续阶段实现</h3>
      <p class="placeholder-desc">
        系统支持上传公开制度文件或模拟制度文件，自动完成文档解析、文本切分、向量化检索，
        并通过 RAG 技术实现自然语言问答，回答必须提供引用来源。
      </p>
      <div class="placeholder-features">
        <el-tag type="info" size="small">文档上传</el-tag>
        <el-tag type="info" size="small">PDF/Word/TXT 解析</el-tag>
        <el-tag type="info" size="small">文本切分</el-tag>
        <el-tag type="info" size="small">向量检索</el-tag>
        <el-tag type="info" size="small">RAG 智能问答</el-tag>
        <el-tag type="info" size="small">答案溯源引用</el-tag>
        <el-tag type="info" size="small">问答日志</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'
</script>
