import { createRouter, createWebHistory } from 'vue-router'

// 嵌入移动基站时以基站路由前缀为 base（由基站 props 下发，/sub/{name} 契约与 PC 基座一致），独立运行时使用根路径
const embedded = !!window.__POWERED_BY_WUJIE__
const base = embedded ? window.$wujie?.props?.urlPrefix || '/sub/biz-demo' : '/'

const router = createRouter({
  history: createWebHistory(base),
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/home', component: () => import('../views/Home.vue'), meta: { title: '示例首页' } },
    { path: '/page-a', component: () => import('../views/PageA.vue'), meta: { title: '页面 A' } }
  ]
})

export default router
