<template>
  <view class="page">
    <!-- 顶部：定位 + 搜索 -->
    <view class="hero">
      <view class="hero-top">
        <view class="city" @click="toast('切换城市/商圈')">
          <text class="city-name">杭州</text>
          <text class="city-arrow">▾</text>
        </view>
        <view class="msg" @click="toast('消息中心 P1 上线')">消息</view>
      </view>
      <view class="search" @click="goSearch">
        <text class="search-ic">⌕</text>
        <text class="search-text">搜索商家 / 菜品 / 团购</text>
        <text class="search-btn">搜索</text>
      </view>
    </view>

    <!-- 分类 -->
    <view class="cats">
      <view class="cat" v-for="c in cats" :key="c.name" @click="toast(c.name + '分类')">
        <view class="cat-ico" :style="{ background: c.bg }">
          <text :style="{ color: c.color }">{{ c.name.slice(0, 1) }}</text>
        </view>
        <text class="cat-name">{{ c.name }}</text>
      </view>
    </view>

    <!-- Banner -->
    <view class="banner" @click="toast('新客立减活动')">
      <view class="banner-left">
        <text class="banner-title">新客立减</text>
        <text class="banner-sub">首单最高立省 ¥20</text>
      </view>
      <view class="banner-right">
        <text class="banner-cta">立即领取 ›</text>
      </view>
    </view>

    <!-- 附近热门 -->
    <view class="section">
      <text class="section-title">附近热门</text>
      <view class="sorts">
        <text class="sort on">综合</text>
        <text class="sort" @click="toast('按距离排序')">距离</text>
        <text class="sort" @click="toast('按评分排序')">评分</text>
      </view>
    </view>

    <view class="shop" v-for="s in shops" :key="s.id" @click="goShop(s.id)">
      <view class="shop-cover">
        <text class="cover-text">{{ s.name.slice(0, 1) }}</text>
        <view class="cover-badge" v-if="s.score >= 48">高分</view>
      </view>
      <view class="shop-info">
        <text class="shop-name">{{ s.name }}</text>
        <view class="shop-score">
          <text class="stars">★★★★★</text>
          <text class="score">{{ (s.score / 10).toFixed(1) }}</text>
          <text class="sold">月售 {{ s.sold || 0 }}</text>
        </view>
        <text class="shop-meta">人均 ¥{{ s.avgPrice || '-' }} · {{ s.area || '附近' }}</text>
        <view class="tags">
          <text class="tag">团购</text>
          <text class="tag light">{{ s.openHours || '营业中' }}</text>
        </view>
      </view>
      <view class="shop-cta">
        <text class="cta-price">¥{{ s.avgPrice || '-' }}</text>
        <text class="cta-btn">抢购</text>
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
  { name: '美食', bg: '#FFF1CC', color: '#B07A00' },
  { name: '火锅', bg: '#FFE7DC', color: '#D9531E' },
  { name: '烧烤', bg: '#FFE1E1', color: '#C93B3B' },
  { name: '奶茶', bg: '#E6F6EA', color: '#2E9E5B' },
  { name: '酒店', bg: '#E7F0FF', color: '#2F6FD0' }
]

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
.page { min-height: 100vh; background: #f6f6f6; padding-bottom: 24rpx; }
.hero { background: #FFD100; padding: 20rpx 28rpx 76rpx; }
.hero-top { display: flex; align-items: center; justify-content: space-between; }
.city { display: flex; align-items: center; }
.city-name { font-size: 34rpx; font-weight: 700; color: #222; }
.city-arrow { font-size: 22rpx; color: #222; margin-left: 6rpx; }
.msg { font-size: 24rpx; color: #333; }
.search { margin-top: 18rpx; background: #fff; border-radius: 36rpx; height: 76rpx; display: flex; align-items: center; padding: 0 10rpx 0 24rpx; }
.search-ic { color: #bbb; font-size: 30rpx; }
.search-text { flex: 1; color: #b7b7b7; font-size: 26rpx; margin-left: 8rpx; }
.search-btn { background: #FFD100; color: #222; font-size: 24rpx; font-weight: 600; border-radius: 30rpx; padding: 10rpx 26rpx; }
.cats { display: flex; background: #fff; margin: -56rpx 24rpx 0; border-radius: 20rpx; padding: 26rpx 8rpx 22rpx; box-shadow: 0 6rpx 20rpx rgba(0,0,0,.04); position: relative; }
.cat { flex: 1; display: flex; flex-direction: column; align-items: center; }
.cat-ico { width: 84rpx; height: 84rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32rpx; font-weight: 700; }
.cat-name { font-size: 22rpx; color: #333; margin-top: 10rpx; }
.banner { margin: 18rpx 24rpx 0; background: linear-gradient(100deg, #FFF3C4, #FFE07A); border-radius: 18rpx; padding: 24rpx; display: flex; align-items: center; justify-content: space-between; }
.banner-title { font-size: 32rpx; font-weight: 700; color: #6B4A00; }
.banner-sub { display: block; font-size: 22rpx; color: #8A6500; margin-top: 6rpx; }
.banner-cta { font-size: 24rpx; color: #6B4A00; font-weight: 600; }
.section { display: flex; align-items: center; justify-content: space-between; padding: 28rpx 28rpx 12rpx; }
.section-title { font-size: 32rpx; font-weight: 700; color: #222; }
.sorts { display: flex; }
.sort { font-size: 24rpx; color: #888; margin-left: 24rpx; }
.sort.on { color: #222; font-weight: 600; }
.shop { margin: 0 24rpx 18rpx; background: #fff; border-radius: 18rpx; padding: 20rpx; display: flex; }
.shop-cover { position: relative; width: 168rpx; height: 168rpx; border-radius: 12rpx; background: linear-gradient(135deg, #FFE08A, #FFC300); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.cover-text { font-size: 52rpx; font-weight: 700; color: #7A5600; }
.cover-badge { position: absolute; left: 8rpx; top: 8rpx; background: rgba(0,0,0,.55); color: #FFD100; font-size: 18rpx; border-radius: 6rpx; padding: 2rpx 8rpx; }
.shop-info { flex: 1; margin-left: 18rpx; min-width: 0; }
.shop-name { font-size: 30rpx; font-weight: 700; color: #222; }
.shop-score { display: flex; align-items: center; margin-top: 8rpx; }
.stars { color: #FF8A00; font-size: 20rpx; letter-spacing: 1rpx; }
.score { color: #FF8A00; font-size: 24rpx; font-weight: 600; margin-left: 8rpx; }
.sold { color: #999; font-size: 20rpx; margin-left: 12rpx; }
.shop-meta { display: block; color: #777; font-size: 22rpx; margin-top: 8rpx; }
.tags { display: flex; margin-top: 10rpx; }
.tag { font-size: 18rpx; color: #B07A00; background: #FFF6D9; border-radius: 6rpx; padding: 2rpx 8rpx; margin-right: 8rpx; }
.tag.light { color: #888; background: #F5F5F5; }
.shop-cta { display: flex; flex-direction: column; align-items: flex-end; justify-content: center; margin-left: 8rpx; }
.cta-price { color: #FF4B14; font-size: 26rpx; font-weight: 700; }
.cta-btn { margin-top: 12rpx; background: #FFD100; color: #222; font-size: 22rpx; font-weight: 600; border-radius: 24rpx; padding: 8rpx 22rpx; }
.empty { text-align: center; color: #999; font-size: 26rpx; padding: 90rpx 0; }
</style>