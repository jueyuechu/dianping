<template>
  <view class="page" v-if="detail">
    <view class="hero">
      <text class="hero-text">{{ detail.shop.name.slice(0, 1) }}</text>
      <view class="hero-mask"></view>
    </view>

    <view class="main-card">
      <text class="name">{{ detail.shop.name }}</text>
      <view class="rating-row">
        <text class="stars">★★★★★</text>
        <text class="score">{{ (detail.shop.score / 10).toFixed(1) }}</text>
        <text class="sold">月售 {{ detail.shop.sold || 0 }}</text>
        <text class="comments">{{ detail.shop.comments }} 条评价</text>
      </view>
      <view class="meta-row">
        <text class="meta-label">地址</text>
        <text class="meta-value">{{ (detail.shop.area || '') }} {{ detail.shop.address || '' }}</text>
      </view>
      <view class="meta-row">
        <text class="meta-label">营业</text>
        <text class="meta-value">{{ detail.shop.openHours || '—' }}</text>
      </view>
      <view class="meta-row">
        <text class="meta-label">电话</text>
        <text class="meta-value">{{ detail.shop.phone || '—' }}</text>
      </view>
    </view>

    <view class="score-card">
      <view class="score-big">
        <text class="score-num">{{ (detail.shop.score / 10).toFixed(1) }}</text>
        <text class="score-label">综合评分</text>
      </view>
      <view class="dims">
        <view class="dim"><text class="dim-label">口味</text><text class="dim-val">{{ (detail.shop.tasteScore / 10).toFixed(1) }}</text></view>
        <view class="dim"><text class="dim-label">环境</text><text class="dim-val">{{ (detail.shop.envScore / 10).toFixed(1) }}</text></view>
        <view class="dim"><text class="dim-label">服务</text><text class="dim-val">{{ (detail.shop.serviceScore / 10).toFixed(1) }}</text></view>
        <view class="dim"><text class="dim-label">性价比</text><text class="dim-val">{{ (detail.shop.valueScore / 10).toFixed(1) }}</text></view>
      </view>
    </view>

    <view class="section-head">
      <text class="section-title">团购套餐</text>
      <text class="section-sub">{{ detail.groupbuys.length }} 个优惠</text>
    </view>
    <view class="gb" v-for="g in detail.groupbuys" :key="g.id" @click="goGroupbuy(g.id)">
      <view class="gb-thumb"><text>{{ g.name.slice(0, 1) }}</text></view>
      <view class="gb-info">
        <text class="gb-name">{{ g.name }}</text>
        <text class="gb-content">{{ g.content || '到店可用，随时退' }}</text>
        <view class="gb-price-row">
          <text class="price">¥{{ g.groupbuyPrice }}</text>
          <text class="orig">¥{{ g.originalPrice }}</text>
          <text class="sold-text">已售 {{ g.sold || 0 }}</text>
        </view>
      </view>
      <view class="buy">抢购</view>
    </view>
    <view v-if="!detail.groupbuys.length" class="empty">暂无团购</view>

    <view class="section-head">
      <text class="section-title">推荐菜品</text>
      <text class="section-sub">{{ detail.dishes.length }} 道</text>
    </view>
    <view class="dish" v-for="d in detail.dishes" :key="d.id">
      <view class="dish-thumb"><text>{{ d.name.slice(0, 1) }}</text></view>
      <view class="dish-info">
        <text class="dish-name">{{ d.name }}</text>
        <text class="dish-desc">{{ d.description || '招牌推荐' }}</text>
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
.page { min-height: 100vh; background: #f6f6f6; padding-bottom: 40rpx; }
.hero { position: relative; height: 320rpx; background: linear-gradient(135deg, #FFE79A, #FFC300); display: flex; align-items: center; justify-content: center; }
.hero-text { font-size: 110rpx; font-weight: 700; color: #8A6200; z-index: 1; }
.hero-mask { position: absolute; inset: 0; background: linear-gradient(180deg, rgba(0,0,0,0) 40%, rgba(0,0,0,.25)); }
.main-card { position: relative; margin: -70rpx 24rpx 0; background: #fff; border-radius: 20rpx; padding: 26rpx; box-shadow: 0 6rpx 24rpx rgba(0,0,0,.06); }
.name { font-size: 36rpx; font-weight: 700; color: #222; }
.rating-row { display: flex; align-items: center; margin-top: 12rpx; }
.stars { color: #FF8A00; font-size: 22rpx; }
.score { color: #FF8A00; font-size: 26rpx; font-weight: 700; margin-left: 8rpx; }
.sold, .comments { color: #999; font-size: 22rpx; margin-left: 16rpx; }
.meta-row { display: flex; margin-top: 14rpx; }
.meta-label { width: 70rpx; color: #999; font-size: 24rpx; }
.meta-value { flex: 1; color: #444; font-size: 24rpx; line-height: 1.5; }
.score-card { margin: 18rpx 24rpx 0; background: #fff; border-radius: 18rpx; padding: 24rpx; display: flex; align-items: center; }
.score-big { width: 170rpx; display: flex; flex-direction: column; align-items: center; border-right: 1rpx solid #f0f0f0; }
.score-num { color: #FF8A00; font-size: 54rpx; font-weight: 700; }
.score-label { color: #999; font-size: 20rpx; margin-top: 4rpx; }
.dims { flex: 1; display: flex; flex-wrap: wrap; padding-left: 20rpx; }
.dim { width: 50%; display: flex; justify-content: space-between; padding: 6rpx 0; }
.dim-label { color: #777; font-size: 22rpx; }
.dim-val { color: #FF8A00; font-size: 22rpx; font-weight: 600; margin-right: 24rpx; }
.section-head { display: flex; align-items: baseline; justify-content: space-between; padding: 28rpx 28rpx 12rpx; }
.section-title { font-size: 30rpx; font-weight: 700; color: #222; }
.section-sub { font-size: 22rpx; color: #999; }
.gb { margin: 0 24rpx 16rpx; background: #fff; border-radius: 18rpx; padding: 20rpx; display: flex; align-items: center; }
.gb-thumb { width: 120rpx; height: 120rpx; border-radius: 12rpx; background: linear-gradient(135deg, #FFE08A, #FFC300); display: flex; align-items: center; justify-content: center; color: #7A5600; font-size: 40rpx; font-weight: 700; flex-shrink: 0; }
.gb-info { flex: 1; margin-left: 18rpx; min-width: 0; }
.gb-name { font-size: 28rpx; font-weight: 600; color: #222; }
.gb-content { display: block; color: #999; font-size: 22rpx; margin-top: 6rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.gb-price-row { display: flex; align-items: baseline; margin-top: 10rpx; }
.price { color: #FF4B14; font-size: 32rpx; font-weight: 700; }
.orig { color: #bbb; font-size: 20rpx; text-decoration: line-through; margin-left: 10rpx; }
.sold-text { color: #999; font-size: 20rpx; margin-left: 14rpx; }
.buy { background: #FFD100; color: #222; font-size: 24rpx; font-weight: 600; border-radius: 30rpx; padding: 12rpx 30rpx; flex-shrink: 0; }
.dish { margin: 0 24rpx 14rpx; background: #fff; border-radius: 16rpx; padding: 18rpx; display: flex; align-items: center; }
.dish-thumb { width: 100rpx; height: 100rpx; border-radius: 10rpx; background: #FFF4CE; color: #B07A00; font-size: 34rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; }
.dish-info { flex: 1; margin-left: 16rpx; display: flex; flex-direction: column; }
.dish-name { font-size: 28rpx; color: #222; }
.dish-desc { color: #999; font-size: 22rpx; margin-top: 6rpx; }
.dish-price { color: #FF4B14; font-size: 28rpx; font-weight: 700; }
.empty { text-align: center; color: #999; font-size: 24rpx; padding: 30rpx 0; }
</style>