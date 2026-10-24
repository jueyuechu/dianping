<template>
  <view class="page">
    <view class="head">
      <view class="avatar">{{ (info.shopName || '商').slice(0, 1) }}</view>
      <view class="info">
        <text class="name">{{ info.shopName || '商户' }}</text>
        <text class="sub">账号 {{ info.username }} · {{ roleText }}</text>
      </view>
    </view>

    <view class="list">
      <view class="li" @click="goApply"><text>入驻进度</text><text class="arrow">›</text></view>
      <view class="li" @click="goShop"><text>店铺信息</text><text class="arrow">›</text></view>
      <view class="li" @click="goGroupbuy"><text>团购管理</text><text class="arrow">›</text></view>
      <view class="li" @click="goReview"><text>评价管理</text><text class="arrow">›</text></view>
      <view class="li" @click="toast('对账结算 P1 上线')"><text>对账与结算</text><text class="arrow">›</text></view>
      <view class="li" @click="toast('客服与申诉 P1 上线')"><text>客服与申诉</text><text class="arrow">›</text></view>
    </view>

    <view class="logout" @click="doLogout">退出登录</view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMerchant, logout, requireLogin } from '../../utils/auth'

const info = ref({})
const roleText = computed(() => ({ 1: '老板', 2: '店长', 3: '员工' }[info.value.role] || '员工'))

onShow(() => {
  if (!requireLogin()) return
  info.value = getMerchant() || {}
})

function goApply() {
  uni.navigateTo({ url: '/pages/apply-status/apply-status' })
}
function goShop() {
  uni.navigateTo({ url: '/pages/shop/edit' })
}
function goGroupbuy() {
  uni.switchTab({ url: '/pages/groupbuy/list' })
}
function goReview() {
  uni.navigateTo({ url: '/pages/review/list' })
}
function toast(title) {
  uni.showToast({ title, icon: 'none' })
}
function doLogout() {
  logout()
  uni.reLaunch({ url: '/pages/login/login' })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 40rpx; }
.head { background: linear-gradient(135deg, #00b578, #37d99b); padding: 50rpx 32rpx; display: flex; align-items: center; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 28rpx; background: #fff; color: #00b578; font-size: 46rpx; display: flex; align-items: center; justify-content: center; }
.info { margin-left: 24rpx; color: #fff; }
.name { font-size: 34rpx; font-weight: 600; display: block; }
.sub { font-size: 22rpx; opacity: .9; margin-top: 8rpx; display: block; }
.list { margin-top: 20rpx; background: #fff; }
.li { display: flex; justify-content: space-between; padding: 30rpx 32rpx; font-size: 28rpx; color: #333; border-bottom: 1rpx solid #f2f2f5; }
.li:last-child { border-bottom: none; }
.arrow { color: #ccc; }
.logout { margin: 40rpx 32rpx 0; background: #fff; color: #ff3b30; text-align: center; padding: 26rpx 0; border-radius: 16rpx; font-size: 28rpx; }
</style>