<!--
/**
 * 文件名: LandlordAdmin.vue
 * 作者: 牛迦楠
 * 学校: 东华理工大学
 * 专业: 软件工程（中外合作办学）
 * 课题: 融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 创建日期: 2026-01-06
 * 描述: 房东管理主页面，提供房屋管理、租约管理、租客管理等功能的统一入口
 */
-->
<template>
  <div class="landlord-admin">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-brand">
        <h2>房东管理端</h2>
        <span class="user-info">房东: {{ currentUser }}</span>
      </div>
      <div class="nav-links">
        <router-link to="/" class="client-link">切换到客户端</router-link>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </nav>

    <!-- 侧边栏菜单 -->
    <div class="admin-container">
      <div class="sidebar">
        <div class="menu-section">
          <h3>房屋管理</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'my-houses' }"
              @click="switchTab('my-houses')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8h5z"/>
                </svg>
              </span>
              工作台
            </li>
            <li 
              :class="{ active: activeTab === 'house-status' }"
              @click="switchTab('house-status')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99l1.5 1.5z"/>
                </svg>
              </span>
              房屋状态
            </li>
            <li 
              :class="{ active: activeTab === 'add-house' }"
              @click="switchTab('add-house')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
                </svg>
              </span>
              添加房屋
            </li>
          </ul>
        </div>

        <div class="menu-section">
          <h3>租约管理</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'tenant-management' }"
              @click="switchTab('tenant-management')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
                </svg>
              </span>
              租约列表
            </li>
            <li 
              :class="{ active: activeTab === 'my-tenants' }"
              @click="switchTab('my-tenants')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
              </span>
              我的租户
            </li>
            <li 
              :class="{ active: activeTab === 'tenant-matching' }"
              @click="switchTab('tenant-matching')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
              </span>
              租客匹配
            </li>
            <li 
              :class="{ active: activeTab === 'maintenance' }"
              @click="switchTab('maintenance')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/>
                </svg>
              </span>
              维修报修
            </li>
          </ul>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">

        <!-- 我的房屋 -->
        <div v-if="activeTab === 'my-houses'" class="dashboard-view">
          <!-- 仪表盘统计卡片 -->
          <landlord-dashboard-stats :stats="dashboardStats" />
          
          <!-- 数据驾驶舱图表 -->
          <landlord-data-charts 
            :houses="myHouses"
            :contracts="tenantContracts"
            :statistics="statistics"
          />
        </div>

        <!-- 房屋状态 -->
        <div v-if="activeTab === 'house-status'">
          <house-status 
            :myHouses="myHouses"
            :loadingHouses="loadingHouses"
            :addingHouse="addingHouse"
            :tenantContracts="tenantContracts"
            :pendingAppointments="pendingAppointments"
            @refresh="loadMyHouses"
            @editHouse="editHouse"
            @toggleHouseStatus="toggleHouseStatus"
            @viewHouseDetail="viewHouseDetail"
            @submitAddHouse="submitAddHouse"
          />
        </div>

        <!-- 添加房屋 -->
        <div v-if="activeTab === 'add-house'">
          <add-house 
            @submit="handleAddHouse"
            @cancel="switchTab('my-houses')"
          />
        </div>

        <!-- 租约管理 -->
        <div v-if="activeTab === 'tenant-management'">
          <tenant-management :userPhone="userPhone" @goToTenantMatching="handleGoToTenantMatching" />
        </div>

        <!-- 租客匹配 -->
        <div v-if="activeTab === 'tenant-matching'">
          <landlord-tenant-matching 
            :userPhone="userPhone" 
            :preSelectedData="preSelectedMatchData"
          />
        </div>

        <!-- 我的租户 -->
        <div v-if="activeTab === 'my-tenants'">
          <my-tenants :userPhone="userPhone" :userId="userId" />
        </div>

        <!-- 维修报修 -->
        <div v-if="activeTab === 'maintenance'">
          <landlord-maintenance :userId="userId" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 导入组件
import MyHouses from './components/MyHouses.vue'
import HouseStatus from './components/HouseStatus.vue'
import AddHouse from './components/AddHouse.vue'
import TenantManagement from './components/TenantManagement.vue'
import LandlordTenantMatching from './components/LandlordTenantMatching.vue'
import MyTenants from './components/MyTenants.vue'

