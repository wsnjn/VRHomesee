<template>
  <div class="tenant-matching">
  
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

          <div class="arrow">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><polyline points="19 12 12 19 5 12"></polyline></svg>
          </div>

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
                <p v-if="user.rentalBudgetMin && user.rentalBudgetMax">
                  <strong>预算:</strong> ¥{{ user.rentalBudgetMin }}-{{ user.rentalBudgetMax }}/月
                </p>
              </div>
              <div class="item-actions">
                <button 
                  class="intention-btn" 
                  :class="{ 'has-intention': hasIntention(user) }"
                  @click.stop="viewUserIntention(user)"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  {{ hasIntention(user) ? '有明确意向' : '暂无意向' }}
                  <span v-if="getUserIntentions(user).length > 0" class="intention-count">({{ getUserIntentions(user).length }})</span>
                </button>
                <button class="contact-btn" @click.stop="contactUser(user)">
                  <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                  立即联系
                </button>
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

    <!-- 意向详情弹窗 -->
    <div v-if="showIntentionModal" class="modal-overlay">
      <div class="modal intention-modal">
        <div class="modal-header">
          <h3>{{ selectedIntentionUser?.realName || selectedIntentionUser?.username }} 的预约意向</h3>
          <button @click="showIntentionModal = false" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <div v-if="selectedUserIntentions.length === 0" class="empty-intentions">
            <p>该用户暂无预约记录</p>
          </div>
          
          <div v-else class="intentions-list">
            <div 
              v-for="(apt, index) in selectedUserIntentions" 
              :key="apt.id || index"
              class="intention-item"
            >
              <div class="intention-header">
                <span class="intention-date">{{ formatDate(apt.preferredDate) }}</span>
                <span class="intention-status" :class="getAppointmentStatusClass(apt.status)">
                  {{ getAppointmentStatusText(apt.status) }}
                </span>
              </div>
              <div class="intention-content">
                <p><strong>预约房屋:</strong> {{ getHouseAddress(apt) }}</p>
                <p><strong>预约类型:</strong> {{ apt.appointmentType === 1 ? '现场看房' : '视频看房' }}</p>
                <p><strong>预约时段:</strong> {{ apt.preferredTimeSlot || '未设置' }}</p>
                <p v-if="apt.expectedMoveInDate"><strong>期望入住:</strong> {{ formatDate(apt.expectedMoveInDate) }}</p>
                <p v-if="apt.rentalIntention" class="intention-text">
                  <strong>租房意向:</strong> {{ apt.rentalIntention }}
                </p>
                <p v-else class="no-intention">暂未填写租房意向</p>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="showIntentionModal = false" class="cancel-btn">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'

// Props
const props = defineProps({
  preSelectedData: {
    type: Object,
    default: null
  }
})

// API基础URL
const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const housesLoading = ref(false)
const usersLoading = ref(false)
const houses = ref([])
const users = ref([])
const appointments = ref([]) // 预约数据
const selectedHouse = ref(null)
const selectedUser = ref(null)
const showCreateContractModal = ref(false)
const showIntentionModal = ref(false) // 意向详情弹窗
const selectedUserIntentions = ref([]) // 选中用户的意向列表
const selectedIntentionUser = ref(null) // 选中查看意向的用户

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

