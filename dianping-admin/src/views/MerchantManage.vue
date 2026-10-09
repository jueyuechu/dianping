<template>
  <el-card>
    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="商户列表" name="merchant" />
      <el-tab-pane label="店铺列表" name="shop" />
    </el-tabs>

    <!-- 商户列表 -->
    <template v-if="tab === 'merchant'">
      <el-form inline>
        <el-form-item>
          <el-input v-model="mQuery.keyword" placeholder="搜索商户名称" clearable style="width:200px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="mQuery.status" clearable placeholder="状态" style="width:130px" @change="load">
            <el-option :value="0" label="待审核" />
            <el-option :value="1" label="正常" />
            <el-option :value="2" label="冻结" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="merchants" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商户名称" />
        <el-table-column prop="legalPerson" label="法人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="{ 0: 'warning', 1: 'success', 2: 'danger' }[row.status]">
              {{ { 0: '待审核', 1: '正常', 2: '冻结' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入驻时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="danger" @click="freeze(row)">冻结</el-button>
            <el-button v-if="row.status === 2" link type="success" @click="unfreeze(row)">解冻</el-button>
            <el-button v-if="row.status === 0" link type="primary" @click="$router.push('/audit')">去审核</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                     :total="mTotal" :page-size="mQuery.size" v-model:current-page="mQuery.current" @current-change="load" />
    </template>

    <!-- 店铺列表 -->
    <template v-else>
      <el-table :data="shops" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="店铺名" />
        <el-table-column prop="area" label="商圈" width="120" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column label="评分" width="90">
          <template #default="{ row }">{{ (row.score / 10).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="sold" label="销量" width="90" />
        <el-table-column label="营业状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '营业' : '休息' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                     :total="sTotal" :page-size="sQuery.size" v-model:current-page="sQuery.current" @current-change="load" />
    </template>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { merchantList, merchantFreeze, merchantUnfreeze, shopList } from '../api'

const tab = ref('merchant')
const loading = ref(false)

const merchants = ref([])
const mTotal = ref(0)
const mQuery = reactive({ current: 1, size: 10, keyword: '', status: null })

const shops = ref([])
const sTotal = ref(0)
const sQuery = reactive({ current: 1, size: 10 })

async function load() {
  loading.value = true
  try {
    if (tab.value === 'merchant') {
      const data = await merchantList(mQuery)
      merchants.value = data.records
      mTotal.value = Number(data.total)
    } else {
      const data = await shopList(sQuery)
      shops.value = data.records
      sTotal.value = Number(data.total)
    }
  } finally {
    loading.value = false
  }
}

async function freeze(row) {
  await ElMessageBox.confirm(`确认冻结商户「${row.name}」？冻结后其店铺将暂停经营。`, '冻结商户', { type: 'warning' })
  await merchantFreeze(row.id)
  ElMessage.success('已冻结')
  load()
}

async function unfreeze(row) {
  await merchantUnfreeze(row.id)
  ElMessage.success('已解冻')
  load()
}

onMounted(load)
</script>