import LandlordMaintenance from './components/LandlordMaintenance.vue'
import LandlordDashboardStats from './LandlordDashboardStats.vue'
import LandlordDataCharts from './LandlordDataCharts.vue'

// 占位页面组件
const PlaceholderPage = {
  props: ['title', 'message'],
  template: `
    <div class="page-placeholder">
      <h2>{{ title }}</h2>
      <p>{{ message }}</p>
    </div>
  `
}

const router = useRouter()

// API基础URL
const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const activeTab = ref('my-houses')
const currentUser = ref('')
const userPhone = ref('')
const userId = ref(null)
const statistics = ref({})
const expiringContracts = ref([])

// 待处理维修数量
const pendingMaintenanceCount = ref(0)

// 预选匹配数据（从租约列表跳转时使用）
const preSelectedMatchData = ref(null)

// 处理跳转到租客匹配
const handleGoToTenantMatching = (data) => {
  preSelectedMatchData.value = data
  activeTab.value = 'tenant-matching'
}

// 租户合同数据
const tenantContracts = ref([])

// 加载租户合同数据
const loadTenantContracts = async () => {
  if (!userPhone.value) return
  try {
    const res = await axios.get(`${API_BASE_URL}/landlord/matching/contracts?landlordPhone=${userPhone.value}`)
    if (res.data.success) {
      tenantContracts.value = res.data.contracts || []
    }
  } catch (e) {
    console.error('加载租户合同失败:', e)
  }
}

// 计算即将到期租约（30天内）
const expiringLeasesCount = computed(() => {
  const today = new Date()
  const thirtyDaysLater = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000)
  
  return tenantContracts.value.filter(contract => {
    if (!contract.contractEndDate) return false
    const endDate = new Date(contract.contractEndDate)
    // 合同状态为履行中(2)或已签约(1)且未来30天内到期
    return (contract.contractStatus === 1 || contract.contractStatus === 2) &&
           endDate >= today && endDate <= thirtyDaysLater
  }).length
})

// 计算月租金总额（作为本月收入）
const totalMonthlyRent = computed(() => {
  return tenantContracts.value
    .filter(c => c.contractStatus === 1 || c.contractStatus === 2) // 只计算有效合同
    .reduce((sum, c) => sum + (parseFloat(c.monthlyRent) || 0), 0)
})

// 计算已出租房屋数（从租户合同）
const rentedHousesCount = computed(() => {
  // 获取状态为履行中或已签约的合同的房屋ID（去重）
  const rentedRoomIds = new Set(
    tenantContracts.value
      .filter(c => c.contractStatus === 1 || c.contractStatus === 2)
      .map(c => c.roomId)
  )
  return rentedRoomIds.size
})

// 仪表盘统计数据
const dashboardStats = computed(() => ({
  monthlyIncome: totalMonthlyRent.value,
  occupationRate: statistics.value.totalHouses > 0 
    ? rentedHousesCount.value / statistics.value.totalHouses 
    : 0,
  rentedHouses: rentedHousesCount.value,
  totalHouses: statistics.value.totalHouses || 0,
  pendingMaintenance: pendingMaintenanceCount.value,
  expiringLeases: expiringLeasesCount.value
}))

// 加载待处理维修数量
const loadPendingMaintenance = async () => {
  if (!userId.value) return
  try {
    const res = await axios.get(`${API_BASE_URL}/maintenance/landlord/${userId.value}`)
    if (res.data.success) {
      pendingMaintenanceCount.value = (res.data.data || []).filter(r => r.requestStatus === 0).length
    }
  } catch (e) {
    console.error('加载维修数据失败:', e)
  }
}

// 待确认预约列表（用于预租计数）
const pendingAppointments = ref([])

// 加载待确认预约
// 加载待确认及已确认预约 (用于预租计数)
const loadPendingAppointments = async () => {
  if (!userPhone.value) return
  try {
    const res0 = await axios.get(`${API_BASE_URL}/viewing-appointment/landlord/${userPhone.value}/status/0`)
    const res1 = await axios.get(`${API_BASE_URL}/viewing-appointment/landlord/${userPhone.value}/status/1`)
    
    let appointments = []
    if (res0.data.success) {
      appointments = [...appointments, ...(res0.data.appointments || [])]
    }
    if (res1.data.success) {
      appointments = [...appointments, ...(res1.data.appointments || [])]
    }
    pendingAppointments.value = appointments
  } catch (e) {
    console.error('加载预约数据失败:', e)
  }
}

