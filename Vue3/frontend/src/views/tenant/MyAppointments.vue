<template>
  <div class="appointments-container">
    <Navbar />
    <div class="page-header-spacer"></div>

    <main class="main-content">
      <div v-if="!isLoggedIn" class="login-prompt">
        <p>请先登录以查看预约信息</p>
        <button @click="goBack" class="btn-primary">返回首页</button>
      </div>

      <div v-else class="content-grid">
        <!-- Left Column: House Info -->
        <div class="left-column">
          <div class="section-title">
            <h3>当前房屋 (CURRENT HOUSE)</h3>
          </div>
          
          <div class="house-card" v-if="activeLease">
            <div class="card-header">
              <h4 class="card-title">租约: {{ activeLease.contractNumber }}</h4>
              <span class="status-badge" :class="getLeaseStatusClass(activeLease.contractStatus)">{{ getLeaseStatusText(activeLease.contractStatus) }}</span>
            </div>
            
            <div class="info-grid">
              <div class="info-item">
                <span class="label">租期</span>
                <span class="value">{{ formatDate(activeLease.contractStartDate) }} - {{ formatDate(activeLease.contractEndDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">月租金</span>
                <span class="value price">¥{{ activeLease.monthlyRent }}<span class="unit">/月</span></span>
              </div>
              <div class="info-item">
                <span class="label">付款方式</span>
                <span class="value">{{ getPaymentMethodText(activeLease.paymentCycle) }}</span>
              </div>
            </div>
            
            <!-- Utilities -->
            <div class="subsection">
              <h5 class="subsection-title">水电费用</h5>
              <div class="info-grid compact">
                <div class="info-item">
                  <span class="label">水费单价</span>
                  <span class="value">{{ houseDetails?.waterPrice ? '¥' + houseDetails.waterPrice : '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">电费单价</span>
                  <span class="value">{{ houseDetails?.electricPrice ? '¥' + houseDetails.electricPrice : '-' }}</span>
                </div>
              </div>
            </div>

            <!-- House Details -->
            <div class="subsection" v-if="houseDetails">
              <h5 class="subsection-title">房屋详情</h5>
              <div class="info-grid compact">
                <div class="info-item full">
                  <span class="label">地址</span>
                  <span class="value">{{ getFullAddress(houseDetails) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">面积</span>
                  <span class="value">{{ houseDetails.roomArea || '-' }}㎡</span>
                </div>
                <div class="info-item">
                  <span class="label">楼层</span>
                  <span class="value">{{ houseDetails.floorInfo || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-data-card">
            <p>暂无有效租约</p>
          </div>
        </div>

        <!-- Right Column: Appointments List -->
        <div class="right-column">
          <div class="section-title">
             <h3>预约列表 (APPOINTMENTS)</h3>
          </div>

          <div class="appointments-content">
            <div v-if="loadingAppointments" class="loading">
              加载中...
            </div>
            
            <div v-else-if="appointments.length === 0" class="no-data-card">
              <p>暂无预约记录</p>
            </div>
            
            <div v-else class="appointments-list">
              <div v-for="appointment in paginatedAppointments" :key="appointment.id" class="appointment-card">
                <div class="card-header">
                  <span class="appointment-number">#{{ appointment.appointmentNumber }}</span>
                  <span class="status-badge" :class="getStatusClass(appointment.status)">
                    {{ getAppointmentStatusText(appointment.status) }}
                  </span>
                </div>
                
                <div class="card-body">
                  <div class="detail-row">
                    <span class="label">类型</span>
                    <span class="value">{{ getAppointmentTypeText(appointment.appointmentType) }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">日期</span>
                    <span class="value highlight">{{ formatDate(appointment.preferredDate) }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">时间</span>
                    <span class="value">{{ appointment.preferredTimeSlot }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">联系人</span>
                    <span class="value">{{ appointment.contactName }} ({{ appointment.contactPhone }})</span>
                  </div>
                </div>
              </div>
              
              <!-- Pagination -->
              <div class="pagination-controls" v-if="totalPages > 1">
                <button 
                  class="btn-secondary small" 
                  :disabled="currentPage === 1"
                  @click="changePage(currentPage - 1)"
                >
                  上一页
                </button>
                <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
                <button 
                  class="btn-secondary small" 
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

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const user = ref(null)
const appointments = ref([])
const loadingAppointments = ref(false)
const activeLease = ref(null)
const houseDetails = ref(null)
const currentPage = ref(1)
const pageSize = 5

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
    fetchUserAppointments()
    fetchActiveLease()
  }
})

const isLoggedIn = computed(() => user.value !== null)

const fetchUserAppointments = async () => {
  if (!user.value) return
  loadingAppointments.value = true
  try {
    const url = `https://api.homesee.xyz/api/viewing-appointment/user/${user.value.id}`
    const response = await fetch(url)
    if (response.ok) {
      const data = await response.json()
      if (data.success) appointments.value = data.appointments
    }
  } catch (error) {
    console.error(error)
  } finally {
    loadingAppointments.value = false
  }
}

const fetchActiveLease = async () => {
  if (!user.value) return
  try {
    const res = await fetch(`https://api.homesee.xyz/api/admin/tenant/tenant/${user.value.id}`)
    const data = await res.json()
    if (data.success && data.contracts && data.contracts.length > 0) {
      const active = data.contracts.find(c => c.contractStatus === 1 || c.contractStatus === 2)
      if (active) {
        activeLease.value = active
        if (active.roomId) await fetchHouseDetails(active.roomId)
      }
    }
  } catch (e) { console.error(e) }
}

const fetchHouseDetails = async (roomId) => {
  try {
    const res = await fetch(`https://api.homesee.xyz/api/room-info/${roomId}`)
    const data = await res.json()
    if (data.success && data.room) houseDetails.value = data.room
  } catch (e) { console.error(e) }
}

/* Helpers & Formatters */
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const goBack = () => router.push('/')

const getAppointmentStatusText = (status) => {
  const map = { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消', 4: '已过期', 5: '用户爽约' }
  return map[status] || '未知状态'
}

const getAppointmentTypeText = (type) => type === 1 ? '现场看房' : '视频看房'

const getStatusClass = (status) => {
  const map = { 
    0: 'status-pending', 1: 'status-blue', 2: 'status-green', 
    3: 'status-red', 4: 'status-gray', 5: 'status-red' 
  }
  return map[status] || 'status-gray'
}

const getLeaseStatusText = (status) => {
  const map = { 0: '待签约', 1: '已签约', 2: '履行中', 3: '已到期', 4: '提前解约', 5: '已退租' }
  return map[status] || '未知状态'
}

const getLeaseStatusClass = (status) => {
  const map = { 0: 'status-pending', 1: 'status-green', 2: 'status-green', 3: 'status-gray', 4: 'status-red', 5: 'status-gray' }
  return map[status] || ''
}

const getPaymentMethodText = (m) => ['月付', '季付', '半年付', '年付'][m] || '未知'
const getFullAddress = (h) => h ? `${h.city} ${h.district} ${h.communityName}` : ''
</script>

<style scoped>
.appointments-container {
  min-height: 100vh;
  background-color: #FFFFFF;
  font-family: 'Inter', sans-serif;
  color: #111827;
}

.page-header-spacer { height: 64px; }

.main-content {
  padding: 40px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.content-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 40px;
  align-items: start;
}

@media (max-width: 900px) {
  .content-grid { grid-template-columns: 1fr; }
}

/* Typography */
.section-title h3 {
  font-size: 0.85rem;
  font-weight: 700;
  color: #9CA3AF;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 16px;
}

.subsection-title {
  font-size: 0.75rem;
  font-weight: 700;
  color: #9CA3AF;
  margin-bottom: 12px;
  margin-top: 24px;
  letter-spacing: 0.5px;
}

/* Cards */
.house-card, .appointment-card, .no-data-card, .login-prompt {
  background: white;
  border: 1px solid #E5E7EB;
  padding: 24px;
  border-radius: 0;
}

.no-data-card, .login-prompt {
  text-align: center;
  color: #6B7280;
  border-style: dashed;
  font-size: 0.9rem;
}

/* Card Headers */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F3F4F6;
}

.card-title {
  font-size: 1rem;
  font-weight: 800;
  margin: 0;
  color: #111827;
}

.appointment-number {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 700;
  font-size: 1rem;
}

/* Status Badges */
.status-badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 2px 8px;
  border: 1px solid currentColor;
  border-radius: 0;
}

.status-pending { color: #D97706; }
.status-green { color: #059669; }
.status-blue { color: #2563EB; }
.status-red { color: #DC2626; }
.status-gray { color: #6B7280; }

/* Info Grids */
.info-grid {
  display: grid;
  gap: 12px;
}

.info-grid.compact {
  grid-template-columns: 1fr 1fr;
  gap: x 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full { grid-column: span 2; }

.label {
  font-size: 0.7rem;
  color: #9CA3AF;
  text-transform: uppercase;
  font-weight: 600;
}

.value {
  font-size: 0.9rem;
  color: #111827;
  font-weight: 500;
}

.value.price {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 700;
}

.value.highlight {
  font-weight: 700; 
}

.unit {
  font-size: 0.7rem;
  color: #6B7280;
  font-weight: 400;
}

/* Appointment List */
.appointments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.9rem;
}

/* Buttons */
.btn-primary {
  background: #111827;
  color: white;
  border: none;
  padding: 12px 24px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 0;
  margin-top: 16px;
}

.btn-secondary {
  background: white;
  border: 1px solid #E5E7EB;
  color: #111827;
  padding: 8px 16px;
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
  border-radius: 0;
  transition: all 0.2s;
}

.btn-secondary:hover:not(:disabled) {
  border-color: #111827;
}

.btn-secondary:disabled {
  color: #D1D5DB;
  border-color: #F3F4F6;
  cursor: not-allowed;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.page-info {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.9rem;
  color: #6B7280;
}
</style>
