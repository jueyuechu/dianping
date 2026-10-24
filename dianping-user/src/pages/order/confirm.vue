<template>
  <view class="page" v-if="data">
    <view class="card">
      <view class="row"><text class="label">门店</text><text class="value">{{ data.shopName }}</text></view>
      <view class="row"><text class="label">团购</text><text class="value">{{ data.groupbuy.name }}</text></view>
      <view class="row"><text class="label">单价</text><text class="value">¥{{ data.groupbuy.groupbuyPrice }}</text></view>
      <view class="row">
        <text class="label">数量</text>
        <view class="stepper">
          <button class="step" @click="changeQty(-1)">−</button>
          <text class="qty">{{ qty }}</text>
          <button class="step" @click="changeQty(1)">+</button>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="row"><text class="label">使用规则</text><text class="value small">{{ data.rule?.holidayUsable === 1 ? '节假日通用' : '节假日不可用' }}</text></view>
      <view class="row"><text class="label">退款规则</text><text class="value small">{{ refundText(data.rule?.refundRule) }}</text></view>
    </view>

    <view class="bottom">
      <view class="total">合计 <text class="amount">¥{{ total }}</text></view>
      <view class="pay-btn" :class="{ disabled: paying }" @click="submit">{{ paying ? '支付中…' : '微信支付' }}</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api'

const data = ref(null)
const qty = ref(1)
const paying = ref(false)

const total = computed(() => {
  if (!data.value) return '0.00'
  return (Number(data.value.groupbuy.groupbuyPrice) * qty.value).toFixed(2)
})

onLoad(async options => {
  try {
    data.value = await api.groupbuyDetail(options.groupbuyId)
  } catch (e) { /* 已统一提示 */ }
})

function changeQty(delta) {
  qty.value = Math.max(1, qty.value + delta)
}

function refundText(v) {
  return { 1: '随时退', 2: '过期退', 3: '不可退' }[v] || '—'
}

async function submit() {
  if (paying.value) return
  paying.value = true
  try {
    const order = await api.createOrder({ groupbuyId: data.value.groupbuy.id, quantity: qty.value })
    await api.pay(order.id)
    uni.showToast({ title: '支付成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/order/verify?orderId=${order.id}&shopId=${data.value.shopId}`
      })
    }, 600)
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 160rpx; }
.card { margin: 20rpx 24rpx; background: #fff; border-radius: 20rpx; padding: 8rpx 24rpx; }
.row { display: flex; align-items: center; justify-content: space-between; padding: 22rpx 0; border-bottom: 1rpx solid #f2f2f5; }
.row:last-child { border-bottom: none; }
.label { color: #999; font-size: 26rpx; }
.value { font-size: 28rpx; color: #333; }
.value.small { font-size: 24rpx; color: #777; }
.stepper { display: flex; align-items: center; }
.step { width: 52rpx; height: 52rpx; min-width: 52rpx; padding: 0; line-height: 1; border-radius: 50%; border: 1rpx solid #ddd; background: #fff; display: flex; align-items: center; justify-content: center; font-size: 32rpx; color: #333; }
.step::after { border: none; }
.qty { width: 70rpx; text-align: center; font-size: 28rpx; }
.bottom { position: fixed; left: 0; right: 0; bottom: 0; height: 120rpx; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 32rpx; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,.06); }
.total { font-size: 26rpx; color: #666; }
.amount { color: #FF4B14; font-size: 40rpx; font-weight: 700; }
.pay-btn { background: #FFD100; color: #222; font-weight: 600; font-size: 30rpx; padding: 18rpx 56rpx; border-radius: 40rpx; }
.pay-btn.disabled { opacity: .6; }
</style>