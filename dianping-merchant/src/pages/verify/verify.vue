<template>
  <view class="page">
    <view class="scan-card">
      <view class="scan-box">扫</view>
      <text class="scan-tip">扫描用户核销码，或手动输入 6 位核销码</text>
      <view class="code-row">
        <input class="code-input" v-model="code" type="number" maxlength="6" placeholder="输入 6 位核销码" />
        <button class="verify-btn" :class="{ disabled: verifying }" @click="doVerify">核销</button>
      </view>
    </view>

    <view class="card" v-if="lastResult">
      <text class="card-title">核销成功</text>
      <view class="row"><text class="label">订单号</text><text class="value">{{ lastResult.orderNo }}</text></view>
      <view class="row"><text class="label">团购 ID</text><text class="value">{{ lastResult.groupbuyId }}</text></view>
      <view class="row"><text class="label">数量</text><text class="value">{{ lastResult.quantity }}</text></view>
      <view class="row"><text class="label">核销时间</text><text class="value">{{ now }}</text></view>
    </view>

    <view class="card">
      <text class="card-title">核销记录</text>
      <view class="record" v-for="r in records" :key="r.id">
        <text class="rec-order">订单 #{{ r.orderId }}</text>
        <text class="rec-time">{{ fmt(r.verifyTime) }}</text>
      </view>
      <view v-if="!records.length" class="empty">暂无核销记录</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'
import { requireLogin } from '../../utils/auth'

const code = ref('')
const verifying = ref(false)
const lastResult = ref(null)
const records = ref([])
const now = ref('')

function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : '—'
}

async function loadRecords() {
  try {
    const page = await api.verifyRecords('current=1&size=20')
    records.value = page.records
  } catch (e) { /* 已提示 */ }
}

async function doVerify() {
  if (verifying.value) return
  if (code.value.length !== 6) {
    uni.showToast({ title: '请输入 6 位核销码', icon: 'none' })
    return
  }
  verifying.value = true
  try {
    const order = await api.verify({ verifyCode: code.value, method: 2 })
    lastResult.value = order
    now.value = fmt(new Date().toISOString())
    code.value = ''
    uni.showToast({ title: '核销成功', icon: 'success' })
    loadRecords()
  } finally {
    verifying.value = false
  }
}

onShow(() => {
  if (!requireLogin()) return
  loadRecords()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding: 20rpx 0 40rpx; }
.scan-card { margin: 0 24rpx 20rpx; background: #fff; border-radius: 20rpx; padding: 40rpx 24rpx; display: flex; flex-direction: column; align-items: center; }
.scan-box { width: 180rpx; height: 180rpx; border: 2rpx dashed #00b578; border-radius: 20rpx; color: #00b578; font-size: 48rpx; display: flex; align-items: center; justify-content: center; }
.scan-tip { margin-top: 20rpx; color: #999; font-size: 22rpx; }
.code-row { margin-top: 30rpx; display: flex; align-items: center; width: 100%; }
.code-input { flex: 1; height: 84rpx; background: #f5f6f8; border-radius: 14rpx; padding: 0 24rpx; font-size: 30rpx; letter-spacing: 6rpx; }
.verify-btn { margin-left: 16rpx; background: #00b578; color: #fff; font-size: 26rpx; border-radius: 40rpx; padding: 0 36rpx; height: 84rpx; line-height: 84rpx; }
.verify-btn.disabled { opacity: .6; }
.card { margin: 0 24rpx 20rpx; background: #fff; border-radius: 20rpx; padding: 20rpx 24rpx; }
.card-title { font-size: 28rpx; font-weight: 600; display: block; padding: 8rpx 0 12rpx; }
.row { display: flex; justify-content: space-between; padding: 14rpx 0; border-bottom: 1rpx solid #f2f2f5; }
.row:last-child { border-bottom: none; }
.label { color: #999; font-size: 24rpx; }
.value { font-size: 24rpx; color: #333; }
.record { display: flex; justify-content: space-between; padding: 18rpx 0; border-bottom: 1rpx solid #f2f2f5; }
.record:last-child { border-bottom: none; }
.rec-order { font-size: 26rpx; color: #333; }
.rec-time { font-size: 22rpx; color: #bbb; }
.empty { text-align: center; color: #999; font-size: 24rpx; padding: 30rpx 0; }
</style>