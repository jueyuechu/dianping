<template>
  <view class="page">
    <view class="search-bar">
      <input class="input" v-model="keyword" placeholder="搜索商家名称" confirm-type="search" @confirm="doSearch" />
      <view class="btn" @click="doSearch">搜索</view>
    </view>

    <view class="shop" v-for="s in results" :key="s.id" @click="goShop(s.id)">
      <view class="cover">{{ s.name.slice(0, 1) }}</view>
      <view class="info">
        <text class="name">{{ s.name }}</text>
        <text class="meta">{{ (s.score / 10).toFixed(1) }} 分 · 人均 ¥{{ s.avgPrice || '-' }} · {{ s.area || '' }}</text>
      </view>
    </view>

    <view v-if="searched && !results.length" class="empty">没有找到相关商家</view>
    <text class="tip">当前版本为本地筛选（Phase 1 接入全文检索）</text>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../../api'

const keyword = ref('')
const results = ref([])
const searched = ref(false)
let all = []

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    uni.showToast({ title: '请输入关键词', icon: 'none' })
    return
  }
  if (!all.length) {
    try {
      all = await api.nearby()
    } catch (e) {
      all = []
    }
  }
  results.value = all.filter(s => s.name && s.name.includes(kw))
  searched.value = true
}

function goShop(id) {
  uni.navigateTo({ url: `/pages/shop/detail?id=${id}` })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; }
.search-bar { background: #fff; padding: 20rpx 24rpx; display: flex; align-items: center; }
.input { flex: 1; height: 72rpx; background: #f4f5f7; border-radius: 36rpx; padding: 0 28rpx; font-size: 26rpx; }
.btn { margin-left: 16rpx; color: #ff6633; font-size: 28rpx; }
.shop { margin: 20rpx 24rpx 0; background: #fff; border-radius: 20rpx; padding: 24rpx; display: flex; align-items: center; }
.cover { width: 100rpx; height: 100rpx; border-radius: 16rpx; background: #ff6633; color: #fff; font-size: 36rpx; display: flex; align-items: center; justify-content: center; }
.info { margin-left: 20rpx; display: flex; flex-direction: column; }
.name { font-size: 28rpx; font-weight: 600; }
.meta { font-size: 22rpx; color: #999; margin-top: 10rpx; }
.empty { text-align: center; color: #999; font-size: 26rpx; padding: 100rpx 0; }
.tip { display: block; text-align: center; color: #ccc; font-size: 20rpx; margin-top: 40rpx; }
</style>