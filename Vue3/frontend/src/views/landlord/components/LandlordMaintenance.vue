<!--
  项目名称：融合大模型交互与3D全景预览的智能选房平台
  文件名称：LandlordMaintenance.vue
  开发者：牛迦楠
  专业：软件工程（中外合作办学）
  学校：东华理工大学
  功能描述：房东维修工单管理组件，提供维修请求处理、状态跟踪和费用管理功能
  创建日期：2026-01-06
-->
<template>
  <div class="landlord-maintenance">
    <div class="header">
      <h3>维修工单管理</h3>
      <div class="filters">
        <button 
          v-for="status in statusFilters" 
          :key="status.value"
          :class="['filter-btn', { active: currentFilter === status.value }]"
          @click="currentFilter = status.value"
        >
          {{ status.label }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredRequests.length === 0" class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#e5e7eb" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg>
      <p>暂无维修工单</p>
    </div>

    <div v-else class="request-list">
      <div v-for="req in filteredRequests" :key="req.id" class="request-card">
        <div class="card-body">
          <!-- 头部内容合并至此 -->
          <div class="new-card-header">
            <div class="header-left">
              <span class="req-id">#{{ req.id }}</span>
              <span class="req-title">{{ req.requestTitle }}</span>
            </div>
            <!-- 状态进度条/徽章 -->
            <div class="status-container">
              <div class="status-progress" v-if="req.requestStatus !== 3">
                <div class="progress-step" :class="{ active: req.requestStatus >= 0, current: req.requestStatus === 0 }">
                  <div class="step-dot"></div>
                  <span class="step-label">已提交</span>
                </div>
                <div class="progress-line" :class="{ active: req.requestStatus >= 1 }"></div>
                <div class="progress-step" :class="{ active: req.requestStatus >= 1, current: req.requestStatus === 1 }">
                  <div class="step-dot"></div>
                  <span class="step-label">处理中</span>
                </div>
                <div class="progress-line" :class="{ active: req.requestStatus >= 2 }"></div>
                <div class="progress-step" :class="{ active: req.requestStatus >= 2, current: req.requestStatus === 2 }">
                  <div class="step-dot"></div>
                  <span class="step-label">已完成</span>
                </div>
              </div>
              <span v-else class="status-badge status-closed">已关闭</span>
            </div>
          </div>

          <p class="req-desc">{{ req.requestDescription }}</p>
          
          <div class="info-grid">
            <div class="info-item">
              <span class="label">租户</span>
              <span class="value">{{ req.tenantName || '未知' }} ({{ req.tenantPhone || '--' }})</span>
            </div>
            <div class="info-item">
              <span class="label">房屋</span>
              <span class="value">{{ req.roomAddress || '未知地址' }}</span>
            </div>
            <div class="info-item">
              <span class="label">申请时间</span>
              <span class="value">{{ formatDate(req.requestDate) }}</span>
            </div>
            <div class="info-item">
              <span class="label">期望维修</span>
              <span class="value">{{ formatDate(req.expectedFixDate) || '未指定' }}</span>
            </div>
            <div class="info-item" v-if="req.requestStatus === 2">
              <span class="label">维修费用</span>
              <span class="value">¥{{ req.repairCost || 0 }}</span>
            </div>
            <div class="info-item" v-if="req.requestStatus === 2">
              <span class="label">费用承担</span>
              <span class="value">{{ getCostBearerText(req.costBearer) }}</span>
            </div>
          </div>

          <!-- 操作按钮合并至此 -->
          <div class="actions">
            <button 
              v-if="req.requestStatus === 0" 
              class="action-btn btn-process"
              @click="updateStatus(req, 1)"
            >
              开始处理
            </button>
            <button 
              v-if="req.requestStatus === 1" 
              class="action-btn btn-complete"
              @click="openCompleteModal(req)"
            >
              完成维修
            </button>
            <button 
              v-if="req.requestStatus !== 3" 
              class="action-btn btn-close"
              @click="updateStatus(req, 3)"
            >
              关闭工单
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 完成维修模态框 -->
    <div v-if="showCompleteModal" class="modal-overlay">
      <div class="modal-content">
        <h3>完成维修工单</h3>
        <div class="form-group">
          <label>维修费用 (元)</label>
          <input type="number" v-model="completeForm.repairCost" placeholder="请输入维修费用">
        </div>
        <div class="form-group">
          <label>费用承担方</label>
          <select v-model="completeForm.costBearer">
            <option :value="1">房东承担</option>
            <option :value="2">租客承担</option>
            <option :value="3">共同分摊</option>
          </select>
        </div>
        <div class="modal-actions">
          <button @click="showCompleteModal = false" class="btn-cancel">取消</button>
          <button @click="submitComplete" class="btn-confirm">确认完成</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({
  userId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const requests = ref([])
const currentFilter = ref('all')
const showCompleteModal = ref(false)
const completeForm = ref({
  id: null,
  repairCost: 0,
  costBearer: 1
})

const statusFilters = [
  { value: 'all', label: '全部' },
  { value: 0, label: '待处理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已关闭' }
]

const filteredRequests = computed(() => {
  if (currentFilter.value === 'all') {
    return requests.value
  }
  return requests.value.filter(req => req.requestStatus === currentFilter.value)
})

const loadRequests = async () => {
  if (!props.userId) return
  
  loading.value = true
  try {
    const res = await axios.get(`https://api.homesee.xyz/api/maintenance/landlord/${props.userId}`)
    if (res.data.success) {
      requests.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载维修请求失败:', e)
  } finally {
    loading.value = false
  }
}

const openCompleteModal = (req) => {
  completeForm.value = {
    id: req.id,
    repairCost: 0,
    costBearer: 1,
    req: req // 保持引用以更新本地状态
  }
  showCompleteModal.value = true
}

const submitComplete = async () => {
  const req = completeForm.value.req
  try {
    const updateData = {
      id: req.id,
      requestStatus: 2, // 已完成
      tenantManagementId: req.tenantManagementId,
      requestTitle: req.requestTitle,
      requestDescription: req.requestDescription,
      requestDate: req.requestDate,
      expectedFixDate: req.expectedFixDate,
      actualFixDate: new Date().toISOString(),
      repairCost: completeForm.value.repairCost,
      costBearer: completeForm.value.costBearer
    }

    const res = await axios.post('https://api.homesee.xyz/api/maintenance/update', updateData)
    if (res.data.success) {
      // 更新本地状态
      req.requestStatus = 2
      req.repairCost = completeForm.value.repairCost
      req.costBearer = completeForm.value.costBearer
      req.actualFixDate = updateData.actualFixDate
      showCompleteModal.value = false
    } else {
      alert('更新失败')
    }
  } catch (e) {
    console.error('完成请求失败:', e)
    alert('更新失败，请重试')
  }
}

const updateStatus = async (req, newStatus) => {
  if (!confirm('确定要更新此工单状态吗？')) return

  try {
    const updateData = {
      id: req.id,
      requestStatus: newStatus,
      tenantManagementId: req.tenantManagementId,
      requestTitle: req.requestTitle,
      requestDescription: req.requestDescription,
      requestDate: req.requestDate,
      expectedFixDate: req.expectedFixDate,
      actualFixDate: newStatus === 2 ? new Date().toISOString() : req.actualFixDate,
      repairCost: req.repairCost,
      costBearer: req.costBearer
    }

    const res = await axios.post('https://api.homesee.xyz/api/maintenance/update', updateData)
    if (res.data.success) {
      req.requestStatus = newStatus
      if (newStatus === 2) {
        req.actualFixDate = updateData.actualFixDate
      }
    } else {
      alert('更新失败')
    }
  } catch (e) {
    console.error('更新状态失败:', e)
    alert('更新失败，请重试')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const getStatusText = (status) => {
  const map = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 
    0: 'status-pending', 
    1: 'status-processing', 
    2: 'status-completed', 
    3: 'status-closed' 
  }
  return map[status] || ''
}

const getCostBearerText = (bearer) => {
  const map = { 1: '房东承担', 2: '租客承担', 3: '共同分摊' }
  return map[bearer] || '未指定'
}

onMounted(() => {
  loadRequests()
})
</script>

<style scoped>
/* 个人开发风格 - 轻工业风 (紧凑) */
.landlord-maintenance {
  padding: 16px; /* 从 24px 减少 */
  width: 100%;
  max-width: none;
  background-color: #FFFFFF;
  min-height: 100vh;
  color: #1F2937;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px; /* 已减少 */
  border-bottom: 1px solid #E5E7EB;
  padding-bottom: 16px; /* Reduced */
}

.header h3 {
  font-size: 18px; /* 略微变小 */
  font-weight: 600;
  color: #111827;
  margin: 0;
  letter-spacing: -0.5px;
}

/* 筛选器 - 工具栏风格 */
.filters {
  display: flex;
  gap: -1px;
  background: transparent;
  padding: 0;
  border: 1px solid #E5E7EB;
}

.filter-btn {
  padding: 6px 16px; /* 减少内边距 */
  border: none;
  border-right: 1px solid #E5E7EB;
  background: white;
  border-radius: 0;
  font-size: 12px; /* 字体变小 */
  color: #6B7280;
  cursor: pointer;
  transition: all 0s;
}

.filter-btn:last-child {
  border-right: none;
}

.filter-btn:hover {
  background: #F9FAFB;
  color: #111827;
}

.filter-btn.active {
  background: #F3F4F6;
  color: #3A6EA5;
  font-weight: 600;
}

/* 请求列表 */
.request-list {
  display: flex;
  flex-direction: column;
  gap: 12px; /* 减少间隙 */
}

.request-card {
  background: white;
  border: 1px solid #E5E7EB;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
}

.request-card:hover {
  border-color: #9CA3AF;
}

.new-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F3F4F6;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px; /* 减少间隙 */
}

.req-id {
  font-family: 'JetBrains Mono', 'Menlo', monospace;
  color: #374151;
  background: white;
  padding: 2px 6px;
  border: 1px solid #E5E7EB;
  border-radius: 0;
  font-size: 11px;
}

.req-title {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

/* 状态徽章 - 极简 */
.status-badge {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 0;
  font-weight: 500;
  border: 1px solid #E5E7EB;
}

.status-pending { background: #FFF7ED; color: #9A3412; border-color: #FED7AA; }
.status-processing { background: #EFF6FF; color: #1E40AF; border-color: #BFDBFE; }
.status-completed { background: #F0FDF4; color: #166534; border-color: #BBF7D0; }
.status-closed { background: #F3F4F6; color: #4B5563; border-color: #E5E7EB; }

/* 进度条 - 线性方形 */
.status-progress {
  display: flex;
  align-items: center;
}

.progress-step {
  display: flex;
  align-items: center;
  gap: 6px; /* 已减少 */
}

.step-dot {
  width: 6px; /* 更小 */
  height: 6px;
  border-radius: 0;
  background: #E5E7EB;
  border: none;
}

.progress-step.active .step-dot {
  background: #9CA3AF;
}

.progress-step.current .step-dot {
  background: #3A6EA5;
  box-shadow: none;
}

.step-label {
  font-size: 11px;
  font-family: 'Inter', sans-serif;
  color: #9CA3AF;
}

.progress-step.active .step-label {
  color: #4B5563;
}

.progress-step.current .step-label {
  color: #3A6EA5;
  font-weight: 600;
}

.progress-line {
  width: 16px; /* 更短的线 */
  height: 1px;
  background: #E5E7EB;
  margin: 0 6px;
}

.progress-line.active {
  background: #9CA3AF;
}

/* 卡片主体 */
.card-body {
  padding: 16px; /* 减少内边距 */
}

.req-desc {
  color: #374151;
  margin-bottom: 16px; /* 减少外边距 */
  font-size: 13px; /* 字体变小 */
  line-height: 1.5;
  background: #F9FAFB;
  padding: 10px; /* 减少内边距 */
  border: 1px solid #E5E7EB;
  border-radius: 0;
  font-family: 'JetBrains Mono', monospace;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px; /* 减少间隙 */
  background: transparent;
  padding: 0;
  border: none;
  margin-bottom: 16px; /* 操作按钮前增加外边距 */
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px; /* 减少间隙 */
  border-left: 1px solid #E5E7EB; /* 更细的边框 */
  padding-left: 12px; /* 减少内边距 */
}

.info-item .label {
  font-size: 11px;
  color: #6B7280;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.info-item .value {
  font-size: 13px;
  color: #111827;
  font-weight: 500;
}

/* 操作按钮合并入主体 */
.actions {
  display: flex;
  gap: 8px; /* 减少间隙 */
  justify-content: flex-end;
  border-top: 1px solid #F3F4F6;
  padding-top: 12px;
}

.action-btn {
  padding: 6px 16px; /* 减少内边距 */
  border-radius: 0;
  font-size: 12px; /* 字体变小 */
  font-weight: 500;
  cursor: pointer;
  background: white;
  border: 1px solid #D1D5DB;
  color: #374151;
  transition: all 0s;
}

.action-btn:hover {
  background: #F9FAFB;
  border-color: #9CA3AF;
  color: #111827;
}

.btn-process {
  color: #3A6EA5;
  border-color: #3A6EA5;
}

.btn-process:hover {
  background: #EFF6FF;
}

.btn-complete {
  background: #111827;
  color: white;
  border-color: #111827;
}

.btn-complete:hover {
  background: #374151;
  border-color: #374151;
  color: white;
}

/* 空状态与加载中 */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0; /* 减少内边距 */
  color: #9CA3AF;
  border: 1px dashed #E5E7EB;
  border-radius: 0;
}

.empty-state svg {
  margin-bottom: 12px;
  color: #D1D5DB;
  width: 48px;
  height: 48px;
}

.spinner {
  border: 2px solid #E5E7EB;
  border-top: 2px solid #3A6EA5;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px; /* 已减少 */
  border: 1px solid #E5E7EB;
  border-radius: 0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 360px; /* 减少最大宽度 */
}

.modal-content h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #111827;
  border-left: 3px solid #3A6EA5;
  padding-left: 12px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #4B5563;
  font-weight: 500;
}

.form-group input, .form-group select {
  width: 100%;
  background: #F9FAFB;
  border: 1px solid #D1D5DB;
  color: #111827;
  border-radius: 0;
  padding: 8px;
  font-size: 13px;
}

.form-group input:focus, .form-group select:focus {
  background: white;
  border-color: #3A6EA5;
  outline: none;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn-cancel {
  padding: 6px 16px;
  border: 1px solid #D1D5DB;
  background: white;
  border-radius: 0;
  cursor: pointer;
  color: #374151;
  font-size: 13px;
}

.btn-cancel:hover {
  background: #F3F4F6;
}

.btn-confirm {
  padding: 6px 16px;
  border: none;
  background: #3A6EA5;
  color: white;
  border-radius: 0;
  cursor: pointer;
  font-size: 13px;
}

.btn-confirm:hover {
  background: #2D5A87;
}
</style>
