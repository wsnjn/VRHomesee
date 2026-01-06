<!--
/**
 * 文件名: MyTenants.vue
 * 作者: 牛迦楠
 * 学校: 东华理工大学
 * 专业: 软件工程（中外合作办学）
 * 课题: 融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 创建日期: 2026-01-06
 * 描述: 我的租户组件，展示当前租户信息和租约详情
 */
-->
<template>
  <div class="my-tenants">

    <!-- 统计信息 -->
    <div class="stats-grid" v-if="statistics">
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ statistics.totalTenants || 0 }}</span>
          <span class="stat-label">总租户数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
            <polyline points="9 22 9 12 15 12 15 22"></polyline>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ statistics.rentedHouses || 0 }}</span>
          <span class="stat-label">已出租房屋</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ statistics.expiringContracts || 0 }}</span>
          <span class="stat-label">即将到期</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="1" x2="12" y2="23"></line>
            <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">¥{{ statistics.monthlyIncome || 0 }}</span>
          <span class="stat-label">月租金收入</span>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="search-box">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索租户姓名、电话或房屋地址..." 
          @input="filterTenants"
        />
      </div>
      <div class="filter-controls">
        <select v-model="statusFilter" @change="filterTenants">
          <option value="">全部状态</option>
          <option value="1">已签约</option>
          <option value="2">履行中</option>
          <option value="3">已到期</option>
          <option value="4">提前解约</option>
          <option value="5">已退租</option>
        </select>
        <select v-model="rentStatusFilter" @change="filterTenants">
          <option value="">全部租金状态</option>
          <option value="0">未付款</option>
          <option value="1">已付款</option>
          <option value="2">逾期</option>
          <option value="3">部分付款</option>
        </select>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-section">
      <div class="spinner"></div>
      <p>正在加载租户信息...</p>
    </div>

    <!-- 租户列表 -->
    <div v-else class="tenants-list">
      <div v-if="filteredTenants.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
          <circle cx="9" cy="7" r="4"></circle>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
        </svg>
        <p>{{ searchQuery || statusFilter || rentStatusFilter ? '暂无符合条件的租户' : '暂无租户信息' }}</p>
      </div>

      <div v-else class="tenant-cards">
        <div 
          v-for="tenant in filteredTenants" 
          :key="tenant.id"
          class="tenant-card"
        >
          <div class="tenant-header">
            <div class="tenant-info">
              <h4 class="tenant-name">{{ tenant.tenantName || '未知租户' }}</h4>
              <p class="tenant-phone">{{ formatPhoneNumber(tenant.tenantPhone) }}</p>
            </div>
            <div class="contract-status">
              <span class="status-tag" :class="getContractStatusClass(tenant.contractStatus)">
                {{ getContractStatusText(tenant.contractStatus) }}
              </span>
              <span class="rent-status" :class="getRentStatusClass(tenant.rentStatus)">
                {{ getRentStatusText(tenant.rentStatus) }}
              </span>
            </div>
          </div>

          <div class="house-info">
            <h5 class="house-title">{{ getHouseAddress(tenant) }}</h5>
            <div class="house-details">
              <span class="detail-item">{{ tenant.roomArea }}㎡</span>
              <span class="detail-divider">|</span>
              <span class="detail-item">{{ getRentalTypeText(tenant.rentalType) }}</span>
              <span class="detail-divider">|</span>
              <span class="detail-item">{{ getDecorationText(tenant.decoration) }}</span>
            </div>
          </div>

          <div class="contract-info">
            <div class="contract-dates">
              <div class="date-item">
                <span class="date-label">租期</span>
                <span class="date-value">{{ formatDate(tenant.contractStartDate) }} 至 {{ formatDate(tenant.contractEndDate) }}</span>
              </div>
              <div class="date-item">
                <span class="date-label">剩余天数</span>
                <span class="date-value" :class="getDaysLeftClass(tenant.contractEndDate)">
                  {{ calculateDaysLeft(tenant.contractEndDate) }}天
                </span>
              </div>
            </div>
            <div class="financial-info">
              <div class="amount-item">
                <span class="amount-label">月租金</span>
                <span class="amount-value">¥{{ tenant.monthlyRent }}</span>
              </div>
              <div class="amount-item">
                <span class="amount-label">押金</span>
                <span class="amount-value">¥{{ tenant.depositAmount }}</span>
              </div>
            </div>
          </div>

          <div class="tenant-actions">
            <button class="action-btn contact-btn" @click="contactTenant(tenant)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
              </svg>
              联系租户
            </button>
            <button class="action-btn details-btn" @click="viewContractDetails(tenant)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
              合同详情
            </button>
          </div>

          <!-- 紧急联系人信息 -->
          <div v-if="tenant.emergencyContact" class="emergency-contact">
            <div class="emergency-label">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              紧急联系人
            </div>
            <div class="emergency-info">
              <span>{{ tenant.emergencyContact }}</span>
              <span class="emergency-phone">{{ formatPhoneNumber(tenant.emergencyPhone) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 合同详情弹窗 -->
    <div v-if="showContractModal" class="modal-overlay" @click.self="closeContractModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>合同详情</h3>
          <button class="close-btn" @click="closeContractModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-body" v-if="selectedContract">
          <!-- 基本信息 -->
          <div class="detail-section">
            <h4 class="section-title">基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">合同编号</span>
                <span class="value">{{ selectedContract.contractNumber }}</span>
              </div>
              <div class="detail-item">
                <span class="label">合同状态</span>
                <div class="status-edit">
                  <select v-model="editingStatuses.contractStatus" class="status-select">
                    <option :value="0">待签约</option>
                    <option :value="1">已签约</option>
                    <option :value="2">履行中</option>
                    <option :value="3">已到期</option>
                    <option :value="4">提前解约</option>
                    <option :value="5">已退租</option>
                  </select>
                  <button class="save-icon-btn" @click="saveStatusChange('contract')" title="保存状态" :disabled="savingStatus">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path><polyline points="17 21 17 13 7 13 7 21"></polyline><polyline points="7 3 7 8 15 8"></polyline></svg>
                  </button>
                </div>
              </div>
              <div class="detail-item">
                <span class="label">签约时间</span>
                <span class="value">{{ formatDate(selectedContract.contractSignedTime) }}</span>
              </div>
            </div>
          </div>

          <!-- 房屋信息 -->
          <div class="detail-section">
            <h4 class="section-title">房屋信息</h4>
            <div class="detail-grid">
              <div class="detail-item full-width">
                <span class="label">房屋地址</span>
                <span class="value">{{ getHouseAddress(selectedContract) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">租赁类型</span>
                <span class="value">{{ getRentalTypeText(selectedContract.rentalType) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">装修程度</span>
                <span class="value">{{ getDecorationText(selectedContract.decoration) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">房屋面积</span>
                <span class="value">{{ selectedContract.roomArea }}㎡</span>
              </div>
              <div class="detail-item">
                <span class="label">楼层信息</span>
                <span class="value">{{ selectedContract.floorInfo || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">电梯</span>
                <span class="value">{{ selectedContract.hasElevator === 1 ? '有' : '无' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">朝向</span>
                <span class="value">{{ selectedContract.orientation || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 租客信息 -->
          <div class="detail-section">
            <h4 class="section-title">租客信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">租客姓名</span>
                <span class="value">{{ selectedContract.tenantName || '未知' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">联系电话</span>
                <span class="value">{{ formatPhoneNumber(selectedContract.tenantPhone) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">紧急联系人</span>
                <span class="value">{{ selectedContract.emergencyContact || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">紧急电话</span>
                <span class="value">{{ formatPhoneNumber(selectedContract.emergencyPhone) || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 租期信息 -->
          <div class="detail-section">
            <h4 class="section-title">租期信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">起止日期</span>
                <span class="value">{{ formatDate(selectedContract.contractStartDate) }} 至 {{ formatDate(selectedContract.contractEndDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">实际入住</span>
                <span class="value">{{ formatDate(selectedContract.actualMoveInDate) || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">实际搬离</span>
                <span class="value">{{ formatDate(selectedContract.actualMoveOutDate) || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">剩余天数</span>
                <span class="value" :class="getDaysLeftClass(selectedContract.contractEndDate)">
                  {{ calculateDaysLeft(selectedContract.contractEndDate) }}天
                </span>
              </div>
            </div>
          </div>

          <!-- 财务信息 -->
          <div class="detail-section">
            <h4 class="section-title">财务信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">月租金</span>
                <span class="value highlight">¥{{ selectedContract.monthlyRent }}</span>
              </div>
              <div class="detail-item">
                <span class="label">押金</span>
                <span class="value">¥{{ selectedContract.depositAmount }}</span>
              </div>
              <div class="detail-item">
                <span class="label">支付周期</span>
                <span class="value">{{ selectedContract.paymentCycle }}个月/付</span>
              </div>
              <div class="detail-item">
                <span class="label">租金状态</span>
                <div class="status-edit">
                  <select v-model="editingStatuses.rentStatus" class="status-select">
                    <option :value="0">未付款</option>
                    <option :value="1">已付款</option>
                    <option :value="2">逾期</option>
                    <option :value="3">部分付款</option>
                  </select>
                  <button class="save-icon-btn" @click="saveStatusChange('rent')" title="保存状态" :disabled="savingStatus">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path><polyline points="17 21 17 13 7 13 7 21"></polyline><polyline points="7 3 7 8 15 8"></polyline></svg>
                  </button>
                </div>
              </div>
              <div class="detail-item">
                <span class="label">押金状态</span>
                <div class="status-edit">
                  <select v-model="editingStatuses.depositStatus" class="status-select">
                    <option :value="0">未付</option>
                    <option :value="1">已付</option>
                    <option :value="2">已退</option>
                    <option :value="3">抵扣中</option>
                  </select>
                  <button class="save-icon-btn" @click="saveStatusChange('deposit')" title="保存状态" :disabled="savingStatus">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path><polyline points="17 21 17 13 7 13 7 21"></polyline><polyline points="7 3 7 8 15 8"></polyline></svg>
                  </button>
                </div>
              </div>
              <div class="detail-item">
                <span class="label">水费单价</span>
                <span class="value">{{ selectedContract.waterPrice ? `¥${selectedContract.waterPrice}/吨` : '包含在租金内' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">电费单价</span>
                <span class="value">{{ selectedContract.electricPrice ? `¥${selectedContract.electricPrice}/度` : '包含在租金内' }}</span>
              </div>
            </div>
          </div>

          <!-- 水电读数 -->
          <div class="detail-section">
            <h4 class="section-title">水电读数</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">水表初始</span>
                <span class="value">{{ selectedContract.waterInitialReading || 0 }}</span>
              </div>
              <div class="detail-item">
                <span class="label">水表最新</span>
                <div class="edit-reading">
                  <input 
                    type="number" 
                    v-model="editingReadings.water" 
                    class="reading-input"
                    placeholder="输入读数"
                  />
                </div>
              </div>
              <div class="detail-item">
                <span class="label">电表初始</span>
                <span class="value">{{ selectedContract.electricInitialReading || 0 }}</span>
              </div>
              <div class="detail-item">
                <span class="label">电表最新</span>
                <div class="edit-reading">
                  <input 
                    type="number" 
                    v-model="editingReadings.electric" 
                    class="reading-input"
                    placeholder="输入读数"
                  />
                </div>
              </div>
            </div>
            <div class="reading-actions">
              <button class="action-btn save-btn" @click="saveMeterReadings" :disabled="savingReadings">
                {{ savingReadings ? '保存中...' : '保存读数' }}
              </button>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="action-btn download-btn" @click="openPdfPreview">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            下载合同 PDF
          </button>
          <button class="action-btn contact-btn" @click="contactTenant(selectedContract)">
            联系租户
          </button>
          <button class="action-btn secondary-btn" @click="closeContractModal">
            关闭
          </button>
        </div>
      </div>
    </div>

    <!-- PDF 预览弹窗 -->
    <div v-if="showPdfPreview" class="modal-overlay pdf-modal" @click.self="closePdfPreview">
      <div class="modal-content pdf-preview-content">
        <div class="modal-header">
          <h3>合同PDF预览</h3>
          <button class="close-btn" @click="closePdfPreview">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="pdf-preview-body">
          <ContractTemplate 
            v-if="selectedContract"
            :contract="selectedContract"
            :room="selectedContract"
            :landlord="landlordInfo"
            :tenant="{ realName: selectedContract.tenantName, phone: selectedContract.tenantPhone }"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import ContractTemplate from '@/components/ContractTemplate.vue'

const props = defineProps({
  userPhone: {
    type: String,
    required: true
  },
  userId: {
    type: Number,
    default: null
  }
})

// API基础URL
const API_BASE_URL = 'https://api.homesee.xyz/api'

// 响应式数据
const loading = ref(false)
const tenants = ref([])
const statistics = ref({})
const searchQuery = ref('')
const statusFilter = ref('')
const rentStatusFilter = ref('')

// 加载租户数据
const loadTenants = async () => {
  if (!props.userPhone && !props.userId) {
    console.error('用户手机号或ID不存在')
    return
  }

  loading.value = true
  try {
    let url = `${API_BASE_URL}/landlord/tenants?landlordPhone=${props.userPhone}`
    
    // 如果有userId，优先使用新的API endpoint
    if (props.userId) {
      url = `${API_BASE_URL}/admin/tenant/landlord/${props.userId}`
    }

    const response = await axios.get(url)
    if (response.data.success) {
      // 新接口返回的是 { success: true, contracts: [...] }
      // 旧接口返回的是 { success: true, tenants: [...], statistics: ... }
      // 需要适配两种格式
      
      if (response.data.contracts) {
        tenants.value = response.data.contracts
        // 统计信息可能需要另外获取或从contracts计算
        statistics.value = {
          totalTenants: tenants.value.length,
          rentedHouses: tenants.value.filter(t => t.contractStatus === 1 || t.contractStatus === 2).length,
          expiringContracts: tenants.value.filter(t => calculateDaysLeft(t.contractEndDate) <= 30).length,
          monthlyIncome: tenants.value.reduce((sum, t) => sum + (t.monthlyRent || 0), 0)
        }
      } else {
        tenants.value = response.data.tenants || []
        statistics.value = response.data.statistics || {}
      }
    } else {
      console.error('获取租户列表失败:', response.data.message)
      tenants.value = []
    }
  } catch (error) {
    console.error('加载租户列表失败:', error)
    tenants.value = []
  } finally {
    loading.value = false
  }
}

// 过滤租户列表
const filteredTenants = computed(() => {
  let filtered = tenants.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(tenant => 
      (tenant.tenantName && tenant.tenantName.toLowerCase().includes(query)) ||
      (tenant.tenantPhone && tenant.tenantPhone.includes(query)) ||
      getHouseAddress(tenant).toLowerCase().includes(query)
    )
  }

  // 状态过滤
  if (statusFilter.value) {
    filtered = filtered.filter(tenant => tenant.contractStatus === parseInt(statusFilter.value))
  }

  // 租金状态过滤
  if (rentStatusFilter.value) {
    filtered = filtered.filter(tenant => tenant.rentStatus === parseInt(rentStatusFilter.value))
  }

  return filtered
})

// 格式化电话号码
const formatPhoneNumber = (phone) => {
  if (!phone) return ''
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`
  }
  return phone
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 计算剩余天数
const calculateDaysLeft = (endDate) => {
  if (!endDate) return 0
  const today = new Date()
  const end = new Date(endDate)
  const diffTime = end - today
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays > 0 ? diffDays : 0
}

// 获取剩余天数样式类
const getDaysLeftClass = (endDate) => {
  const daysLeft = calculateDaysLeft(endDate)
  if (daysLeft <= 30) return 'days-critical'
  if (daysLeft <= 90) return 'days-warning'
  return 'days-normal'
}

// 获取合同状态文字
const getContractStatusText = (status) => {
  const map = { 
    0: '待签约', 
    1: '已签约', 
    2: '履行中', 
    3: '已到期', 
    4: '提前解约', 
    5: '已退租' 
  }
  return map[status] || '未知状态'
}

// 获取合同状态样式类
const getContractStatusClass = (status) => {
  const map = { 
    0: 'status-pending', 
    1: 'status-signed', 
    2: 'status-active', 
    3: 'status-expired', 
    4: 'status-terminated', 
    5: 'status-moved-out' 
  }
  return map[status] || ''
}

// 获取租金状态文字
const getRentStatusText = (status) => {
  const map = { 
    0: '未付款', 
    1: '已付款', 
    2: '逾期', 
    3: '部分付款' 
  }
  return map[status] || '未知状态'
}

// 获取租金状态样式类
const getRentStatusClass = (status) => {
  const map = { 
    0: 'rent-unpaid', 
    1: 'rent-paid', 
    2: 'rent-overdue', 
    3: 'rent-partial' 
  }
  return map[status] || ''
}

// 获取租赁类型文字
const getRentalTypeText = (type) => {
  const types = { 0: '整租', 1: '合租', 2: '单间' }
  return types[type] || '未知'
}

// 获取装修程度文字
const getDecorationText = (decoration) => {
  const types = { 1: '毛坯', 2: '简装', 3: '精装', 4: '豪装' }
  return types[decoration] || '未知'
}

// 获取房屋地址
const getHouseAddress = (tenant) => {
  if (!tenant) return ''
  const parts = [
    tenant.province,
    tenant.city,
    tenant.district,
    tenant.street,
    tenant.communityName,
    tenant.buildingUnit,
    tenant.doorNumber,
    tenant.roomNumber
  ].filter(part => part && part.trim() !== '')
  
  return parts.join(' ')
}

// 过滤租户（用于搜索和筛选）
const filterTenants = () => {
  // 计算属性会自动处理过滤，这里只是占位函数
}

// 联系租户
const contactTenant = (tenant) => {
  if (tenant.tenantPhone) {
    alert(`联系租户: ${tenant.tenantName}\n电话: ${tenant.tenantPhone}`)
  } else {
    alert('该租户未提供联系电话')
  }
}

// 查看合同详情
const showContractModal = ref(false)
const selectedContract = ref(null)
const editingReadings = ref({
  water: '',
  electric: ''
})
const editingStatuses = ref({
  contractStatus: '',
  rentStatus: '',
  depositStatus: ''
})
const savingReadings = ref(false)
const savingStatus = ref(false)

const viewContractDetails = (tenant) => {
  selectedContract.value = tenant
  editingReadings.value = {
    water: tenant.lastWaterReading || '',
    electric: tenant.lastElectricReading || ''
  }
  editingStatuses.value = {
    contractStatus: tenant.contractStatus,
    rentStatus: tenant.rentStatus,
    depositStatus: tenant.depositStatus
  }
  showContractModal.value = true
}

const closeContractModal = () => {
  showContractModal.value = false
  selectedContract.value = null
  editingReadings.value = { water: '', electric: '' }
  editingStatuses.value = { contractStatus: '', rentStatus: '', depositStatus: '' }
}

// PDF 预览相关
const showPdfPreview = ref(false)
const landlordInfo = ref({})

const openPdfPreview = () => {
  // 从 localStorage 获取房东信息
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  landlordInfo.value = {
    realName: user.realName || user.username || '房东',
    phone: user.phone || '',
    idNumber: user.idNumber || ''
  }
  showPdfPreview.value = true
}

const closePdfPreview = () => {
  showPdfPreview.value = false
}

// 保存状态更改
const saveStatusChange = async (type) => {
  if (!selectedContract.value) return
  
  savingStatus.value = true
  try {
    let url = ''
    let data = {}
    let successMsg = ''
    
    if (type === 'contract') {
      url = `${API_BASE_URL}/admin/tenant/${selectedContract.value.id}/status`
      data = { status: editingStatuses.value.contractStatus }
      successMsg = '合同状态更新成功'
    } else if (type === 'rent') {
      url = `${API_BASE_URL}/admin/tenant/${selectedContract.value.id}/rent-status`
      data = { rentStatus: editingStatuses.value.rentStatus }
      successMsg = '租金状态更新成功'
    } else if (type === 'deposit') {
      url = `${API_BASE_URL}/admin/tenant/${selectedContract.value.id}/deposit-status`
      data = { depositStatus: editingStatuses.value.depositStatus }
      successMsg = '押金状态更新成功'
    }
    
    const response = await axios.put(url, data)
    
    if (response.data.success) {
      alert(successMsg)
      // 更新本地数据
      if (type === 'contract') selectedContract.value.contractStatus = editingStatuses.value.contractStatus
      if (type === 'rent') selectedContract.value.rentStatus = editingStatuses.value.rentStatus
      if (type === 'deposit') selectedContract.value.depositStatus = editingStatuses.value.depositStatus
      
      // 更新列表中的数据
      const index = tenants.value.findIndex(t => t.id === selectedContract.value.id)
      if (index !== -1) {
        if (type === 'contract') tenants.value[index].contractStatus = editingStatuses.value.contractStatus
        if (type === 'rent') tenants.value[index].rentStatus = editingStatuses.value.rentStatus
        if (type === 'deposit') tenants.value[index].depositStatus = editingStatuses.value.depositStatus
      }
    } else {
      alert('更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    alert('更新失败，请稍后重试')
  } finally {
    savingStatus.value = false
  }
}

// 保存水电读数
const saveMeterReadings = async () => {
  if (!selectedContract.value) return
  
  savingReadings.value = true
  try {
    const response = await axios.post(`${API_BASE_URL}/admin/tenant/meter-readings`, {
      contractId: selectedContract.value.id,
      waterReading: editingReadings.value.water,
      electricReading: editingReadings.value.electric
    })
    
    if (response.data.success) {
      alert('水电读数更新成功')
      // 更新本地数据
      selectedContract.value.lastWaterReading = editingReadings.value.water
      selectedContract.value.lastElectricReading = editingReadings.value.electric
      
      // 更新列表中的数据
      const index = tenants.value.findIndex(t => t.id === selectedContract.value.id)
      if (index !== -1) {
        tenants.value[index].lastWaterReading = editingReadings.value.water
        tenants.value[index].lastElectricReading = editingReadings.value.electric
      }
    } else {
      alert('更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新水电读数失败:', error)
    alert('更新失败，请稍后重试')
  } finally {
    savingReadings.value = false
  }
}

// 页面加载时初始化数据
onMounted(() => {
  loadTenants()
})
</script>

<style scoped>
.my-tenants {
  padding: 0;
  font-family: 'Inter', sans-serif;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 1.5rem;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px 0;
}

.page-description {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

/* 统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px; /* 紧凑 */
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 16px;
  border-radius: 0; /* 直角 */
  box-shadow: none; /* 扁平 */
  border: 1px solid #ddd;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: border-color 0.2s;
}

.stat-card:hover {
  border-color: #1e3a5f;
}

.stat-icon {
  width: 40px;
  height: 40px;
  background-color: #f0f2f5;
  color: #1e3a5f;
  border-radius: 0; /* 直角 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.8rem;
  color: #888;
  font-weight: 500;
}

/* 筛选区域 */
.filter-section {
  background: white;
  padding: 12px 16px; /* 紧凑 */
  border-radius: 0; /* 直角 */
  box-shadow: none;
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  align-items: center;
  border: 1px solid #ddd;
}

.search-box {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.search-box svg {
  position: absolute;
  left: 10px;
  color: #999;
  width: 16px;
  height: 16px;
}

.search-box input {
  width: 100%;
  padding: 8px 12px 8px 32px; /* 紧凑 */
  border: 1px solid #ddd;
  border-radius: 0; /* 直角 */
  font-size: 13px;
  transition: border-color 0.2s;
}

.search-box input:focus {
  outline: none;
  border-color: #1e3a5f;
}

.filter-controls {
  display: flex;
  gap: 8px;
}

.filter-controls select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 0; /* 直角 */
  background: white;
  font-size: 13px;
  cursor: pointer;
  min-width: 120px;
}

.filter-controls select:focus {
  outline: none;
  border-color: #1e3a5f;
}

/* 加载与空状态 */
.loading-section, .empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
  border: 1px dashed #ddd; /* 工程风格 */
  background: #fcfcfc;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #eee;
  border-top: 3px solid #1e3a5f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 租户卡片 */
.tenant-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tenant-card {
  background: white;
  border-radius: 0; /* 直角 */
  box-shadow: none; /* 扁平 */
  border: 1px solid #eee;
  padding: 0; /* 重置 */
  transition: border-color 0.2s;
}

.tenant-card:hover {
  border-color: #1e3a5f;
}

.tenant-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px 16px;
  background: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.tenant-info h4 {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
}

.tenant-phone {
  margin: 2px 0 0 0;
  color: #666;
  font-size: 0.85rem;
}

.contract-status {
  display: flex;
  gap: 8px;
  align-items: center;
}

.status-tag, .rent-status {
  padding: 2px 8px;
  border-radius: 0; /* 直角 */
  font-size: 12px;
  font-weight: 500;
  border: 1px solid transparent; /* 确保边框对齐 */
}

/* 状态颜色（扁平） */
.status-pending { background: #fff3cd; color: #856404; border-color: #ffeeba; }
.status-signed { background: #d1ecf1; color: #0c5460; border-color: #bee5eb; }
.status-active { background: #d4edda; color: #155724; border-color: #c3e6cb; }
.status-expired { background: #e2e3e5; color: #383d41; border-color: #d6d8db; }
.status-terminated { background: #f8d7da; color: #721c24; border-color: #f5c6cb; }
.status-moved-out { background: #fcfcfc; color: #666; border-color: #ddd; }

.rent-unpaid { background: #f8d7da; color: #721c24; border-color: #f5c6cb; }
.rent-paid { background: #d4edda; color: #155724; border-color: #c3e6cb; }
.rent-overdue { background: #fff3cd; color: #856404; border-color: #ffeeba; }
.rent-partial { background: #d1ecf1; color: #0c5460; border-color: #bee5eb; }

/* 内容区域 */
.house-info, .contract-info, .emergency-contact {
  padding: 12px 16px;
}

.house-info {
  border-bottom: 1px solid #eee;
}

.house-title {
  margin: 0 0 8px 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
}

.house-details {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.detail-divider {
  color: #ddd;
}

.contract-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  border-bottom: 1px solid #eee;
}

.date-item, .amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.date-label, .amount-label {
  font-size: 12px;
  color: #888;
}

.date-value, .amount-value {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.days-critical { color: #d9534f; }
.days-warning { color: #f0ad4e; }
.days-normal { color: #5cb85c; }

/* 操作按钮 */
.tenant-actions {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
}

.action-btn {
  flex: 1;
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 0; /* 直角 */
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
  background: white;
  color: #333;
}

.action-btn:hover {
  border-color: #1e3a5f;
  color: #1e3a5f;
  background-color: #f8f9fa;
}

/* 如果需要，可在此覆盖，但通用样式更整洁 */

/* 紧急联系人 */
.emergency-contact {
  background: #fafafa;
  border-top: 1px solid #eee;
  padding: 8px 16px;
}

.emergency-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}

.emergency-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #333;
}

.emergency-phone {
  color: #666;
  font-family: monospace;
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
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: white;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  border-radius: 0; /* 直角 */
  box-shadow: none;
  border: 1px solid #333;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  color: #999;
  cursor: pointer;
  padding: 0;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  padding-bottom: 4px;
  border-bottom: 2px solid #1e3a5f; /* 强调下划线 */
  display: inline-block;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item.full-width {
  grid-column: span 2;
}

.detail-item .label {
  font-size: 12px;
  color: #888;
}

.detail-item .value {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.detail-item .value.highlight {
  color: #1e3a5f;
  font-weight: 600;
}

.modal-footer {
  padding: 12px 16px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  background: #fcfcfc;
}

/* 模态框中的编辑输入框/选择框 */
.status-select, .reading-input {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 0; /* 直角 */
  font-size: 12px;
}

.status-select:focus, .reading-input:focus {
  outline: none;
  border-color: #1e3a5f;
}

.save-icon-btn {
  border: 1px solid #ddd;
  background: white;
  border-radius: 0;
  padding: 2px 6px;
  margin-left: 4px;
  cursor: pointer;
  color: #1e3a5f;
}

.save-icon-btn:hover {
  background: #f0f2f5;
}

.save-btn {
  background: #1e3a5f;
  color: white;
  border: none;
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 0;
  cursor: pointer;
}

.save-btn:hover {
  background: #2d5a87;
}

.secondary-btn {
  background: #eee;
  border: none;
  color: #333;
  padding: 6px 16px;
  font-size: 12px;
  border-radius: 0;
  cursor: pointer;
}

.secondary-btn:hover {
  background: #e0e0e0;
}

/* 滚动条 */
.modal-body::-webkit-scrollbar {
  width: 4px;
}
.modal-body::-webkit-scrollbar-track {
  background: transparent;
}
.modal-body::-webkit-scrollbar-thumb {
  background-color: #ddd;
}

/* 响应式 */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-controls {
    flex-direction: column;
  }
  .contract-info {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .detail-item.full-width {
    grid-column: span 1;
  }
}

/* 下载按钮 */
.download-btn {
  background: #10B981;
  color: white;
  display: flex;
  align-items: center;
  gap: 6px;
}

.download-btn:hover {
  background: #059669;
}

/* PDF预览模态框 */
.pdf-modal .modal-content {
  max-width: 900px;
  width: 95%;
  max-height: 90vh;
}

.pdf-modal .modal-content.pdf-preview-content {
  padding: 0;
}

.pdf-preview-body {
  max-height: calc(90vh - 60px);
  overflow-y: auto;
  background: #f5f5f5;
}
</style>
