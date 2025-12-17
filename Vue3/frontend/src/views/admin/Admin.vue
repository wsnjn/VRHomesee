<template>
  <div class="admin">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-brand">
        <h2>房屋租赁管理系统 - 管理端</h2>
      </div>
      <div class="nav-links">
        <div class="user-profile-box" v-if="user">
          <img :src="getAvatarUrl()" class="nav-avatar" alt="avatar" />
          <span class="user-info">{{ user.nickname || user.username }}</span>
        </div>
        <span v-else class="user-info">管理员: {{ currentUser }}</span>
        <router-link to="/landlord-admin" class="nav-btn">房东管理端</router-link>
        <router-link to="/" class="nav-btn">客户端</router-link>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </nav>

    <!-- 侧边栏菜单 -->
    <div class="admin-container">
      <div class="sidebar">
        <div class="menu-section">
          <h3>系统管理</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'dashboard' }"
              @click="switchTab('dashboard')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
              </span>
              数据概览
            </li>
          </ul>
        </div>

        <div class="menu-section">
          <h3>房屋管理</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'house-management' }"
              @click="switchTab('house-management')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
              </span>
              房屋列表
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
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
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
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </span>
              租户合同
            </li>
          </ul>
        </div>

        <div class="menu-section">
          <h3>用户管理</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'user-management' }"
              @click="switchTab('user-management')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
              </span>
              用户列表
            </li>
            <li 
              :class="{ active: activeTab === 'tenant-matching' }"
              @click="switchTab('tenant-matching')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
              </span>
              合同创建
            </li>
          </ul>
        </div>

        <div class="menu-section">
          <h3>社区管理</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'resource-management' }"
              @click="switchTab('resource-management')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>
              </span>
              资源管理
            </li>
            <li 
              :class="{ active: activeTab === 'channel-management' }"
              @click="switchTab('channel-management')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>
              </span>
              频道管理
            </li>
          </ul>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content" :class="{ 'dark-theme': activeTab === 'dashboard' }">
        <!-- 数据概览 (大数据可视化风格) -->
        <div v-if="activeTab === 'dashboard'" class="dashboard-viz">

          <!-- KPI 指标面板 -->
          <div class="kpi-row">
            <div class="kpi-card">
              <div class="kpi-icon house-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
              </div>
              <div class="kpi-content">
                <div class="kpi-value">{{ houseStatistics.total || 0 }}</div>
                <div class="kpi-label">房源总数</div>
                <div class="kpi-sub">待租: {{ houseStatusStats.available }} / 已租: {{ houseStatusStats.rented }}</div>
              </div>
            </div>
            
            <div class="kpi-card">
              <div class="kpi-icon user-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
              </div>
              <div class="kpi-content">
                <div class="kpi-value">{{ userStatistics.total || 0 }}</div>
                <div class="kpi-label">用户总数</div>
                <div class="kpi-sub">房东: {{ userStatistics.landlords }} / 租客: {{ userStatistics.tenants }}</div>
              </div>
            </div>
            
            <div class="kpi-card">
              <div class="kpi-icon contract-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
              </div>
              <div class="kpi-content">
                <div class="kpi-value">{{ statistics.totalContracts || 0 }}</div>
                <div class="kpi-label">租约合同</div>
                <div class="kpi-sub">有效: {{ statistics.activeContracts }}</div>
              </div>
            </div>
            
            <div class="kpi-card">
              <div class="kpi-icon appointment-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
              </div>
              <div class="kpi-content">
                <div class="kpi-value">{{ appointmentStats.total || 0 }}</div>
                <div class="kpi-label">看房预约</div>
                <div class="kpi-sub">待处理: {{ appointmentStats.pending }}</div>
              </div>
            </div>
            
            <div class="kpi-card">
              <div class="kpi-icon community-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>
              </div>
              <div class="kpi-content">
                <div class="kpi-value">{{ communityStats.posts + communityStats.messages }}</div>
                <div class="kpi-label">社区活跃</div>
                <div class="kpi-sub">动态: {{ communityStats.posts }} / 消息: {{ communityStats.messages }}</div>
              </div>
            </div>
          </div>

          <!-- 图表网格 -->
          <div class="viz-grid">
            <!-- 左侧列 -->
            <div class="viz-col left-col">
              <!-- 房屋状态分布 (保留饼图，增强信息) -->
              <div class="viz-card">
                <div class="card-header">
                  <h3>房屋状态分布</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-with-info">
                  <div class="chart-container" ref="houseStatusChartRef"></div>
                  <div class="status-info-list">
                    <div class="status-info-item">
                      <span class="dot available"></span>
                      <span class="name">待出租</span>
                      <span class="value">{{ houseStatusStats.available }}</span>
                    </div>
                    <div class="status-info-item">
                      <span class="dot rented"></span>
                      <span class="name">已出租</span>
                      <span class="value">{{ houseStatusStats.rented }}</span>
                    </div>
                    <div class="status-info-item">
                      <span class="dot offline"></span>
                      <span class="name">维护中</span>
                      <span class="value">{{ houseStatusStats.offline }}</span>
                    </div>
                    <div class="status-info-item">
                      <span class="dot prerent"></span>
                      <span class="name">预定中</span>
                      <span class="value">{{ houseStatusStats.preRent }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 用户增长趋势 -->
              <div class="viz-card">
                <div class="card-header">
                  <h3>用户增长趋势</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-container" ref="userTrendChartRef"></div>
              </div>
            </div>

            <!-- 中间列 (只有地图) -->
            <div class="viz-col center-col">
              <div class="viz-card map-card-full">
                <div class="card-header">
                  <h3>全国房源分布</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-container map-container-full" ref="mapChartRef"></div>
              </div>
            </div>

            <!-- 右侧列 -->
            <div class="viz-col right-col">
              <!-- 社区活跃趋势 -->
              <div class="viz-card">
                <div class="card-header">
                  <h3>社区活跃趋势</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-container" ref="communityTrendChartRef"></div>
              </div>

              <!-- 签约趋势 -->
              <div class="viz-card">
                <div class="card-header">
                  <h3>签约趋势 (近30天)</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-container" ref="trendChartRef"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 租约管理 -->
        <div v-if="activeTab === 'tenant-management'">
          <tenant-management @goToTenantMatching="handleGoToTenantMatching" />
        </div>

        <!-- 我的租户 -->
        <div v-if="activeTab === 'my-tenants'">
          <my-tenants />
        </div>

        <!-- 租客匹配 -->
        <div v-if="activeTab === 'tenant-matching'">
          <tenant-matching :preSelectedData="preSelectedMatchData" />
        </div>

        <!-- 房屋管理 -->
        <div v-if="activeTab === 'house-management'">
          <house-management />
        </div>



        <!-- 用户管理 -->
        <div v-if="activeTab === 'user-management'">
          <user-management />
        </div>

        <!-- 资源管理 -->
        <div v-if="activeTab === 'resource-management'">
          <resource-management />
        </div>

        <!-- 频道管理 -->
        <div v-if="activeTab === 'channel-management'">
          <channel-management />
        </div>


      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import * as echarts from 'echarts'
import TenantManagement from './TenantManagement.vue'
import TenantMatching from './TenantMatching.vue'
import HouseManagement from './HouseManagement.vue'
import UserManagement from './UserManagement.vue'
import ResourceManagement from './community/ResourceManagement.vue'

import ChannelManagement from './community/ChannelManagement.vue'
import MyTenants from './MyTenants.vue'

const router = useRouter()

// API基础URL
const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const activeTab = ref('dashboard')
const currentUser = ref('root')
const user = ref(null)
const currentTime = ref('')
let timer = null

// Raw Data
const rawHouses = ref([])
const rawContracts = ref([])
const rawUsers = ref([])

// Aggregated Data
const statistics = ref({
  totalContracts: 0,
  activeContracts: 0,
  statusDistribution: {}
})
const houseStatistics = ref({
  total: 0,
  available: 0,
  rented: 0
})
const userStatistics = ref({
  total: 0,
  landlords: 0,
  tenants: 0
})
const expiringContracts = ref([])
const contractTrends = ref({})
const cityDistribution = ref({})

// 预选匹配数据（从租约列表跳转时使用）
const preSelectedMatchData = ref(null)

// 处理跳转到租客匹配
const handleGoToTenantMatching = (data) => {
  preSelectedMatchData.value = data
  activeTab.value = 'tenant-matching'
}

// 预约统计
const appointmentStats = ref({
  total: 0,
  pending: 0,
  confirmed: 0,
  completed: 0,
  cancelled: 0
})

// 社区统计
const communityStats = ref({
  posts: 0,
  messages: 0
})

// 原始预约数据
const rawAppointments = ref([])

// 房屋状态统计
const houseStatusStats = computed(() => {
  const houses = rawHouses.value
  return {
    available: houses.filter(h => h.status === 0).length,
    rented: houses.filter(h => h.status === 1).length,
    offline: houses.filter(h => h.status === 2).length,
    preRent: houses.filter(h => h.status === 3).length
  }
})

// 出租率计算
const occupancyRate = computed(() => {
  const total = houseStatistics.value.total
  if (!total) return 0
  return Math.round((houseStatusStats.value.rented / total) * 100)
})

// Chart Refs
const contractChartRef = ref(null)
const mapChartRef = ref(null)
const occupancyChartRef = ref(null)
const trendChartRef = ref(null)
const houseStatusChartRef = ref(null)
const userTrendChartRef = ref(null)
const communityTrendChartRef = ref(null)
const appointmentTrendChartRef = ref(null)
let charts = []

// 城市坐标映射
const cityCoords = {
  '北京市': [116.407526, 39.90403],
  '上海市': [121.473701, 31.230416],
  '广州市': [113.264434, 23.129162],
  '深圳市': [114.085947, 22.547],
  '成都市': [104.066541, 30.572269],
  '杭州市': [120.15507, 30.274085],
  '武汉市': [114.305393, 30.593099],
  '西安市': [108.93977, 34.341574],
  '重庆市': [106.551556, 29.563009],
  '南京市': [118.796877, 32.060255],
  '天津市': [117.200983, 39.084158],
  '苏州市': [120.585315, 31.298886],
  '长沙市': [112.938814, 28.228209],
  '沈阳市': [123.431474, 41.805698],
  '青岛市': [120.382612, 36.067108],
  '郑州市': [113.625368, 34.7466],
  '大连市': [121.614682, 38.914003],
  '厦门市': [118.089425, 24.479833],
  '宁波市': [121.550357, 29.874556],
  '昆明市': [102.832891, 24.880095],
  '合肥市': [117.227239, 31.820587],
  '福州市': [119.296531, 26.074508],
  '哈尔滨市': [126.534967, 45.803775],
  '济南市': [117.1205, 36.651128],
  '长春市': [125.323544, 43.817071],
  '石家庄市': [114.51486, 38.042307],
  '南宁市': [108.366543, 22.817002],
  '贵阳市': [106.630153, 26.647661],
  '南昌市': [115.857963, 28.682892],
  '太原市': [112.548879, 37.87059]
}

// 切换标签页
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'dashboard') {
    nextTick(() => {
      loadData()
    })
  }
}

