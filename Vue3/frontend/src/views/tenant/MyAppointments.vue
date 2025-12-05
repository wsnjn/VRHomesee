<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()

// 从localStorage获取用户信息
const user = ref(null)

// 预约信息
const appointments = ref([])
const loadingAppointments = ref(false)

// 房屋和租约信息
const activeLease = ref(null)
const houseDetails = ref(null)

// 分页状态
const currentPage = ref(1)
const pageSize = 2

const paginatedAppointments = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return appointments.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(appointments.value.length / pageSize)
})

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
    // 如果用户已登录，获取预约信息和房屋信息
    fetchUserAppointments()
    fetchActiveLease()
  }
})

// 计算属性：是否已登录
const isLoggedIn = computed(() => {
  return user.value !== null
})

// 获取用户预约信息
const fetchUserAppointments = async () => {
  if (!user.value) {
    console.log('用户未登录，无法获取预约信息')
    return
  }
  
  loadingAppointments.value = true
  try {
    const url = `https://api.homesee.xyz/api/viewing-appointment/user/${user.value.id}`
    
    const response = await fetch(url)
    
    if (response.ok) {
      const data = await response.json()
      
      if (data.success) {
        appointments.value = data.appointments
      } else {
        console.error('获取预约信息失败:', data.message)
      }
    } else {
      console.error('获取预约信息失败，HTTP状态码:', response.status)
    }
  } catch (error) {
    console.error('获取预约信息时出错:', error)
  } finally {
    loadingAppointments.value = false
  }
}

// 获取当前租约信息
const fetchActiveLease = async () => {
  if (!user.value) return
  try {
    const res = await fetch(`https://api.homesee.xyz/api/admin/tenant/tenant/${user.value.id}`)
    const data = await res.json()
    if (data.success && data.contracts && data.contracts.length > 0) {
      // Find the active contract (status 1=Signed or 2=In Progress)
      const active = data.contracts.find(c => c.contractStatus === 1 || c.contractStatus === 2)
      if (active) {
        activeLease.value = active
        // Fetch house details if roomId is available
        if (active.roomId) {
          await fetchHouseDetails(active.roomId)
        }
      }
    }
  } catch (e) {
    console.error(e)
  }
}

// 获取房屋详细信息
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

// 返回首页
const goBack = () => {
  router.push('/')
}

