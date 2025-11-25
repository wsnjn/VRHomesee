<template>
  <div class="tenant-management">
    <div class="page-header">
      <h2>租约管理</h2>
      <div class="header-actions">
        <button @click="showCreateModal = true" class="create-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          新建租约
        </button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item">
          <label>合同状态：</label>
          <select v-model="filters.contractStatus" @change="loadContracts">
            <option value="">全部状态</option>
            <option value="0">待签约</option>
            <option value="1">已签约</option>
            <option value="2">履行中</option>
            <option value="3">已到期</option>
            <option value="4">提前解约</option>
            <option value="5">已退租</option>
          </select>
        </div>

        <div class="filter-item">
          <label>租金状态：</label>
          <select v-model="filters.rentStatus" @change="loadContracts">
            <option value="">全部状态</option>
            <option value="0">未付款</option>
            <option value="1">已付款</option>
            <option value="2">逾期</option>
            <option value="3">部分付款</option>
          </select>
        </div>

        <div class="filter-item">
          <label>押金状态：</label>
          <select v-model="filters.depositStatus" @change="loadContracts">
            <option value="">全部状态</option>
            <option value="0">未付</option>
            <option value="1">已付</option>
            <option value="2">已退</option>
            <option value="3">抵扣中</option>
          </select>
        </div>

        <div class="filter-item">
          <label>搜索：</label>
          <input 
            v-model="filters.search" 
            placeholder="合同编号、租客姓名..." 
            @input="onSearchInput"
          />
        </div>
      </div>
    </div>

    <!-- 租约列表 -->
    <div class="contracts-table">
      <div class="table-header">
        <div class="table-row">
          <div class="table-cell">合同编号</div>
          <div class="table-cell">房屋信息</div>
          <div class="table-cell">租客信息</div>
          <div class="table-cell">租期</div>
          <div class="table-cell">租金/押金</div>
          <div class="table-cell">合同状态</div>
          <div class="table-cell">租金状态</div>
          <div class="table-cell">押金状态</div>
          <div class="table-cell">操作</div>
        </div>
      </div>

      <div class="table-body">
        <div v-if="loading" class="loading-row">
          <div class="loading-text">正在加载数据...</div>
        </div>

        <div v-else-if="contracts.length === 0" class="empty-row">
          <div class="empty-text">暂无租约数据</div>
        </div>

        <div 
          v-else
          v-for="contract in contracts" 
          :key="contract.id"
          class="table-row"
        >
          <div class="table-cell">
            <strong>{{ contract.contractNumber }}</strong>
          </div>
          <div class="table-cell">
            <div class="house-info">
              <div>房屋ID: {{ contract.roomId }}</div>
              <div class="text-muted">需要关联房屋详情</div>
            </div>
          </div>
          <div class="table-cell">
            <div class="tenant-info">
              <div>租客ID: {{ contract.tenantId }}</div>
              <div class="text-muted">需要关联用户详情</div>
            </div>
          </div>
          <div class="table-cell">
            <div class="date-info">
              <div>开始: {{ formatDate(contract.contractStartDate) }}</div>
              <div>结束: {{ formatDate(contract.contractEndDate) }}</div>
              <div class="text-muted">
                剩余: {{ calculateDaysLeft(contract.contractEndDate) }}天
              </div>
            </div>
          </div>
          <div class="table-cell">
            <div class="amount-info">
              <div>月租: ¥{{ contract.monthlyRent }}</div>
              <div>押金: ¥{{ contract.depositAmount }}</div>
            </div>
          </div>
          <div class="table-cell">
            <select 
              v-model.number="contract.contractStatus" 
              @change="updateContractStatus(contract.id, contract.contractStatus)"
              class="status-select"
            >
              <option value="0">待签约</option>
              <option value="1">已签约</option>
              <option value="2">履行中</option>
              <option value="3">已到期</option>
              <option value="4">提前解约</option>
              <option value="5">已退租</option>
            </select>
          </div>
          <div class="table-cell">
            <select 
              v-model.number="contract.rentStatus" 
              @change="updateRentStatus(contract.id, contract.rentStatus)"
              class="status-select"
            >
              <option value="0">未付款</option>
              <option value="1">已付款</option>
              <option value="2">逾期</option>
              <option value="3">部分付款</option>
            </select>
          </div>
          <div class="table-cell">
            <select 
              v-model.number="contract.depositStatus" 
              @change="updateDepositStatus(contract.id, contract.depositStatus)"
              class="status-select"
            >
              <option value="0">未付</option>
              <option value="1">已付</option>
              <option value="2">已退</option>
              <option value="3">抵扣中</option>
            </select>
          </div>
          <div class="table-cell">
            <div class="action-buttons">
              <button 
                @click="viewContractDetail(contract.id)" 
                class="action-btn view-btn"
                title="查看详情"
              >
                查看
              </button>
              <button 
                @click="editContract(contract)" 
                class="action-btn edit-btn"
                title="编辑"
              >
                编辑
              </button>
              <button 
                @click="updateContractStatus(contract.id, 5)" 
                class="action-btn complete-btn"
                title="标记为已退租"
                v-if="contract.contractStatus !== 5"
              >
                退租
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="contracts.length > 0">
      <button 
        @click="prevPage" 
        :disabled="currentPage === 1"
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </span>
      <button 
        @click="nextPage" 
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 创建租约模态框 -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>新建租约</h3>
          <button @click="showCreateModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>合同编号 *</label>
            <input v-model="newContract.contractNumber" placeholder="请输入合同编号" />
          </div>
          <div class="form-group">
            <label>房屋ID *</label>
            <input v-model="newContract.roomId" type="number" placeholder="请输入房屋ID" />
          </div>
          <div class="form-group">
            <label>房东ID *</label>
            <input v-model="newContract.landlordId" type="number" placeholder="请输入房东ID" />
          </div>
          <div class="form-group">
            <label>租客ID *</label>
            <input v-model="newContract.tenantId" type="number" placeholder="请输入租客ID" />
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
              <input v-model="newContract.monthlyRent" type="number" placeholder="0.00" />
            </div>
            <div class="form-group">
              <label>押金金额 *</label>
              <input v-model="newContract.depositAmount" type="number" placeholder="0.00" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showCreateModal = false" class="cancel-btn">取消</button>
          <button @click="createContract" class="confirm-btn">创建</button>
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
const loading = ref(false)
const contracts = ref([])
const showCreateModal = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