// 获取标签页标题
const getTabTitle = (tab) => {
  const titles = {
    'dashboard': '数据概览',
    'resource-management': '资源管理',
    'channel-management': '频道管理',
    'house-management': '房屋管理',
    'house-status': '房屋状态',
    'user-management': '用户管理',
    'tenant-matching': '租客匹配'
  }
  return titles[tab] || '未知页面'
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

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', { hour12: false })
}

// 处理数据聚合
const processData = () => {
  // 1. 房屋统计
  const houses = rawHouses.value
  houseStatistics.value = {
    total: houses.length,
    available: houses.filter(h => h.status === 0).length,
    rented: houses.filter(h => h.status === 1).length
  }

  // 2. 城市分布 (Map Data)
  const cityDist = {}
  houses.forEach(h => {
    if (h.city) {
      cityDist[h.city] = (cityDist[h.city] || 0) + 1
    }
  })
  cityDistribution.value = cityDist

  // 3. 租约统计
  const contracts = rawContracts.value
  const statusDist = {}
  let activeCount = 0
  
  contracts.forEach(c => {
    // 状态分布
    statusDist[c.contractStatus] = (statusDist[c.contractStatus] || 0) + 1
    
    // 活跃租约 (1=已签约, 2=履行中)
    if (c.contractStatus === 1 || c.contractStatus === 2) {
      activeCount++
    }
  })
  
  statistics.value = {
    totalContracts: contracts.length,
    activeContracts: activeCount,
    statusDistribution: statusDist
  }

  // 4. 即将到期 (30天内)
  const today = new Date()
  const thirtyDaysLater = new Date()
  thirtyDaysLater.setDate(today.getDate() + 30)
  
  expiringContracts.value = contracts.filter(c => {
    if (c.contractStatus !== 1 && c.contractStatus !== 2) return false
    const endDate = new Date(c.contractEndDate)
    return endDate >= today && endDate <= thirtyDaysLater
  }).sort((a, b) => new Date(a.contractEndDate) - new Date(b.contractEndDate))

  // 5. 签约趋势 (最近7天)
  const trends = {}
  // 初始化最近7天
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(today.getDate() - i)
    const dateStr = d.toISOString().split('T')[0]
    trends[dateStr] = 0
  }
  
  contracts.forEach(c => {
    if (c.contractSignedTime) {
      const dateStr = c.contractSignedTime.split('T')[0]
      if (trends.hasOwnProperty(dateStr)) {
        trends[dateStr]++
      }
    }
  })
  contractTrends.value = trends

  // 6. 用户统计
  const users = rawUsers.value
  userStatistics.value = {
    total: users.length,
    landlords: users.filter(u => u.userType === 2 || u.userType === 3).length,
    tenants: users.filter(u => u.userType === 1 || u.userType === 3).length
  }
}

