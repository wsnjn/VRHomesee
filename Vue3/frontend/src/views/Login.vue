<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import EarthModel from '../components/EarthModel.vue'

const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)
const errorMessage = ref('')

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

// 表单数据
const formData = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  realName: ''
})

const toggleForm = () => {
  isLogin.value = !isLogin.value
  errorMessage.value = ''
  // 清空表单数据
  Object.keys(formData).forEach(key => {
    formData[key] = ''
  })
}

const handleSubmit = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    if (isLogin.value) {
      // 登录逻辑
      const response = await axios.post(`${API_BASE_URL}/user/login`, {
        phone: formData.phone,
        password: formData.password
      })

      if (response.data.success) {
        // 登录成功，保存用户信息到本地存储
        localStorage.setItem('user', JSON.stringify(response.data.user))
        // 根据userType决定跳转路径
        const user = response.data.user
        if (user.userType === 3) {
          // 管理员跳转到管理端
          router.push('/admin')
        } else if (user.userType === 2) {
          // 房东跳转到房东管理端
          router.push('/landlord-admin')
        } else {
          // 租客跳转到首页
          router.push('/')
        }
      } else {
        errorMessage.value = response.data.message
      }
    } else {
      // 注册逻辑
      // 验证密码确认
      if (formData.password !== formData.confirmPassword) {
        errorMessage.value = '两次输入的密码不一致'
        loading.value = false
        return
      }

      // 验证必填字段
      if (!formData.username || !formData.phone || !formData.password || !formData.realName) {
        errorMessage.value = '请填写所有必填字段'
        loading.value = false
        return
      }

      const response = await axios.post(`${API_BASE_URL}/user/register`, {
        username: formData.username,
        phone: formData.phone,
        password: formData.password,
        realName: formData.realName
      })

      if (response.data.success) {
        // 注册成功，切换到登录表单
        isLogin.value = true
        errorMessage.value = '注册成功，请登录'
        // 清空表单数据
        Object.keys(formData).forEach(key => {
          formData[key] = ''
        })
      } else {
        errorMessage.value = response.data.message
      }
    }
  } catch (error) {
    console.error('请求失败:', error)
    errorMessage.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 检查手机号是否可用
const checkPhone = async () => {
  if (!formData.phone) return
  
  try {
    const response = await axios.get(`${API_BASE_URL}/user/check-phone/${formData.phone}`)
    if (response.data.exists) {
      errorMessage.value = response.data.message
    }
  } catch (error) {
    console.error('检查手机号失败:', error)
  }
}

// 检查用户名是否可用
const checkUsername = async () => {
  if (!formData.username) return
  
  try {
    const response = await axios.get(`${API_BASE_URL}/user/check-username/${formData.username}`)
    if (response.data.exists) {
      errorMessage.value = response.data.message
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 计算按钮文本
const buttonText = computed(() => {
  return loading.value ? '处理中...' : (isLogin.value ? '登录' : '注册')
})

// 计算按钮字符数组（用于动画）
const buttonChars = computed(() => {
  if (loading.value) {
    return []
  }
  return buttonText.value.split('')
})
</script>

<template>
  <div class="auth-container">
    <!-- 左侧区域 - 60% -->
    <div class="auth-left">
      <!-- 地球模型 -->
      <EarthModel class="earth-model" />
    </div>

    <!-- 右侧区域 - 40% -->
    <div class="auth-right">
      <div class="auth-form">
        <h2>{{ isLogin ? '登录' : '注册' }}</h2>
        
        <!-- 错误消息显示 -->
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        
        <form @submit.prevent="handleSubmit">
          <!-- 登录表单 -->
          <div v-if="isLogin" class="field">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M13.106 7.222c0-2.967-2.249-5.032-5.482-5.032-3.35 0-5.646 2.318-5.646 5.702 0 3.493 2.235 5.708 5.762 5.708.862 0 1.689-.123 2.304-.335v-.862c-.43.199-1.354.328-2.29.328-2.926 0-4.813-1.88-4.813-4.798 0-2.844 1.921-4.881 4.594-4.881 2.735 0 4.608 1.688 4.608 4.156 0 1.682-.554 2.769-1.416 2.769-.492 0-.772-.28-.772-.76V5.206H8.923v.834h-.11c-.266-.595-.881-.964-1.6-.964-1.4 0-2.378 1.162-2.378 2.823 0 1.737.957 2.906 2.379 2.906.8 0 1.415-.39 1.709-1.087h.11c.081.67.703 1.148 1.503 1.148 1.572 0 2.57-1.415 2.57-3.643zm-7.177.704c0-1.197.54-1.907 1.456-1.907.93 0 1.524.738 1.524 1.907S8.308 9.84 7.371 9.84c-.895 0-1.442-.725-1.442-1.914z"></path>
            </svg>
            <input 
              type="text" 
              id="phone" 
              v-model="formData.phone" 
              class="input-field"
              required
              placeholder="请输入手机号"
            >
          </div>
          <div v-if="isLogin" class="field">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M8 1a2 2 0 0 1 2 2v4H6V3a2 2 0 0 1 2-2zm3 6V3a3 3 0 0 0-6 0v4a2 2 0 0 0-2 2v5a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"></path>
            </svg>
            <input 
              type="password" 
              id="password" 
              v-model="formData.password" 
              class="input-field"
              required
              placeholder="请输入密码"
            >
          </div>
          
          <!-- 注册表单 -->
          <div v-if="!isLogin" class="field">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M13.106 7.222c0-2.967-2.249-5.032-5.482-5.032-3.35 0-5.646 2.318-5.646 5.702 0 3.493 2.235 5.708 5.762 5.708.862 0 1.689-.123 2.304-.335v-.862c-.43.199-1.354.328-2.29.328-2.926 0-4.813-1.88-4.813-4.798 0-2.844 1.921-4.881 4.594-4.881 2.735 0 4.608 1.688 4.608 4.156 0 1.682-.554 2.769-1.416 2.769-.492 0-.772-.28-.772-.76V5.206H8.923v.834h-.11c-.266-.595-.881-.964-1.6-.964-1.4 0-2.378 1.162-2.378 2.823 0 1.737.957 2.906 2.379 2.906.8 0 1.415-.39 1.709-1.087h.11c.081.67.703 1.148 1.503 1.148 1.572 0 2.57-1.415 2.57-3.643zm-7.177.704c0-1.197.54-1.907 1.456-1.907.93 0 1.524.738 1.524 1.907S8.308 9.84 7.371 9.84c-.895 0-1.442-.725-1.442-1.914z"></path>
            </svg>
            <input 
              type="text" 
              id="username" 
              v-model="formData.username" 
              @blur="checkUsername"
              class="input-field"
              required
              placeholder="请输入用户名"
            >
          </div>
          <div v-if="!isLogin" class="field">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6zm2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm4 8c0 1-1 1-1 1H3s-1 0-1-1 1-4 6-4 6 3 6 4zm-1-.004c-.001-.246-.154-.986-.832-1.664C11.516 10.68 10.289 10 8 10c-2.29 0-3.516.68-4.168 1.332-.678.678-.83 1.418-.832 1.664h10z"></path>
            </svg>
            <input 
              type="text" 
              id="realName" 
              v-model="formData.realName" 
              class="input-field"
              required
              placeholder="请输入真实姓名"
            >
          </div>
          <div v-if="!isLogin" class="field">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M13.106 7.222c0-2.967-2.249-5.032-5.482-5.032-3.35 0-5.646 2.318-5.646 5.702 0 3.493 2.235 5.708 5.762 5.708.862 0 1.689-.123 2.304-.335v-.862c-.43.199-1.354.328-2.29.328-2.926 0-4.813-1.88-4.813-4.798 0-2.844 1.921-4.881 4.594-4.881 2.735 0 4.608 1.688 4.608 4.156 0 1.682-.554 2.769-1.416 2.769-.492 0-.772-.28-.772-.76V5.206H8.923v.834h-.11c-.266-.595-.881-.964-1.6-.964-1.4 0-2.378 1.162-2.378 2.823 0 1.737.957 2.906 2.379 2.906.8 0 1.415-.39 1.709-1.087h.11c.081.67.703 1.148 1.503 1.148 1.572 0 2.57-1.415 2.57-3.643zm-7.177.704c0-1.197.54-1.907 1.456-1.907.93 0 1.524.738 1.524 1.907S8.308 9.84 7.371 9.84c-.895 0-1.442-.725-1.442-1.914z"></path>
            </svg>
            <input 
              type="text" 
              id="phone" 
              v-model="formData.phone" 
              @blur="checkPhone"
              class="input-field"
              required
              placeholder="请输入手机号"
            >
          </div>
          <div v-if="!isLogin" class="field">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M8 1a2 2 0 0 1 2 2v4H6V3a2 2 0 0 1 2-2zm3 6V3a3 3 0 0 0-6 0v4a2 2 0 0 0-2 2v5a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"></path>
            </svg>
            <input 
              type="password" 
              id="password" 
              v-model="formData.password" 
              class="input-field"
              required
              placeholder="请输入密码"
            >
          </div>
          <div v-if="!isLogin" class="field">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M8 1a2 2 0 0 1 2 2v4H6V3a2 2 0 0 1 2-2zm3 6V3a3 3 0 0 0-6 0v4a2 2 0 0 0-2 2v5a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"></path>
            </svg>
            <input 
              type="password" 
              id="confirmPassword" 
              v-model="formData.confirmPassword" 
              class="input-field"
              required
              placeholder="请再次输入密码"
            >
          </div>
          
          <!-- 加载状态使用普通按钮 -->
          <button 
            v-if="loading" 
            type="submit" 
            class="submit-btn" 
            disabled
          >
            处理中...
          </button>
          <!-- 非加载状态使用新样式按钮 -->
          <button 
            v-else 
            type="submit" 
            class="animated-button"
          >
            <div class="bgContainer">
              <span>{{ isLogin ? '登录' : '注册' }}</span>
            </div>
            <div class="arrowContainer">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 12H20M20 12L14 6M20 12L14 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </button>
        </form>
        <p class="toggle-text">
          {{ isLogin ? '没有账号？' : '已有账号？' }}
          <button @click="toggleForm" class="toggle-btn">
            {{ isLogin ? '立即注册' : '立即登录' }}
          </button>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import '../assets/css/btn-styles.css';
.auth-container {
  display: flex;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.auth-left {
  flex: 0 0 60%;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.earth-model {
  width: 100%;
  height: 100%;
}

.auth-right {
  background: white;
  flex: 0 0 40%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.auth-form {
  background: rgb(78, 202, 255);
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 4px 6px 0px black;
  border: 4px solid;
  width: 100%;
  max-width: 400px;
  transition: box-shadow 250ms, transform 250ms;
}

.auth-form h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  border: 1px solid #f5c6cb;
  font-size: 0.9rem;
}

.field {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5em;
  border-radius: 15px;
  padding: 0.6em;
  border: 4px solid;
  outline: none;
  color: #333;
  background-color: yellow;
  box-shadow: 4px 6px 0px black;
  margin-bottom: 1rem;
  transition: box-shadow 250ms, transform 250ms;
}

.input-icon {
  height: 1.3em;
  width: 1.3em;
  fill: #333;
}

.input-field {
  background: none;
  border: none;
  outline: none;
  width: 100%;
  color: #333;
  font-size: 1rem;
  font-weight: 600;
}

.input-field::placeholder {
  color: #666;
}

.submit-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
  transition: background-color 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #0056b3;
}

.submit-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.toggle-text {
  text-align: center;
  margin-top: 1rem;
  color: #666;
}

.toggle-btn {
  background: none;
  border: none;
  color: #007bff;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s;
}

.toggle-btn:hover {
  color: #0056b3;
}

.animated-button {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 1em 0em 1em 1em;
  background-color: yellow;
  cursor: pointer;
  box-shadow: 4px 6px 0px black;
  border: 4px solid;
  border-radius: 15px;
  position: relative;
  overflow: hidden;
  z-index: 100;
  transition: box-shadow 250ms, transform 250ms, filter 50ms;
  width: 100%;
  margin-top: 1rem;
}

.animated-button:hover {
  transform: translate(2px, 2px);
  box-shadow: 2px 3px 0px black;
}

.animated-button:active {
  filter: saturate(0.75);
}

.animated-button::after {
  content: "";
  position: absolute;
  inset: 0;
  background-color: pink;
  z-index: -1;
  transform: translateX(-100%);
  transition: transform 250ms;
}

.animated-button:hover::after {
  transform: translateX(0);
}

.bgContainer {
  position: relative;
  display: flex;
  justify-content: start;
  align-items: center;
  overflow: hidden;
  max-width: 35%;
  font-size: 2em;
  font-weight: 600;
}

.bgContainer span {
  position: relative;
  transform: translateX(-100%);
  transition: all 250ms;
}

.animated-button:hover .bgContainer > span {
  transform: translateX(0);
}

.arrowContainer {
  padding: 1em;
  margin-inline-end: 1em;
  border: 4px solid;
  border-radius: 50%;
  background-color: pink;
  position: relative;
  overflow: hidden;
  transition: transform 250ms, background-color 250ms;
  z-index: 100;
}

.arrowContainer::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background-color: yellow;
  transform: translateX(-100%);
  z-index: -1;
  transition: transform 250ms ease-in-out;
}

.animated-button:hover .arrowContainer::after {
  transform: translateX(0);
}

.animated-button:hover .arrowContainer {
  transform: translateX(5px);
}

.animated-button:active .arrowContainer {
  transform: translateX(8px);
}

.arrowContainer svg {
  vertical-align: middle;
}
</style>
