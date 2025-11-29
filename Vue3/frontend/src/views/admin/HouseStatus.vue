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
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8h5z"></path>
              </svg>
              <span>可租</span>
              <div class="status-count">{{ statistics.available || 0 }}</div>
            </label>

            <label for="switch-rented" class="switch-button">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"></path>
              </svg>
              <span>已租</span>
              <div class="status-count">{{ statistics.rented || 0 }}</div>
            </label>

            <label for="switch-offline" class="switch-button">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
              </svg>
              <span>下架</span>
              <div class="status-count">{{ statistics.offline || 0 }}</div>
            </label>

            <label for="switch-pre-rent" class="switch-button">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
              </svg>
              <span>预租</span>
              <div class="status-count">{{ statistics.preRent || 0 }}</div>
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
          @click="openHouseDetail(house)"
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
          <div v-if="houseDetailData" class="house-detail-content">
            <!-- 房屋基本信息 -->
            <div class="detail-section">
              <h4 class="section-title">基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>地址：</label>
                  <span>{{ getHouseFullAddress(houseDetailData) }}</span>
                </div>
                <div class="detail-item">
                  <label>面积：</label>
                  <span>{{ houseDetailData.roomArea || '--' }} ㎡</span>
                </div>
                <div class="detail-item">
                  <label>楼层：</label>
                  <span>{{ houseDetailData.floorInfo || '--' }}</span>
                </div>
                <div class="detail-item">
                  <label>朝向：</label>
                  <span>{{ houseDetailData.orientation || '--' }}</span>
                </div>
                <div class="detail-item">
                  <label>装修：</label>
                  <span>{{ getDecorationText(houseDetailData.decoration) }}</span>
                </div>
                <div class="detail-item">
                  <label>电梯：</label>
                  <span>{{ houseDetailData.hasElevator === 1 ? '有' : '无' }}</span>
                </div>
                <div class="detail-item">
                  <label>租赁类型：</label>
                  <span>{{ getRentalTypeText(houseDetailData.rentalType) }}</span>
                </div>
                <div class="detail-item">
                  <label>状态：</label>
                  <span class="house-status" :class="getStatusClass(houseDetailData.status)">
                    {{ getStatusText(houseDetailData.status) }}
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
                  <span class="price-highlight">{{ houseDetailData.rentPrice }} 元/月</span>
                </div>
                <div class="detail-item">
                  <label>水费单价：</label>
                  <span>{{ houseDetailData.waterPrice ? houseDetailData.waterPrice + '元/吨' : '包含在租金内' }}</span>
                </div>
                <div class="detail-item">
                  <label>电费单价：</label>
                  <span>{{ houseDetailData.electricPrice ? houseDetailData.electricPrice + '元/度' : '包含在租金内' }}</span>
                </div>
              </div>
            </div>

            <!-- 描述信息 -->
            <div v-if="houseDetailData.description" class="detail-section">
              <h4 class="section-title">房屋描述</h4>
              <div class="description-content">
                {{ houseDetailData.description }}
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
import { ref, computed, defineProps } from 'vue'

const props = defineProps({
  statistics: {
    type: Object,
    default: () => ({})
  },
  houses: {
    type: Array,
    default: () => []
  }
})

// 当前选中的状态
const selectedStatus = ref('0') // 默认选中"可租"状态

// 过滤后的房屋列表
const filteredHouses = computed(() => {
  return props.houses.filter(house => house.status === parseInt(selectedStatus.value))
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

// 房屋详情相关
const showHouseDetail = ref(false)
const houseDetailData = ref(null)

// 打开房屋详情
const openHouseDetail = (house) => {
  showHouseDetail.value = true
  houseDetailData.value = house
}

// 关闭房屋详情
const closeHouseDetail = () => {
  showHouseDetail.value = false
  houseDetailData.value = null
}
</script>

<style scoped>
.house-status-page {
  width: 100%;
  padding: 20px;
}

/* 翻转开关样式 */
.flip-switch-container {
  --card-width: 110px;
  --card-height: 120px;
  --switch-bg: rgba(255, 255, 255, 0.1);
  --switch-border-color: rgba(255, 255, 255, 0.2);
  --text-color: #000000;
  --inactive-text-color: #333333;
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
}

.flip-switch {
  display: flex;
  position: relative;
  width: calc(var(--card-width) * 4);
  height: var(--card-height);
  background: var(--switch-bg);
  border-radius: 20px;
  border: 1px solid var(--switch-border-color);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(12px);
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
  position: relative;
}

.flip-switch .switch-button:hover {
  color: var(--text-color);
}

.flip-switch .switch-button svg {
  width: 24px;
  height: 24px;
  transition: all 0.3s;
}

.flip-switch .switch-button span {
  font-size: 14px;
  font-weight: 500;
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
}

.flip-switch .switch-card {
  position: absolute;
  top: 0;
  left: 0;
  width: var(--card-width);
  height: var(--card-height);
  z-index: 1;
  transition: transform 0.6s cubic-bezier(0.76, 0, 0.24, 1);
}

.flip-switch .card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 20px;
  background: var(--card-bg);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.flip-switch #switch-available:checked ~ .switch-card { transform: translateX(0%); }
.flip-switch #switch-rented:checked ~ .switch-card { transform: translateX(100%); }
.flip-switch #switch-offline:checked ~ .switch-card { transform: translateX(200%); }
.flip-switch #switch-pre-rent:checked ~ .switch-card { transform: translateX(300%); }

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
}

/* 房屋列表样式 */
.houses-list-section {
  margin-top: 2rem;
}

.houses-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.status-house-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border-left: 4px solid transparent;
}

.status-house-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.house-available { border-left-color: #28a745; }
.house-rented { border-left-color: #dc3545; }
.house-offline { border-left-color: #6c757d; }
.house-pre-rent { border-left-color: #ffc107; }

.house-address {
  font-size: 1.1rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.house-price {
  color: #e74c3c;
  font-weight: 600;
  font-size: 1.1rem;
}

.no-houses {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem;
  color: #6c757d;
  font-size: 1.1rem;
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
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
}

.detail-section {
  margin-bottom: 2rem;
}

.section-title {
  margin: 0 0 1rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f0f2f5;
  color: #2c3e50;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detail-item label {
  color: #6c757d;
  font-size: 0.9rem;
}

.detail-item span {
  color: #2c3e50;
  font-weight: 500;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
}

.cancel-btn {
  padding: 0.5rem 1.5rem;
  border: 1px solid #e9ecef;
  background: white;
  border-radius: 6px;
  cursor: pointer;
}

.price-highlight {
  color: #e74c3c !important;
  font-size: 1.2rem;
  font-weight: 700 !important;
}

.status-available { color: #28a745 !important; }
.status-rented { color: #dc3545 !important; }
.status-offline { color: #6c757d !important; }
.status-pre-rent { color: #ffc107 !important; }
</style>
