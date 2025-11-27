<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DinoOverlay from './DinoOverlay.vue'

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

// 计算属性：显示的用户名（优先显示真实姓名，如果没有则显示用户名）
const displayName = computed(() => {
  if (!user.value) return ''
  return user.value.realName || user.value.username || '用户'
})

// 计算属性：获取头像URL
const avatarUrl = computed(() => {
  if (!user.value || !user.value.avatar) {
    return '/models/image/default-avatar.png'
  }
  
  // 如果是完整的HTTP URL，直接使用
  if (user.value.avatar.startsWith('http')) {
    return user.value.avatar
  }
  
  // 使用文件服务器获取头像
  const FILE_SERVER_HOST = 'http://39.108.142.250:8088'
  return `${FILE_SERVER_HOST}/api/files/download/${user.value.avatar}`
})

const navigateToLogin = () => {
  router.push('/login')
}

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

const navigateToUserProfile = () => {
  router.push('/user-profile')
  showUserMenu.value = false
}

const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  user.value = null
  showUserMenu.value = false
  router.push('/login')
}

const toggleUserMenu = () => {
  if (isLoggedIn.value) {
    showUserMenu.value = !showUserMenu.value
  }
}

// 点击其他地方关闭用户菜单
const handleClickOutside = (event) => {
  const userMenu = document.querySelector('.user-menu')
  const userInfo = document.querySelector('.user-info-container')
  if (userMenu && userInfo && 
      !userMenu.contains(event.target) && 
      !userInfo.contains(event.target)) {
    showUserMenu.value = false
  }
}

// 添加全局点击事件监听
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

