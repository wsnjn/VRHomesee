<template>
  <div class="house-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>房屋管理</h2>
      <div class="header-actions">
        <button @click="showAddDialog = true" class="add-btn">
          <span>+</span> 添加房屋
        </button>
        <button @click="refreshData" class="refresh-btn">
          🔄 刷新
        </button>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.total || 0 }}</h3>
          <p>总房屋数</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.available || 0 }}</h3>
          <p>可租房屋</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.rented || 0 }}</h3>
          <p>已租房屋</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.maintenance || 0 }}</h3>
          <p>维护中</p>
        </div>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item">
          <label>状态筛选：</label>
          <select v-model="filterStatus" @change="loadHouses">
            <option value="">全部状态</option>
            <option value="0">可租</option>
            <option value="1">已租</option>
            <option value="2">下架</option>
            <option value="3">预租</option>
          </select>
        </div>
        <div class="filter-item">
          <label>租赁类型：</label>
          <select v-model="filterRentalType" @change="loadHouses">
            <option value="">全部类型</option>
            <option value="0">整租</option>
            <option value="1">合租</option>
            <option value="2">单间</option>
          </select>
        </div>
        <div class="filter-item">
          <label>搜索：</label>
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="输入小区名、地址等关键词"
            @input="onSearchInput"
          />
        </div>
      </div>
    </div>

    <!-- 房屋列表 -->
    <div class="house-list">
      <div class="table-container">
        <table class="house-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>地址信息</th>
              <th>房屋信息</th>
              <th>价格信息</th>
              <th>租赁信息</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="house in houses" :key="house.id">
              <td>{{ house.id }}</td>
              <td>
                <div class="address-info">
                  <strong>{{ house.communityName }}</strong>
                  <div class="address-detail">
                    {{ house.province }}{{ house.city }}{{ house.district }}{{ house.street }}
                  </div>
                  <div class="room-detail">
                    {{ house.buildingUnit || '' }}{{ house.doorNumber }}{{ house.roomNumber ? house.roomNumber + '室' : '' }}
                  </div>
                </div>
              </td>
              <td>
                <div class="house-details">
                  <div v-if="house.roomArea">面积: {{ house.roomArea }}㎡</div>
                  <div v-if="house.floorInfo">楼层: {{ house.floorInfo }}</div>
                  <div v-if="house.orientation">朝向: {{ house.orientation }}</div>
                  <div v-if="house.decoration">装修: {{ getDecorationText(house.decoration) }}</div>
                  <div>电梯: {{ house.hasElevator ? '有' : '无' }}</div>
                </div>
              </td>
              <td>
                <div class="price-info">
                  <div class="rent-price">{{ house.rentPrice }}元/月</div>
                  <div v-if="house.waterPrice" class="utility-price">水费: {{ house.waterPrice }}元/吨</div>
                  <div v-if="house.electricPrice" class="utility-price">电费: {{ house.electricPrice }}元/度</div>
                </div>
              </td>
              <td>
                <div class="rental-info">
                  <div>类型: {{ getRentalTypeText(house.rentalType) }}</div>
                  <div>房东: {{ house.landlordPhone }}</div>
                </div>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(house.status)">
                  {{ getStatusText(house.status) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button @click="editHouse(house)" class="edit-btn">编辑</button>
                  <button @click="updateHouseStatus(house.id, house.status === 0 ? 1 : 0)" 
                          class="status-btn"
                          :class="house.status === 0 ? 'rent-btn' : 'available-btn'">
                    {{ house.status === 0 ? '标记已租' : '标记可租' }}
                  </button>
                  <button @click="showVrDialog(house)" class="vr-btn">设置VR</button>
                  <button @click="deleteHouse(house.id)" class="delete-btn">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading">
          <p>加载中...</p>
        </div>
        
        <!-- 空状态 -->
        <div v-if="!loading && houses.length === 0" class="empty-state">
          <p>暂无房屋数据</p>
        </div>
      </div>
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

const API_BASE_URL = 'http://39.108.142.250:8080/api'

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
    if (response.data.success) {
      let filteredHouses = response.data.rooms || []
      
      // 应用状态筛选
      if (filterStatus.value !== '') {
        filteredHouses = filteredHouses.filter(house => house.status === parseInt(filterStatus.value))
      }
      
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
    const response = await axios.get(`${API_BASE_URL}/rooms/count`)
    if (response.data.success) {
      statistics.value = response.data
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

.add-btn, .refresh-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
}

.add-btn:hover {
  background: linear-gradient(135deg, #0056b3, #004085);
  transform: translateY(-2px);
}

.refresh-btn {
  background: #6c757d;
  color: white;
}

.refresh-btn:hover {
  background: #5a6268;
  transform: translateY(-2px);
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
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: transform 0.3s;
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
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.filter-item label {
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.filter-item select,
.filter-item input {
  padding: 10px 12px;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
}

.filter-item select:focus,
.filter-item input:focus {
  outline: none;
  border-color: #007bff;
}

.house-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-container {
  overflow-x: auto;
}

.house-table {
  width: 100%;
  border-collapse: collapse;
}

.house-table th,
.house-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.house-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.house-table tbody tr:hover {
  background-color: #f8f9fa;
}

.address-info strong {
  display: block;
  margin-bottom: 4px;
  color: #2c3e50;
}

.vr-btn {
  background-color: #6f42c1;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.3s;
}
.vr-btn:hover {
  background-color: #59359a;
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
  border-radius: 12px;
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
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.dialog-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
}

.close-btn:hover {
  color: #dc3545;
}

.dialog-content {
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 6px;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.cancel-btn,
.submit-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.cancel-btn {
  background: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background: #5a6268;
}

.submit-btn {
  background: #007bff;
  color: white;
}

.submit-btn:hover {
  background: #0056b3;
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
