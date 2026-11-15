<template>
  <div class="sub-app">
    <div v-if="!authStore.embedded" class="status-bar">
      <span>子应用（独立运行）</span>
      <span style="margin-left: auto">{{ authStore.nickname || '未登录' }}</span>
    </div>
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
  // fullPath 为可选附加字段：基座 bare 宿主路由上报据此携带 query（页面状态写进 query 即可被聊天分享卡片带走）
  router.afterEach((to) => {
    const title = to.meta?.title as string | undefined
    const urlPrefix = window.$wujie?.props?.urlPrefix
    if (title && urlPrefix) window.$wujie?.bus.$emit('sub:title', { urlPrefix, path: to.path, fullPath: to.fullPath, title })
  })
})
</script>

<style scoped>
.sub-app {
  height: 100%;
  display: flex;
  flex-direction: column;
}
/* 状态条仅独立运行时展示；嵌入主应用时菜单/登录态由基座提供，不显示本条 */
.status-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-light);
}
.sub-body {
  flex: 1;
  overflow: auto;
}
</style>
