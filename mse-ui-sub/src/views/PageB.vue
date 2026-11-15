<template>
  <el-card header="路由参数演示">
    <p class="desc">
      子应用内部路由会同步到主应用地址栏（wujie sync 模式）：当前主应用地址为
      <code>{{ $wujieUrlPrefix }}/page-b/{{ route.params.id }}</code>
    </p>
    <el-space>
      <span>路径参数 id：</span>
      <el-input-number v-model="id" :min="1" @change="go" />
    </el-space>
    <p class="desc" style="margin-top: 16px">
      query 参数 foo = <el-tag>{{ route.query.foo || '（未携带）' }}</el-tag>
      <el-button style="margin-left: 12px" @click="goWithQuery">携带 foo=bar 跳转</el-button>
    </p>
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const id = ref(Number(route.params.id) || 1)

const $wujieUrlPrefix = computed(() => window.$wujie?.props?.urlPrefix || '')

function go() {
  router.push(`/page-b/${id.value}`)
}

function goWithQuery() {
  router.push({ path: `/page-b/${id.value}`, query: { foo: 'bar' } })
}
</script>

<style scoped>
.desc {
  color: var(--el-text-color-regular);
}
</style>
