import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // mse-ui-shared 以 file: 源码直出（.ts/.vue），排除出 dev 预构建交由插件链按源码处理
  optimizeDeps: {
    exclude: ['mse-ui-shared']
  },

  server: {
    port: 3100,
    // wujie 主应用跨域加载子应用资源，开发模式必须放开 CORS
    headers: {
      'Access-Control-Allow-Origin': '*'
    },
    proxy: {
      // 开发代理：单体模式指向 8080；微服务模式改为 48080（网关）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
