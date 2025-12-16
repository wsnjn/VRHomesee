<template>
  <div class="house-management">
    <!-- 工具栏 -->
    <div class="toolbar">
      <!-- 左侧：租赁类型选择器 -->
      <div class="rental-type-filter">
        <select v-model="filterRentalType" @change="loadHouses">
          <option value="">全部类型</option>
          <option value="0">整租</option>
          <option value="1">合租</option>
          <option value="2">单间</option>
        </select>
      </div>
      <!-- 右侧：状态标签 + 搜索框 + 按钮 -->
      <div class="toolbar-right">
        <div class="status-tabs">
          <button 
            class="filter-tab" 
            :class="{ active: filterStatus === '' }"
            @click="filterStatus = ''; loadHouses()"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/>
            </svg>
            <span>全部</span>
            <span class="count">{{ statistics.total || 0 }}</span>
          </button>
          <button 
            class="filter-tab" 
            :class="{ active: filterStatus === '0' }"
            @click="filterStatus = '0'; loadHouses()"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
            </svg>
            <span>可租</span>
            <span class="count">{{ statistics.available || 0 }}</span>
          </button>
          <button 
            class="filter-tab" 
            :class="{ active: filterStatus === '1' }"
            @click="filterStatus = '1'; loadHouses()"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
            <span>已租</span>
            <span class="count">{{ statistics.rented || 0 }}</span>
          </button>
          <button 
            class="filter-tab" 
            :class="{ active: filterStatus === '2' }"
            @click="filterStatus = '2'; loadHouses()"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
            </svg>
            <span>下架</span>
            <span class="count">{{ statistics.maintenance || 0 }}</span>
          </button>
          <button 
            class="filter-tab" 
            :class="{ active: filterStatus === '3' }"
            @click="filterStatus = '3'; loadHouses()"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
            </svg>
            <span>预租</span>
            <span class="count">{{ statistics.reserved || 0 }}</span>
          </button>
        </div>
        <div class="search-box">
          <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="M21 21l-4.35-4.35"></path>
          </svg>
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索房屋..."
            @input="onSearchInput"
          />
        </div>
        <button @click="refreshData" class="toolbar-refresh-btn">刷新</button>
        <button @click="showAddDialog = true" class="toolbar-add-btn">添加房屋</button>
      </div>
    </div>

    <!-- 房屋列表 -->
    <div class="house-list">
      <div class="house-row" v-for="house in houses" :key="house.id">
        <!-- 左侧：地址盒子 -->
        <div class="row-address">
          <div class="addr-main">{{ house.province }} {{ house.city }} {{ house.district }} {{ house.street }} {{ house.communityName }} {{ house.doorNumber }}</div>
          <div class="addr-sub">{{ house.communityName }} · 公寓 · {{ house.roomNumber || '' }}</div>
        </div>
        <!-- 中间：房屋信息 -->
        <div class="row-info">
          <span class="info-label">面积:</span><span class="info-val">{{ house.roomArea || '-' }}㎡</span>
          <span class="info-label">楼层:</span><span class="info-val">{{ house.floorInfo || '-' }}</span>
          <span class="info-label">朝向:</span><span class="info-val">{{ house.orientation || '-' }}</span>
          <span class="info-label">装修:</span><span class="info-val">{{ getDecorationText(house.decoration) }}</span>
          <span class="info-label">电梯:</span><span class="info-val">{{ house.hasElevator ? '有' : '无' }}</span>
        </div>
        <!-- 价格 -->
        <div class="row-price">
          <div class="price-main">{{ house.rentPrice }}元/月</div>
          <div class="price-sub">水费:{{ house.waterPrice || '-' }}元/吨</div>
          <div class="price-sub">电费:{{ house.electricPrice || '-' }}元/度</div>
        </div>
        <!-- 状态 -->
        <div class="row-status">
          <span class="status-tag" :class="getStatusClass(house.status)">{{ getStatusText(house.status) }}</span>
        </div>
        <!-- 操作按钮 -->
        <div class="row-actions">
          <button @click="editHouse(house)">编辑</button>
          <button @click="updateHouseStatus(house.id, house.status === 0 ? 1 : 0)">设为可租</button>
          <button @click="viewHouseDetail(house)">详情</button>
          <button @click="showVrDialog(house)">VR</button>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">加载中...</div>
      
      <!-- 空状态 -->
      <div v-if="!loading && houses.length === 0" class="empty-state">暂无房屋数据</div>
    </div>

    <!-- 添加/编辑房屋对话框 -->
    <div v-if="showAddDialog || showEditDialog" class="dialog-overlay">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ showAddDialog ? '添加房屋' : '编辑房屋' }}</h3>
          <button @click="closeDialog" class="close-btn">×</button>
        </div>
        <div class="dialog-content">
          <form @submit.prevent="submitHouseForm">
            <div class="form-grid">
              <div class="form-group">
                <label>省份 *</label>
                <input v-model="houseForm.province" type="text" required />
              </div>
              <div class="form-group">
                <label>城市 *</label>
                <input v-model="houseForm.city" type="text" required />
              </div>
              <div class="form-group">
                <label>区县 *</label>
                <input v-model="houseForm.district" type="text" required />
              </div>
              <div class="form-group">
                <label>街道 *</label>
                <input v-model="houseForm.street" type="text" required />
              </div>
              <div class="form-group">
                <label>小区名称 *</label>
                <input v-model="houseForm.communityName" type="text" required />
              </div>
              <div class="form-group">
                <label>单元号</label>
                <input v-model="houseForm.buildingUnit" type="text" />
              </div>
              <div class="form-group">
                <label>门牌号 *</label>
                <input v-model="houseForm.doorNumber" type="text" required />
              </div>
              <div class="form-group">
                <label>房间号</label>
                <input v-model="houseForm.roomNumber" type="text" />
              </div>
              <div class="form-group">
                <label>是否有电梯 *</label>
                <select v-model="houseForm.hasElevator" required>
                  <option :value="false">无</option>
                  <option :value="true">有</option>
                </select>
              </div>
              <div class="form-group">
                <label>房间面积(㎡)</label>
                <input v-model="houseForm.roomArea" type="number" step="0.01" />
              </div>
              <div class="form-group">
                <label>楼层信息</label>
                <input v-model="houseForm.floorInfo" type="text" placeholder="如：5/18" />
              </div>
              <div class="form-group">
                <label>朝向</label>
                <input v-model="houseForm.orientation" type="text" />
              </div>
              <div class="form-group">
                <label>装修程度</label>
                <select v-model="houseForm.decoration">
                  <option value="">请选择</option>
                  <option value="1">毛坯</option>
                  <option value="2">简装</option>
                  <option value="3">精装</option>
                  <option value="4">豪装</option>
                </select>
              </div>
              <div class="form-group">
                <label>月租金(元) *</label>
                <input v-model="houseForm.rentPrice" type="number" step="0.01" required />
              </div>
              <div class="form-group">
                <label>水费单价(元/吨)</label>
                <input v-model="houseForm.waterPrice" type="number" step="0.01" />
              </div>
              <div class="form-group">
                <label>电费单价(元/度)</label>
                <input v-model="houseForm.electricPrice" type="number" step="0.01" />
              </div>
              <div class="form-group">
                <label>租赁类型 *</label>
                <select v-model="houseForm.rentalType" required>
                  <option value="0">整租</option>
                  <option value="1">合租</option>
                  <option value="2">单间</option>
                </select>
              </div>
              <div class="form-group">
                <label>房东电话 *</label>
                <input v-model="houseForm.landlordPhone" type="text" required />
              </div>
              <div class="form-group">
                <label>房源状态 *</label>
                <select v-model="houseForm.status" required>
                  <option value="0">可租</option>
                  <option value="1">已租</option>
                  <option value="2">下架</option>
                  <option value="3">预租</option>
                </select>
              </div>
              <div class="form-group full-width">
                <label>房源描述</label>
                <textarea v-model="houseForm.description" rows="3"></textarea>
              </div>
            </div>
            <div class="form-actions">
              <button type="button" @click="closeDialog" class="cancel-btn">取消</button>
              <button type="submit" class="submit-btn">
                {{ showAddDialog ? '添加' : '更新' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- VR设置对话框 -->
    <div v-if="showVrDialogVisible" class="dialog-overlay">
      <div class="dialog vr-dialog">
        <div class="dialog-header">
          <h3>VR全景图管理 - {{ currentHouse?.communityName }}</h3>
          <button @click="closeVrDialog" class="close-btn">×</button>
        </div>
        <div class="dialog-content">
           <!-- 现有场景列表 -->
          <div class="scene-list-manage">
            <h4>现有场景</h4>
            <div v-if="vrScenes.length === 0" class="no-scenes">暂无VR场景</div>
            <div v-else class="scenes-grid">
              <div v-for="scene in vrScenes" :key="scene.id" class="scene-card">
                <div class="scene-preview">
                  <!-- Display image directly? Note: src/assets might not work dynamically without import. 
                       But we are in dev mode, let's try using the path directly or a computed property.
                       Actually, for dynamic assets in Vite, we might need a different approach.
                       But let's assume the backend returns a path we can use or we fix it later. -->
                  <img :src="scene.imageUrl" :alt="scene.sceneName">
                </div>
                <div class="scene-info">
                  <span>{{ scene.sceneName }}</span>
                  <button @click="deleteVrScene(scene.id)" class="delete-icon">🗑️</button>
                </div>
              </div>
            </div>
            </div>
          <!-- 上传新场景 -->
          <div class="upload-section">
            <h4>上传新场景</h4>
            <div class="upload-form">
              <input v-model="newSceneName" type="text" placeholder="场景名称 (如: 客厅)" class="scene-name-input" />
              <input type="file" @change="handleFileSelect" accept="image/*" class="file-input" />
              <button @click="uploadVrScene" :disabled="!newSceneName || !selectedFile || uploading" class="upload-btn">
                {{ uploading ? '上传中...' : '上传' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'

const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const loading = ref(false)
const houses = ref([])
const statistics = ref({})
const filterStatus = ref('')
const filterRentalType = ref('')
const searchKeyword = ref('')
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const showVrDialogVisible = ref(false)
const vrScenes = ref([])
const currentHouse = ref(null)
const newSceneName = ref('')
const selectedFile = ref(null)
const uploading = ref(false)

// 表单数据
const houseForm = reactive({
  province: '',
  city: '',
  district: '',
  street: '',
  communityName: '',
  buildingUnit: '',
  doorNumber: '',
  roomNumber: '',
  hasElevator: false,
  roomArea: null,
  floorInfo: '',
  orientation: '',
  decoration: '',
  rentPrice: null,
  waterPrice: null,
  electricPrice: null,
  rentalType: 0,
  landlordPhone: '',
  status: 0,
  description: ''
})


let editingHouseId = null

// 加载房屋数据
const loadHouses = async () => {
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/rooms/all`)
    // 同时获取租约和预约数据用于筛选
    const contractsRes = await axios.get(`${API_BASE_URL}/admin/tenant/all`)
    const appointmentsRes = await axios.get(`${API_BASE_URL}/viewing-appointment/all`)
    
    if (response.data.success) {
      let filteredHouses = response.data.rooms || []
      const allContracts = contractsRes.data?.contracts || []
      const allAppointments = appointmentsRes.data?.appointments || []
      
      // 已租房屋ID集合（有活跃租约）
      const rentedRoomIds = new Set(
        allContracts
          .filter(c => c.contractStatus === 1 || c.contractStatus === 2)
          .map(c => c.roomId)
      )
      
      // 预租房屋ID集合（有待确认或已确认预约）
      const preRentedRoomIds = new Set(
        allAppointments
          .filter(a => a.status === 0 || a.status === 1)
          .map(a => a.roomId)
      )
      
      // 应用状态筛选
      if (filterStatus.value === '0') {
        // 可租：房屋状态为0
        filteredHouses = filteredHouses.filter(house => house.status === 0)
      } else if (filterStatus.value === '1') {
        // 已租：根据租约数据筛选
        filteredHouses = filteredHouses.filter(house => rentedRoomIds.has(house.id))
      } else if (filterStatus.value === '2') {
        // 下架：房屋状态为2
        filteredHouses = filteredHouses.filter(house => house.status === 2)
      } else if (filterStatus.value === '3') {
        // 预租：根据预约数据筛选
        filteredHouses = filteredHouses.filter(house => preRentedRoomIds.has(house.id))
      }
      // filterStatus === '' 表示全部，不筛选
      
      // 应用租赁类型筛选
      if (filterRentalType.value !== '') {
        filteredHouses = filteredHouses.filter(house => house.rentalType === parseInt(filterRentalType.value))
      }
      
      // 应用搜索筛选
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        filteredHouses = filteredHouses.filter(house => 
          house.communityName.toLowerCase().includes(keyword) ||
          house.province.toLowerCase().includes(keyword) ||
          house.city.toLowerCase().includes(keyword) ||
          house.district.toLowerCase().includes(keyword) ||
          house.street.toLowerCase().includes(keyword)
        )
      }
      
      houses.value = filteredHouses
    }
  } catch (error) {
    console.error('加载房屋数据失败:', error)
    houses.value = []
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    // 获取所有房屋
    const allHousesRes = await axios.get(`${API_BASE_URL}/rooms/all`)
    // 获取所有租约数据来计算"已租"
    const contractsRes = await axios.get(`${API_BASE_URL}/admin/tenant/all`)
    // 获取所有预约数据来计算"预租"
    const appointmentsRes = await axios.get(`${API_BASE_URL}/viewing-appointment/all`)
    
    if (allHousesRes.data.success) {
      const allHouses = allHousesRes.data.rooms || []
      const allContracts = contractsRes.data?.contracts || []
      const allAppointments = appointmentsRes.data?.appointments || []
      
      // 已租：有活跃租约的房屋ID（合同状态为已签约=1或履行中=2）
      const rentedRoomIds = new Set(
        allContracts
          .filter(c => c.contractStatus === 1 || c.contractStatus === 2)
          .map(c => c.roomId)
      )
      
      // 预租：有待确认=0或已确认=1预约的房屋ID
      const preRentedRoomIds = new Set(
        allAppointments
          .filter(a => a.status === 0 || a.status === 1)
          .map(a => a.roomId)
      )
      
      // 统计（类别可以重叠，不必加起来等于全部）
      statistics.value.total = allHouses.length
      // 可租：状态为0的房屋
      statistics.value.available = allHouses.filter(h => h.status === 0).length
      // 已租：根据活跃租约数据统计
      statistics.value.rented = rentedRoomIds.size
      // 下架：状态为2的房屋
      statistics.value.maintenance = allHouses.filter(h => h.status === 2).length
      // 预租：有待确认或已确认预约的房屋数量
      statistics.value.reserved = preRentedRoomIds.size
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 搜索输入处理
const onSearchInput = () => {
  // 防抖处理，避免频繁请求
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadHouses()
  }, 500)
}

let searchTimer = null

// 刷新数据
const refreshData = () => {
  loadHouses()
  loadStatistics()
}

// 添加房屋
const addHouse = async () => {
  try {
    const response = await axios.post(`${API_BASE_URL}/rooms/add`, houseForm)
    if (response.data.success) {
      alert('房屋添加成功')
      closeDialog()
      refreshData()
    } else {
      alert('添加失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('添加房屋失败:', error)
    alert('添加失败: ' + error.message)
  }
}

// 编辑房屋
const editHouse = (house) => {
  Object.assign(houseForm, house)
  editingHouseId = house.id
  showEditDialog.value = true
}

// 更新房屋
const updateHouse = async () => {
  try {
    const response = await axios.put(`${API_BASE_URL}/rooms/update/${editingHouseId}`, houseForm)
    if (response.data.success) {
      alert('房屋信息更新成功')
      closeDialog()
      refreshData()
    } else {
      alert('更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新房屋失败:', error)
    alert('更新失败: ' + error.message)
  }
}

// 删除房屋
const deleteHouse = async (id) => {
  if (confirm('确定要删除这个房屋吗？此操作不可恢复。')) {
    try {
      const response = await axios.delete(`${API_BASE_URL}/rooms/delete/${id}`)
      if (response.data.success) {
        alert('房屋删除成功')
        refreshData()
      } else {
        alert('删除失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('删除房屋失败:', error)
      alert('删除失败: ' + error.message)
    }
  }
}

// 更新房屋状态
const updateHouseStatus = async (id, newStatus) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/rooms/${id}/status`, { status: newStatus })
    if (response.data.success) {
      alert('房屋状态更新成功')
      refreshData()
    } else {
      alert('状态更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新房屋状态失败:', error)
    alert('状态更新失败: ' + error.message)
  }
}

// VR Dialog Logic
const showVrDialog = async (house) => {
  currentHouse.value = house
  showVrDialogVisible.value = true
  await loadVrScenes(house.id)
}


const closeVrDialog = () => {
  showVrDialogVisible.value = false
  currentHouse.value = null
  vrScenes.value = []
  newSceneName.value = ''
  selectedFile.value = null
}
const loadVrScenes = async (roomId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/vr-scenes/${roomId}`)
    if (response.data.success) {
       vrScenes.value = response.data.data
    }
  } catch (error) {
    console.error('Load scenes failed:', error)
  }
}
const handleFileSelect = (event) => {
  selectedFile.value = event.target.files[0]
}
const uploadVrScene = async () => {
  if (!currentHouse.value || !newSceneName.value || !selectedFile.value) return
  
  uploading.value = true
  const formData = new FormData()
  formData.append('roomId', currentHouse.value.id)
  formData.append('sceneName', newSceneName.value)
  formData.append('file', selectedFile.value)
  try {
    const response = await axios.post(`${API_BASE_URL}/vr-scenes/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.success) {
      alert('上传成功')
      newSceneName.value = ''
      selectedFile.value = null
      // Clear file input
      const fileInput = document.querySelector('.file-input')
      if (fileInput) fileInput.value = ''
      
      await loadVrScenes(currentHouse.value.id)
    } else {
       alert('上传失败: ' + response.data.message)
    }
  } catch (error) {
     console.error('Upload failed:', error)
    alert('上传失败')
  } finally {
    uploading.value = false
  }
}

const deleteVrScene = async (id) => {
  if (!confirm('确定删除该场景吗？')) return
  
  try {
    const response = await axios.delete(`${API_BASE_URL}/vr-scenes/${id}`)
    if (response.data.success) {
      await loadVrScenes(currentHouse.value.id)
    } else {
      alert('删除失败')
    }
  } catch (error) {
    console.error('Delete failed:', error)
  }
}


// 关闭对话框
const closeDialog = () => {
  showAddDialog.value = false
  showEditDialog.value = false
  resetForm()
}


// 重置表单
const resetForm = () => {
  Object.assign(houseForm, {
    province: '',
    city: '',
    district: '',
    street: '',
    communityName: '',
    buildingUnit: '',
    doorNumber: '',
    roomNumber: '',
    hasElevator: false,
    roomArea: null,
    floorInfo: '',
    orientation: '',
    decoration: '',
    rentPrice: null,
    waterPrice: null,
    electricPrice: null,
    rentalType: 0,
    landlordPhone: '',
    status: 0,
    description: ''
  })
  editingHouseId = null
}

// 提交房屋表单
const submitHouseForm = () => {
  if (showAddDialog.value) {
    addHouse()
  } else if (showEditDialog.value) {
    updateHouse()
  }
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
    2: 'status-maintenance',
    3: 'status-reserved'
  }
  return classMap[status] || 'status-unknown'
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

// 获取装修程度文本
const getDecorationText = (decoration) => {
  const decorationMap = {
    1: '毛坯',
    2: '简装',
    3: '精装',
    4: '豪装'
  }
  return decorationMap[decoration] || '未知'
}

// 页面加载时初始化数据
onMounted(() => {
  loadHouses()
  loadStatistics()
})
</script>

<style scoped>
.house-management {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e9ecef;
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8rem;
}

.header-actions {
  display: flex;
  gap: 12px;
}


/* 工具栏样式 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #1e3a5f;
  border-radius: 4px;
  margin-bottom: 20px;
}

.rental-type-filter select {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
  color: #333;
  cursor: pointer;
}

.rental-type-filter select:focus {
  outline: none;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-tabs {
  display: flex;
  gap: 0;
  background: #fff;
  border-radius: 4px;
  padding: 2px;
}

.filter-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
  line-height: 1;
}

.filter-tab:hover {
  background-color: #f0f0f0;
  color: #333;
}

.filter-tab.active {
  background-color: #1e3a5f;
  color: #fff;
}

.filter-tab svg {
  display: block;
  flex-shrink: 0;
}

.filter-tab span {
  display: inline-block;
  vertical-align: middle;
}

.filter-tab .count {
  font-weight: 600;
  color: inherit;
}

.search-box {
  position: relative;
  width: 270px;
  flex-shrink: 0;
}

.search-box input {
  width: 100%;
  padding: 8px 12px 8px 36px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
  color: #333;
  box-sizing: border-box;
}

.search-box input::placeholder {
  color: #999;
}

.search-box input:focus {
  outline: none;
}

.search-box .search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  pointer-events: none;
}

.toolbar .refresh-btn {
  padding: 8px 16px;
  background: #4a7ab0;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
  white-space: nowrap;
}

.toolbar .refresh-btn:hover {
  background: #3a6a9f;
}

.toolbar .add-btn {
  padding: 8px 16px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
  white-space: nowrap;
}

.toolbar .add-btn:hover {
  background: #218838;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: transform 0.2s;
  border: 1px solid #e9ecef;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-content h3 {
  margin: 0;
  font-size: 1.8rem;
  color: #2c3e50;
}

.stat-content p {
  margin: 5px 0 0 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.filter-section {
  background: #1e3a5f;
  padding: 15px 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 15px;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-weight: 500;
  color: #fff;
  white-space: nowrap;
}

.filter-item select {
  padding: 8px 12px;
  border: 1px solid #3a5a7f;
  border-radius: 4px;
  font-size: 14px;
  background: #2a4a6f;
  color: #fff;
}

.filter-item select:focus {
  outline: none;
  border-color: #4a9eff;
}

.search-item {
  flex: 1;
  position: relative;
}

.search-item input {
  width: 100%;
  padding: 8px 12px 8px 36px;
  border: 1px solid #3a5a7f;
  border-radius: 4px;
  font-size: 14px;
  background: #2a4a6f;
  color: #fff;
}

.search-item input::placeholder {
  color: #8a9aaf;
}

.search-item input:focus {
  outline: none;
  border-color: #4a9eff;
}

.search-item .search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #8a9aaf;
}

.header-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* 工具栏按钮样式 - 按个人风格指南 */
.toolbar-refresh-btn,
.toolbar-add-btn {
  padding: 4px 12px;
  background: transparent;
  color: #fff;
  border: 1px solid rgba(255,255,255,0.4);
  cursor: pointer;
  font-size: 12px;
  font-weight: 400;
}

.toolbar-refresh-btn:hover,
.toolbar-add-btn:hover {
  background: rgba(255,255,255,0.1);
}

.house-list {
  background: #fff;
  border: 1px solid #ddd;
}

.house-row {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid #e5e5e5;
  font-size: 13px;
}

.house-row:hover {
  background: #f9f9f9;
}

.row-address {
  flex: 0 0 280px;
  padding-right: 16px;
  border-right: 1px solid #e5e5e5;
}

.addr-main {
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.addr-sub {
  font-size: 12px;
  color: #888;
}

.row-info {
  flex: 1;
  padding: 0 16px;
  color: #666;
}

.info-label {
  color: #888;
  margin-right: 2px;
}

.info-val {
  color: #333;
  margin-right: 16px;
}

.row-price {
  flex: 0 0 120px;
  padding: 0 16px;
  text-align: right;
}

.price-main {
  font-weight: 600;
  color: #c00;
  font-size: 14px;
}

.price-sub {
  font-size: 11px;
  color: #888;
}

.row-status {
  flex: 0 0 60px;
  padding: 0 16px;
  text-align: center;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  background: #1e3a5f;
  color: #fff;
}

.row-actions {
  flex: 0 0 auto;
  display: flex;
  gap: 4px;
}

.row-actions button {
  padding: 4px 8px;
  border: 1px solid #ddd;
  background: #fff;
  color: #333;
  font-size: 12px;
  cursor: pointer;
}

.row-actions button:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

.loading,
.empty-state {
  padding: 24px;
  text-align: center;
  color: #888;
}
.vr-dialog {
  width: 600px;
  max-width: 90%;
}
.scene-list-manage {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}
.scenes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
  margin-top: 10px;
}
.scene-card {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 5px;
  background: #f9f9f9;
}
.scene-preview {
  height: 80px;
  background: #eee;
  margin-bottom: 5px;
  border-radius: 4px;
  overflow: hidden;
}
.scene-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.scene-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.delete-icon {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px;
}
.upload-form {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
}
.scene-name-input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 150px;
}
.upload-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
.upload-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.address-detail,
.room-detail {
  font-size: 0.85rem;
  color: #6c757d;
}

