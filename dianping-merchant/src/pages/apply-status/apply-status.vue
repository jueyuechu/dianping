<template>
  <view class="page">
    <view class="card">
      <text class="title">{{ data.merchantName || '商户入驻' }}</text>
      <view class="row"><text class="label">商户状态</text><text class="value">{{ merchantStatusText }}</text></view>
      <view class="row"><text class="label">审核状态</text><text class="value" :class="{ danger: data.auditStatus === 4 }">{{ auditText }}</text></view>
      <view class="row" v-if="data.reason"><text class="label">驳回原因</text><text class="value danger">{{ data.reason }}</text></view>
      <view class="row" v-if="data.submitTime"><text class="label">提交时间</text><text class="value">{{ fmt(data.submitTime) }}</text></view>
    </view>

    <button class="btn" @click="load">刷新进度</button>
    <button class="btn ghost" @click="goLogin">返回登录</button>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'
import { AUDIT_STATUS } from '../../utils/auth'

const data = ref({})

const merchantStatusText = computed(() => ({ 0: '待审核', 1: '正常经营', 2: '已冻结' }[data.value.merchantStatus] || '—'))
const auditText = computed(() => {
  const s = data.value.auditStatus
  return s === null || s === undefined ? '未提交' : (AUDIT_STATUS[s] || s)
})

function fmt(t) {
  return String(t).replace('T', ' ').slice(0, 16)
}

async function load() {
  try {
    data.value = await api.applyStatus()
  } catch (e) { /* 已提示 */ }
}

function goLogin() {
  uni.redirectTo({ url: '/pages/login/login' })
}

onShow(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding: 30rpx 24rpx; }
.card { background: #fff; border-radius: 20rpx; padding: 30rpx 26rpx; }
.title { font-size: 34rpx; font-weight: 700; display: block; margin-bottom: 20rpx; }
.row { display: flex; justify-content: space-between; padding: 18rpx 0; border-bottom: 1rpx solid #f2f2f5; }
.row:last-child { border-bottom: none; }
.label { color: #999; font-size: 26rpx; }
.value { font-size: 26rpx; color: #333; max-width: 420rpx; text-align: right; }
.danger { color: #ff3b30; }
.btn { margin-top: 30rpx; background: #00b578; color: #fff; border-radius: 44rpx; font-size: 28rpx; }
.btn.ghost { background: #fff; color: #666; }
</style>