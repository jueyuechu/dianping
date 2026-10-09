<template>
  <el-card>
    <!-- 筛选 -->
    <el-form inline>
      <el-form-item label="业务类型">
        <el-select v-model="query.bizType" clearable placeholder="全部" style="width:140px" @change="load">
          <el-option v-for="(name, val) in bizTypes" :key="val" :label="name" :value="Number(val)" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width:140px" @change="load">
          <el-option v-for="(name, val) in statuses" :key="val" :label="name" :value="Number(val)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- 任务列表 -->
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="id" label="任务ID" width="90" />
      <el-table-column label="业务类型" width="100">
        <template #default="{ row }">{{ bizTypes[row.bizType] || row.bizType }}</template>
      </el-table-column>
      <el-table-column prop="bizId" label="对象ID" width="100" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statuses[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submitterId" label="提交人" width="100" />
      <el-table-column prop="reason" label="驳回原因" show-overflow-tooltip />
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 1 || row.status === 2">
            <el-button link type="success" @click="pass(row)">通过</el-button>
            <el-button link type="danger" @click="openReject(row)">驳回</el-button>
          </template>
          <span v-else style="color:#999;font-size:12px">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.size" v-model:current-page="query.current" @current-change="load" />

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回审核" width="420px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因">
          <el-select v-model="rejectForm.template" placeholder="选择模板（可选）" clearable style="width:100%"
                     @change="v => rejectForm.reason = templates[v] || rejectForm.reason">
            <el-option v-for="(t, i) in templates" :key="i" :label="t" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因说明">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请填写驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="reject">确认驳回</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditTasks, auditPass, auditReject } from '../api'

// 对齐《审核状态机详细设计.md》
const bizTypes = { 1: '商户', 2: '店铺', 3: '菜品', 4: '团购', 5: '评价', 6: '图片', 7: '帖子' }
const statuses = { 0: '草稿', 1: '待审核', 2: '审核中', 3: '通过', 4: '驳回', 5: '上架', 6: '下架', 7: '封禁' }
// 驳回原因模板（对齐状态机文档第七节）
const templates = [
  '营业执照信息不清晰，请重新上传', '法人信息与营业执照不一致', '门店照片不符合规范',
  '位置信息不准确，请重新定位', '菜品图片不清晰或含水印', '菜品描述含违规内容',
  '团购价格虚高，请调整原价', '团购使用规则不明确', '评价含违规内容',
  '图片含违规内容', '帖子含违规/广告引流内容'
]

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, bizType: null, status: null })

const rejectVisible = ref(false)
const rejectForm = reactive({ taskId: null, reason: '', template: null })

function statusTagType(s) {
  return { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }[s] || 'info'
}

async function load() {
  loading.value = true
  try {
    const data = await auditTasks(query)
    rows.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

async function pass(row) {
  await ElMessageBox.confirm(`确认通过任务 #${row.id}（${bizTypes[row.bizType]}）？`, '审核通过', { type: 'success' })
  await auditPass(row.id)
  ElMessage.success('已通过')
  load()
}

function openReject(row) {
  rejectForm.taskId = row.id
  rejectForm.reason = ''
  rejectForm.template = null
  rejectVisible.value = true
}

async function reject() {
  if (!rejectForm.reason) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  await auditReject(rejectForm.taskId, { reason: rejectForm.reason, templateId: rejectForm.template })
  ElMessage.success('已驳回')
  rejectVisible.value = false
  load()
}

onMounted(load)
</script>
