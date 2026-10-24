<template>
  <view class="page">
    <view class="review" v-for="r in reviews" :key="r.id">
      <view class="review-head">
        <text class="stars">{{ '★'.repeat(Math.round(r.score / 10)) }}</text>
        <text class="time">{{ (r.createTime || '').replace('T', ' ').slice(0, 16) }}</text>
      </view>
      <text class="content">{{ r.content }}</text>
      <view class="tags" v-if="r.tags">
        <text class="tag" v-for="t in r.tags.split(',')" :key="t">{{ t }}</text>
      </view>
    </view>
    <view v-if="!reviews.length" class="empty">还没有发布过评价</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'

const reviews = ref([])

onShow(async () => {
  try {
    reviews.value = await api.myReviews()
  } catch (e) {
    reviews.value = []
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding: 20rpx 0; }
.review { margin: 0 24rpx 20rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.review-head { display: flex; justify-content: space-between; align-items: center; }
.stars { color: #ffb400; font-size: 26rpx; }
.time { color: #bbb; font-size: 22rpx; }
.content { display: block; margin-top: 14rpx; font-size: 28rpx; color: #333; line-height: 1.7; }
.tags { margin-top: 14rpx; }
.tag { font-size: 20rpx; color: #ff6633; border: 1rpx solid #ff6633; border-radius: 6rpx; padding: 2rpx 10rpx; margin-right: 10rpx; }
.empty { text-align: center; color: #999; font-size: 26rpx; padding: 120rpx 0; }
</style>