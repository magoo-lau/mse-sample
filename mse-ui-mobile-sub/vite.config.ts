import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // mse-ui-shared 以 file: 源码直出（.ts/.vue），排除出 dev 预构建交由插件链按源码处理
  optimizeDeps: {
    exclude: ['mse-ui-shared']
  },

  server: {
    // 移动子应用 dev 端口段 4210+（模板 3210，分配登记见 zcode-plugin alloc-tables.md）
    port: 3210,
    // wujie 主应用跨域加载子应用资源，开发模式必须放开 CORS
    headers: {
      'Access-Control-Allow-Origin': '*',
      // wujie fetch 子应用资源走 HTTP 缓存会拿到改码前的旧模块（vite dev 响应无 Cache-Control 时启发式缓存），开发期禁存
      'Cache-Control': 'no-store'
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
