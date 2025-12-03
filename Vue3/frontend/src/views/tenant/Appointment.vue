<template>
  <div class="appointment-page">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-brand">
        <h2>预约看房</h2>
      </div>
      <div class="nav-links">
        <router-link to="/house-selection" class="nav-link">返回房屋列表</router-link>
      </div>
    </nav>

    <!-- 主要内容 -->
    <div class="appointment-container">
      <!-- 房屋信息卡片 -->
      <div class="house-card" v-if="houseInfo">
        <div class="house-header">
          <h3>{{ houseInfo.communityName }} {{ houseInfo.buildingUnit || '' }}{{ houseInfo.doorNumber }}</h3>
          <span class="house-status" :class="getStatusClass(houseInfo.status)">
            {{ getStatusText(houseInfo.status) }}
          </span>
        </div>
        <div class="house-details">
          <p><strong>地址：</strong>{{ houseInfo.province }}{{ houseInfo.city }}{{ houseInfo.district }}{{ houseInfo.street }}{{ houseInfo.communityName }}</p>
          <p><strong>租金：</strong>{{ houseInfo.rentPrice }}元/月</p>
          <p><strong>面积：</strong>{{ houseInfo.roomArea }}㎡</p>
          <p><strong>楼层：</strong>{{ houseInfo.floorInfo }}</p>
          <p><strong>装修：</strong>{{ getDecorationText(houseInfo.decoration) }}</p>
        </div>
      </div>

      <!-- 预约表单 -->
      <div class="appointment-form">
        <h3>填写预约信息</h3>
        <form @submit.prevent="submitAppointment">
          <div class="form-row">
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
          </div>

          <div class="form-row">
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
                placeholder="请输入租客人数"
                required
              />
            </div>
          </div>

          <div class="form-row">
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
                placeholder="请输入联系人姓名"
                required
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="contactPhone">联系电话 *</label>
              <input
                id="contactPhone"
                type="tel"
                v-model="appointmentForm.contactPhone"
                placeholder="请输入联系电话"
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
          </div>

          <div class="form-group full-width">
            <label for="rentalIntention">租赁意向描述</label>
            <textarea
              id="rentalIntention"
              v-model="appointmentForm.rentalIntention"
              placeholder="请描述您的租赁需求，如租期、预算、特殊要求等（可选）"
              rows="4"
            ></textarea>
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
              :class="{ 'disabled': submitting }"
            >
              {{ submitting ? '提交中...' : '提交预约' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 预约成功弹窗 -->
    <div v-if="showSuccessModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>预约成功！</h3>
        </div>
        <div class="modal-body">
          <p>您的预约已提交成功，预约编号：<strong>{{ appointmentNumber }}</strong></p>
          <p>业务员会尽快与您联系确认看房事宜。</p>
          <div class="appointment-details">
            <p><strong>房屋信息：</strong>{{ houseInfo.communityName }} {{ houseInfo.buildingUnit || '' }}{{ houseInfo.doorNumber }}</p>
            <p><strong>预约时间：</strong>{{ appointmentForm.preferredDate }} {{ appointmentForm.preferredTimeSlot }}</p>
            <p><strong>看房方式：</strong>{{ appointmentForm.appointmentType === '1' ? '现场看房' : '视频看房' }}</p>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="goToHouseSelection" class="primary-btn">返回房屋列表</button>
          <button @click="viewMyAppointments" class="secondary-btn">查看我的预约</button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <p>加载中...</p>
      </div>
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
39.108.142.250:8080
// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

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
  return statusMap[status] || '未知状态'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-available',
    1: 'status-unavailable'
  }
  return classMap[status] || 'status-unknown'
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

// 返回上一页
const goBack = () => {
  router.back()
}

// 返回房屋列表
const goToHouseSelection = () => {
  router.push('/house-selection')
}

// 查看我的预约
const viewMyAppointments = () => {
  showSuccessModal.value = false
  router.push('/my-appointments')
}

// 页面加载时初始化数据
onMounted(() => {
  loadHouseInfo()
})
</script>

<style scoped>
.appointment-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.navbar {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: rgba(44, 62, 80, 0.9);
  color: white;
  z-index: 100;
  backdrop-filter: blur(10px);
}

.nav-brand h2 {
  margin: 0;
  color: white;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
  cursor: pointer;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.appointment-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.house-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  border: 1px solid #f0f0f0;
}

.house-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.house-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
  flex: 1;
  margin-right: 16px;
}

.house-status {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.875rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-available {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-unavailable {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.status-unknown {
  background-color: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

.house-details p {
  margin: 8px 0;
  color: #666;
  line-height: 1.5;
}

.house-details strong {
  color: #333;
}

.appointment-form {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.appointment-form h3 {
  margin: 0 0 24px 0;
  color: #2c3e50;
  font-size: 1.25rem;
  border-bottom: 2px solid #007bff;
  padding-bottom: 12px;
}

.form-row {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  flex: 0 0 100%;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
  background-color: #f8f9fa;
}

.form-group input:hover,
.form-group select:hover,
.form-group textarea:hover {
  border-color: #adb5bd;
}

.form-group select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=US-ASCII,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 4 5'><path fill='%23666' d='M2 0L0 2h4zm0 5L0 3h4z'/></svg>");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 12px;
  padding-right: 40px;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.cancel-btn,
.submit-btn {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-width: 120px;
}

.cancel-btn {
  background: white;
  color: #6c757d;
  border: 2px solid #6c757d;
}

.cancel-btn:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.2);
}

.submit-btn {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: 2px solid #28a745;
}

.submit-btn:hover:not(.disabled) {
  background: linear-gradient(135deg, #20c997 0%, #1ba87e 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
}

.submit-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.submit-btn.disabled:hover {
  transform: none;
  box-shadow: none;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 100%;
  overflow: hidden;
}

.modal-header {
  padding: 24px 24px 0;
  text-align: center;
}

.modal-header h3 {
  margin: 0;
  color: #28a745;
  font-size: 1.5rem;
}

.modal-body {
  padding: 24px;
  text-align: center;
}

.modal-body p {
  margin: 12px 0;
  color: #666;
  line-height: 1.5;
}

.appointment-details {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: 16px;
  text-align: left;
}

.appointment-details p {
  margin: 8px 0;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  gap: 12px;
  padding: 0 24px 24px;
}

.primary-btn,
.secondary-btn {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.primary-btn {
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
}

.primary-btn:hover {
  background: linear-gradient(135deg, #0056b3 0%, #004085 100%);
  transform: translateY(-1px);
}

.secondary-btn {
  background: white;
  color: #6c757d;
  border: 2px solid #6c757d;
}

.secondary-btn:hover {
  background: #f8f9fa;
  transform: translateY(-1px);
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-content {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.loading-content p {
  margin: 0;
  color: #666;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .appointment-container {
    margin: 10px auto;
    padding: 0 10px;
  }

  .form-row {
    flex-direction: column;
    gap: 16px;
  }

  .form-actions {
    flex-direction: column;
  }

  .cancel-btn,
  .submit-btn {
    width: 100%;
  }

  .modal-actions {
    flex-direction: column;
  }

  .navbar {
    padding: 1rem;
  }
}
</style>
