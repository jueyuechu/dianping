<template>
  <view class="page">
    <view class="logo">商户中心</view>
    <text class="slogan">点评平台 · 商家经营工作台</text>

    <view class="form">
      <input class="input" v-model="username" placeholder="商户账号" />
      <input class="input" v-model="password" password placeholder="登录密码" />
      <button class="btn" :class="{ disabled: loading }" @click="doLogin">{{ loading ? '登录中…' : '登 录' }}</button>
    </view>

    <view class="link" @click="goRegister">还没有账号？申请入驻 ›</view>
    <text class="demo">演示账号：mer001 / 123456</text>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../../api'
import { saveLogin } from '../../utils/auth'

const username = ref('')
const password = ref('')
const loading = ref(false)

async function doLogin() {
  if (loading.value) return
  if (!username.value || !password.value) {
    uni.showToast({ title: '请输入账号和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const data = await api.login({ username: username.value, password: password.value })
    saveLogin(data)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      if (data.merchantStatus === 0) {
        uni.redirectTo({ url: '/pages/apply-status/apply-status' })
      } else {
        uni.switchTab({ url: '/pages/workbench/workbench' })
      }
    }, 500)
  } finally {
    loading.value = false
  }
}

function goRegister() {
  uni.navigateTo({ url: '/pages/register/register' })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #fff; display: flex; flex-direction: column; align-items: center; padding-top: 120rpx; }
.logo { width: 150rpx; height: 150rpx; border-radius: 36rpx; background: linear-gradient(135deg, #00b578, #37d99b); color: #fff; font-size: 40rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; }
.slogan { margin-top: 24rpx; color: #888; font-size: 24rpx; }
.form { width: 620rpx; margin-top: 80rpx; }
.input { height: 92rpx; background: #f5f6f8; border-radius: 16rpx; padding: 0 28rpx; font-size: 28rpx; margin-bottom: 24rpx; }
.btn { margin-top: 16rpx; background: #00b578; color: #fff; border-radius: 44rpx; font-size: 30rpx; }
.btn.disabled { opacity: .6; }
.link { margin-top: 40rpx; color: #00b578; font-size: 26rpx; }
.demo { margin-top: 20rpx; color: #bbb; font-size: 22rpx; }
</style>