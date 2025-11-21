<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 从localStorage获取用户信息
const user = ref(null)

// 预约信息
const appointments = ref([])
const loadingAppointments = ref(false)

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
    // 如果用户已登录，获取预约信息
    fetchUserAppointments()
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
  
  console.log('当前用户ID:', user.value.id)
  console.log('用户信息:', user.value)
  
  loadingAppointments.value = true
  try {
    const url = `http://localhost:8080/api/viewing-appointment/user/${user.value.id}`
    console.log('请求URL:', url)
    
    const response = await fetch(url)
    console.log('响应状态:', response.status, response.statusText)
    
    if (response.ok) {
      const data = await response.json()
      console.log('后端返回数据:', data)
      
      if (data.success) {
        appointments.value = data.appointments
        console.log('预约信息列表:', data.appointments)
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
</script>

<template>
  <div class="appointments-container">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="logo-container">
        <img src="@/assets/logo/logo (1).png" alt="HOMSEE Logo" class="nav-logo">
      </div>
      <div class="nav-content">
        <button @click="goBack" class="back-btn">返回首页</button>
      </div>
    </nav>

    <!-- 主内容区域 -->
    <main class="main-content">
      <h1>我的预约信息</h1>
      
      <div v-if="!isLoggedIn" class="login-prompt">
        <p>请先登录以查看预约信息</p>
        <button @click="goBack" class="login-btn">返回首页登录</button>
      </div>

      <div v-else class="appointments-content">
        <div v-if="loadingAppointments" class="loading">
          加载中...
        </div>
        
        <div v-else-if="appointments.length === 0" class="no-appointments">
          <p>暂无预约信息</p>
          <p>您可以前往房源选择页面预约看房</p>
        </div>
        
        <div v-else class="appointments-list">
          <div v-for="appointment in appointments" :key="appointment.id" class="appointment-item">
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
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.appointments-container {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.navbar {
  display: flex;
  align-items: center;
  background-color: #9ac7e0;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  height: 70px;
}

.logo-container {
  width: 94px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0;
  border-right: 1px solid rgba(255, 255, 255, 0.2);
}

.nav-logo {
  height: 70px;
  width: auto;
  object-fit: contain;
  margin: 0;
  padding: 0;
}

.nav-content {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 2rem;
}

.back-btn {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.back-btn:hover {
  background-color: #0056b3;
}

.main-content {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.main-content h1 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.login-prompt {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login-prompt p {
  margin-bottom: 1.5rem;
  color: #666;
  font-size: 1.1rem;
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

.appointments-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.loading, .no-appointments {
  text-align: center;
  color: #666;
  padding: 2rem;
  font-style: italic;
}

.no-appointments p {
  margin-bottom: 1rem;
}

.appointments-list {
  max-height: 600px;
  overflow-y: auto;
}

.appointment-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1rem;
  border: 1px solid #e9ecef;
  transition: box-shadow 0.3s;
}

.appointment-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.appointment-item:last-child {
  margin-bottom: 0;
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #dee2e6;
}

.appointment-number {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1rem;
}

.appointment-status {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
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

.appointment-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
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
</style>
