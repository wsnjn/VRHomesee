<!--
/**
 * 文件名: HouseSelection.vue
 * 作者: 牛迦楠
 * 学校: 东华理工大学
 * 专业: 软件工程（中外合作办学）
 * 课题: 融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 创建日期: 2026-01-06
 * 描述: 房屋选择组件，提供多维度筛选功能和房源列表展示
 */
-->
<template>
  <div class="house-selection">
    <Navbar />
    <div class="page-header-spacer"></div>

    <div class="content-wrapper">
      <!-- 筛选条件 -->
      <div class="filter-section">
        <div class="search-row">
          <div class="search-box">
            <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索小区、地址..." 
              @keyup.enter="loadHouses"
            />
            <button @click="loadHouses" class="search-btn">搜索</button>
          </div>
        </div>

        <div class="filters-grid">
          <div class="filter-group">
            <label>区域选择</label>
            <div class="filter-row">
              <select v-model="selectedProvince" @change="onProvinceChange">
                <option value="">省份</option>
                <option v-for="province in provinces" :key="province" :value="province">{{ province }}</option>
              </select>
              <select v-model="selectedCity" @change="onCityChange">
                <option value="">城市</option>
                <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
              </select>
              <select v-model="selectedDistrict" @change="onDistrictChange">
                <option value="">区县</option>
                <option v-for="district in districts" :key="district" :value="district">{{ district }}</option>
              </select>
              <select v-model="selectedStreet" @change="onStreetChange">
                <option value="">街道</option>
                <option v-for="street in streets" :key="street" :value="street">{{ street }}</option>
              </select>
              <select v-model="selectedCommunity" @change="onCommunityChange">
                <option value="">小区</option>
                <option v-for="community in communities" :key="community" :value="community">{{ community }}</option>
              </select>
            </div>
          </div>

          <div class="filter-group">
            <label>基本筛选</label>
            <div class="filter-row">
              <select v-model="selectedRentalType">
                <option value="">租赁类型</option>
                <option v-for="(type, key) in rentalTypes" :key="key" :value="key">{{ type }}</option>
              </select>
              <select v-model="selectedDecoration">
                <option value="">装修程度</option>
                <option v-for="(decoration, key) in decorationTypes" :key="key" :value="key">{{ decoration }}</option>
              </select>
              <select v-model="selectedElevator">
                <option value="">电梯</option>
                <option v-for="(elevator, key) in elevatorOptions" :key="key" :value="key">{{ elevator }}</option>
              </select>
              <select v-model="selectedOrientation">
                <option value="">朝向</option>
                <option v-for="(orientation, key) in orientationOptions" :key="key" :value="key">{{ orientation }}</option>
              </select>
            </div>
          </div>

          <div class="filter-group">
            <label>价格与面积</label>
            <div class="filter-row range-inputs">
              <div class="range-group">
                <input type="number" v-model="minPrice" placeholder="最低租金" min="0" />
                <span>-</span>
                <input type="number" v-model="maxPrice" placeholder="最高租金" min="0" />
              </div>
              <div class="range-group">
                <input type="number" v-model="minArea" placeholder="最小面积" min="0" />
                <span>-</span>
                <input type="number" v-model="maxArea" placeholder="最大面积" min="0" />
              </div>
            </div>
          </div>
        </div>

        <div class="filter-actions">
          <button @click="resetFilters" class="reset-btn">重置筛选</button>
          <button @click="applyFilters" class="apply-btn">应用筛选</button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-section">
        <div class="spinner"></div>
        <p>正在寻找合适房源...</p>
      </div>

      <!-- 房屋列表 -->
      <div v-else class="house-list-section">
        <div class="list-header">
          <h3>发现 {{ houses.length }} 套好房</h3>
        </div>
        
        <div class="house-grid">
          <div 
            v-for="house in houses" 
            :key="house.id"
            class="house-card"
            @click="selectHouse(house)"
          >
            <div class="house-image-wrapper">
              <img :src="getHouseImage(house)" :alt="house.communityName" class="house-image" />
              <div class="house-tags">
                <span class="tag status-tag" :class="getStatusClass(house.status)">
                  {{ getStatusText(house.status) }}
                </span>
                <span class="tag type-tag">{{ getLayoutType(house.rentalType) }}</span>
              </div>
              <div class="hover-overlay">
                <button @click.stop="viewHouseTour(house)" class="overlay-btn vr-btn">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12h20"></path><path d="M2 12l5-5"></path><path d="M22 12l-5-5"></path><path d="M12 7v10"></path></svg>
                  VR看房
                </button>
              </div>
            </div>
            
            <div class="house-info">
              <h4 class="house-title">{{ getHouseTitle(house) }}</h4>
              <p class="house-address">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                {{ getHouseAddress(house) }}
              </p>
              <p v-if="house.landlordPhone" class="landlord-phone">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                房东电话: {{ formatPhoneNumber(house.landlordPhone) }}
              </p>
              
              <div class="house-meta">
                <span class="meta-item">{{ house.roomArea }}㎡</span>
                <span class="meta-divider">|</span>
                <span class="meta-item">{{ house.floorInfo }}</span>
                <span class="meta-divider">|</span>
                <span class="meta-item">{{ getDecorationText(house.decoration) }}</span>
              </div>
              
              <div class="house-footer">
                <div class="price-container">
                  <span class="price-symbol">¥</span>
                  <span class="price-value">{{ house.rentPrice }}</span>
                  <span class="price-unit">/月</span>
                </div>
                <button 
                  @click.stop="makeAppointment(house)" 
                  class="action-btn appointment-btn"
                  :disabled="house.status !== 0"
                >
                  {{ house.status === 0 ? '预约看房' : '已下架' }}
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="houses.length === 0" class="empty-state">
          <p>暂无符合条件的房源，换个筛选条件试试吧</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()

