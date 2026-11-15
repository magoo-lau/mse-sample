<template>
  <div class="home">
    <van-cell-group inset class="home-cells">
      <van-cell title="当前用户" :value="authStore.nickname || '（未登录）'" />
      <van-cell title="租户编号" :value="String(authStore.tenantId || '-')" />
      <van-cell title="权限码数量" :value="authStore.permissions.includes('*') ? '超管（*）' : String(authStore.permissions.length)" />
    </van-cell-group>
    <div class="home-actions">
      <van-button type="primary" block round to="/page-a">进入页面 A</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
</script>

<style scoped>
.home {
  padding: 12px 0;
}
.home-cells {
  margin-top: 12px;
}
.home-actions {
  padding: 20px 16px;
}
</style>
