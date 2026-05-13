<template>
  <div class="forbidden-page">
    <el-result icon="warning" title="403" sub-title="无权限访问此页面">
      <template #extra>
        <el-button type="primary" @click="goBack">返回工作台</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goBack() {
  router.push('/dashboard')
}
</script>

<style scoped lang="scss">
.forbidden-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>
