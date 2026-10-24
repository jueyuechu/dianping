<template>
  <view class="page">
    <view class="card">
      <text class="card-title">门店信息</text>
      <input class="input" v-model="form.name" placeholder="店铺名称" />
      <input class="input" v-model="form.area" placeholder="商圈（如：大关）" />
      <input class="input" v-model="form.address" placeholder="详细地址" />
      <view class="row">
        <text class="label">人均（元）</text>
        <input class="input num" v-model="form.avgPrice" type="number" placeholder="0" />
      </view>
      <view class="row">
        <text class="label">营业时间</text>
        <input class="input num" v-model="form.openHours" placeholder="10:00-22:00" />
      </view>
      <input class="input" v-model="form.phone" placeholder="联系电话" />
    </view>

    <view class="card">
      <text class="card-title">地图坐标</text>
      <view class="row">
        <text class="label">经度 x</text>
        <input class="input num" v-model="form.x" type="digit" placeholder="120.15" />
      </view>
      <view class="row">
        <text class="label">纬度 y</text>
        <input class="input num" v-model="form.y" type="digit" placeholder="30.31" />
      </view>
      <text class="hint">Phase 1 将接入地图选点（高德地图 Web 服务）</text>
    </view>

    <button class="btn" :class="{ disabled: saving }" @click="save">{{ saving ? '提交中…' : (shopId ? '保存并提交审核' : '创建门店') }}</button>
    <text class="note">位置、照片等关键信息修改需平台审核后生效</text>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api'

const shopId = ref(null)
const saving = ref(false)
const form = reactive({
  name: '',
  area: '',
  address: '',
  avgPrice: '',
  openHours: '10:00-22:00',
  phone: '',
  x: '',
  y: '',
  typeId: 2
})

onLoad(async () => {
  try {
    const shops = await api.shops()
    if (shops.length) {
      const s = shops[0]
      shopId.value = s.id
      Object.assign(form, {
        name: s.name,
        area: s.area,
        address: s.address,
        avgPrice: s.avgPrice,
        openHours: s.openHours,
        phone: s.phone,
        x: s.x,
        y: s.y,
        typeId: s.typeId
      })
      uni.setNavigationBarTitle({ title: '店铺信息' })
    }
  } catch (e) { /* 已提示 */ }
})

async function save() {
  if (saving.value) return
  if (!form.name || !form.address) {
    uni.showToast({ title: '请填写店铺名称与地址', icon: 'none' })
    return
  }
  saving.value = true
  const payload = {
    name: form.name,
    area: form.area,
    address: form.address,
    avgPrice: Number(form.avgPrice || 0),
    openHours: form.openHours,
    phone: form.phone,
    x: form.x ? Number(form.x) : null,
    y: form.y ? Number(form.y) : null,
    typeId: form.typeId
  }
  try {
    if (shopId.value) {
      await api.updateShop(shopId.value, payload)
    } else {
      await api.createShop(payload)
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
.row { display: flex; align-items: center; justify-content: space-between; padding: 6rpx 0; }
.label { font-size: 26rpx; color: #666; }
.row .input.num { width: 320rpx; margin-bottom: 0; text-align: right; }
.hint { display: block; color: #bbb; font-size: 20rpx; margin-top: 12rpx; }
.btn { margin: 20rpx 24rpx 0; background: #00b578; color: #fff; border-radius: 44rpx; font-size: 30rpx; }
.btn.disabled { opacity: .6; }
.note { display: block; text-align: center; color: #bbb; font-size: 22rpx; margin-top: 18rpx; }
</style>