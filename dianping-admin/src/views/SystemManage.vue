<template>
  <div>
    <el-card style="margin-bottom:16px">
      <template #header><b>管理员账号</b></template>
      <el-table :data="accounts" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                     :total="total" :page-size="query.size" v-model:current-page="query.current" @current-change="loadAccounts" />
    </el-card>

    <el-card>
      <template #header><b>角色列表（RBAC）</b></template>
      <el-table :data="roles" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名" />
        <el-table-column prop="roleKey" label="角色标识" />
        <el-table-column prop="sort" label="排序" width="90" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { accountList, roleList } from '../api'

const loading = ref(false)
const accounts = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10 })
const roles = ref([])

async function loadAccounts() {
  loading.value = true
  try {
    const data = await accountList(query)
    accounts.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  roles.value = await roleList()
}

onMounted(() => {
  loadAccounts()
  loadRoles()
})
</script>
