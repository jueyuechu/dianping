<template>
  <el-card>
    <el-form inline>
      <el-form-item>
        <el-input v-model="query.keyword" placeholder="搜索昵称" clearable style="width:200px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="nickName" label="昵称" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="openid" label="OpenID" width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '封禁' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" link type="danger" @click="openBan(row)">封禁</el-button>
          <el-button v-else link type="success" @click="unban(row)">解封</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.size" v-model:current-page="query.current" @current-change="load" />

    <!-- 封禁弹窗（分级 + 时长） -->
    <el-dialog v-model="banVisible" title="封禁用户" width="440px">
      <el-form label-width="90px">
        <el-form-item label="封禁范围">
          <el-select v-model="banForm.banType" style="width:100%">
            <el-option :value="1" label="禁止登录" />
            <el-option :value="2" label="禁止评价" />
            <el-option :value="3" label="禁止评论" />
            <el-option :value="4" label="禁止下单" />
            <el-option :value="5" label="禁止私信" />
          </el-select>
        </el-form-item>
        <el-form-item label="封禁时长">
          <el-select v-model="banForm.duration" style="width:100%">
            <el-option :value="1" label="1 天" />
            <el-option :value="7" label="7 天" />
            <el-option :value="30" label="30 天" />
            <el-option :value="0" label="永久" />
          </el-select>
        </el-form-item>
        <el-form-item label="封禁原因">
          <el-input v-model="banForm.reason" type="textarea" :rows="2" placeholder="如：刷评、广告、辱骂" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banVisible = false">取消</el-button>
        <el-button type="danger" @click="ban">确认封禁</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userList, userBan, userUnban } from '../api'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, keyword: '' })

const banVisible = ref(false)
const banForm = reactive({ userId: null, banType: 2, duration: 7, reason: '' })

async function load() {
  loading.value = true
  try {
    const data = await userList(query)
    rows.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

function openBan(row) {
  banForm.userId = row.id
  banForm.banType = 2
  banForm.duration = 7
  banForm.reason = ''
  banVisible.value = true
}

async function ban() {
  if (!banForm.reason) {
    ElMessage.warning('请填写封禁原因')
    return
  }
  await userBan(banForm.userId, { banType: banForm.banType, duration: banForm.duration, reason: banForm.reason })
  ElMessage.success('已封禁')
  banVisible.value = false
  load()
}

async function unban(row) {
  await ElMessageBox.confirm(`确认解封用户「${row.nickName}」？`, '解封', { type: 'warning' })
  await userUnban(row.id)
  ElMessage.success('已解封')
  load()
}

onMounted(load)
</script>
