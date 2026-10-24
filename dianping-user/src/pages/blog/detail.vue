<template>
  <view class="page" v-if="blog">
    <view class="card">
      <view class="author">
        <view class="avatar">{{ (blog.authorName || '用').slice(0, 1) }}</view>
        <view class="author-info">
          <text class="author-name">{{ blog.authorName }}</text>
          <text class="time">{{ fmt(blog.createTime) }}</text>
        </view>
      </view>
      <text class="content">{{ blog.content }}</text>
      <view class="imgs" v-if="blog.images && blog.images.length">
        <image class="img" v-for="(img, i) in blog.images" :key="i" :src="img" mode="widthFix" />
      </view>
      <view class="shop-chip" v-if="blog.shopName">📍 {{ blog.shopName }}</view>
      <view class="loc-chip" v-else-if="blog.location">📍 {{ blog.location }}</view>
      <view class="stats">
        <text>{{ blog.likedCount || 0 }} 点赞</text>
        <text>{{ blog.commentCount || 0 }} 评论</text>
      </view>
    </view>

    <view class="section">评论</view>
    <view class="comment" v-for="c in comments" :key="c.id">
      <view class="c-avatar">{{ (c.userName || '用').slice(0, 1) }}</view>
      <view class="c-body">
        <text class="c-name">{{ c.userName }}</text>
        <text class="c-text">{{ c.content }}</text>
      </view>
    </view>
    <view v-if="!comments.length" class="empty">还没有评论，来说两句</view>

    <view class="report" @click="report">举报该笔记</view>

    <view class="bar">
      <view class="bar-act" :class="{ liked: blog.liked }" @click="toggleLike">{{ blog.liked ? '♥' : '♡' }} {{ blog.likedCount || 0 }}</view>
      <input class="bar-input" v-model="draft" placeholder="说点什么…" @confirm="send" />
      <button class="bar-btn" @click="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api'
import { requireLogin } from '../../utils/auth'

const id = ref('')
const blog = ref(null)
const comments = ref([])
const draft = ref('')

function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

async function loadDetail() {
  try {
    blog.value = await api.blogDetail(id.value)
  } catch (e) { /* 已提示 */ }
}

async function loadComments() {
  try {
    const page = await api.blogComments(id.value, 1, 50)
    comments.value = page.records || []
  } catch (e) {
    comments.value = []
  }
}

async function toggleLike() {
  if (!requireLogin()) return
  const res = await api.likeBlog(id.value)
  blog.value.liked = res.liked
  blog.value.likedCount = Math.max(0, (blog.value.likedCount || 0) + (res.liked ? 1 : -1))
}

async function send() {
  if (!requireLogin()) return
  if (!draft.value.trim()) {
    uni.showToast({ title: '请输入评论内容', icon: 'none' })
    return
  }
  await api.commentBlog(id.value, { content: draft.value.trim() })
  draft.value = ''
  uni.showToast({ title: '评论成功', icon: 'success' })
  loadDetail()
  loadComments()
}

function report() {
  if (!requireLogin()) return
  uni.showModal({
    title: '举报笔记',
    content: '确认举报该笔记含违规内容？',
    success: async res => {
      if (!res.confirm) return
      await api.reportBlog(id.value, '含违规内容')
      uni.showToast({ title: '已提交举报', icon: 'none' })
    }
  })
}

onLoad(options => {
  id.value = options.id
  loadDetail()
  loadComments()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding: 20rpx 0 160rpx; }
.card { margin: 0 24rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.author { display: flex; align-items: center; }
.avatar { width: 76rpx; height: 76rpx; border-radius: 50%; background: linear-gradient(135deg, #FFE08A, #FFC300); color: #6B4A00; font-weight: 600; display: flex; align-items: center; justify-content: center; font-size: 28rpx; }
.author-info { margin-left: 16rpx; }
.author-name { font-size: 28rpx; font-weight: 600; display: block; }
.time { font-size: 20rpx; color: #bbb; }
.content { display: block; margin-top: 20rpx; font-size: 30rpx; color: #333; line-height: 1.75; }
.imgs { margin-top: 16rpx; }
.img { width: 100%; border-radius: 12rpx; margin-bottom: 12rpx; }
.shop-chip, .loc-chip { display: inline-block; margin-top: 16rpx; background: #FFF6D9; border: 1rpx solid #FFE08A; color: #B07A00; font-size: 22rpx; border-radius: 26rpx; padding: 8rpx 20rpx; }
.stats { margin-top: 18rpx; display: flex; font-size: 22rpx; color: #999; }
.stats text { margin-right: 30rpx; }
.section { padding: 30rpx 32rpx 12rpx; font-size: 28rpx; font-weight: 600; }
.comment { display: flex; margin: 0 24rpx 18rpx; }
.c-avatar { width: 60rpx; height: 60rpx; border-radius: 50%; background: #FFF4CE; color: #B07A00; display: flex; align-items: center; justify-content: center; font-size: 22rpx; flex-shrink: 0; }
.c-body { margin-left: 16rpx; display: flex; flex-direction: column; }
.c-name { font-size: 22rpx; color: #999; }
.c-text { font-size: 26rpx; color: #333; margin-top: 6rpx; line-height: 1.6; }
.empty { text-align: center; color: #999; font-size: 24rpx; padding: 30rpx 0; }
.report { text-align: right; padding: 10rpx 32rpx 30rpx; color: #bbb; font-size: 22rpx; }
.bar { position: fixed; left: 0; right: 0; bottom: 0; height: 110rpx; background: #fff; display: flex; align-items: center; padding: 0 24rpx; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,.06); }
.bar-act { font-size: 24rpx; color: #666; margin-right: 20rpx; }
.bar-act.liked { color: #ff3b30; }
.bar-input { flex: 1; height: 68rpx; background: #f4f5f7; border-radius: 34rpx; padding: 0 24rpx; font-size: 24rpx; }
.bar-btn { margin-left: 14rpx; background: #FFD100; color: #222; font-weight: 600; font-size: 24rpx; border-radius: 34rpx; padding: 0 28rpx; height: 68rpx; line-height: 68rpx; }
</style>