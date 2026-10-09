import request from '../utils/request'

// 管理员端 API（对齐《核心接口文档.md》第四节）

// ---- 认证 ----
export const adminLogin = data => request.post('/admin/auth/login', data)

// ---- 看板 ----
export const dashboardOverview = () => request.get('/admin/dashboard/overview')

// ---- 用户管理 ----
export const userList = params => request.get('/admin/user/list', { params })
export const userDetail = id => request.get(`/admin/user/${id}`)
export const userBan = (id, data) => request.post(`/admin/user/${id}/ban`, data)
export const userUnban = id => request.post(`/admin/user/${id}/unban`)
export const banRecords = params => request.get('/admin/user/ban-records', { params })

// ---- 商户管理 ----
export const merchantList = params => request.get('/admin/merchant/list', { params })
export const merchantFreeze = id => request.post(`/admin/merchant/${id}/freeze`)
export const merchantUnfreeze = id => request.post(`/admin/merchant/${id}/unfreeze`)
export const shopList = params => request.get('/admin/merchant/shop/list', { params })

// ---- 统一审核 ----
export const auditTasks = params => request.get('/admin/audit/tasks', { params })
export const auditPass = taskId => request.post(`/admin/audit/${taskId}/pass`)
export const auditReject = (taskId, data) => request.post(`/admin/audit/${taskId}/reject`, data)

// ---- 举报 ----
export const reportList = params => request.get('/admin/report/list', { params })
export const reportHandle = (id, result) => request.post(`/admin/report/${id}/handle`, null, { params: { result } })

// ---- 内容管理 ----
export const dishList = params => request.get('/admin/dish/list', { params })
export const dishStatus = (id, status) => request.put(`/admin/dish/${id}/status`, null, { params: { status } })
export const dishDelete = id => request.delete(`/admin/dish/${id}`)
export const groupbuyList = params => request.get('/admin/groupbuy/list', { params })
export const groupbuyStatus = (id, status) => request.put(`/admin/groupbuy/${id}/status`, null, { params: { status } })
export const groupbuyDelete = id => request.delete(`/admin/groupbuy/${id}`)
export const reviewList = params => request.get('/admin/review/list', { params })
export const reviewStatus = (id, status) => request.put(`/admin/review/${id}/status`, null, { params: { status } })
export const reviewDelete = id => request.delete(`/admin/review/${id}`)

// ---- 系统设置 ----
export const accountList = params => request.get('/admin/account/list', { params })
export const roleList = () => request.get('/admin/role/list')
