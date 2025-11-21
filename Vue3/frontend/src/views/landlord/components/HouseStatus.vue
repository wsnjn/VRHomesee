<template>
  <div class="house-status-page">
    <div class="page-header">
      <h2>房屋状态监控</h2>
      <div class="header-actions">
        <button @click="emit('refreshStatistics')" class="refresh-btn">刷新</button>
        <button @click="emit('refreshHouses')" class="refresh-btn">刷新房屋</button>
      </div>
    </div>

    <!-- 状态筛选器 -->
    <div class="status-filter">
      <div class="filter-title">状态筛选：</div>
      <div class="filter-buttons">
        <button 
          v-for="status in statusFilters" 
          :key="status.value"
          @click="toggleStatusFilter(status.value)"
          class="filter-btn"
          :class="{ active: selectedStatusFilters.includes(status.value) }"
        >
          <span class="filter-dot" :class="`dot-${status.value}`"></span>
          {{ status.label }}
        </button>
      </div>
    </div>

    <!-- 状态统计概览 -->
    <div class="status-overview">
      <div class="status-cards">
        <div class="status-card status-available" @click="toggleStatusFilter(0)">
          <div class="status-icon">🏠</div>
          <div class="status-info">
            <h3>{{ statistics.availableHouses || 0 }}</h3>
            <p>可租房屋</p>
          </div>
        </div>
        <div class="status-card status-rented" @click="toggleStatusFilter(1)">
          <div class="status-icon">✅</div>
          <div class="status-info">
            <h3>{{ statistics.rentedHouses || 0 }}</h3>
            <p>已租房屋</p>
          </div>
        </div>
        <div class="status-card status-offline" @click="toggleStatusFilter(2)">
          <div class="status-icon">⏸️</div>
          <div class="status-info">
            <h3>{{ statistics.offlineHouses || 0 }}</h3>
            <p>下架房屋</p>
          </div>
        </div>
        <div class="status-card status-pre-rent" @click="toggleStatusFilter(3)">
          <div class="status-icon">⏰</div>
          <div class="status-info">
            <h3>{{ statistics.preRentHouses || 0 }}</h3>
            <p>预租房屋</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 状态分布和详情 -->
    <div class="status-container">
      <!-- 房屋状态分布 -->
      <div class="chart-section">
        <h3>房屋状态分布</h3>
        <div class="status-distribution">
          <div class="distribution-bar">
            <div 
              class="distribution-segment available" 
              :style="{ width: getStatusPercentage('available') }"
              :title="`可租: ${statistics.availableHouses || 0}`"
            ></div>
            <div 
              class="distribution-segment rented" 
              :style="{ width: getStatusPercentage('rented') }"
              :title="`已租: ${statistics.rentedHouses || 0}`"
            ></div>
            <div 
              class="distribution-segment offline" 
              :style="{ width: getStatusPercentage('offline') }"
              :title="`下架: ${statistics.offlineHouses || 0}`"
            ></div>
            <div 
              class="distribution-segment pre-rent" 
              :style="{ width: getStatusPercentage('pre-rent') }"
              :title="`预租: ${statistics.preRentHouses || 0}`"
            ></div>
          </div>
          <div class="distribution-legend">
            <div class="legend-item">
              <span class="legend-color available"></span>
              <span>可租 ({{ statistics.availableHouses || 0 }})</span>
            </div>
            <div class="legend-item">
              <span class="legend-color rented"></span>
              <span>已租 ({{ statistics.rentedHouses || 0 }})</span>
            </div>
            <div class="legend-item">
              <span class="legend-color offline"></span>
              <span>下架 ({{ statistics.offlineHouses || 0 }})</span>
            </div>
            <div class="legend-item">
              <span class="legend-color pre-rent"></span>
              <span>预租 ({{ statistics.preRentHouses || 0 }})</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 房屋状态详情 -->
      <div class="chart-section">
        <h3>房屋状态详情</h3>
        <div class="status-details">
          <!-- 按状态分组显示 -->
          <div class="status-groups">
            <div 
              v-for="status in statusFilters" 
              :key="status.value"
              class="status-group"
              v-show="selectedStatusFilters.includes(status.value)"
            >
              <div class="group-header" :class="`header-${status.value}`">
                <h4>{{ status.label }} ({{ getHousesByStatus(status.value).length }})</h4>
                <span class="group-count">{{ getHousesByStatus(status.value).length }}</span>
              </div>
              <div class="group-houses">
                <div 
                  v-for="house in getHousesByStatus(status.value)" 
                  :key="house.id"
                  class="status-house-card"
                  :class="`house-${house.status}`"
                >
                  <div class="house-main-info">
                    <div class="house-address">
                      <strong>{{ getHouseFullAddress(house) }}</strong>
                      <span class="house-community">{{ house.communityName }}</span>
                    </div>
                    <div class="house-price-status">
                      <span class="house-price">{{ house.rentPrice }} 元/月</span>
                      <span class="house-status" :class="getStatusClass(house.status)">
                        {{ getStatusText(house.status) }}
                      </span>
                    </div>
                  </div>
                  <div class="house-details">
                    <div class="detail-item">
                      <span class="detail-label">面积：</span>
                      <span class="detail-value">{{ house.roomArea || '--' }} ㎡</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">楼层：</span>
                      <span class="detail-value">{{ house.floorInfo || '--' }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">朝向：</span>
                      <span class="detail-value">{{ house.orientation || '--' }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">装修：</span>
                      <span class="detail-value">{{ getDecorationText(house.decoration) }}</span>
                    </div>
                  </div>
                  <div class="house-actions">
                    <button 
                      @click="emit('toggleHouseStatus', house)" 
                      class="action-btn status-btn"
                      :class="getStatusBtnClass(house.status)"
                    >
                      {{ getStatusBtnText(house.status) }}
                    </button>
                    <button @click="emit('viewHouseDetail', house)" class="action-btn detail-btn">详情</button>
                    <button @click="emit('editHouse', house)" class="action-btn edit-btn">编辑</button>
                  </div>
                </div>
                <div v-if="getHousesByStatus(status.value).length === 0" class="no-houses-in-group">
                  暂无{{ status.label }}的房屋
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'

const props = defineProps({
  statistics: {
    type: Object,
    default: () => ({})
  },
  myHouses: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'refreshStatistics',
  'refreshHouses',
  'toggleHouseStatus',
  'viewHouseDetail',
  'editHouse'
])

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
  return props.myHouses.filter(house => house.status === status)
}

// 获取状态百分比
const getStatusPercentage = (statusType) => {
  const total = props.statistics.totalHouses || 1
  let count = 0
  
  switch (statusType) {
    case 'available':
      count = props.statistics.availableHouses || 0
      break
    case 'rented':
      count = props.statistics.rentedHouses || 0
      break
    case 'offline':
      count = props.statistics.offlineHouses || 0
      break
    case 'pre-rent':
      count = props.statistics.preRentHouses || 0
      break
  }
  
  return `${(count / total * 100).toFixed(1)}%`
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
</script>

<style scoped>
.house-status-page {
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

.refresh-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  background-color: #6c757d;
  color: white;
}

.refresh-btn:hover {
  background-color: #5a6268;
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

/* 状态统计概览 */
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
  cursor: pointer;
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

/* 状态容器 - 垂直布局 */
.status-container {
  display: flex;
  flex-direction: column;
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
  .status-charts {
    grid-template-columns: 1fr;
  }
  
  .distribution-legend {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .status-cards {
    grid-template-columns: 1fr 1fr;
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
  
  .house-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .status-cards {
    grid-template-columns: 1fr;
  }
  
  .filter-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-btn {
    justify-content: center;
  }
  
  .house-actions {
    flex-direction: column;
  }
  
  .house-actions .action-btn {
    flex: 1;
    min-width: 80px;
  }
}
</style>
