<template>
  <el-card header="接口调用演示">
    <p class="desc">
      子应用通过自己的 axios 封装（src/api/request.ts）调用后端，token 取自主应用下发；
      401 时会通过 bus 通知主应用统一登出。下方调用基座自带的个人资料接口（需后端已启动）：
    </p>
    <el-button type="primary" :loading="loading" @click="load">GET /system/profile/me</el-button>
    <pre v-if="result" class="result">{{ result }}</pre>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '../api/request'

const loading = ref(false)
const result = ref('')

async function load() {
  loading.value = true
  try {
    result.value = JSON.stringify(await request.get('/system/profile/me'), null, 2)
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.desc {
  color: var(--el-text-color-regular);
  margin: 0 0 12px;
}
.result {
  margin-top: 16px;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  max-height: 400px;
  overflow: auto;
}
</style>
