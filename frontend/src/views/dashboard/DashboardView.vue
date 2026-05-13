<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">工作台</h2>
      <p class="page-desc">系统运行概况与关键业务动态</p>
    </div>

    <!-- 指标卡片行 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">采购申请总数</div>
          <div class="stat-value">--</div>
          <div class="stat-hint">待后续阶段接入数据</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">待审批数量</div>
          <div class="stat-value">--</div>
          <div class="stat-hint">待后续阶段接入数据</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">知识库文档</div>
          <div class="stat-value">--</div>
          <div class="stat-hint">待后续阶段接入数据</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">安全告警</div>
          <div class="stat-value">--</div>
          <div class="stat-hint">待后续阶段接入数据</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 动态 & 告警卡片行 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-title">
              <el-icon><ShoppingCart /></el-icon>
              <span>最近采购动态</span>
            </div>
          </template>
          <el-empty
            description="采购数据将在后续阶段接入"
            :image-size="80"
          />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-title">
              <el-icon><Lock /></el-icon>
              <span>最近安全告警</span>
            </div>
          </template>
          <el-empty
            description="安全告警数据将在后续阶段接入"
            :image-size="80"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ShoppingCart, Lock } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;

  :deep(.el-card__body) {
    padding: 24px 16px;
  }
}
</style>