// 加载预约数据
const loadAppointments = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/viewing-appointment/all`)
    if (response.data.success) {
      appointments.value = response.data.appointments || []
      // 调试：查看数据结构
      if (appointments.value.length > 0) {
        console.log('预约数据示例:', appointments.value[0])
      }
    }
  } catch (error) {
    console.error('加载预约数据失败:', error)
    appointments.value = []
  }
}

// 获取用户的意向记录（根据手机号匹配）
const getUserIntentions = (user) => {
  return appointments.value
    .filter(apt => apt.contactPhone === user.phone)
    .sort((a, b) => new Date(b.appointmentDate) - new Date(a.appointmentDate)) // 按时间倒序
}

// 检查用户是否有意向
const hasIntention = (user) => {
  const intentions = getUserIntentions(user)
  return intentions.some(apt => apt.rentalIntention && apt.rentalIntention.trim() !== '')
}

// 查看用户意向详情
const viewUserIntention = (user) => {
  selectedIntentionUser.value = user
  selectedUserIntentions.value = getUserIntentions(user)
  showIntentionModal.value = true
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

// 预约状态相关方法
const getAppointmentStatusClass = (status) => {
  const classes = {
    0: 'apt-status-pending',
    1: 'apt-status-confirmed',
    2: 'apt-status-completed',
    3: 'apt-status-cancelled',
    4: 'apt-status-expired',
    5: 'apt-status-missed'
  }
  return classes[status] || 'apt-status-unknown'
}

const getAppointmentStatusText = (status) => {
  const texts = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消',
    4: '已过期',
    5: '用户爽约'
  }
  return texts[status] || '未知状态'
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 获取房屋地址（多种来源尝试）
const getHouseAddress = (apt) => {
  // 优先使用 roomInfo 中的地址
  if (apt.roomInfo?.address) {
    return apt.roomInfo.address
  }
  
  // 尝试通过 roomId 从房屋列表中查找
  if (apt.roomId) {
    const house = houses.value.find(h => h.id === apt.roomId)
    if (house?.address) {
      return house.address
    }
  }
  
  // 返回未知
  return '未知'
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

// 联系用户
const contactUser = (user) => {
  if (user.phone) {
    window.open(`tel:${user.phone}`)
  } else {
    alert('该用户未提供联系电话')
  }
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
onMounted(async () => {
  await loadHouses()
  await loadUsers()
  await loadAppointments()
  
  // 如果有预选数据，自动选择
  if (props.preSelectedData) {
    applyPreSelectedData(props.preSelectedData)
  }
})

// 应用预选数据
const applyPreSelectedData = (data) => {
  if (!data) return
  
  // 自动选择房屋
  if (data.roomId) {
    const house = houses.value.find(h => h.id === data.roomId)
    if (house) {
      selectHouse(house)
    }
  }
  
  // 自动选择用户（通过手机号匹配）
  if (data.contactPhone) {
    const user = users.value.find(u => u.phone === data.contactPhone)
    if (user) {
      selectUser(user)
    }
  }
  
  // 自动填充合同编号（使用预约编号）
  if (data.appointmentNumber) {
    newContract.value.contractNumber = data.appointmentNumber
  }
}

// 监听预选数据变化
watch(() => props.preSelectedData, (newData) => {
  if (newData) {
    applyPreSelectedData(newData)
  }
}, { immediate: false })
</script>

<style scoped>
.tenant-matching {
  max-width: 1400px;
  margin: 0 auto;
}

.matching-container {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 16px;
  height: 70vh;
  align-items: start;
}

.panel {
  background: #fff;
  border: 1px solid #ddd;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #ddd;
  background: #f9f9f9;
}

.panel-header h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.filter-controls {
  display: flex;
  gap: 8px;
}

.search-input, .status-select, .type-select {
  padding: 8px;
  border: 1px solid #ddd;
  font-size: 12px;
  flex: 1;
}

.search-input:focus, .status-select:focus, .type-select:focus {
  outline: none;
  border-color: #3A6EA5;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.loading, .empty {
  text-align: center;
  padding: 24px;
  color: #888;
  font-size: 12px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.item-card {
  border: 1px solid #ddd;
  border-bottom: none;
  padding: 8px 12px;
  cursor: pointer;
}

.item-card:last-child {
  border-bottom: 1px solid #ddd;
}

.item-card:hover {
  background: #f9f9f9;
}

.item-card.selected {
  border-color: #3A6EA5;
  background-color: #f0f5fa;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.item-header h4 {
  margin: 0;
  color: #333;
  font-size: 12px;
  font-weight: 500;
  flex: 1;
}

.status-badge, .type-badge {
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 500;
  text-align: center;
}

/* 房屋状态颜色 - 不同状态不同颜色 */
.status-available { background: #3A6EA5; color: #fff; } /* 待出租 - 蓝色 */
.status-rented { background: #2d8a4e; color: #fff; } /* 已出租 - 绿色 */
.status-maintenance { background: #c07700; color: #fff; } /* 维护中 - 橙色 */
.status-unknown { background: #888; color: #fff; }

/* 用户类型颜色 - 不同类型不同颜色 */
.type-landlord { background: #1e3a5f; color: #fff; } /* 房东 - 深蓝 */
.type-tenant { background: #3A6EA5; color: #fff; } /* 租客 - 蓝色 */
.type-unknown { background: #888; color: #fff; }

.item-details p {
  margin: 2px 0;
  font-size: 11px;
  color: #888;
}

/* 用户卡片操作按钮 */
.item-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #eee;
}

.intention-btn,
.contact-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: 10px;
  cursor: pointer;
  border: 1px solid #ddd;
  background: transparent;
  color: #888;
}

.intention-btn svg,
.contact-btn svg {
  width: 12px;
  height: 12px;
}

.intention-btn.has-intention {
  border-color: #3A6EA5;
  color: #3A6EA5;
  background: #f0f5fa;
}

.intention-count {
  font-size: 9px;
  color: inherit;
}

.contact-btn {
  border-color: #3A6EA5;
  color: #3A6EA5;
}

.contact-btn:hover {
  background: #3A6EA5;
  color: #fff;
}

/* 预约状态颜色 */
.apt-status-pending { background: #c07700; color: #fff; }
.apt-status-confirmed { background: #3A6EA5; color: #fff; }
.apt-status-completed { background: #2d8a4e; color: #fff; }
.apt-status-cancelled { background: #888; color: #fff; }
.apt-status-expired { background: #888; color: #fff; }
.apt-status-missed { background: #c00; color: #fff; }
.apt-status-unknown { background: #888; color: #fff; }

/* 意向弹窗样式 */
.intention-modal {
  max-width: 600px;
}

.empty-intentions {
  text-align: center;
  padding: 24px;
  color: #888;
  font-size: 12px;
}

.intentions-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.intention-item {
  border: 1px solid #ddd;
  border-bottom: none;
  padding: 12px;
}

.intention-item:last-child {
  border-bottom: 1px solid #ddd;
}

.intention-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.intention-date {
  font-size: 12px;
  color: #333;
  font-weight: 500;
}

.intention-status {
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 500;
}

.intention-content p {
  margin: 4px 0;
  font-size: 11px;
  color: #888;
}

.intention-text {
  color: #333 !important;
  font-weight: 500;
}

.no-intention {
  color: #888;
  font-style: italic;
}

.action-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  gap: 16px;
  min-width: 180px;
  padding-top: 8px;
}

.selected-info {
  text-align: center;
}

.selected-item {
  background: #fff;
  padding: 8px 12px;
  border: 1px solid #ddd;
  margin-bottom: 8px;
}

.selected-item h4 {
  margin: 0 0 4px 0;
  color: #333;
  font-size: 12px;
  font-weight: 500;
}

.selected-item p {
  margin: 2px 0;
  font-size: 11px;
  color: #888;
}

.placeholder {
  padding: 16px;
  color: #888;
  background: #f9f9f9;
  border: 1px dashed #ddd;
  font-size: 12px;
}

.arrow {
  font-size: 16px;
  color: #1e3a5f;
  margin: 8px 0;
}

.create-contract-btn {
  background: #1e3a5f;
  color: #fff;
  border: 1px solid #1e3a5f;
  padding: 8px 16px;
  cursor: pointer;
  font-weight: 400;
  font-size: 12px;
}

.create-contract-btn:hover:not(:disabled) {
  background: #2d5a87;
  border-color: #2d5a87;
}

.create-contract-btn:disabled {
  background: #888;
  border-color: #888;
  cursor: not-allowed;
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
  background: #fff;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid #ddd;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ddd;
  background: #f9f9f9;
}

.modal-header h3 {
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

.modal-body {
  padding: 16px;
}

.contract-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px;
  background: #f9f9f9;
  border: 1px solid #ddd;
}

.info-section h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 12px;
  font-weight: 500;
}

.info-section p {
  margin: 4px 0;
  font-size: 11px;
  color: #888;
}

.form-section {
  margin-top: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: #333;
  font-size: 12px;
}

.form-group input, .form-group textarea, .form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  font-size: 13px;
  box-sizing: border-box;
}

.form-group input:focus, .form-group textarea:focus, .form-group select:focus {
  outline: none;
  border-color: #3A6EA5;
}

.form-group textarea {
  min-height: 60px;
  resize: vertical;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.modal-footer {
  padding: 16px;
  border-top: 1px solid #ddd;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.cancel-btn, .confirm-btn {
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

.confirm-btn {
  background: #1e3a5f;
  color: #fff;
  border-color: #1e3a5f;
}

.confirm-btn:hover {
  background: #2d5a87;
  border-color: #2d5a87;
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
