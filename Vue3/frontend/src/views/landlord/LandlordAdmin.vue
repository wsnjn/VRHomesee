<template>
  <div class="landlord-admin">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-brand">
        <h2>房屋租赁管理系统 - 房东管理端</h2>
      </div>
      <div class="nav-links">
        <span class="user-info">房东: {{ currentUser }}</span>
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
              <span class="menu-icon">🏠</span>
              我的房屋
            </li>
            <li 
              :class="{ active: activeTab === 'house-status' }"
              @click="switchTab('house-status')"
            >
              <span class="menu-icon">📈</span>
              房屋状态
            </li>
            <li 
              :class="{ active: activeTab === 'add-house' }"
              @click="switchTab('add-house')"
            >
              <span class="menu-icon">➕</span>
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
              <span class="menu-icon">📋</span>
              租约列表
            </li>
            <li 
              :class="{ active: activeTab === 'tenant-matching' }"
              @click="switchTab('tenant-matching')"
            >
              <span class="menu-icon">🤝</span>
              租客匹配
            </li>
            <li 
              :class="{ active: activeTab === 'rent-payment' }"
              @click="switchTab('rent-payment')"
            >
              <span class="menu-icon">💰</span>
              租金管理
            </li>
            <li 
              :class="{ active: activeTab === 'maintenance' }"
              @click="switchTab('maintenance')"
            >
              <span class="menu-icon">🔧</span>
              维修报修
            </li>
          </ul>
        </div>

        <div class="menu-section">
          <h3>数据统计</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'dashboard' }"
              @click="switchTab('dashboard')"
            >
              <span class="menu-icon">📊</span>
              数据概览
            </li>
            <li 
              :class="{ active: activeTab === 'income-stats' }"
              @click="switchTab('income-stats')"
            >
              <span class="menu-icon">💵</span>
              收入统计
            </li>
          </ul>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 数据概览 -->
        <div v-if="activeTab === 'dashboard'">
          <landlord-dashboard 
            :statistics="statistics"
            :expiringContracts="expiringContracts"
          />
        </div>

        <!-- 我的房屋 -->
        <div v-if="activeTab === 'my-houses'">
          <my-houses 
            :myHouses="myHouses"
            :loadingHouses="loadingHouses"
            :addingHouse="addingHouse"
            @refresh="loadMyHouses"
            @editHouse="editHouse"
            @toggleHouseStatus="toggleHouseStatus"
            @viewHouseDetail="viewHouseDetail"
            @submitAddHouse="submitAddHouse"
          />
        </div>

        <!-- 房屋状态 -->
        <div v-if="activeTab === 'house-status'">
          <house-status 
            :statistics="statistics"
            :myHouses="myHouses"
            @refreshStatistics="loadStatistics"
            @refreshHouses="loadMyHouses"
            @toggleHouseStatus="toggleHouseStatus"
            @viewHouseDetail="viewHouseDetail"
            @editHouse="editHouse"
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
          <tenant-management :userPhone="userPhone" />
        </div>

        <!-- 租客匹配 -->
        <div v-if="activeTab === 'tenant-matching'">
          <landlord-tenant-matching :userPhone="userPhone" />
        </div>

        <!-- 租金管理 -->
        <div v-if="activeTab === 'rent-payment'">
          <placeholder-page 
            title="租金管理"
            message="租金收付管理功能开发中..."
          />
        </div>

        <!-- 维修报修 -->
        <div v-if="activeTab === 'maintenance'">
          <placeholder-page 
            title="维修报修"
            message="维修工单管理功能开发中..."
          />
        </div>

        <!-- 收入统计 -->
        <div v-if="activeTab === 'income-stats'">
          <placeholder-page 
            title="收入统计"
            message="收入数据分析功能开发中..."
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 导入组件
import LandlordDashboard from './components/LandlordDashboard.vue'
import MyHouses from './components/MyHouses.vue'
import HouseStatus from './components/HouseStatus.vue'
import AddHouse from './components/AddHouse.vue'
import TenantManagement from './components/TenantManagement.vue'
import LandlordTenantMatching from './components/LandlordTenantMatching.vue'

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
const API_BASE_URL = 'http://localhost:8080/api'

