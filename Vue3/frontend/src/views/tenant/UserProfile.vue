<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const user = ref(null)
const loading = ref(false)
const isEditing = ref(false)
const saveLoading = ref(false)
const message = ref('')
const editingField = ref('')
const avatarUploading = ref(false)
const avatarFile = ref(null)

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

// 用户类型映射
const userTypeMap = {
  1: '租客',
  2: '房东',
  3: '租客+房东'
}

// 性别映射
const genderMap = {
  0: '未知',
  1: '男',
  2: '女'
}

// 编辑表单数据
const editForm = reactive({
  realName: '',
  gender: 0,
  birthday: '',
  idCard: '',
  email: '',
  wechat: '',
  qq: '',
  job: '',
  company: '',
  monthlyIncome: '',
  rentalBudgetMin: '',
  rentalBudgetMax: '',
  preferredDistricts: '',
  houseRequirements: ''
})

onMounted(async () => {
  // 从localStorage获取用户基本信息
  const userData = localStorage.getItem('user')
  if (userData) {
    const localUser = JSON.parse(userData)
    user.value = localUser
    initEditForm()
    
    // 从后端API获取最新用户信息
    await fetchUserInfo(localUser.id)
  } else {
    // 如果没有登录信息，跳转到登录页
    router.push('/login')
  }
})

// 从后端获取用户信息
const fetchUserInfo = async (userId) => {
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/user/${userId}`)
    if (response.data.success) {
      // 更新用户信息，确保数据来自数据库
      user.value = response.data.user
      // 更新本地存储
      localStorage.setItem('user', JSON.stringify(response.data.user))
      // 重新初始化表单
      initEditForm()
    } else {
      console.error('获取用户信息失败:', response.data.message)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化编辑表单数据
const initEditForm = () => {
  if (!user.value) return
  
  editForm.realName = user.value.realName || ''
  editForm.gender = user.value.gender || 0
  editForm.birthday = user.value.birthday ? formatDateForInput(user.value.birthday) : ''
  editForm.idCard = user.value.idCard || ''
  editForm.email = user.value.email || ''
  editForm.wechat = user.value.wechat || ''
  editForm.qq = user.value.qq || ''
  editForm.job = user.value.job || ''
  editForm.company = user.value.company || ''
  editForm.monthlyIncome = user.value.monthlyIncome || ''
  editForm.rentalBudgetMin = user.value.rentalBudgetMin || ''
  editForm.rentalBudgetMax = user.value.rentalBudgetMax || ''
  editForm.preferredDistricts = user.value.preferredDistricts || ''
  editForm.houseRequirements = user.value.houseRequirements || ''
}

// 将日期格式化为input type="date"需要的格式
const formatDateForInput = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toISOString().split('T')[0]
}

const goBack = () => {
  router.back()
}

const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 开始编辑某个字段
const startEditField = (fieldName) => {
  editingField.value = fieldName
  isEditing.value = true
  message.value = ''
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  editingField.value = ''
  initEditForm()
  message.value = ''
}

// 保存用户信息
const saveUserInfo = async () => {
  saveLoading.value = true
  message.value = ''
  
  try {
    const response = await axios.put(`${API_BASE_URL}/user/update/${user.value.id}`, editForm)
    
    if (response.data.success) {
      message.value = '信息更新成功'
      isEditing.value = false
      editingField.value = ''
      
      // 更新本地存储的用户信息
      const updatedUser = { ...user.value, ...response.data.user }
      user.value = updatedUser
      localStorage.setItem('user', JSON.stringify(updatedUser))
      
      // 重新初始化表单
      initEditForm()
    } else {
      message.value = response.data.message
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    message.value = '更新失败，请稍后重试'
  } finally {
    saveLoading.value = false
  }
}

// 获取字段显示值
const getFieldDisplayValue = (fieldName) => {
  if (!user.value) return '未设置'
  
  const value = user.value[fieldName]
  if (value === null || value === undefined || value === '') return '未设置'
  
  switch (fieldName) {
    case 'gender':
      return genderMap[value] || '未设置'
    case 'birthday':
      return formatDate(value)
    case 'monthlyIncome':
      return `${value}元`
    case 'rentalBudgetMin':
      return `${value}元/月`
    case 'rentalBudgetMax':
      return `${value}元/月`
    case 'isCertified':
      return value === 1 ? '已认证' : '未认证'
    case 'status':
      return value === 1 ? '正常' : value === 2 ? '未激活' : '禁用'
    default:
      return value
  }
}

// 获取字段标签
const getFieldLabel = (fieldName) => {
  const labels = {
    realName: '真实姓名',
    gender: '性别',
    birthday: '生日',
    idCard: '身份证号',
    email: '邮箱',
    wechat: '微信号',
    qq: 'QQ号',
    job: '职业',
    company: '工作单位',
    monthlyIncome: '月收入',
    rentalBudgetMin: '最低预算',
    rentalBudgetMax: '最高预算',
    preferredDistricts: '偏好区域',
    houseRequirements: '租房需求'
  }
  return labels[fieldName] || fieldName
}

// 获取头像URL
const getAvatarUrl = () => {
  
  if (!user.value || !user.value.avatar) {
    console.log('使用默认头像')
    return '/src/assets/image/default-avatar.png'
  }
  
  const avatarUrl = `/src/assets/image/${user.value.avatar}?t=${new Date().getTime()}`

  return avatarUrl
}

// 处理头像文件选择
const handleAvatarSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    message.value = '请选择图片文件'
    return
  }
  
  // 检查文件大小（限制为2MB）
  if (file.size > 2 * 1024 * 1024) {
    message.value = '图片大小不能超过2MB'
    return
  }
  
  avatarFile.value = file
  
  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    const preview = document.getElementById('avatar-preview')
    if (preview) {
      preview.src = e.target.result
    }
  }
  reader.readAsDataURL(file)
}

// 上传头像
const uploadAvatar = async () => {
  if (!avatarFile.value) {
    message.value = '请先选择头像图片'
    return
  }
  
  avatarUploading.value = true
  message.value = ''
  
  try {
    const formData = new FormData()
    formData.append('avatar', avatarFile.value)
    formData.append('userId', user.value.id)
    
    const response = await axios.post(`${API_BASE_URL}/user/upload-avatar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.success) {
      message.value = '头像上传成功'
      // 更新用户信息
      user.value.avatar = response.data.avatar
      localStorage.setItem('user', JSON.stringify(user.value))
      // 重新获取最新用户信息
      await fetchUserInfo(user.value.id)
      // 重置文件输入
      avatarFile.value = null
      const fileInput = document.getElementById('avatar-input')
      if (fileInput) {
        fileInput.value = ''
      }
    } else {
      message.value = response.data.message
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    message.value = '上传失败，请稍后重试'
  } finally {
    avatarUploading.value = false
  }
}

