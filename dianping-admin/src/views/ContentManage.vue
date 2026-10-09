<template>
  <el-card>
    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="菜品管理" name="dish" />
      <el-tab-pane label="团购管理" name="groupbuy" />
      <el-tab-pane label="评价管理" name="review" />
    </el-tabs>

    <!-- 菜品 -->
    <template v-if="tab === 'dish'">
      <el-table :data="dishes" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="菜品名" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="sold" label="销量" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ dishStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="warning" @click="setDishStatus(row, 0)">下架</el-button>
            <el-button v-else link type="success" @click="setDishStatus(row, 1)">上架</el-button>
            <el-button link type="danger" @click="delDish(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                     :total="total" :page-size="query.size" v-model:current-page="query.current" @current-change="load" />
    </template>

    <!-- 团购 -->
    <template v-else-if="tab === 'groupbuy'">
      <el-table :data="groupbuys" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="团购名" />
        <el-table-column label="团购价" width="100">
          <template #default="{ row }">¥{{ row.groupbuyPrice }}</template>
        </el-table-column>
        <el-table-column label="原价" width="100">
          <template #default="{ row }">¥{{ row.originalPrice }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="sold" label="销量" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="{ 1: 'success', 0: 'info', 2: 'warning' }[row.status]">
              {{ { 1: '上架', 0: '下架', 2: '审核中' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="warning" @click="setGbStatus(row, 0)">下架</el-button>
            <el-button v-else link type="success" @click="setGbStatus(row, 1)">上架</el-button>
            <el-button link type="danger" @click="delGb(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                     :total="total" :page-size="query.size" v-model:current-page="query.current" @current-change="load" />
    </template>

    <!-- 评价 -->
    <template v-else>
      <el-table :data="reviews" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="shopId" label="店铺ID" width="90" />
        <el-table-column label="评分" width="80">
          <template #default="{ row }">{{ (row.score / 10).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="{ 1: 'success', 0: 'warning', 2: 'danger' }[row.status]">
              {{ { 1: '正常', 0: '隐藏', 2: '删除' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="warning" @click="setReviewStatus(row, 0)">隐藏</el-button>
            <el-button v-else link type="success" @click="setReviewStatus(row, 1)">恢复</el-button>
            <el-button link type="danger" @click="delReview(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                     :total="total" :page-size="query.size" v-model:current-page="query.current" @current-change="load" />
    </template>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  dishList, dishStatus, dishDelete,
  groupbuyList, groupbuyStatus, groupbuyDelete,
  reviewList, reviewStatus, reviewDelete
} from '../api'

const tab = ref('dish')
const loading = ref(false)
const total = ref(0)
const query = reactive({ current: 1, size: 10 })

const dishes = ref([])
const groupbuys = ref([])
const reviews = ref([])

function dishStatusText(s) {
  return { 1: '上架', 0: '下架', 2: '售罄', 3: '停售' }[s] || s
}

async function load() {
  query.current = 1
  loading.value = true
  try {
    if (tab.value === 'dish') {
      const data = await dishList(query)
      dishes.value = data.records
      total.value = Number(data.total)
    } else if (tab.value === 'groupbuy') {
      const data = await groupbuyList(query)
      groupbuys.value = data.records
      total.value = Number(data.total)
    } else {
      const data = await reviewList(query)
      reviews.value = data.records
      total.value = Number(data.total)
    }
  } finally {
    loading.value = false
  }
}

async function setDishStatus(row, status) {
  await dishStatus(row.id, status)
  ElMessage.success('操作成功')
  load()
}
async function delDish(row) {
  await ElMessageBox.confirm(`确认删除菜品「${row.name}」？`, '删除', { type: 'warning' })
  await dishDelete(row.id)
  ElMessage.success('已删除')
  load()
}
async function setGbStatus(row, status) {
  await groupbuyStatus(row.id, status)
  ElMessage.success('操作成功')
  load()
}
async function delGb(row) {
  await ElMessageBox.confirm(`确认删除团购「${row.name}」？`, '删除', { type: 'warning' })
  await groupbuyDelete(row.id)
  ElMessage.success('已删除')
  load()
}
async function setReviewStatus(row, status) {
  await reviewStatus(row.id, status)
  ElMessage.success('操作成功')
  load()
}
async function delReview(row) {
  await ElMessageBox.confirm('确认删除该评价？', '删除', { type: 'warning' })
  await reviewDelete(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
