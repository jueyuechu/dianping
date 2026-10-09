import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 管理员端开发配置：/api 代理到后端 8080
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})