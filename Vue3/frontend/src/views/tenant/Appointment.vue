<!--
/**
 * 文件名: Appointment.vue
 * 作者: 牛迦楠
 * 学校: 东华理工大学
 * 专业: 软件工程（中外合作办学）
 * 课题: 融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 创建日期: 2026-01-06
 * 描述: 预约看房组件，提供在线预约表单和预约成功反馈
 */
-->
<template>
  <div class="appointment-page">
    <!-- Top Nav -->
    <nav class="navbar">
      <div class="nav-brand">
        <h2>预约看房</h2>
      </div>
      <div class="nav-links">
        <router-link to="/house-selection" class="nav-link">返回房屋列表</router-link>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="appointment-container">
      <!-- House Info Section -->
      <div class="section-title">房屋信息</div>
      <div class="house-card" v-if="houseInfo">
        <div class="house-header">
          <h3>{{ houseInfo.communityName }} {{ houseInfo.buildingUnit || '' }}{{ houseInfo.doorNumber }}</h3>
          <span class="house-status" :class="getStatusClass(houseInfo.status)">
            <span class="status-dot"></span>
            {{ getStatusText(houseInfo.status) }}
          </span>
        </div>
        <div class="house-details-grid">
          <div class="detail-item">
            <span class="label">地址</span>
            <span class="value">{{ houseInfo.province }}{{ houseInfo.city }}{{ houseInfo.district }}{{ houseInfo.street }}{{ houseInfo.communityName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">租金</span>
            <span class="value price">{{ houseInfo.rentPrice }} <small>元/月</small></span>
          </div>
          <div class="detail-item">
            <span class="label">面积</span>
            <span class="value">{{ houseInfo.roomArea }}㎡</span>
          </div>
          <div class="detail-item">
            <span class="label">楼层</span>
            <span class="value">{{ houseInfo.floorInfo }}</span>
          </div>
          <div class="detail-item">
            <span class="label">装修</span>
            <span class="value">{{ getDecorationText(houseInfo.decoration) }}</span>
          </div>
        </div>
      </div>

      <!-- Appointment Form -->
      <div class="section-title">预约信息</div>
      <div class="appointment-form">
        <form @submit.prevent="submitAppointment">
          <div class="form-grid">
            <div class="form-group">
              <label for="preferredDate">期望看房日期 *</label>
              <input
                id="preferredDate"
                type="date"
                v-model="appointmentForm.preferredDate"
                :min="minDate"
                required
              />
            </div>

            <div class="form-group">
              <label for="preferredTimeSlot">期望时间段 *</label>
              <select
                id="preferredTimeSlot"
                v-model="appointmentForm.preferredTimeSlot"
                required
              >
                <option value="">请选择时间段</option>
                <option value="09:00-10:00">09:00-10:00</option>
                <option value="10:00-11:00">10:00-11:00</option>
                <option value="11:00-12:00">11:00-12:00</option>
                <option value="14:00-15:00">14:00-15:00</option>
                <option value="15:00-16:00">15:00-16:00</option>
                <option value="16:00-17:00">16:00-17:00</option>
                <option value="17:00-18:00">17:00-18:00</option>
              </select>
            </div>

            <div class="form-group">
              <label for="appointmentType">看房方式 *</label>
              <select
                id="appointmentType"
                v-model="appointmentForm.appointmentType"
                required
              >
                <option value="1">现场看房</option>
                <option value="2">视频看房</option>
              </select>
            </div>

            <div class="form-group">
              <label for="tenantCount">租客人数 *</label>
              <input
                id="tenantCount"
                type="number"
                v-model="appointmentForm.tenantCount"
                min="1"
                max="10"
                placeholder="请输入人数"
                required
              />
            </div>

            <div class="form-group">
              <label for="expectedMoveInDate">期望入住日期 *</label>
              <input
                id="expectedMoveInDate"
                type="date"
                v-model="appointmentForm.expectedMoveInDate"
                :min="minDate"
                required
              />
            </div>

            <div class="form-group">
              <label for="contactName">联系人姓名 *</label>
              <input
                id="contactName"
                type="text"
                v-model="appointmentForm.contactName"
                placeholder="请输入姓名"
                required
              />
            </div>

            <div class="form-group">
              <label for="contactPhone">联系电话 *</label>
              <input
                id="contactPhone"
                type="tel"
                v-model="appointmentForm.contactPhone"
                placeholder="请输入电话"
                required
              />
            </div>

            <div class="form-group">
              <label for="wechatId">微信号</label>
              <input
                id="wechatId"
                type="text"
                v-model="appointmentForm.wechatId"
                placeholder="请输入微信号（可选）"
              />
            </div>

            <div class="form-group full-width">
              <label for="rentalIntention">租赁意向描述</label>
              <textarea
                id="rentalIntention"
                v-model="appointmentForm.rentalIntention"
                placeholder="请描述您的租赁需求..."
                rows="4"
              ></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button
              type="button"
              @click="goBack"
              class="cancel-btn"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="submit-btn"
            >
              {{ submitting ? '提交中...' : '提交预约' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Success Modal -->
    <div v-if="showSuccessModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>预约提交成功</h3>
        </div>
        <div class="modal-body">
          <p>预约编号</p>
          <div class="appointment-number">{{ appointmentNumber }}</div>
          
          <div class="modal-info-list">
             <div class="info-row">
               <span>房屋</span>
               <span>{{ houseInfo.communityName }} {{ houseInfo.buildingUnit || '' }}{{ houseInfo.doorNumber }}</span>
             </div>
             <div class="info-row">
               <span>时间</span>
               <span>{{ appointmentForm.preferredDate }} {{ appointmentForm.preferredTimeSlot }}</span>
             </div>
             <div class="info-row">
               <span>方式</span>
               <span>{{ appointmentForm.appointmentType === '1' ? '现场看房' : '视频看房' }}</span>
             </div>
          </div>
          
          <p class="modal-tip">业务员将尽快联系您确认。</p>
        </div>
        <div class="modal-actions">
          <button @click="goToHouseSelection" class="btn-outline">返回列表</button>
          <button @click="viewMyAppointments" class="btn-primary">查看我的预约</button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { userState } from '../../state/user.js'

const router = useRouter()
const route = useRoute()

// API基础URL
const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const houseInfo = ref(null)
const showSuccessModal = ref(false)
const appointmentNumber = ref('')

// 预约表单数据
const appointmentForm = ref({
  preferredDate: '',
  preferredTimeSlot: '',
  appointmentType: '1',
  contactName: '',
  contactPhone: '',
  wechatId: '',
  tenantCount: '',
  expectedMoveInDate: '',
  rentalIntention: ''
})

// 计算最小日期（今天）
const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

// 获取房屋信息
const loadHouseInfo = async () => {
  const houseId = route.query.houseId
  if (!houseId) {
    router.push('/house-selection')
    return
  }

  loading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/room-info/${houseId}`)
    if (response.data.success) {
      houseInfo.value = response.data.room
    } else {
      alert(`加载房屋信息失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('加载房屋信息失败:', error)
    alert('加载房屋信息失败，请返回重试')
  } finally {
    loading.value = false
  }
}

// 提交预约
const submitAppointment = async () => {
  if (!validateForm()) {
    return
  }

  submitting.value = true
  try {
    // 获取当前用户ID（这里需要根据实际登录状态获取）
    const userId = getCurrentUserId()
    
    const requestData = {
      roomId: route.query.houseId,
      userId: userId,
      preferredDate: appointmentForm.value.preferredDate,
      preferredTimeSlot: appointmentForm.value.preferredTimeSlot,
      appointmentType: parseInt(appointmentForm.value.appointmentType),
      contactName: appointmentForm.value.contactName,
      contactPhone: appointmentForm.value.contactPhone,
      wechatId: appointmentForm.value.wechatId || null,
      tenantCount: appointmentForm.value.tenantCount ? parseInt(appointmentForm.value.tenantCount) : null,
      expectedMoveInDate: appointmentForm.value.expectedMoveInDate || null,
      rentalIntention: appointmentForm.value.rentalIntention || null
    }

    const response = await axios.post(`${API_BASE_URL}/viewing-appointment/create`, requestData)
    
    if (response.data.success) {
      appointmentNumber.value = response.data.appointmentNumber
      showSuccessModal.value = true
    } else {
      alert(`预约失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('提交预约失败:', error)
    alert('提交预约失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 表单验证
const validateForm = () => {
  const form = appointmentForm.value
  
  if (!form.preferredDate) {
    alert('请选择期望看房日期')
    return false
  }

  if (!form.preferredTimeSlot) {
    alert('请选择期望时间段')
    return false
  }

  if (!form.tenantCount) {
    alert('请输入租客人数')
    return false
  }

  if (!form.expectedMoveInDate) {
    alert('请选择期望入住日期')
    return false
  }

  if (!form.contactName.trim()) {
    alert('请输入联系人姓名')
    return false
  }

  if (!form.contactPhone.trim()) {
    alert('请输入联系电话')
    return false
  }

  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(form.contactPhone)) {
    alert('请输入正确的手机号码')
    return false
  }

  return true
}

// 获取当前用户ID
const getCurrentUserId = () => {
  // 从用户状态管理中获取当前登录用户的ID
  if (userState.user && userState.user.id) {
    return userState.user.id
  }
  
  // 如果用户状态中没有用户信息，尝试从localStorage获取
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      const userData = JSON.parse(storedUser)
      return userData.id
    } catch (e) {
      console.error('解析用户数据失败:', e)
    }
  }
  
  // 如果无法获取用户ID，提示用户登录
  alert('请先登录后再进行预约')
  router.push('/')
  throw new Error('用户未登录')
}

// 获取状态文字
const getStatusText = (status) => {
  const statusMap = {
    0: '在租',
    1: '已下架'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  return status === 0 ? 'status-available' : 'status-unavailable'
}

// 获取装修程度文字
const getDecorationText = (decoration) => {
  const types = {
    1: '毛坯',
    2: '简装',
    3: '精装',
    4: '豪装'
  }
  return types[decoration] || '未知'
}

const goBack = () => {
  router.back()
}

const goToHouseSelection = () => {
  router.push('/house-selection')
}

const viewMyAppointments = () => {
  showSuccessModal.value = false
  router.push('/my-appointments')
}

onMounted(() => {
  loadHouseInfo()
})
</script>

<style scoped>
.appointment-page {
  min-height: 100vh;
  background-color: #FFFFFF;
  font-family: 'Inter', sans-serif;
  color: #111827;
}

/* Navbar: Industrial Minimal */
.navbar {
  position: sticky;
  top: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: white;
  border-bottom: 1px solid #111827;
  z-index: 100;
}

.nav-brand h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 800;
  text-transform: uppercase;
  color: #111827;
  letter-spacing: -0.5px;
}

.nav-link {
  color: #6B7280;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #111827;
}

/* Container */
.appointment-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px; /* Tighter container padding */
}

.section-title {
  font-size: 0.85rem;
  font-weight: 700;
  color: #9CA3AF;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 12px; /* Reduced margin */
  margin-top: 24px; /* Reduced margin */
}

.section-title:first-child {
  margin-top: 0;
}

/* House Card */
.house-card {
  background: white;
  padding: 20px; /* Reduced padding */
  border: 1px solid #E5E7EB;
  border-radius: 0;
  margin-bottom: 24px;
}

.house-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px; /* Reduced margin */
  padding-bottom: 12px; /* Reduced padding */
  border-bottom: 1px solid #E5E7EB;
}

.house-header h3 {
  margin: 0;
  color: #111827;
  font-size: 1.15rem; /* Slightly smaller */
  font-weight: 700;
}

.house-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%; /* Circle Exception */
}

.status-available .status-dot { background-color: #10B981; }
.status-available { color: #10B981; }

.status-unavailable .status-dot { background-color: #EF4444; }
.status-unavailable { color: #EF4444; }

/* Details Grid */
.house-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); /* Tighter grid */
  gap: 16px; /* Reduced gap */
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px; /* Tighter gap */
}

.detail-item .label {
  font-size: 0.75rem;
  color: #6B7280;
  text-transform: uppercase;
}

.detail-item .value {
  font-size: 0.95rem;
  color: #111827;
  font-weight: 500;
}

.detail-item .price {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 700;
}

/* Form */
.appointment-form {
  background: white;
  padding: 20px; /* Added card padding */
  border: 1px solid #E5E7EB; /* Added card border */
  border-radius: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px; /* Reduced gap */
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: span 2;
}

.form-group label {
  margin-bottom: 6px; /* Reduced margin */
  font-weight: 600;
  color: #374151;
  font-size: 0.8rem; /* Slightly smaller */
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px; /* Reduced padding */
  border: 1px solid #E5E7EB;
  border-radius: 0; /* Square Law */
  background: white;
  font-size: 0.9rem;
  outline: none;
  font-family: inherit;
  transition: border-color 0.2s;
  color: #111827;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #111827; /* Industrial Black Focus */
}

.form-group input:hover,
.form-group select:hover,
.form-group textarea:hover {
  border-color: #9CA3AF;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #E5E7EB;
}

/* Buttons */
.cancel-btn {
  background: white;
  border: 1px solid #E5E7EB;
  color: #111827;
  padding: 8px 24px; /* Smaller button */
  cursor: pointer;
  border-radius: 0;
  font-weight: 600;
  text-transform: uppercase;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.cancel-btn:hover {
  border-color: #111827;
}

.submit-btn {
  background: #111827;
  color: white;
  border: 1px solid #111827;
  padding: 10px 48px;
  cursor: pointer;
  border-radius: 0;
  font-weight: 600;
  text-transform: uppercase;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: black;
}

.submit-btn:disabled {
  background: #E5E7EB;
  border-color: #E5E7EB;
  cursor: not-allowed;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255, 255, 255, 0.9); /* Lighter backdrop */
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  width: 440px;
  border: 1px solid #111827; /* Hard border */
  border-radius: 0;
  box-shadow: none;
  padding: 0;
}

.modal-header {
  padding: 24px 32px 0;
  text-align: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 800;
  text-transform: uppercase;
  color: #111827;
}

.modal-body {
  padding: 24px 32px;
  text-align: center;
}

.appointment-number {
  font-family: 'JetBrains Mono', monospace;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 8px 0 24px;
  padding: 12px;
  background: #F3F4F6;
  border: 1px dashed #9CA3AF;
}

.modal-info-list {
  text-align: left;
  margin-bottom: 24px;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
}

.info-row span:first-child {
  color: #6B7280;
  font-weight: 500;
}

.info-row span:last-child {
  color: #111827;
  font-weight: 600;
  text-align: right;
}

.modal-tip {
  color: #6B7280;
  font-size: 0.85rem;
  margin-top: 16px;
}

.modal-actions {
  display: flex;
  border-top: 1px solid #E5E7EB;
}

.modal-actions button {
  flex: 1;
  padding: 16px;
  border: none;
  background: white;
  cursor: pointer;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.9rem;
  border-radius: 0;
  transition: all 0.2s;
}

.btn-outline {
  color: #6B7280;
  border-right: 1px solid #E5E7EB;
}

.btn-outline:hover { background: #F9FAFB; color: #111827; }

.btn-primary {
  color: white;
  background: #111827;
}

.btn-primary:hover { background: black; }

/* Loading */
.loading-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #E5E7EB;
  border-top-color: #111827;
  border-radius: 50%; /* Circle Exception */
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  .form-group.full-width {
    grid-column: span 1;
  }
}
</style>
