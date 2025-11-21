<script setup>
import { ref, reactive } from 'vue'
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
          <div v-if="isLogin" class="form-group">
            <label for="phone">手机号</label>
            <input 
              type="text" 
              id="phone" 
              v-model="formData.phone" 
              required
              placeholder="请输入手机号"
            >
          </div>
          <div v-if="isLogin" class="form-group">
            <label for="password">密码</label>
            <input 
              type="password" 
              id="password" 
              v-model="formData.password" 
              required
              placeholder="请输入密码"
            >
          </div>
          
          <!-- 注册表单 -->
          <div v-if="!isLogin" class="form-group">
            <label for="username">用户名</label>
            <input 
              type="text" 
              id="username" 
              v-model="formData.username" 
              @blur="checkUsername"
              required
              placeholder="请输入用户名"
            >
          </div>
          <div v-if="!isLogin" class="form-group">
            <label for="realName">真实姓名</label>
            <input 
              type="text" 
              id="realName" 
              v-model="formData.realName" 
              required
              placeholder="请输入真实姓名"
            >
          </div>
          <div v-if="!isLogin" class="form-group">
            <label for="phone">手机号</label>
            <input 
              type="text" 
              id="phone" 
              v-model="formData.phone" 
              @blur="checkPhone"
              required
              placeholder="请输入手机号"
            >
          </div>
          <div v-if="!isLogin" class="form-group">
            <label for="password">密码</label>
            <input 
              type="password" 
              id="password" 
              v-model="formData.password" 
              required
              placeholder="请输入密码"
            >
          </div>
          <div v-if="!isLogin" class="form-group">
            <label for="confirmPassword">确认密码</label>
            <input 
              type="password" 
              id="confirmPassword" 
              v-model="formData.confirmPassword" 
              required
              placeholder="请再次输入密码"
            >
          </div>
          
          <button 
            type="submit" 
            class="submit-btn" 
            :disabled="loading"
          >
            {{ loading ? '处理中...' : (isLogin ? '登录' : '注册') }}
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
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
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

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input {
  width: 90%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
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
</style>
