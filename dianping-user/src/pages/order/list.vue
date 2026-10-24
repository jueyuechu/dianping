<template>
  <view class="page">
    <view class="tabs">
      <view class="tab" :class="{ on: active === 0 }" @click="switchTab(0)">全部</view>
      <view class="tab" :class="{ on: active === 2 }" @click="switchTab(2)">待使用</view>
      <view class="tab" :class="{ on: active === 3 }" @click="switchTab(3)">已使用</view>
    </view>

    <view v-if="!logged" class="login-card">
      <text class="login-tip">登录后查看你的团购订单</text>
      <view class="login-btn" @click="goLogin">去登录</view>
    </view>

    <view v-else>
      <view class="order" v-for="o in orders" :key="o.id" @click="tapOrder(o)">
        <view class="order-head">
          <text class="order-shop">{{ o.shopName || ('店铺 #' + o.shopId) }}</text>
          <text class="order-status" :class="'s' + o.status">{{ statusText(o.status) }}</text>
        </view>
        <text class="order-name">{{ o.groupbuyName || ('团购 #' + o.groupbuyId) }} ×{{ o.quantity }}</text>
        <view class="order-foot">
          <text class="order-no">{{ o.orderNo }}</text>
          <text class="order-amount">¥{{ o.amount }}</text>
        </view>
        <view class="order-actions">
          <view v-if="o.status === 2" class="action-btn" @click.stop="goVerify(o)">出示核销码</view>
          <view v-if="o.status === 3" class="action-btn ghost" @click.stop="goReview(o)">去评价</view>
        </view>
      </view>
      <view v-if="!orders.length" class="empty">暂无订单</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'
import { isLogin, ORDER_STATUS } from '../../utils/auth'

const logged = ref(false)
const active = ref(0)
const orders = ref([])

function statusText(s) {
  return ORDER_STATUS[s] || '未知'
}

async function load() {
  logged.value = isLogin()
  if (!logged.value) {
    orders.value = []
    return
  }
  try {
    orders.value = await api.orders(active.value || undefined)
  } catch (e) {
    orders.value = []
  }
}

function switchTab(v) {
  active.value = v
  load()
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}

function tapOrder(o) {
  if (o.status === 2) goVerify(o)
  else if (o.status === 3) goReview(o)
  else uni.showToast({ title: statusText(o.status), icon: 'none' })
}

function goVerify(o) {
  uni.navigateTo({ url: `/pages/order/verify?orderId=${o.id}&shopId=${o.shopId}` })
}

function goReview(o) {
  uni.navigateTo({ url: `/pages/review/write?orderId=${o.id}&shopId=${o.shopId}` })
}

onShow(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 30rpx; }
.tabs { display: flex; background: #fff; }
.tab { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #666; }
.tab.on { color: #E6A800; font-weight: 600; border-bottom: 4rpx solid #FFC300; }
.login-card { margin: 80rpx 24rpx; background: #fff; border-radius: 20rpx; padding: 60rpx; text-align: center; }
.login-tip { color: #999; font-size: 26rpx; }
.login-btn { margin: 30rpx auto 0; background: #FFD100; color: #222; font-weight: 600; width: 300rpx; padding: 18rpx 0; border-radius: 40rpx; font-size: 28rpx; }
.order { margin: 20rpx 24rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.order-head { display: flex; justify-content: space-between; align-items: center; }
.order-shop { font-size: 28rpx; font-weight: 600; }
.order-status { font-size: 24rpx; color: #FF8A00; }
.order-status.s3 { color: #999; }
.order-status.s4, .order-status.s5 { color: #bbb; }
.order-name { display: block; margin-top: 14rpx; font-size: 26rpx; color: #555; }
.order-foot { display: flex; justify-content: space-between; margin-top: 16rpx; align-items: baseline; }
.order-no { font-size: 20rpx; color: #aaa; }
.order-amount { color: #ff3b30; font-size: 32rpx; font-weight: 700; }
.order-actions { display: flex; justify-content: flex-end; margin-top: 16rpx; }
.action-btn { border: 1rpx solid #FFC300; color: #B07A00; font-size: 24rpx; padding: 10rpx 26rpx; border-radius: 30rpx; }
.action-btn.ghost { border-color: #ddd; color: #666; }
.empty { text-align: center; color: #999; font-size: 26rpx; padding: 100rpx 0; }
</style>