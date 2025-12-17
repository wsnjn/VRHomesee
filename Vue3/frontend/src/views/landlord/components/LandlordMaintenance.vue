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
          <!-- Header Content Merged Here -->
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

          <!-- Actions Merged Here -->
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

    <!-- Complete Modal -->
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
    console.error('Failed to load maintenance requests:', e)
  } finally {
    loading.value = false
  }
}

const openCompleteModal = (req) => {
  completeForm.value = {
    id: req.id,
    repairCost: 0,
    costBearer: 1,
    req: req // Keep reference to update local state
  }
  showCompleteModal.value = true
}

const submitComplete = async () => {
  const req = completeForm.value.req
  try {
    const updateData = {
      id: req.id,
      requestStatus: 2, // Completed
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
      // Update local state
      req.requestStatus = 2
      req.repairCost = completeForm.value.repairCost
      req.costBearer = completeForm.value.costBearer
      req.actualFixDate = updateData.actualFixDate
      showCompleteModal.value = false
    } else {
      alert('更新失败')
    }
  } catch (e) {
    console.error('Failed to complete request:', e)
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
    console.error('Failed to update status:', e)
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
/* Personal Development Style - Light Industrial (Compact) */
.landlord-maintenance {
  padding: 16px; /* Reduced from 24px */
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
  margin-bottom: 16px; /* Reduced */
  border-bottom: 1px solid #E5E7EB;
  padding-bottom: 16px; /* Reduced */
}

.header h3 {
  font-size: 18px; /* Slightly smaller */
  font-weight: 600;
  color: #111827;
  margin: 0;
  letter-spacing: -0.5px;
}

/* Filters - Toolbar Style */
.filters {
  display: flex;
  gap: -1px;
  background: transparent;
  padding: 0;
  border: 1px solid #E5E7EB;
}

.filter-btn {
  padding: 6px 16px; /* Reduced padding */
  border: none;
  border-right: 1px solid #E5E7EB;
  background: white;
  border-radius: 0;
  font-size: 12px; /* Smaller font */
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

/* Request List */
.request-list {
  display: flex;
  flex-direction: column;
  gap: 12px; /* Reduced gap */
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
  gap: 12px; /* Reduced gap */
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

/* Status Badges - Minimal */
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

/* Progress Bar - Linear Square */
.status-progress {
  display: flex;
  align-items: center;
}

.progress-step {
  display: flex;
  align-items: center;
  gap: 6px; /* Reduced */
}

.step-dot {
  width: 6px; /* Smaller */
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
  width: 16px; /* Shorter line */
  height: 1px;
  background: #E5E7EB;
  margin: 0 6px;
}

.progress-line.active {
  background: #9CA3AF;
}

/* Card Body */
.card-body {
  padding: 16px; /* Reduced padding */
}

.req-desc {
  color: #374151;
  margin-bottom: 16px; /* Reduced margin */
  font-size: 13px; /* Smaller font */
  line-height: 1.5;
  background: #F9FAFB;
  padding: 10px; /* Reduced padding */
  border: 1px solid #E5E7EB;
  border-radius: 0;
  font-family: 'JetBrains Mono', monospace;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px; /* Reduced gap */
  background: transparent;
  padding: 0;
  border: none;
  margin-bottom: 16px; /* Added margin before actions */
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px; /* Reduced gap */
  border-left: 1px solid #E5E7EB; /* Thinner border */
  padding-left: 12px; /* Reduced padding */
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

/* Actions merged into body */
.actions {
  display: flex;
  gap: 8px; /* Reduced gap */
  justify-content: flex-end;
  border-top: 1px solid #F3F4F6;
  padding-top: 12px;
}

.action-btn {
  padding: 6px 16px; /* Reduced padding */
  border-radius: 0;
  font-size: 12px; /* Smaller font */
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

/* Empty & Loading */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0; /* Reduced padding */
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

/* Modal */
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
  padding: 20px; /* Reduced */
  border: 1px solid #E5E7EB;
  border-radius: 0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 360px; /* Reduced max-width */
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
