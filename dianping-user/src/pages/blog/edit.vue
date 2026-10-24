<template>
  <view class="page">
    <view class="chips">
      <view class="chip" :class="{ on: form.type === 1 }" @click="setType(1)">探店笔记</view>
      <view class="chip" :class="{ on: form.type === 2 }" @click="setType(2)">自由动态</view>
    </view>

    <textarea class="textarea" v-model="form.content" placeholder="分享你的探店心得或生活动态…" maxlength="500" />

    <view class="row" v-if="form.type === 1">
      <text class="label">关联店铺</text>
      <picker v-if="shops.length" :range="shopNames" :value="shopIndex" @change="e => shopIndex = Number(e.detail.value)">
        <text class="picker">{{ shopNames[shopIndex] }} ▾</text>
      </picker>
      <text v-else class="picker" @click="loadShops">点击加载店铺</text>
    </view>

    <view class="row">
      <text class="label">定位</text>
      <input class="input" v-model="form.location" placeholder="如：杭州 · 西湖" />
    </view>

    <view class="uploader">
      <view class="img-box" v-for="(img, i) in images" :key="i">
        <image class="img" :src="img" mode="aspectFill" />
        <text class="del" @click="removeImage(i)">×</text>
      </view>
      <view v-if="images.length < 3" class="add" @click="chooseImage">+ 图片</view>
    </view>

    <view class="note">支持 jpg/png/gif/webp，单张不超过 10MB</view>

    <button class="btn" :class="{ disabled: submitting }" @click="publish">{{ submitting ? '发布中…' : '发布笔记' }}</button>
    <text class="tip">笔记先发后审：发布即展示，敏感词与举报由审核处理</text>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api, uploadImage } from '../../api'

const submitting = ref(false)
const shops = ref([])
const shopIndex = ref(0)
const images = ref([])
const shopNames = computed(() => shops.value.map(s => s.name))

const form = reactive({
  type: 1,
  content: '',
  location: ''
})

onLoad(() => {
  loadShops()
})

async function loadShops() {
  try {
    shops.value = await api.nearby()
  } catch (e) { /* 已提示 */ }
}

function setType(t) {
  form.type = t
}

function chooseImage() {
  uni.chooseImage({
    count: 3 - images.value.length,
    success: async res => {
      for (const path of res.tempFilePaths) {
        try {
          const url = await uploadImage(path, 'blog')
          images.value.push(url)
        } catch (e) { /* 已提示 */ }
      }
    }
  })
}

function removeImage(i) {
  images.value.splice(i, 1)
}

async function publish() {
  if (submitting.value) return
  if (!form.content.trim()) {
    uni.showToast({ title: '请填写正文', icon: 'none' })
    return
  }
  if (form.type === 1 && !shops.value.length) {
    uni.showToast({ title: '探店笔记需要关联店铺', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await api.createBlog({
      type: form.type,
      shopId: form.type === 1 ? shops.value[shopIndex.value].id : null,
      content: form.content.trim(),
      location: form.location,
      images: images.value
    })
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 700)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding: 20rpx 0 40rpx; }
.chips { display: flex; padding: 0 24rpx 16rpx; }
.chip { font-size: 26rpx; color: #666; border: 1rpx solid #ddd; background: #fff; border-radius: 30rpx; padding: 12rpx 30rpx; margin-right: 16rpx; }
.chip.on { color: #ff6633; border-color: #ff6633; background: #fff4ee; }
.textarea { margin: 0 24rpx; width: auto; height: 300rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; font-size: 28rpx; box-sizing: border-box; }
.row { margin: 20rpx 24rpx 0; background: #fff; border-radius: 20rpx; padding: 24rpx; display: flex; align-items: center; justify-content: space-between; }
.label { font-size: 26rpx; color: #666; }
.picker { font-size: 26rpx; color: #ff6633; }
.input { flex: 1; margin-left: 20rpx; text-align: right; font-size: 26rpx; }
.note { margin: 20rpx 32rpx 0; color: #bbb; font-size: 22rpx; }
.uploader { display: flex; flex-wrap: wrap; padding: 0 24rpx; }
.img-box { position: relative; width: 200rpx; height: 200rpx; margin: 0 16rpx 16rpx 0; }
.img { width: 200rpx; height: 200rpx; border-radius: 12rpx; }
.del { position: absolute; top: -10rpx; right: -10rpx; width: 40rpx; height: 40rpx; border-radius: 50%; background: rgba(0,0,0,.6); color: #fff; font-size: 26rpx; display: flex; align-items: center; justify-content: center; }
.add { width: 200rpx; height: 200rpx; border: 1rpx dashed #ccc; border-radius: 12rpx; color: #999; font-size: 24rpx; display: flex; align-items: center; justify-content: center; }
.btn { margin: 40rpx 24rpx 0; background: #ff6633; color: #fff; border-radius: 44rpx; font-size: 30rpx; }
.btn.disabled { opacity: .6; }
.tip { display: block; text-align: center; color: #bbb; font-size: 22rpx; margin-top: 18rpx; }
</style>