// API基础URL
const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const loading = ref(false)
const houses = ref([])
const provinces = ref([])
const cities = ref([])
const districts = ref([])
const streets = ref([])
const communities = ref([])
const rentalTypes = ref({})
const decorationTypes = ref({})
const elevatorOptions = ref({})
const orientationOptions = ref({})
const searchQuery = ref('')

// 筛选条件
const selectedProvince = ref('')
const selectedCity = ref('')
const selectedDistrict = ref('')
const selectedStreet = ref('')
const selectedCommunity = ref('')
const selectedRentalType = ref('')
const selectedDecoration = ref('')
const selectedElevator = ref('')
const selectedOrientation = ref('')
const minPrice = ref('')
const maxPrice = ref('')
const minArea = ref('')
const maxArea = ref('')

// 加载筛选选项
const loadFilterOptions = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/room-info/filter-options`)
    const options = response.data
    
    provinces.value = options.provinces || []
    rentalTypes.value = options.rentalTypes || {}
    decorationTypes.value = options.decorationTypes || {}
    elevatorOptions.value = options.elevatorOptions || {}
    orientationOptions.value = options.orientationOptions || {}
  } catch (error) {
    console.error('加载筛选选项失败:', error)
  }
}

// 根据省份加载城市
const loadCitiesByProvince = async (province) => {
  if (!province) {
    cities.value = []
    selectedCity.value = ''
    selectedDistrict.value = ''
    selectedStreet.value = ''
    selectedCommunity.value = ''
    districts.value = []
    streets.value = []
    communities.value = []
    return
  }
  
  try {
    const response = await axios.get(`${API_BASE_URL}/room-info/cities/${encodeURIComponent(province)}`)
    cities.value = response.data || []
  } catch (error) {
    console.error('加载城市数据失败:', error)
    cities.value = []
  }
}

// 根据城市加载区县
const loadDistrictsByCity = async (city) => {
  if (!city) {
    districts.value = []
    selectedDistrict.value = ''
    selectedStreet.value = ''
    selectedCommunity.value = ''
    streets.value = []
    communities.value = []
    return
  }
  
  try {
    const response = await axios.get(`${API_BASE_URL}/room-info/districts/${encodeURIComponent(city)}`)
    districts.value = response.data || []
  } catch (error) {
    console.error('加载区县数据失败:', error)
    districts.value = []
  }
}

// 根据区县加载街道
const loadStreetsByDistrict = async (district) => {
  if (!district) {
    streets.value = []
    selectedStreet.value = ''
    selectedCommunity.value = ''
    communities.value = []
    return
  }
  
  try {
    const response = await axios.get(`${API_BASE_URL}/room-info/streets/${encodeURIComponent(district)}`)
    streets.value = response.data || []
  } catch (error) {
    console.error('加载街道数据失败:', error)
    streets.value = []
  }
}

// 根据街道加载小区
const loadCommunitiesByStreet = async (street) => {
  if (!street) {
    communities.value = []
    selectedCommunity.value = ''
    return
  }
  
  try {
    const response = await axios.get(`${API_BASE_URL}/room-info/communities/${encodeURIComponent(street)}`)
    communities.value = response.data || []
  } catch (error) {
    console.error('加载小区数据失败:', error)
    communities.value = []
  }
}

// 加载房屋数据
const loadHouses = async () => {
  loading.value = true
  try {
    const params = {
      province: selectedProvince.value,
      city: selectedCity.value,
      district: selectedDistrict.value,
      street: selectedStreet.value,
      communityName: selectedCommunity.value,
      rentalType: selectedRentalType.value ? parseInt(selectedRentalType.value) : undefined,
      decoration: selectedDecoration.value ? parseInt(selectedDecoration.value) : undefined,
      minPrice: minPrice.value ? parseFloat(minPrice.value) : undefined,
      maxPrice: maxPrice.value ? parseFloat(maxPrice.value) : undefined,
      minArea: minArea.value ? parseFloat(minArea.value) : undefined,
      maxArea: maxArea.value ? parseFloat(maxArea.value) : undefined,
      keyword: searchQuery.value || undefined
    }

    // 移除undefined参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })

    const response = await axios.get(`${API_BASE_URL}/room-info/filter`, { params })
    houses.value = response.data || []
  } catch (error) {
    console.error('加载房屋数据失败:', error)
    houses.value = []
  } finally {
    loading.value = false
  }
}

// 监听筛选条件变化
watch([
  selectedProvince,
  selectedCity,
  selectedDistrict,
  selectedStreet,
  selectedCommunity,
  selectedRentalType,
  selectedDecoration,
  selectedElevator,
  selectedOrientation,
  minPrice,
  maxPrice,
  minArea,
  maxArea
], () => {
  // 可以选择自动加载，或者只在点击应用时加载
  // loadHouses()
})

// 筛选变化处理
const onProvinceChange = () => {
  selectedCity.value = ''
  selectedDistrict.value = ''
  selectedStreet.value = ''
  selectedCommunity.value = ''
  cities.value = []
  districts.value = []
  streets.value = []
  communities.value = []
  loadCitiesByProvince(selectedProvince.value)
}

const onCityChange = () => {
  selectedDistrict.value = ''
  selectedStreet.value = ''
  selectedCommunity.value = ''
  districts.value = []
  streets.value = []
  communities.value = []
  loadDistrictsByCity(selectedCity.value)
}

const onDistrictChange = () => {
  selectedStreet.value = ''
  selectedCommunity.value = ''
  streets.value = []
  communities.value = []
  loadStreetsByDistrict(selectedDistrict.value)
}

const onStreetChange = () => {
  selectedCommunity.value = ''
  communities.value = []
  loadCommunitiesByStreet(selectedStreet.value)
}

const onCommunityChange = () => {
  // 小区变化时自动重新加载房屋数据
}

// 重置筛选
const resetFilters = () => {
  selectedProvince.value = ''
  selectedCity.value = ''
  selectedDistrict.value = ''
  selectedStreet.value = ''
  selectedCommunity.value = ''
  selectedRentalType.value = ''
  selectedDecoration.value = ''
  selectedElevator.value = ''
  selectedOrientation.value = ''
  minPrice.value = ''
  maxPrice.value = ''
  minArea.value = ''
  maxArea.value = ''
  cities.value = []
  districts.value = []
  streets.value = []
  communities.value = []
  searchQuery.value = ''
  loadHouses()
}

// 应用筛选
const applyFilters = () => {
  loadHouses()
}

// 选择房屋
const selectHouse = (house) => {
  router.push({
    path: '/house-tour',
    query: { houseId: house.id }
  })
}

// 获取房屋标题
const getHouseTitle = (house) => {
  return `${house.communityName} ${house.buildingUnit || ''}${house.doorNumber}`
}

// 获取房屋地址
const getHouseAddress = (house) => {
  return `${house.district}${house.street}${house.communityName}`
}

// 获取租赁类型文字
const getLayoutType = (rentalType) => {
  const types = {
    0: '整租',
    1: '合租',
    2: '单间'
  }
  return types[rentalType] || '未知类型'
}

// 获取装修程度文字
const getDecorationText = (decoration) => {
  const types = {
    1: '毛坯',
    2: '简装',
    3: '精装',
    4: '豪装'
  }
  return types[decoration] || '未知'
}

// 获取房屋图片
const getHouseImage = (house) => {
  // 这里可以使用真实的图片URL，暂时使用占位图
  return '/models/客厅.jpg'
}

// 获取状态文字
const getStatusText = (status) => {
  const statusMap = {
    0: '在租',
    1: '已下架'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-available',
    1: 'status-unavailable'
  }
  return classMap[status] || 'status-unknown'
}

// VR看房
const viewHouseTour = (house) => {
  router.push({
    path: '/house-tour',
    query: { houseId: house.id }
  })
}

// 预约看房
const makeAppointment = (house) => {
  router.push({
    path: '/appointment',
    query: { houseId: house.id }
  })
}

// 格式化电话号码
const formatPhoneNumber = (phone) => {
  if (!phone) return ''
  // 将手机号格式化为 123-4567-8901 的格式
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`
  }
  return phone
}

