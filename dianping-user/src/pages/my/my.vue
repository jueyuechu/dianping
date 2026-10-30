<template>
  <view class="page">
    <view class="head">
      <view class="avatar">{{ user ? (user.nickName || '用').slice(0, 1) : '未' }}</view>
      <view class="head-info">
        <text class="name">{{ user ? user.nickName : '未登录' }}</text>
        <text class="sub">{{ user ? ('用户 ID：' + user.id) : '登录后可下单、评价' }}</text>
      </view>
      <view v-if="!user" class="login-btn" @click="goLogin">登录</view>
    </view>

    <view class="grid">
      <view class="grid-item" @click="goOrders"><text class="grid-num">订单</text><text class="grid-label">我的订单</text></view>
      <view class="grid-item" @click="goReviews"><text class="grid-num">评价</text><text class="grid-label">我的评价</text></view>
      <view class="grid-item" @click="toast('收藏功能 P1 上线')"><text class="grid-num">收藏</text><text class="grid-label">我的收藏</text></view>
      <view class="grid-item" @click="toast('优惠券功能 P1 上线')"><text class="grid-num">券</text><text class="grid-label">优惠券</text></view>
    </view>

    <view class="list">
      <view class="li" @click="goReviews"><text>我的评价</text><text class="arrow">›</text></view>
      <view class="li" @click="goMessages"><text>消息通知{{ unread ? '（' + unread + '）' : '' }}</text><text class="arrow">›</text></view>
      <view class="li" @click="toast('客服与反馈')"><text>客服与反馈</text><text class="arrow">›</text></view>
    </view>

    <view v-if="user" class="logout" @click="doLogout">退出登录</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'
import { getUser, logout } from '../../utils/auth'

const user = ref(null)
const unread = ref(0)

onShow(() => {
  user.value = getUser()
  loadUnread()
})

function loadUnread() {
  if (!user.value) {
    unread.value = 0
    return
  }
  api.unreadMessages()
    .then(n => { unread.value = n || 0 })
    .catch(() => {})
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}

function goOrders() {
  uni.switchTab({ url: '/pages/order/list' })
}

function goReviews() {
  if (!user.value) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/my/reviews' })
}

function goMessages() {
  if (!user.value) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/my/messages' })
}

function toast(title) {
  uni.showToast({ title, icon: 'none' })
}

function doLogout() {
  logout()
  user.value = null
  uni.showToast({ title: '已退出登录', icon: 'none' })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 40rpx; }
.head { background: #FFD100; padding: 50rpx 32rpx; display: flex; align-items: center; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 50%; background: #fff; color: #B07A00; font-size: 48rpx; font-weight: 600; display: flex; align-items: center; justify-content: center; }
.head-info { flex: 1; margin-left: 24rpx; color: #222; }
.name { font-size: 34rpx; font-weight: 700; display: block; }
.sub { font-size: 22rpx; color: #6B4A00; margin-top: 8rpx; display: block; }
.login-btn { background: #fff; color: #222; font-size: 26rpx; font-weight: 600; padding: 12rpx 36rpx; border-radius: 30rpx; }
.grid { display: flex; background: #fff; padding: 30rpx 0; }
.grid-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.grid-num { font-size: 30rpx; font-weight: 600; color: #333; }
.grid-label { font-size: 22rpx; color: #999; margin-top: 8rpx; }
.list { margin-top: 20rpx; background: #fff; }
.li { display: flex; justify-content: space-between; padding: 30rpx 32rpx; font-size: 28rpx; color: #333; border-bottom: 1rpx solid #f2f2f5; }
.li:last-child { border-bottom: none; }
.arrow { color: #ccc; }
.logout { margin: 40rpx 32rpx 0; background: #fff; color: #ff3b30; text-align: center; padding: 26rpx 0; border-radius: 16rpx; font-size: 28rpx; }
</style>