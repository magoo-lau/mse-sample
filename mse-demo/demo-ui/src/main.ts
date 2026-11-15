import { createApp } from 'vue'
import type { App as VueApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 暗色变量基线（html.dark 作用域），随基座 theme:change 切换
import 'element-plus/theme-chalk/dark/css-vars.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { permission } from './directives/permission'
import { elementPlusUseOptions, fixPopperContainer } from 'mse-ui-shared/pc'

let app: VueApp | null = null

function render() {
  app = createApp(App)
  app.use(createPinia())
  app.use(router)
  app.use(ElementPlus, elementPlusUseOptions) // 紧凑型 UI 风格（约定见 AGENTS.md）
  // 按钮级权限：数据源为主应用下发的权限码集合（stores/auth.ts）
  app.directive('permission', permission)
  fixPopperContainer()
  app.mount('#app')
}

function destroy() {
  app?.unmount()
  app = null
}

if (window.__POWERED_BY_WUJIE__) {
  // 嵌入主应用：按 wujie 官方契约暴露 __WUJIE_MOUNT/__WUJIE_UNMOUNT。alive 保活下 mount 仅首次执行，
  // 切走时基座 keep-alive 仅把容器移出 DOM，不会触发卸载，子应用状态得以保留。
  // 注意不可写成 window.__WUJIE.mount/unmount——iframeWindow.__WUJIE 就是沙箱实例，覆盖其内部
  // 方法后，容器每次移出 DOM 都会被 disconnectedCallback 当成卸载调用，切回即白屏
  window.__WUJIE_MOUNT = render
  window.__WUJIE_UNMOUNT = destroy
} else {
  // 独立运行：直接挂载，方便单独开发调试
  render()
}
