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
  
  // 初始化Canvas徽标动画
  initLogoAnimation()
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

const navigateToLogin = () => {
  router.push('/login')
}

const navigateToHouseSelection = () => {
  router.push('/house-selection')
}

const navigateToMyAppointments = () => {
  router.push('/my-appointments')
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

// Canvas徽标动画初始化
const initLogoAnimation = () => {
  // 获取Canvas元素和上下文
  const canvas = document.getElementById('logoCanvas')
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  
  // 设置Canvas大小为窗口大小
  function resizeCanvas() {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    drawLogo()
  }
  
  // 颜色定义
  const colors = {
    background: 'transparent',
    darkBlue: '#0066CC',
    mediumBlue: '#3399FF',
    lightBlue: '#66B2FF'
  }
  
  // 徽标参数
  const logoParams = {
    baseWidth: 0.3,
    baseHeight: 0.03,
    building1Width: 0.075,
    building1Height: 0.15,
    building2Width: 0.06,
    building2Height: 0.22,
    depth: 0.05
  }
  
  // 动画状态
  let animationProgress = 0
  let animationDirection = 1
  const animationSpeed = 0.002
  
  // 绘制徽标
  function drawLogo() {
    // 清除Canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    
    // 计算徽标位置和尺寸 - 底部对齐
    const centerX = canvas.width / 2
    const centerY = canvas.height * 0.85
    
    const baseWidth = canvas.width * logoParams.baseWidth
    const baseHeight = canvas.height * logoParams.baseHeight
    const building1Width = canvas.width * logoParams.building1Width
    const building1Height = canvas.height * logoParams.building1Height
    const building2Width = canvas.width * logoParams.building2Width
    const building2Height = canvas.height * logoParams.building2Height
    const depth = canvas.width * logoParams.depth
    
    
    // 绘制建筑1（矮胖）
    const building1X = centerX - baseWidth * 0.25
    drawBuilding(building1X, centerY, building1Width, building1Height, depth, 0)
    
    // 绘制建筑2（高耸）
    const building2X = centerX + baseWidth * 0.25 - building2Width
    drawBuilding(building2X, centerY, building2Width, building2Height, depth, 1)
    
    // 绘制文字
    drawText(centerX, centerY + baseHeight + canvas.height * 0.03)
    
    // 更新动画进度
    animationProgress += animationSpeed * animationDirection
    if (animationProgress >= 1 || animationProgress <= 0) {
      animationDirection *= -1
    }
  }
  
  
  // 绘制单个建筑
  function drawBuilding(x, baseY, width, height, depth, isTall) {
    const y = baseY - height
    
    ctx.save()
    
    // 创建渐变 - 根据建筑高度调整颜色
    const gradient = ctx.createLinearGradient(x, y, x + width, y + height)
    if (isTall) {
      gradient.addColorStop(0, colors.lightBlue)
      gradient.addColorStop(0.5, colors.mediumBlue)
      gradient.addColorStop(1, colors.darkBlue)
    } else {
      gradient.addColorStop(0, colors.darkBlue)
      gradient.addColorStop(0.5, colors.mediumBlue)
      gradient.addColorStop(1, colors.lightBlue)
    }
    
    ctx.strokeStyle = gradient
    ctx.lineWidth = 3
    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'
    
    // 应用动画效果
    ctx.globalAlpha = animationProgress
    
    // 绘制建筑正面
    ctx.beginPath()
    ctx.moveTo(x, baseY)
    ctx.lineTo(x + width, baseY)
    ctx.lineTo(x + width, y)
    ctx.lineTo(x, y)
    ctx.closePath()
    ctx.stroke()
    
    // 绘制立体效果
    ctx.beginPath()
    ctx.moveTo(x + width, baseY)
    ctx.lineTo(x + width + depth, baseY - depth)
    ctx.lineTo(x + width + depth, y - depth)
    ctx.lineTo(x + width, y)
    ctx.stroke()
    
    ctx.beginPath()
    ctx.moveTo(x + width, y)
    ctx.lineTo(x + width + depth, y - depth)
    ctx.lineTo(x + depth, y - depth)
    ctx.lineTo(x, y)
    ctx.stroke()
    
    // 添加一些细节线条增强立体感
    ctx.globalAlpha = 0.5 * animationProgress
    ctx.beginPath()
    for (let i = 1; i < 4; i++) {
      const segmentHeight = height / 4
      ctx.moveTo(x, baseY - segmentHeight * i)
      ctx.lineTo(x + width, baseY - segmentHeight * i)
    }
    ctx.stroke()
    
    ctx.restore()
  }
  
  // 绘制文字
  function drawText(centerX, y) {
    ctx.save()
    
    // 创建文字渐变
    const gradient = ctx.createLinearGradient(
      centerX - 150, y, 
      centerX + 150, y + 60
    )
    gradient.addColorStop(0, colors.darkBlue)
    gradient.addColorStop(0.5, colors.mediumBlue)
    gradient.addColorStop(1, colors.lightBlue)
    
    ctx.strokeStyle = gradient
    ctx.lineWidth = 3
    ctx.font = `bold ${canvas.width * 0.06}px Arial, sans-serif`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    
    // 应用动画效果
    ctx.globalAlpha = animationProgress
    
    // 绘制文字描边
    ctx.strokeText('HOMESEE', centerX, y)
    
    // 添加微弱的填充增强可读性
    ctx.fillStyle = 'rgba(255, 255, 255, 0.1)'
    ctx.fillText('HOMESEE', centerX, y)
    
    ctx.restore()
  }
  
  // 动画循环
  function animate() {
    drawLogo()
    requestAnimationFrame(animate)
  }
  
  // 初始化
  window.addEventListener('resize', resizeCanvas)
  resizeCanvas()
  animate()
}
</script>

<template>
  <div class="home-container">
    <!-- Canvas背景 -->
    <canvas id="logoCanvas" class="logo-canvas"></canvas>
    
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="logo-container">
        <img src="@/assets/logo/logo (1).png" alt="HOMSEE Logo" class="nav-logo">
      </div>
      <div class="nav-content">
        <!-- 用户信息区域 - 在最右侧显示 -->
        <div class="user-info-container" v-if="isLoggedIn">
          <div class="user-info" @click="toggleUserMenu">
            <div class="user-name">{{ displayName }}</div>
            <div class="user-phone">{{ user.phone }}</div>
          </div>
          
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
      <h1>欢迎来到HOMSEE</h1>
      <p>专业的租房平台，为您提供优质的房屋租赁服务</p>
      <div class="features">
        <div class="feature-card" @click="navigateToHouseSelection">
          <h3>VR漫游看房</h3>
          <p>体验沉浸式的房屋漫游功能，支持360度全景浏览和场景切换</p>
        </div>
        <div class="feature-card">
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
}

.logo-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
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
  background-color: #007bff;
}

.login-btn {
  background-color: #007bff;
  border: none;
  cursor: pointer;
  font-size: 1rem;
}

.login-btn:hover {
  background-color: #0056b3;
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
  background: white;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  z-index: 1000;
  margin-top: 4px;
  overflow: hidden;
}

.menu-item {
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
  color: #333;
  font-size: 0.9rem;
}

.menu-item:hover {
  background-color: #f8f9fa;
}

.menu-item:not(:last-child) {
  border-bottom: 1px solid #e9ecef;
}

.main-content {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.main-content h1 {
  text-align: center;
  margin-bottom: 1rem;
  color: #333;
}

.main-content p {
  text-align: center;
  margin-bottom: 2rem;
  color: #666;
  font-size: 1.1rem;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.feature-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.feature-card h3 {
  margin-top: 0;
  color: #2c3e50;
}

.feature-card p {
  text-align: left;
  margin: 0;
  color: #666;
  line-height: 1.5;
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