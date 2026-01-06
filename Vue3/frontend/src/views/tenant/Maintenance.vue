<!--
/**
 * 文件名: Maintenance.vue
 * 作者: 牛迦楠
 * 学校: 东华理工大学
 * 专业: 软件工程（中外合作办学）
 * 课题: 融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 创建日期: 2026-01-06
 * 描述: 维修服务组件，提供在线报修申请和维修记录查看功能
 */
-->
<template>
  <div class="maintenance-page">
    <Navbar />
    <div class="page-header-spacer"></div>
    <div class="maintenance-container">
      <div class="header">
        <div class="title-section">
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square" stroke-linejoin="miter" class="icon"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg>
          <h2>维修服务</h2>
        </div>
        <button class="btn-primary" @click="showCreateModal = true">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square" stroke-linejoin="miter"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          申请维修
        </button>
      </div>

      <div class="content-grid">
        <!-- Right Column: Maintenance Requests -->
        <div class="right-column">
          <div class="section-title" style="margin-top: 0;">
            <h3>维修记录 / HISTORY</h3>
          </div>

          <div v-if="requests.length === 0" class="no-data-card">
            <p>暂无维修记录</p>
          </div>
          <div v-else class="request-list">
            <div class="request-card" v-for="req in requests" :key="req.id">
              <div class="req-header">
                <span class="req-title">{{ req.requestTitle }}</span>
                <span class="req-status" :class="getStatusClass(req.requestStatus)">{{ getStatusText(req.requestStatus) }}</span>
              </div>
              <p class="req-desc">{{ req.requestDescription }}</p>
              <div class="req-meta">
                <div class="meta-item">
                  <span class="label">申请日期:</span>
                  <span>{{ formatDate(req.requestDate) }}</span>
                </div>
                <div class="meta-item" v-if="req.expectedFixDate">
                  <span class="label">期望日期:</span>
                  <span>{{ formatDate(req.expectedFixDate) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Modal -->
      <div v-if="showCreateModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>申请维修 / NEW REQUEST</h3>
            <button class="close-btn" @click="showCreateModal = false">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square" stroke-linejoin="miter"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>
          </div>
          <form @submit.prevent="submitRequest">
            <div class="form-group">
              <label>标题</label>
              <input v-model="newRequest.requestTitle" required placeholder="请简要描述问题 (如: 水龙头漏水)" />
            </div>
            <div class="form-group">
              <label>详细描述</label>
              <textarea v-model="newRequest.requestDescription" required placeholder="请详细描述故障情况和发生位置..."></textarea>
            </div>
            <div class="form-group">
              <label>期望维修日期</label>
              <input type="date" v-model="newRequest.expectedFixDate" />
            </div>
            <div class="modal-actions">
              <button type="button" class="btn-secondary" @click="showCreateModal = false">取消</button>
              <button type="submit" class="btn-primary">提交申请</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../../components/Navbar.vue'
import { userState } from '../../state/user'

const requests = ref([])
const showCreateModal = ref(false)
const activeLease = ref(null)
const houseDetails = ref(null)

const newRequest = ref({
  requestTitle: '',
  requestDescription: '',
  expectedFixDate: ''
})

const fetchActiveLease = async () => {
  // Get user ID dynamically to ensure it's available even if state updates late
  let uid = userState.user ? userState.user.id : 0;
  if (!uid) {
    const local = localStorage.getItem('user');
    if (local) {
       try { uid = JSON.parse(local).id; } catch(e){}
    }
  }
  
  if (!uid) return;

  try {
    const res = await fetch(`https://api.homesee.xyz/api/admin/tenant/tenant/${uid}`)
    const data = await res.json()
    if (data.success && data.contracts && data.contracts.length > 0) {
      const active = data.contracts.find(c => c.contractStatus === 1 || c.contractStatus === 2)
      if (active) {
        activeLease.value = active
        if (active.roomId) {
          await fetchHouseDetails(active.roomId)
        }
        await fetchRequests()
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchHouseDetails = async (roomId) => {
  try {
    const res = await fetch(`https://api.homesee.xyz/api/room-info/${roomId}`)
    const data = await res.json()
    if (data.success && data.room) {
      houseDetails.value = data.room
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchRequests = async () => {
  if (!activeLease.value) return
  
  try {
      const res = await fetch(`https://api.homesee.xyz/api/maintenance/list/${activeLease.value.id}`)
      const data = await res.json()
      if (data.success) {
          requests.value = data.data
      }
  } catch (e) {
      console.error(e)
  }
}

const submitRequest = async () => {
  if (!activeLease.value) return alert('无有效租约')
  
  const payload = {
    tenantManagementId: activeLease.value.id,
    ...newRequest.value,
    requestStatus: 0
  }

  try {
    const res = await fetch('https://api.homesee.xyz/api/maintenance/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (data.success) {
      alert('提交成功')
      showCreateModal.value = false
      fetchRequests()
      newRequest.value = { requestTitle: '', requestDescription: '', expectedFixDate: '' }
    } else {
      alert('提交失败')
    }
  } catch (e) {
    console.error(e)
    alert('网络错误')
  }
}

const getStatusText = (status) => {
  const map = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-pending', 1: 'status-processing', 2: 'status-completed', 3: 'status-closed' }
  return map[status] || ''
}

const formatDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleDateString()
}

onMounted(() => {
  fetchActiveLease()
})
</script>

<style scoped>
.maintenance-page {
  min-height: 100vh;
  background-color: #FFFFFF;
  font-family: 'Inter', sans-serif;
  color: #111827;
}

.page-header-spacer {
  height: 64px;
}

.maintenance-container {
  padding: 40px 24px;
  max-width: 1000px;
  margin: 0 auto;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
  border-bottom: 1px solid #E5E7EB;
  padding-bottom: 20px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-section h2 {
  font-size: 1.5rem;
  font-weight: 800;
  margin: 0;
  color: #111827;
  text-transform: uppercase;
  letter-spacing: -0.5px;
}

.icon {
  color: #111827;
  width: 32px;
  height: 32px;
}

/* Content */
.content-grid {
  display: grid;
  gap: 40px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  color: #111827;
}

.section-title h3 {
  font-size: 1rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0;
  color: #6B7280;
}

/* Request List */
.request-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.request-card {
  background: white;
  padding: 24px;
  border: 1px solid #E5E7EB;
  transition: all 0.2s;
  border-radius: 0;
}

.request-card:hover {
  border-color: #111827;
}

.req-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.req-title {
  font-weight: 700;
  font-size: 1.1rem;
  color: #111827;
}

.req-status {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  padding: 4px 8px;
  letter-spacing: 0.5px;
  border-radius: 0;
}

/* Status variants */
.status-pending { color: #D97706; border: 1px solid #D97706; background: white; }
.status-processing { color: #2563EB; border: 1px solid #2563EB; background: white; }
.status-completed { color: #059669; border: 1px solid #059669; background: white; }
.status-closed { color: #4B5563; border: 1px solid #4B5563; background: #F3F4F6; }

.req-desc {
  color: #4B5563;
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 20px;
}

.req-meta {
  display: flex;
  gap: 32px;
  border-top: 1px solid #F3F4F6;
  padding-top: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #111827;
  font-family: 'JetBrains Mono', monospace;
}

.meta-item .label {
  color: #6B7280;
  font-weight: 500;
}

.no-data-card {
  padding: 60px;
  text-align: center;
  color: #9CA3AF;
  border: 1px dashed #E5E7EB;
  font-size: 0.9rem;
  border-radius: 0;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 40px;
  border-radius: 0;
  width: 90%;
  max-width: 500px;
  border: 1px solid #E5E7EB;
  box-shadow: none;
}

.modal-header {
  margin-bottom: 32px;
  border-bottom: 1px solid #E5E7EB;
  padding-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-size: 1.1rem;
  font-weight: 800;
  text-transform: uppercase;
  margin: 0;
  color: #111827;
}

.close-btn {
  background: none;
  border: 1px solid transparent;
  cursor: pointer;
  color: #6B7280;
  padding: 4px;
  border-radius: 0;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #F3F4F6;
  color: #111827;
  border-color: #E5E7EB;
}

/* Forms */
.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  color: #374151;
}

.form-group input, .form-group textarea {
  width: 100%;
  border-radius: 0;
  border: 1px solid #E5E7EB;
  padding: 12px;
  font-size: 0.95rem;
  color: #111827;
  background: #FFFFFF;
  box-sizing: border-box;
  font-family: inherit;
  transition: all 0.2s;
}

.form-group input:focus, .form-group textarea:focus {
  border-color: #111827;
  outline: none;
}

/* Buttons */
.modal-actions {
  display: flex;
  gap: 16px;
  margin-top: 32px;
}

.btn-primary {
  background: #111827;
  color: white;
  border: 1px solid #111827;
  border-radius: 0;
  padding: 10px 24px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #000000;
  border-color: #000000;
}

.btn-secondary {
  background: white;
  color: #111827;
  border-radius: 0;
  border: 1px solid #E5E7EB;
  padding: 10px 24px;
  font-weight: 600;
  text-transform: uppercase;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-secondary:hover {
  border-color: #9CA3AF;
}
</style>