// 获取用户预约信息
const fetchUserAppointments = async () => {
  if (!user.value) return
  
  loadingAppointments.value = true
  try {
    const response = await fetch(`http://localhost:8080/api/viewing-appointment/user/${user.value.id}`)
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
    <nav class="navbar" :class="{ 'scrolled': isScrolled }">
      <div class="nav-content">
        <div class="logo-container">
          <img src="/models/logo (1).png" alt="HOMESEE Logo" class="nav-logo-canvas">
          <span class="logo-text">HOMESEE</span>
        </div>
        
        <div class="nav-links-container">
          <!-- 用户信息区域 -->
          <div class="user-info-container" v-if="isLoggedIn">
            <button id="btn-message" class="button-message" @click.stop="toggleUserMenu">
              <div class="content-avatar">
                <div class="status-user"></div>
                <div class="avatar">
                  <img :src="avatarUrl" alt="用户头像" class="user-img">
                </div>
              </div>
              <div class="notice-content">
                <div class="username">{{ displayName }}</div>
                <div class="lable-message">{{ user.phone }}</div>
                <div class="user-id">@{{ user.username }}</div>
              </div>
            </button>
            
            <!-- 用户菜单下拉框 -->
            <transition name="fade">
              <div v-if="showUserMenu" class="user-menu">
                <div class="menu-item" @click="navigateToUserProfile">
                  <span class="menu-icon">👤</span> 个人信息
                </div>
                <div class="menu-item" @click="logout">
                  <span class="menu-icon">🚪</span> 退出登录
                </div>
              </div>
            </transition>
          </div>
          
          <button v-else @click="navigateToLogin" class="nav-link login-btn">登录 / 注册</button>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <header class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">发现理想生活</h1>
        <p class="hero-subtitle">专业的租房平台，为您提供优质的房屋租赁服务</p>
        <div class="hero-actions">
          <button class="primary-btn" @click="navigateToHouseSelection">
            开始找房
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
          </button>
          <button class="secondary-btn" @click="navigateToSmartMatching">
            智能匹配
          </button>
        </div>
      </div>
      <div class="hero-background">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
        <div class="blob blob-3"></div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 特性卡片 -->
      <section class="features-section">
        <h2 class="section-title">核心功能</h2>
        <div class="features-grid">
          <div class="feature-card" @click="navigateToHouseSelection">
            <div class="card-icon vr-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12h20"></path><path d="M2 12l5-5"></path><path d="M22 12l-5-5"></path><path d="M12 7v10"></path></svg>
            </div>
            <h3>VR漫游看房</h3>
            <p>体验沉浸式的房屋漫游功能，支持360度全景浏览和场景切换</p>
            <span class="link-text">立即体验 <span class="arrow">→</span></span>
          </div>
          
          <div class="feature-card" @click="navigateToSmartMatching">
            <div class="card-icon match-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="14.31" y1="8" x2="20.05" y2="17.94"></line><line x1="9.69" y1="8" x2="21.17" y2="8"></line><line x1="7.38" y1="12" x2="13.12" y2="2.06"></line><line x1="9.69" y1="16" x2="3.95" y2="6.06"></line><line x1="14.31" y1="16" x2="2.83" y2="16"></line><line x1="16.62" y1="12" x2="10.88" y2="21.94"></line></svg>
            </div>
            <h3>智能匹配</h3>
            <p>根据您的偏好和预算，智能推荐最适合的房源</p>
            <span class="link-text">开始匹配 <span class="arrow">→</span></span>
          </div>
          
          <div class="feature-card" @click="navigateToMyAppointments">
            <div class="card-icon safe-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
            </div>
            <h3>安全保障</h3>
            <p>严格的房源审核机制，确保每一笔交易的安全可靠</p>
            <span class="link-text">查看预约 <span class="arrow">→</span></span>
          </div>
          
          <div class="feature-card" @click="navigateToMaintenance">
            <div class="card-icon fix-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg>
            </div>
            <h3>维修处理</h3>
            <p>房屋维修服务，快速响应您的维修需求</p>
            <span class="link-text">申请维修 <span class="arrow">→</span></span>
          </div>
          
          <div class="feature-card" @click="navigateToCommunity">
            <div class="card-icon community-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            </div>
            <h3>社区交流</h3>
            <p>与邻居交流，分享生活点滴，建立社区联系</p>
            <span class="link-text">进入社区 <span class="arrow">→</span></span>
          </div>
        </div>
      </section>

      <!-- 预约信息概览 (仅登录显示) -->
      <section v-if="isLoggedIn && appointments.length > 0" class="appointments-section">
        <div class="section-header">
          <h2 class="section-title">我的预约</h2>
          <button class="view-all-btn" @click="navigateToMyAppointments">查看全部</button>
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
              <button class="detail-btn" @click="navigateToMyAppointments">详情</button>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 智能匹配小恐龙 -->
    <DinoOverlay />
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

.home-container {
  min-height: 100vh;
  background-color: #f8f9fa;
  font-family: 'Inter', sans-serif;
  overflow-x: hidden;
}

/* Navbar Styles */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 80px;
  z-index: 1000;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.navbar.scrolled {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-logo-canvas {
  height: 50px;
  width: auto;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}

.nav-links-container {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.login-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* Hero Section */
.hero-section {
  position: relative;
  min-height: 90vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 8rem 2rem 4rem;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 800px;
  animation: fadeUp 1s ease-out;
}

.hero-title {
  font-size: 4.5rem;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 1.5rem;
  background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 1.5rem;
  color: #666;
  margin-bottom: 3rem;
  font-weight: 300;
}

.hero-actions {
  display: flex;
  gap: 1.5rem;
  justify-content: center;
}

.primary-btn {
  background: #2c3e50;
  color: white;
  border: none;
  padding: 1rem 2.5rem;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.primary-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(44, 62, 80, 0.2);
}

.secondary-btn {
  background: white;
  color: #2c3e50;
  border: 2px solid #2c3e50;
  padding: 1rem 2.5rem;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.secondary-btn:hover {
  background: #f8f9fa;
  transform: translateY(-3px);
}

/* Background Blobs */
.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 20s infinite ease-in-out;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: #667eea;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: #764ba2;
  bottom: -50px;
  right: -50px;
  animation-delay: -5s;
}

.blob-3 {
  width: 300px;
  height: 300px;
  background: #4facfe;
  top: 40%;
  left: 60%;
  animation-delay: -10s;
}

/* Features Section */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 4rem 2rem;
  position: relative;
  z-index: 2;
}

.section-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 3rem;
  text-align: center;
  position: relative;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  margin: 1rem auto 0;
  border-radius: 2px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  padding: 2.5rem;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  cursor: pointer;
  border: 1px solid rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
  color: white;
}

.card-icon svg {
  width: 30px;
  height: 30px;
}

.vr-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.match-icon { background: linear-gradient(135deg, #FF9A9E, #FECFEF); }
.safe-icon { background: linear-gradient(135deg, #a18cd1, #fbc2eb); }
.fix-icon { background: linear-gradient(135deg, #84fab0, #8fd3f4); }
.community-icon { background: linear-gradient(135deg, #fccb90, #d57eeb); }

.feature-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 2rem;
  flex-grow: 1;
}

.link-text {
  color: #667eea;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: gap 0.3s ease;
}

.feature-card:hover .link-text {
  gap: 1rem;
}

/* Appointments Section */
.appointments-section {
  margin-top: 6rem;
  background: white;
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.section-header .section-title {
  margin: 0;
  text-align: left;
}

.section-header .section-title::after {
  display: none;
}

.view-all-btn {
  color: #667eea;
  background: none;
  border: none;
  font-weight: 600;
  cursor: pointer;
}

.appointments-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.appointment-card {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 12px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.appointment-card:hover {
  background: white;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
}

.apt-status {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.apt-info {
  margin-bottom: 1rem;
}

.apt-time {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.apt-type {
  font-size: 0.9rem;
  color: #666;
}

.detail-btn {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.detail-btn:hover {
  background: #f8f9fa;
  border-color: #ccc;
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