// 快捷操作处理
const handleQuickAction = (route) => {
  if (route === '/landlord/add') {
    switchTab('add-house')
  } else if (route === '/landlord/maintenance') {
    switchTab('maintenance')
  } else if (route === '/landlord/matching') {
    switchTab('tenant-matching')
  } else if (route === '/landlord/tenants') {
    switchTab('my-tenants')
  }
}

// 房屋管理相关数据
const myHouses = ref([])
const loadingHouses = ref(false)
const showAddHouse = ref(false)
const addingHouse = ref(false)

// 新房屋数据
const newHouse = ref({
  province: '',
  city: '',
  district: '',
  street: '',
  communityName: '',
  buildingUnit: '',
  doorNumber: '',
  roomNumber: '',
  roomArea: null,
  floorInfo: '',
  orientation: '',
  decoration: '2', // 默认简装
  hasElevator: '0', // 默认无电梯
  rentPrice: null,
  waterPrice: 3.5, // 默认水费
  electricPrice: 0.8, // 默认电费
  rentalType: '0', // 默认整租
  status: '0', // 默认可租
  description: ''
})

// 切换标签页时自动加载房屋列表
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'my-houses') {
    loadMyHouses()
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 计算剩余天数
const calculateDaysLeft = (endDate) => {
  if (!endDate) return 0
  const today = new Date()
  const end = new Date(endDate)
  const diffTime = end - today
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays > 0 ? diffDays : 0
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    if (!userPhone.value) {
      console.error('用户手机号不存在')
      return
    }

    const response = await axios.get(`${API_BASE_URL}/landlord/statistics?landlordPhone=${userPhone.value}`)
    if (response.data.success) {
      statistics.value = response.data.data || response.data.statistics || {}
    } else {
      console.error('获取统计数据失败:', response.data.message)
      // 使用默认数据
      statistics.value = {
        totalHouses: 0,
        rentedHouses: 0,
        availableHouses: 0,
        offlineHouses: 0,
        preRentHouses: 0,
        monthlyIncome: 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 使用默认数据
    statistics.value = {
      totalHouses: 0,
      rentedHouses: 0,
      availableHouses: 0,
      offlineHouses: 0,
      preRentHouses: 0,
      monthlyIncome: 0
    }
  }
}

// 加载即将到期的租约
const loadExpiringContracts = async () => {
  try {
    if (!userPhone.value) {
      console.error('用户手机号不存在')
      expiringContracts.value = []
      return
    }

    // 从API获取真实数据
    const response = await axios.get(`${API_BASE_URL}/landlord/expiring-contracts?landlordPhone=${userPhone.value}`)
    if (response.data.success) {
      expiringContracts.value = response.data.contracts || []
    } else {
      console.error('获取即将到期租约失败:', response.data.message)
      expiringContracts.value = []
    }
  } catch (error) {
    console.error('加载即将到期租约失败:', error)
    expiringContracts.value = []
  }
}

// 设置当前用户
const setCurrentUser = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  currentUser.value = user.realName || '房东用户'
  userPhone.value = user.phone || ''
  userId.value = user.id || null
}

// 加载我的房屋列表
const loadMyHouses = async () => {
  if (!userPhone.value) {
    console.error('用户手机号不存在')
    return
  }

  loadingHouses.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/landlord/houses?landlordPhone=${userPhone.value}`)
    if (response.data.success) {
      // 兼容多种 API 返回格式
      myHouses.value = response.data.data || response.data.houses || response.data.rooms || []
    } else {
      console.error('获取房屋列表失败:', response.data.message)
    }
  } catch (error) {
    console.error('加载房屋列表失败:', error)
  } finally {
    loadingHouses.value = false
  }
}

// 获取房屋完整地址
const getHouseFullAddress = (house) => {
  const parts = [
    house.province,
    house.city,
    house.district,
    house.street,
    house.communityName,
    house.buildingUnit,
    house.doorNumber,
    house.roomNumber
  ].filter(part => part && part.trim() !== '')
  
  return parts.join(' ')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '可租',
    1: '已租',
    2: '下架',
    3: '预租'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-available',
    1: 'status-rented',
    2: 'status-offline',
    3: 'status-pre-rent'
  }
  return classMap[status] || 'status-unknown'
}

// 获取房屋状态样式类
const getHouseStatusClass = (status) => {
  const classMap = {
    0: 'house-available',
    1: 'house-rented',
    2: 'house-offline',
    3: 'house-pre-rent'
  }
  return classMap[status] || 'house-unknown'
}

// 获取装修程度文本
const getDecorationText = (decoration) => {
  const decorationMap = {
    1: '毛坯',
    2: '简装',
    3: '精装',
    4: '豪装'
  }
  return decorationMap[decoration] || '--'
}

// 获取租赁类型文本
const getRentalTypeText = (rentalType) => {
  const typeMap = {
    0: '整租',
    1: '合租',
    2: '单间'
  }
  return typeMap[rentalType] || '未知'
}

// 获取状态按钮文本
const getStatusBtnText = (status) => {
  const textMap = {
    0: '下架',
    1: '设为可租',
    2: '上架',
    3: '设为可租'
  }
  return textMap[status] || '操作'
}

// 获取状态按钮样式类
const getStatusBtnClass = (status) => {
  const classMap = {
    0: 'btn-offline',
    1: 'btn-available',
    2: 'btn-online',
    3: 'btn-available'
  }
  return classMap[status] || 'btn-default'
}

// 切换房屋状态
const toggleHouseStatus = async (house) => {
  try {
    let newStatus
    if (house.status === 0) {
      newStatus = 2 // 可租 -> 下架
    } else if (house.status === 1) {
      newStatus = 0 // 已租 -> 可租
    } else if (house.status === 2) {
      newStatus = 0 // 下架 -> 可租
    } else if (house.status === 3) {
      newStatus = 0 // 预租 -> 可租
    } else {
      newStatus = 0
    }

    const response = await axios.put(`${API_BASE_URL}/landlord/house/${house.id}/status`, {
      status: newStatus
    })

    if (response.data.success) {
      // 更新本地状态
      house.status = newStatus
      // 重新加载统计数据和房屋列表
      await loadStatistics()
    } else {
      console.error('更新房屋状态失败:', response.data.message)
    }
  } catch (error) {
    console.error('切换房屋状态失败:', error)
  }
}

// 编辑房屋
const editHouse = (house) => {
  alert(`编辑房屋功能开发中: ${house.id}`)
}

// 查看房屋详情
const viewHouseDetail = (house) => {
  alert(`查看房屋详情功能开发中: ${house.id}`)
}

// 提交添加房屋
const submitAddHouse = async () => {
  if (!userPhone.value) {
    alert('请先登录')
    return
  }

  addingHouse.value = true
  try {
    const houseData = {
      ...newHouse.value,
      landlordPhone: userPhone.value
    }

    // 转换数据类型
    houseData.roomArea = houseData.roomArea ? parseFloat(houseData.roomArea) : null
    houseData.rentPrice = parseFloat(houseData.rentPrice)
    houseData.waterPrice = houseData.waterPrice ? parseFloat(houseData.waterPrice) : null
    houseData.electricPrice = houseData.electricPrice ? parseFloat(houseData.electricPrice) : null
    houseData.decoration = parseInt(houseData.decoration)
    houseData.hasElevator = parseInt(houseData.hasElevator)
    houseData.rentalType = parseInt(houseData.rentalType)
    houseData.status = parseInt(houseData.status)

    const response = await axios.post(`${API_BASE_URL}/landlord/house`, houseData)
    
    if (response.data.success) {
      alert('房屋添加成功！')
      // 重置表单
      resetNewHouseForm()
      showAddHouse.value = false
      // 重新加载房屋列表和统计数据
      await loadMyHouses()
      await loadStatistics()
    } else {
      alert(`添加失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('添加房屋失败:', error)
    alert('添加房屋失败，请稍后重试')
  } finally {
    addingHouse.value = false
  }
}

