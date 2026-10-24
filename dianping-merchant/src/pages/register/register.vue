<template>
  <view class="page">
    <view class="card">
      <text class="card-title">资质信息</text>
      <input class="input" v-model="form.name" placeholder="商户名称" />
      <input class="input" v-model="form.licenseNo" placeholder="营业执照号" />
      <input class="input" v-model="form.legalPerson" placeholder="法定代表人" />
      <input class="input" v-model="form.contactPhone" placeholder="联系电话" />
    </view>

    <view class="card">
      <text class="card-title">登录账号</text>
      <input class="input" v-model="form.username" placeholder="登录账号" />
      <input class="input" v-model="form.password" password placeholder="登录密码" />
    </view>

    <button class="btn" :class="{ disabled: loading }" @click="submit">{{ loading ? '提交中…' : '提交入驻申请' }}</button>
    <text class="note">提交后进入平台审核，审核结果可在「入驻进度」查看</text>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { api } from '../../api'

const loading = ref(false)
const form = reactive({
  name: '',
  licenseNo: '',
  legalPerson: '',
  contactPhone: '',
  username: '',
  password: ''
})

async function submit() {
  if (loading.value) return
  for (const key of Object.keys(form)) {
    if (!form[key]) {
      uni.showToast({ title: '请填写完整资料', icon: 'none' })
      return
    }
  }
  loading.value = true
  try {
    await api.register(form)
    uni.showToast({ title: '提交成功，等待审核', icon: 'none' })
    setTimeout(() => uni.redirectTo({ url: '/pages/login/login' }), 900)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6f8; padding: 20rpx 0 40rpx; }
.card { margin: 0 24rpx 20rpx; background: #fff; border-radius: 20rpx; padding: 20rpx 24rpx; }
.card-title { font-size: 28rpx; font-weight: 600; display: block; padding: 10rpx 0 16rpx; }
.input { height: 84rpx; background: #f5f6f8; border-radius: 14rpx; padding: 0 24rpx; font-size: 26rpx; margin-bottom: 18rpx; }
.btn { margin: 40rpx 24rpx 0; background: #00b578; color: #fff; border-radius: 44rpx; font-size: 30rpx; }
.btn.disabled { opacity: .6; }
.note { display: block; text-align: center; color: #bbb; font-size: 22rpx; margin-top: 20rpx; }
</style>