// 筛选条件
const filters = ref({
  contractStatus: '',
  rentStatus: '',
  depositStatus: '',
  search: ''
})

// 新租约数据
const newContract = ref({
  contractNumber: '',
  roomId: '',
  landlordId: '',
  tenantId: '',
  contractStartDate: '',
  contractEndDate: '',
  monthlyRent: '',
  depositAmount: ''
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(totalCount.value / pageSize.value)
})

// 加载租约列表
const loadContracts = async () => {
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/admin/tenant/all`)
    if (response.data.success) {
      contracts.value = response.data.contracts || []
    }
  } catch (error) {
    console.error('加载租约列表失败:', error)
    contracts.value = []
  } finally {
    loading.value = false
  }
}

// 搜索输入处理
const onSearchInput = () => {
  // 防抖处理，可以添加延迟搜索
  loadContracts()
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

// 状态相关方法
const getStatusClass = (status) => {
  const classes = {
    0: 'status-pending',
    1: 'status-active',
    2: 'status-processing',
    3: 'status-expired',
    4: 'status-cancelled',
    5: 'status-completed'
  }
  return classes[status] || 'status-unknown'
}

const getStatusText = (status) => {
  const texts = {
    0: '待签约',
    1: '已签约',
    2: '履行中',
    3: '已到期',
    4: '提前解约',
    5: '已退租'
  }
  return texts[status] || '未知状态'
}

const getRentStatusClass = (status) => {
  const classes = {
    0: 'status-unpaid',
    1: 'status-paid',
    2: 'status-overdue',
    3: 'status-partial'
  }
  return classes[status] || 'status-unknown'
}

const getRentStatusText = (status) => {
  const texts = {
    0: '未付款',
    1: '已付款',
    2: '逾期',
    3: '部分付款'
  }
  return texts[status] || '未知状态'
}

const getDepositStatusClass = (status) => {
  const classes = {
    0: 'status-unpaid',
    1: 'status-paid',
    2: 'status-refunded',
    3: 'status-deducting'
  }
  return classes[status] || 'status-unknown'
}

const getDepositStatusText = (status) => {
  const texts = {
    0: '未付',
    1: '已付',
    2: '已退',
    3: '抵扣中'
  }
  return texts[status] || '未知状态'
}

// 操作按钮方法
const viewContractDetail = (contractId) => {
  // 这里可以跳转到详情页面或显示详情模态框
  alert(`查看合同详情: ${contractId}`)
}

const editContract = (contract) => {
  // 这里可以打开编辑模态框
  alert(`编辑合同: ${contract.contractNumber}`)
}

const updateContractStatus = async (contractId, status) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/admin/tenant/${contractId}/status`, {
      status: status
    })
    if (response.data.success) {
      alert('合同状态更新成功')
      loadContracts()
    } else {
      alert('合同状态更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新合同状态失败:', error)
    alert('更新合同状态失败')
  }
}

const updateRentStatus = async (contractId, rentStatus) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/admin/tenant/${contractId}/rent-status`, {
      rentStatus: rentStatus
    })
    if (response.data.success) {
      alert('租金状态更新成功')
      loadContracts()
    } else {
      alert('租金状态更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新租金状态失败:', error)
    alert('更新租金状态失败')
  }
}

const updateDepositStatus = async (contractId, depositStatus) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/admin/tenant/${contractId}/deposit-status`, {
      depositStatus: depositStatus
    })
    if (response.data.success) {
      alert('押金状态更新成功')
      loadContracts()
    } else {
      alert('押金状态更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新押金状态失败:', error)
    alert('更新押金状态失败')
  }
}

