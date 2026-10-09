<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside :width="collapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-if="!collapse">点评管理后台</span>
        <span v-else>点</span>
      </div>
      <el-menu :default-active="route.path" :collapse="collapse" router background-color="#1f2430"
               text-color="#a3a8b3" active-text-color="#fff" class="menu">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>数据看板</span></el-menu-item>
        <el-menu-item index="/audit"><el-icon><Stamp /></el-icon><span>审核中心</span></el-menu-item>
        <el-menu-item index="/user"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/merchant"><el-icon><Shop /></el-icon><span>商户管理</span></el-menu-item>
        <el-menu-item index="/content"><el-icon><Document /></el-icon><span>内容管理</span></el-menu-item>
        <el-menu-item index="/report"><el-icon><Warning /></el-icon><span>举报处理</span></el-menu-item>
        <el-menu-item index="/system"><el-icon><Setting /></el-icon><span>系统设置</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapse = !collapse">
            <Expand v-if="collapse" /><Fold v-else />
          </el-icon>
          <span class="page-title">{{ route.meta.title }}</span>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="30" style="background:#4f6ef7">{{ avatarText }}</el-avatar>
            <span class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapse = ref(false)

const avatarText = computed(() =>
  (userStore.userInfo?.realName || userStore.userInfo?.username || '管').slice(0, 1))

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #1f2430; transition: width .2s; overflow: hidden; }
.logo {
  height: 56px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 16px; font-weight: 700; letter-spacing: 1px;
}
.menu { border-right: none; }
.header {
  background: #fff; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid #ececf0;
}
.header-left { display: flex; align-items: center; gap: 14px; }
.collapse-btn { font-size: 18px; cursor: pointer; color: #666; }
.page-title { font-size: 15px; font-weight: 600; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-name { font-size: 13px; color: #333; }
.main { background: #f5f6f8; padding: 16px; overflow-y: auto; }
</style>
