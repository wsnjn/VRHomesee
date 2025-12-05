<template>
  <div class="house-status-page">

    <!-- 状态统计概览 -->
    <div class="status-overview">
      <div class="status-cards">
        <!-- 四个状态的翻转开关 -->
        <div class="flip-switch-container">
          <div class="flip-switch">
            <input type="radio" id="switch-available" name="house-status-switch" value="0" v-model="selectedStatus" />
            <input type="radio" id="switch-rented" name="house-status-switch" value="1" v-model="selectedStatus" />
            <input type="radio" id="switch-offline" name="house-status-switch" value="2" v-model="selectedStatus" />
            <input type="radio" id="switch-pre-rent" name="house-status-switch" value="3" v-model="selectedStatus" />

            <label for="switch-available" class="switch-button">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                width="24"
                height="24"
                fill="currentColor"
              >
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8h5z"></path>
              </svg>
              <span>可租</span>
              <div class="status-count">{{ statistics.availableHouses || 0 }}</div>
            </label>

            <label for="switch-rented" class="switch-button">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                width="24"
                height="24"
                fill="currentColor"
              >
                <path
                  d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
                ></path>
              </svg>
              <span>已租</span>
              <div class="status-count">{{ statistics.rentedHouses || 0 }}</div>
            </label>

            <label for="switch-offline" class="switch-button">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                width="24"
                height="24"
                fill="currentColor"
              >
                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
              </svg>
              <span>下架</span>
              <div class="status-count">{{ statistics.offlineHouses || 0 }}</div>
            </label>

            <label for="switch-pre-rent" class="switch-button">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                width="24"
                height="24"
                fill="currentColor"
              >
                <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
              </svg>
              <span>预租</span>
              <div class="status-count">{{ statistics.preRentHouses || 0 }}</div>
            </label>

            <div class="switch-card">
              <div class="card-face card-front"></div>
              <div class="card-face card-back"></div>
              <div class="card-face card-left"></div>
              <div class="card-face card-right"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 房屋列表 -->
    <div class="houses-list-section">
      <div class="houses-list">
        <div 
          v-for="house in filteredHouses" 
          :key="house.id"
          class="status-house-card"
          :class="`house-${house.status}`"
        >
          <div class="house-main-info">
            <div class="house-address">
              <strong>{{ getHouseFullAddress(house) }}</strong>
            </div>
          <div class="house-price-status">
            <span class="house-price">{{ house.rentPrice }} 元/月</span>
          </div>
          </div>
        </div>
        <div v-if="filteredHouses.length === 0" class="no-houses">
          暂无{{ getStatusText(selectedStatus) }}的房屋
        </div>
      </div>
    </div>

    <!-- 房屋详情模态框 -->
    <div v-if="showHouseDetail" class="modal-overlay">
      <div class="modal-content" style="max-width: 800px;">
        <div class="modal-header">
          <h3>房屋详情</h3>
          <button @click="closeHouseDetail" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div v-if="loadingHouseDetail" class="loading">加载中...</div>
          <div v-else-if="houseDetailError" class="error-message">{{ houseDetailError }}</div>
          <div v-else-if="houseDetailData" class="house-detail-content">
            <!-- 房屋基本信息 -->
            <div class="detail-section">
              <h4 class="section-title">基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>地址：</label>
                  <span>{{ getHouseFullAddress(houseDetailData.houseInfo) }}</span>
                </div>
                <div class="detail-item">
                  <label>面积：</label>
                  <span>{{ houseDetailData.houseInfo.roomArea || '--' }} ㎡</span>
                </div>
                <div class="detail-item">
                  <label>楼层：</label>
                  <span>{{ houseDetailData.houseInfo.floorInfo || '--' }}</span>
                </div>
                <div class="detail-item">
                  <label>朝向：</label>
                  <span>{{ houseDetailData.houseInfo.orientation || '--' }}</span>
                </div>
                <div class="detail-item">
                  <label>装修：</label>
                  <span>{{ getDecorationText(houseDetailData.houseInfo.decoration) }}</span>
                </div>
                <div class="detail-item">
                  <label>电梯：</label>
                  <span>{{ houseDetailData.houseInfo.hasElevator === 1 ? '有' : '无' }}</span>
                </div>
                <div class="detail-item">
                  <label>租赁类型：</label>
                  <span>{{ getRentalTypeText(houseDetailData.houseInfo.rentalType) }}</span>
                </div>
                <div class="detail-item">
                  <label>状态：</label>
                  <span class="house-status" :class="getStatusClass(houseDetailData.houseInfo.status)">
                    {{ getStatusText(houseDetailData.houseInfo.status) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 价格信息 -->
            <div class="detail-section">
              <h4 class="section-title">价格信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>月租金：</label>
                  <span class="price-highlight">{{ houseDetailData.houseInfo.rentPrice }} 元/月</span>
                </div>
                <div class="detail-item">
                  <label>水费单价：</label>
                  <span>{{ houseDetailData.houseInfo.waterPrice ? houseDetailData.houseInfo.waterPrice + '元/吨' : '包含在租金内' }}</span>
                </div>
                <div class="detail-item">
                  <label>电费单价：</label>
                  <span>{{ houseDetailData.houseInfo.electricPrice ? houseDetailData.houseInfo.electricPrice + '元/度' : '包含在租金内' }}</span>
                </div>
              </div>
            </div>

            <!-- 租客信息 -->
            <div class="detail-section">
              <h4 class="section-title">租客信息</h4>
              <div v-if="!houseDetailData.hasTenant" class="no-tenant">
                <p>当前没有租客</p>
              </div>
              <div v-else class="tenants-list">
                <div v-for="(tenant, index) in houseDetailData.tenants" :key="tenant.id" class="tenant-item">
                  <h5>租客 {{ index + 1 }}</h5>
                  <div class="tenant-details">
                    <div class="detail-item">
                      <label>合同编号：</label>
                      <span>{{ tenant.contractNumber }}</span>
                    </div>
                    <div class="detail-item">
                      <label>租约状态：</label>
                      <span>{{ getContractStatusText(tenant.contractStatus) }}</span>
                    </div>
                    <div class="detail-item">
                      <label>合同期限：</label>
                      <span>{{ formatDate(tenant.contractStartDate) }} 至 {{ formatDate(tenant.contractEndDate) }}</span>
                    </div>
                    <div class="detail-item">
                      <label>月租金：</label>
                      <span>{{ tenant.monthlyRent }} 元</span>
                    </div>
                    <div class="detail-item">
                      <label>押金：</label>
                      <span>{{ tenant.depositAmount }} 元</span>
                    </div>
                    <div class="detail-item">
                      <label>付款周期：</label>
                      <span>{{ getPaymentCycleText(tenant.paymentCycle) }}</span>
                    </div>
                    <div class="detail-item">
                      <label>租金状态：</label>
                      <span>{{ getRentStatusText(tenant.rentStatus) }}</span>
                    </div>
                    <div class="detail-item">
                      <label>押金状态：</label>
                      <span>{{ getDepositStatusText(tenant.depositStatus) }}</span>
                    </div>
                    <div v-if="tenant.emergencyContact" class="detail-item">
                      <label>紧急联系人：</label>
                      <span>{{ tenant.emergencyContact }} ({{ tenant.emergencyPhone || '无电话' }})</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 描述信息 -->
            <div v-if="houseDetailData.houseInfo.description" class="detail-section">
              <h4 class="section-title">房屋描述</h4>
              <div class="description-content">
                {{ houseDetailData.houseInfo.description }}
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeHouseDetail" class="cancel-btn">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue'
import axios from 'axios'

const API_BASE_URL = 'https://api.homesee.xyz/api'

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

// 当前选中的状态
const selectedStatus = ref('0') // 默认选中"可租"状态

// 过滤后的房屋列表
const filteredHouses = computed(() => {
  return props.myHouses.filter(house => house.status === parseInt(selectedStatus.value))
})

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

// 获取租赁类型文本
const getRentalTypeText = (rentalType) => {
  const rentalTypeMap = {
    0: '整租',
    1: '合租',
    2: '单间'
  }
  return rentalTypeMap[rentalType] || '未知'
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

// 房屋详情相关
const showHouseDetail = ref(false)
const loadingHouseDetail = ref(false)
const houseDetailData = ref(null)
const houseDetailError = ref('')

// 切换房屋状态
const toggleHouseStatus = async (house) => {
  if (!house.id) return
  
  const currentStatus = house.status
  let newStatus
  
  // 根据当前状态确定新状态
  switch (currentStatus) {
    case 0: // 可租 -> 下架
      newStatus = 2
      break
    case 1: // 已租 -> 可租
      newStatus = 0
      break
    case 2: // 下架 -> 可租
      newStatus = 0
      break
    case 3: // 预租 -> 可租
      newStatus = 0
      break
    default:
      return
  }
  
  try {
    const response = await axios.put(`${API_BASE_URL}/landlord/house/${house.id}`, {
      status: newStatus
    })
    
    if (response.data.success) {
      alert('房屋状态更新成功！')
      // 重新加载房屋列表
      emit('refreshHouses')
    } else {
      alert(`更新失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('更新房屋状态失败:', error)
    alert('更新房屋状态失败，请稍后重试')
  }
}

// 打开房屋详情
const openHouseDetail = async (house) => {
  showHouseDetail.value = true
  loadingHouseDetail.value = true
  houseDetailError.value = ''
  houseDetailData.value = null
  
  try {
    const response = await axios.get(`${API_BASE_URL}/landlord/house/${house.id}`)
    
    if (response.data.success) {
      houseDetailData.value = response.data.data
    } else {
      houseDetailError.value = response.data.message || '获取房屋详情失败'
    }
  } catch (error) {
    console.error('获取房屋详情失败:', error)
    houseDetailError.value = '获取房屋详情失败，请稍后重试'
  } finally {
    loadingHouseDetail.value = false
  }
}

// 关闭房屋详情
const closeHouseDetail = () => {
  showHouseDetail.value = false
  houseDetailData.value = null
  houseDetailError.value = ''
}

// 租约状态文本映射
const getContractStatusText = (status) => {
  const statusMap = {
    0: '待签约',
    1: '已签约',
    2: '履行中',
    3: '已到期',
    4: '提前解约',
    5: '已退租'
  }
  return statusMap[status] || '未知'
}

// 付款周期文本映射
const getPaymentCycleText = (cycle) => {
  const cycleMap = {
    1: '月付',
    2: '季付',
    3: '年付'
  }
  return cycleMap[cycle] || '未知'
}

// 租金状态文本映射
const getRentStatusText = (status) => {
  const statusMap = {
    0: '未付款',
    1: '已付款',
    2: '逾期',
    3: '部分付款'
  }
  return statusMap[status] || '未知'
}

// 押金状态文本映射
const getDepositStatusText = (status) => {
  const statusMap = {
    0: '未付',
    1: '已付',
    2: '已退',
    3: '抵扣中'
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
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

/* 翻转开关样式 */
.flip-switch-container {
  --card-width: 110px;
  --card-height: 120px;
  --switch-bg: rgba(255, 255, 255, 0.1);
  --switch-border-color: rgba(255, 255, 255, 0.2);
  --text-color: #000000;  /* 改为黑色文字 */
  --inactive-text-color: #333333;  /* 改为深灰色 */
  --icon-shadow-color: rgba(0, 0, 0, 0.3);
  --card-bg: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.2),
    rgba(255, 255, 255, 0.1)
  );
  --highlight-color: #64ffda;

  display: grid;
  place-content: center;
  min-height: 100%;
  font-family: "Poppins", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.flip-switch {
  display: flex;
  position: relative;
  width: calc(var(--card-width) * 4);
  height: var(--card-height);
  background: var(--switch-bg);
  border-radius: 20px;
  border: 1px solid var(--switch-border-color);
  box-shadow:
    0 8px 32px 0 rgba(31, 38, 135, 0.37),
    inset 0 4px 8px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  perspective: 1000px;
}

.flip-switch input[type="radio"] {
  display: none;
}

.flip-switch .switch-button {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  z-index: 2;
  color: var(--inactive-text-color);
  transition: all 0.3s ease;
  -webkit-tap-highlight-color: transparent;
  position: relative;
}

.flip-switch .switch-button:hover {
  color: var(--text-color);
}

.flip-switch .switch-button:hover svg {
  transform: translateY(-3px);
  filter: drop-shadow(0 4px 6px var(--icon-shadow-color)) brightness(1.2);
}

.flip-switch .switch-button svg {
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
  filter: drop-shadow(0 2px 3px var(--icon-shadow-color));
  width: 24px;
  height: 24px;
}

.flip-switch .switch-button span {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.flip-switch .status-count {
  font-size: 18px;
  font-weight: 700;
  margin-top: 4px;
}

.flip-switch #switch-available:checked ~ [for="switch-available"],
.flip-switch #switch-rented:checked ~ [for="switch-rented"],
.flip-switch #switch-offline:checked ~ [for="switch-offline"],
.flip-switch #switch-pre-rent:checked ~ [for="switch-pre-rent"] {
  color: var(--text-color);
  text-shadow: 0 0 8px rgba(100, 255, 218, 0.5);
}

.flip-switch .switch-card {
  position: absolute;
  top: 0;
  left: 0;
  width: var(--card-width);
  height: var(--card-height);
  z-index: 1;
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.76, 0, 0.24, 1);
}

.flip-switch .card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 20px;
  background: var(--card-bg);
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.2),
    inset 0 2px 4px rgba(255, 255, 255, 0.1);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.flip-switch #switch-available:checked ~ .switch-card {
  transform: translateX(0%);
}

.flip-switch #switch-rented:checked ~ .switch-card {
  transform: translateX(100%);
}

.flip-switch #switch-offline:checked ~ .switch-card {
  transform: translateX(200%);
}

.flip-switch #switch-pre-rent:checked ~ .switch-card {
  transform: translateX(300%);
}

.flip-switch #switch-available:checked ~ [for="switch-available"]::after,
.flip-switch #switch-rented:checked ~ [for="switch-rented"]::after,
.flip-switch #switch-offline:checked ~ [for="switch-offline"]::after,
.flip-switch #switch-pre-rent:checked ~ [for="switch-pre-rent"]::after {
  content: "";
  position: absolute;
  bottom: -5px;
  width: 30px;
  height: 3px;
  background: var(--highlight-color);
  border-radius: 2px;
  animation: glow 1.5s infinite alternate;
}

@keyframes glow {
  from {
    box-shadow: 0 0 5px var(--highlight-color);
  }
  to {
    box-shadow:
      0 0 15px var(--highlight-color),
      0 0 25px var(--highlight-color);
  }
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
  /* 移除高度限制，让内容自然展开 */
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
  cursor: pointer;
}

.group-header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.collapse-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.collapse-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.collapse-icon {
  display: inline-block;
  transition: transform 0.3s ease;
  font-size: 0.8rem;
}

.collapse-icon.collapsed {
  transform: rotate(-90deg);
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
  position: relative;
  overflow: hidden;
}

.status-house-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 6px;
  height: 100%;
  background: linear-gradient(135deg, rgba(40, 167, 69, 0.3), rgba(40, 167, 69, 0.1));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 3px 0 0 3px;
}

.status-house-card.house-0::before {
  background: linear-gradient(135deg, rgba(40, 167, 69, 0.3), rgba(40, 167, 69, 0.1));
}

.status-house-card.house-1::before {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.3), rgba(220, 53, 69, 0.1));
}

.status-house-card.house-2::before {
  background: linear-gradient(135deg, rgba(108, 117, 125, 0.3), rgba(108, 117, 125, 0.1));
}

.status-house-card.house-3::before {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.3), rgba(255, 193, 7, 0.1));
}

.status-house-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

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
  gap: 0.75rem;
  justify-content: flex-end;
}

.action-btn {
  padding: 0.75rem 1.25rem;
  border: 2px solid rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
  font-size: 0.9rem;
  white-space: nowrap;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #000000;
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.1),
    inset 0 2px 4px rgba(255, 255, 255, 0.1);
}

.action-btn::after {
  content: "";
  position: absolute;
  inset: 0;
  background: rgba(100, 255, 218, 0.1);
  z-index: -1;
  transform: translateX(-100%);
  transition: transform 0.3s ease;
}

.action-btn:hover::after {
  transform: translateX(0);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 6px 20px rgba(0, 0, 0, 0.15),
    inset 0 2px 4px rgba(255, 255, 255, 0.2);
  border-color: rgba(100, 255, 218, 0.5);
}

.action-btn:active {
  transform: translateY(0);
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.1),
    inset 0 1px 2px rgba(255, 255, 255, 0.1);
}

.edit-btn {
  background: linear-gradient(135deg, rgba(23, 162, 184, 0.1), rgba(19, 132, 150, 0.1));
  border-color: rgba(23, 162, 184, 0.3);
}

.edit-btn:hover {
  background: linear-gradient(135deg, rgba(23, 162, 184, 0.2), rgba(19, 132, 150, 0.2));
  border-color: rgba(23, 162, 184, 0.6);
}

.detail-btn {
  background: linear-gradient(135deg, rgba(108, 117, 125, 0.1), rgba(90, 98, 104, 0.1));
  border-color: rgba(108, 117, 125, 0.3);
}

.detail-btn:hover {
  background: linear-gradient(135deg, rgba(108, 117, 125, 0.2), rgba(90, 98, 104, 0.2));
  border-color: rgba(108, 117, 125, 0.6);
}

.status-btn {
  background: linear-gradient(135deg, rgba(100, 255, 218, 0.1), rgba(100, 255, 218, 0.05));
  border-color: rgba(100, 255, 218, 0.3);
}

.status-btn:hover {
  background: linear-gradient(135deg, rgba(100, 255, 218, 0.2), rgba(100, 255, 218, 0.1));
  border-color: rgba(100, 255, 218, 0.6);
}

.btn-offline {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.1), rgba(200, 35, 51, 0.05));
  border-color: rgba(220, 53, 69, 0.3);
}

.btn-offline:hover {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.2), rgba(200, 35, 51, 0.1));
  border-color: rgba(220, 53, 69, 0.6);
}

.btn-available {
  background: linear-gradient(135deg, rgba(40, 167, 69, 0.1), rgba(33, 136, 56, 0.05));
  border-color: rgba(40, 167, 69, 0.3);
}

.btn-available:hover {
  background: linear-gradient(135deg, rgba(40, 167, 69, 0.2), rgba(33, 136, 56, 0.1));
  border-color: rgba(40, 167, 69, 0.6);
}

.btn-online {
  background: linear-gradient(135deg, rgba(23, 162, 184, 0.1), rgba(19, 132, 150, 0.05));
  border-color: rgba(23, 162, 184, 0.3);
}

.btn-online:hover {
  background: linear-gradient(135deg, rgba(23, 162, 184, 0.2), rgba(19, 132, 150, 0.1));
  border-color: rgba(23, 162, 184, 0.6);
}

.btn-default {
  background: linear-gradient(135deg, rgba(108, 117, 125, 0.1), rgba(90, 98, 104, 0.05));
  border-color: rgba(108, 117, 125, 0.3);
}

.btn-default:hover {
  background: linear-gradient(135deg, rgba(108, 117, 125, 0.2), rgba(90, 98, 104, 0.1));
  border-color: rgba(108, 117, 125, 0.6);
}

.no-houses-in-group {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
  font-style: italic;
  background: white;
  border-radius: 8px;
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
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-height: 90vh;
  overflow-y: auto;
  width: 100%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
  border-radius: 12px 12px 0 0;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
  padding: 0.25rem;
  line-height: 1;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #dc3545;
}

.modal-body {
  padding: 1.5rem;
  max-height: calc(90vh - 120px);
  overflow-y: auto;
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.cancel-btn {
  padding: 0.75rem 1.5rem;
  border: 1px solid #6c757d;
  border-radius: 6px;
  background: white;
  color: #6c757d;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #6c757d;
  color: white;
}

/* 房屋详情内容样式 */
.loading, .error-message {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}

.error-message {
  color: #dc3545;
}

.detail-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e9ecef;
}

.detail-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.detail-item label {
  font-weight: 600;
  color: #495057;
  margin-right: 1rem;
  min-width: 100px;
}

.detail-item span {
  color: #2c3e50;
  text-align: right;
  flex: 1;
}

.price-highlight {
  color: #dc3545;
  font-weight: 700;
  font-size: 1.1rem;
}

/* 租客信息样式 */
.no-tenant {
  text-align: center;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 6px;
  color: #6c757d;
}

.tenants-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.tenant-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  border-left: 4px solid #17a2b8;
}

.tenant-item h5 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.tenant-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.75rem;
}

.tenant-details .detail-item {
  background: white;
  margin-bottom: 0.5rem;
}

.description-content {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 6px;
  line-height: 1.6;
  color: #495057;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-content {
    margin: 1rem;
    max-height: 85vh;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .tenant-details {
    grid-template-columns: 1fr;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-item label {
    margin-bottom: 0.25rem;
    min-width: auto;
  }
  
  .detail-item span {
    text-align: left;
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
  
  .modal-body {
    padding: 1rem;
  }
  
  .modal-header, .modal-footer {
    padding: 1rem;
  }
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