// 创建租约
const createContract = async () => {
  try {
    // 验证必填字段
    if (!newContract.value.contractNumber || !newContract.value.roomId || 
        !newContract.value.landlordId || !newContract.value.tenantId ||
        !newContract.value.contractStartDate || !newContract.value.contractEndDate ||
        !newContract.value.monthlyRent || !newContract.value.depositAmount) {
      alert('请填写所有必填字段')
      return
    }

    const response = await axios.post(`${API_BASE_URL}/admin/tenant/create`, newContract.value)
    if (response.data.success) {
      alert('租约创建成功')
      showCreateModal.value = false
      resetNewContract()
      loadContracts()
    } else {
      alert('租约创建失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('创建租约失败:', error)
    alert('创建租约失败')
  }
}

// 重置新租约数据
const resetNewContract = () => {
  newContract.value = {
    contractNumber: '',
    roomId: '',
    landlordId: '',
    tenantId: '',
    contractStartDate: '',
    contractEndDate: '',
    monthlyRent: '',
    depositAmount: ''
  }
}

// 分页方法
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadContracts()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadContracts()
  }
}

// 页面加载时初始化数据
onMounted(() => {
  loadContracts()
})
</script>

<style scoped>
.tenant-management {
  max-width: 1400px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.create-btn {
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.filter-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.filter-item {
  display: flex;
  flex-direction: column;
  min-width: 180px;
  flex: 1;
}

.filter-item label {
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.filter-item select,
.filter-item input {
  padding: 0.75rem;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  background: white;
  font-size: 0.9rem;
  transition: border-color 0.3s;
}

.filter-item select:focus,
.filter-item input:focus {
  outline: none;
  border-color: #007bff;
}

.contracts-table {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr 0.8fr 0.8fr 0.8fr 1.2fr;
  gap: 1rem;
  padding: 1rem;
  align-items: center;
}

.table-header .table-row {
  font-weight: 600;
  color: #2c3e50;
}

.table-body .table-row {
  border-bottom: 1px solid #f8f9fa;
  transition: background-color 0.3s;
}

.table-body .table-row:hover {
  background-color: #f8f9fa;
}

.table-cell {
  word-break: break-word;
}

.loading-row,
.empty-row {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem;
}

.loading-text,
.empty-text {
  color: #6c757d;
  font-size: 1.1rem;
}

.text-muted {
  color: #6c757d;
  font-size: 0.8rem;
}

.house-info,
.tenant-info,
.date-info,
.amount-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  text-align: center;
  display: inline-block;
}

.status-pending { background: #fff3cd; color: #856404; }
.status-active { background: #d4edda; color: #155724; }
.status-processing { background: #cce7ff; color: #004085; }
.status-expired { background: #f8d7da; color: #721c24; }
.status-cancelled { background: #e2e3e5; color: #383d41; }
.status-completed { background: #d1ecf1; color: #0c5460; }
.status-unpaid { background: #f8d7da; color: #721c24; }
.status-paid { background: #d4edda; color: #155724; }
.status-overdue { background: #f8d7da; color: #721c24; }
.status-partial { background: #fff3cd; color: #856404; }
.status-refunded { background: #d4edda; color: #155724; }
.status-deducting { background: #fff3cd; color: #856404; }
.status-unknown { background: #e2e3e5; color: #383d41; }

.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 600;
  transition: all 0.3s;
}

.status-select {
  width: 100%;
  padding: 0.5rem;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  background: white;
  font-size: 0.8rem;
  transition: border-color 0.3s;
}

.status-select:focus {
  outline: none;
  border-color: #007bff;
}

.view-btn {
  background: #17a2b8;
  color: white;
}

.edit-btn {
  background: #ffc107;
  color: #212529;
}

.complete-btn {
  background: #28a745;
  color: white;
}

.action-btn:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #6c757d;
  font-weight: 600;
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

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
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

.cancel-btn,
.confirm-btn {
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
  .table-row {
    grid-template-columns: 1fr 1fr 1fr 1fr 1fr;
    gap: 0.5rem;
  }
  
  .table-cell:nth-child(6),
  .table-cell:nth-child(7),
  .table-cell:nth-child(8),
  .table-cell:nth-child(9) {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
  }
  
  .table-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
    border: 1px solid #e9ecef;
    margin-bottom: 1rem;
    border-radius: 8px;
  }
  
  .table-cell {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem 0;
    border-bottom: 1px solid #f8f9fa;
  }
  
  .table-cell:last-child {
    border-bottom: none;
  }
  
  .table-cell::before {
    content: attr(data-label);
    font-weight: 600;
    color: #2c3e50;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
