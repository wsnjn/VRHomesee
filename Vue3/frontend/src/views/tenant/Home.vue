<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DinoOverlay from './DinoOverlay.vue'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const showUserMenu = ref(false)
const isScrolled = ref(false)

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
  
  window.addEventListener('scroll', handleScroll)
})

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

// 计算属性：是否已登录
const isLoggedIn = computed(() => {
  return user.value !== null
})

const navigateToHouseSelection = () => {
  router.push('/house-selection')
}

const navigateToMyAppointments = () => {
  router.push('/my-appointments')
}

const navigateToSmartMatching = () => {
  router.push('/smart-matching')
}

const navigateToMaintenance = () => {
  router.push('/maintenance')
}

const navigateToCommunity = () => {
  router.push('/community')
}

const navigateToMapSearch = () => {
  router.push('/map-search')
}


// 获取用户预约信息
const fetchUserAppointments = async () => {
  if (!user.value) return
  
  loadingAppointments.value = true
  try {
    const response = await fetch(`https://api.homesee.xyz/api/viewing-appointment/user/${user.value.id}`)
    if (response.ok) {
      appointments.value = await response.json()
    } else {
      console.error('获取预约信息失败')
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
</script>

<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <Navbar />

    <!-- Hero Section -->
    <header class="hero-section">
      <div class="hero-content">
       <h1 class="hero-title">发现<br>理想生活</h1>
        <p class="hero-subtitle">专业的租房平台，为您提供优质的房屋租赁服务</p>
        <div class="hero-actions">
          <button class="primary-btn" @click="navigateToHouseSelection">
            Start Finding
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="square" stroke-linejoin="miter"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
          </button>
          <button class="secondary-btn" @click="navigateToSmartMatching">
            AI Matching
          </button>
        </div>
      </div>
      <div class="hero-background">
        <!-- Industrial Grid Background -->
        <div class="grid-line horizontal" v-for="i in 20" :key="`h-${i}`" :style="{ top: `${i * 5}%` }"></div>
        <div class="grid-line vertical" v-for="i in 20" :key="`v-${i}`" :style="{ left: `${i * 5}%` }"></div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 特性卡片 -->
      <section class="features-section">
        <h2 class="section-title">CORE FEATURES</h2>
        <div class="features-grid">
          <div class="feature-card" @click="navigateToHouseSelection">
            <div class="card-icon vr-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="square" stroke-linejoin="miter"><path d="M2 12h20"></path><path d="M2 12l5-5"></path><path d="M22 12l-5-5"></path><path d="M12 7v10"></path></svg>
            </div>
            <h3>VR Tour</h3>
            <p>全景漫游 / 沉浸式体验 / 360度浏览</p>
            <span class="link-text">EXPLORE <span class="arrow">→</span></span>
          </div>
          
          <div class="feature-card" @click="navigateToSmartMatching">
            <div class="card-icon match-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="square" stroke-linejoin="miter"><circle cx="12" cy="12" r="10"></circle><line x1="14.31" y1="8" x2="20.05" y2="17.94"></line><line x1="9.69" y1="8" x2="21.17" y2="8"></line><line x1="7.38" y1="12" x2="13.12" y2="2.06"></line><line x1="9.69" y1="16" x2="3.95" y2="6.06"></line><line x1="14.31" y1="16" x2="2.83" y2="16"></line><line x1="16.62" y1="12" x2="10.88" y2="21.94"></line></svg>
            </div>
            <h3>Smart Match</h3>
            <p>智能算法 / 偏好分析 / 精准推荐</p>
            <span class="link-text">START <span class="arrow">→</span></span>
          </div>
          
          <div class="feature-card" @click="navigateToMyAppointments">
            <div class="card-icon safe-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="square" stroke-linejoin="miter"><rect x="3" y="11" width="18" height="11" rx="0" ry="0"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
            </div>
            <h3>Security</h3>
            <p>实名认证 / 交易担保 / 隐私保护</p>
            <span class="link-text">CHECK <span class="arrow">→</span></span>
          </div>
          
          <div class="feature-card" @click="navigateToMaintenance">
            <div class="card-icon fix-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="square" stroke-linejoin="miter"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg>
            </div>
            <h3>Maintenance</h3>
            <p>在线报修 / 进度追踪 / 快速响应</p>
            <span class="link-text">APPLY <span class="arrow">→</span></span>
          </div>
          
          <div class="feature-card" @click="navigateToCommunity">
            <div class="card-icon community-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="square" stroke-linejoin="miter"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            </div>
            <h3>Community</h3>
            <p>邻里互动 / 经验分享 / 活动组织</p>
            <span class="link-text">JOIN <span class="arrow">→</span></span>
          </div>

          <div class="feature-card" @click="navigateToMapSearch">
            <div class="card-icon map-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="square" stroke-linejoin="miter"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
            </div>
            <h3>Map Search</h3>
            <p>地图选房 / 区域筛选 / 周边配套</p>
            <span class="link-text">EXPLORE <span class="arrow">→</span></span>
          </div>


        </div>
      </section>

      <!-- 预约信息概览 (仅登录显示) -->
      <section v-if="isLoggedIn && appointments.length > 0" class="appointments-section">
        <div class="section-header">
          <h2 class="section-title">APPOINTMENTS</h2>
          <button class="view-all-btn" @click="navigateToMyAppointments">VIEW ALL</button>
        </div>
        
        <div class="appointments-list">
          <div v-for="apt in appointments.slice(0, 3)" :key="apt.id" class="appointment-card">
            <div class="apt-status" :class="getStatusClass(apt.status)">
              {{ getAppointmentStatusText(apt.status) }}
            </div>
            <div class="apt-info">
              <div class="apt-time">{{ formatDate(apt.appointmentTime) }}</div>
              <div class="apt-type">{{ getAppointmentTypeText(apt.type) }}</div>
            </div>
            <div class="apt-action">
              <button class="detail-btn" @click="navigateToMyAppointments">DETAIL</button>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 智能匹配小恐龙 -->
    <!-- 智能匹配小恐龙 -->
    <!-- <DinoOverlay /> -->

  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap');

.home-container {
  min-height: 100vh;
  background-color: #FFFFFF; /* Pure White */
  color: #111827; /* Near Black */
  font-family: 'Inter', sans-serif;
  overflow-x: hidden;
}

/* Navbar Styles */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px; /* More compact */
  z-index: 1000;
  background: white;
  border-bottom: 1px solid #E5E7EB; /* Simple border */
}

.nav-content {
  max-width: 1400px; /* Wider container */
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.5px;
  text-transform: uppercase;
}

/* Hero Section - Industrial */
.hero-section {
  position: relative;
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: flex-start; /* Left Align */
  text-align: left;
  padding: 0 24px;
  max-width: 1400px;
  margin: 0 auto;
  border-left: 1px solid #F3F4F6;
  border-right: 1px solid #F3F4F6;
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 600px;
  padding-left: 48px; /* Offset from border */
}

.hero-title {
  font-size: 5rem;
  font-weight: 800;
  line-height: 0.9;
  margin-bottom: 2rem;
  color: #111827;
  letter-spacing: -3px;
  text-transform: uppercase;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: #6B7280;
  margin-bottom: 3rem;
  font-weight: 400;
  font-family: 'JetBrains Mono', monospace;
  max-width: 400px;
  border-left: 2px solid #2563EB;
  padding-left: 16px;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.primary-btn {
  background: #111827; /* Black */
  color: white;
  border: 1px solid #111827;
  padding: 12px 32px;
  border-radius: 0; /* Square */
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.primary-btn:hover {
  background: white;
  color: #111827;
}

.secondary-btn {
  background: white;
  color: #111827;
  border: 1px solid #E5E7EB;
  padding: 12px 32px;
  border-radius: 0; /* Square */
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.secondary-btn:hover {
  border-color: #111827;
}

/* Industrial Grid Background */
.hero-background {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 1;
  pointer-events: none;
  opacity: 0.4;
}

.grid-line {
  position: absolute;
  background: #E5E7EB;
}

.grid-line.horizontal {
  left: 0; right: 0; height: 1px;
}

.grid-line.vertical {
  top: 0; bottom: 0; width: 1px;
}

/* Features Section */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  border-left: 1px solid #F3F4F6;
  border-right: 1px solid #F3F4F6;
}

.features-section {
  padding: 80px 24px;
  border-top: 1px solid #E5E7EB;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: #9CA3AF;
  margin-bottom: 48px;
  text-align: left;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.section-title::after { display: none; }

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* Strict 3-col grid */
  gap: 0; /* Minimal gap for border overlap effect */
  border: 1px solid #E5E7EB;
}

.feature-card {
  background: white;
  padding: 48px;
  border-radius: 0;
  box-shadow: none;
  transition: all 0.2s;
  cursor: pointer;
  border: 1px solid #E5E7EB; /* Grid borders */
  margin: -1px 0 0 -1px; /* Overlap borders */
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-height: 300px;
}

.feature-card:hover {
  transform: none;
  border-color: #111827;
  z-index: 10;
  background: #F9FAFB;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 0; /* Square */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: #111827;
  background: transparent !important; /* Override gradients */
  border: 1px solid #E5E7EB;
}

.card-icon svg { width: 24px; height: 24px; stroke-width: 1.5; }

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: -0.5px;
}

.feature-card p {
  color: #6B7280;
  line-height: 1.6;
  margin-bottom: 32px;
  font-size: 0.95rem;
  font-family: 'JetBrains Mono', monospace;
}

.link-text {
  color: #2563EB; /* Industrial Blue */
  font-weight: 700;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: gap 0.2s ease;
  margin-top: auto;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.feature-card:hover .link-text { gap: 12px; }

/* Appointments Section */
.appointments-section {
  margin-top: 0;
  background: white;
  border-radius: 0;
  padding: 80px 24px;
  box-shadow: none;
  border-top: 1px solid #E5E7EB;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 48px;
}

.section-header .section-title { margin: 0; }

.view-all-btn {
  color: #111827;
  background: none;
  border: 1px solid #E5E7EB;
  padding: 8px 24px;
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
  border-radius: 0;
  transition: all 0.2s;
}

.view-all-btn:hover {
  background: #111827;
  color: white;
}

.appointments-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.appointment-card {
  background: white;
  padding: 32px;
  border-radius: 0;
  border: 1px solid #E5E7EB;
  transition: all 0.2s ease;
}

.appointment-card:hover {
  background: white;
  border-color: #111827;
  box-shadow: none;
}

.apt-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 0;
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 16px;
  text-transform: uppercase;
  border: 1px solid transparent;
  font-family: 'JetBrains Mono', monospace;
}

.status-pending { color: #D97706; border-color: #D97706; background: transparent; }
.status-confirmed { color: #2563EB; border-color: #2563EB; background: transparent; }
.status-completed { color: #059669; border: 1px solid #059669; background: transparent; }
.status-cancelled { color: #9CA3AF; border-color: #E5E7EB; background: transparent; text-decoration: line-through; }

.apt-info { margin-bottom: 24px; }

.apt-time {
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
  font-family: 'JetBrains Mono', monospace;
}

.apt-type { font-size: 0.85rem; color: #6B7280; }

.detail-btn {
  width: 100%;
  padding: 12px;
  border: 1px solid #111827;
  background: white;
  border-radius: 0;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 600;
  font-size: 0.8rem;
  text-transform: uppercase;
}

.detail-btn:hover {
  background: #111827;
  color: white;
}


/* User Menu Styles (Updated) */
.user-info-container {
  position: relative;
}

#btn-message {
  --text-color: #000000;
  --bg-color-sup: #e0e0e0;
  --bg-color: #ffffff;
  --bg-hover-color: #f5f5f5;
  --online-status: #00da00;
  --font-size: 16px;
  --btn-transition: all 0.2s ease-out;
}

.button-message {
  display: flex;
  justify-content: center;
  align-items: center;
  font: 400 var(--font-size) Helvetica Neue, sans-serif;
  box-shadow: 0 0 2.17382px rgba(0,0,0,.049),0 1.75px 6.01034px rgba(0,0,0,.07),0 3.63px 14.4706px rgba(0,0,0,.091),0 22px 48px rgba(0,0,0,.14);
  background-color: var(--bg-color);
  border-radius: 68px;
  cursor: pointer;
  padding: 6px 10px 6px 6px;
  width: fit-content;
  height: 40px;
  border: 0;
  overflow: hidden;
  position: relative;
  transition: var(--btn-transition);
}

.button-message:hover {
  height: 56px;
  padding: 8px 20px 8px 8px;
  background-color: var(--bg-hover-color);
  transition: var(--btn-transition);
}

.button-message:active {
  transform: scale(0.99);
}

.content-avatar {
  width: 30px;
  height: 30px;
  margin: 0;
  transition: var(--btn-transition);
  position: relative;
}

.button-message:hover .content-avatar {
  width: 40px;
  height: 40px;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--bg-color-sup);
}

.user-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-user {
  position: absolute;
  width: 6px;
  height: 6px;
  right: 1px;
  bottom: 1px;
  border-radius: 50%;
  outline: solid 2px var(--bg-color);
  background-color: var(--online-status);
  transition: var(--btn-transition);
  animation: active-status 2s ease-in-out infinite;
}

.button-message:hover .status-user {
  width: 10px;
  height: 10px;
  right: 1px;
  bottom: 1px;
  outline: solid 3px var(--bg-hover-color);
}

.notice-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding-left: 8px;
  text-align: initial;
  color: var(--text-color);
}

.username {
  letter-spacing: -6px;
  height: 0;
  opacity: 0;
  transform: translateY(-20px);
  transition: var(--btn-transition);
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-color);
}

.user-id {
  font-size: 12px;
  letter-spacing: -6px;
  height: 0;
  opacity: 0;
  transform: translateY(10px);
  transition: var(--btn-transition);
  color: var(--text-color);
}

.lable-message {
  display: flex;
  align-items: center;
  opacity: 1;
  transform: scaleY(1);
  transition: var(--btn-transition);
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-color);
}

.button-message:hover .username {
  height: auto;
  letter-spacing: normal;
  opacity: 1;
  transform: translateY(0);
  transition: var(--btn-transition);
}

.button-message:hover .user-id {
  height: auto;
  letter-spacing: normal;
  opacity: 1;
  transform: translateY(0);
  transition: var(--btn-transition);
}

.button-message:hover .lable-message {
  height: 0;
  transform: scaleY(0);
  transition: var(--btn-transition);
}

.number-message {
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  margin-left: 8px;
  font-size: 12px;
  width: 16px;
  height: 16px;
  background-color: var(--bg-color-sup);
  border-radius: 20px;
}

/*==============================================*/
@keyframes active-status {
  0% {
    background-color: var(--online-status);
  }

  33.33% {
    background-color: #93e200;
  }

  66.33% {
    background-color: #93e200;
  }

  100% {
    background-color: var(--online-status);
  }
}

.user-menu {
  position: absolute;
  top: 120%;
  right: 0;
  width: 200px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  padding: 0.5rem;
  border: 1px solid rgba(0,0,0,0.05);
}

.menu-item {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  color: #2c3e50;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.menu-item:hover {
  background: #f8f9fa;
  color: #667eea;
}

.menu-icon {
  font-size: 1.1rem;
}

/* Animations */
@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0% { transform: translate(0, 0); }
  50% { transform: translate(20px, 20px); }
  100% { transform: translate(0, 0); }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 3rem;
  }
  
  .hero-actions {
    flex-direction: column;
  }
  
  .nav-content {
    padding: 0 1rem;
  }
  
  .logo-text {
    display: none;
  }
}
</style>
