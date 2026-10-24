<template>
  <view class="page">
    <view class="topbar">
      <picker v-if="shops.length" :range="shopNames" @change="onShopChange">
        <view class="shop-picker">{{ currentShop ? currentShop.name : '选择门店' }} ▾</view>
      </picker>
      <button class="add" @click="goAdd">+ 新增团购</button>
    </view>

    <view v-if="!shops.length" class="empty-card">
      <text class="empty-text">你还没有门店，请先创建门店</text>
      <button class="btn" @click="goShop">去创建门店</button>
    </view>

    <view class="gb" v-for="g in groupbuys" :key="g.id">
      <view class="gb-head">
        <text class="gb-name">{{ g.name }}</text>
        <text class="status" :class="'s' + g.status">{{ statusText(g.status) }}</text>
      </view>
      <view class="gb-price">
        <text class="price">¥{{ g.groupbuyPrice }}</text>
        <text class="orig">¥{{ g.originalPrice }}</text>
      </view>
      <view class="gb-meta">
        <text class="meta">库存 {{ g.stock }}</text>
        <text class="meta">销量 {{ g.sold }}</text>
      </view>
      <view class="gb-actions">
        <button class="act" v-if="g.status === 1" @click="toggle(g, 0)">下架</button>
        <button class="act green" v-if="g.status === 0" @click="toggle(g, 1)">上架</button>
        <button class="act" @click="edit(g.id)">编辑</button>
      </view>
    </view>

    <view v-if="shops.length && !groupbuys.length" class="empty-card">
      <text class="empty-text">还没有团购，创建一个吧</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'
import { GROUPBUY_STATUS, requireLogin } from '../../utils/auth'

const shops = ref([])
const currentIndex = ref(0)
const groupbuys = ref([])
const shopNames = computed(() => shops.value.map(s => s.name))
const currentShop = computed(() => shops.value[currentIndex.value] || null)

function statusText(s) {
  return GROUPBUY_STATUS[s] || '—'
}

async function load() {
  if (!requireLogin()) return
  try {
    shops.value = await api.shops()
    if (shops.value.length) {
      if (currentIndex.value >= shops.value.length) currentIndex.value = 0
      groupbuys.value = await api.groupbuys(shops.value[currentIndex.value].id)
    } else {
      groupbuys.value = []
    }
  } catch (e) { /* 已提示 */ }
}

function onShopChange(e) {
  currentIndex.value = Number(e.detail.value)
  load()
}

async function toggle(g, status) {
  await api.setGroupbuyStatus(g.id, status)
  uni.showToast({ title: '操作成功', icon: 'success' })
  load()
}

function edit(id) {
  uni.navigateTo({ url: `/pages/groupbuy/edit?id=${id}` })
}

function goAdd() {
  if (!shops.value.length) {
    uni.showToast({ title: '请先创建门店', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/groupbuy/edit?shopId=${currentShop.value.id}` })
}

function goShop() {
  uni.navigateTo({ url: '/pages/shop/edit' })
}

onShow(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 30rpx; }
.topbar { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 24rpx; background: #fff; }
.shop-picker { font-size: 26rpx; color: #333; }
.add { background: #00b578; color: #fff; font-size: 24rpx; border-radius: 30rpx; padding: 0 24rpx; height: 60rpx; line-height: 60rpx; }
.gb { margin: 20rpx 24rpx 0; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.gb-head { display: flex; justify-content: space-between; align-items: center; }
.gb-name { font-size: 30rpx; font-weight: 600; }
.status { font-size: 22rpx; color: #00b578; }
.status.s0 { color: #999; }
.status.s2 { color: #e88a1a; }
.gb-price { margin-top: 12rpx; }
.price { color: #ff3b30; font-size: 34rpx; font-weight: 700; }
.orig { color: #999; font-size: 22rpx; text-decoration: line-through; margin-left: 10rpx; }
.gb-meta { margin-top: 10rpx; }
.meta { font-size: 22rpx; color: #999; margin-right: 24rpx; }
.gb-actions { display: flex; justify-content: flex-end; margin-top: 16rpx; }
.act { margin-left: 16rpx; background: #fff; border: 1rpx solid #ddd; color: #666; font-size: 24rpx; border-radius: 30rpx; padding: 0 26rpx; height: 60rpx; line-height: 60rpx; }
.act.green { border-color: #00b578; color: #00b578; }
.empty-card { margin: 40rpx 24rpx; background: #fff; border-radius: 20rpx; padding: 60rpx 30rpx; text-align: center; }
.empty-text { color: #999; font-size: 26rpx; display: block; }
.btn { margin-top: 24rpx; background: #00b578; color: #fff; border-radius: 40rpx; font-size: 26rpx; }
</style>