// 初始化图表
const initCharts = async () => {
  // 清理旧图表
  charts.forEach(chart => chart.dispose())
  charts = []

  // 等待 DOM 更新
  await nextTick()

  // 1. 房屋状态分布饼图 (保留)
  if (houseStatusChartRef.value && houseStatusChartRef.value.offsetWidth && houseStatusChartRef.value.offsetHeight) {
    const houseStatusChart = echarts.init(houseStatusChartRef.value)
    houseStatusChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        name: '房屋状态',
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '50%'],
        itemStyle: { borderRadius: 4, borderColor: '#1a1a2e', borderWidth: 2 },
        label: { show: false },
        data: [
          { value: houseStatusStats.value.available || 0, name: '待出租', itemStyle: { color: '#3A6EA5' } },
          { value: houseStatusStats.value.rented || 0, name: '已出租', itemStyle: { color: '#2d8a4e' } },
          { value: houseStatusStats.value.offline || 0, name: '维护中', itemStyle: { color: '#c07700' } },
          { value: houseStatusStats.value.preRent || 0, name: '预定中', itemStyle: { color: '#6b5b95' } }
        ].filter(d => d.value > 0)
      }]
    })
    charts.push(houseStatusChart)
  }

  // 2. 房源出租率 (仪表盘)
  if (occupancyChartRef.value && occupancyChartRef.value.offsetWidth && occupancyChartRef.value.offsetHeight) {
    const occupancyChart = echarts.init(occupancyChartRef.value)
    const rate = occupancyRate.value || 0

    occupancyChart.setOption({
      series: [{
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 5,
        itemStyle: { color: '#00f2ff' },
        progress: { show: true, roundCap: true, width: 18 },
        pointer: { show: false },
        axisLine: { roundCap: true, lineStyle: { width: 18, color: [[1, '#2c3e50']] } },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        title: { show: false },
        detail: {
          valueAnimation: true,
          offsetCenter: [0, '20%'],
          fontSize: 40,
          fontWeight: 'bolder',
          formatter: '{value}%',
          color: '#fff'
        },
        data: [{ value: rate }]
      }]
    })
    charts.push(occupancyChart)
  }

  // 3. 签约趋势 (折线图)
  if (trendChartRef.value && trendChartRef.value.offsetWidth && trendChartRef.value.offsetHeight) {
    const trendChart = echarts.init(trendChartRef.value)
    const trendKeys = Object.keys(contractTrends.value).sort()
    const trendValues = trendKeys.map(key => contractTrends.value[key])
    
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { top: '10%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendKeys,
        axisLine: { lineStyle: { color: '#666' } },
        axisLabel: { color: '#aaa', fontSize: 10 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#333' } },
        axisLabel: { color: '#aaa' }
      },
      series: [{
        name: '新增租约',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3, color: '#00f2ff' },
        itemStyle: { color: '#00f2ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 242, 255, 0.5)' },
            { offset: 1, color: 'rgba(0, 242, 255, 0)' }
          ])
        },
        data: trendValues
      }]
    })
    charts.push(trendChart)
  }

  // 4. 中国地图 (模拟飞行)
  if (mapChartRef.value && mapChartRef.value.offsetWidth && mapChartRef.value.offsetHeight) {
    const mapChart = echarts.init(mapChartRef.value)
   
    try {
      // 加载中国地图GeoJSON
      const chinaJson = await axios.get('/geojson/china.json')
      echarts.registerMap('china', chinaJson.data)
      
      // 准备地图数据
      const mapData = []
      const flightData = []
      const centerCity = '北京市' // 假设中心点
      const centerCoord = cityCoords[centerCity]
    
    Object.entries(cityDistribution.value).forEach(([city, count]) => {
      // 尝试匹配坐标，支持不带"市"的匹配
      let coord = cityCoords[city]
      if (!coord && city.endsWith('市')) {
        coord = cityCoords[city]
      } else if (!coord) {
        // 尝试添加"市"再匹配
        coord = cityCoords[city + '市']
      }
      
      if (coord) {
        mapData.push({
          name: city,
          value: [...coord, count]
        })
        
        // 如果不是中心城市，添加飞行线
        if (city !== centerCity && centerCoord) {
          flightData.push({
            fromName: centerCity,
            toName: city,
            coords: [centerCoord, coord]
          })
        }
      }
    })
    
    // 如果没有数据，添加一些默认点以展示效果
    if (mapData.length === 0) {
      mapData.push({ name: '北京市', value: [...cityCoords['北京市'], 0] })
    }

    mapChart.setOption({
      geo: {
        map: 'china',
        roam: true,
        itemStyle: {
          areaColor: '#091629',
          borderColor: '#1773c3',
          shadowColor: 'rgba(0, 242, 255, 0.5)',
          shadowBlur: 10
        },
        emphasis: {
          itemStyle: { areaColor: '#0f2c4e' },
          label: { show: true, color: '#fff' }
        }
      },
      series: [
        // 散点图 (城市)
        {
          type: 'effectScatter',
          coordinateSystem: 'geo',
          data: mapData,
          symbolSize: function (val) {
            return Math.max(5, Math.min(20, val[2] * 2));
          },
          showEffectOn: 'render',
          rippleEffect: { brushType: 'stroke' },
          label: {
            formatter: '{b}: {@[2]}',
            position: 'right',
            show: true,
            color: '#fff'
          },
          itemStyle: {
            color: '#00f2ff',
            shadowBlur: 10,
            shadowColor: '#00f2ff'
          },
          zlevel: 1
        },
        // 飞行线
        {
          type: 'lines',
          zlevel: 2,
          effect: {
            show: true,
            period: 6,
            trailLength: 0.7,
            color: '#fff',
            symbolSize: 3
          },
          lineStyle: {
            color: '#a6c84c',
            width: 0,
            curveness: 0.2
          },
          data: flightData
        },
        {
          type: 'lines',
          zlevel: 3,
          symbol: ['none', 'arrow'],
          symbolSize: 10,
          effect: {
            show: true,
            period: 6,
            trailLength: 0,
            symbol: 'circle',
            symbolSize: 3
          },
          lineStyle: {
            color: '#a6c84c',
            width: 1,
            opacity: 0.6,
            curveness: 0.2
          },
          data: flightData
        }
      ]
    })
    } catch (error) {
      console.error('加载地图数据失败:', error)
    }
    
    charts.push(mapChart)
  }

  // 6. 用户增长趋势 (折线图 - 模拟数据)
  if (userTrendChartRef.value && userTrendChartRef.value.offsetWidth && userTrendChartRef.value.offsetHeight) {
    const userTrendChart = echarts.init(userTrendChartRef.value)
    // 模拟7天数据
    const days = []
    const userData = []
    const landlordData = []
    const tenantData = []
    const today = new Date()
    for (let i = 6; i >= 0; i--) {
      const d = new Date()
      d.setDate(today.getDate() - i)
      days.push(`${d.getMonth()+1}/${d.getDate()}`)
      // 模拟累计数据
      const base = userStatistics.value.total - (6-i) * Math.floor(Math.random() * 3)
      userData.push(Math.max(0, base))
      landlordData.push(Math.max(0, Math.floor(base * 0.3)))
      tenantData.push(Math.max(0, Math.floor(base * 0.7)))
    }
    
    userTrendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['总用户', '房东', '租客'], textStyle: { color: '#aaa', fontSize: 10 }, top: 0 },
      grid: { top: '20%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: days, axisLabel: { color: '#aaa', fontSize: 10 }, axisLine: { lineStyle: { color: '#444' } } },
      yAxis: { type: 'value', axisLabel: { color: '#aaa', fontSize: 10 }, splitLine: { lineStyle: { color: '#333' } } },
      series: [
        { name: '总用户', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { color: '#00f2ff', width: 2 }, itemStyle: { color: '#00f2ff' }, data: userData },
        { name: '房东', type: 'line', smooth: true, symbol: 'circle', symbolSize: 4, lineStyle: { color: '#1e3a5f', width: 1 }, itemStyle: { color: '#1e3a5f' }, data: landlordData },
        { name: '租客', type: 'line', smooth: true, symbol: 'circle', symbolSize: 4, lineStyle: { color: '#3A6EA5', width: 1 }, itemStyle: { color: '#3A6EA5' }, data: tenantData }
      ]
    })
    charts.push(userTrendChart)
  }

  // 7. 社区活跃趋势 (折线图)
  if (communityTrendChartRef.value && communityTrendChartRef.value.offsetWidth && communityTrendChartRef.value.offsetHeight) {
    const communityTrendChart = echarts.init(communityTrendChartRef.value)
    const days = []
    const postsData = []
    const messagesData = []
    const today = new Date()
    for (let i = 6; i >= 0; i--) {
      const d = new Date()
      d.setDate(today.getDate() - i)
      days.push(`${d.getMonth()+1}/${d.getDate()}`)
      postsData.push(Math.floor(Math.random() * communityStats.value.posts / 3) + 1)
      messagesData.push(Math.floor(Math.random() * communityStats.value.messages / 3) + 1)
    }
    
    communityTrendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['动态', '消息'], textStyle: { color: '#aaa', fontSize: 10 }, top: 0 },
      grid: { top: '20%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: days, axisLabel: { color: '#aaa', fontSize: 10 }, axisLine: { lineStyle: { color: '#444' } } },
      yAxis: { type: 'value', axisLabel: { color: '#aaa', fontSize: 10 }, splitLine: { lineStyle: { color: '#333' } } },
      series: [
        { name: '动态', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { color: '#3A6EA5', width: 2 }, itemStyle: { color: '#3A6EA5' }, areaStyle: { color: 'rgba(58, 110, 165, 0.2)' }, data: postsData },
        { name: '消息', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { color: '#2d8a4e', width: 2 }, itemStyle: { color: '#2d8a4e' }, areaStyle: { color: 'rgba(45, 138, 78, 0.2)' }, data: messagesData }
      ]
    })
    charts.push(communityTrendChart)
  }

  // 8. 预约完成趋势 (折线图)
  if (appointmentTrendChartRef.value && appointmentTrendChartRef.value.offsetWidth && appointmentTrendChartRef.value.offsetHeight) {
    const appointmentTrendChart = echarts.init(appointmentTrendChartRef.value)
    const days = []
    const completedData = []
    const today = new Date()
    for (let i = 6; i >= 0; i--) {
      const d = new Date()
      d.setDate(today.getDate() - i)
      days.push(`${d.getMonth()+1}/${d.getDate()}`)
      completedData.push(Math.floor(Math.random() * Math.max(1, appointmentStats.value.completed / 2)))
    }
    
    appointmentTrendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { top: '10%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: days, axisLabel: { color: '#aaa', fontSize: 10 }, axisLine: { lineStyle: { color: '#444' } } },
      yAxis: { type: 'value', axisLabel: { color: '#aaa', fontSize: 10 }, splitLine: { lineStyle: { color: '#333' } } },
      series: [{
        name: '完成预约',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: '#c07700', width: 2 },
        itemStyle: { color: '#c07700', borderWidth: 2, borderColor: '#fff' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(192, 119, 0, 0.4)' }, { offset: 1, color: 'rgba(192, 119, 0, 0)' }]) },
        data: completedData
      }]
    })
    charts.push(appointmentTrendChart)
  }

  // 响应式调整
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  charts.forEach(chart => chart.resize())
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    0: '待签约',
    1: '已签约',
    2: '履行中',
    3: '已到期',
    4: '提前解约',
    5: '已退租'
  }
  return texts[status] || '未知'
}

