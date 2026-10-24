<template>
  <view class="page">
    <view class="head">
      <text class="hello">{{ info.shopName || '商户中心' }}</text>
      <text class="sub">账号 {{ info.username }} · 角色 {{ roleText }}</text>
    </view>

    <view class="stats">
      <view class="stat"><text class="num">{{ stats.shopCount }}</text><text class="label">门店</text></view>
      <view class="stat"><text class="num">{{ stats.groupbuyCount }}</text><text class="label">团购</text></view>
      <view class="stat"><text class="num">{{ stats.pendingOrders }}</text><text class="label">待核销</text></view>
      <view class="stat"><text class="num">{{ stats.verifyCount }}</text><text class="label">核销记录</text></view>
    </view>

    <view class="card">
      <text class="card-title">快捷操作</text>
      <view class="actions">
        <view class="action" @click="goVerify"><text class="act-ico">✓</text><text class="act-text">扫码核销</text></view>
        <view class="action" @click="goGroupbuy"><text class="act-ico">券</text><text class="act-text">团购管理</text></view>
        <view class="action" @click="goShop"><text class="act-ico">店</text><text class="act-text">店铺信息</text></view>
      </view>
    </view>

    <view class="card">
      <view class="card-head">
        <text class="card-title">最近订单</text>
        <text class="more" @click="goVerify">全部 ›</text>
      </view>
      <view class="order" v-for="o in recentOrders" :key="o.id">
        <view class="order-left">
          <text class="order-name">{{ o.groupbuyName || ('团购 #' + o.groupbuyId) }} ×{{ o.quantity }}</text>
          <text class="order-no">{{ o.orderNo }}</text>
        </view>
        <text class="order-status">{{ statusText(o.status) }}</text>
      </view>
      <view v-if="!recentOrders.length" class="empty">暂无订单</view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'
import { getMerchant, requireLogin } from '../../utils/auth'

const info = ref(getMerchant() || {})
const roleText = { 1: '老板', 2: '店长', 3: '员工' }[info.value.role] || '员工'
const stats = reactive({ shopCount: 0, groupbuyCount: 0, pendingOrders: 0, verifyCount: 0 })
const recentOrders = ref([])

function statusText(s) {
  return { 1: '待支付', 2: '待使用', 3: '已使用', 4: '已退款', 5: '已过期' }[s] || '—'
}

async function load() {
  if (!requireLogin()) return
  info.value = getMerchant() || {}
  if (info.value.merchantStatus === 0) {
    uni.redirectTo({ url: '/pages/apply-status/apply-status' })
    return
  }
  try {
    const shops = await api.shops()
    stats.shopCount = shops.length
    if (shops.length) {
      const groups = await api.groupbuys(shops[0].id)
      stats.groupbuyCount = groups.length
    }
    const orders = await api.orders('current=1&size=100')
    stats.pendingOrders = orders.records.filter(o => o.status === 2).length
    recentOrders.value = orders.records.slice(0, 3)
    const records = await api.verifyRecords('current=1&size=100')
    stats.verifyCount = Number(records.total)
  } catch (e) { /* 已提示 */ }
}

function goVerify() {
  uni.switchTab({ url: '/pages/verify/verify' })
}
function goGroupbuy() {
  uni.switchTab({ url: '/pages/groupbuy/list' })
}
function goShop() {
  uni.navigateTo({ url: '/pages/shop/edit' })
}

onShow(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 30rpx; }
.head { background: linear-gradient(135deg, #00b578, #37d99b); padding: 40rpx 32rpx; color: #fff; }
.hello { font-size: 36rpx; font-weight: 700; display: block; }
.sub { font-size: 22rpx; opacity: .9; margin-top: 8rpx; display: block; }
.stats { display: flex; background: #fff; padding: 28rpx 0; }
.stat { flex: 1; display: flex; flex-direction: column; align-items: center; }
.num { font-size: 36rpx; font-weight: 700; color: #00b578; }
.label { font-size: 22rpx; color: #999; margin-top: 6rpx; }
.card { margin: 20rpx 24rpx 0; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.card-title { font-size: 28rpx; font-weight: 600; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.more { font-size: 22rpx; color: #00b578; }
.actions { display: flex; margin-top: 20rpx; }
.action { flex: 1; display: flex; flex-direction: column; align-items: center; }
.act-ico { width: 80rpx; height: 80rpx; border-radius: 24rpx; background: #e8f8f1; color: #00b578; font-size: 32rpx; display: flex; align-items: center; justify-content: center; }
.act-text { font-size: 22rpx; color: #555; margin-top: 10rpx; }
.order { display: flex; justify-content: space-between; align-items: center; padding: 18rpx 0; border-bottom: 1rpx solid #f2f2f5; }
.order:last-child { border-bottom: none; }
.order-left { display: flex; flex-direction: column; }
.order-name { font-size: 26rpx; color: #333; }
.order-no { font-size: 20rpx; color: #bbb; margin-top: 6rpx; }
.order-status { font-size: 22rpx; color: #00b578; }
.empty { text-align: center; color: #999; font-size: 24rpx; padding: 30rpx 0; }
</style>