.house-details div,
.price-info div,
.rental-info div {
  margin-bottom: 4px;
  font-size: 0.9rem;
}

.rent-price {
  font-weight: bold;
  color: #e74c3c;
  font-size: 1.1rem;
}

.utility-price {
  font-size: 0.8rem;
  color: #6c757d;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 2px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-available {
  background: #d4edda;
  color: #155724;
}

.status-rented {
  background: #d1ecf1;
  color: #0c5460;
}

.status-maintenance {
  background: #fff3cd;
  color: #856404;
}

.status-reserved {
  background: #e2e3e5;
  color: #383d41;
}

.status-unknown {
  background: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.action-buttons button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.3s;
}

.edit-btn {
  background: #17a2b8;
  color: white;
}

.edit-btn:hover {
  background: #138496;
}

.status-btn {
  color: white;
}

.rent-btn {
  background: #28a745;
}

.rent-btn:hover {
  background: #218838;
}

.available-btn {
  background: #007bff;
}

.available-btn:hover {
  background: #0056b3;
}

.price-btn {
  background: #ffc107;
  color: #212529;
}

.price-btn:hover {
  background: #e0a800;
}

.delete-btn {
  background: #dc3545;
  color: white;
}

.delete-btn:hover {
  background: #c82333;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px;
  color: #6c757d;
}

