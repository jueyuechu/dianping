<template>
  <view class="page" v-if="data">
    <view class="hero">
      <text class="gb-name">{{ data.groupbuy.name }}</text>
      <view class="price-row">
        <text class="price">¥{{ data.groupbuy.groupbuyPrice }}</text>
        <text class="orig">原价 ¥{{ data.groupbuy.originalPrice }}</text>
      </view>
      <text class="shop">{{ data.shopName }}</text>
    </view>

    <view class="card">
      <text class="card-title">套餐内容</text>
      <text class="content">{{ data.groupbuy.content || '—' }}</text>
    </view>

    <view class="card">
      <text class="card-title">使用规则</text>
      <view class="rule-row" v-if="data.rule">
        <text class="rule-label">节假日</text>
        <text class="rule-value">{{ data.rule.holidayUsable === 1 ? '可用' : '不可用' }}</text>
      </view>
      <view class="rule-row" v-if="data.rule">
        <text class="rule-label">限购</text>
        <text class="rule-value">{{ data.rule.perUserLimit > 0 ? `每人限购 ${data.rule.perUserLimit} 份` : '不限购' }}</text>
      </view>
      <view class="rule-row" v-if="data.rule">
        <text class="rule-label">每桌限用</text>
        <text class="rule-value">{{ data.rule.perTableLimit }} 份</text>
      </view>
      <view class="rule-row" v-if="data.rule">
        <text class="rule-label">退款</text>
        <text class="rule-value">{{ refundText(data.rule.refundRule) }}</text>
      </view>
      <view class="rule-row">
        <text class="rule-label">库存</text>
        <text class="rule-value">{{ data.groupbuy.stock }} 份</text>
      </view>
    </view>

    <view class="bottom">
      <view class="bottom-price">
        <text class="label">团购价</text>
        <text class="price">¥{{ data.groupbuy.groupbuyPrice }}</text>
      </view>
      <view class="buy-btn" @click="buy">立即购买</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api'
import { requireLogin } from '../../utils/auth'

const data = ref(null)

onLoad(async options => {
  try {
    data.value = await api.groupbuyDetail(options.id)
  } catch (e) { /* 已统一提示 */ }
})

function refundText(v) {
  return { 1: '随时退', 2: '过期退', 3: '不可退' }[v] || '—'
}

function buy() {
  if (!requireLogin()) return
  uni.navigateTo({
    url: `/pages/order/confirm?groupbuyId=${data.value.groupbuy.id}&shopId=${data.value.shopId}`
  })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 140rpx; }
.hero { background: linear-gradient(180deg, #ff6633, #ff8c5a); padding: 40rpx 32rpx; color: #fff; }
.gb-name { font-size: 38rpx; font-weight: 700; }
.price-row { margin-top: 20rpx; display: flex; align-items: baseline; }
.price { font-size: 48rpx; font-weight: 700; }
.orig { font-size: 24rpx; opacity: .8; margin-left: 16rpx; text-decoration: line-through; }
.shop { display: block; margin-top: 16rpx; font-size: 24rpx; opacity: .9; }
.card { margin: 20rpx 24rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.card-title { font-size: 28rpx; font-weight: 600; display: block; margin-bottom: 16rpx; }
.content { font-size: 26rpx; color: #555; line-height: 1.8; }
.rule-row { display: flex; justify-content: space-between; padding: 10rpx 0; }
.rule-label { color: #999; font-size: 24rpx; }
.rule-value { font-size: 24rpx; color: #333; }
.bottom { position: fixed; left: 0; right: 0; bottom: 0; height: 120rpx; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 32rpx; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,.06); }
.bottom-price { display: flex; align-items: baseline; }
.label { font-size: 24rpx; color: #999; margin-right: 10rpx; }
.bottom-price .price { color: #ff3b30; font-size: 40rpx; }
.buy-btn { background: #ff6633; color: #fff; font-size: 30rpx; padding: 18rpx 64rpx; border-radius: 40rpx; }
</style>