// 响应式数据
const activeTab = ref('dashboard')
const currentUser = ref('')
const userPhone = ref('')
const statistics = ref({})
const expiringContracts = ref([])

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
      statistics.value = response.data.statistics
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
      myHouses.value = response.data.houses || []
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
  padding: 1rem 2rem;
  background-color: rgba(40, 167, 69, 0.95);
  color: white;
  z-index: 100;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-brand h2 {
  margin: 0;
  color: white;
  font-size: 1.5rem;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.user-info {
  font-weight: 600;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
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
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  color: #6c757d;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-list li {
  padding: 0.75rem 1.5rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-left: 3px solid transparent;
}

.menu-list li:hover {
  background-color: #f8f9fa;
  color: #28a745;
}

.menu-list li.active {
  background-color: #e8f5e8;
  color: #28a745;
  border-left-color: #28a745;
  font-weight: 600;
}

.menu-icon {
  font-size: 1.1rem;
}

.main-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

/* 数据概览样式 */
.dashboard {
  max-width: 1200px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  color: #2c3e50;
}

.stat-info p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.chart-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.chart-card h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.chart-placeholder {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6c757d;
  border: 2px dashed #e9ecef;
  border-radius: 8px;
  padding: 1rem;
}

.status-list {
  width: 100%;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-rented { background-color: #28a745; }
.status-vacant { background-color: #ffc107; }
.status-maintenance { background-color: #dc3545; }

.expiring-list {
  max-height: 300px;
  overflow-y: auto;
}

.expiring-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #f8f9fa;
  transition: background-color 0.3s;
}

.expiring-item:hover {
  background-color: #f8f9fa;
}

.contract-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.contract-info strong {
  color: #2c3e50;
}

.contract-info span {
  color: #6c757d;
  font-size: 0.9rem;
}

.days-left {
  background: #fff3cd;
  color: #856404;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.no-data {
  text-align: center;
  color: #6c757d;
  padding: 2rem;
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

/* 我的房屋页面样式 */
.my-houses {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.refresh-btn, .add-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.refresh-btn {
  background-color: #6c757d;
  color: white;
}

.refresh-btn:hover {
  background-color: #5a6268;
}

.add-btn {
  background-color: #28a745;
  color: white;
}

.add-btn:hover {
  background-color: #218838;
}

/* 房屋列表表格样式 */
.houses-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
  font-size: 1.1rem;
}

.no-houses {
  padding: 4rem 2rem;
}

.empty-state {
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
}

.empty-state p {
  color: #6c757d;
  margin-bottom: 2rem;
}

.add-first-btn {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s;
}

.add-first-btn:hover {
  background-color: #218838;
}

.houses-table {
  width: 100%;
}

.houses-table-content {
  width: 100%;
  border-collapse: collapse;
}

.houses-table-content th {
  background-color: #f8f9fa;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #e9ecef;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.houses-table-content td {
  padding: 1rem;
  border-bottom: 1px solid #f8f9fa;
  vertical-align: top;
}

.house-row {
  transition: background-color 0.3s;
}

.house-row:hover {
  background-color: #f8f9fa;
}

.house-row:last-child td {
  border-bottom: none;
}

/* 房屋状态行样式 */
.house-available {
  border-left: 4px solid #28a745;
}

.house-rented {
  border-left: 4px solid #dc3545;
}

.house-offline {
  border-left: 4px solid #6c757d;
}

.house-pre-rent {
  border-left: 4px solid #ffc107;
}

.house-unknown {
  border-left: 4px solid #6c757d;
}

/* 表格单元格样式 */
.address-cell {
  min-width: 200px;
}

.address-info strong {
  display: block;
  margin-bottom: 0.5rem;
  color: #2c3e50;
  font-size: 1rem;
}

.address-details {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #6c757d;
}

.address-details span {
  background: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.info-cell {
  min-width: 180px;
}

.house-basic-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.info-label {
  color: #6c757d;
  font-weight: 500;
  min-width: 50px;
}

.info-value {
  color: #2c3e50;
  font-weight: 600;
  text-align: right;
}

.price-cell {
  min-width: 150px;
}

.price-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.main-price {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.main-price strong {
  color: #dc3545;
  font-size: 1.1rem;
}

.rental-type {
  background: #28a745;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
}

.utility-prices {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: #6c757d;
}

.status-cell {
  min-width: 80px;
}

.house-status {
  padding: 0.5rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
  display: inline-block;
  text-align: center;
  min-width: 60px;
}

.status-available {
  background-color: #d4edda;
  color: #155724;
}

.status-rented {
  background-color: #f8d7da;
  color: #721c24;
}

.status-offline {
  background-color: #e2e3e5;
  color: #383d41;
}

.status-pre-rent {
  background-color: #fff3cd;
  color: #856404;
}

.status-unknown {
  background-color: #e2e3e5;
  color: #383d41;
}

.actions-cell {
  min-width: 180px;
}

.house-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.5rem 0.75rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  font-size: 0.8rem;
  white-space: nowrap;
}

.edit-btn {
  background-color: #17a2b8;
  color: white;
}

.edit-btn:hover {
  background-color: #138496;
}

.detail-btn {
  background-color: #6c757d;
  color: white;
}

.detail-btn:hover {
  background-color: #5a6268;
}

.status-btn {
  color: white;
}

.btn-offline {
  background-color: #dc3545;
}

.btn-offline:hover {
  background-color: #c82333;
}

.btn-available {
  background-color: #28a745;
}

.btn-available:hover {
  background-color: #218838;
}

.btn-online {
  background-color: #17a2b8;
}

.btn-online:hover {
  background-color: #138496;
}

.btn-default {
  background-color: #6c757d;
}

.btn-default:hover {
  background-color: #5a6268;
}

.house-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
  transition: transform 0.3s, box-shadow 0.3s;
  border-left: 4px solid #28a745;
}

.house-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

/* 房屋状态样式 */
.house-available {
  border-left-color: #28a745;
}

.house-rented {
  border-left-color: #dc3545;
}

.house-offline {
  border-left-color: #6c757d;
}

.house-pre-rent {
  border-left-color: #ffc107;
}

.house-unknown {
  border-left-color: #6c757d;
}

.house-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.house-title {
  margin: 0;
  font-size: 1.2rem;
  color: #2c3e50;
  line-height: 1.4;
  flex: 1;
  margin-right: 1rem;
}

.house-status {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-available {
  background-color: #d4edda;
  color: #155724;
}

.status-rented {
  background-color: #f8d7da;
  color: #721c24;
}

.status-offline {
  background-color: #e2e3e5;
  color: #383d41;
}

.status-pre-rent {
  background-color: #fff3cd;
  color: #856404;
}

.status-unknown {
  background-color: #e2e3e5;
  color: #383d41;
}

.house-info {
  margin-bottom: 1rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #f8f9fa;
}

.info-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.info-label {
  color: #6c757d;
  font-weight: 500;
}

.info-value {
  color: #2c3e50;
  font-weight: 600;
}

.house-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #dc3545;
}

.rental-type {
  background: #28a745;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.house-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  flex: 1;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.edit-btn {
  background-color: #17a2b8;
  color: white;
}

.edit-btn:hover {
  background-color: #138496;
}

.detail-btn {
  background-color: #6c757d;
  color: white;
}

.detail-btn:hover {
  background-color: #5a6268;
}

.status-btn {
  color: white;
}

.btn-offline {
  background-color: #dc3545;
}

.btn-offline:hover {
  background-color: #c82333;
}

.btn-available {
  background-color: #28a745;
}

.btn-available:hover {
  background-color: #218838;
}

.btn-online {
  background-color: #17a2b8;
}

.btn-online:hover {
  background-color: #138496;
}

.btn-default {
  background-color: #6c757d;
}

.btn-default:hover {
  background-color: #5a6268;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #dc3545;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
}

.cancel-btn, .confirm-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.confirm-btn {
  background-color: #28a745;
  color: white;
}

.confirm-btn:hover {
  background-color: #218838;
}

.confirm-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

/* 房屋状态页面样式 */
.house-status-page {
  width: 100%;
}

.status-overview {
  margin-bottom: 2rem;
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.status-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.status-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.status-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  color: #2c3e50;
}

.status-info p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

/* 状态卡片颜色 */
.status-available {
  border-left: 4px solid #28a745;
}

.status-rented {
  border-left: 4px solid #dc3545;
}

.status-offline {
  border-left: 4px solid #6c757d;
}

.status-pre-rent {
  border-left: 4px solid #ffc107;
}

.status-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.chart-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.chart-section h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.status-distribution {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.distribution-bar {
  width: 100%;
  height: 30px;
  background: #f8f9fa;
  border-radius: 15px;
  overflow: hidden;
  display: flex;
}

.distribution-segment {
  height: 100%;
  transition: width 0.3s ease;
}

.distribution-segment.available {
  background: #28a745;
}

.distribution-segment.rented {
  background: #dc3545;
}

.distribution-segment.offline {
  background: #6c757d;
}

.distribution-segment.pre-rent {
  background: #ffc107;
}

.distribution-legend {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #6c757d;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-color.available {
  background: #28a745;
}

.legend-color.rented {
  background: #dc3545;
}

.legend-color.offline {
  background: #6c757d;
}

.legend-color.pre-rent {
  background: #ffc107;
}

.status-details {
  max-height: 400px;
  overflow-y: auto;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.status-item:hover {
  background: #e9ecef;
}

.house-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.house-info strong {
  color: #2c3e50;
  font-size: 0.95rem;
}

.house-price {
  color: #dc3545;
  font-weight: 600;
  font-size: 0.9rem;
}

/* 状态筛选器样式 */
.status-filter {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.filter-title {
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
}

.filter-buttons {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 20px;
  background: white;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.filter-btn:hover {
  border-color: #28a745;
  color: #28a745;
}

.filter-btn.active {
  border-color: #28a745;
  background: #28a745;
  color: white;
}

.filter-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-0 { background-color: #28a745; }
.dot-1 { background-color: #dc3545; }
.dot-2 { background-color: #6c757d; }
.dot-3 { background-color: #ffc107; }

/* 状态分组样式 */
.status-groups {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.status-group {
  background: #f8f9fa;
  border-radius: 12px;
  overflow: hidden;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  color: white;
  font-weight: 600;
}

.group-header h4 {
  margin: 0;
  font-size: 1.1rem;
}

.group-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.9rem;
}

.header-0 { background: #28a745; }
.header-1 { background: #dc3545; }
.header-2 { background: #6c757d; }
.header-3 { background: #ffc107; color: #856404; }

.group-houses {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 状态房屋卡片样式 */
.status-house-card {
  background: white;
  border-radius: 8px;
  padding: 1.25rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s, box-shadow 0.3s;
  border-left: 4px solid #28a745;
}

.status-house-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.status-house-card.house-0 { border-left-color: #28a745; }
.status-house-card.house-1 { border-left-color: #dc3545; }
.status-house-card.house-2 { border-left-color: #6c757d; }
.status-house-card.house-3 { border-left-color: #ffc107; }

.house-main-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  gap: 1rem;
}

.house-address {
  flex: 1;
}

.house-address strong {
  display: block;
  color: #2c3e50;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
  line-height: 1.4;
}

.house-community {
  color: #6c757d;
  font-size: 0.9rem;
}

.house-price-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
  min-width: 120px;
}

.house-price {
  font-size: 1.2rem;
  font-weight: 700;
  color: #dc3545;
}

.house-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.detail-label {
  color: #6c757d;
  font-weight: 500;
}

.detail-value {
  color: #2c3e50;
  font-weight: 600;
}

.house-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.no-houses-in-group {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
  font-style: italic;
  background: white;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .status-charts {
    grid-template-columns: 1fr;
  }
  
  .house-details {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .admin-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e9ecef;
  }
  
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
  
  .status-cards {
    grid-template-columns: 1fr 1fr;
  }
  
  .distribution-legend {
    grid-template-columns: 1fr;
  }
  
  .status-filter {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-buttons {
    width: 100%;
  }
  
  .house-main-info {
    flex-direction: column;
  }
  
  .house-price-status {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
    align-items: center;
  }
  
  .house-details {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .status-cards {
    grid-template-columns: 1fr;
  }
  
  .navbar {
    padding: 1rem;
  }
  
  .main-content {
    padding: 1rem;
  }
  
  .filter-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-btn {
    justify-content: center;
  }
  
  .house-actions {
    flex-wrap: wrap;
  }
  
  .house-actions .action-btn {
    flex: 1;
    min-width: 80px;
  }
}
</style>
