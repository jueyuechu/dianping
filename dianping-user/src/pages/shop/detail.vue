<template>
  <view class="page" v-if="detail">
    <view class="cover">{{ detail.shop.name.slice(0, 1) }}</view>

    <view class="card">
      <text class="name">{{ detail.shop.name }}</text>
      <view class="meta-row">
        <text class="meta">{{ detail.shop.area || '' }} · {{ detail.shop.address || '' }}</text>
      </view>
      <view class="meta-row">
        <text class="meta">营业 {{ detail.shop.openHours || '—' }}</text>
      </view>
      <view class="meta-row">
        <text class="meta">电话 {{ detail.shop.phone || '—' }}</text>
      </view>
    </view>

    <view class="card score-box">
      <view class="score-big">{{ (detail.shop.score / 10).toFixed(1) }}</view>
      <view class="score-dims">
        <text class="dim">口味 {{ (detail.shop.tasteScore / 10).toFixed(1) }}　环境 {{ (detail.shop.envScore / 10).toFixed(1) }}</text>
        <text class="dim">服务 {{ (detail.shop.serviceScore / 10).toFixed(1) }}　性价比 {{ (detail.shop.valueScore / 10).toFixed(1) }}</text>
        <text class="dim">{{ detail.shop.comments }} 条评价</text>
      </view>
    </view>

    <view class="section-title">团购套餐</view>
    <view class="gb" v-for="g in detail.groupbuys" :key="g.id" @click="goGroupbuy(g.id)">
      <view class="gb-ico">券</view>
      <view class="gb-info">
        <text class="gb-name">{{ g.name }}</text>
        <view>
          <text class="price">¥{{ g.groupbuyPrice }}</text>
          <text class="orig">¥{{ g.originalPrice }}</text>
        </view>
      </view>
      <view class="buy">抢购</view>
    </view>
    <view v-if="!detail.groupbuys.length" class="empty">暂无团购</view>

    <view class="section-title">菜品</view>
    <view class="dish" v-for="d in detail.dishes" :key="d.id">
      <view class="dish-ico">{{ d.name.slice(0, 1) }}</view>
      <view class="dish-info">
        <text class="dish-name">{{ d.name }}</text>
        <text class="dish-desc">{{ d.description || '' }}</text>
      </view>
      <text class="dish-price">¥{{ d.price }}</text>
    </view>
    <view v-if="!detail.dishes.length" class="empty">暂无菜品</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api'

const detail = ref(null)

onLoad(async options => {
  try {
    detail.value = await api.shopDetail(options.id)
    uni.setNavigationBarTitle({ title: detail.value.shop.name })
  } catch (e) { /* 已统一提示 */ }
})

function goGroupbuy(id) {
  uni.navigateTo({ url: `/pages/groupbuy/detail?id=${id}` })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 30rpx; }
.cover { height: 280rpx; background: linear-gradient(135deg, #ffb199, #ff6633); color: #fff; font-size: 96rpx; display: flex; align-items: center; justify-content: center; }
.card { margin: 20rpx 24rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.name { font-size: 36rpx; font-weight: 700; }
.meta-row { margin-top: 12rpx; }
.meta { color: #777; font-size: 24rpx; }
.score-box { display: flex; align-items: center; }
.score-big { font-size: 64rpx; color: #ffb400; font-weight: 700; margin-right: 28rpx; }
.score-dims { display: flex; flex-direction: column; }
.dim { font-size: 22rpx; color: #777; line-height: 1.8; }
.section-title { padding: 24rpx 32rpx 12rpx; font-size: 30rpx; font-weight: 600; }
.gb { margin: 0 24rpx 16rpx; background: #fff; border-radius: 16rpx; padding: 20rpx; display: flex; align-items: center; }
.gb-ico { width: 88rpx; height: 88rpx; border-radius: 16rpx; background: #ff6633; color: #fff; font-size: 32rpx; display: flex; align-items: center; justify-content: center; }
.gb-info { flex: 1; margin-left: 20rpx; }
.gb-name { font-size: 28rpx; font-weight: 600; }
.price { color: #ff3b30; font-size: 32rpx; font-weight: 700; }
.orig { color: #999; font-size: 22rpx; text-decoration: line-through; margin-left: 10rpx; }
.buy { background: #ff6633; color: #fff; font-size: 24rpx; padding: 12rpx 28rpx; border-radius: 30rpx; }
.dish { margin: 0 24rpx 14rpx; background: #fff; border-radius: 16rpx; padding: 20rpx; display: flex; align-items: center; }
.dish-ico { width: 80rpx; height: 80rpx; border-radius: 12rpx; background: #ffd9c7; color: #ff6633; font-size: 30rpx; display: flex; align-items: center; justify-content: center; }
.dish-info { flex: 1; margin-left: 18rpx; display: flex; flex-direction: column; }
.dish-name { font-size: 28rpx; }
.dish-desc { color: #999; font-size: 22rpx; margin-top: 6rpx; }
.dish-price { color: #ff3b30; font-size: 28rpx; font-weight: 600; }
.empty { text-align: center; color: #999; font-size: 24rpx; padding: 30rpx 0; }
</style>