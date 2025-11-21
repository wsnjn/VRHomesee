<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps(['userPhone'])
const emit = defineEmits(['refresh'])

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

// 响应式数据
const appointments = ref([])
const loadingAppointments = ref(false)
const selectedStatusFilter = ref('all') // all, 0, 1, 2, 3, 4, 5

// 状态筛选选项
const statusFilters = [
  { value: 'all', label: '全部状态' },
  { value: '0', label: '待确认' },
  { value: '1', label: '已确认' },
  { value: '2', label: '已完成' },
  { value: '3', label: '已取消' },
  { value: '4', label: '已过期' },
  { value: '5', label: '用户爽约' }
]

// 加载租约列表
const loadAppointments = async () => {
  if (!props.userPhone) {
    console.error('用户手机号不存在')
    return
  }

  loadingAppointments.value = true
  try {
    const url = selectedStatusFilter.value === 'all' 
      ? `${API_BASE_URL}/viewing-appointment/landlord/${props.userPhone}`
      : `${API_BASE_URL}/viewing-appointment/landlord/${props.userPhone}/status/${selectedStatusFilter.value}`
    
    console.log('请求租约列表URL:', url)
    
    const response = await axios.get(url)
    console.log('租约列表响应:', response.data)
    
    if (response.data.success) {
      appointments.value = response.data.appointments || []
      console.log('租约列表数据:', appointments.value)
    } else {
      console.error('获取租约列表失败:', response.data.message)
    }
  } catch (error) {
    console.error('加载租约列表失败:', error)
  } finally {
    loadingAppointments.value = false
  }
}

// 预约状态映射
const getAppointmentStatusText = (status) => {
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消',
    4: '已过期',
    5: '用户爽约'
  }
  return statusMap[status] || '未知状态'
}

// 预约类型映射
const getAppointmentTypeText = (type) => {
  return type === 1 ? '现场看房' : '视频看房'
}

// 状态样式类映射
const getStatusClass = (status) => {
  const statusClassMap = {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-completed',
    3: 'status-cancelled',
    4: 'status-expired',
    5: 'status-missed'
  }
  return statusClassMap[status] || 'status-unknown'
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 时间格式化
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '未设置'
  const date = new Date(dateTimeString)
  return date.toLocaleString('zh-CN')
}

// 筛选后的租约列表
const filteredAppointments = computed(() => {
  return appointments.value
})

// 状态筛选变化处理
const handleStatusFilterChange = () => {
  loadAppointments()
}

// 状态修改相关数据
const updatingStatus = ref(false)
const showStatusModalFlag = ref(false)
const selectedAppointment = ref(null)
const selectedNewStatus = ref('')

// 状态选项
const statusOptions = [
  { value: '0', label: '待确认' },
  { value: '1', label: '已确认' },
  { value: '2', label: '已完成' },
  { value: '3', label: '已取消' },
  { value: '4', label: '已过期' },
  { value: '5', label: '用户爽约' }
]

// 显示状态修改模态框
const showStatusModal = (appointment) => {
  selectedAppointment.value = appointment
  selectedNewStatus.value = appointment.status.toString()
  showStatusModalFlag.value = true
}

// 关闭状态模态框
const closeStatusModal = () => {
  showStatusModalFlag.value = false
  selectedAppointment.value = null
  selectedNewStatus.value = ''
}

