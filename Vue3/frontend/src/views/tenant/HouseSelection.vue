<template>
  <div class="house-selection">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-brand">
        <h2>房屋选择</h2>
      </div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">返回首页</router-link>
      </div>
    </nav>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item">
          <label for="province">省份：</label>
          <select id="province" v-model="selectedProvince" @change="onProvinceChange">
            <option value="">请选择省份</option>
            <option v-for="province in provinces" :key="province" :value="province">
              {{ province }}
            </option>
          </select>
        </div>

        <div class="filter-item">
          <label for="city">城市：</label>
          <select id="city" v-model="selectedCity" @change="onCityChange">
            <option value="">请选择城市</option>
            <option v-for="city in cities" :key="city" :value="city">
              {{ city }}
            </option>
          </select>
        </div>

        <div class="filter-item">
          <label for="district">区县：</label>
          <select id="district" v-model="selectedDistrict" @change="onDistrictChange">
            <option value="">请选择区县</option>
            <option v-for="district in districts" :key="district" :value="district">
              {{ district }}
            </option>
          </select>
        </div>

        <div class="filter-item">
          <label for="street">街道：</label>
          <select id="street" v-model="selectedStreet" @change="onStreetChange">
            <option value="">请选择街道</option>
            <option v-for="street in streets" :key="street" :value="street">
              {{ street }}
            </option>
          </select>
        </div>

        <div class="filter-item">
          <label for="community">小区：</label>
          <select id="community" v-model="selectedCommunity" @change="onCommunityChange">
            <option value="">请选择小区</option>
            <option v-for="community in communities" :key="community" :value="community">
              {{ community }}
            </option>
          </select>
        </div>
      </div>

      <div class="filter-row">
        <div class="filter-item">
          <label for="rentalType">租赁类型：</label>
          <select id="rentalType" v-model="selectedRentalType">
            <option value="">全部类型</option>
            <option v-for="(type, key) in rentalTypes" :key="key" :value="key">
              {{ type }}
            </option>
          </select>
        </div>

        <div class="filter-item">
          <label for="minPrice">最低租金：</label>
          <input 
            id="minPrice" 
            type="number" 
            v-model="minPrice" 
            placeholder="0" 
            min="0"
          />
        </div>

        <div class="filter-item">
          <label for="maxPrice">最高租金：</label>
          <input 
            id="maxPrice" 
            type="number" 
            v-model="maxPrice" 
            placeholder="不限" 
            min="0"
          />
        </div>

        <div class="filter-item">
          <label for="minArea">最小面积：</label>
          <input 
            id="minArea" 
            type="number" 
            v-model="minArea" 
            placeholder="0" 
            min="0"
          />
        </div>

        <div class="filter-item">
          <label for="maxArea">最大面积：</label>
          <input 
            id="maxArea" 
            type="number" 
            v-model="maxArea" 
            placeholder="不限" 
            min="0"
          />
        </div>
      </div>

      <div class="filter-row">
        <div class="filter-item">
          <label for="decoration">装修程度：</label>
          <select id="decoration" v-model="selectedDecoration">
            <option value="">全部</option>
            <option v-for="(decoration, key) in decorationTypes" :key="key" :value="key">
              {{ decoration }}
            </option>
          </select>
        </div>

        <div class="filter-item">
          <label for="elevator">电梯：</label>
          <select id="elevator" v-model="selectedElevator">
            <option value="">全部</option>
            <option v-for="(elevator, key) in elevatorOptions" :key="key" :value="key">
              {{ elevator }}
            </option>
          </select>
        </div>

        <div class="filter-item">
          <label for="orientation">朝向：</label>
          <select id="orientation" v-model="selectedOrientation">
            <option value="">全部</option>
            <option v-for="(orientation, key) in orientationOptions" :key="key" :value="key">
              {{ orientation }}
            </option>
          </select>
        </div>
      </div>

      <div class="filter-actions">
        <button @click="resetFilters" class="reset-btn">重置筛选</button>
        <button @click="applyFilters" class="apply-btn">查看房屋</button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-section">
      <p>正在加载数据...</p>
    </div>

    <!-- 房屋列表 -->
    <div v-else class="house-list-section">
      <h3>可选房屋列表 ({{ houses.length }} 套)</h3>
      <div class="house-grid">
        <div 
          v-for="house in houses" 
          :key="house.id"
          class="house-card"
          @click="selectHouse(house)"
        >
          <div class="house-image">
            <img :src="getHouseImage(house)" :alt="house.communityName" />
          </div>
            <div class="house-info">
            <div class="house-header">
              <h4>{{ getHouseTitle(house) }}</h4>
              <span class="house-status" :class="getStatusClass(house.status)">
                {{ getStatusText(house.status) }}
              </span>
            </div>
            <p class="house-address">{{ getHouseAddress(house) }}</p>
            <p class="house-details">
              {{ house.roomArea }}㎡ · {{ getLayoutType(house.rentalType) }} · {{ house.floorInfo }} · {{ getDecorationText(house.decoration) }}
            </p>
            <p class="house-price">{{ house.rentPrice }}元/月</p>
            <div class="house-actions">
              <button @click.stop="viewHouseTour(house)" class="tour-btn">VR看房</button>
              <button 
                @click.stop="makeAppointment(house)" 
                class="appointment-btn"
                :disabled="house.status !== 0"
                :class="{ 'disabled': house.status !== 0 }"
              >
                {{ house.status === 0 ? '预约看房' : '已下架' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

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
      maxArea: maxArea.value ? parseFloat(maxArea.value) : undefined
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
  loadHouses()
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
}

// 应用筛选（与自动筛选重复，但保留按钮）
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
  return '/src/assets/客厅.jpg'
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

// 页面加载时初始化数据
onMounted(async () => {
  await loadFilterOptions()
  await loadHouses()
})
</script>

<style scoped>
.house-selection {
  min-height: 100vh;
  background-color: #f5f5f5;
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
  background-color: rgba(44, 62, 80, 0.9);
  color: white;
  z-index: 100;
  backdrop-filter: blur(10px);
}

.nav-brand h2 {
  margin: 0;
  color: white;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
  cursor: pointer;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.filter-section {
  background: white;
  margin: 20px;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 24px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  min-width: 180px;
  flex: 1;
}

.filter-item label {
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.filter-item select,
.filter-item input {
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.filter-item select:focus,
.filter-item input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
  background-color: #f8f9fa;
}

.filter-item select:hover,
.filter-item input:hover {
  border-color: #adb5bd;
}

.filter-item select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=US-ASCII,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 4 5'><path fill='%23666' d='M2 0L0 2h4zm0 5L0 3h4z'/></svg>");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 12px;
  padding-right: 40px;
}

.filter-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.reset-btn, .apply-btn {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reset-btn {
  background: linear-gradient(135deg, #6c757d 0%, #5a6268 100%);
  color: white;
  border: 2px solid #6c757d;
}

.reset-btn:hover {
  background: linear-gradient(135deg, #5a6268 0%, #495057 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
}

.reset-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(108, 117, 125, 0.3);
}

.apply-btn {
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
  border: 2px solid #007bff;
}

.apply-btn:hover {
  background: linear-gradient(135deg, #0056b3 0%, #004085 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.apply-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 123, 255, 0.3);
}

/* 按钮点击效果 */
.reset-btn::after, .apply-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.3s, height 0.3s;
}

.reset-btn:active::after, .apply-btn:active::after {
  width: 100px;
  height: 100px;
}

/* 按钮禁用状态 */
.reset-btn:disabled, .apply-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.reset-btn:disabled:hover, .apply-btn:disabled:hover {
  transform: none;
  box-shadow: none;
}

.house-list-section {
  background: white;
  margin: 20px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.house-list-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 1.5rem;
}

.house-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.house-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  background: white;
}

.house-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.house-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.house-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.house-info {
  padding: 15px;
}

.house-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.house-header h4 {
  margin: 0;
  color: #333;
  font-size: 1.1rem;
  flex: 1;
  margin-right: 10px;
}

.house-status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-available {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-unavailable {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.status-unknown {
  background-color: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

.house-address {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 8px 0;
}

.house-details {
  color: #888;
  font-size: 0.85rem;
  margin: 0 0 8px 0;
}

.house-price {
  color: #e74c3c;
  font-size: 1.1rem;
  font-weight: bold;
  margin: 0 0 12px 0;
}

.house-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.tour-btn, .appointment-btn {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.tour-btn {
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%);
  color: white;
  border: 1px solid #17a2b8;
}

.tour-btn:hover {
  background: linear-gradient(135deg, #138496 0%, #117a8b 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(23, 162, 184, 0.3);
}

.appointment-btn {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: 1px solid #28a745;
}

.appointment-btn:hover {
  background: linear-gradient(135deg, #20c997 0%, #1ba87e 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.3);
}

.tour-btn:active, .appointment-btn:active {
  transform: translateY(0);
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .filter-item {
    min-width: auto;
  }
  
  .house-grid {
    grid-template-columns: 1fr;
  }
  
  .navbar {
    padding: 1rem;
  }
}
</style>
