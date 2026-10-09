import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'audit', name: 'Audit', component: () => import('../views/AuditCenter.vue'), meta: { title: '审核中心' } },
      { path: 'user', name: 'UserManage', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'merchant', name: 'MerchantManage', component: () => import('../views/MerchantManage.vue'), meta: { title: '商户管理' } },
      { path: 'content', name: 'ContentManage', component: () => import('../views/ContentManage.vue'), meta: { title: '内容管理' } },
      { path: 'report', name: 'ReportManage', component: () => import('../views/ReportManage.vue'), meta: { title: '举报处理' } },
      { path: 'system', name: 'System', component: () => import('../views/SystemManage.vue'), meta: { title: '系统设置' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.isLogin) {
    next('/login')
  } else {
    next()
  }
})

export default router