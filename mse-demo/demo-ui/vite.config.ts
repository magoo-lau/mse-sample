import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // mse-ui-shared 以 file: 源码直出（.ts/.vue），排除出 dev 预构建交由插件链按源码处理
  optimizeDeps: {
    exclude: ['mse-ui-shared']
  },

  server: {
    port: 3300,
    // wujie 主应用跨域加载子应用资源，开发模式必须放开 CORS
    headers: {
      'Access-Control-Allow-Origin': '*'
    },
    proxy: {
      // 开发代理：指向网关 48080（demo-boot 为微服务，经网关 lb://mse-demo 路由）
      '/api': {
        // target: 'http://localhost:48080',
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
