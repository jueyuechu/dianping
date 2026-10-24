export function isLogin() {
  return !!uni.getStorageSync('token')
}

export function getUser() {
  return uni.getStorageSync('userInfo') || null
}

export function saveLogin(data) {
  uni.setStorageSync('token', data.token)
  uni.setStorageSync('userInfo', { id: data.id, nickName: data.nickName, icon: data.icon })
}

export function logout() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
}

/**
 * 需要登录的操作：未登录时跳转登录页并返回 false
 */
export function requireLogin() {
  if (isLogin()) return true
  uni.navigateTo({ url: '/pages/login/login' })
  return false
}

/**
 * 状态文案
 */
export const ORDER_STATUS = {
  1: '待支付',
  2: '待使用',
  3: '已使用',
  4: '已退款',
  5: '已过期'
}