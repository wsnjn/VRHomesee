<template>
  <div class="maintenance-container">
    <div class="header">
      <div class="title-section">
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg>
        <h2>维修服务</h2>
      </div>
      <button class="btn-primary" @click="showCreateModal = true">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
        申请维修
      </button>
    </div>

    <div class="content-grid">
      <!-- Left Column: House Info -->
      <div class="left-column">
        <div class="section-title">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
          <h3>当前房屋信息</h3>
        </div>
        
        <div class="lease-card" v-if="activeLease">
          <div class="lease-header">
            <h4 class="lease-title">当前租约信息</h4>
            <span class="lease-status" :class="getLeaseStatusClass(activeLease.contractStatus)">{{ getLeaseStatusText(activeLease.contractStatus) }}</span>
          </div>
          
          <div class="lease-info-grid">
            <div class="info-item">
              <span class="label">合同编号</span>
              <span class="value">{{ activeLease.contractNumber }}</span>
            </div>

            <div class="info-item">
              <span class="label">租期</span>
              <span class="value">{{ formatDate(activeLease.contractStartDate) }} 至 {{ formatDate(activeLease.contractEndDate) }}</span>
            </div>
            <div class="info-item">
              <span class="label">月租金</span>
              <span class="value">¥{{ activeLease.monthlyRent }}</span>
            </div>
            <div class="info-item">
              <span class="label">押金</span>
              <span class="value">¥{{ activeLease.depositAmount ?? 0 }}</span>
            </div>
            <div class="info-item">
              <span class="label">付款方式</span>
              <span class="value">{{ getPaymentMethodText(activeLease.paymentCycle) }}</span>
            </div>
            <div class="info-item">
              <span class="label">押金状态</span>
              <span class="value">{{ getDepositStatusText(activeLease.depositStatus) }}</span>
            </div>
            <div class="info-item">
              <span class="label">合同状态</span>
              <span class="value">{{ getLeaseStatusText(activeLease.contractStatus) }}</span>
            </div>
            <div class="info-item">
              <span class="label">签约日期</span>
              <span class="value">{{ formatDate(activeLease.contractSignedTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">合同备注</span>
              <span class="value">{{ activeLease.contractNotes || '无备注' }}</span>
            </div>
          </div>
          
          <!-- 水电费用信息 -->
          <div class="house-details-section">
            <h5 class="section-subtitle">水电费用信息</h5>
            <div class="house-details-grid">
              <div class="detail-item">
                <span class="label">水费单价</span>
                <span class="value">{{ houseDetails?.waterPrice ? '¥' + houseDetails.waterPrice + '/吨' : '未设置' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">电费单价</span>
                <span class="value">{{ houseDetails?.electricPrice ? '¥' + houseDetails.electricPrice + '/度' : '未设置' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">上期水表读数</span>
                <span class="value">{{ activeLease.lastWaterReading ?? 0 }}</span>
              </div>
              <div class="detail-item">
                <span class="label">上期电表读数</span>
                <span class="value">{{ activeLease.lastElectricReading ?? 0 }}</span>
              </div>
            </div>
          </div>

          <!-- 房屋详细信息 -->
          <div class="house-details-section" v-if="houseDetails">
            <h5 class="section-subtitle">房屋详细信息</h5>
            <div class="house-details-grid">
              <div class="detail-item">
                <span class="label">小区名称</span>
                <span class="value">{{ houseDetails.communityName || '未知' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">详细地址</span>
                <span class="value">{{ getFullAddress(houseDetails) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">房屋面积</span>
                <span class="value">{{ houseDetails.roomArea || '未知' }}㎡</span>
              </div>
              <div class="detail-item">
                <span class="label">楼层信息</span>
                <span class="value">{{ houseDetails.floorInfo || '未知' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">装修程度</span>
                <span class="value">{{ getDecorationText(houseDetails.decoration) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">朝向</span>
                <span class="value">{{ getOrientationText(houseDetails.orientation) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">租赁类型</span>
                <span class="value">{{ getRentalTypeText(houseDetails.rentalType) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">房东电话</span>
                <span class="value">{{ houseDetails.landlordPhone ? formatPhoneNumber(houseDetails.landlordPhone) : '未提供' }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-data-card">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="9" y1="9" x2="15" y2="15"></line><line x1="15" y1="9" x2="9" y2="15"></line></svg>
          <p>暂无有效租约</p>
        </div>
      </div>

      <!-- Right Column: Maintenance Requests -->
      <div class="right-column">
        <div class="section-title" style="margin-top: 0;">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
          <h3>维修记录</h3>
        </div>

        <div v-if="requests.length === 0" class="no-data-card">
          <p>暂无维修记录</p>
        </div>
        <div v-else class="request-list">
          <div class="request-card" v-for="req in requests" :key="req.id">
            <div class="req-header">
              <span class="req-title">{{ req.requestTitle }}</span>
              <span class="req-status" :class="getStatusClass(req.requestStatus)">{{ getStatusText(req.requestStatus) }}</span>
            </div>
            <p class="req-desc">{{ req.requestDescription }}</p>
            <div class="req-meta">
              <div class="meta-item">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                <span>申请: {{ formatDate(req.requestDate) }}</span>
              </div>
              <div class="meta-item" v-if="req.expectedFixDate">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                <span>期望: {{ formatDate(req.expectedFixDate) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Modal -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>申请维修</h3>
          <button class="close-btn" @click="showCreateModal = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
        <form @submit.prevent="submitRequest">
          <div class="form-group">
            <label>标题</label>
            <input v-model="newRequest.requestTitle" required placeholder="例如：水龙头漏水" />
          </div>
          <div class="form-group">
            <label>详细描述</label>
            <textarea v-model="newRequest.requestDescription" required placeholder="请详细描述故障情况..."></textarea>
          </div>
          <div class="form-group">
            <label>期望维修日期</label>
            <input type="date" v-model="newRequest.expectedFixDate" />
          </div>
          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="showCreateModal = false">取消</button>
            <button type="submit" class="btn-primary">提交申请</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userState } from '../../state/user'

const requests = ref([])
const showCreateModal = ref(false)
const activeLease = ref(null)
const houseDetails = ref(null)
const currentUserId = userState.user ? userState.user.id : 0

const newRequest = ref({
  requestTitle: '',
  requestDescription: '',
  expectedFixDate: ''
})

const fetchActiveLease = async () => {
  if (!currentUserId) return
  try {
    // Use the new endpoint to get all contracts for the tenant
    const res = await fetch(`http://localhost:8080/api/admin/tenant/tenant/${currentUserId}`)
    const data = await res.json()
    if (data.success && data.contracts && data.contracts.length > 0) {
      // Find the active contract (status 1=Signed or 2=In Progress)
      const active = data.contracts.find(c => c.contractStatus === 1 || c.contractStatus === 2)
      if (active) {
        activeLease.value = active
        // Fetch house details if roomId is available
        if (active.roomId) {
          await fetchHouseDetails(active.roomId)
        }
        await fetchRequests()
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchHouseDetails = async (roomId) => {
  try {
    const res = await fetch(`http://localhost:8080/api/room-info/${roomId}`)
    const data = await res.json()
    if (data.success && data.room) {
      houseDetails.value = data.room
    }
  } catch (e) {
    console.error('获取房屋详情失败:', e)
  }
}

const fetchRequests = async () => {
  if (!activeLease.value) return
  
  try {
      const res = await fetch(`http://localhost:8080/api/maintenance/list/${activeLease.value.id}`)
      const data = await res.json()
      if (data.success) {
          requests.value = data.data
      }
  } catch (e) {
      console.error(e)
  }
}

const submitRequest = async () => {
  if (!activeLease.value) return alert('无有效租约')
  
  const payload = {
    tenantManagementId: activeLease.value.id,
    ...newRequest.value,
    requestStatus: 0
  }

  try {
    const res = await fetch('http://localhost:8080/api/maintenance/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (data.success) {
      alert('提交成功')
      showCreateModal.value = false
      fetchRequests()
      newRequest.value = { requestTitle: '', requestDescription: '', expectedFixDate: '' }
    } else {
      alert('提交失败')
    }
  } catch (e) {
    console.error(e)
    alert('网络错误')
  }
}

const getStatusText = (status) => {
  const map = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-pending', 1: 'status-processing', 2: 'status-completed', 3: 'status-closed' }
  return map[status] || ''
}

const formatDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleDateString()
}

// 格式化电话号码
const formatPhoneNumber = (phone) => {
  if (!phone) return ''
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`
  }
  return phone
}

// 获取合同状态文字
const getLeaseStatusText = (status) => {
  const map = { 0: '待签约', 1: '已签约', 2: '履行中', 3: '已到期', 4: '提前解约', 5: '已退租' }
  return map[status] || '未知状态'
}

// 获取合同状态样式类
const getLeaseStatusClass = (status) => {
  const map = { 
    0: 'status-pending', 
    1: 'status-active', 
    2: 'status-active', 
    3: 'status-expired', 
    4: 'status-terminated',
    5: 'status-terminated'
  }
  return map[status] || ''
}

// 获取付款方式文字
const getPaymentMethodText = (method) => {
  const map = { 0: '月付', 1: '季付', 2: '半年付', 3: '年付' }
  return map[method] || '未知'
}

// 获取押金状态文字
const getDepositStatusText = (status) => {
  const map = { 0: '未付', 1: '已付', 2: '已退', 3: '抵扣中' }
  return map[status] || '未知'
}

// 获取装修程度文字
const getDecorationText = (decoration) => {
  const types = { 1: '毛坯', 2: '简装', 3: '精装', 4: '豪装' }
  return types[decoration] || '未知'
}

// 获取朝向文字
const getOrientationText = (orientation) => {
  const map = { 1: '东', 2: '南', 3: '西', 4: '北', 5: '东南', 6: '西南', 7: '东北', 8: '西北' }
  return map[orientation] || '未知'
}

// 获取租赁类型文字
const getRentalTypeText = (type) => {
  const types = { 0: '整租', 1: '合租', 2: '单间' }
  return types[type] || '未知'
}

// 获取完整地址
const getFullAddress = (house) => {
  if (!house) return ''
  const parts = [house.province, house.city, house.district, house.street, house.communityName]
  return parts.filter(part => part).join('')
}

onMounted(() => {
  fetchActiveLease()
})
</script>

<style scoped>
.maintenance-container {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Inter', sans-serif;
  color: #333;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  align-items: start;
}

@media (max-width: 900px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-section h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.icon {
  color: #6366f1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  color: #4b5563;
  margin-top: 30px;
}

.section-title h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.lease-card {
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  border: 1px solid rgba(0,0,0,0.05);
  transition: transform 0.2s;
}

.lease-card:hover {
  transform: translateY(-2px);
}

.lease-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f1f3f5;
}

.lease-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.lease-status {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.status-active { background: #f0fdf4; color: #15803d; }
.status-pending { background: #fff7ed; color: #c2410c; }
.status-expired { background: #f3f4f6; color: #6b7280; }
.status-terminated { background: #fef2f2; color: #dc2626; }

.lease-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.house-details-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f1f3f5;
}

.section-subtitle {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.house-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-item .label {
  font-size: 11px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.detail-item .value {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-item .label {
  font-size: 11px;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item .value {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
}

.no-data-card {
  background: #f9fafb;
  padding: 40px;
  border-radius: 16px;
  text-align: center;
  color: #9ca3af;
  border: 2px dashed #e5e7eb;
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.request-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
  border: 1px solid #f3f4f6;
  transition: all 0.2s;
}

.request-card:hover {
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.req-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.req-title {
  font-weight: 600;
  font-size: 16px;
  color: #1f2937;
}

.req-status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.status-pending { background: #fff7ed; color: #c2410c; }
.status-processing { background: #eff6ff; color: #1d4ed8; }
.status-completed { background: #f0fdf4; color: #15803d; }
.status-closed { background: #f3f4f6; color: #4b5563; }

.req-desc {
  color: #4b5563;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
}

.req-meta {
  display: flex;
  gap: 20px;
  border-top: 1px solid #f3f4f6;
  padding-top: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
  padding: 5px;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: #f3f4f6;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-group input, .form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-group input:focus, .form-group textarea:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
}

.btn-primary {
  background: #6366f1;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #4f46e5;
}

.btn-secondary {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.btn-secondary:hover {
  background: #f9fafb;
}
</style>
