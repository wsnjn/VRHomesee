<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps(['userPhone'])
const emit = defineEmits(['refresh', 'goToTenantMatching'])

// API基础URL
const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const appointments = ref([])
const loadingAppointments = ref(false)
const selectedStatusFilter = ref('all') // all, 0, 1, 2, 3, 4, 5
const expandedAppointments = ref({}) // 存储每个预约项的展开状态
const searchQuery = ref('') // 搜索查询

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

// 搜索后的租约列表
const searchedAppointments = computed(() => {
  if (!searchQuery.value) {
    return appointments.value
  }
  
  const query = searchQuery.value.toLowerCase()
  return appointments.value.filter(appointment => {
    // 搜索预约编号
    const appointmentNumber = appointment.appointmentNumber ? appointment.appointmentNumber.toLowerCase() : ''
    return appointmentNumber.includes(query)
  })
})

// 筛选后的租约列表
const filteredAppointments = computed(() => {
  return searchedAppointments.value
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

// 切换预约项的展开/折叠状态
const toggleAppointment = (appointmentId) => {
  expandedAppointments.value[appointmentId] = !expandedAppointments.value[appointmentId]
}

// 跳转到租客匹配页面并自动选择
const goToTenantMatching = (appointment) => {
  emit('goToTenantMatching', {
    roomId: appointment.roomId,
    appointmentId: appointment.id,
    contactName: appointment.contactName,
    contactPhone: appointment.contactPhone
  })
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
        <div class="search-wrapper">
          <div class="container">
            <div class="search-container">
              <input class="input" type="text" placeholder="搜索预约编号..." v-model="searchQuery">
              <svg viewBox="0 0 24 24" class="search__icon">
                <g>
                  <path d="M21.53 20.47l-3.66-3.66C19.195 15.24 20 13.214 20 11c0-4.97-4.03-9-9-9s-9 4.03-9 9 4.03 9 9 9c2.215 0 4.24-.804 5.808-2.13l3.66 3.66c.147.146.34.22.53.22s.385-.073.53-.22c.295-.293.295-.767.002-1.06zM3.5 11c0-4.135 3.365-7.5 7.5-7.5s7.5 3.365 7.5 7.5-3.365 7.5-7.5 7.5-7.5-3.365-7.5-7.5z">
                  </path>
                </g>
              </svg>
            </div>
          </div>
        </div>
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
          <div class="appointment-header" @click="toggleAppointment(appointment.id)">
            <div class="appointment-number">
              <strong>预约编号：</strong>{{ appointment.appointmentNumber }}
            </div>
            <div class="header-right">
              <div 
                class="appointment-status clickable-status" 
                :class="getStatusClass(appointment.status)"
                @click.stop="showStatusModal(appointment)"
              >
                {{ getAppointmentStatusText(appointment.status) }}
              </div>
              <div class="expand-icon" :class="{ expanded: expandedAppointments[appointment.id] }">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                  <path d="M8 10.586L3.707 6.293a1 1 0 00-1.414 1.414l5 5a1 1 0 001.414 0l5-5a1 1 0 10-1.414-1.414L8 10.586z"/>
                </svg>
              </div>
            </div>
          </div>

          <div v-if="expandedAppointments[appointment.id]" class="appointment-content">

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

            <!-- 快捷操作：跳转租客匹配（仅已确认状态显示） -->
            <div v-if="appointment.status === 1" class="quick-actions">
              <button class="quick-match-btn" @click="goToTenantMatching(appointment)">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="8.5" cy="7" r="4"></circle>
                  <line x1="20" y1="8" x2="20" y2="14"></line>
                  <line x1="23" y1="11" x2="17" y2="11"></line>
                </svg>
                创建租约合同
              </button>
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
  margin-bottom: 1.5rem;
  padding: 1.25rem 1.5rem;
  background-color: #1e3a5f;
  border-radius: 8px;
}

.page-header h2 {
  margin: 0;
  color: white;
  font-size: 1.5rem;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.search-wrapper {
  margin-right: 0.75rem;
}

.container {
  position: relative;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 6px;
  padding: 0;
  display: grid;
  place-content: center;
  z-index: 0;
  max-width: 240px;
  width: 240px;
}

.search-container {
  position: relative;
  width: 100%;
  border-radius: 6px;
  background-color: white;
  padding: 0;
  display: flex;
  align-items: center;
  border: 1px solid #e9ecef;
}

.search-container::after, .search-container::before {
  display: none;
}

.input {
  padding: 8px 12px;
  width: calc(100% - 40px);
  background: transparent;
  border: none;
  color: #2c3e50;
  font-size: 13px;
  border-radius: 6px;
}

.input::placeholder {
  color: #95a5a6;
}

.input:focus {
  outline: none;
}

.search__icon {
  width: 16px;
  aspect-ratio: 1;
  border: none;
  padding: 0;
  margin-right: 10px;
}

.search__icon path {
  fill: #7f8c8d;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  font-size: 13px;
  transition: background-color 0.2s ease;
  background-color: #1e3a5f;
  color: white;
}

.refresh-btn:hover:not(:disabled) {
  background-color: #2d5a87;
}

.refresh-btn:disabled {
  background-color: #7f8c8d;
  cursor: not-allowed;
  opacity: 0.7;
}

/* 状态筛选器样式 */
.status-filter {
  background: white;
  padding: 1.5rem;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  border: 1px solid #e9ecef;
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
  padding: 0.4rem 0.8rem;
  border: 1px solid #e9ecef;
  border-radius: 2px;
  background: white;
  color: #5a6c7d;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
  font-size: 13px;
}

.filter-btn:hover {
  border-color: #1e3a5f;
  color: #1e3a5f;
}

.filter-btn.active {
  border-color: #1e3a5f;
  background: #1e3a5f;
  color: white;
}

/* 租约列表样式 */
.appointments-container {
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  border: 1px solid #e9ecef;
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
  cursor: pointer;
  transition: all 0.3s;
}

.appointment-header:hover {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin: -1rem -1rem 0 -1rem;
}

.appointment-number {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.expand-icon {
  transition: transform 0.3s ease;
  color: #6c757d;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.clickable-status {
  padding: 0.5rem 1rem;
  border-radius: 2px;
  font-size: 0.8rem;
  font-weight: 600;
  text-align: center;
  min-width: 80px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
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
  gap: 0.75rem;
}

.info-section h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 0.85rem;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.5rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0.4rem 0.6rem;
  background: #f8f9fa;
  border-radius: 4px;
  font-size: 0.8rem;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item .label {
  font-weight: 500;
  color: #495057;
  min-width: 70px;
  margin-right: 0.5rem;
}

.info-item .value {
  color: #2c3e50;
  text-align: left;
  flex: 1;
  word-break: break-word;
}

/* 快捷操作按钮 */
.quick-actions {
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px dashed #e9ecef;
  display: flex;
  gap: 0.5rem;
}

.quick-match-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.8rem;
  background-color: #27ae60;
  color: white;
  border: none;
  border-radius: 2px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.quick-match-btn:hover {
  background-color: #219a52;
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
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
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
