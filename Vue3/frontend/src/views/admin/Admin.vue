<template>
  <div class="admin">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-brand">
        <h2>房屋租赁管理系统 - 管理端</h2>
      </div>
      <div class="nav-links">
        <span class="user-info">管理员: {{ currentUser }}</span>
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
          <h3>租约管理</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'tenant-management' }"
              @click="switchTab('tenant-management')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
              </span>
              租约列表
            </li>
            <li 
              :class="{ active: activeTab === 'rent-payment' }"
              @click="switchTab('rent-payment')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
              </span>
              租金管理
            </li>
            <li 
              :class="{ active: activeTab === 'maintenance' }"
              @click="switchTab('maintenance')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg>
              </span>
              维修报修
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
            <li 
              :class="{ active: activeTab === 'house-status' }"
              @click="switchTab('house-status')"
            >
              <span class="menu-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"></path><path d="M22 12A10 10 0 0 0 12 2v10z"></path></svg>
              </span>
              房屋状态
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
              租客匹配
            </li>
          </ul>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content" :class="{ 'dark-theme': activeTab === 'dashboard' }">
        <!-- 数据概览 (大数据可视化风格) -->
        <div v-if="activeTab === 'dashboard'" class="dashboard-viz">


          <div class="viz-grid">
            <!-- 左侧列 -->
            <div class="viz-col left-col">
              <div class="viz-card title-card">
                <div class="title-content">
                  <h2>运营数据监控中心</h2>
                  <div class="digital-clock">{{ currentTime }}</div>
                  <div class="system-status">
                    <span class="status-dot"></span> 系统运行正常
                  </div>
                </div>
              </div>

              <div class="viz-card">
                <div class="card-header">
                  <h3>租约状态分布</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-container" ref="contractChartRef"></div>
              </div>
            </div>

            <!-- 中间列 (核心指标 + 地图) -->
            <div class="viz-col center-col">
              <div class="kpi-board">
                <div class="kpi-item">
                  <div class="kpi-icon">🏠</div>
                  <div class="kpi-value">{{ houseStatistics.total || 0 }}</div>
                  <div class="kpi-label">房源总数</div>
                </div>
                <div class="kpi-item">
                  <div class="kpi-icon">📝</div>
                  <div class="kpi-value">{{ statistics.totalContracts || 0 }}</div>
                  <div class="kpi-label">累计签约</div>
                </div>
                <div class="kpi-item">
                  <div class="kpi-icon">👥</div>
                  <div class="kpi-value">{{ userStatistics.total || 0 }}</div>
                  <div class="kpi-label">用户总数</div>
                </div>
              </div>

              <div class="viz-card map-card">
                <div class="card-header">
                  <h3>全国房源分布与实时监控</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-container map-container" ref="mapChartRef"></div>
              </div>
            </div>

            <!-- 右侧列 -->
            <div class="viz-col right-col">
              <div class="viz-card">
                <div class="card-header">
                  <h3>房源出租率</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-container" ref="occupancyChartRef"></div>
              </div>

              <div class="viz-card">
                <div class="card-header">
                  <h3>近期签约趋势 (7天)</h3>
                  <div class="decoration-line"></div>
                </div>
                <div class="chart-container" ref="trendChartRef"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 租约管理 -->
        <div v-if="activeTab === 'tenant-management'">
          <tenant-management />
        </div>

        <!-- 租客匹配 -->
        <div v-if="activeTab === 'tenant-matching'">
          <tenant-matching />
        </div>

        <!-- 房屋管理 -->
        <div v-if="activeTab === 'house-management'">
          <house-management />
        </div>

        <!-- 用户管理 -->
        <div v-if="activeTab === 'user-management'">
          <user-management />
        </div>

        <!-- 其他页面占位 -->
        <div v-else-if="activeTab !== 'dashboard' && activeTab !== 'tenant-management' && activeTab !== 'tenant-matching' && activeTab !== 'house-management' && activeTab !== 'user-management'" class="page-placeholder">
          <h2>{{ getTabTitle(activeTab) }}</h2>
          <p>功能开发中...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import * as echarts from 'echarts'
