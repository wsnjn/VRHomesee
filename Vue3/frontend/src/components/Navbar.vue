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
                <svg class="menu-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                个人信息
              </div>
              <div class="menu-item" @click="navigateTo('/my-appointments')">
                <svg class="menu-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="0" ry="0"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                我的预约
              </div>
              <div class="menu-item" @click="navigateTo('/maintenance')">
                <svg class="menu-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path>
                </svg>
                维修报修
              </div>
              <div class="menu-item" @click="logout">
                <svg class="menu-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                  <polyline points="16 17 21 12 16 7"></polyline>
                  <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                退出登录
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
  height: 64px; /* Industrial compact height */
  z-index: 1000;
  background: white;
  border-bottom: 1px solid #E5E7EB;
  transition: all 0.2s ease;
}

.navbar.transparent {
  background: white; /* Always solid for consistency */
  border-bottom: 1px solid #E5E7EB;
}

.navbar.scrolled {
  background: white;
  border-bottom: 1px solid #111827; /* Darker border on scroll */
}

.nav-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.nav-logo-canvas {
  height: 32px; /* Smaller, sharper */
  width: auto;
}

.logo-text {
  font-size: 1.1rem;
  font-weight: 800;
  color: #111827; /* Solid Black */
  letter-spacing: -0.5px;
  text-transform: uppercase;
  background: none;
  -webkit-text-fill-color: initial;
}

.nav-links-center {
  display: flex;
  gap: 32px;
  align-items: center;
}

.nav-item {
  font-size: 0.9rem;
  font-weight: 500;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  padding: 4px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.nav-item:hover {
  color: #111827;
}

.nav-item.active {
  color: #111827;
  font-weight: 700;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -22px; /* Align with navbar border */
  left: 0;
  width: 100%;
  height: 2px;
  background: #111827;
  border-radius: 0;
}

.nav-links-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.login-btn {
  background: #111827;
  color: white;
  border: 1px solid #111827;
  padding: 8px 24px;
  border-radius: 0; /* Square */
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: none;
  font-size: 0.85rem;
  text-transform: uppercase;
}

.login-btn:hover {
  background: white;
  color: #111827;
  transform: none;
  box-shadow: none;
}

/* User Menu Styles - Restored Interactive */
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
  border-radius: 68px; /* Restored roundness */
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
  border-radius: 50%; /* Restored circle */
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
  border-radius: 50%; /* Restored circle */
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
  display: flex; /* Restored display */
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

/* Dropdown Menu */
.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  width: 200px;
  background: white;
  border-radius: 0;
  box-shadow: none;
  border: 1px solid #E5E7EB;
  padding: 0;
  z-index: 1000;
  margin-top: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #374151;
  cursor: pointer;
  border-radius: 0;
  transition: all 0.1s;
  font-weight: 500;
  font-size: 0.9rem;
  border-bottom: 1px solid #F3F4F6;
}

.menu-item:last-child { border-bottom: none; }

.menu-item:hover {
  background: #F9FAFB;
  color: #111827;
  padding-left: 20px; /* Technical interaction feedback */
}

.menu-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.1s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