// 重置新房屋表单
const resetNewHouseForm = () => {
  newHouse.value = {
    province: '',
    city: '',
    district: '',
    street: '',
    communityName: '',
    buildingUnit: '',
    doorNumber: '',
    roomNumber: '',
    roomArea: null,
    floorInfo: '',
    orientation: '',
    decoration: '2',
    hasElevator: '0',
    rentPrice: null,
    waterPrice: 3.5,
    electricPrice: 0.8,
    rentalType: '0',
    status: '0',
    description: ''
  }
}

// 退出登录
const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/login')
}

// 获取状态百分比
const getStatusPercentage = (statusType) => {
  const total = statistics.value.totalHouses || 1
  let count = 0
  
  switch (statusType) {
    case 'available':
      count = statistics.value.availableHouses || 0
      break
    case 'rented':
      count = statistics.value.rentedHouses || 0
      break
    case 'offline':
      count = statistics.value.offlineHouses || 0
      break
    case 'pre-rent':
      count = statistics.value.preRentHouses || 0
      break
  }
  
  return `${(count / total * 100).toFixed(1)}%`
}

// 状态筛选相关数据
const selectedStatusFilters = ref([0, 1, 2, 3]) // 默认显示所有状态
const statusFilters = [
  { value: 0, label: '可租' },
  { value: 1, label: '已租' },
  { value: 2, label: '下架' },
  { value: 3, label: '预租' }
]

