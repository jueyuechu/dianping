<template>
  <view class="page">
    <view class="topbar">
      <picker v-if="shops.length" :range="shopNames" @change="onShopChange">
        <view class="shop-picker">{{ currentShop ? currentShop.name : '选择门店' }} ▾</view>
      </picker>
      <text class="count">共 {{ total }} 条评价</text>
    </view>

    <view class="review" v-for="r in reviews" :key="r.id">
      <view class="head">
        <text class="stars">{{ stars(r.score) }}</text>
        <text class="time">{{ fmt(r.createTime) }}</text>
      </view>
      <text class="content">{{ r.content || '（无文字内容）' }}</text>
      <view class="tags" v-if="r.tags">
        <text class="tag" v-for="t in r.tags.split(',')" :key="t">{{ t }}</text>
      </view>

      <view class="replies" v-if="replyMap[r.id] && replyMap[r.id].length">
        <view class="reply" v-for="rp in replyMap[r.id]" :key="rp.id">
          <text class="reply-label">商家回复：</text>{{ rp.content }}
        </view>
      </view>

      <view class="actions">
        <button class="act" @click="openReply(r)">回复</button>
      </view>

      <view class="reply-box" v-if="activeId === r.id">
        <textarea class="reply-input" v-model="draft" placeholder="回复用户评价…" maxlength="200" />
        <view class="reply-actions">
          <button class="cancel" @click="closeReply">取消</button>
          <button class="send" @click="submitReply(r)">发送</button>
        </view>
      </view>
    </view>

    <view v-if="!reviews.length" class="empty">暂无评价</view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'
import { requireLogin } from '../../utils/auth'

const shops = ref([])
const currentIndex = ref(0)
const reviews = ref([])
const total = ref(0)
const replyMap = ref({})
const activeId = ref(null)
const draft = ref('')

const shopNames = computed(() => shops.value.map(s => s.name))
const currentShop = computed(() => shops.value[currentIndex.value] || null)

function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

function stars(score) {
  const n = Math.round((score || 0) / 10)
  return '★'.repeat(n) + '☆'.repeat(Math.max(0, 5 - n))
}

async function load() {
  if (!requireLogin()) return
  try {
    shops.value = await api.shops()
    if (!shops.value.length) return
    if (currentIndex.value >= shops.value.length) currentIndex.value = 0
    const shopId = shops.value[currentIndex.value].id
    const page = await api.reviews(`current=1&size=20&shopId=${shopId}`)
    reviews.value = page.records || []
    total.value = Number(page.total)
    replyMap.value = {}
    for (const r of reviews.value) {
      const list = await api.reviewReplies(r.id)
      if (list.length) replyMap.value[r.id] = list
    }
  } catch (e) { /* 已提示 */ }
}

function onShopChange(e) {
  currentIndex.value = Number(e.detail.value)
  load()
}

function openReply(r) {
  activeId.value = r.id
  draft.value = ''
  uni.showToast({ title: '在下方输入回复', icon: 'none' })
}

function closeReply() {
  activeId.value = null
  draft.value = ''
}

async function submitReply(r) {
  if (!draft.value.trim()) {
    uni.showToast({ title: '请输入回复内容', icon: 'none' })
    return
  }
  await api.replyReview(r.id, draft.value.trim())
  uni.showToast({ title: '回复成功', icon: 'success' })
  closeReply()
  load()
}

onShow(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 30rpx; }
.topbar { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 24rpx; background: #fff; }
.shop-picker { font-size: 26rpx; color: #333; }
.count { font-size: 22rpx; color: #999; }
.review { margin: 20rpx 24rpx 0; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.head { display: flex; justify-content: space-between; align-items: center; }
.stars { color: #ffb400; font-size: 26rpx; }
.time { font-size: 20rpx; color: #bbb; }
.content { display: block; margin-top: 14rpx; font-size: 28rpx; color: #333; line-height: 1.7; }
.tags { margin-top: 12rpx; }
.tag { font-size: 20rpx; color: #00b578; border: 1rpx solid #00b578; border-radius: 6rpx; padding: 2rpx 10rpx; margin-right: 10rpx; }
.replies { margin-top: 16rpx; background: #f6f7f9; border-radius: 12rpx; padding: 14rpx 16rpx; }
.reply { font-size: 24rpx; color: #555; line-height: 1.6; }
.reply-label { color: #00b578; }
.actions { display: flex; justify-content: flex-end; margin-top: 12rpx; }
.act { background: #fff; border: 1rpx solid #00b578; color: #00b578; font-size: 24rpx; border-radius: 30rpx; padding: 0 28rpx; height: 60rpx; line-height: 60rpx; }
.reply-box { margin-top: 16rpx; }
.reply-input { width: auto; height: 140rpx; background: #f5f6f8; border-radius: 12rpx; padding: 16rpx; font-size: 26rpx; }
.reply-actions { display: flex; justify-content: flex-end; margin-top: 12rpx; }
.cancel { background: #fff; border: 1rpx solid #ddd; color: #666; font-size: 24rpx; border-radius: 30rpx; padding: 0 28rpx; height: 60rpx; line-height: 60rpx; margin-right: 12rpx; }
.send { background: #00b578; color: #fff; font-size: 24rpx; border-radius: 30rpx; padding: 0 30rpx; height: 60rpx; line-height: 60rpx; }
.empty { text-align: center; color: #999; font-size: 26rpx; padding: 100rpx 0; }
</style>