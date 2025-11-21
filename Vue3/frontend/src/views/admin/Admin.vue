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
              <span class="menu-icon">📊</span>
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
              <span class="menu-icon">📋</span>
              租约列表
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
          <h3>房屋管理</h3>
          <ul class="menu-list">
            <li 
              :class="{ active: activeTab === 'house-management' }"
              @click="switchTab('house-management')"
            >
              <span class="menu-icon">🏠</span>
              房屋列表
            </li>
            <li 
              :class="{ active: activeTab === 'house-status' }"
              @click="switchTab('house-status')"
            >
              <span class="menu-icon">📈</span>
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
              <span class="menu-icon">👥</span>
              用户列表
            </li>
            <li 
              :class="{ active: activeTab === 'tenant-matching' }"
              @click="switchTab('tenant-matching')"
            >
              <span class="menu-icon">🤝</span>
              租客匹配
            </li>
          </ul>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 数据概览 -->
        <div v-if="activeTab === 'dashboard'" class="dashboard">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">📋</div>
              <div class="stat-info">
                <h3>{{ statistics.totalContracts || 0 }}</h3>
                <p>总租约数</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">✅</div>
              <div class="stat-info">
                <h3>{{ statistics.activeContracts || 0 }}</h3>
                <p>活跃租约</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">⏰</div>
              <div class="stat-info">
                <h3>{{ statistics.expiringContracts || 0 }}</h3>
                <p>即将到期</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">🏠</div>
              <div class="stat-info">
                <h3>{{ houseStatistics.totalHouses || 0 }}</h3>
                <p>总房屋数</p>
              </div>
            </div>
          </div>

          <div class="charts-section">
            <div class="chart-card">
              <h3>租约状态分布</h3>
              <div class="chart-placeholder">
                <!-- 这里可以放置图表组件 -->
                <p>租约状态分布图表</p>
                <div v-if="statistics.statusDistribution" class="status-list">
                  <div 
                    v-for="(count, status) in statistics.statusDistribution" 
                    :key="status"
                    class="status-item"
                  >
                    <span class="status-dot" :class="getStatusColor(status)"></span>
                    <span>{{ getStatusText(status) }}: {{ count }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="chart-card">
              <h3>近期到期租约</h3>
              <div class="expiring-list">
                <div 
                  v-for="contract in expiringContracts" 
                  :key="contract.id"
                  class="expiring-item"
                >
                  <div class="contract-info">
                    <strong>{{ contract.contractNumber }}</strong>
                    <span>到期: {{ formatDate(contract.contractEndDate) }}</span>
                  </div>
                  <span class="days-left">
                    {{ calculateDaysLeft(contract.contractEndDate) }}天
                  </span>
                </div>
                <div v-if="expiringContracts.length === 0" class="no-data">
                  暂无即将到期的租约
                </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
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
const statistics = ref({})
const houseStatistics = ref({})
const expiringContracts = ref([])

// 切换标签页
const switchTab = (tab) => {
  activeTab.value = tab
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

// 获取状态颜色
const getStatusColor = (status) => {
  const colors = {
    0: 'status-pending',
    1: 'status-active',
    2: 'status-processing',
    3: 'status-expired',
    4: 'status-cancelled',
    5: 'status-completed'
  }
  return colors[status] || 'status-unknown'
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
  return texts[status] || '未知状态'
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
    const response = await axios.get(`${API_BASE_URL}/admin/tenant/statistics`)
    if (response.data.success) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载房屋统计
const loadHouseStatistics = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/rooms/count`)
    if (response.data.success) {
      houseStatistics.value = response.data
    } else {
      houseStatistics.value = { totalHouses: 0 }
    }
  } catch (error) {
    console.error('加载房屋统计失败:', error)
    houseStatistics.value = { totalHouses: 0 }
  }
}

// 加载即将到期的租约
const loadExpiringContracts = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/admin/tenant/expiring/30`)
    if (response.data.success) {
      expiringContracts.value = response.data.contracts || []
    }
  } catch (error) {
    console.error('加载即将到期租约失败:', error)
  }
}

// 退出登录
const logout = () => {
  router.push('/login')
}

// 页面加载时初始化数据
onMounted(async () => {
  await loadStatistics()
  await loadHouseStatistics()
  await loadExpiringContracts()
})
</script>

<style scoped>
.admin {
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
  background-color: rgba(44, 62, 80, 0.95);
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
  color: #007bff;
}

.menu-list li.active {
  background-color: #e3f2fd;
  color: #007bff;
  border-left-color: #007bff;
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

.status-pending { background-color: #ffc107; }
.status-active { background-color: #28a745; }
.status-processing { background-color: #007bff; }
.status-expired { background-color: #dc3545; }
.status-cancelled { background-color: #6c757d; }
.status-completed { background-color: #17a2b8; }
.status-unknown { background-color: #e9ecef; }

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

/* 响应式设计 */
@media (max-width: 1024px) {
  .charts-section {
    grid-template-columns: 1fr;
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
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .navbar {
    padding: 1rem;
  }
  
  .main-content {
    padding: 1rem;
  }
}
</style>
