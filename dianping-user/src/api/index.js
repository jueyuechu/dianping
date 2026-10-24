// 统一请求封装：对齐后端 Result<T>（code=200 成功）
// H5 / 小程序均直连后端；后端已开启 CORS
const BASE_URL = 'http://localhost:8080/api'

export function getToken() {
  return uni.getStorageSync('token') || ''
}

/**
 * 上传图片/视频（评价配图、笔记配图）
 * @returns Promise<string> 可访问 URL
 */
export function uploadImage(filePath, dir = 'common') {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.uploadFile({
      url: `${BASE_URL}/user/upload?dir=${dir}`,
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: res => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) {
            resolve(body.data.url)
            return
          }
          uni.showToast({ title: body.msg || '上传失败', icon: 'none' })
          reject(body)
        } catch (e) {
          reject(e)
        }
      },
      fail: err => {
        uni.showToast({ title: '图片上传失败', icon: 'none' })
        reject(err)
      }
    })
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
        // GET 不带 JSON Content-Type，避免触发不必要的 CORS 预检
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
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
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
  // 店铺
  nearby: () => request('/user/shop/nearby'),
  shopDetail: id => request(`/user/shop/${id}`),
  // 团购
  groupbuyDetail: id => request(`/user/groupbuy/${id}`),
  // 订单
  createOrder: data => request('/user/groupbuy/order', { method: 'POST', data }),
  pay: orderId => request('/user/order/pay', { method: 'POST', data: { orderId } }),
  orders: status => request('/user/order/list' + (status ? `?status=${status}` : '')),
  orderDetail: id => request(`/user/order/${id}`),
  verifyCode: id => request(`/user/order/${id}/verify-code`),
  // 评价
  createReview: data => request('/user/review', { method: 'POST', data }),
  myReviews: () => request('/user/review/my'),
  // 登录
  login: code => request('/user/auth/login', { method: 'POST', data: { code, nickName: '微信用户' } }),
  // 社区
  blogFeed: (type = 'recommend', current = 1, size = 10, keyword = '') =>
    request(`/user/blog/feed?type=${type}&current=${current}&size=${size}${keyword ? `&keyword=${encodeURIComponent(keyword)}` : ''}`),
  blogDetail: id => request(`/user/blog/${id}`),
  blogComments: (id, current = 1, size = 20) =>
    request(`/user/blog/${id}/comments?current=${current}&size=${size}`),
  createBlog: data => request('/user/blog', { method: 'POST', data }),
  likeBlog: id => request(`/user/blog/${id}/like`, { method: 'POST' }),
  commentBlog: (id, data) => request(`/user/blog/${id}/comment`, { method: 'POST', data }),
  reportBlog: (id, reason) =>
    request(`/user/blog/${id}/report?reason=${encodeURIComponent(reason)}`, { method: 'POST' })
}