<!--
  项目名称：融合大模型交互与3D全景预览的智能选房平台
  文件名称：LandlordTenantMatching.vue
  开发者：牛迦楠
  专业：软件工程（中外合作办学）
  学校：东华理工大学
  功能描述：房东租客匹配组件，基于AI推荐系统为空置房源匹配合适租客
  创建日期：2026-01-06
-->
<template>
  <div class="landlord-tenant-matching">


    <div class="matching-container">
      <!-- 左侧：当前房东的房屋列表 -->
      <div class="panel house-panel">
        <div class="panel-header">
          <h3>我的房屋列表</h3>
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
              <div class="item-details muted-text">
                <p>地址: {{ house.address || '未设置' }}</p>
                <p>面积: {{ house.area || 0 }}㎡</p>
                <p>租金: ¥{{ house.monthlyRent || 0 }}</p>
                <p>房东: {{ house.landlordName || '未知' }}</p>
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
            <p>用户状态: {{ getUserLatestStatusText(selectedUser) }}</p>
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
            <select v-model="userFilters.appointmentStatus" class="status-select">
              <option value="">全部状态</option>
              <option value="0">待确认</option>
              <option value="1">已确认</option>
              <option value="2">已完成</option>
              <option value="3">已取消</option>
              <option value="4">已过期</option>
              <option value="5">用户爽约</option>
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
                <span class="status-badge" :class="getUserLatestStatusClass(user)">
                  {{ getUserLatestStatusText(user) }}
                </span>
              </div>
              <div class="item-details muted-text">
                <p>用户名: {{ user.username }}</p>
                <p>手机: {{ user.phone }}</p>
                <p>邮箱: {{ user.email || '未设置' }}</p>
                <p v-if="user.rentalBudgetMin && user.rentalBudgetMax">
                  预算: ¥{{ user.rentalBudgetMin }}-{{ user.rentalBudgetMax }}/月
                </p>
              </div>
              <div class="tenant-card-actions">
                <button 
                  class="btn-view-intention" 
                  :class="{ 'has-intention': hasIntention(user) }"
                  @click.stop="viewUserIntention(user)"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  <div class="btn-text-group">
                    <span v-if="getUserIntentionCount(user) > 0" class="count-text">{{ getUserIntentionCount(user) }} 个意向</span>
                    <span v-else class="count-text">暂无意向</span>
                  </div>
                </button>
                <a :href="'tel:' + user.phone" class="btn-contact-tenant" @click.stop>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                  立即联系
                </a>
              </div>
            </div>
          </div>
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
                <p><strong>预约房屋:</strong> {{ getHouseAddress(apt.roomInfo) }}</p>
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
              <p><strong>租客:</strong> {{ selectedUser?.realName || selectedUser?.username || '未选择' }}</p>
              <p><strong>手机:</strong> {{ selectedUser?.phone || '未选择' }}</p>
              <p><strong>邮箱:</strong> {{ selectedUser?.email || '未设置' }}</p>
            </div>
          </div>

          <div class="form-section">
            <div class="form-group">
              <label>合同编号</label>
              <input v-model="newContract.contractNumber" readonly class="readonly-input" />
              <p class="hint-text">合同编号将使用预约编号自动生成</p>
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
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'

const props = defineProps(['userPhone', 'preSelectedData'])

// API基础URL
const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const housesLoading = ref(false)
const usersLoading = ref(false)
const appointmentsLoading = ref(false)
const houses = ref([])
const users = ref([])
const appointments = ref([])
const selectedHouse = ref(null)
const selectedUser = ref(null)
const selectedAppointment = ref(null)
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
  appointmentStatus: ''
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
  
  // 预约状态筛选：只显示有该状态预约的用户
  if (userFilters.value.appointmentStatus !== '') {
    const statusFilter = parseInt(userFilters.value.appointmentStatus)
    filtered = filtered.filter(user => {
      // 检查该用户是否有指定状态的预约
      const userAppointments = appointments.value.filter(apt => apt.contactPhone === user.phone)
      return userAppointments.some(apt => apt.status === statusFilter)
    })
  }
  
  return filtered
})

