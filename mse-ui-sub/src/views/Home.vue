<template>
  <el-card header="子应用接入说明">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="当前模式">
        <el-tag :type="authStore.embedded ? 'success' : 'info'">
          {{ authStore.embedded ? '嵌入主应用（wujie）' : '独立运行' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="路由前缀（urlPrefix）">{{ urlPrefix }}</el-descriptions-item>
      <el-descriptions-item label="登录用户">{{ authStore.nickname || '-' }}</el-descriptions-item>
      <el-descriptions-item label="租户 / 平台">
        {{ authStore.platform ? '平台端' : `租户 ${authStore.tenantId || '-'}` }}
      </el-descriptions-item>
      <el-descriptions-item label="token">
        {{ authStore.token ? authStore.token.slice(0, 12) + '...' : '（无，独立运行未登录）' }}
      </el-descriptions-item>
    </el-descriptions>
    <el-alert class="tips" type="info" :closable="false"
      title="本工程是微前端子应用模板：复制 mse-ui-sub 后按 README 修改 name / 端口 / prodEntry 即可接入主应用" />
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const urlPrefix = computed(() => window.$wujie?.props?.urlPrefix || '/')
</script>

<style scoped>
.tips {
  margin-top: 16px;
}
</style>
