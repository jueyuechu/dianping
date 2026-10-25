import { defineStore } from 'pinia'
import { adminLogin, adminLogout } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: JSON.parse(localStorage.getItem('admin_info') || 'null')
  }),
  getters: {
    isLogin: state => !!state.token
  },
  actions: {
    async login(form) {
      const data = await adminLogin(form)
      this.token = data.token
      this.userInfo = { id: data.id, username: data.username, realName: data.realName }
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_info', JSON.stringify(this.userInfo))
    },
    async logout() {
      // 通知后端从 Redis 白名单移除 token（失败不阻断本地登出）
      try {
        await adminLogout()
      } catch (e) {
        // ignore
      }
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
    }
  }
})