// 加载当前房东的房屋列表
const loadHouses = async () => {
  if (!props.userPhone) {
    console.error('用户手机号不存在')
    return
  }

  housesLoading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/landlord/houses?landlordPhone=${props.userPhone}`)
    if (response.data.success) {
      // 转换房屋数据格式
      houses.value = (response.data.houses || []).map(house => ({
        ...house,
        roomName: house.communityName + ' ' + house.doorNumber + (house.roomNumber ? '-' + house.roomNumber : ''),
        address: house.province + house.city + house.district + house.street + house.communityName,
        area: house.roomArea,
        monthlyRent: house.rentPrice,
        landlordName: house.landlordName || '房东'
      }))
    }
  } catch (error) {
    console.error('加载房屋列表失败:', error)
    houses.value = []
  } finally {
    housesLoading.value = false
  }
}

// 加载用户列表（租客）
const loadUsers = async () => {
  usersLoading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/admin/user/all`)
    if (response.data.success) {
      // 只保留租客用户（userType = 1 或 3）
      users.value = (response.data.users || []).filter(u => u.userType === 1 || u.userType === 3)
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    users.value = []
  } finally {
    usersLoading.value = false
  }
}

// 获取用户的意向记录（根据手机号匹配）
const getUserIntentions = (user) => {
  return appointments.value
    .filter(apt => apt.contactPhone === user.phone)
    .sort((a, b) => new Date(b.preferredDate) - new Date(a.preferredDate)) // 按时间倒序
}

// 获取用户意向数量
const getUserIntentionCount = (user) => {
  return getUserIntentions(user).length
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

// 选择用户
const selectUser = (user) => {
  console.log('选择用户:', user)
  selectedUser.value = user
  
  // 自动填充租客信息
  if (user) {
    newContract.value.tenantId = user.id
    
    // 获取该用户最新的预约，用于生成合同编号
    const userAppointments = appointments.value
      .filter(apt => apt.contactPhone === user.phone)
      .sort((a, b) => new Date(b.preferredDate) - new Date(a.preferredDate))
    
    if (userAppointments.length > 0) {
      const latestApt = userAppointments[0]
      newContract.value.contractNumber = latestApt.appointmentNumber || `APPT-${latestApt.id}`
      console.log('自动填充合同编号:', newContract.value.contractNumber)
    }
  }
}

// 获取用户最新预约的状态
const getUserLatestStatus = (user) => {
  const userAppointments = appointments.value
    .filter(apt => apt.contactPhone === user.phone)
    .sort((a, b) => new Date(b.preferredDate) - new Date(a.preferredDate))
  return userAppointments.length > 0 ? userAppointments[0].status : null
}

// 获取用户最新预约状态的CSS类
const getUserLatestStatusClass = (user) => {
  const status = getUserLatestStatus(user)
  const statusClassMap = {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-completed',
    3: 'status-cancelled',
    4: 'status-expired',
    5: 'status-missed'
  }
  return statusClassMap[status] || 'status-unknown'
}

// 获取用户最新预约状态的文本
const getUserLatestStatusText = (user) => {
  const status = getUserLatestStatus(user)
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消',
    4: '已过期',
    5: '用户爽约'
  }
  return statusMap[status] || '未知'
}