// 切换状态筛选
const toggleStatusFilter = (status) => {
  const index = selectedStatusFilters.value.indexOf(status)
  if (index > -1) {
    selectedStatusFilters.value.splice(index, 1)
  } else {
    selectedStatusFilters.value.push(status)
  }
}

// 根据状态获取房屋列表
const getHousesByStatus = (status) => {
  return myHouses.value.filter(house => house.status === status)
}

// 处理添加房屋
const handleAddHouse = async (houseData) => {
  if (!userPhone.value) {
    alert('请先登录')
    return
  }

  try {
    // 准备提交数据
    const submitData = {
      ...houseData,
      landlordPhone: userPhone.value
    }

    const response = await axios.post(`${API_BASE_URL}/landlord/house`, submitData)
    
    if (response.data.success) {
      alert('房屋添加成功！')
      // 重新加载房屋列表和统计数据
      await loadMyHouses()
      await loadStatistics()
      // 切换到我的房屋页面
      switchTab('my-houses')
    } else {
      alert(`添加失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('添加房屋失败:', error)
    alert('添加房屋失败，请稍后重试')
  }
}

// 页面加载时初始化数据
onMounted(async () => {
  setCurrentUser()
  await loadStatistics()
  await loadExpiringContracts()
  await loadMyHouses()
  await loadPendingMaintenance()
  await loadTenantContracts()
  await loadPendingAppointments()
})
</script>

<style scoped>
.landlord-admin {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.navbar {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem 2rem;
  background-color: #1e3a5f;
  color: #ffffff;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-brand h2 {
  margin: 0;
  color: white;
  font-size: 1.25rem;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.nav-links {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.client-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  transition: all 0.2s ease;
  font-weight: 500;
  font-size: 0.875rem;
}

.client-link:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-1px);
}

.user-info {
  font-weight: 500;
  font-size: 0.875rem;
  opacity: 0.9;
}

.logout-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-weight: 500;
  font-size: 0.875rem;
}

.logout-btn:hover {
  background-color: #c0392b;
}

.admin-container {
  display: flex;
  min-height: calc(100vh - 80px);
}

.sidebar {
  width: 260px;
  background: white;
  border-right: 1px solid #e9ecef;
  padding: 1.5rem 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.menu-section {
  margin-bottom: 2rem;
}

.menu-section h3 {
  padding: 0 1.5rem;
  margin: 0 0 0.75rem 0;
  font-size: 0.75rem;
  color: #95a5a6;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-list li {
  padding: 0.75rem 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-left: 3px solid transparent;
  color: #5a6c7d;
}

.menu-list li:hover {
  background-color: #f5f7fa;
  color: #1e3a5f;
}

.menu-list li.active {
  background-color: #e8f4fd;
  color: #1e3a5f;
  border-left-color: #1e3a5f;
  font-weight: 600;
}

.menu-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-icon svg {
  width: 100%;
  height: 100%;
}

.main-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

.page-placeholder {
  background: white;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.page-placeholder h2 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

.page-placeholder p {
  color: #6c757d;
}
</style>
