import { createRouter, createWebHistory } from 'vue-router'

// 嵌入主应用时以主应用路由前缀为 base（由主应用 props 下发），独立运行时使用根路径
const embedded = !!window.__POWERED_BY_WUJIE__
const base = embedded ? window.$wujie?.props?.urlPrefix || '/sub/demo' : '/'

const router = createRouter({
  history: createWebHistory(base),
  routes: [
    { path: '/', redirect: '/order' },
    { path: '/order', component: () => import('../views/Order.vue'), meta: { title: '订单演示' } }
  ]
})

export default router
