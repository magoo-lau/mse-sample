import { createRouter, createWebHistory } from 'vue-router'

// 嵌入主应用时以主应用路由前缀为 base（由主应用 props 下发），独立运行时使用根路径
const embedded = !!window.__POWERED_BY_WUJIE__
const base = embedded ? window.$wujie?.props?.urlPrefix || '/sub/biz-demo' : '/'

const router = createRouter({
  history: createWebHistory(base),
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/home', component: () => import('../views/Home.vue'), meta: { title: '示例首页' } },
    { path: '/page-a', component: () => import('../views/PageA.vue'), meta: { title: '页面 A' } },
    { path: '/page-b/:id', component: () => import('../views/PageB.vue'), meta: { title: '页面 B' } }
  ]
})

export default router
