<template>
  <div class="page-a">
    <van-nav-bar title="页面 A" left-arrow @click-left="router.back()" />
    <van-empty image="search" description="把业务页面写在这里（Vant 组件 + v-permission 按钮权限）" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>
