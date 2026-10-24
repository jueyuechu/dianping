<template>
  <view class="page">
    <view class="tip">到店出示给商家核销</view>
    <view class="code-box">
      <text class="code">{{ code || '------' }}</text>
    </view>
    <view class="qr">二维码</view>
    <text class="sub">订单 #{{ orderId }}</text>
    <view class="btn" @click="goReview">已核销 · 去写评价</view>
    <text class="note">评价需在商家核销后才能发布</text>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api'

const code = ref('')
const orderId = ref('')
const shopId = ref('')

onLoad(async options => {
  orderId.value = options.orderId
  shopId.value = options.shopId || ''
  try {
    const data = await api.verifyCode(options.orderId)
    code.value = data.verifyCode
  } catch (e) { /* 已统一提示 */ }
})

function goReview() {
  uni.navigateTo({ url: `/pages/review/write?orderId=${orderId.value}&shopId=${shopId.value}` })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #fff; display: flex; flex-direction: column; align-items: center; padding-top: 70rpx; }
.tip { font-size: 28rpx; color: #333; }
.code-box { margin-top: 40rpx; width: 560rpx; height: 200rpx; border: 2rpx dashed #ff6633; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; }
.code { font-size: 68rpx; font-weight: 700; letter-spacing: 16rpx; color: #ff6633; }
.qr { margin-top: 40rpx; width: 300rpx; height: 300rpx; border: 1rpx solid #eee; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; color: #bbb; font-size: 26rpx; }
.sub { margin-top: 24rpx; color: #999; font-size: 24rpx; }
.btn { margin-top: 70rpx; background: #ff6633; color: #fff; font-size: 30rpx; padding: 20rpx 80rpx; border-radius: 44rpx; }
.note { margin-top: 24rpx; color: #bbb; font-size: 22rpx; }
</style>