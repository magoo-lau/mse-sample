<template>
  <div class="sub-app">
    <div class="sub-body">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { initThemeBridge } from 'mse-ui-shared/pc'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  // 订阅主应用登录态变化（独立运行时为空操作）
  authStore.listenMainApp()
  // 跟随基座外观（独立运行回退本地偏好）
  initThemeBridge()
  // 接收基座菜单下发的导航（wujie sync 只做子→主单向同步，主→子需事件下发）
  window.$wujie?.bus.$on('sub:navigate', (msg: { urlPrefix: string; path: string }) => {
    if (window.$wujie?.props?.urlPrefix === msg.urlPrefix) router.push(msg.path)
  })
  // 上报就绪，基座据此下发当前目标路由（直达/刷新场景子应用从默认路由启动）
  window.$wujie?.bus.$emit('sub:ready', { urlPrefix: window.$wujie?.props?.urlPrefix })
  // 上报当前页中文标题：基座用于页签与面包屑展示（深链/详情页无页面菜单可归并，独立运行无基座不上报）
  router.afterEach((to) => {
    const title = to.meta?.title as string | undefined
    const urlPrefix = window.$wujie?.props?.urlPrefix
    if (title && urlPrefix) window.$wujie?.bus.$emit('sub:title', { urlPrefix, path: to.path, title })
  })
})
</script>

<style scoped>
.sub-app {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.sub-body {
  flex: 1;
  overflow: auto;
}
</style>
