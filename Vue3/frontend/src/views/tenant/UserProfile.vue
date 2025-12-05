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
const API_BASE_URL = 'https://api.homesee.xyz/api'

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
  username: '',
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
  
  if (!user.value) return
  
  editForm.username = user.value.username || ''
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
    return '/models/image/default-avatar.png'
  }
  
  // 如果是完整的HTTP URL，直接使用
  if (user.value.avatar.startsWith('http')) {
    return user.value.avatar
  }
  
  // 放弃本地路径，全部走文件服务器
  const FILE_SERVER_HOST = 'https://files.homesee.xyz'
  return `${FILE_SERVER_HOST}/api/files/download/${user.value.avatar}`
}

// 监听用户头像变化，输出调试信息
import { watch } from 'vue'
watch(() => user.value?.avatar, (newVal) => {
  console.log('=== 头像调试信息 ===')
  console.log('当前用户:', user.value?.username)
  console.log('数据库存储文件名:', newVal || '无')
  console.log('最终加载URL:', getAvatarUrl())
  console.log('==================')
}, { immediate: true })

// 处理头像文件选择
const handleAvatarSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    message.value = '请选择图片文件'
    return
  }
  
  // 检查文件大小（限制为10MB）
  if (file.size > 10 * 1024 * 1024) {
    message.value = '图片大小不能超过10MB'
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
    // 1. 上传到文件服务器
    const formData = new FormData()
    formData.append('file', avatarFile.value)
    
    // 使用确定的文件服务器地址
    const FILE_SERVER_HOST = 'https://files.homesee.xyz'
    
    const uploadResponse = await axios.post(`${FILE_SERVER_HOST}/api/files/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (!uploadResponse.data.success) {
      message.value = '文件上传失败: ' + (uploadResponse.data.error || '未知错误')
      return
    }
    
    // 获取上传后的文件名 - 使用服务器返回的文件名（已经是原始文件名）
    const fileName = uploadResponse.data.fileName
    
    console.log('服务器返回文件名:', fileName)
    
    // 2. 更新后端用户信息 - 只保存文件名
    const updatePayload = { avatar: fileName }
    
    const updateResponse = await axios.put(`${API_BASE_URL}/user/update/${user.value.id}`, updatePayload)
    
    if (updateResponse.data.success) {
      message.value = '头像上传成功'
      // 更新用户信息
      user.value.avatar = fileName
      localStorage.setItem('user', JSON.stringify(user.value))
      
      // 重置文件输入
      avatarFile.value = null
      const fileInput = document.getElementById('avatar-input')
      if (fileInput) {
        fileInput.value = ''
      }
    } else {
      message.value = updateResponse.data.message
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    message.value = '上传失败: ' + (error.response?.data?.message || error.message)
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
      <!-- 左侧：头像卡片 -->
      <div class="left-column">
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
          </div>
        </div>
      </div>

      <!-- 右侧：信息网格 -->
      <div class="right-column">
        <!-- 基本信息卡片 -->
        <div class="info-card">
          <h3 class="card-title">基本信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>用户名：</label>
              <div v-if="!isEditing || editingField !== 'username'" 
                   class="editable-field" 
                   @click="startEditField('username')">
                {{ user.username }}
              </div>
              <input v-else v-model="editForm.username" class="edit-input" type="text" placeholder="请输入用户名">
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
  height: 100vh;
  background: #f0f2f5;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1.5rem;
  background-color: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
  flex-shrink: 0;
  height: 60px; /* Fixed height for calculation */
}

.nav-center h2 {
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
  font-size: 1.2rem;
  font-family: "Galano Grotesque", Poppins, Montserrat, sans-serif;
}

.action-buttons {
  display: flex;
  gap: 0.75rem;
}

.cancel-btn, .save-btn {
  padding: 0.5rem 1rem;
  border-radius: 14px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.2s ease;
  font-family: "Galano Grotesque", Poppins, Montserrat, sans-serif;
}

.cancel-btn {
  background: #e7e7e7;
  color: #666;
}

.save-btn {
  background: #667eea;
  color: white;
}

.message {
  position: absolute;
  top: 4rem;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.5rem 1.5rem;
  border-radius: 14px;
  font-weight: 500;
  z-index: 200;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.message.success {
  background: #d4edda;
  color: #155724;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
}

.loading-section {
  text-align: center;
  padding: 3rem;
  color: #666;
}

/* Layout Styles */
.profile-content {
  flex: 1;
  padding: 1rem;
  display: flex;
  gap: 1rem;
  overflow: hidden; /* Prevent outer scroll, let grid handle it if needed */
  width: 100%;
  box-sizing: border-box;
}

.left-column {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.right-column {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3 Columns */
  grid-template-rows: repeat(2, 1fr);    /* 2 Rows */
  gap: 1rem;
  height: 100%; /* Fill the available height */
}

/* Theme Variables matching the button */
.info-card {
  --neutral-1: #f7f8f7;
  --neutral-2: #e7e7e7;
  --radius: 14px;

  background: linear-gradient(to bottom right, var(--neutral-1), var(--neutral-2));
  border-radius: var(--radius);
  padding: 1rem;
  box-shadow: 
    0 0.5px 0.5px 1px rgba(255, 255, 255, 0.5),
    0 4px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.4);
  font-family: "Galano Grotesque", Poppins, Montserrat, sans-serif;
  display: flex;
  flex-direction: column;
  overflow-y: auto; /* Allow scroll inside card if content is too big */
}

.avatar-card {
  height: 100%; /* Fill left column height */
  align-items: center;
  justify-content: center;
}

.card-title {
  margin: 0 0 0.75rem 0;
  color: #2c3e50;
  font-size: 1rem;
  font-weight: 600;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  width: 100%;
  flex-shrink: 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  width: 100%;
  flex: 1;
  justify-content: center;
}

.avatar-preview {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-actions {
  display: flex;
  gap: 1rem;
  flex-direction: column;
  width: 100%;
  align-items: center;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
  justify-content: space-evenly; /* Distribute items evenly */
}

.info-item {
  display: flex;
  flex-direction: row; /* Horizontal layout for items to save vertical space */
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0.8rem;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: inset 0 1px 2px rgba(0,0,0,0.02);
}

.info-item label {
  font-weight: 600;
  color: #666;
  font-size: 0.8rem;
  margin: 0;
  flex-shrink: 0;
}

.info-item span, .editable-field {
  color: #2c3e50;
  font-size: 0.9rem;
  font-weight: 500;
  text-align: right;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 60%;
}

.editable-field {
  cursor: pointer;
  transition: color 0.2s;
}

.editable-field:hover {
  color: #667eea;
}

.edit-input, .edit-textarea {
  width: 60%;
  padding: 0.2rem 0.4rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
  font-family: inherit;
  text-align: right;
}

.edit-input:focus, .edit-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.edit-textarea {
  min-height: 40px;
  resize: none;
  text-align: left;
}

/* Special handling for Rental Preferences to allow wrapping */
.info-item.full-width {
  flex-direction: column;
  align-items: flex-start;
  gap: 0.2rem;
}

.info-item.full-width span, .info-item.full-width .editable-field {
  text-align: left;
  max-width: 100%;
  white-space: normal;
}

.info-item.full-width .edit-input, .info-item.full-width .edit-textarea {
  width: 100%;
  text-align: left;
}

/* Status Badges - Simplified */
.certified, .not-certified, .status-active, .status-inactive, .status-disabled, .credit-high, .credit-medium, .credit-low {
  padding: 0.1rem 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
}

.certified, .status-active, .credit-high {
  background: #e6fffa;
  color: #2c7a7b;
}

.not-certified, .status-disabled, .credit-low {
  background: #fff5f5;
  color: #c53030;
}

.status-inactive, .credit-medium {
  background: #fffaf0;
  color: #c05621;
}

/* Special Button Styles from User Request (Preserved but sized) */
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
  min-width: 100px;
  padding: 0 12px;
  height: 40px;
  font-family: "Galano Grotesque", Poppins, Montserrat, sans-serif;
  font-style: normal;
  font-size: 13px;
  font-weight: 600;
  color: #333;
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

/* Responsive */
@media (max-width: 1200px) {
  .right-column {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    overflow-y: auto;
  }
  
  .left-column {
    width: 100%;
    height: auto;
  }
  
  .right-column {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
    height: auto;
  }
  
  .avatar-card {
    flex-direction: row;
    padding: 1rem;
  }
  
  .avatar-section {
    flex-direction: row;
  }
  
  .avatar-actions {
    margin-left: 1rem;
    align-items: flex-start;
  }
}
</style>
