<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps({
  transparent: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const route = useRoute()
const showUserMenu = ref(false)
const isScrolled = ref(false)
const user = ref(null)

// Initialize user data
const initUser = () => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
  }
}

onMounted(() => {
  initUser()
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside)
})

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

const isLoggedIn = computed(() => user.value !== null)

const displayName = computed(() => {
  if (!user.value) return ''
  return user.value.realName || user.value.username || '用户'
})

const avatarUrl = computed(() => {
  if (!user.value || !user.value.avatar) {
    return '/models/image/default-avatar.png'
  }
  if (user.value.avatar.startsWith('http')) {
    return user.value.avatar
  }
  const FILE_SERVER_HOST = 'https://files.homesee.xyz'
  return `${FILE_SERVER_HOST}/api/files/download/${user.value.avatar}`
})

const navigateTo = (path) => {
  router.push(path)
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

const handleClickOutside = (event) => {
  const userMenu = document.querySelector('.user-menu')
  const userInfo = document.querySelector('.user-info-container')
  if (userMenu && userInfo && 
      !userMenu.contains(event.target) && 
      !userInfo.contains(event.target)) {
    showUserMenu.value = false
  }
}

const isActive = (path) => route.path === path
</script>

<template>
  <nav class="navbar" :class="{ 'scrolled': isScrolled, 'transparent': transparent && !isScrolled }">
    <div class="nav-content">
      <div class="logo-container" @click="navigateTo('/')">
        <img src="/models/logo (1).png" alt="HOMESEE Logo" class="nav-logo-canvas">
        <span class="logo-text">HOMESEE</span>
      </div>
      
      <!-- Center Navigation Links -->
      <div class="nav-links-center">
        <a class="nav-item" :class="{ active: isActive('/') }" @click="navigateTo('/')">首页</a>
        <a class="nav-item" :class="{ active: isActive('/house-selection') }" @click="navigateTo('/house-selection')">房源精选</a>
        <a class="nav-item" :class="{ active: isActive('/community') }" @click="navigateTo('/community')">社区交流</a>
        <a class="nav-item" :class="{ active: isActive('/smart-matching') }" @click="navigateTo('/smart-matching')">智能匹配</a>
      </div>

      <div class="nav-links-right">
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
          
          <transition name="fade">
            <div v-if="showUserMenu" class="user-menu">
              <div class="menu-item" @click="navigateTo('/user-profile')">
                <span class="menu-icon">👤</span> 个人信息
              </div>
              <div class="menu-item" @click="navigateTo('/my-appointments')">
                <span class="menu-icon">📅</span> 我的预约
              </div>
              <div class="menu-item" @click="logout">
                <span class="menu-icon">🚪</span> 退出登录
              </div>
            </div>
          </transition>
        </div>
        
        <button v-else @click="navigateTo('/login')" class="nav-link login-btn">登录 / 注册</button>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 80px;
  z-index: 1000;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.navbar.transparent {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: none;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.navbar.scrolled {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 1400px;
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
  cursor: pointer;
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

.nav-links-center {
  display: flex;
  gap: 2.5rem;
  align-items: center;
}

.nav-item {
  font-size: 1rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  padding: 0.5rem 0;
}

.nav-item:hover {
  color: #667eea;
}

.nav-item.active {
  color: #667eea;
  font-weight: 600;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 2px;
}

.nav-links-right {
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

/* User Menu Styles */
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
  z-index: 10;
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

/* Dropdown Menu */
.user-menu {
  position: absolute;
  top: 120%;
  right: 0;
  width: 200px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  padding: 0.5rem;
  z-index: 1000;
  border: 1px solid rgba(0,0,0,0.05);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  color: #2c3e50;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
  font-weight: 500;
}

.menu-item:hover {
  background: #f8f9fa;
  color: #667eea;
}

.menu-icon {
  font-size: 1.2rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
