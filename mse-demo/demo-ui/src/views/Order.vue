<template>
  <el-card>
    <el-alert type="info" :closable="false" style="margin-bottom: 12px"
              title="演示中心 = 独立工程 mse-demo（demo-boot 后端 + demo-ui 前端子应用）：验证混合租户路由、RocketMQ 租户事件与 Seata 跨库事务" />
    <div style="margin-bottom: 12px; display: flex; gap: 8px">
      <el-button v-permission="'demo:order:create'" type="success" @click="place(false)">下一单（正常）</el-button>
      <el-button v-permission="'demo:order:create'" type="danger" @click="place(true)">下一单（模拟失败回滚）</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="rows" border>
      <el-table-column prop="id" label="ID" width="190" />
      <el-table-column prop="orderNo" label="订单号" width="220" />
      <el-table-column prop="productName" label="商品" />
      <el-table-column prop="amount" label="金额" width="100" />
      <el-table-column prop="createTime" label="时间" width="180" />
    </el-table>
    <Pagination :total="total" v-model:page="query.pageNum" v-model:size="query.pageSize" @change="load" />
    <el-alert v-if="lastEvent" type="success" :closable="false" style="margin-top: 12px"
              :title="`最新 MQ 消费事件: ${lastEvent.eventType}（租户 ${lastEvent.tenantId}，异步链路租户恢复正确）`" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { Pagination } from 'mse-ui-shared/pc'

const rows = ref<any[]>([])
const lastEvent = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 20 })
const total = ref(0)

async function load() {
  const data = await request.get('/demo/order/page', { params: query })
  rows.value = data.records
  total.value = Number(data.total)
  try {
    const events = await request.get('/demo/order/events')
    lastEvent.value = events[0]
  } catch { /* 忽略 */ }
}

async function place(fail: boolean) {
  try {
    await request.post('/demo/order/place', { productName: `页面下单-${Date.now() % 10000}`, amount: 10, fail })
    ElMessage.success('下单成功')
  } catch (e: any) {
    if (e?.code === 1301) ElMessage.warning('业务失败：事务已回滚（Seata 全局回滚演示）')
  }
  load()
}

onMounted(load)
</script>
