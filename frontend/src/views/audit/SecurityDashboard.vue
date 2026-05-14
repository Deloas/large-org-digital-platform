<template>
  <div class="page-container dashboard">
    <div class="page-header">
      <h2 class="page-title">安全看板</h2>
      <p class="page-desc">安全告警概览与趋势监控</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">未读告警</div>
          <div class="stat-value" :style="{ color: totalAlertsUnread > 0 ? 'var(--color-danger)' : 'var(--text-primary)' }">
            {{ totalAlertsUnread }}
          </div>
          <div class="stat-hint">待处理</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">高危告警（今日）</div>
          <div class="stat-value danger">{{ highAlertsToday }}</div>
          <div class="stat-hint">需立即关注</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">今日告警</div>
          <div class="stat-value">{{ totalAlertsToday }}</div>
          <div class="stat-hint">全部告警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">黑名单 IP</div>
          <div class="stat-value">{{ blacklistCount }}</div>
          <div class="stat-hint">已封禁</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never" class="content-card">
          <template #header>
            <span class="card-title">近 7 天告警趋势</span>
          </template>
          <div class="bar-chart">
            <div
              v-for="item in alertTrend"
              :key="item.date"
              class="bar-col"
            >
              <div class="bar-value">{{ item.count }}</div>
              <div
                class="bar"
                :style="{ height: barHeight(item.count) + 'px' }"
                :title="`${item.date}: ${item.count} 条告警`"
              ></div>
              <div class="bar-label">{{ item.date }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="content-card">
          <template #header>
            <span class="card-title">告警类型分布</span>
          </template>
          <div class="type-dist" v-if="alertByType.length > 0">
            <div v-for="item in alertByType" :key="item.alertType" class="type-row">
              <span class="type-label">{{ typeLabel(item.alertType) }}</span>
              <el-progress
                :percentage="typePercent(item.count)"
                :color="typeColor(item.alertType)"
                :stroke-width="14"
                :show-text="false"
                style="flex:1;margin:0 12px"
              />
              <span class="type-count">{{ item.count }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无活跃告警" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 明细区 -->
    <el-row :gutter="16" class="detail-row">
      <el-col :span="12">
        <el-card shadow="never" class="content-card">
          <template #header>
            <span class="card-title">近 24h 登录失败 Top 5</span>
          </template>
          <el-table :data="topFailedAccounts" size="small" stripe v-if="topFailedAccounts.length > 0">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="username" label="账号" />
            <el-table-column prop="count" label="失败次数" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="danger" size="small">{{ row.count }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="content-card">
          <template #header>
            <span class="card-title">最新高危告警</span>
          </template>
          <div class="high-alert-list" v-if="recentHighAlerts.length > 0">
            <div
              v-for="item in recentHighAlerts"
              :key="item.id"
              class="high-alert-item"
              @click="$router.push('/audit/alerts')"
            >
              <div class="alert-item-header">
                <el-tag :type="sevTag(item.severity)" size="small" effect="dark">
                  {{ sevLabel(item.severity) }}
                </el-tag>
                <span class="alert-item-type">{{ typeLabel(item.alertType) }}</span>
                <span class="alert-item-time">{{ formatTime(item.lastTime) }}</span>
              </div>
              <div class="alert-item-title">{{ item.title }}</div>
              <div class="alert-item-meta" v-if="item.relatedUser || item.relatedIp">
                <span v-if="item.relatedUser">用户：{{ item.relatedUser }}</span>
                <span v-if="item.relatedIp" style="margin-left:12px">IP：{{ item.relatedIp }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无高危告警" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboard, type DashboardData } from '@/api/audit'

const totalAlertsUnread = ref(0)
const highAlertsToday = ref(0)
const totalAlertsToday = ref(0)
const blacklistCount = ref(0)
const alertTrend = ref<DashboardData['alertTrend']>([])
const alertByType = ref<DashboardData['alertByType']>([])
const recentHighAlerts = ref<DashboardData['recentHighAlerts']>([])
const topFailedAccounts = ref<DashboardData['topFailedAccounts']>([])

const typeMap: Record<string, string> = {
  brute_force: '暴力破解',
  credential_stuffing: '撞库风险',
  off_hours_admin: '非工作时间管理员登录',
  multi_ip: '多 IP 登录',
  blacklisted_ip: '黑名单 IP 登录'
}

const typeColorMap: Record<string, string> = {
  brute_force: '#dc2626',
  credential_stuffing: '#d97706',
  off_hours_admin: '#64748b',
  multi_ip: '#2563eb',
  blacklisted_ip: '#7c3aed'
}

function typeLabel(t: string): string {
  return typeMap[t] || t
}

function typeColor(t: string): string {
  return typeColorMap[t] || '#64748b'
}

function sevLabel(s: string): string {
  if (s === 'high') return '高危'
  if (s === 'medium') return '中危'
  return '低危'
}

function sevTag(s: string): string {
  if (s === 'high') return 'danger'
  if (s === 'medium') return 'warning'
  return 'info'
}

function barHeight(count: number): number {
  const max = Math.max(...alertTrend.value.map(t => t.count), 1)
  return Math.max((count / max) * 140, 4)
}

function typePercent(count: number): number {
  const total = alertByType.value.reduce((s, t) => s + t.count, 0)
  return total > 0 ? Math.round((count / total) * 100) : 0
}

function formatTime(str: string): string {
  if (!str) return '-'
  const d = new Date(str)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getMonth() + 1}-${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(async () => {
  try {
    const res = await getDashboard()
    const d = res.data.data
    totalAlertsUnread.value = d.totalAlertsUnread
    highAlertsToday.value = d.highAlertsToday
    totalAlertsToday.value = d.totalAlertsToday
    blacklistCount.value = d.blacklistCount
    alertTrend.value = d.alertTrend
    alertByType.value = d.alertByType
    recentHighAlerts.value = d.recentHighAlerts
    topFailedAccounts.value = d.topFailedAccounts
  } catch {
    // 加载失败显示空状态
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-row {
    margin-bottom: 16px;

    .stat-card {
      :deep(.el-card__body) {
        padding: 20px 16px;
      }

      .stat-label {
        font-size: 13px;
        color: var(--text-secondary);
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 32px;
        font-weight: 700;
        font-variant-numeric: tabular-nums;
        line-height: 1.2;
        color: var(--text-primary);

        &.danger {
          color: var(--color-danger);
        }
      }

      .stat-hint {
        font-size: 12px;
        color: var(--text-placeholder);
        margin-top: 4px;
      }
    }
  }

  .chart-row {
    margin-bottom: 16px;
  }

  .content-card {
    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }

  .bar-chart {
    display: flex;
    align-items: flex-end;
    justify-content: space-around;
    height: 200px;
    padding: 0 8px;

    .bar-col {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;
      max-width: 60px;

      .bar-value {
        font-size: 12px;
        color: var(--text-secondary);
        margin-bottom: 4px;
        min-height: 18px;
      }

      .bar {
        width: 32px;
        min-height: 4px;
        background: linear-gradient(180deg, var(--color-primary-hover), var(--color-primary));
        border-radius: 4px 4px 0 0;
        transition: height 0.3s ease;
        cursor: pointer;

        &:hover {
          background: linear-gradient(180deg, #60a5fa, var(--color-primary-hover));
        }
      }

      .bar-label {
        font-size: 11px;
        color: var(--text-placeholder);
        margin-top: 8px;
        white-space: nowrap;
      }
    }
  }

  .type-dist {
    .type-row {
      display: flex;
      align-items: center;
      margin-bottom: 14px;

      .type-label {
        width: 130px;
        font-size: 13px;
        color: var(--text-secondary);
        text-align: right;
        flex-shrink: 0;
      }

      .type-count {
        width: 36px;
        font-size: 14px;
        font-weight: 600;
        color: var(--text-primary);
        text-align: right;
      }
    }
  }

  .detail-row {
    .high-alert-list {
      .high-alert-item {
        padding: 10px 0;
        border-bottom: 1px solid var(--border-color);
        cursor: pointer;
        transition: background 0.15s;

        &:hover {
          background: var(--color-primary-light);
          margin: 0 -12px;
          padding-left: 12px;
          padding-right: 12px;
          border-radius: 4px;
        }

        &:last-child {
          border-bottom: none;
        }

        .alert-item-header {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .alert-item-type {
          font-size: 12px;
          color: var(--text-secondary);
        }

        .alert-item-time {
          margin-left: auto;
          font-size: 12px;
          color: var(--text-placeholder);
        }

        .alert-item-title {
          font-size: 13px;
          color: var(--text-primary);
          margin-top: 4px;
        }

        .alert-item-meta {
          font-size: 12px;
          color: var(--text-placeholder);
          margin-top: 2px;
        }
      }
    }
  }
}
</style>
