<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">采购管理</h2>
      <p class="page-desc">采购申请、审批流程、供应商管理与合同归档</p>
    </div>

    <el-card shadow="never" class="placeholder-card">
      <el-icon :size="56" class="placeholder-icon"><ShoppingCart /></el-icon>
      <h3>采购管理功能将在后续阶段实现</h3>
      <p class="placeholder-desc">
        系统模拟大型组织采购申请与审批流程，根据金额自动生成审批链路，
        支持供应商管理、合同归档和付款节点跟踪，全流程保留审批记录与操作日志。
      </p>
      <div class="placeholder-features">
        <el-tag type="info" size="small">采购申请</el-tag>
        <el-tag type="info" size="small">金额驱动审批链路</el-tag>
        <el-tag type="info" size="small">多级审批</el-tag>
        <el-tag type="info" size="small">供应商管理</el-tag>
        <el-tag type="info" size="small">合同归档</el-tag>
        <el-tag type="info" size="small">付款节点</el-tag>
        <el-tag type="info" size="small">采购看板</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ShoppingCart } from '@element-plus/icons-vue'
</script>
