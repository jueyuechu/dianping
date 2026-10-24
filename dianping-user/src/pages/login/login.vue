<template>
  <view class="page">
    <view class="logo">点评</view>
    <text class="slogan">发现身边的好店</text>
    <view class="btn" :class="{ disabled: loading }" @click="doLogin">
      {{ loading ? '登录中…' : '微信一键登录' }}
    </view>
    <text class="note">H5 环境使用模拟 code 登录；小程序端将接入 uni.login</text>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../../api'
import { saveLogin } from '../../utils/auth'

const loading = ref(false)

async function doLogin() {
  if (loading.value) return
  loading.value = true
  try {
    // H5 无法调用微信授权，使用模拟 code（后端按 code 生成 openid）
    const data = await api.login('h5-' + Date.now())
    saveLogin(data)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      const pages = getCurrentPages()
      if (pages.length > 1) uni.navigateBack()
      else uni.switchTab({ url: '/pages/index/index' })
    }, 600)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #fff; display: flex; flex-direction: column; align-items: center; padding-top: 160rpx; }
.logo { width: 160rpx; height: 160rpx; border-radius: 40rpx; background: linear-gradient(135deg, #FFE08A, #FFC300); color: #6B4A00; font-size: 56rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; }
.slogan { margin-top: 28rpx; color: #666; font-size: 28rpx; }
.btn { margin-top: 90rpx; width: 560rpx; background: #FFD100; color: #222; font-weight: 600; text-align: center; padding: 26rpx 0; border-radius: 44rpx; font-size: 30rpx; }
.btn.disabled { opacity: .6; }
.note { margin-top: 26rpx; color: #bbb; font-size: 22rpx; }
</style>