import TenantManagement from './TenantManagement.vue'
import TenantMatching from './TenantMatching.vue'
import HouseManagement from './HouseManagement.vue'
import UserManagement from './UserManagement.vue'

const router = useRouter()

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

// 响应式数据
const activeTab = ref('dashboard')
const currentUser = ref('root')
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

// Chart Refs
const contractChartRef = ref(null)
const mapChartRef = ref(null)
const occupancyChartRef = ref(null)
const trendChartRef = ref(null)
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
    'tenant-management': '租约管理',
    'rent-payment': '租金管理',
    'maintenance': '维修报修',
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

  if (!contractChartRef.value) return

  // 1. 租约状态分布 (饼图)
  const contractChart = echarts.init(contractChartRef.value)
  const statusData = statistics.value.statusDistribution || {}
  const pieData = Object.entries(statusData).map(([key, value]) => ({
    name: getStatusText(parseInt(key)),
    value: value
  }))

  contractChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: '5%', left: 'center', textStyle: { color: '#fff' } },
    series: [{
      name: '租约状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#1a1a2e',
        borderWidth: 2
      },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: '20', fontWeight: 'bold', color: '#fff' }
      },
      labelLine: { show: false },
      data: pieData.length ? pieData : [{ value: 0, name: '无数据' }]
    }]
  })
  charts.push(contractChart)

  // 2. 房源出租率 (仪表盘/环形图)
  const occupancyChart = echarts.init(occupancyChartRef.value)
  const total = houseStatistics.value.total || 1
  const rented = houseStatistics.value.rented || 0
  const rate = Math.round((rented / total) * 100)

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

  // 3. 业务趋势 (折线图 - 真实数据)
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
      axisLabel: { color: '#aaa' }
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
      lineStyle: { width: 3, color: '#00f2ff' },
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

  // 4. 中国地图 (模拟飞行)
  const mapChart = echarts.init(mapChartRef.value)
  
  try {
    // 加载中国地图GeoJSON
    const chinaJson = await axios.get('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
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
    const [housesRes, contractsRes, usersRes] = await Promise.all([
      axios.get(`${API_BASE_URL}/rooms/all`),
      axios.get(`${API_BASE_URL}/admin/tenant/all`),
      axios.get(`${API_BASE_URL}/admin/user/all`)
    ])

    if (housesRes.data.success) rawHouses.value = housesRes.data.rooms || []
    if (contractsRes.data.success) rawContracts.value = contractsRes.data.contracts || []
    if (usersRes.data.success) rawUsers.value = usersRes.data.users || []

    processData()

    if (activeTab.value === 'dashboard') {
      nextTick(() => initCharts())
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 退出登录
const logout = () => {
  router.push('/login')
}

onMounted(() => {
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
  background-color: #2c3e50;
  color: white;
  z-index: 100;
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

.logout-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
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
  background-color: #f8f9fa;
  color: #007bff;
}

.menu-list li.active {
  background-color: #e7f1ff;
  color: #007bff;
  border-left-color: #007bff;
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
  gap: 1.5rem;
  flex: 1;
}

.viz-col {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.viz-card {
  background: rgba(20, 20, 35, 0.8);
  border: 1px solid #2a2a40;
  border-radius: 8px;
  padding: 1rem;
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
  margin-bottom: 1rem;
}

.card-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #fff;
  font-weight: 500;
}

.decoration-line {
  height: 2px;
  width: 30px;
  background: #00f2ff;
  margin-top: 5px;
  box-shadow: 0 0 5px #00f2ff;
}

.chart-container {
  flex: 1;
  min-height: 200px;
}

.map-container {
  min-height: 400px;
}

/* KPI Board */
.kpi-board {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

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
