import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni(),
  ],
  // H5 预览端口（管理员端占用 5173）
  server: {
    port: 5174
  }
})