// 更新预约状态
const updateAppointmentStatus = async (appointmentId, newStatus) => {
  updatingStatus.value = true
  try {
    const response = await axios.put(`${API_BASE_URL}/viewing-appointment/${appointmentId}/status`, {
      status: parseInt(newStatus)
    })
    
    if (response.data.success) {
      closeStatusModal()
      // 重新加载租约列表
      await loadAppointments()
    } else {
      alert(`状态更新失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('更新预约状态失败:', error)
    alert('状态更新失败，请稍后重试')
  } finally {
    updatingStatus.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadAppointments()
})
</script>

<template>
  <div class="tenant-management">
    <div class="page-header">
      <h2>租约列表</h2>
      <div class="header-actions">
        <button @click="loadAppointments" class="refresh-btn" :disabled="loadingAppointments">
          {{ loadingAppointments ? '刷新中...' : '刷新' }}
        </button>
      </div>
    </div>

    <!-- 状态筛选器 -->
    <div class="status-filter">
      <div class="filter-title">预约状态：</div>
      <div class="filter-buttons">
        <button
          v-for="filter in statusFilters"
          :key="filter.value"
          :class="['filter-btn', { active: selectedStatusFilter === filter.value }]"
          @click="selectedStatusFilter = filter.value; handleStatusFilterChange()"
        >
          {{ filter.label }}
        </button>
      </div>
    </div>

    <!-- 租约列表 -->
    <div class="appointments-container">
      <div v-if="loadingAppointments" class="loading">
        加载中...
      </div>

      <div v-else-if="filteredAppointments.length === 0" class="no-appointments">
        <div class="empty-state">
          <span class="empty-icon">📋</span>
          <h3>暂无租约信息</h3>
          <p>当前没有用户预约您的房源</p>
        </div>
      </div>

      <div v-else class="appointments-list">
        <div v-for="appointment in filteredAppointments" :key="appointment.id" class="appointment-item">
          <div class="appointment-header">
            <div class="appointment-number">
              <strong>预约编号：</strong>{{ appointment.appointmentNumber }}
            </div>
            <div 
              class="appointment-status clickable-status" 
              :class="getStatusClass(appointment.status)"
              @click="showStatusModal(appointment)"
            >
              {{ getAppointmentStatusText(appointment.status) }}
            </div>
          </div>

          <div class="appointment-content">

            <!-- 用户信息 -->
            <div class="info-section">
              <h4>用户信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">联系人：</span>
                  <span class="value">{{ appointment.contactName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系电话：</span>
                  <span class="value">{{ appointment.contactPhone }}</span>
                </div>
                <div v-if="appointment.wechatId" class="info-item">
                  <span class="label">微信号：</span>
                  <span class="value">{{ appointment.wechatId }}</span>
                </div>
                <div v-if="appointment.tenantCount" class="info-item">
                  <span class="label">租客人数：</span>
                  <span class="value">{{ appointment.tenantCount }}人</span>
                </div>
              </div>
            </div>

            <!-- 房屋信息 -->
            <div v-if="appointment.roomInfo" class="info-section">
              <h4>房屋信息</h4>
              <div class="info-grid">
                <div class="info-item full-width">
                  <span class="label">房屋地址：</span>
                  <span class="value">{{ appointment.roomInfo.address }}</span>
                </div>
              </div>
            </div>

            <!-- 预约信息 -->
            <div class="info-section">
              <h4>预约信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">预约类型：</span>
                  <span class="value">{{ getAppointmentTypeText(appointment.appointmentType) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">期望日期：</span>
                  <span class="value">{{ formatDate(appointment.preferredDate) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">时间段：</span>
                  <span class="value">{{ appointment.preferredTimeSlot }}</span>
                </div>
                <div v-if="appointment.actualDate" class="info-item">
                  <span class="label">实际日期：</span>
                  <span class="value">{{ formatDate(appointment.actualDate) }}</span>
                </div>
                <div v-if="appointment.actualTimeSlot" class="info-item">
                  <span class="label">实际时间段：</span>
                  <span class="value">{{ appointment.actualTimeSlot }}</span>
                </div>
              </div>
            </div>

            <!-- 租房意向 -->
            <div v-if="appointment.expectedMoveInDate || appointment.rentalIntention" class="info-section">
              <h4>租房意向</h4>
              <div class="info-grid">
                <div v-if="appointment.expectedMoveInDate" class="info-item">
                  <span class="label">期望入住：</span>
                  <span class="value">{{ formatDate(appointment.expectedMoveInDate) }}</span>
                </div>
                <div v-if="appointment.rentalIntention" class="info-item full-width">
                  <span class="label">租房意向：</span>
                  <span class="value">{{ appointment.rentalIntention }}</span>
                </div>
              </div>
            </div>

            <!-- 时间信息 -->
            <div class="info-section">
              <h4>时间信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">创建时间：</span>
                  <span class="value">{{ formatDateTime(appointment.appointmentCreatedTime) }}</span>
                </div>
                <div v-if="appointment.confirmedTime" class="info-item">
                  <span class="label">确认时间：</span>
                  <span class="value">{{ formatDateTime(appointment.confirmedTime) }}</span>
                </div>
                <div v-if="appointment.completedTime" class="info-item">
                  <span class="label">完成时间：</span>
                  <span class="value">{{ formatDateTime(appointment.completedTime) }}</span>
                </div>
                <div v-if="appointment.cancelledTime" class="info-item">
                  <span class="label">取消时间：</span>
                  <span class="value">{{ formatDateTime(appointment.cancelledTime) }}</span>
                </div>
              </div>
            </div>

            <!-- 备注信息 -->
            <div v-if="appointment.agentNotes || appointment.adminNotes" class="info-section">
              <h4>备注信息</h4>
              <div class="info-grid">
                <div v-if="appointment.agentNotes" class="info-item full-width">
                  <span class="label">业务员备注：</span>
                  <span class="value">{{ appointment.agentNotes }}</span>
                </div>
                <div v-if="appointment.adminNotes" class="info-item full-width">
                  <span class="label">管理员备注：</span>
                  <span class="value">{{ appointment.adminNotes }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 状态修改弹窗 -->
    <div v-if="showStatusModalFlag" class="modal-overlay">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h3>修改预约状态</h3>
            <button @click="closeStatusModal" class="close-btn">&times;</button>
          </div>
          <div class="modal-body">
            <p>预约编号：<strong>{{ selectedAppointment?.appointmentNumber }}</strong></p>
            <p>当前状态：<span :class="getStatusClass(selectedAppointment?.status)">{{ getAppointmentStatusText(selectedAppointment?.status) }}</span></p>
            
            <div class="status-options">
              <h4>选择新状态：</h4>
              <div class="status-buttons">
                <button 
                  v-for="status in statusOptions" 
                  :key="status.value"
                  :class="['status-btn', getStatusClass(status.value), { active: selectedNewStatus === status.value }]"
                  @click="selectedNewStatus = status.value"
                >
                  {{ status.label }}
                </button>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="closeStatusModal" class="btn btn-secondary">取消</button>
            <button 
              @click="updateAppointmentStatus(selectedAppointment.id, selectedNewStatus)" 
              class="btn btn-primary" 
              :disabled="updatingStatus || !selectedNewStatus"
            >
              {{ updatingStatus ? '更新中...' : '确认修改' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tenant-management {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.refresh-btn {
  background-color: #17a2b8;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background-color: #138496;
}

.refresh-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

/* 状态筛选器样式 */
.status-filter {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.filter-title {
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
}

.filter-buttons {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 0.5rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 20px;
  background: white;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.filter-btn:hover {
  border-color: #28a745;
  color: #28a745;
}

.filter-btn.active {
  border-color: #28a745;
  background: #28a745;
  color: white;
}

/* 租约列表样式 */
.appointments-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.loading, .no-appointments {
  text-align: center;
  color: #666;
  padding: 3rem;
  font-style: italic;
}

.empty-state {
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
}

.empty-state p {
  color: #6c757d;
  margin-bottom: 0;
}

.appointments-list {
  max-height: 800px;
  overflow-y: auto;
}

.appointment-item {
  border-bottom: 1px solid #e9ecef;
  padding: 1.5rem;
  transition: background-color 0.3s;
}

.appointment-item:hover {
  background-color: #f8f9fa;
}

.appointment-item:last-child {
  border-bottom: none;
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f8f9fa;
}

.appointment-number {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1rem;
}

.clickable-status {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-align: center;
  min-width: 80px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s;
}

.clickable-status:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.status-pending {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.status-confirmed {
  background-color: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.status-completed {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-cancelled {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.status-expired {
  background-color: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

.status-missed {
  background-color: #f5e6e8;
  color: #721c24;
  border: 1px solid #f1b0b7;
}

.status-unknown {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #e9ecef;
}

.appointment-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-section h4 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1rem;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 0.9rem;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item .label {
  font-weight: 500;
  color: #495057;
  min-width: 100px;
  margin-right: 1rem;
}

.info-item .value {
  color: #2c3e50;
  text-align: right;
  flex: 1;
  word-break: break-word;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.modal-dialog {
  width: 90%;
  max-width: 500px;
  animation: slideIn 0.3s ease;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.3rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #6c757d;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: #e9ecef;
  color: #495057;
}

.modal-body {
  padding: 1.5rem;
}

.modal-body p {
  margin: 0 0 1rem 0;
  color: #495057;
  font-size: 0.95rem;
}

.modal-body p:last-child {
  margin-bottom: 1.5rem;
}

.status-options h4 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.status-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.status-btn {
  padding: 0.75rem;
  border: 2px solid transparent;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  font-size: 0.9rem;
}

.status-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.status-btn.active {
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #5a6268;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn:disabled {
  background-color: #adb5bd;
  cursor: not-allowed;
  opacity: 0.7;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    transform: translateY(-50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .status-filter {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-buttons {
    width: 100%;
  }

  .filter-btn {
    flex: 1;
    text-align: center;
  }

  .appointment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .info-item .label {
    min-width: auto;
  }

  .info-item .value {
    text-align: left;
  }

  /* 移动端弹窗适配 */
  .modal-dialog {
    width: 95%;
    margin: 1rem;
  }

  .status-buttons {
    grid-template-columns: 1fr;
  }

  .modal-footer {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