// 辅助函数 (来自 Maintenance.vue)
const formatPhoneNumber = (phone) => {
  if (!phone) return ''
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`
  }
  return phone
}

const getLeaseStatusText = (status) => {
  const map = { 0: '待签约', 1: '已签约', 2: '履行中', 3: '已到期', 4: '提前解约', 5: '已退租' }
  return map[status] || '未知状态'
}

const getLeaseStatusClass = (status) => {
  const map = { 
    0: 'status-pending', 
    1: 'status-active', 
    2: 'status-active', 
    3: 'status-expired', 
    4: 'status-terminated',
    5: 'status-terminated'
  }
  return map[status] || ''
}

const getPaymentMethodText = (method) => {
  const map = { 0: '月付', 1: '季付', 2: '半年付', 3: '年付' }
  return map[method] || '未知'
}

const getDepositStatusText = (status) => {
  const map = { 0: '未付', 1: '已付', 2: '已退', 3: '抵扣中' }
  return map[status] || '未知'
}

const getDecorationText = (decoration) => {
  const types = { 1: '毛坯', 2: '简装', 3: '精装', 4: '豪装' }
  return types[decoration] || '未知'
}

const getOrientationText = (orientation) => {
  const map = { 1: '东', 2: '南', 3: '西', 4: '北', 5: '东南', 6: '西南', 7: '东北', 8: '西北' }
  return map[orientation] || '未知'
}

const getRentalTypeText = (type) => {
  const types = { 0: '整租', 1: '合租', 2: '单间' }
  return types[type] || '未知'
}

const getFullAddress = (house) => {
  if (!house) return ''
  const parts = [house.province, house.city, house.district, house.street, house.communityName]
  return parts.filter(part => part).join('')
}
</script>

<template>
  <div class="appointments-container">
    <Navbar />
    <div class="page-header-spacer"></div>

    <!-- 主内容区域 -->
    <main class="main-content">

      
      <div v-if="!isLoggedIn" class="login-prompt">
        <p>请先登录以查看预约信息</p>
        <button @click="goBack" class="login-btn">返回首页登录</button>
      </div>

      <div v-else class="content-grid">
        <!-- Left Column: House Info (From Maintenance.vue) -->
        <div class="left-column">
          <div class="section-title">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
            <h3>当前房屋信息</h3>
          </div>
          
          <div class="lease-card" v-if="activeLease">
            <div class="lease-header">
              <h4 class="lease-title">当前租约信息</h4>
              <span class="lease-status" :class="getLeaseStatusClass(activeLease.contractStatus)">{{ getLeaseStatusText(activeLease.contractStatus) }}</span>
            </div>
            
            <div class="lease-info-grid">
              <div class="info-item">
                <span class="label">合同编号</span>
                <span class="value">{{ activeLease.contractNumber }}</span>
              </div>

              <div class="info-item">
                <span class="label">租期</span>
                <span class="value">{{ formatDate(activeLease.contractStartDate) }} 至 {{ formatDate(activeLease.contractEndDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">月租金</span>
                <span class="value">¥{{ activeLease.monthlyRent }}</span>
              </div>
              <div class="info-item">
                <span class="label">押金</span>
                <span class="value">¥{{ activeLease.depositAmount ?? 0 }}</span>
              </div>
              <div class="info-item">
                <span class="label">付款方式</span>
                <span class="value">{{ getPaymentMethodText(activeLease.paymentCycle) }}</span>
              </div>
              <div class="info-item">
                <span class="label">押金状态</span>
                <span class="value">{{ getDepositStatusText(activeLease.depositStatus) }}</span>
              </div>
              <div class="info-item">
                <span class="label">合同状态</span>
                <span class="value">{{ getLeaseStatusText(activeLease.contractStatus) }}</span>
              </div>
              <div class="info-item">
                <span class="label">签约日期</span>
                <span class="value">{{ formatDate(activeLease.contractSignedTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">合同备注</span>
                <span class="value">{{ activeLease.contractNotes || '无备注' }}</span>
              </div>
            </div>
            
            <!-- 水电费用信息 -->
            <div class="house-details-section">
              <h5 class="section-subtitle">水电费用信息</h5>
              <div class="house-details-grid">
                <div class="detail-item">
                  <span class="label">水费单价</span>
                  <span class="value">{{ houseDetails?.waterPrice ? '¥' + houseDetails.waterPrice + '/吨' : '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">电费单价</span>
                  <span class="value">{{ houseDetails?.electricPrice ? '¥' + houseDetails.electricPrice + '/度' : '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">上期水表读数</span>
                  <span class="value">{{ activeLease.lastWaterReading ?? 0 }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">上期电表读数</span>
                  <span class="value">{{ activeLease.lastElectricReading ?? 0 }}</span>
                </div>
              </div>
            </div>

            <!-- 房屋详细信息 -->
            <div class="house-details-section" v-if="houseDetails">
              <h5 class="section-subtitle">房屋详细信息</h5>
              <div class="house-details-grid">
                <div class="detail-item">
                  <span class="label">小区名称</span>
                  <span class="value">{{ houseDetails.communityName || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">详细地址</span>
                  <span class="value">{{ getFullAddress(houseDetails) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">房屋面积</span>
                  <span class="value">{{ houseDetails.roomArea || '未知' }}㎡</span>
                </div>
                <div class="detail-item">
                  <span class="label">楼层信息</span>
                  <span class="value">{{ houseDetails.floorInfo || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">装修程度</span>
                  <span class="value">{{ getDecorationText(houseDetails.decoration) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">朝向</span>
                  <span class="value">{{ getOrientationText(houseDetails.orientation) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">租赁类型</span>
                  <span class="value">{{ getRentalTypeText(houseDetails.rentalType) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">房东电话</span>
                  <span class="value">{{ houseDetails.landlordPhone ? formatPhoneNumber(houseDetails.landlordPhone) : '未提供' }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-data-card">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="9" y1="9" x2="15" y2="15"></line><line x1="15" y1="9" x2="9" y2="15"></line></svg>
            <p>暂无有效租约</p>
          </div>
        </div>

        <!-- Right Column: Appointments List -->
        <div class="right-column">
          <div class="section-title">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
            <h3>预约列表</h3>
          </div>

          <div class="appointments-content">
            <div v-if="loadingAppointments" class="loading">
              加载中...
            </div>
            
            <div v-else-if="appointments.length === 0" class="no-appointments">
              <p>暂无预约信息</p>
              <p>您可以前往房源选择页面预约看房</p>
            </div>
            
            <div v-else class="appointments-list">
              <div v-for="appointment in paginatedAppointments" :key="appointment.id" class="appointment-item">
                <div class="appointment-header">
                  <span class="appointment-number">预约编号: {{ appointment.appointmentNumber }}</span>
                  <span class="appointment-status" :class="getStatusClass(appointment.status)">
                    {{ getAppointmentStatusText(appointment.status) }}
                  </span>
                </div>
                <div class="appointment-details">
                  <div class="detail-row">
                    <span class="label">预约类型:</span>
                    <span>{{ getAppointmentTypeText(appointment.appointmentType) }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">期望日期:</span>
                    <span>{{ formatDate(appointment.preferredDate) }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">时间段:</span>
                    <span>{{ appointment.preferredTimeSlot }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">联系人:</span>
                    <span>{{ appointment.contactName }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">联系电话:</span>
                    <span>{{ appointment.contactPhone }}</span>
                  </div>
                  <div v-if="appointment.wechatId" class="detail-row">
                    <span class="label">微信号:</span>
                    <span>{{ appointment.wechatId }}</span>
                  </div>
                  <div v-if="appointment.tenantCount" class="detail-row">
                    <span class="label">租客人数:</span>
                    <span>{{ appointment.tenantCount }}人</span>
                  </div>
                  <div v-if="appointment.expectedMoveInDate" class="detail-row">
                    <span class="label">期望入住日期:</span>
                    <span>{{ formatDate(appointment.expectedMoveInDate) }}</span>
                  </div>
                  <div v-if="appointment.rentalIntention" class="detail-row">
                    <span class="label">租房意向:</span>
                    <span>{{ appointment.rentalIntention }}</span>
                  </div>
                </div>
              </div>
              
              <!-- Pagination Controls -->
              <div class="pagination-controls" v-if="totalPages > 1">
                <button 
                  class="page-btn" 
                  :disabled="currentPage === 1"
                  @click="changePage(currentPage - 1)"
                >
                  上一页
                </button>
                <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
                <button 
                  class="page-btn" 
                  :disabled="currentPage === totalPages"
                  @click="changePage(currentPage + 1)"
                >
                  下一页
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.appointments-container {
  min-height: 100vh;
  background-color: #f8fafc;
}

.page-header-spacer {
  height: 80px;
}

.main-content {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Inter', sans-serif;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  color: #333;
  font-size: 24px;
  font-weight: 700;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  /* align-items: start; Removed to allow stretching */
}

@media (max-width: 900px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

.left-column, .right-column {
  display: flex;
  flex-direction: column;
}

/* Sidebar Styles (From Maintenance.vue) */
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  color: #4b5563;
}

.section-title h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.lease-card {
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  border: 1px solid rgba(0,0,0,0.05);
  transition: transform 0.2s;
}

.lease-card:hover {
  transform: translateY(-2px);
}

.lease-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f1f3f5;
}

.lease-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.lease-status {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.status-active { background: #f0fdf4; color: #15803d; }
.status-pending { background: #fff7ed; color: #c2410c; }
.status-expired { background: #f3f4f6; color: #6b7280; }
.status-terminated { background: #fef2f2; color: #dc2626; }

.lease-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.house-details-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f1f3f5;
}

.section-subtitle {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.house-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.detail-item, .info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-item .label, .info-item .label {
  font-size: 11px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.detail-item .value, .info-item .value {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
}

.no-data-card {
  background: #f9fafb;
  padding: 40px;
  border-radius: 16px;
  text-align: center;
  color: #9ca3af;
  border: 2px dashed #e5e7eb;
}

/* Appointments List Styles */
.appointments-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  border: 1px solid rgba(0,0,0,0.05);
  padding: 25px;
  flex: 1; /* Fill height */
  display: flex;
  flex-direction: column;
}

.loading, .no-appointments {
  text-align: center;
  color: #666;
  padding: 2rem;
  font-style: italic;
}

.appointments-list {
  /* max-height: 600px; Removed to allow filling space */
  flex: 1;
  overflow-y: auto;
}

.appointment-item {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  border: 1px solid #e9ecef;
  transition: all 0.2s;
}

.appointment-item:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid #dee2e6;
}

.appointment-number {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1rem;
}

.appointment-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-pending { background-color: #fff3cd; color: #856404; border: 1px solid #ffeaa7; }
.status-confirmed { background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }
.status-completed { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
.status-cancelled { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
.status-expired { background-color: #e2e3e5; color: #383d41; border: 1px solid #d6d8db; }
.status-missed { background-color: #f5e6e8; color: #721c24; border: 1px solid #f1b0b7; }
.status-unknown { background-color: #f8f9fa; color: #6c757d; border: 1px solid #e9ecef; }

.appointment-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.detail-row .label {
  font-weight: 500;
  color: #495057;
  min-width: 120px;
}

.detail-row span:last-child {
  color: #6c757d;
  text-align: right;
  flex: 1;
}

.login-prompt {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login-btn {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.login-btn:hover {
  background-color: #0056b3;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f1f3f5;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #374151;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f9fafb;
}

.page-info {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}
</style>
