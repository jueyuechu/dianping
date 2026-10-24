<template>
  <view class="page">
    <view class="card">
      <text class="card-title">基本信息</text>
      <input class="input" v-model="form.name" placeholder="团购名称" />
      <view class="row">
        <text class="label">原价</text>
        <input class="input num" v-model="form.originalPrice" type="digit" placeholder="0.00" />
      </view>
      <view class="row">
        <text class="label">团购价</text>
        <input class="input num" v-model="form.groupbuyPrice" type="digit" placeholder="0.00" />
      </view>
      <view class="row">
        <text class="label">库存</text>
        <input class="input num" v-model="form.stock" type="number" placeholder="0" />
      </view>
      <textarea class="textarea" v-model="form.content" placeholder="套餐内容明细" maxlength="500" />
    </view>

    <view class="card">
      <text class="card-title">使用规则</text>
      <view class="row">
        <text class="label">类型</text>
        <picker :range="typeNames" :value="typeIndex" @change="e => typeIndex = Number(e.detail.value)">
          <text class="picker">{{ typeNames[typeIndex] }} ▾</text>
        </picker>
      </view>
      <view class="row">
        <text class="label">节假日可用</text>
        <switch :checked="form.holidayUsable === 1" color="#00b578" @change="e => form.holidayUsable = e.detail.value ? 1 : 0" />
      </view>
      <view class="row">
        <text class="label">每人限购</text>
        <input class="input num" v-model="form.perUserLimit" type="number" placeholder="0 表示不限" />
      </view>
      <view class="row">
        <text class="label">每桌限用</text>
        <input class="input num" v-model="form.perTableLimit" type="number" placeholder="1" />
      </view>
      <view class="row">
        <text class="label">退款规则</text>
        <picker :range="refundNames" :value="refundIndex" @change="e => refundIndex = Number(e.detail.value)">
          <text class="picker">{{ refundNames[refundIndex] }} ▾</text>
        </picker>
      </view>
    </view>

    <button class="btn" :class="{ disabled: saving }" @click="save">{{ saving ? '提交中…' : '提交审核' }}</button>
    <text class="note">团购先审后发，提交后由平台审核通过才可售卖</text>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api'

const groupbuyId = ref(null)
const shopId = ref(null)
const saving = ref(false)
const typeNames = ['套餐', '代金券', '折扣券', '次卡', '多人餐']
const refundNames = ['随时退', '过期退', '不可退']
const typeIndex = ref(0)
const refundIndex = ref(0)

const form = reactive({
  name: '',
  originalPrice: '',
  groupbuyPrice: '',
  stock: 100,
  content: '',
  holidayUsable: 1,
  perUserLimit: 0,
  perTableLimit: 1,
  verifyType: 3
})

const finish = ref(false)

onLoad(async options => {
  groupbuyId.value = options.id || null
  shopId.value = options.shopId || null
  if (groupbuyId.value) {
    try {
      const data = await api.groupbuyDetail(groupbuyId.value)
      shopId.value = data.groupbuy.shopId
      Object.assign(form, {
        name: data.groupbuy.name,
        originalPrice: data.groupbuy.originalPrice,
        groupbuyPrice: data.groupbuy.groupbuyPrice,
        stock: data.groupbuy.stock,
        content: data.groupbuy.content,
        verifyType: data.groupbuy.verifyType
      })
      typeIndex.value = Math.max(0, (data.groupbuy.type || 1) - 1)
      if (data.rule) {
        form.holidayUsable = data.rule.holidayUsable
        form.perUserLimit = data.rule.perUserLimit
        form.perTableLimit = data.rule.perTableLimit
        refundIndex.value = Math.max(0, (data.rule.refundRule || 1) - 1)
      }
      uni.setNavigationBarTitle({ title: '编辑团购' })
    } catch (e) { /* 已提示 */ }
  } else if (!shopId.value) {
    try {
      const shops = await api.shops()
      if (shops.length) shopId.value = shops[0].id
      else {
        uni.showToast({ title: '请先创建门店', icon: 'none' })
        setTimeout(() => uni.redirectTo({ url: '/pages/shop/edit' }), 800)
      }
    } catch (e) { /* 已提示 */ }
  }
})

async function save() {
  if (saving.value) return
  if (!form.name || !form.groupbuyPrice || !form.originalPrice) {
    uni.showToast({ title: '请填写名称与价格', icon: 'none' })
    return
  }
  if (Number(form.groupbuyPrice) > Number(form.originalPrice)) {
    uni.showToast({ title: '团购价不能高于原价', icon: 'none' })
    return
  }
  saving.value = true
  const payload = {
    shopId: Number(shopId.value),
    name: form.name,
    originalPrice: Number(form.originalPrice),
    groupbuyPrice: Number(form.groupbuyPrice),
    stock: Number(form.stock || 0),
    content: form.content,
    type: typeIndex.value + 1,
    verifyType: form.verifyType,
    holidayUsable: form.holidayUsable,
    perUserLimit: Number(form.perUserLimit || 0),
    perTableLimit: Number(form.perTableLimit || 1),
    refundRule: refundIndex.value + 1
  }
  try {
    if (groupbuyId.value) {
      await api.updateGroupbuy(groupbuyId.value, payload)
    } else {
      await api.createGroupbuy(payload)
    }
    uni.showToast({ title: '已提交审核', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 700)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding: 20rpx 0 40rpx; }
.card { margin: 0 24rpx 20rpx; background: #fff; border-radius: 20rpx; padding: 20rpx 24rpx; }
.card-title { font-size: 28rpx; font-weight: 600; display: block; padding: 8rpx 0 16rpx; }
.input { height: 80rpx; background: #f5f6f8; border-radius: 14rpx; padding: 0 22rpx; font-size: 26rpx; margin-bottom: 16rpx; }
.row { display: flex; align-items: center; justify-content: space-between; padding: 8rpx 0; }
.label { font-size: 26rpx; color: #666; }
.row .input.num { width: 300rpx; margin-bottom: 0; text-align: right; }
.picker { font-size: 26rpx; color: #333; padding: 10rpx 0; }
.textarea { width: auto; height: 180rpx; background: #f5f6f8; border-radius: 14rpx; padding: 20rpx; font-size: 26rpx; }
.btn { margin: 30rpx 24rpx 0; background: #00b578; color: #fff; border-radius: 44rpx; font-size: 30rpx; }
.btn.disabled { opacity: .6; }
.note { display: block; text-align: center; color: #bbb; font-size: 22rpx; margin-top: 18rpx; }
</style>