/* 对话框样式 */
.dialog-overlay {
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
}

.dialog {
  background: #fff;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid #ddd;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ddd;
  background: #f9f9f9;
}

.dialog-header h3 {
  margin: 0;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.close-btn {
  background: transparent;
  border: 1px solid #ddd;
  width: 24px;
  height: 24px;
  font-size: 14px;
  cursor: pointer;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.dialog-content {
  padding: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 4px;
  font-weight: 500;
  color: #333;
  font-size: 12px;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 8px;
  border: 1px solid #ddd;
  font-size: 13px;
  background: #fff;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3A6EA5;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #ddd;
}

.cancel-btn,
.submit-btn {
  padding: 4px 16px;
  border: 1px solid #ddd;
  cursor: pointer;
  font-weight: 400;
  font-size: 12px;
  background: transparent;
  color: #333;
}

.cancel-btn:hover {
  background: #f5f5f5;
}

.submit-btn {
  background: #1e3a5f;
  color: #fff;
  border-color: #1e3a5f;
}

.submit-btn:hover {
  background: #2d5a87;
  border-color: #2d5a87;
}

.price-form {
  max-width: 400px;
  margin: 0 auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .filter-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .stats-cards {
    grid-template-columns: 1fr 1fr;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: row;
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .house-management {
    padding: 10px;
  }
}
</style>
