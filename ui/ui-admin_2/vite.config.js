import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5174, //独立端口，可与 ui-admin 同时运行
    proxy: {
      '/api': { //请求路径中包含了/api
        target: process.env.API_TARGET || 'http://localhost:8081', //默认后台服务地址，可用环境变量 API_TARGET 覆盖
        changeOrigin: true, //要不要更换源
        rewrite: (path) => path.replace(/^\/api/, '') //路径重写/api替换为’’
      }
    }
  },
  build: {
    //页面已按路由分包；Element Plus 全家桶较大，提高告警阈值避免噪音
    chunkSizeWarningLimit: 1600
  }
})
