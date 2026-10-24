export function isLogin() {
  return !!uni.getStorageSync('merchant_token')
}

export function getMerchant() {
  return uni.getStorageSync('merchant_info') || null
}

export function saveLogin(data) {
  uni.setStorageSync('merchant_token', data.token)
  uni.setStorageSync('merchant_info', {
    id: data.id,
    username: data.username,
    merchantId: data.merchantId,
    role: data.role,
    shopName: data.shopName,
    merchantStatus: data.merchantStatus
  })
}

export function logout() {
  uni.removeStorageSync('merchant_token')
  uni.removeStorageSync('merchant_info')
}

/** 未登录跳转登录页 */
export function requireLogin() {
  if (isLogin()) return true
  uni.reLaunch({ url: '/pages/login/login' })
  return false
}

export const AUDIT_STATUS = {
  null: '未提交',
  0: '草稿',
  1: '待审核',
  2: '审核中',
  3: '审核通过',
  4: '已驳回',
  5: '已上架',
  6: '已下架',
  7: '已封禁'
}

export const GROUPBUY_STATUS = {
  0: '已下架',
  1: '上架中',
  2: '审核中'
}