// 页面加载时初始化数据
onMounted(async () => {
  await loadFilterOptions()
  await loadHouses()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap');

.house-selection {
  min-height: 100vh;
  background-color: #FFFFFF;
  font-family: 'Inter', sans-serif;
  padding-bottom: 4rem;
  color: #111827;
}

.page-header-spacer {
  height: 64px; /* Matches Navbar height */
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  border-left: 1px solid #F3F4F6;
  border-right: 1px solid #F3F4F6;
  min-height: calc(100vh - 64px);
}

/* Filter Section */
.filter-section {
  background: white;
  border-radius: 0;
  padding: 40px 0;
  box-shadow: none;
  margin-bottom: 40px;
  border-bottom: 1px solid #E5E7EB;
}

.search-row {
  margin-bottom: 32px;
  display: flex;
  justify-content: flex-start; /* Left align for industrial look */
}

.search-box {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 100%; /* Full width */
  background: white;
  border: 1px solid #111827; /* Strong border */
  border-radius: 0;
  padding: 0;
  transition: all 0.2s;
}

.search-box:focus-within {
  box-shadow: none;
  border-color: #2563EB;
}

.search-icon {
  margin-left: 16px;
  color: #111827;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 16px;
  font-size: 1rem;
  color: #111827;
  font-family: 'Inter', sans-serif;
}

.search-box input:focus {
  outline: none;
}

.search-btn {
  background: #111827;
  color: white;
  border: none;
  padding: 0 32px;
  height: 100%; /* Full height of container */
  border-radius: 0;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 0;
}

.search-btn:hover {
  background: #2563EB;
  transform: none;
  box-shadow: none;
}

.filters-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.filter-group {
  border-bottom: 1px solid #F3F4F6;
  padding-bottom: 24px;
}

.filter-group:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.filter-group label {
  display: block;
  font-size: 0.8rem;
  font-weight: 700;
  color: #9CA3AF;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

select, .range-group input {
  padding: 10px 12px;
  border: 1px solid #E5E7EB;
  border-radius: 0;
  background-color: white;
  color: #111827;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
  min-width: 0; /* Fix grid overflow */
}

select:hover, .range-group input:hover {
  border-color: #111827;
}

select:focus, .range-group input:focus {
  outline: none;
  border-color: #111827;
  box-shadow: none;
}

.range-inputs {
  display: flex;
  gap: 24px;
}

.range-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-group span {
  color: #9CA3AF;
}

.range-group input {
  width: 100px;
  min-width: auto;
  font-family: 'JetBrains Mono', monospace;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #E5E7EB;
}

.reset-btn {
  background: white;
  border: 1px solid #E5E7EB;
  color: #111827;
  padding: 10px 24px;
  border-radius: 0;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  font-size: 0.85rem;
}

.reset-btn:hover {
  background: #F9FAFB;
  border-color: #111827;
}

.apply-btn {
  background: #111827;
  color: white;
  border: 1px solid #111827;
  padding: 10px 32px;
  border-radius: 0;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  font-size: 0.85rem;
}

.apply-btn:hover {
  background: white;
  color: #111827;
  transform: none;
  box-shadow: none;
}

/* House List */
.house-list-section {
  margin-top: 0;
}

.list-header {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.list-header h3 {
  font-size: 1rem;
  color: #9CA3AF;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.house-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* Strict grid */
  gap: 24px;
  margin-bottom: 40px;
}

.house-card {
  background: white;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
  border: 1px solid #E5E7EB;
  transition: all 0.2s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.house-card:hover {
  border-color: #111827;
  transform: translateY(-2px);
}

.house-image-wrapper {
  position: relative;
  height: 240px;
  overflow: hidden;
  background-color: #F3F4F6;
}

.house-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.house-card:hover .house-image {
  transform: scale(1.05);
}

.house-tags {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  gap: 8px;
}

.tag {
  padding: 4px 8px;
  border-radius: 0;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-family: 'JetBrains Mono', monospace;
}

.status-tag.status-available { background: #111827; color: white; }
.status-tag.status-unavailable { background: #9CA3AF; color: white; }
.type-tag { background: white; color: #111827; border: 1px solid #111827; }

.hover-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(17, 24, 39, 0.4); /* Dark overlay */
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.house-card:hover .hover-overlay {
  opacity: 1;
}

.overlay-btn {
  background: white;
  color: #111827;
  border: none;
  padding: 12px 24px;
  border-radius: 0;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 1px;
}

.overlay-btn:hover {
  background: #111827;
  color: white;
}

.house-info {
  padding: 24px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.house-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.house-address {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6B7280;
  font-size: 0.85rem;
  margin: 0 0 16px 0;
}

.landlord-phone {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6B7280;
  font-size: 0.85rem;
  margin: 0 0 16px 0;
  font-family: 'JetBrains Mono', monospace;
}

.house-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #F3F4F6;
}

.meta-item {
  color: #374151;
  font-size: 0.9rem;
  font-weight: 500;
}

.meta-divider { color: #E5E7EB; }

.house-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-container {
  color: #111827;
  font-family: 'JetBrains Mono', monospace;
}

.price-symbol { font-size: 1rem; font-weight: 600; }
.price-value { font-size: 1.5rem; font-weight: 700; margin: 0 2px; }
.price-unit { font-size: 0.85rem; color: #6B7280; }

.action-btn {
  background: white;
  border: 1px solid #111827;
  color: #111827;
  padding: 8px 16px;
  border-radius: 0;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.85rem;
  text-transform: uppercase;
}

.action-btn:hover {
  background: #111827;
  color: white;
}

.action-btn:disabled {
  border-color: #E5E7EB;
  color: #9CA3AF;
  cursor: not-allowed;
  background: white;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #9CA3AF;
  border: 1px dashed #E5E7EB;
  margin-top: 40px;
}

.loading-section {
  padding: 80px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #6B7280;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 2px solid #E5E7EB;
  border-top-color: #111827;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 1024px) {
  .house-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .house-grid { grid-template-columns: 1fr; }
}
</style>