// 加载数据
const loadData = async () => {
  try {
    const [housesRes, contractsRes, usersRes, appointmentsRes, postsRes, messagesRes] = await Promise.all([
      axios.get(`${API_BASE_URL}/rooms/all`),
      axios.get(`${API_BASE_URL}/admin/tenant/all`),
      axios.get(`${API_BASE_URL}/admin/user/all`),
      axios.get(`${API_BASE_URL}/viewing-appointment/all`).catch(() => ({ data: { success: false } })),
      axios.get(`${API_BASE_URL}/admin/channel/community/posts`).catch(() => ({ data: [] })),
      axios.get(`${API_BASE_URL}/admin/channel/chat/messages`).catch(() => ({ data: [] }))
    ])

    if (housesRes.data.success) rawHouses.value = housesRes.data.rooms || []
    if (contractsRes.data.success) rawContracts.value = contractsRes.data.contracts || []
    if (usersRes.data.success) rawUsers.value = usersRes.data.users || []
    
    // 处理预约数据
    if (appointmentsRes.data.success) {
      rawAppointments.value = appointmentsRes.data.appointments || []
      const apts = rawAppointments.value
      appointmentStats.value = {
        total: apts.length,
        pending: apts.filter(a => a.status === 0).length,
        confirmed: apts.filter(a => a.status === 1).length,
        completed: apts.filter(a => a.status === 2).length,
        cancelled: apts.filter(a => a.status === 3).length
      }
    }
    
    // 处理社区数据
    const posts = Array.isArray(postsRes.data) ? postsRes.data : []
    const messages = Array.isArray(messagesRes.data) ? messagesRes.data : []
    communityStats.value = {
      posts: posts.length,
      messages: messages.length
    }

    processData()

    if (activeTab.value === 'dashboard') {
      nextTick(() => initCharts())
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 获取用户信息
const fetchUserInfo = async (userId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/user/${userId}`)
    if (response.data.success) {
      user.value = response.data.user
      localStorage.setItem('user', JSON.stringify(response.data.user))
      currentUser.value = user.value.username
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 获取头像URL
const getAvatarUrl = () => {
  if (!user.value || !user.value.avatar) {
    return '/models/image/default-avatar.png'
  }
  
  if (user.value.avatar.startsWith('http')) {
    return user.value.avatar
  }
  
  const FILE_SERVER_HOST = 'https://files.homesee.xyz'
  return `${FILE_SERVER_HOST}/api/files/download/${user.value.avatar}`
}

// 退出登录
const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(() => {
  // 获取当前用户信息
  const userData = localStorage.getItem('user')
  if (userData) {
    try {
      const localUser = JSON.parse(userData)
      user.value = localUser
      currentUser.value = localUser.username
      fetchUserInfo(localUser.id)
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }

  loadData()
  timer = setInterval(updateTime, 1000)
  updateTime()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  charts.forEach(chart => chart.dispose())
})
</script>

<style scoped>
.admin {
  min-height: 100vh;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
}

.navbar {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  background-color: #1e3a5f;
  color: white;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-brand h2 {
  margin: 0;
  font-size: 1.2rem;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.user-profile-box {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 10px;
}

.nav-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.logout-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
  font-size: 0.875rem;
}

.logout-btn:hover {
  background-color: #c0392b;
}

.nav-btn {
  color: white;
  text-decoration: none;
  padding: 0.4rem 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  transition: all 0.2s;
  font-size: 0.875rem;
  background: rgba(255, 255, 255, 0.1);
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.admin-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e9ecef;
  padding: 2rem 0;
  overflow-y: auto;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.02);
}

.menu-section h3 {
  padding: 0 1.5rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: #adb5bd;
  margin-bottom: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0 0 2rem 0;
}

.menu-list li {
  padding: 0.8rem 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 1rem;
  color: #495057;
  font-weight: 500;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.menu-list li:hover {
  background-color: #f5f7fa;
  color: #1e3a5f;
}

.menu-list li.active {
  background-color: #e8f4fd;
  color: #1e3a5f;
  border-left-color: #1e3a5f;
  border-right: none;
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

.main-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background-color: #f8f9fa;
}

/* Dark Theme for Dashboard */
.main-content.dark-theme {
  background-color: #0b0b15;
  color: #fff;
  padding: 0;
}

.dashboard-viz {
  padding: 1.5rem;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.viz-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #1a1a2e;
  padding-bottom: 1rem;
}

.viz-header h1 {
  margin: 0;
  font-size: 1.8rem;
  background: linear-gradient(to right, #00f2ff, #007bff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.viz-time {
  font-family: 'Courier New', monospace;
  font-size: 1.2rem;
  color: #00f2ff;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.5);
}

.viz-grid {
  display: grid;
  grid-template-columns: 28% 44% 28%;
  gap: 0.4rem;
  flex: 1;
}

.viz-col {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.viz-card {
  background: rgba(20, 20, 35, 0.8);
  border: 1px solid #2a2a40;
  border-radius: 0;
  padding: 0.4rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
}

.viz-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00f2ff, transparent);
}

.card-header {
  margin-bottom: 0.3rem;
}

.card-header h3 {
  margin: 0;
  font-size: 0.9rem;
  color: #fff;
  font-weight: 500;
}

.decoration-line {
  height: 2px;
  width: 20px;
  background: #00f2ff;
  margin-top: 3px;
  box-shadow: 0 0 5px #00f2ff;
}

.chart-container {
  flex: 1;
  min-height: 100px;
}

.map-container {
  min-height: 200px;
}

/* Full height map for center column */
.map-card-full {
  flex: 1;
}

.map-container-full {
  flex: 1;
  min-height: 100%;
}

/* KPI Board */
.kpi-board {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

/* Dashboard Header */
.top-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 0.5rem;
  padding: 0.3rem 0.5rem;
}

.clock-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.clock-section .status-dot {
  width: 8px;
  height: 8px;
  background: #00f2ff;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
  box-shadow: 0 0 8px #00f2ff;
}

.clock-section .digital-clock {
  font-family: 'Courier New', monospace;
  font-size: 1.2rem;
  color: #00f2ff;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.5);
}

/* Chart with Info Layout */
.chart-with-info {
  display: flex;
  flex: 1;
  gap: 0.5rem;
}

.chart-with-info .chart-container {
  flex: 1;
  min-height: 180px;
}

.status-info-list {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.8rem;
  min-width: 100px;
}

.status-info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
}

.status-info-item .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

.status-info-item .dot.available { background: #3A6EA5; box-shadow: 0 0 5px #3A6EA5; }
.status-info-item .dot.rented { background: #2d8a4e; box-shadow: 0 0 5px #2d8a4e; }
.status-info-item .dot.offline { background: #c07700; box-shadow: 0 0 5px #c07700; }
.status-info-item .dot.prerent { background: #6b5b95; box-shadow: 0 0 5px #6b5b95; }

.status-info-item .name { color: #888; }
.status-info-item .value { color: #fff; font-weight: bold; margin-left: auto; }

.viz-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 1rem 1.5rem;
  background: rgba(20, 20, 35, 0.9);
  border: 1px solid #2a2a40;
}

.header-left h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #fff;
  font-weight: 600;
}

.header-left .system-status {
  margin-top: 0.3rem;
  font-size: 0.8rem;
  color: #888;
}

.header-right .digital-clock {
  font-family: 'Courier New', monospace;
  font-size: 1.3rem;
  color: #00f2ff;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.5);
}

/* KPI Row - 5 Cards */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.3rem;
  margin-bottom: 0.4rem;
}

.kpi-card {
  background: rgba(20, 20, 35, 0.8);
  border: 1px solid #2a2a40;
  padding: 0.4rem 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  position: relative;
  overflow: hidden;
}

.kpi-card::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00f2ff, transparent);
}

.kpi-card .kpi-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 242, 255, 0.1);
  border: 1px solid rgba(0, 242, 255, 0.2);
}

.kpi-card .kpi-icon svg {
  width: 18px;
  height: 18px;
}

.kpi-card .kpi-icon.house-icon { background: rgba(58, 110, 165, 0.2); border-color: rgba(58, 110, 165, 0.4); }
.kpi-card .kpi-icon.house-icon svg { color: #3A6EA5; }
.kpi-card .kpi-icon.rent-icon { background: rgba(45, 138, 78, 0.2); border-color: rgba(45, 138, 78, 0.4); }
.kpi-card .kpi-icon.rent-icon svg { color: #2d8a4e; }
.kpi-card .kpi-icon.user-icon { background: rgba(107, 91, 149, 0.2); border-color: rgba(107, 91, 149, 0.4); }
.kpi-card .kpi-icon.user-icon svg { color: #6b5b95; }
.kpi-card .kpi-icon.contract-icon { background: rgba(30, 58, 95, 0.3); border-color: rgba(30, 58, 95, 0.5); }
.kpi-card .kpi-icon.contract-icon svg { color: #4a7fb5; }
.kpi-card .kpi-icon.appointment-icon { background: rgba(192, 119, 0, 0.2); border-color: rgba(192, 119, 0, 0.4); }
.kpi-card .kpi-icon.appointment-icon svg { color: #c07700; }
.kpi-card .kpi-icon.community-icon { background: rgba(0, 242, 255, 0.1); border-color: rgba(0, 242, 255, 0.3); }
.kpi-card .kpi-icon.community-icon svg { color: #00f2ff; }

.kpi-card .kpi-content {
  flex: 1;
}

.kpi-card .kpi-value {
  font-size: 1.1rem;
  font-weight: bold;
  color: #fff;
  line-height: 1;
}

.kpi-card .kpi-label {
  color: #888;
  font-size: 0.6rem;
  text-transform: uppercase;
  letter-spacing: 0.2px;
}

.kpi-card .kpi-sub {
  color: #555;
  font-size: 0.55rem;
  margin-top: 0;
}

/* Old KPI Item (keep for compatibility) */
.kpi-item {
  background: rgba(0, 242, 255, 0.1);
  border: 1px solid rgba(0, 242, 255, 0.2);
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  position: relative;
}

.kpi-icon {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.kpi-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

.kpi-label {
  color: #888;
  font-size: 0.9rem;
  text-transform: uppercase;
}

/* Expiring List Viz */
.expiring-list-viz {
  flex: 1;
  overflow-y: auto;
  padding-right: 5px;
}

.expiring-item-viz {
  background: rgba(255, 255, 255, 0.05);
  margin-bottom: 0.5rem;
  padding: 0.8rem;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 3px solid #ff4757;
}

.item-info {
  display: flex;
  flex-direction: column;
}

.contract-no {
  color: #fff;
  font-weight: bold;
}

.expiry-date {
  font-size: 0.8rem;
  color: #aaa;
}

.days-badge {
  background: rgba(255, 71, 87, 0.2);
  color: #ff4757;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.8rem;
}

.no-data-viz {
  text-align: center;
  color: #666;
  padding: 2rem;
}

/* Scrollbar for dark theme */
.dark-theme ::-webkit-scrollbar {
  width: 6px;
}

.dark-theme ::-webkit-scrollbar-track {
  background: #0b0b15;
}

.dark-theme ::-webkit-scrollbar-thumb {
  background: #2a2a40;
  border-radius: 3px;
}

.dark-theme ::-webkit-scrollbar-thumb:hover {
  background: #00f2ff;
}

@media (max-width: 1200px) {
  .viz-grid {
    grid-template-columns: 1fr 1fr;
  }
  .center-col {
    grid-column: span 2;
    order: -1;
  }
}

@media (max-width: 768px) {
  .viz-grid {
    grid-template-columns: 1fr;
  }
  .center-col {
    grid-column: span 1;
  }
  .kpi-board {
    grid-template-columns: 1fr;
  }
}
</style>

<style scoped>
/* Title Card Styles */
.title-card {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(20, 20, 35, 0.9), rgba(10, 10, 20, 0.95));
}

.title-content {
  text-align: center;
}

.title-content h2 {
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
  background: linear-gradient(to right, #00f2ff, #007bff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.digital-clock {
  font-family: 'Courier New', monospace;
  font-size: 2.5rem;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 0 15px rgba(0, 242, 255, 0.6);
  margin-bottom: 1rem;
}

.system-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #00f2ff;
  font-size: 0.9rem;
  background: rgba(0, 242, 255, 0.1);
  padding: 4px 12px;
  border-radius: 20px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background-color: #00f2ff;
  border-radius: 50%;
  box-shadow: 0 0 8px #00f2ff;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1); }
}
</style>
