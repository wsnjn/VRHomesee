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
        <div class="card-header">
          <div class="header-left">
            <span class="req-id">#{{ req.id }}</span>
            <span class="req-title">{{ req.requestTitle }}</span>
          </div>
          <!-- 状态进度条 -->
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

        <div class="card-body">
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
        </div>

        <div class="card-footer">
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
.landlord-maintenance {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h3 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.filters {
  display: flex;
  gap: 10px;
  background: white;
  padding: 4px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border: 1px solid #e9ecef;
}

.filter-btn {
  padding: 6px 16px;
  border: none;
  background: none;
  border-radius: 2px;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn.active {
  background: #f3f4f6;
  color: #1f2937;
  font-weight: 500;
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.request-card {
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  border: 1px solid #e9ecef;
  overflow: hidden;
  transition: transform 0.2s;
}

.request-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f9fafb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fcfcfd;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.req-id {
  font-family: monospace;
  color: #9ca3af;
  font-size: 13px;
}

.req-title {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.status-badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 2px;
  font-weight: 500;
}

.status-pending { background: #fff7ed; color: #c2410c; }
.status-processing { background: #eff6ff; color: #1d4ed8; }
.status-completed { background: #f0fdf4; color: #15803d; }
.status-closed { background: #f3f4f6; color: #4b5563; }

/* 进度条样式 */
.status-progress {
  display: flex;
  align-items: center;
  gap: 0;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #e5e7eb;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
}

.progress-step.active .step-dot {
  background: #10b981;
  border-color: #10b981;
}

.progress-step.current .step-dot {
  background: white;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.step-label {
  font-size: 11px;
  color: #9ca3af;
  white-space: nowrap;
}

.progress-step.active .step-label {
  color: #374151;
  font-weight: 500;
}

.progress-step.current .step-label {
  color: #3b82f6;
  font-weight: 600;
}

.progress-line {
  width: 40px;
  height: 2px;
  background: #e5e7eb;
  margin: 0 4px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.progress-line.active {
  background: #10b981;
}

.card-body {
  padding: 20px;
}

.req-desc {
  color: #4b5563;
  margin-bottom: 20px;
  line-height: 1.6;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  background: #f9fafb;
  padding: 16px;
  border-radius: 4px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #9ca3af;
  text-transform: uppercase;
}

.info-item .value {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.card-footer {
  padding: 16px 20px;
  border-top: 1px solid #f9fafb;
  display: flex;
  justify-content: flex-end;
}

.actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 2px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.btn-process {
  background: #eff6ff;
  color: #1d4ed8;
  border-color: #dbeafe;
}

.btn-process:hover {
  background: #dbeafe;
}

.btn-complete {
  background: #f0fdf4;
  color: #15803d;
  border-color: #dcfce7;
}

.btn-complete:hover {
  background: #dcfce7;
}

.btn-close {
  background: white;
  color: #4b5563;
  border-color: #e5e7eb;
}

.btn-close:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 60px 0;
  color: #9ca3af;
}

.spinner {
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 4px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-content h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #1f2937;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 2px;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
}

.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 2px;
  cursor: pointer;
  color: #374151;
}

.btn-confirm {
  padding: 8px 16px;
  border: none;
  background: #1e3a5f;
  color: white;
  border-radius: 2px;
  cursor: pointer;
}

.btn-confirm:hover {
  background: #2d5a87;
}
</style>
