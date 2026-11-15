/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, any>
  export default component
}

// MainAppProps / Window.$wujie 契约类型由 mse-ui-shared 全局提供（工程内 import 该包即生效）
