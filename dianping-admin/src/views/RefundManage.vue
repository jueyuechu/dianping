<template>
  <el-card>
    <div class="head">
      <div class="left">
        <span class="title">退款审核</span>
        <el-select v-model="status" placeholder="全部状态" clearable style="width:150px;margin-left:16px" @change="load">
          <el-option label="待审核" :value="1" />
          <el-option label="审核中" :value="2" />
          <el-option label="已退款" :value="4" />
          <el-option label="已驳回" :value="5" />
        </el-select>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderNo" label="订单号" min-width="200" />
      <el-table-column prop="amount" label="退款金额" width="110">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status <= 2">
            <el-button size="small" type="success" link @click="approve(row)">通过</el-button>
            <el-button size="small" type="danger" link @click="reject(row)">驳回</el-button>
          </template>
          <span v-else class="done">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" layout="total, prev, pager, next" :total="total"
                   :page-size="size" :current-page="current"
                   @current-change="onPage" />
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { refundApprove, refundList, refundReject } from '../api'

const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const status = ref(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await refundList({ current: current.value, size: size.value, status: status.value || undefined })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onPage(p) {
  current.value = p
  load()
}

async function approve(row) {
  await ElMessageBox.confirm(`确认通过订单 ${row.orderNo} 的退款 ¥${row.amount}？`, '退款审核')
  await refundApprove(row.id)
  ElMessage.success('已退款')
  load()
}

async function reject(row) {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回退款', {
    inputPlaceholder: '例如：订单已核销，不符合退款条件'
  })
  await refundReject(row.id, value || '退款申请被驳回')
  ElMessage.success('已驳回')
  load()
}

function statusText(s) {
  return { 1: '待审核', 2: '审核中', 3: '退款中', 4: '已退款', 5: '已驳回' }[s] || '未知'
}

function statusType(s) {
  return { 1: 'warning', 2: 'warning', 3: 'primary', 4: 'success', 5: 'info' }[s] || 'info'
}

onMounted(load)
</script>

<style scoped>
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.title { font-size: 16px; font-weight: 600; }
.left { display: flex; align-items: center; }
.pager { margin-top: 16px; justify-content: flex-end; }
.done { color: #bbb; font-size: 12px; }
</style>
