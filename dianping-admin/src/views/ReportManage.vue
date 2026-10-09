<template>
  <el-card>
    <el-form inline>
      <el-form-item label="对象类型">
        <el-select v-model="query.targetType" clearable placeholder="全部" style="width:130px" @change="load">
          <el-option v-for="(name, val) in targetTypes" :key="val" :label="name" :value="Number(val)" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态">
        <el-select v-model="query.handled" clearable placeholder="全部" style="width:130px" @change="load">
          <el-option :value="0" label="待处理" />
          <el-option :value="1" label="已处理" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="举报对象" width="110">
        <template #default="{ row }">{{ targetTypes[row.targetType] || row.targetType }}</template>
      </el-table-column>
      <el-table-column prop="targetId" label="对象ID" width="100" />
      <el-table-column prop="reporterId" label="举报人" width="100" />
      <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
      <el-table-column label="处理结果" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.result" :type="resultTagType(row.result)">{{ results[row.result] }}</el-tag>
          <el-tag v-else type="warning">待处理</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="举报时间" width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <template v-if="!row.result">
            <el-button link type="info" @click="handle(row, 1)">驳回</el-button>
            <el-button link type="warning" @click="handle(row, 2)">删除内容</el-button>
            <el-button link type="primary" @click="handle(row, 3)">警告</el-button>
            <el-button link type="danger" @click="handle(row, 4)">封禁</el-button>
          </template>
          <span v-else style="color:#999;font-size:12px">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.size" v-model:current-page="query.current" @current-change="load" />
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reportList, reportHandle } from '../api'

const targetTypes = { 1: '用户', 2: '评价', 3: '商家', 4: '帖子' }
const results = { 1: '驳回', 2: '删除内容', 3: '警告', 4: '封禁' }

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, targetType: null, handled: null })

function resultTagType(r) {
  return { 1: 'info', 2: 'warning', 3: 'primary', 4: 'danger' }[r] || 'info'
}

async function load() {
  loading.value = true
  try {
    const data = await reportList(query)
    rows.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

async function handle(row, result) {
  await ElMessageBox.confirm(
    `确认对举报 #${row.id}（${targetTypes[row.targetType]}）执行「${results[result]}」？`,
    '举报处理', { type: 'warning' })
  await reportHandle(row.id, result)
  ElMessage.success('已处理')
  load()
}

onMounted(load)
</script>
