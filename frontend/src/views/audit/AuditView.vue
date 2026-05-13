<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">安全审计</h2>
      <p class="page-desc">登录日志、操作日志、异常检测与安全告警</p>
    </div>

    <el-card shadow="never" class="placeholder-card">
      <el-icon :size="56" class="placeholder-icon"><Lock /></el-icon>
      <h3>安全审计功能将在后续阶段实现</h3>
      <p class="placeholder-desc">
        系统记录 Web 应用的登录日志和业务操作日志，通过规则引擎检测暴力破解、
        撞库风险、异常登录等安全事件，自动生成告警并支持处理和误报标记。
      </p>
      <div class="placeholder-features">
        <el-tag type="info" size="small">登录日志</el-tag>
        <el-tag type="info" size="small">操作日志</el-tag>
        <el-tag type="info" size="small">暴力破解检测</el-tag>
        <el-tag type="info" size="small">撞库风险检测</el-tag>
        <el-tag type="info" size="small">异常登录检测</el-tag>
        <el-tag type="info" size="small">IP 黑名单</el-tag>
        <el-tag type="info" size="small">安全看板</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue'
</script>
