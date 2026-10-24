<template>
  <view class="page">
    <view class="stars">
      <text v-for="i in 5" :key="i" class="star" :class="{ on: i <= score }" @click="score = i">★</text>
    </view>

    <view class="card">
      <view class="dim-row" v-for="d in dims" :key="d.key">
        <text class="dim-label">{{ d.label }}</text>
        <view class="dim-stars">
          <text v-for="i in 5" :key="i" class="star small" :class="{ on: i <= d.value }" @click="d.value = i">★</text>
        </view>
      </view>
    </view>

    <textarea class="textarea" v-model="content" placeholder="说说你的体验吧～" maxlength="500" />

    <view class="tags">
      <view v-for="t in tagOptions" :key="t" class="tag" :class="{ on: tags.includes(t) }" @click="toggleTag(t)">{{ t }}</view>
    </view>

    <view class="uploader">
      <view class="img-box" v-for="(img, i) in images" :key="i">
        <image class="img" :src="img" mode="aspectFill" />
        <text class="del" @click="removeImage(i)">×</text>
      </view>
      <view v-if="images.length < 3" class="add" @click="chooseImage">+ 图片</view>
    </view>

    <view class="submit" :class="{ disabled: submitting }" @click="submit">{{ submitting ? '发布中…' : '发布评价' }}</view>
    <text class="note">评价关联订单，发布后先展示后审核</text>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api, uploadImage } from '../../api'

const orderId = ref('')
const shopId = ref('')
const score = ref(5)
const content = ref('')
const tags = ref([])
const images = ref([])
const submitting = ref(false)
const tagOptions = ['分量足', '味道好', '环境好', '服务好', '性价比高', '排队久']
const dims = reactive([
  { key: 'taste', label: '口味', value: 5 },
  { key: 'env', label: '环境', value: 5 },
  { key: 'service', label: '服务', value: 5 },
  { key: 'value', label: '性价比', value: 5 }
])

onLoad(options => {
  orderId.value = options.orderId
  shopId.value = options.shopId
})

function toggleTag(t) {
  const i = tags.value.indexOf(t)
  if (i >= 0) tags.value.splice(i, 1)
  else tags.value.push(t)
}

function chooseImage() {
  uni.chooseImage({
    count: 3 - images.value.length,
    success: async res => {
      for (const path of res.tempFilePaths) {
        try {
          const url = await uploadImage(path, 'review')
          images.value.push(url)
        } catch (e) { /* 已提示 */ }
      }
    }
  })
}

function removeImage(i) {
  images.value.splice(i, 1)
}

async function submit() {
  if (submitting.value) return
  if (!content.value.trim()) {
    uni.showToast({ title: '请填写评价内容', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await api.createReview({
      orderId: Number(orderId.value),
      shopId: Number(shopId.value),
      score: score.value * 10,
      tasteScore: dims[0].value * 10,
      envScore: dims[1].value * 10,
      serviceScore: dims[2].value * 10,
      valueScore: dims[3].value * 10,
      content: content.value,
      tags: tags.value,
      images: images.value,
      isAnonymous: 0
    })
    uni.showToast({ title: '评价已发布', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/order/list' }), 700)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding-bottom: 60rpx; }
.stars { display: flex; justify-content: center; padding: 50rpx 0 30rpx; }
.star { font-size: 64rpx; color: #e3e3e8; margin: 0 12rpx; }
.star.on { color: #ffb400; }
.star.small { font-size: 36rpx; margin: 0 6rpx; }
.card { margin: 0 24rpx; background: #fff; border-radius: 20rpx; padding: 10rpx 24rpx; }
.dim-row { display: flex; justify-content: space-between; align-items: center; padding: 18rpx 0; border-bottom: 1rpx solid #f2f2f5; }
.dim-row:last-child { border-bottom: none; }
.dim-label { font-size: 28rpx; color: #333; }
.dim-stars { display: flex; }
.textarea { margin: 20rpx 24rpx; width: auto; height: 240rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; font-size: 28rpx; box-sizing: border-box; }
.tags { display: flex; flex-wrap: wrap; padding: 0 24rpx; }
.tag { font-size: 24rpx; color: #666; border: 1rpx solid #ddd; border-radius: 24rpx; padding: 10rpx 24rpx; margin: 0 16rpx 16rpx 0; }
.tag.on { color: #fff; background: #ff6633; border-color: #ff6633; }
.uploader { display: flex; flex-wrap: wrap; padding: 0 24rpx; }
.img-box { position: relative; width: 160rpx; height: 160rpx; margin: 0 16rpx 16rpx 0; }
.img { width: 160rpx; height: 160rpx; border-radius: 12rpx; }
.del { position: absolute; top: -10rpx; right: -10rpx; width: 36rpx; height: 36rpx; border-radius: 50%; background: rgba(0,0,0,.6); color: #fff; font-size: 24rpx; display: flex; align-items: center; justify-content: center; }
.add { width: 160rpx; height: 160rpx; border: 1rpx dashed #ccc; border-radius: 12rpx; color: #999; font-size: 24rpx; display: flex; align-items: center; justify-content: center; }
.submit { margin: 30rpx 24rpx 0; background: #ff6633; color: #fff; text-align: center; padding: 24rpx 0; border-radius: 44rpx; font-size: 30rpx; }
.submit.disabled { opacity: .6; }
.note { display: block; text-align: center; color: #bbb; font-size: 22rpx; margin-top: 20rpx; }
</style>