</script>

<template>
  <div class="user-profile-container">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-left">
        <button @click="goBack" class="button back-button">
          <div class="outline"></div>
          <div class="state state--default">
            <div class="icon">
              <svg
                width="1em"
                height="1em"
                viewBox="0 0 24 24"
                fill="currentColor"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M14.2199 21.63C13.0399 21.63 11.3699 20.8 10.0499 16.83L9.32988 14.67L7.16988 13.95C3.20988 12.63 2.37988 10.96 2.37988 9.78001C2.37988 8.61001 3.20988 6.93001 7.16988 5.60001L15.6599 2.77001C17.7799 2.06001 19.5499 2.27001 20.6399 3.35001C21.7299 4.43001 21.9399 6.21001 21.2299 8.33001L18.3999 16.82C17.0699 20.8 15.3999 21.63 14.2199 21.63ZM7.63988 7.03001C4.85988 7.96001 3.86988 9.06001 3.86988 9.78001C3.86988 10.5 4.85988 11.6 7.63988 12.52L10.1599 13.36C10.3799 13.43 10.5599 13.61 10.6299 13.83L11.4699 16.35C12.3899 19.13 13.4999 20.12 14.2199 20.12C14.9399 20.12 16.0399 19.13 16.9699 16.35L19.7999 7.86001C20.3099 6.32001 20.2199 5.06001 19.5699 4.41001C18.9199 3.76001 17.6599 3.68001 16.1299 4.19001L7.63988 7.03001Z"
                ></path>
                <path
                  d="M10.11 14.4C9.92005 14.4 9.73005 14.33 9.58005 14.18C9.29005 13.89 9.29005 13.41 9.58005 13.12L13.16 9.53C13.45 9.24 13.93 9.24 14.22 9.53C14.51 9.82 14.51 10.3 14.22 10.59L10.64 14.18C10.5 14.33 10.3 14.4 10.11 14.4Z"
                ></path>
              </svg>
            </div>
            <p>
              <span style="--i:0">返</span>
              <span style="--i:1">回</span>
            </p>
          </div>
        </button>
      </div>
      <div class="nav-center">
        <h2>个人信息</h2>
      </div>
      <div class="nav-right">
        <!-- 保存按钮 -->
        <div v-if="isEditing" class="action-buttons">
          <button @click="cancelEdit" class="cancel-btn">取消</button>
          <button @click="saveUserInfo" :disabled="saveLoading" class="save-btn">
            {{ saveLoading ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </nav>

    <!-- 消息提示 -->
    <div v-if="message" class="message" :class="{ 'success': message.includes('成功'), 'error': message.includes('失败') }">
      {{ message }}
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-section">
      <p>正在加载用户信息...</p>
    </div>

    <!-- 用户信息内容 -->
    <div v-else-if="user" class="profile-content">
      <!-- 头像卡片 -->
      <div class="info-card avatar-card">
        <h3 class="card-title">头像设置</h3>
        <div class="avatar-section">
          <div class="avatar-preview">
            <img :src="getAvatarUrl()" id="avatar-preview" alt="用户头像" class="avatar-image">
          </div>
          <div class="avatar-actions">
            <div class="file-input-container">
              <input 
                type="file" 
                id="avatar-input"
                accept="image/*" 
                @change="handleAvatarSelect" 
                class="file-input"
                :disabled="avatarUploading"
              >
              <label for="avatar-input" class="button select-avatar-button">
                <div class="outline"></div>
                <div class="state state--default">
                  <div class="icon">
                    <svg
                      width="1em"
                      height="1em"
                      viewBox="0 0 24 24"
                      fill="currentColor"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M14.2199 21.63C13.0399 21.63 11.3699 20.8 10.0499 16.83L9.32988 14.67L7.16988 13.95C3.20988 12.63 2.37988 10.96 2.37988 9.78001C2.37988 8.61001 3.20988 6.93001 7.16988 5.60001L15.6599 2.77001C17.7799 2.06001 19.5499 2.27001 20.6399 3.35001C21.7299 4.43001 21.9399 6.21001 21.2299 8.33001L18.3999 16.82C17.0699 20.8 15.3999 21.63 14.2199 21.63ZM7.63988 7.03001C4.85988 7.96001 3.86988 9.06001 3.86988 9.78001C3.86988 10.5 4.85988 11.6 7.63988 12.52L10.1599 13.36C10.3799 13.43 10.5599 13.61 10.6299 13.83L11.4699 16.35C12.3899 19.13 13.4999 20.12 14.2199 20.12C14.9399 20.12 16.0399 19.13 16.9699 16.35L19.7999 7.86001C20.3099 6.32001 20.2199 5.06001 19.5699 4.41001C18.9199 3.76001 17.6599 3.68001 16.1299 4.19001L7.63988 7.03001Z"
                      ></path>
                      <path
                        d="M10.11 14.4C9.92005 14.4 9.73005 14.33 9.58005 14.18C9.29005 13.89 9.29005 13.41 9.58005 13.12L13.16 9.53C13.45 9.24 13.93 9.24 14.22 9.53C14.51 9.82 14.51 10.3 14.22 10.59L10.64 14.18C10.5 14.33 10.3 14.4 10.11 14.4Z"
                      ></path>
                    </svg>
                  </div>
                  <p>
                    <span style="--i:0">{{ avatarFile ? '重' : '选' }}</span>
                    <span style="--i:1">{{ avatarFile ? '新' : '择' }}</span>
                    <span style="--i:2">{{ avatarFile ? '选' : '头' }}</span>
                    <span style="--i:3">{{ avatarFile ? '择' : '像' }}</span>
                  </p>
                </div>
              </label>
            </div>
            <button 
              @click="uploadAvatar" 
              :disabled="!avatarFile || avatarUploading" 
              class="button upload-avatar-button"
            >
              <div class="outline"></div>
              <div class="state state--default">
                <div class="icon">
                  <svg
                    width="1em"
                    height="1em"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M14.2199 21.63C13.0399 21.63 11.3699 20.8 10.0499 16.83L9.32988 14.67L7.16988 13.95C3.20988 12.63 2.37988 10.96 2.37988 9.78001C2.37988 8.61001 3.20988 6.93001 7.16988 5.60001L15.6599 2.77001C17.7799 2.06001 19.5499 2.27001 20.6399 3.35001C21.7299 4.43001 21.9399 6.21001 21.2299 8.33001L18.3999 16.82C17.0699 20.8 15.3999 21.63 14.2199 21.63ZM7.63988 7.03001C4.85988 7.96001 3.86988 9.06001 3.86988 9.78001C3.86988 10.5 4.85988 11.6 7.63988 12.52L10.1599 13.36C10.3799 13.43 10.5599 13.61 10.6299 13.83L11.4699 16.35C12.3899 19.13 13.4999 20.12 14.2199 20.12C14.9399 20.12 16.0399 19.13 16.9699 16.35L19.7999 7.86001C20.3099 6.32001 20.2199 5.06001 19.5699 4.41001C18.9199 3.76001 17.6599 3.68001 16.1299 4.19001L7.63988 7.03001Z"
                    ></path>
                    <path
                      d="M10.11 14.4C9.92005 14.4 9.73005 14.33 9.58005 14.18C9.29005 13.89 9.29005 13.41 9.58005 13.12L13.16 9.53C13.45 9.24 13.93 9.24 14.22 9.53C14.51 9.82 14.51 10.3 14.22 10.59L10.64 14.18C10.5 14.33 10.3 14.4 10.11 14.4Z"
                    ></path>
                  </svg>
                </div>
                <p>
                  <span style="--i:0">{{ avatarUploading ? '上' : '上' }}</span>
                  <span style="--i:1">{{ avatarUploading ? '传' : '传' }}</span>
                  <span style="--i:2">{{ avatarUploading ? '中' : '头' }}</span>
                  <span style="--i:3">{{ avatarUploading ? '...' : '像' }}</span>
                </p>
              </div>
            </button>
          </div>
          <div class="avatar-tips">
            <p>支持 JPG、PNG 格式，大小不超过 2MB</p>
          </div>
        </div>
      </div>

      <!-- 基本信息卡片 -->
      <div class="info-card">
        <h3 class="card-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>用户名：</label>
            <span>{{ user.username }}</span>
          </div>
          <div class="info-item">
            <label>真实姓名：</label>
            <div v-if="!isEditing || editingField !== 'realName'" 
                 class="editable-field" 
                 @click="startEditField('realName')">
              {{ getFieldDisplayValue('realName') }}
            </div>
            <input v-else v-model="editForm.realName" class="edit-input" type="text" placeholder="请输入真实姓名">
          </div>
          <div class="info-item">
            <label>手机号：</label>
            <span>{{ user.phone }}</span>
          </div>
          <div class="info-item">
            <label>用户类型：</label>
            <span>{{ userTypeMap[user.userType] || '未知' }}</span>
          </div>
          <div class="info-item">
            <label>注册时间：</label>
            <span>{{ formatDateTime(user.registerTime) }}</span>
          </div>
          <div class="info-item">
            <label>最后登录：</label>
            <span>{{ user.lastLoginTime ? formatDateTime(user.lastLoginTime) : '从未登录' }}</span>
          </div>
        </div>
      </div>

      <!-- 身份信息卡片 -->
      <div class="info-card">
        <h3 class="card-title">身份信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>性别：</label>
            <div v-if="!isEditing || editingField !== 'gender'" 
                 class="editable-field" 
                 @click="startEditField('gender')">
              {{ getFieldDisplayValue('gender') }}
            </div>
            <select v-else v-model="editForm.gender" class="edit-input">
              <option value="0">未知</option>
              <option value="1">男</option>
              <option value="2">女</option>
            </select>
          </div>
          <div class="info-item">
            <label>生日：</label>
            <div v-if="!isEditing || editingField !== 'birthday'" 
                 class="editable-field" 
                 @click="startEditField('birthday')">
              {{ getFieldDisplayValue('birthday') }}
            </div>
            <input v-else v-model="editForm.birthday" class="edit-input" type="date">
          </div>
          <div class="info-item">
            <label>身份证号：</label>
            <div v-if="!isEditing || editingField !== 'idCard'" 
                 class="editable-field" 
                 @click="startEditField('idCard')">
              {{ getFieldDisplayValue('idCard') }}
            </div>
            <input v-else v-model="editForm.idCard" class="edit-input" type="text" placeholder="请输入身份证号">
          </div>
          <div class="info-item">
            <label>实名认证：</label>
            <span :class="{ 'certified': user.isCertified === 1, 'not-certified': user.isCertified !== 1 }">
              {{ getFieldDisplayValue('isCertified') }}
            </span>
          </div>
          <div class="info-item" v-if="user.isCertified === 1">
            <label>认证时间：</label>
            <span>{{ formatDateTime(user.certificationTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 联系信息卡片 -->
      <div class="info-card">
        <h3 class="card-title">联系信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>邮箱：</label>
            <div v-if="!isEditing || editingField !== 'email'" 
                 class="editable-field" 
                 @click="startEditField('email')">
              {{ getFieldDisplayValue('email') }}
            </div>
            <input v-else v-model="editForm.email" class="edit-input" type="email" placeholder="请输入邮箱">
          </div>
          <div class="info-item">
            <label>微信号：</label>
            <div v-if="!isEditing || editingField !== 'wechat'" 
                 class="editable-field" 
                 @click="startEditField('wechat')">
              {{ getFieldDisplayValue('wechat') }}
            </div>
            <input v-else v-model="editForm.wechat" class="edit-input" type="text" placeholder="请输入微信号">
          </div>
          <div class="info-item">
            <label>QQ号：</label>
            <div v-if="!isEditing || editingField !== 'qq'" 
                 class="editable-field" 
                 @click="startEditField('qq')">
              {{ getFieldDisplayValue('qq') }}
            </div>
            <input v-else v-model="editForm.qq" class="edit-input" type="text" placeholder="请输入QQ号">
          </div>
        </div>
      </div>

      <!-- 职业信息卡片 -->
      <div class="info-card">
        <h3 class="card-title">职业信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>职业：</label>
            <div v-if="!isEditing || editingField !== 'job'" 
                 class="editable-field" 
                 @click="startEditField('job')">
              {{ getFieldDisplayValue('job') }}
            </div>
            <input v-else v-model="editForm.job" class="edit-input" type="text" placeholder="请输入职业">
          </div>
          <div class="info-item">
            <label>工作单位：</label>
            <div v-if="!isEditing || editingField !== 'company'" 
                 class="editable-field" 
                 @click="startEditField('company')">
              {{ getFieldDisplayValue('company') }}
            </div>
            <input v-else v-model="editForm.company" class="edit-input" type="text" placeholder="请输入工作单位">
          </div>
          <div class="info-item">
            <label>月收入：</label>
            <div v-if="!isEditing || editingField !== 'monthlyIncome'" 
                 class="editable-field" 
                 @click="startEditField('monthlyIncome')">
              {{ getFieldDisplayValue('monthlyIncome') }}
            </div>
            <input v-else v-model="editForm.monthlyIncome" class="edit-input" type="number" placeholder="请输入月收入">
          </div>
        </div>
      </div>

      <!-- 租房偏好卡片 -->
      <div class="info-card">
        <h3 class="card-title">租房偏好</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>最低预算：</label>
            <div v-if="!isEditing || editingField !== 'rentalBudgetMin'" 
                 class="editable-field" 
                 @click="startEditField('rentalBudgetMin')">
              {{ getFieldDisplayValue('rentalBudgetMin') }}
            </div>
            <input v-else v-model="editForm.rentalBudgetMin" class="edit-input" type="number" placeholder="请输入最低预算">
          </div>
          <div class="info-item">
            <label>最高预算：</label>
            <div v-if="!isEditing || editingField !== 'rentalBudgetMax'" 
                 class="editable-field" 
                 @click="startEditField('rentalBudgetMax')">
              {{ getFieldDisplayValue('rentalBudgetMax') }}
            </div>
            <input v-else v-model="editForm.rentalBudgetMax" class="edit-input" type="number" placeholder="请输入最高预算">
          </div>
          <div class="info-item full-width">
            <label>偏好区域：</label>
            <div v-if="!isEditing || editingField !== 'preferredDistricts'" 
                 class="editable-field" 
                 @click="startEditField('preferredDistricts')">
              {{ getFieldDisplayValue('preferredDistricts') }}
            </div>
            <input v-else v-model="editForm.preferredDistricts" class="edit-input" type="text" placeholder="请输入偏好区域">
          </div>
          <div class="info-item full-width">
            <label>租房需求：</label>
            <div v-if="!isEditing || editingField !== 'houseRequirements'" 
                 class="editable-field" 
                 @click="startEditField('houseRequirements')">
              {{ getFieldDisplayValue('houseRequirements') }}
            </div>
            <textarea v-else v-model="editForm.houseRequirements" class="edit-textarea" placeholder="请输入租房需求"></textarea>
          </div>
        </div>
      </div>

      <!-- 账户状态卡片 -->
      <div class="info-card">
        <h3 class="card-title">账户状态</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>账户状态：</label>
            <span :class="{
              'status-active': user.status === 1,
              'status-inactive': user.status === 2,
              'status-disabled': user.status === 0
            }">
              {{ getFieldDisplayValue('status') }}
            </span>
          </div>
          <div class="info-item">
            <label>信用分：</label>
            <span :class="{
              'credit-high': user.creditScore >= 80,
              'credit-medium': user.creditScore >= 60 && user.creditScore < 80,
              'credit-low': user.creditScore < 60
            }">
              {{ user.creditScore }}分
            </span>
          </div>
          <div class="info-item">
            <label>最后更新：</label>
            <span>{{ formatDateTime(user.updatedTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 未登录提示 -->
    <div v-else class="not-logged-in">
      <p>请先登录查看个人信息</p>
      <button @click="router.push('/login')" class="login-btn">去登录</button>
    </div>
  </div>
</template>

<style scoped>
.user-profile-container {
  min-height: 100vh;
  background: #ffffff;
  padding-bottom: 2rem;
  position: relative;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-center h2 {
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
  font-size: 1.5rem;
}

.back-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.action-buttons {
  display: flex;
  gap: 0.75rem;
}

.cancel-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(108, 117, 125, 0.3);
}

.cancel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(108, 117, 125, 0.4);
}

.save-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.save-btn:disabled {
  background: #6c757d;
  transform: none;
  box-shadow: none;
  cursor: not-allowed;
}

.message {
  padding: 1rem 2rem;
  margin: 1rem 2rem;
  border-radius: 12px;
  font-weight: 500;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.message.success {
  background: rgba(40, 167, 69, 0.1);
  color: #155724;
  border: 2px solid rgba(40, 167, 69, 0.3);
}

.message.error {
  background: rgba(220, 53, 69, 0.1);
  color: #721c24;
  border: 2px solid rgba(220, 53, 69, 0.3);
}

.loading-section {
  text-align: center;
  padding: 3rem;
  color: #666;
  font-size: 1.2rem;
}

.profile-content {
  padding: 1rem;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  width: 100%;
  box-sizing: border-box;
}

.info-card {
  background: white;
  border-radius: 8px;
  padding: 1.25rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  transition: all 0.2s ease;
  height: fit-content;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-card {
  text-align: center;
  grid-column: 1 / -1;
}

.card-title {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
}

.file-input-container {
  position: relative;
}

.file-input {
  display: none;
}

.file-input-label {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  font-size: 0.9rem;
}

.file-input-label:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.upload-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  font-size: 0.9rem;
}

.upload-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.upload-btn:disabled {
  background: #6c757d;
  transform: none;
  box-shadow: none;
  cursor: not-allowed;
}

.avatar-tips {
  color: #6c757d;
  font-size: 0.8rem;
  text-align: center;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.75rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.info-item label {
  font-weight: 600;
  color: #495057;
  margin-bottom: 0.25rem;
  font-size: 0.85rem;
}

.info-item span {
  color: #2c3e50;
  font-size: 1rem;
  font-weight: 500;
}

.editable-field {
  color: #2c3e50;
  font-size: 1rem;
  font-weight: 500;
  padding: 0.25rem 0;
  cursor: pointer;
  border-bottom: 2px dashed transparent;
  transition: all 0.3s ease;
  border-radius: 4px;
}

.editable-field:hover {
  background: rgba(102, 126, 234, 0.1);
  border-bottom: 2px dashed #667eea;
  padding: 0.25rem 0.5rem;
}

.edit-input, .edit-textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  background: white;
}

.edit-input:focus, .edit-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.edit-textarea {
  min-height: 60px;
  resize: vertical;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

/* 状态样式 */
.certified {
  color: #155724;
  font-weight: 600;
  background: #d4edda;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  display: inline-block;
  border: 1px solid #c3e6cb;
}

.not-certified {
  color: #721c24;
  font-weight: 600;
  background: #f8d7da;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  display: inline-block;
  border: 1px solid #f5c6cb;
}

.status-active {
  color: #155724;
  font-weight: 600;
  background: #d4edda;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  display: inline-block;
  border: 1px solid #c3e6cb;
}

.status-inactive {
  color: #856404;
  font-weight: 600;
  background: #fff3cd;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  display: inline-block;
  border: 1px solid #ffeaa7;
}

.status-disabled {
  color: #721c24;
  font-weight: 600;
  background: #f8d7da;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  display: inline-block;
  border: 1px solid #f5c6cb;
}

.credit-high {
  color: #155724;
  font-weight: 600;
  background: #d4edda;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  display: inline-block;
  border: 1px solid #c3e6cb;
}

.credit-medium {
  color: #856404;
  font-weight: 600;
  background: #fff3cd;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  display: inline-block;
  border: 1px solid #ffeaa7;
}

.credit-low {
  color: #721c24;
  font-weight: 600;
  background: #f8d7da;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  display: inline-block;
  border: 1px solid #f5c6cb;
}

.not-logged-in {
  text-align: center;
  padding: 4rem 2rem;
  color: #666;
  background: white;
  border-radius: 12px;
  margin: 2rem;
  border: 1px solid #e0e0e0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.not-logged-in p {
  margin-bottom: 2rem;
  font-size: 1.3rem;
  font-weight: 500;
}

.login-btn {
  padding: 1rem 2.5rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 特殊按钮样式 */
.button {
  --primary: #667eea;
  --neutral-1: #f7f8f7;
  --neutral-2: #e7e7e7;
  --radius: 14px;

  cursor: pointer;
  border-radius: var(--radius);
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.3);
  border: none;
  box-shadow: 0 0.5px 0.5px 1px rgba(255, 255, 255, 0.2),
    0 10px 20px rgba(0, 0, 0, 0.2), 0 4px 5px 0px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s ease;
  min-width: 120px;
  padding: 12px;
  height: 48px;
  font-family: "Galano Grotesque", Poppins, Montserrat, sans-serif;
  font-style: normal;
  font-size: 14px;
  font-weight: 600;
}

.button:hover {
  transform: scale(1.02);
  box-shadow: 0 0 1px 2px rgba(255, 255, 255, 0.3),
    0 15px 30px rgba(0, 0, 0, 0.3), 0 10px 3px -3px rgba(0, 0, 0, 0.04);
}

.button:active {
  transform: scale(1);
  box-shadow: 0 0 1px 2px rgba(255, 255, 255, 0.3),
    0 10px 3px -3px rgba(0, 0, 0, 0.2);
}

.button:after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: var(--radius);
  border: 2.5px solid transparent;
  background: linear-gradient(var(--neutral-1), var(--neutral-2)) padding-box,
    linear-gradient(to bottom, rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.45))
      border-box;
  z-index: 0;
  transition: all 0.4s ease;
}

.button:hover::after {
  transform: scale(1.05, 1.1);
  box-shadow: inset 0 -1px 3px 0 rgba(255, 255, 255, 1);
}

.button::before {
  content: "";
  inset: 5px 4px 4px 4px;
  position: absolute;
  background: linear-gradient(to top, var(--neutral-1), var(--neutral-2));
  border-radius: 20px;
  filter: blur(0.5px);
  z-index: 2;
}

.state p {
  display: flex;
  align-items: center;
  justify-content: center;
}

.state .icon {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  margin: auto;
  transform: scale(1.15);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.state .icon svg {
  overflow: visible;
}

/* Outline */
.outline {
  position: absolute;
  border-radius: inherit;
  overflow: hidden;
  z-index: 1;
  opacity: 0;
  transition: opacity 0.4s ease;
  inset: -2px -3.5px;
}

.outline::before {
  content: "";
  position: absolute;
  inset: -100%;
  background: conic-gradient(
    from 180deg,
    transparent 60%,
    white 80%,
    transparent 100%
  );
  animation: spin 2s linear infinite;
  animation-play-state: paused;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.button:hover .outline {
  opacity: 1;
}

.button:hover .outline::before {
  animation-play-state: running;
}

/* Letters */
.state p span {
  display: block;
  opacity: 0;
  animation: slideDown 0.8s ease forwards calc(var(--i) * 0.03s);
}

.button:hover p span {
  opacity: 1;
  animation: wave 0.5s ease forwards calc(var(--i) * 0.02s);
}

.button:focus p span {
  opacity: 1;
  animation: disapear 0.6s ease forwards calc(var(--i) * 0.03s);
}

@keyframes wave {
  30% {
    opacity: 1;
    transform: translateY(4px) translateX(0) rotate(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-3px) translateX(0) rotate(0);
    color: var(--primary);
  }
  100% {
    opacity: 1;
    transform: translateY(0) translateX(0) rotate(0);
  }
}

@keyframes slideDown {
  0% {
    opacity: 0;
    transform: translateY(-20px) translateX(5px) rotate(-90deg);
    color: var(--primary);
    filter: blur(5px);
  }
  30% {
    opacity: 1;
    transform: translateY(4px) translateX(0) rotate(0);
    filter: blur(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-3px) translateX(0) rotate(0);
  }
  100% {
    opacity: 1;
    transform: translateY(0) translateX(0) rotate(0);
  }
}

@keyframes disapear {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
    transform: translateX(5px) translateY(20px);
    color: var(--primary);
    filter: blur(5px);
  }
}

/* Plane */
.state--default .icon svg {
  animation: land 0.6s ease forwards;
}

.button:hover .state--default .icon {
  transform: rotate(45deg) scale(1.15);
}

.button:focus .state--default svg {
  animation: takeOff 0.8s linear forwards;
}

.button:focus .state--default .icon {
  transform: rotate(0) scale(1.15);
}

@keyframes takeOff {
  0% {
    opacity: 1;
  }
  60% {
    opacity: 1;
    transform: translateX(70px) rotate(45deg) scale(2);
  }
  100% {
    opacity: 0;
    transform: translateX(160px) rotate(45deg) scale(0);
  }
}

@keyframes land {
  0% {
    transform: translateX(-60px) translateY(30px) rotate(-50deg) scale(2);
    opacity: 0;
    filter: blur(3px);
  }
  100% {
    transform: translateX(0) translateY(0) rotate(0);
    opacity: 1;
    filter: blur(0);
  }
}

/* Contrail */
.state--default .icon:before {
  content: "";
  position: absolute;
  top: 50%;
  height: 2px;
  width: 0;
  left: -5px;
  background: linear-gradient(to right, transparent, rgba(0, 0, 0, 0.5));
}

.button:focus .state--default .icon:before {
  animation: contrail 0.8s linear forwards;
}

@keyframes contrail {
  0% {
    width: 0;
    opacity: 1;
  }
  8% {
    width: 15px;
  }
  60% {
    opacity: 0.7;
    width: 80px;
  }
  100% {
    opacity: 0;
    width: 160px;
  }
}

/* States */
.state {
  padding-left: 25px;
  z-index: 2;
  display: flex;
  position: relative;
}

.state--default span:nth-child(4) {
  margin-right: 5px;
}

.state--sent {
  display: none;
}

.state--sent svg {
  transform: scale(1.15);
  margin-right: 8px;
}

.button:focus .state--default {
  position: absolute;
}

.button:focus .state--sent {
  display: flex;
}

.button:focus .state--sent span {
  opacity: 0;
  animation: slideDown 0.8s ease forwards calc(var(--i) * 0.2s);
}

.button:focus .state--sent .icon svg {
  opacity: 0;
  animation: appear 1.2s ease forwards 0.8s;
}

@keyframes appear {
  0% {
    opacity: 0;
    transform: scale(4) rotate(-40deg);
    color: var(--primary);
    filter: blur(4px);
  }
  30% {
    opacity: 1;
    transform: scale(0.6);
    filter: blur(1px);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
    filter: blur(0);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 移除旧的按钮样式 */
.back-btn,
.file-input-label,
.upload-btn {
  display: none;
}

/* 特定按钮样式调整 */
.back-button,
.select-avatar-button,
.upload-avatar-button {
  min-width: 120px;
}

/* 文件输入容器调整 */
.file-input-container {
  position: relative;
  display: inline-block;
}

.file-input {
  display: none;
}

@media (max-width: 768px) {
  .navbar {
    padding: 1rem;
    flex-direction: column;
    gap: 1rem;
  }
  
  .profile-content {
    padding: 1rem;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .info-card {
    padding: 1.5rem;
    margin-bottom: 1.5rem;
  }
  
  .avatar-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .select-avatar-button,
  .upload-avatar-button {
    width: 100%;
    text-align: center;
  }
}
</style>
