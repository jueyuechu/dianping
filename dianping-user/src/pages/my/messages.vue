<template>
  <view class="page">
    <view class="top">
      <text class="title">消息通知</text>
      <text v-if="messages.length" class="action" @click="readAll">全部已读</text>
    </view>

    <view v-if="!messages.length" class="empty">
      <text class="empty-icon">🔔</text>
      <text class="empty-text">暂无消息</text>
      <text class="empty-sub">下单、核销、审核结果会在这里通知你</text>
    </view>

    <view v-for="m in messages" :key="m.id" class="item" :class="{ unread: m.isRead === 0 }" @click="open(m)">
      <view class="dot" v-if="m.isRead === 0"></view>
      <view class="body">
        <text class="content">{{ m.content }}</text>
        <text class="time">{{ m.createTime }}</text>
      </view>
    </view>

    <view v-if="hasMore" class="more" @click="loadMore">加载更多</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onReachBottom, onShow } from '@dcloudio/uni-app'
import { api } from '../../api'

const messages = ref([])
const current = ref(1)
const total = ref(0)
const hasMore = ref(false)
const loading = ref(false)

onShow(() => {
  current.value = 1
  load(true)
})

onReachBottom(() => {
  if (hasMore.value) loadMore()
})

async function load(reset = false) {
  if (loading.value) return
  loading.value = true
  try {
    const data = await api.messages(current.value, 20)
    const records = data.records || []
    messages.value = reset ? records : messages.value.concat(records)
    total.value = data.total || 0
    hasMore.value = messages.value.length < total.value
  } catch (e) {
    // 请求封装已提示
  } finally {
    loading.value = false
  }
}

function loadMore() {
  current.value += 1
  load()
}

async function open(m) {
  if (m.isRead === 0) {
    try {
      await api.readMessage(m.id)
      m.isRead = 1
    } catch (e) {
      // ignore
    }
  }
}

async function readAll() {
  try {
    await api.readAllMessages()
    messages.value.forEach(m => { m.isRead = 1 })
    uni.showToast({ title: '已全部标记为已读', icon: 'none' })
  } catch (e) {
    // ignore
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 40rpx; }
.top { background: #FFD100; padding: 24rpx 32rpx; display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 32rpx; font-weight: 700; color: #222; }
.action { font-size: 24rpx; color: #6B4A00; background: rgba(255, 255, 255, .55); padding: 8rpx 20rpx; border-radius: 20rpx; }
.item { display: flex; align-items: flex-start; background: #fff; margin: 20rpx 24rpx 0; padding: 26rpx 24rpx; border-radius: 16rpx; }
.item.unread { background: #fffdf3; }
.dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #E6A800; margin: 10rpx 16rpx 0 0; flex-shrink: 0; }
.body { flex: 1; }
.content { font-size: 27rpx; color: #333; line-height: 1.6; display: block; }
.time { font-size: 22rpx; color: #999; margin-top: 10rpx; display: block; }
.empty { padding: 160rpx 0; display: flex; flex-direction: column; align-items: center; }
.empty-icon { font-size: 80rpx; }
.empty-text { font-size: 28rpx; color: #666; margin-top: 20rpx; }
.empty-sub { font-size: 22rpx; color: #aaa; margin-top: 12rpx; }
.more { text-align: center; padding: 30rpx 0; font-size: 24rpx; color: #999; }
</style>
