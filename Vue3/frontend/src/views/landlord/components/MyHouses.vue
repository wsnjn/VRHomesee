<template>
  <div class="my-houses">
    <div class="page-header">
      <h2>我的房屋</h2>
      <div class="header-actions">
        <button @click="emit('refresh')" class="refresh-btn">刷新</button>
        <button @click="showAddHouse = true" class="add-btn">添加房屋</button>
    </div>

    <!-- 编辑房屋模态框 -->
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

      <!-- 编辑房屋模态框 -->
      <div v-if="showEditHouse" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑房屋信息</h3>
          <button @click="closeEditHouse" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmitEditHouse" class="edit-house-form">
            <div class="form-grid">
              <!-- 价格信息 -->
              <div class="form-group">
                <label for="edit-rentPrice">月租金 (元) *</label>
                <input 
                  type="number" 
                  id="edit-rentPrice" 
                  v-model="editHouseData.rentPrice" 
                  required 
                  step="0.01" 
                  placeholder="如：2500"
                >
              </div>
              <div class="form-group">
                <label for="edit-waterPrice">水费单价 (元/吨)</label>
                <input 
                  type="number" 
                  id="edit-waterPrice" 
                  v-model="editHouseData.waterPrice" 
                  step="0.01" 
                  placeholder="如：3.5"
                >
              </div>
              <div class="form-group">
                <label for="edit-electricPrice">电费单价 (元/度)</label>
                <input 
                  type="number" 
                  id="edit-electricPrice" 
                  v-model="editHouseData.electricPrice" 
                  step="0.01" 
                  placeholder="如：0.8"
                >
              </div>

              <!-- 房屋基本信息 -->
              <div class="form-group">
                <label for="edit-roomArea">房屋面积 (㎡)</label>
                <input 
                  type="number" 
                  id="edit-roomArea" 
                  v-model="editHouseData.roomArea" 
                  step="0.01" 
                  placeholder="如：85.5"
                >
              </div>
              <div class="form-group">
                <label for="edit-floorInfo">楼层信息</label>
                <input 
                  type="text" 
                  id="edit-floorInfo" 
                  v-model="editHouseData.floorInfo" 
                  placeholder="如：5/18"
                >
              </div>
              <div class="form-group">
                <label for="edit-orientation">朝向</label>
                <input 
                  type="text" 
                  id="edit-orientation" 
                  v-model="editHouseData.orientation" 
                  placeholder="如：南"
                >
              </div>
              <div class="form-group">
                <label for="edit-decoration">装修程度</label>
                <select id="edit-decoration" v-model="editHouseData.decoration">
                  <option value="1">毛坯</option>
                  <option value="2">简装</option>
                  <option value="3">精装</option>
                  <option value="4">豪装</option>
                </select>
              </div>
              <div class="form-group">
                <label for="edit-hasElevator">有无电梯</label>
                <select id="edit-hasElevator" v-model="editHouseData.hasElevator">
                  <option value="0">无</option>
                  <option value="1">有</option>
                </select>
              </div>

              <!-- 租赁信息 -->
              <div class="form-group">
                <label for="edit-rentalType">租赁类型 *</label>
                <select id="edit-rentalType" v-model="editHouseData.rentalType" required>
                  <option value="0">整租</option>
                  <option value="1">合租</option>
                  <option value="2">单间</option>
                </select>
              </div>
              <div class="form-group">
                <label for="edit-status">房屋状态 *</label>
                <select id="edit-status" v-model="editHouseData.status" required>
                  <option value="0">可租</option>
                  <option value="1">已租</option>
                  <option value="2">下架</option>
                  <option value="3">预租</option>
                </select>
              </div>

              <!-- 描述信息 -->
              <div class="form-group full-width">
                <label for="edit-description">房屋描述</label>
                <textarea 
                  id="edit-description" 
                  v-model="editHouseData.description" 
                  rows="4" 
                  placeholder="请描述房屋的装修情况、设施配备、周边环境等信息..."
                ></textarea>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button @click="closeEditHouse" class="cancel-btn">取消</button>
          <button @click="handleSubmitEditHouse" class="confirm-btn" :disabled="updatingHouse">
            {{ updatingHouse ? '更新中...' : '确认更新' }}
          </button>
        </div>
      </div>
    </div>
  </div>

    <!-- 房屋列表表格 -->
    <div class="houses-table-container">
      <div v-if="loadingHouses" class="loading">加载中...</div>
      
      <div v-else-if="myHouses.length === 0" class="no-houses">
        <div class="empty-state">
          <span class="empty-icon">🏠</span>
          <h3>暂无房屋</h3>
          <p>您还没有添加任何房屋信息</p>
          <button @click="showAddHouse = true" class="add-first-btn">添加第一套房屋</button>
        </div>
      </div>

      <div v-else class="houses-table">
        <table class="houses-table-content">
          <thead>
            <tr>
              <th>地址信息</th>
              <th>房屋信息</th>
              <th>价格信息</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="house in myHouses" 
              :key="house.id" 
              class="house-row"
              :class="getHouseStatusClass(house.status)"
            >
              <td class="address-cell">
                <div class="address-info">
                  <strong>{{ getHouseFullAddress(house) }}</strong>
                  <div class="address-details">
                    <span v-if="house.communityName">{{ house.communityName }}</span>
                    <span v-if="house.buildingUnit">{{ house.buildingUnit }}</span>
                    <span v-if="house.doorNumber">{{ house.doorNumber }}</span>
                    <span v-if="house.roomNumber">{{ house.roomNumber }}</span>
                  </div>
                </div>
              </td>
              <td class="info-cell">
                <div class="house-basic-info">
                  <div class="info-item">
                    <span class="info-label">面积：</span>
                    <span class="info-value">{{ house.roomArea || '--' }} ㎡</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">楼层：</span>
                    <span class="info-value">{{ house.floorInfo || '--' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">朝向：</span>
                    <span class="info-value">{{ house.orientation || '--' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">装修：</span>
                    <span class="info-value">{{ getDecorationText(house.decoration) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">电梯：</span>
                    <span class="info-value">{{ house.hasElevator === 1 ? '有' : '无' }}</span>
                  </div>
                </div>
              </td>
              <td class="price-cell">
                <div class="price-info">
                  <div class="main-price">
                    <strong>{{ house.rentPrice }} 元/月</strong>
                    <span class="rental-type">{{ getRentalTypeText(house.rentalType) }}</span>
                  </div>
                  <div class="utility-prices">
                    <span v-if="house.waterPrice">水费: {{ house.waterPrice }}元/吨</span>
                    <span v-if="house.electricPrice">电费: {{ house.electricPrice }}元/度</span>
                  </div>
                </div>
              </td>
              <td class="status-cell">
                <span class="house-status" :class="getStatusClass(house.status)">
                  {{ getStatusText(house.status) }}
                </span>
              </td>
              <td class="actions-cell">
                <div class="house-actions">
                  <button @click="openEditHouse(house)" class="action-btn edit-btn">编辑</button>
                  <button 
                    @click="toggleHouseStatus(house)" 
                    class="action-btn status-btn"
                    :class="getStatusBtnClass(house.status)"
                  >
                    {{ getStatusBtnText(house.status) }}
                  </button>
                  <button @click="openHouseDetail(house)" class="action-btn detail-btn">详情</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 添加房屋模态框 -->
    <div v-if="showAddHouse" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加房屋</h3>
          <button @click="showAddHouse = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="emit('submitAddHouse', newHouse)" class="add-house-form">
            <div class="form-grid">
              <!-- 地址信息 -->
              <div class="form-group">
                <label for="province">省份 *</label>
                <input 
                  type="text" 
                  id="province" 
                  v-model="newHouse.province" 
                  required 
                  placeholder="如：广东省"
                >
              </div>
              <div class="form-group">
                <label for="city">城市 *</label>
                <input 
                  type="text" 
                  id="city" 
                  v-model="newHouse.city" 
                  required 
                  placeholder="如：深圳市"
                >
              </div>
              <div class="form-group">
                <label for="district">区县 *</label>
                <input 
                  type="text" 
                  id="district" 
                  v-model="newHouse.district" 
                  required 
                  placeholder="如：南山区"
                >
              </div>
              <div class="form-group">
                <label for="street">街道 *</label>
                <input 
                  type="text" 
                  id="street" 
                  v-model="newHouse.street" 
                  required 
                  placeholder="如：科技园街道"
                >
              </div>
              <div class="form-group">
                <label for="communityName">小区名称 *</label>
                <input 
                  type="text" 
                  id="communityName" 
                  v-model="newHouse.communityName" 
                  required 
                  placeholder="如：万科城"
                >
              </div>
              <div class="form-group">
                <label for="buildingUnit">单元号</label>
                <input 
                  type="text" 
                  id="buildingUnit" 
                  v-model="newHouse.buildingUnit" 
                  placeholder="如：1单元"
                >
              </div>
              <div class="form-group">
                <label for="doorNumber">门牌号 *</label>
                <input 
                  type="text" 
                  id="doorNumber" 
                  v-model="newHouse.doorNumber" 
                  required 
                  placeholder="如：101号"
                >
              </div>
              <div class="form-group">
                <label for="roomNumber">房间号</label>
                <input 
                  type="text" 
                  id="roomNumber" 
                  v-model="newHouse.roomNumber" 
                  placeholder="如：301室"
                >
              </div>

              <!-- 房屋基本信息 -->
              <div class="form-group">
                <label for="roomArea">房屋面积 (㎡)</label>
                <input 
                  type="number" 
                  id="roomArea" 
                  v-model="newHouse.roomArea" 
                  step="0.01" 
                  placeholder="如：85.5"
                >
              </div>
              <div class="form-group">
                <label for="floorInfo">楼层信息</label>
                <input 
                  type="text" 
                  id="floorInfo" 
                  v-model="newHouse.floorInfo" 
                  placeholder="如：5/18"
                >
              </div>
              <div class="form-group">
                <label for="orientation">朝向</label>
                <input 
                  type="text" 
                  id="orientation" 
                  v-model="newHouse.orientation" 
                  placeholder="如：南"
                >
              </div>
              <div class="form-group">
                <label for="decoration">装修程度</label>
                <select id="decoration" v-model="newHouse.decoration">
                  <option value="1">毛坯</option>
                  <option value="2">简装</option>
                  <option value="3">精装</option>
                  <option value="4">豪装</option>
                </select>
              </div>
              <div class="form-group">
                <label for="hasElevator">有无电梯</label>
                <select id="hasElevator" v-model="newHouse.hasElevator">
                  <option value="0">无</option>
                  <option value="1">有</option>
                </select>
              </div>

              <!-- 价格信息 -->
              <div class="form-group">
                <label for="rentPrice">月租金 (元) *</label>
                <input 
                  type="number" 
                  id="rentPrice" 
                  v-model="newHouse.rentPrice" 
                  required 
                  step="0.01" 
                  placeholder="如：2500"
                >
              </div>
              <div class="form-group">
                <label for="waterPrice">水费单价 (元/吨)</label>
                <input 
                  type="number" 
                  id="waterPrice" 
                  v-model="newHouse.waterPrice" 
                  step="0.01" 
                  placeholder="如：3.5"
                >
              </div>
              <div class="form-group">
                <label for="electricPrice">电费单价 (元/度)</label>
                <input 
                  type="number" 
                  id="electricPrice" 
                  v-model="newHouse.electricPrice" 
                  step="0.01" 
                  placeholder="如：0.8"
                >
              </div>

              <!-- 租赁信息 -->
              <div class="form-group">
                <label for="rentalType">租赁类型 *</label>
                <select id="rentalType" v-model="newHouse.rentalType" required>
                  <option value="0">整租</option>
                  <option value="1">合租</option>
                  <option value="2">单间</option>
                </select>
              </div>
              <div class="form-group">
                <label for="status">房屋状态 *</label>
                <select id="status" v-model="newHouse.status" required>
                  <option value="0">可租</option>
                  <option value="1">已租</option>
                  <option value="2">下架</option>
                  <option value="3">预租</option>
                </select>
              </div>

              <!-- 描述信息 -->
              <div class="form-group full-width">
                <label for="description">房屋描述</label>
                <textarea 
                  id="description" 
                  v-model="newHouse.description" 
                  rows="4" 
                  placeholder="请描述房屋的装修情况、设施配备、周边环境等信息..."
                ></textarea>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button @click="showAddHouse = false" class="cancel-btn">取消</button>
          <button @click="handleSubmitAddHouse" class="confirm-btn" :disabled="addingHouse">
            {{ addingHouse ? '添加中...' : '确认添加' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const props = defineProps({
  myHouses: {
    type: Array,
    default: () => []
  },
  loadingHouses: {
    type: Boolean,
    default: false
  },
  addingHouse: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'refresh',
  'editHouse',
  'viewHouseDetail',
  'submitAddHouse'
])

const showAddHouse = ref(false)
const showEditHouse = ref(false)
const editingHouse = ref(null)
const updatingHouse = ref(false)

// 房屋详情相关
const showHouseDetail = ref(false)
const loadingHouseDetail = ref(false)
const houseDetailData = ref(null)
const houseDetailError = ref('')

// 编辑房屋数据
const editHouseData = ref({
  rentPrice: null,
  waterPrice: null,
  electricPrice: null,
  status: '0',
  roomArea: null,
  floorInfo: '',
  orientation: '',
  decoration: '2',
  hasElevator: '0',
  rentalType: '0',
  description: ''
})

// 打开编辑房屋模态框
const openEditHouse = (house) => {
  editingHouse.value = house
  editHouseData.value = {
    rentPrice: house.rentPrice,
    waterPrice: house.waterPrice,
    electricPrice: house.electricPrice,
    status: house.status.toString(),
    roomArea: house.roomArea,
    floorInfo: house.floorInfo || '',
    orientation: house.orientation || '',
    decoration: house.decoration ? house.decoration.toString() : '2',
    hasElevator: house.hasElevator ? house.hasElevator.toString() : '0',
    rentalType: house.rentalType ? house.rentalType.toString() : '0',
    description: house.description || ''
  }
  showEditHouse.value = true
}

// 提交编辑房屋
const handleSubmitEditHouse = async () => {
  if (!editingHouse.value) return
  
  updatingHouse.value = true
  try {
    const updateData = {
      rentPrice: parseFloat(editHouseData.value.rentPrice),
      waterPrice: editHouseData.value.waterPrice ? parseFloat(editHouseData.value.waterPrice) : null,
      electricPrice: editHouseData.value.electricPrice ? parseFloat(editHouseData.value.electricPrice) : null,
      status: parseInt(editHouseData.value.status),
      roomArea: editHouseData.value.roomArea ? parseFloat(editHouseData.value.roomArea) : null,
      floorInfo: editHouseData.value.floorInfo,
      orientation: editHouseData.value.orientation,
      decoration: parseInt(editHouseData.value.decoration),
      hasElevator: parseInt(editHouseData.value.hasElevator),
      rentalType: parseInt(editHouseData.value.rentalType),
      description: editHouseData.value.description
    }

    const response = await axios.put(`${API_BASE_URL}/landlord/house/${editingHouse.value.id}`, updateData)
    
    if (response.data.success) {
      alert('房屋信息更新成功！')
      showEditHouse.value = false
      // 重新加载房屋列表
      emit('refresh')
    } else {
      alert(`更新失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('更新房屋信息失败:', error)
    alert('更新房屋信息失败，请稍后重试')
  } finally {
    updatingHouse.value = false
  }
}

// 关闭编辑模态框
const closeEditHouse = () => {
  showEditHouse.value = false
  editingHouse.value = null
  editHouseData.value = {
    rentPrice: null,
    waterPrice: null,
    electricPrice: null,
    status: '0',
    roomArea: null,
    floorInfo: '',
    orientation: '',
    decoration: '2',
    hasElevator: '0',
    rentalType: '0',
    description: ''
  }
}

// 新房屋数据
const newHouse = ref({
  province: '',
  city: '',
  district: '',
  street: '',
  communityName: '',
  buildingUnit: '',
  doorNumber: '',
  roomNumber: '',
  roomArea: null,
  floorInfo: '',
  orientation: '',
  decoration: '2', // 默认简装
  hasElevator: '0', // 默认无电梯
  rentPrice: null,
  waterPrice: 3.5, // 默认水费
  electricPrice: 0.8, // 默认电费
  rentalType: '0', // 默认整租
  status: '0', // 默认可租
  description: ''
})

// 处理提交添加房屋
const handleSubmitAddHouse = () => {
  emit('submitAddHouse', newHouse.value)
  // 重置表单
  resetNewHouseForm()
  showAddHouse.value = false
}

// 重置新房屋表单
const resetNewHouseForm = () => {
  newHouse.value = {
    province: '',
    city: '',
    district: '',
    street: '',
    communityName: '',
    buildingUnit: '',
    doorNumber: '',
    roomNumber: '',
    roomArea: null,
    floorInfo: '',
    orientation: '',
    decoration: '2',
    hasElevator: '0',
    rentPrice: null,
    waterPrice: 3.5,
    electricPrice: 0.8,
    rentalType: '0',
    status: '0',
    description: ''
  }
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

// 获取房屋状态样式类
const getHouseStatusClass = (status) => {
  const classMap = {
    0: 'house-available',
    1: 'house-rented',
    2: 'house-offline',
    3: 'house-pre-rent'
  }
  return classMap[status] || 'house-unknown'
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
  const typeMap = {
    0: '整租',
    1: '合租',
    2: '单间'
  }
  return typeMap[rentalType] || '未知'
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
</script>

<style scoped>
.my-houses {
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

.refresh-btn, .add-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.refresh-btn {
  background-color: #6c757d;
  color: white;
}

.refresh-btn:hover {
  background-color: #5a6268;
}

.add-btn {
  background-color: #28a745;
  color: white;
}

.add-btn:hover {
  background-color: #218838;
}

/* 房屋列表表格样式 */
.houses-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
  font-size: 1.1rem;
}

.no-houses {
  padding: 4rem 2rem;
}

.empty-state {
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
}

.empty-state p {
  color: #6c757d;
  margin-bottom: 2rem;
}

.add-first-btn {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s;
}

.add-first-btn:hover {
  background-color: #218838;
}

.houses-table {
  width: 100%;
}

.houses-table-content {
  width: 100%;
  border-collapse: collapse;
}

.houses-table-content th {
  background-color: #f8f9fa;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #e9ecef;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.houses-table-content td {
  padding: 1rem;
  border-bottom: 1px solid #f8f9fa;
  vertical-align: top;
}

.house-row {
  transition: background-color 0.3s;
}

.house-row:hover {
  background-color: #f8f9fa;
}

.house-row:last-child td {
  border-bottom: none;
}

/* 房屋状态行样式 */
.house-available {
  border-left: 4px solid #28a745;
}

.house-rented {
  border-left: 4px solid #dc3545;
}

.house-offline {
  border-left: 4px solid #6c757d;
}

.house-pre-rent {
  border-left: 4px solid #ffc107;
}

.house-unknown {
  border-left: 4px solid #6c757d;
}

/* 表格单元格样式 */
.address-cell {
  min-width: 200px;
}

.address-info strong {
  display: block;
  margin-bottom: 0.5rem;
  color: #2c3e50;
  font-size: 1rem;
}

.address-details {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #6c757d;
}

.address-details span {
  background: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.info-cell {
  min-width: 180px;
}

.house-basic-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.info-label {
  color: #6c757d;
  font-weight: 500;
  min-width: 50px;
}

.info-value {
  color: #2c3e50;
  font-weight: 600;
  text-align: right;
}

.price-cell {
  min-width: 150px;
}

.price-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.main-price {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.main-price strong {
  color: #dc3545;
  font-size: 1.1rem;
}

.rental-type {
  background: #28a745;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
}

.utility-prices {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: #6c757d;
}

.status-cell {
  min-width: 80px;
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

.actions-cell {
  min-width: 180px;
}

.house-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
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
  width: 100%;
  max-width: 600px;
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

.modal-footer {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
}

.cancel-btn, .confirm-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.confirm-btn {
  background-color: #28a745;
  color: white;
}

.confirm-btn:hover {
  background-color: #218838;
}

.confirm-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

/* 表单样式 */
.add-house-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #28a745;
  box-shadow: 0 0 0 2px rgba(40, 167, 69, 0.2);
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .houses-table-content {
    display: block;
    overflow-x: auto;
  }
  
  .house-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .modal-content {
    margin: 1rem;
  }
  
  .modal-body {
    padding: 1rem;
  }
  
  .modal-footer {
    flex-direction: column;
  }
}
</style>