// 加载当前房东的预约列表
const loadAppointments = async () => {
  if (!props.userPhone) {
    console.error('用户手机号不存在')
    return
  }

  appointmentsLoading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/viewing-appointment/landlord/${props.userPhone}`)
    if (response.data.success) {
      // 获取预约列表
      const appointmentsData = response.data.appointments || []
      
      // 为每个预约获取房屋信息
      const appointmentsWithRoomInfo = await Promise.all(
        appointmentsData.map(async (appointment) => {
          try {
            // 获取房屋信息
            const roomResponse = await axios.get(`${API_BASE_URL}/rooms/${appointment.roomId}`)
            if (roomResponse.data.success) {
              return {
                ...appointment,
                roomInfo: roomResponse.data.room
              }
            }
            return appointment
          } catch (error) {
            return appointment
          }
        })
      )
      
      appointments.value = appointmentsWithRoomInfo

      // 从预约数据中提取唯一租客列表
      const uniqueUsersMap = new Map()
      appointments.value.forEach(app => {
        const key = app.userId || app.contactPhone
        if (!key) return
        
        if (!uniqueUsersMap.has(key)) {
          uniqueUsersMap.set(key, {
            id: app.userId,
            username: `tenant_${app.contactPhone}`, // 构造临时用户名
            realName: app.contactName || '未知用户',
            phone: app.contactPhone,
            email: app.contactPhone + '@tenant.com', // 临时构造或留空
            userType: 1, // 默认为租客
            // 尝试从预约中获取预算或其他信息（如果有）
            rentalBudgetMin: 2000, // 示例默认值，实际应从用户画像API获取
            rentalBudgetMax: 3500
          })
        }
      })
      users.value = Array.from(uniqueUsersMap.values())
      console.log('已提取租客列表:', users.value)

    }
  } catch (error) {
    console.error('加载预约数据失败:', error)
    appointments.value = []
    users.value = []
  } finally {
    appointmentsLoading.value = false
  }
}

// 选择房屋
const selectHouse = (house) => {
  selectedHouse.value = house
  // 自动填充房屋相关信息
  if (house) {
    newContract.value.roomId = house.id
    newContract.value.monthlyRent = house.monthlyRent || ''
  }
}

// 选择预约（租客）
const selectAppointment = (appointment) => {
  selectedAppointment.value = appointment
  // 自动填充租客信息和合同编号
  if (appointment) {
    newContract.value.tenantId = appointment.userId
    // 使用预约编号作为合同编号
    newContract.value.contractNumber = appointment.appointmentNumber || `APPT-${appointment.id}`
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
  const statusClassMap = {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-completed',
    3: 'status-cancelled',
    4: 'status-expired',
    5: 'status-missed'
  }
  return statusClassMap[status] || 'status-unknown'
}

const getAppointmentStatusText = (status) => {
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消',
    4: '已过期',
    5: '用户爽约'
  }
  return statusMap[status] || '未知状态'
}

const getAppointmentTypeText = (type) => {
  return type === 1 ? '现场看房' : '视频看房'
}

// 获取房屋地址信息
const getHouseAddress = (roomInfo) => {
  if (!roomInfo) return '未知地址'
  
  const parts = [
    roomInfo.province,
    roomInfo.city,
    roomInfo.district,
    roomInfo.street,
    roomInfo.communityName,
    roomInfo.buildingUnit,
    roomInfo.doorNumber,
    roomInfo.roomNumber
  ].filter(part => part && part.trim() !== '')
  
  return parts.join(' ') || '未知地址'
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
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

    const response = await axios.post(`${API_BASE_URL}/landlord/matching/create?landlordPhone=${props.userPhone}`, contractData)
    if (response.data.success) {
      alert('租赁合同创建成功')
      showCreateContractModal.value = false
      resetNewContract()
      // 重新加载数据
      loadHouses()
      loadAppointments()
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
  selectedAppointment.value = null
}

// 页面加载时初始化数据
onMounted(() => {
  loadHouses()
  loadUsers()
  loadAppointments()
})

// 用户类型相关方法
const getUserTypeClass = (userType) => {
  const classes = {
    1: 'type-tenant',
    2: 'type-landlord'
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

// 监听预选数据，自动选择对应房屋和用户
watch(() => props.preSelectedData, async (newData) => {
  if (newData && newData.roomId) {
    // 等待数据加载完成
    await Promise.all([loadHouses(), loadUsers(), loadAppointments()])
    
    // 自动选择房屋
    const house = houses.value.find(h => h.id === newData.roomId)
    if (house) {
      selectHouse(house)
    }
    
    // 自动选择用户（通过手机号匹配）
    if (newData.contactPhone) {
      const user = users.value.find(u => u.phone === newData.contactPhone)
      if (user) {
        selectUser(user)
      }
    }
  }
}, { immediate: true })
</script>

<style scoped>
.landlord-tenant-matching {
  max-width: 1400px;
  margin: 0 auto;
  font-family: 'Inter', sans-serif;
}

/* 租户卡片操作 - 紧凑风格 */
.tenant-card-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  align-items: center;
}

.btn-view-intention {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid #ddd;
  background: white;
  color: #666;
  text-decoration: none;
  border-radius: 2px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-view-intention:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

.btn-view-intention.has-intention {
  border-color: #3A6EA5;
  color: #3A6EA5;
  background: #f0f5fa;
}

.btn-view-intention svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.btn-text-group {
  display: inline;
}

.count-text {
  font-size: 12px;
}

.btn-contact-tenant {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid #3A6EA5;
  background: white;
  color: #3A6EA5;
  text-decoration: none;
  border-radius: 2px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-contact-tenant:hover {
  background: #3A6EA5;
  color: white;
}

.btn-contact-tenant svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

/* 浅灰色详情文字 */
.muted-text p {
  color: #888;
  font-size: 12px;
  margin: 4px 0;
}

/* 预约状态徽章 */
.status-badge {
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 500;
  text-align: center;
  border-radius: 2px;
}

.status-pending { background: #FEF3C7; color: #92400E; }    /* 待确认 - 黄色 */
.status-confirmed { background: #DBEAFE; color: #1E40AF; }  /* 已确认 - 蓝色 */
.status-completed { background: #D1FAE5; color: #065F46; }  /* 已完成 - 绿色 */
.status-cancelled { background: #F3F4F6; color: #6B7280; }  /* 已取消 - 灰色 */
.status-expired { background: #FEE2E2; color: #991B1B; }    /* 已过期 - 红色 */
.status-missed { background: #FCE7F3; color: #9D174D; }     /* 用户爽约 - 粉色 */
.status-unknown { background: #E5E7EB; color: #4B5563; }    /* 未知 - 灰色 */

/* 意向弹窗样式 */
.intention-modal {
  max-width: 700px;
  max-height: 80vh;
}

.empty-intentions {
  text-align: center;
  padding: 40px;
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
}

.no-intention {
  color: #c00 !important;
  font-style: italic;
}

/* 页面头部 */
.page-header {
  margin-bottom: 20px;
  padding: 16px 24px;
  background-color: #1e3a5f;
  border-radius: 0; /* 方形圆角 */
}

.page-header h2 {
  margin: 0;
  color: white;
  font-size: 1.5rem;
  font-weight: 500;
}

.page-description {
  color: rgba(255, 255, 255, 0.8);
  margin: 4px 0 0 0;
  font-size: 13px;
}

.matching-container {
  display: grid;
  grid-template-columns: 1fr 280px 1fr; /* 操作面板固定宽度 */
  gap: 24px;
  height: calc(100vh - 140px); /* 适配头部高度 */
  align-items: stretch;
}

/* 面板样式 */
.panel {
  background: white;
  border-radius: 0; /* 方形圆角 */
  box-shadow: none; /* 扁平 */
  display: flex;
  flex-direction: column;
  border: 1px solid #ddd;
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  background: #f9f9f9;
}

.panel-header h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 14px;
  font-weight: 600;
}

.filter-controls {
  display: flex;
  gap: 8px;
}

.search-input, .status-select {
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 0; /* 方形圆角 */
  flex: 1;
  font-size: 12px;
}

.search-input:focus, .status-select:focus {
  outline: none;
  border-color: #1e3a5f;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fff;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 13px;
  font-style: italic;
}

/* 项目列表 */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-card {
  border: 1px solid #eee;
  border-radius: 0; /* 方形圆角 */
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
  position: relative;
}

.item-card:hover {
  border-color: #1e3a5f;
}

.item-card.selected {
  border-color: #1e3a5f;
  background-color: #f0f4f8;
  border-width: 2px;
  margin: -1px; /* 抵消边框宽度增加 */
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.item-header h4 {
  margin: 0;
  color: #333;
  flex: 1;
  font-size: 13px;
  font-weight: 600;
}

.status-badge {
  padding: 2px 6px;
  border-radius: 0; /* 方形圆角 */
  font-size: 11px;
  font-weight: 500;
  text-align: center;
  margin-left: 8px;
}

.status-available { background: #d4edda; color: #155724; }
.status-rented { background: #cce7ff; color: #004085; }
.status-maintenance { background: #fff3cd; color: #856404; }
.status-unknown { background: #e2e3e5; color: #383d41; }

.status-pending { background-color: #fff3cd; color: #856404; border: 1px solid #ffeaa7; }
.status-confirmed { background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }
.status-completed { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
.status-cancelled { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
.status-expired { background-color: #e2e3e5; color: #383d41; border: 1px solid #d6d8db; }
.status-missed { background-color: #f5e6e8; color: #721c24; border: 1px solid #f1b0b7; }

.item-details p {
  margin: 2px 0;
  font-size: 12px;
  color: #666;
}

.item-details strong {
  color: #444;
}

/* 操作面板 (中间) */
.action-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding-top: 20px;
  gap: 24px;
  padding: 20px 12px 0;
}

.selected-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.selected-item {
  background: white;
  border: 1px solid #1e3a5f;
  padding: 16px;
  width: 100%;
  border-radius: 0;
  text-align: center;
}

.selected-item h4 {
  margin: 0 0 8px 0;
  color: #1e3a5f;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.selected-item p {
  margin: 4px 0;
  font-size: 12px;
  color: #333;
}

.placeholder {
  border: 1px dashed #ccc;
  padding: 24px;
  width: 100%;
  text-align: center;
  color: #999;
  font-size: 13px;
  background: #fafafa;
}

.arrow {
  color: #ccc;
  font-size: 24px;
  font-weight: 300;
}

.create-contract-btn {
  background: #1e3a5f;
  color: white;
  border: none;
  padding: 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 0; /* 方形圆角 */
  box-shadow: none;
  width: 100%;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.create-contract-btn:hover:not(:disabled) {
  background: #2d5a87;
}

.create-contract-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.7;
}

/* 推荐标签 */
.recommendation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #eee;
}

.rec-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border-radius: 0;
  font-size: 10px;
  font-weight: 500;
  border: 1px solid transparent;
}

.rec-tag.match { background: #d1fae5; color: #065f46; border-color: #a7f3d0; }
.rec-tag.intent { background: #dbeafe; color: #1e40af; border-color: #bfdbfe; }
.rec-tag.contact { background: #fef3c7; color: #92400e; border-color: #fde68a; }

/* 快速联系 */
.quick-contact-actions {
  margin-top: 8px;
}

.contact-btn {
  display: block;
  width: 100%;
  text-align: center;
  padding: 6px;
  background: #10b981;
  color: white;
  text-decoration: none;
  font-size: 12px;
  border-radius: 0;
  transition: background 0.2s;
}

.contact-btn:hover {
  background: #059669;
}

/* 模态框 */
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
  backdrop-filter: blur(2px);
}

.modal {
  background: white;
  width: 90%;
  max-width: 600px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh; /* 如果内容过高则滚动 */
  border-radius: 0; /* 方形圆角 */
  box-shadow: none;
  border: 1px solid #333;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  background: #f9f9f9;
}

.modal-header h3 {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  line-height: 1;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
}

.contract-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.info-section h4 {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #1e3a5f;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-section p {
  margin: 4px 0;
  font-size: 12px;
  color: #555;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.form-group input, .form-group select, .form-group textarea {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 0; /* 方形圆角 */
  font-size: 13px;
}

.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  outline: none;
  border-color: #1e3a5f;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.readonly-input {
  background-color: #f9f9f9;
  color: #666;
}

.hint-text {
  font-size: 11px;
  color: #999;
  margin: 0;
}

.modal-footer {
  padding: 16px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #fcfcfc;
}

.cancel-btn {
  background: #eee;
  color: #333;
  border: none;
  padding: 8px 16px;
  border-radius: 0;
  font-size: 13px;
  cursor: pointer;
}

.confirm-btn {
  background: #1e3a5f;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 0;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.cancel-btn:hover { background: #e0e0e0; }
.confirm-btn:hover { background: #2d5a87; }

/* 响应式设计 */
@media (max-width: 1024px) {
  .matching-container {
    grid-template-columns: 1fr;
    height: auto;
  }
  .action-panel {
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
