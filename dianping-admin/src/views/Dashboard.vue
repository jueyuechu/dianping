<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="6" v-for="card in cards2" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:16px">
      <template #header><b>待办提醒</b></template>
      <el-space direction="vertical" :size="8">
        <div class="todo-item">
          <el-tag type="warning">待审核</el-tag>
          <span>{{ overview.pendingAuditCount ?? '-' }} 个任务等待处理</span>
          <el-button link type="primary" @click="$router.push('/audit')">去处理</el-button>
        </div>
        <div class="todo-item">
          <el-tag type="danger">待处理举报</el-tag>
          <span>{{ overview.pendingReportCount ?? '-' }} 条举报等待处理</span>
          <el-button link type="primary" @click="$router.push('/report')">去处理</el-button>
        </div>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { dashboardOverview } from '../api'

const overview = ref({})

onMounted(async () => {
  try {
    overview.value = await dashboardOverview()
  } catch (e) { /* 后端未启动时保持占位 */ }
})

const cards = computed(() => [
  { label: '用户总数', value: overview.value.userCount ?? '-', color: '#4f6ef7' },
  { label: '商户总数', value: overview.value.merchantCount ?? '-', color: '#00b578' },
  { label: '店铺总数', value: overview.value.shopCount ?? '-', color: '#e88a1a' },
  { label: '评价总数', value: overview.value.reviewCount ?? '-', color: '#ff6633' }
])
const cards2 = computed(() => [
  { label: '订单总数', value: overview.value.orderCount ?? '-', color: '#7b5cf0' },
  { label: '待审核任务', value: overview.value.pendingAuditCount ?? '-', color: '#e6a23c' },
  { label: '待处理举报', value: overview.value.pendingReportCount ?? '-', color: '#f56c6c' },
  { label: '今日新增用户', value: '-', color: '#909399' }
])
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: #999; }
.stat-value { font-size: 30px; font-weight: 700; margin-top: 8px; }
.todo-item { display: flex; align-items: center; gap: 12px; }
</style>
