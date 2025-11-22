<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showUserMenu = ref(false)

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

// 计算属性：显示的用户名（优先显示真实姓名，如果没有则显示用户名）
const displayName = computed(() => {
  if (!user.value) return ''
  return user.value.realName || user.value.username || '用户'
})

// 计算属性：获取头像URL
const avatarUrl = computed(() => {
  if (!user.value || !user.value.avatar) {
    return '/src/assets/image/default-avatar.png'
  }
  return `/src/assets/image/${user.value.avatar}?t=${new Date().getTime()}`
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

const navigateToUserProfile = () => {
  router.push('/user-profile')
  showUserMenu.value = false
}

const logout = () => {
  localStorage.removeItem('user')
  user.value = null
  showUserMenu.value = false
  // 可以添加重新加载页面或状态更新的逻辑
  window.location.reload()
}

const toggleUserMenu = () => {
  if (isLoggedIn.value) {
    showUserMenu.value = !showUserMenu.value
  }
}

// 点击其他地方关闭用户菜单
const handleClickOutside = (event) => {
  const userMenu = document.querySelector('.user-menu')
  const userInfo = document.querySelector('.user-info')
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
    <nav class="navbar">
      <div class="logo-container">
        <img src="/src/assets/logo/logo (1).png" alt="HOMESEE Logo" class="nav-logo-canvas">
      </div>
      <div class="nav-content">
        <!-- 用户信息区域 - 在最右侧显示 -->
        <div class="user-info-container" v-if="isLoggedIn">
          <button class="button-message" @click="toggleUserMenu">
            <div class="content-avatar">
              <div class="status-user"></div>
              <div class="avatar">
                <img :src="avatarUrl" alt="用户头像" class="user-img">
              </div>
            </div>
            <div class="notice-content">
              <div class="username">{{ displayName }}</div>
              <div class="lable-message">{{ user.username }}</div>
              <div class="user-id">{{ user.phone }}</div>
            </div>
          </button>
          
          <!-- 用户菜单下拉框 -->
          <div v-if="showUserMenu" class="user-menu">
            <div class="menu-item" @click="navigateToUserProfile">
              <span>个人信息</span>
            </div>
            <div class="menu-item" @click="logout">
              <span>退出登录</span>
            </div>
          </div>
        </div>
        
        <button v-else @click="navigateToLogin" class="nav-link login-btn">登录/注册</button>
      </div>
    </nav>

    <!-- 主内容区域 -->
    <main class="main-content">
      <h1>欢迎来到HOMESEE</h1>
      <p>专业的租房平台，为您提供优质的房屋租赁服务</p>
      <div class="features">
        <div class="feature-card" @click="navigateToHouseSelection">
          <h3>VR漫游看房</h3>
          <p>体验沉浸式的房屋漫游功能，支持360度全景浏览和场景切换</p>
        </div>
        <div class="feature-card" @click="navigateToSmartMatching">
          <h3>智能匹配</h3>
          <p>根据您的偏好和预算，智能推荐最适合的房源</p>
        </div>
        <div class="feature-card" @click="navigateToMyAppointments">
          <h3>安全保障</h3>
          <p>严格的房源审核机制，确保每一笔交易的安全可靠</p>
          <p class="sub-feature">查看我的预约信息 →</p>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  position: relative;
  background: #ffffff;
}

.navbar {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  color: #333;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  height: 70px;
  border-bottom: 1px solid #e0e0e0;
}

.logo-container {
  width: 94px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0;
  border-right: 1px solid #e0e0e0;
}

.nav-logo-canvas {
  width: 94px;
  height: 70px;
}

.nav-content {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 2rem;
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
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.nav-link.router-link-active {
  background-color: #667eea;
}

.login-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.user-info-container {
  position: relative;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-name {
  font-weight: 600;
  font-size: 0.9rem;
  line-height: 1.2;
}

.user-phone {
  font-size: 0.8rem;
  opacity: 0.8;
  line-height: 1.2;
}

.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: rgba(43, 43, 43, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  min-width: 140px;
  z-index: 1000;
  margin-top: 8px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-item {
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  font-weight: 400;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-item:not(:last-child) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.main-content {
  padding: 3rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

.main-content h1 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 3rem;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.main-content p {
  text-align: center;
  margin-bottom: 3rem;
  color: #666;
  font-size: 1.3rem;
  font-weight: 300;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 2.5rem;
  margin-top: 3rem;
}

.feature-card {
  background: white;
  padding: 2.5rem 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-card h3 {
  margin-top: 0;
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  position: relative;
}

.feature-card p {
  text-align: left;
  margin: 0;
  color: #666;
  line-height: 1.6;
  font-size: 1rem;
  font-weight: 300;
}

/* 预约信息样式 */
.appointments-section {
  margin-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
  padding-top: 1rem;
}

.appointments-section h4 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.loading, .no-appointments {
  text-align: center;
  color: #666;
  padding: 1rem;
  font-style: italic;
}

.appointments-list {
  max-height: 300px;
  overflow-y: auto;
}

.appointment-item {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 1rem;
  margin-bottom: 0.75rem;
  border: 1px solid #e9ecef;
}

.appointment-item:last-child {
  margin-bottom: 0;
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #dee2e6;
}

.appointment-number {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.appointment-status {
  padding: 0.25rem 0.5rem;
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
  gap: 0.5rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
}

.detail-row .label {
  font-weight: 500;
  color: #495057;
  min-width: 80px;
}

.detail-row span:last-child {
  color: #6c757d;
  text-align: right;
  flex: 1;
}

/* 新的用户信息按钮样式 */
.user-info-container {
  --text-color: rgb(255, 255, 255);
  --bg-color-sup: #5e5e5e;
  --bg-color: #2b2b2b;
  --bg-hover-color: #161616;
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
}

.user-id {
  font-size: 12px;
  letter-spacing: -6px;
  height: 0;
  opacity: 0;
  transform: translateY(10px);
  transition: var(--btn-transition);
}

.lable-message {
  display: flex;
  align-items: center;
  opacity: 1;
  transform: scaleY(1);
  transition: var(--btn-transition);
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

.lable-message, .username {
  font-weight: 600;
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

/* 在线状态动画 */
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

/* 子功能样式 */
.sub-feature {
  margin-top: 1rem;
  color: #007bff;
  font-weight: 500;
  text-align: center;
  font-size: 0.9rem;
  cursor: pointer;
  transition: color 0.3s;
}

.sub-feature:hover {
  color: #0056b3;
}
</style>
