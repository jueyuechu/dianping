<template>
  <view class="page">
    <view class="head">
      <text class="title">发现</text>
      <text class="sub">探店笔记 · 社区动态</text>
    </view>

    <view class="tabs">
      <view class="tab" :class="{ on: type === 'recommend' }" @click="switchType('recommend')">推荐</view>
      <view class="tab" :class="{ on: type === 'latest' }" @click="switchType('latest')">最新</view>
      <view class="tab" :class="{ on: type === 'follow' }" @click="switchType('follow')">关注</view>
    </view>

    <view class="card" v-for="b in blogs" :key="b.id" @click="goDetail(b.id)">
      <view class="author">
        <view class="avatar">{{ (b.authorName || '用').slice(0, 1) }}</view>
        <view class="author-info">
          <text class="author-name">{{ b.authorName }}</text>
          <text class="time">{{ fmt(b.createTime) }}</text>
        </view>
        <text class="type-tag">{{ b.type === 1 ? '探店' : '动态' }}</text>
      </view>

      <text class="content">{{ b.content }}</text>

      <view class="imgs" v-if="b.images && b.images.length">
        <view class="img" v-for="(img, i) in b.images.slice(0, 3)" :key="i">
          <image class="img-real" :src="img" mode="aspectFill" />
        </view>
      </view>

      <view class="shop-chip" v-if="b.shopName">📍 {{ b.shopName }}</view>
      <view class="loc-chip" v-else-if="b.location">📍 {{ b.location }}</view>

      <view class="actions">
        <view class="act" :class="{ liked: b.liked }" @click.stop="toggleLike(b)">
          <text>{{ b.liked ? '♥' : '♡' }}</text>
          <text class="num">{{ b.likedCount || 0 }}</text>
        </view>
        <view class="act" @click.stop="goDetail(b.id)">
          <text>评论</text>
          <text class="num">{{ b.commentCount || 0 }}</text>
        </view>
      </view>
    </view>

    <view v-if="emptyText" class="empty">{{ emptyText }}</view>

    <view class="fab" @click="goEdit">+</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'
import { isLogin, requireLogin } from '../../utils/auth'

const type = ref('recommend')
const blogs = ref([])
const emptyText = ref('')

function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(5, 16) : ''
}

async function load() {
  emptyText.value = ''
  try {
    const page = await api.blogFeed(type.value, 1, 20)
    blogs.value = page.records || []
    if (!blogs.value.length) {
      emptyText.value = type.value === 'follow' ? '还没有关注的作者，去推荐流逛逛吧' : '还没有笔记，来发布第一篇吧'
    }
  } catch (e) {
    blogs.value = []
    emptyText.value = '加载失败'
  }
}

function switchType(t) {
  type.value = t
  load()
}

async function toggleLike(b) {
  if (!requireLogin()) return
  try {
    const res = await api.likeBlog(b.id)
    b.liked = res.liked
    b.likedCount = Math.max(0, (b.likedCount || 0) + (res.liked ? 1 : -1))
  } catch (e) { /* 已提示 */ }
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/blog/detail?id=${id}` })
}

function goEdit() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/blog/edit' })
}

onShow(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 120rpx; }
.head { background: linear-gradient(135deg, #ff6633, #ff8c5a); padding: 40rpx 32rpx; color: #fff; }
.title { font-size: 40rpx; font-weight: 700; display: block; }
.sub { font-size: 24rpx; opacity: .9; margin-top: 8rpx; display: block; }
.tabs { display: flex; background: #fff; }
.tab { flex: 1; text-align: center; padding: 22rpx 0; font-size: 28rpx; color: #666; }
.tab.on { color: #ff6633; font-weight: 600; border-bottom: 4rpx solid #ff6633; }
.card { margin: 20rpx 24rpx 0; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.author { display: flex; align-items: center; }
.avatar { width: 68rpx; height: 68rpx; border-radius: 50%; background: linear-gradient(135deg, #ffb199, #ff6633); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 26rpx; }
.author-info { margin-left: 16rpx; flex: 1; }
.author-name { font-size: 26rpx; font-weight: 600; display: block; }
.time { font-size: 20rpx; color: #bbb; }
.type-tag { font-size: 20rpx; color: #ff6633; background: #fff4ee; border-radius: 8rpx; padding: 4rpx 12rpx; }
.content { display: block; margin-top: 18rpx; font-size: 28rpx; color: #333; line-height: 1.7; }
.imgs { display: flex; margin-top: 16rpx; }
.img { width: 200rpx; height: 200rpx; border-radius: 12rpx; overflow: hidden; margin-right: 10rpx; background: linear-gradient(135deg, #ffb199, #ff6633); }
.img-real { width: 100%; height: 100%; }
.shop-chip, .loc-chip { display: inline-block; margin-top: 16rpx; background: #fff4ee; border: 1rpx solid #ffd9c7; color: #ff6633; font-size: 22rpx; border-radius: 26rpx; padding: 8rpx 20rpx; }
.actions { margin-top: 18rpx; display: flex; }
.act { display: flex; align-items: center; font-size: 24rpx; color: #999; margin-right: 40rpx; }
.act.liked { color: #ff3b30; }
.num { margin-left: 8rpx; }
.empty { text-align: center; color: #999; font-size: 26rpx; padding: 100rpx 0; }
.fab { position: fixed; right: 36rpx; bottom: 140rpx; width: 100rpx; height: 100rpx; border-radius: 50%; background: #ff6633; color: #fff; font-size: 52rpx; display: flex; align-items: center; justify-content: center; box-shadow: 0 8rpx 24rpx rgba(255, 102, 51, .4); }
</style>