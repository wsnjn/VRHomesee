<template>
  <div class="tenant-matching">
    <div class="page-header">
      <h2>租客匹配</h2>
      <p class="page-description">选择房屋和租客，创建租赁合同</p>
    </div>

    <div class="matching-container">
      <!-- 左侧：房屋列表 -->
      <div class="panel house-panel">
        <div class="panel-header">
          <h3>出租房屋列表</h3>
          <div class="filter-controls">
            <input 
              v-model="houseFilters.search" 
              placeholder="搜索房屋地址、名称..."
              class="search-input"
            />
            <select v-model="houseFilters.status" class="status-select">
              <option value="">全部状态</option>
              <option value="0">待出租</option>
              <option value="1">已出租</option>
              <option value="2">维护中</option>
            </select>
          </div>
        </div>
        
        <div class="panel-body">
          <div v-if="housesLoading" class="loading">正在加载房屋数据...</div>
          <div v-else-if="filteredHouses.length === 0" class="empty">暂无房屋数据</div>
          <div v-else class="items-list">
            <div 
              v-for="house in filteredHouses" 
              :key="house.id"
              class="item-card"
              :class="{ selected: selectedHouse?.id === house.id }"
              @click="selectHouse(house)"
            >
              <div class="item-header">
                <h4>{{ house.roomName || `房屋${house.id}` }}</h4>
                <span class="status-badge" :class="getHouseStatusClass(house.status)">
                  {{ getHouseStatusText(house.status) }}
                </span>
              </div>
              <div class="item-details">
                <p><strong>地址:</strong> {{ house.address || '未设置' }}</p>
                <p><strong>面积:</strong> {{ house.area || 0 }}㎡</p>
                <p><strong>租金:</strong> ¥{{ house.monthlyRent || 0 }}</p>
                <p><strong>房东ID:</strong> {{ house.landlordId || '未设置' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：操作区域 -->
      <div class="action-panel">
        <div class="selected-info">
          <div v-if="selectedHouse" class="selected-item">
            <h4>已选房屋</h4>
            <p><strong>{{ selectedHouse.roomName || `房屋${selectedHouse.id}` }}</strong></p>
            <p>地址: {{ selectedHouse.address || '未设置' }}</p>
            <p>租金: ¥{{ selectedHouse.monthlyRent || 0 }}</p>
          </div>
          <div v-else class="placeholder">请选择房屋</div>

          <div class="arrow">↓</div>

          <div v-if="selectedUser" class="selected-item">
            <h4>已选租客</h4>
            <p><strong>{{ selectedUser.realName || selectedUser.username }}</strong></p>
            <p>手机: {{ selectedUser.phone }}</p>
            <p>类型: {{ getUserTypeText(selectedUser.userType) }}</p>
          </div>
          <div v-else class="placeholder">请选择租客</div>
        </div>

        <button 
          @click="showCreateContractModal = true"
          :disabled="!selectedHouse || !selectedUser"
          class="create-contract-btn"
        >
          创建租赁合同
        </button>
      </div>
      <!-- 右侧：用户列表 -->
      <div class="panel user-panel">
        <div class="panel-header">
          <h3>用户列表</h3>
          <div class="filter-controls">
            <input 
              v-model="userFilters.search" 
              placeholder="搜索用户名、真实姓名..."
              class="search-input"
            />
            <select v-model="userFilters.userType" class="type-select">
              <option value="">全部类型</option>
              <option value="1">租客</option>
              <option value="2">房东</option>
            </select>
          </div>
        </div>
        
        <div class="panel-body">
          <div v-if="usersLoading" class="loading">正在加载用户数据...</div>
          <div v-else-if="filteredUsers.length === 0" class="empty">暂无用户数据</div>
          <div v-else class="items-list">
            <div 
              v-for="user in filteredUsers" 
              :key="user.id"
              class="item-card"
              :class="{ selected: selectedUser?.id === user.id }"
              @click="selectUser(user)"
            >
              <div class="item-header">
                <h4>{{ user.realName || user.username }}</h4>
                <span class="type-badge" :class="getUserTypeClass(user.userType)">
                  {{ getUserTypeText(user.userType) }}
                </span>
              </div>
              <div class="item-details">
                <p><strong>用户名:</strong> {{ user.username }}</p>
                <p><strong>手机:</strong> {{ user.phone }}</p>
                <p><strong>邮箱:</strong> {{ user.email || '未设置' }}</p>
                <p><strong>身份证:</strong> {{ user.idCard || '未设置' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建合同模态框 -->
    <div v-if="showCreateContractModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>创建租赁合同</h3>
          <button @click="showCreateContractModal = false" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <div class="contract-info">
            <div class="info-section">
              <h4>房屋信息</h4>
              <p><strong>房屋:</strong> {{ selectedHouse?.roomName || `房屋${selectedHouse?.id}` }}</p>
              <p><strong>地址:</strong> {{ selectedHouse?.address || '未设置' }}</p>
              <p><strong>月租金:</strong> ¥{{ selectedHouse?.monthlyRent || 0 }}</p>
            </div>
            
            <div class="info-section">
              <h4>租客信息</h4>
              <p><strong>租客:</strong> {{ selectedUser?.realName || selectedUser?.username }}</p>
              <p><strong>手机:</strong> {{ selectedUser?.phone }}</p>
              <p><strong>身份证:</strong> {{ selectedUser?.idCard || '未设置' }}</p>
            </div>
          </div>

          <div class="form-section">
            <div class="form-group">
              <label>合同编号 *</label>
              <input v-model="newContract.contractNumber" placeholder="请输入合同编号" />
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>合同开始日期 *</label>
                <input v-model="newContract.contractStartDate" type="date" />
              </div>
              <div class="form-group">
                <label>合同结束日期 *</label>
                <input v-model="newContract.contractEndDate" type="date" />
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>月租金 *</label>
                <input 
                  v-model="newContract.monthlyRent" 
                  type="number" 
                  placeholder="0.00"
                />
              </div>
              <div class="form-group">
                <label>押金金额 *</label>
                <input v-model="newContract.depositAmount" type="number" placeholder="0.00" />
              </div>
            </div>
            
            <div class="form-group">
              <label>付款周期 *</label>
              <select v-model="newContract.paymentCycle" class="payment-cycle-select">
                <option value="">请选择付款周期</option>
                <option value="1">月付</option>
                <option value="2">季付</option>
                <option value="3">年付</option>
              </select>
            </div>
            
            <div class="form-group">
              <label>备注</label>
              <textarea v-model="newContract.remarks" placeholder="请输入合同备注信息"></textarea>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="showCreateContractModal = false" class="cancel-btn">取消</button>
          <button @click="createContract" class="confirm-btn">创建合同</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

// 响应式数据
const housesLoading = ref(false)
const usersLoading = ref(false)
const houses = ref([])
const users = ref([])
const selectedHouse = ref(null)
const selectedUser = ref(null)
const showCreateContractModal = ref(false)

// 筛选条件
const houseFilters = ref({
  search: '',
  status: ''
})

const userFilters = ref({
  search: '',
  userType: ''
})

// 新合同数据
const newContract = ref({
  contractNumber: '',
  roomId: '',
  landlordId: '',
  tenantId: '',
  contractStartDate: '',
  contractEndDate: '',
  monthlyRent: '',
  depositAmount: '',
  paymentCycle: '',
  remarks: ''
})

// 计算属性：筛选后的房屋列表
const filteredHouses = computed(() => {
  let filtered = houses.value
  
  // 搜索筛选
  if (houseFilters.value.search) {
    const search = houseFilters.value.search.toLowerCase()
    filtered = filtered.filter(house => 
      (house.roomName && house.roomName.toLowerCase().includes(search)) ||
      (house.address && house.address.toLowerCase().includes(search))
    )
  }
  
  // 状态筛选
  if (houseFilters.value.status !== '') {
    filtered = filtered.filter(house => house.status === parseInt(houseFilters.value.status))
  }
  
  return filtered
})

// 计算属性：筛选后的用户列表
const filteredUsers = computed(() => {
  let filtered = users.value
  
  // 搜索筛选
  if (userFilters.value.search) {
    const search = userFilters.value.search.toLowerCase()
    filtered = filtered.filter(user => 
      (user.username && user.username.toLowerCase().includes(search)) ||
      (user.realName && user.realName.toLowerCase().includes(search)) ||
      (user.phone && user.phone.includes(search))
    )
  }
  
  // 用户类型筛选
  if (userFilters.value.userType !== '') {
    filtered = filtered.filter(user => user.userType === parseInt(userFilters.value.userType))
  }
  
  return filtered
})

// 加载房屋列表
const loadHouses = async () => {
  housesLoading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/admin/matching/houses`)
    if (response.data.success) {
      houses.value = response.data.houses || []
    }
  } catch (error) {
    console.error('加载房屋列表失败:', error)
    houses.value = []
  } finally {
    housesLoading.value = false
  }
}

// 加载用户列表
const loadUsers = async () => {
  usersLoading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/admin/user/all`)
    if (response.data.success) {
      users.value = response.data.users || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    users.value = []
  } finally {
    usersLoading.value = false
  }
}

// 选择房屋
const selectHouse = (house) => {
  selectedHouse.value = house
  // 自动填充房屋相关信息
  if (house) {
    console.log('选择的房屋数据:', house)
    newContract.value.roomId = house.id
    // 不设置landlordId，后端会从房屋信息中自动获取
    newContract.value.monthlyRent = house.monthlyRent || ''
  }
}

// 选择用户
const selectUser = (user) => {
  selectedUser.value = user
  // 自动填充租客信息
  if (user) {
    console.log('选择的用户数据:', user)
    newContract.value.tenantId = user.id
  }
}

// 房屋状态相关方法
const getHouseStatusClass = (status) => {
  const classes = {
    0: 'status-available',
    1: 'status-rented',
    2: 'status-maintenance'
  }
  return classes[status] || 'status-unknown'
}

const getHouseStatusText = (status) => {
  const texts = {
    0: '待出租',
    1: '已出租',
    2: '维护中'
  }
  return texts[status] || '未知状态'
}

// 用户类型相关方法
const getUserTypeClass = (userType) => {
  const classes = {
    1: 'type-landlord',
    2: 'type-tenant'
  }
  return classes[userType] || 'type-unknown'
}

const getUserTypeText = (userType) => {
  const texts = {
    1: '租客',
    2: '房东'
  }
  return texts[userType] || '未知类型'
}

// 创建合同
const createContract = async () => {
  try {
    // 简化验证：只验证最核心的字段
    if (!newContract.value.contractNumber) {
      alert('请填写合同编号')
      return
    }
    
    if (!newContract.value.roomId) {
      alert('请选择房屋')
      return
    }
    
    if (!newContract.value.tenantId) {
      alert('请选择租客')
      return
    }

    // 调试：打印要发送的数据
    console.log('发送的合同数据:', JSON.stringify(newContract.value, null, 2))

    // 复制合同数据，但不包括landlordId，因为后端会从房屋信息中自动获取
    const contractData = { ...newContract.value }
    delete contractData.landlordId

    const response = await axios.post(`${API_BASE_URL}/admin/matching/create`, contractData)
    if (response.data.success) {
      alert('租赁合同创建成功')
      showCreateContractModal.value = false
      resetNewContract()
      // 重新加载数据
      loadHouses()
      loadUsers()
    } else {
      alert('创建合同失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('创建合同失败:', error)
    alert('创建合同失败: ' + (error.response?.data?.message || error.message))
  }
}

// 重置新合同数据
const resetNewContract = () => {
  newContract.value = {
    contractNumber: '',
    roomId: '',
    landlordId: '',
    tenantId: '',
    contractStartDate: '',
    contractEndDate: '',
    monthlyRent: '',
    depositAmount: '',
    paymentCycle: '',
    remarks: ''
  }
  selectedHouse.value = null
  selectedUser.value = null
}

// 页面加载时初始化数据
onMounted(() => {
  loadHouses()
  loadUsers()
})
</script>

<style scoped>
.tenant-matching {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
}

.page-description {
  color: #6c757d;
  margin: 0.5rem 0 0 0;
}

.matching-container {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 2rem;
  height: 70vh;
  align-items: start; /* 确保三个面板顶端对齐 */
}

.panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.panel-header h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
}

.filter-controls {
  display: flex;
  gap: 1rem;
}

.search-input, .status-select, .type-select {
  padding: 0.5rem;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  flex: 1;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.loading, .empty {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.item-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.item-card:hover {
  border-color: #007bff;
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.1);
}

.item-card.selected {
  border-color: #007bff;
  background-color: #f8f9ff;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.item-header h4 {
  margin: 0;
  color: #2c3e50;
  flex: 1;
}

.status-badge, .type-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  text-align: center;
}

.status-available { background: #d4edda; color: #155724; }
.status-rented { background: #cce7ff; color: #004085; }
.status-maintenance { background: #fff3cd; color: #856404; }
.status-unknown { background: #e2e3e5; color: #383d41; }

.type-landlord { background: #d1ecf1; color: #0c5460; }
.type-tenant { background: #d4edda; color: #155724; }
.type-unknown { background: #e2e3e5; color: #383d41; }

.item-details p {
  margin: 0.25rem 0;
  font-size: 0.9rem;
  color: #6c757d;
}

.action-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 改为顶部对齐 */
  align-items: center;
  gap: 2rem;
  min-width: 200px;
  padding-top: 1rem; /* 添加顶部内边距 */
}

.selected-info {
  text-align: center;
}

.selected-item {
  background: white;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 1rem;
}

.selected-item h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.selected-item p {
  margin: 0.25rem 0;
  font-size: 0.9rem;
}

.placeholder {
  padding: 2rem;
  color: #6c757d;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.arrow {
  font-size: 2rem;
  color: #007bff;
  margin: 1rem 0;
}

.create-contract-btn {
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.create-contract-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.create-contract-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
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
}

.modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #dc3545;
}

.modal-body {
  padding: 1.5rem;
}

.contract-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-section h4 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
}

.info-section p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
}

.form-section {
  margin-top: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input, .form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s;
}

.form-group input:focus, .form-group textarea:focus {
  outline: none;
  border-color: #007bff;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.cancel-btn, .confirm-btn {
  padding: 0.75rem 1.5rem;
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

.confirm-btn {
  background: #007bff;
  color: white;
}

.cancel-btn:hover {
  background: #5a6268;
}

.confirm-btn:hover {
  background: #0056b3;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .matching-container {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr auto 1fr;
    height: auto;
  }
  
  .action-panel {
    flex-direction: row;
    justify-content: space-between;
    min-width: auto;
  }
  
  .selected-info {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .arrow {
    transform: rotate(90deg);
    margin: 0 1rem;
  }
}

@media (max-width: 768px) {
  .contract-info {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .filter-controls {
    flex-direction: column;
  }
  
  .selected-info {
    flex-direction: column;
  }
  
  .arrow {
    transform: none;
    margin: 1rem 0;
  }
}
</style>
