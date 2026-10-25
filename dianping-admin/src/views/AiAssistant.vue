<template>
  <div class="ai-page">
    <el-card class="chat-card">
      <template #header>
        <div class="card-head">
          <div>
            <span class="title">AI 数据助手</span>
            <span class="sub">用自然语言查询平台数据 · 只读执行</span>
          </div>
          <el-tag :type="mode === false ? 'success' : 'info'" size="small">
            {{ mode === null ? '就绪' : (mode ? '本地规则模式' : 'LLM 模式') }}
          </el-tag>
        </div>
      </template>

      <div class="messages" ref="msgBox">
        <div v-if="!messages.length" class="welcome">
          <div class="welcome-title">你可以直接问，例如：</div>
          <el-space wrap>
            <el-button v-for="s in suggestions" :key="s" size="small" plain @click="ask(s)">{{ s }}</el-button>
          </el-space>
          <div class="tips">仅支持查询（SELECT），禁止写操作；查询结果最多返回 50 行。</div>
        </div>

        <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
          <div class="bubble">
            <template v-if="m.role === 'user'">{{ m.text }}</template>
            <template v-else>
              <div class="answer">{{ m.answer }}</div>
              <pre v-if="m.sql" class="sql">{{ m.sql }}</pre>
              <el-table v-if="m.rows && m.rows.length" :data="m.rows" size="small" border
                        style="margin-top:10px;width:100%">
                <el-table-column v-for="c in m.columns" :key="c" :prop="c" :label="c"
                                 show-overflow-tooltip min-width="110" />
              </el-table>
            </template>
          </div>
        </div>
      </div>

      <div class="input-row">
        <el-input v-model="question" placeholder="例如：最近订单有多少？待审核任务有几个？" clearable
                  :disabled="loading" @keyup.enter="ask()" />
        <el-button type="primary" :loading="loading" @click="ask()">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { aiText2Sql } from '../api'

const suggestions = ['用户数量', '商家数量', '订单销售额', '评价数量', '待审核任务', '待处理举报', '最近订单']
const messages = ref([])
const question = ref('')
const loading = ref(false)
const mode = ref(null)
const msgBox = ref(null)

async function ask(preset) {
  const q = (preset || question.value).trim()
  if (!q || loading.value) return
  messages.value.push({ role: 'user', text: q })
  question.value = ''
  loading.value = true
  scrollToBottom()
  try {
    const r = await aiText2Sql(q)
    mode.value = r.mock
    messages.value.push({
      role: 'ai',
      answer: r.answer,
      sql: r.sql,
      columns: r.columns || [],
      rows: r.rows || []
    })
  } catch (e) {
    messages.value.push({ role: 'ai', answer: '查询失败，请换个问法或稍后重试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}
</script>

<style scoped>
.ai-page { height: calc(100vh - 100px); }
.chat-card { height: 100%; display: flex; flex-direction: column; }
.chat-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; padding: 16px; }
.card-head { display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 15px; font-weight: 600; margin-right: 10px; }
.sub { font-size: 12px; color: #999; }
.messages { flex: 1; overflow-y: auto; padding: 4px 2px; }
.welcome { text-align: center; padding: 40px 10px; }
.welcome-title { color: #666; font-size: 13px; margin-bottom: 14px; }
.tips { color: #bbb; font-size: 12px; margin-top: 18px; }
.msg { display: flex; margin-bottom: 14px; }
.msg.user { justify-content: flex-end; }
.bubble { max-width: 82%; background: #f4f6fa; border-radius: 10px; padding: 10px 14px; font-size: 13px; color: #333; }
.msg.user .bubble { background: #4f6ef7; color: #fff; }
.answer { line-height: 1.6; }
.sql { margin: 8px 0 0; padding: 10px 12px; background: #1f2430; color: #a8e6a3; border-radius: 8px;
       font-size: 12px; white-space: pre-wrap; word-break: break-all; }
.input-row { display: flex; gap: 10px; padding-top: 12px; border-top: 1px solid #f0f0f2; }
</style>