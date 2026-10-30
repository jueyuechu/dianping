// 商户端统一请求封装（后端 Result<T>，code=200 成功）
const BASE_URL = 'http://localhost:8080/api'

export function getToken() {
  return uni.getStorageSync('merchant_token') || ''
}

/** 登出：通知后端从 Redis 白名单移除 token（静默失败，不阻塞本地登出） */
export function logoutRequest() {
  const token = getToken()
  if (!token) return
  uni.request({
    url: `${BASE_URL}/merchant/auth/logout`,
    method: 'POST',
    header: { Authorization: `Bearer ${token}` },
    complete: () => {}
  })
}

export function request(path, options = {}) {
  const { method = 'GET', data } = options
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.request({
      url: BASE_URL + path,
      method,
      data,
      header: {
        ...(method !== 'GET' ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: res => {
        const body = res.data
        if (body && body.code === 200) {
          resolve(body.data)
          return
        }
        if (res.statusCode === 401 || (body && body.code === 401)) {
          uni.removeStorageSync('merchant_token')
          uni.removeStorageSync('merchant_info')
        }
        uni.showToast({ title: (body && body.msg) || '请求失败', icon: 'none' })
        reject(body || res)
      },
      fail: err => {
        uni.showToast({ title: '网络异常，请检查后端是否启动', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const api = {
  // 认证
  login: data => request('/merchant/auth/login', { method: 'POST', data }),
  register: data => request('/merchant/auth/register', { method: 'POST', data }),
  applyStatus: () => request('/merchant/auth/apply-status'),
  // 店铺
  shops: () => request('/merchant/shop'),
  createShop: data => request('/merchant/shop', { method: 'POST', data }),
  updateShop: (id, data) => request(`/merchant/shop/${id}`, { method: 'PUT', data }),
  updateHours: (id, params) => request(`/merchant/shop/${id}/hours?${params}`),
  // 团购
  groupbuys: shopId => request(`/merchant/groupbuy/list?shopId=${shopId}`),
  groupbuyDetail: id => request(`/merchant/groupbuy/${id}`),
  createGroupbuy: data => request('/merchant/groupbuy', { method: 'POST', data }),
  updateGroupbuy: (id, data) => request(`/merchant/groupbuy/${id}`, { method: 'PUT', data }),
  setGroupbuyStatus: (id, status) => request(`/merchant/groupbuy/${id}/status?status=${status}`, { method: 'PUT' }),
  // 订单与核销
  orders: params => request(`/merchant/order/list?${params}`),
  verify: data => request('/merchant/order/verify', { method: 'POST', data }),
  verifyRecords: params => request(`/merchant/order/verify-records?${params}`),
  // 评价
  reviews: params => request(`/merchant/review/list?${params}`),
  replyReview: (id, content) => request(`/merchant/review/${id}/reply`, { method: 'POST', data: { content } }),
  reviewReplies: id => request(`/merchant/review/${id}/replies`),
  // 消息中心
  messages: (current = 1, size = 20, isRead) =>
    request(`/merchant/message/list?current=${current}&size=${size}${isRead !== undefined && isRead !== null ? `&isRead=${isRead}` : ''}`),
  unreadMessages: () => request('/merchant/message/unread-count'),
  readMessage: id => request(`/merchant/message/${id}/read`, { method: 'PUT' }),
  readAllMessages: () => request('/merchant/message/read-all', { method: 'PUT' })
}