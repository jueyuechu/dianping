import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  // 商户端 H5 预览端口（用户端占用 5174、管理端 5173）
  server: {
    port: 5175
  }
})