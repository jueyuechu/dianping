<template>
  <view class="page">
    <!-- 顶部定位 + 搜索 -->
    <view class="hero">
      <view class="city">杭州 · 拱墅区</view>
      <view class="search" @click="goSearch">
        <text class="search-ic">⌕</text>
        <text class="search-text">搜索商家 / 菜品 / 团购</text>
      </view>
    </view>

    <!-- 分类 -->
    <view class="cats">
      <view class="cat" v-for="c in cats" :key="c.name" @click="toast(c.name + '分类')">
        <view class="cat-ico" :style="{ background: c.bg, color: c.color }">{{ c.name.slice(0, 1) }}</view>
        <text class="cat-name">{{ c.name }}</text>
      </view>
    </view>

    <!-- 附近热门 -->
    <view class="section">
      <text class="section-title">附近热门</text>
      <text class="section-sub">{{ shops.length }} 家</text>
    </view>

    <view class="shop" v-for="s in shops" :key="s.id" @click="goShop(s.id)">
      <view class="shop-cover">{{ s.name.slice(0, 1) }}</view>
      <view class="shop-info">
        <text class="shop-name">{{ s.name }}</text>
        <view class="shop-score">
          <text class="stars">{{ stars(s.score) }}</text>
          <text class="score">{{ (s.score / 10).toFixed(1) }}</text>
        </view>
        <text class="shop-meta">人均 ¥{{ s.avgPrice || '-' }} · {{ s.area || '' }}</text>
        <view class="tags">
          <text class="tag">团购</text>
          <text class="tag ghost">{{ s.openHours || '营业中' }}</text>
        </view>
      </view>
    </view>

    <view v-if="!loading && !shops.length" class="empty">暂无商家，请确认后端已启动</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { api } from '../../api'

const shops = ref([])
const loading = ref(false)
const cats = [
  { name: '美食', bg: '#ffe8dd', color: '#ff6633' },
  { name: '火锅', bg: '#fff0d9', color: '#e88a1a' },
  { name: '烧烤', bg: '#ffe3e3', color: '#e04545' },
  { name: '奶茶', bg: '#e8f5e9', color: '#2e9e5b' },
  { name: '酒店', bg: '#e3f2fd', color: '#2f7fd6' }
]

function stars(score) {
  const n = Math.round((score || 0) / 10)
  return '★'.repeat(n) + '☆'.repeat(Math.max(0, 5 - n))
}

async function load() {
  loading.value = true
  try {
    shops.value = await api.nearby()
  } catch (e) {
    shops.value = []
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

function goShop(id) {
  uni.navigateTo({ url: `/pages/shop/detail?id=${id}` })
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/search' })
}

function toast(title) {
  uni.showToast({ title, icon: 'none' })
}

onShow(load)
onPullDownRefresh(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 24rpx; }
.hero { background: linear-gradient(180deg, #ff6633, #ff8c5a); padding: 24rpx 32rpx 40rpx; }
.city { color: #fff; font-size: 30rpx; font-weight: 600; }
.search { margin-top: 20rpx; height: 76rpx; background: #fff; border-radius: 40rpx; display: flex; align-items: center; padding: 0 28rpx; }
.search-ic { color: #b0b0b8; font-size: 32rpx; margin-right: 12rpx; }
.search-text { color: #b0b0b8; font-size: 26rpx; }
.cats { display: flex; background: #fff; padding: 28rpx 16rpx 20rpx; }
.cat { flex: 1; display: flex; flex-direction: column; align-items: center; }
.cat-ico { width: 88rpx; height: 88rpx; border-radius: 28rpx; display: flex; align-items: center; justify-content: center; font-size: 34rpx; font-weight: 600; }
.cat-name { font-size: 22rpx; color: #444; margin-top: 12rpx; }
.section { display: flex; align-items: baseline; justify-content: space-between; padding: 32rpx 32rpx 16rpx; }
.section-title { font-size: 32rpx; font-weight: 600; }
.section-sub { font-size: 22rpx; color: #999; }
.shop { margin: 0 32rpx 20rpx; background: #fff; border-radius: 24rpx; padding: 24rpx; display: flex; }
.shop-cover { width: 176rpx; height: 176rpx; border-radius: 16rpx; background: linear-gradient(135deg, #ffb199, #ff6633); color: #fff; font-size: 56rpx; display: flex; align-items: center; justify-content: center; }
.shop-info { flex: 1; margin-left: 20rpx; display: flex; flex-direction: column; }
.shop-name { font-size: 30rpx; font-weight: 600; }
.shop-score { margin-top: 8rpx; display: flex; align-items: center; }
.stars { color: #ffb400; font-size: 22rpx; }
.score { color: #ffb400; font-size: 24rpx; font-weight: 600; margin-left: 8rpx; }
.shop-meta { color: #999; font-size: 22rpx; margin-top: 8rpx; }
.tags { margin-top: auto; display: flex; }
.tag { font-size: 20rpx; color: #ff6633; border: 1rpx solid #ff6633; border-radius: 6rpx; padding: 2rpx 10rpx; margin-right: 10rpx; }
.tag.ghost { color: #999; border-color: #ddd; }
.empty { text-align: center; color: #999; font-size: 26rpx; padding: 80rpx 0; }
</style>