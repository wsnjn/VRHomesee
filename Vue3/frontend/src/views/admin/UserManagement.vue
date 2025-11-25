<template>
  <div class="user-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <button @click="showAddDialog = true" class="add-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>
          添加用户
        </button>
        <button @click="refreshData" class="refresh-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10" /><polyline points="1 20 1 14 7 14" /><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" /></svg>
          刷新
        </button>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></svg>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.totalUsers || 0 }}</h3>
          <p>总用户数</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" /><polyline points="9 22 9 12 15 12 15 22" /></svg>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.landlordCount || 0 }}</h3>
          <p>房东用户</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.tenantCount || 0 }}</h3>
          <p>租客用户</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12" /></svg>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.activeUsers || 0 }}</h3>
          <p>活跃用户</p>
        </div>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item">
          <label>用户类型：</label>
          <select v-model="filterUserType" @change="loadUsers">
            <option value="">全部类型</option>
            <option value="1">租客</option>
            <option value="2">房东</option>
            <option value="3">租客+房东</option>
          </select>
        </div>
        <div class="filter-item">
          <label>账户状态：</label>
          <select v-model="filterStatus" @change="loadUsers">
            <option value="">全部状态</option>
            <option value="0">禁用</option>
            <option value="1">正常</option>
            <option value="2">未激活</option>
          </select>
        </div>
        <div class="filter-item">
          <label>实名认证：</label>
          <select v-model="filterCertified" @change="loadUsers">
            <option value="">全部</option>
            <option value="1">已认证</option>
            <option value="0">未认证</option>
          </select>
        </div>
        <div class="filter-item">
          <label>搜索：</label>
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="输入用户名、姓名、手机号等关键词"
            @input="onSearchInput"
          />
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="user-list">
      <div class="table-container">
        <table class="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户信息</th>
              <th>联系信息</th>
              <th>基本信息</th>
              <th>租房信息</th>
              <th>账户状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>
                <div class="user-info">
                  <div class="user-avatar">
                    <img :src="user.avatar || '/src/assets/default-avatar.png'" :alt="user.username" />
                  </div>
                  <div class="user-details">
                    <strong>{{ user.username }}</strong>
                    <div class="user-real-name">{{ user.realName || '未实名' }}</div>
                    <div class="user-type">{{ getUserTypeText(user.userType) }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="contact-info">
                  <div class="phone">{{ user.phone }}</div>
                  <div v-if="user.email" class="email">{{ user.email }}</div>
                  <div v-if="user.wechat" class="wechat">微信: {{ user.wechat }}</div>
                  <div v-if="user.qq" class="qq">QQ: {{ user.qq }}</div>
                </div>
              </td>
              <td>
                <div class="basic-info">
                  <div v-if="user.gender">性别: {{ getGenderText(user.gender) }}</div>
                  <div v-if="user.birthday">生日: {{ formatDate(user.birthday) }}</div>
                  <div v-if="user.idCard">身份证: {{ maskIdCard(user.idCard) }}</div>
                  <div v-if="user.job">职业: {{ user.job }}</div>
                  <div v-if="user.company">单位: {{ user.company }}</div>
                </div>
              </td>
              <td>
                <div class="rental-info">
                  <div v-if="user.monthlyIncome">月收入: {{ user.monthlyIncome }}元</div>
                  <div v-if="user.rentalBudgetMin && user.rentalBudgetMax">
                    预算: {{ user.rentalBudgetMin }}-{{ user.rentalBudgetMax }}元/月
                  </div>
                  <div v-if="user.preferredDistricts" class="preferred-districts">
                    偏好区域: {{ user.preferredDistricts }}
                  </div>
                  <div v-if="user.houseRequirements" class="requirements" :title="user.houseRequirements">
                    需求: {{ truncateText(user.houseRequirements, 30) }}
                  </div>
                </div>
              </td>
              <td>
                <div class="account-status">
                  <span class="status-badge" :class="getStatusClass(user.status)">
                    {{ getStatusText(user.status) }}
                  </span>
                  <div class="credit-score">
                    信用分: <span :class="getCreditScoreClass(user.creditScore)">{{ user.creditScore }}</span>
                  </div>
                  <div class="certification-status">
                    <span :class="user.isCertified ? 'certified' : 'not-certified'">
                      {{ user.isCertified ? '已认证' : '未认证' }}
                    </span>
                  </div>
                  <div class="register-time">
                    注册: {{ formatDateTime(user.registerTime) }}
                  </div>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <button @click="editUser(user)" class="edit-btn">编辑</button>
                  <button @click="toggleUserStatus(user.id, user.status === 1 ? 0 : 1)" 
                          class="status-btn"
                          :class="user.status === 1 ? 'disable-btn' : 'enable-btn'">
                    {{ user.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button @click="resetPassword(user.id)" class="password-btn">重置密码</button>
                  <button @click="deleteUser(user.id)" class="delete-btn">删除</button>
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
        <div v-if="!loading && users.length === 0" class="empty-state">
          <p>暂无用户数据</p>
        </div>
      </div>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <div v-if="showAddDialog || showEditDialog" class="dialog-overlay">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ showAddDialog ? '添加用户' : '编辑用户' }}</h3>
          <button @click="closeDialog" class="close-btn">×</button>
        </div>
        <div class="dialog-content">
          <form @submit.prevent="submitUserForm">
            <div class="form-grid">
              <div class="form-group">
                <label>用户名 *</label>
                <input v-model="userForm.username" type="text" required />
              </div>
              <div class="form-group">
                <label>手机号 *</label>
                <input v-model="userForm.phone" type="tel" required />
              </div>
              <div class="form-group">
                <label>邮箱</label>
                <input v-model="userForm.email" type="email" />
              </div>
              <div class="form-group">
                <label>密码 {{ showAddDialog ? '*' : '' }}</label>
                <input v-model="userForm.password" :type="showPassword ? 'text' : 'password'" :required="showAddDialog" />
                <button type="button" @click="showPassword = !showPassword" class="password-toggle">
                  {{ showPassword ? '隐藏' : '显示' }}
                </button>
              </div>
              <div class="form-group">
                <label>真实姓名</label>
                <input v-model="userForm.realName" type="text" />
              </div>
              <div class="form-group">
                <label>用户类型 *</label>
                <select v-model="userForm.userType" required>
                  <option value="1">租客</option>
                  <option value="2">房东</option>
                  <option value="3">租客+房东</option>
                </select>
              </div>
              <div class="form-group">
                <label>性别</label>
                <select v-model="userForm.gender">
                  <option value="">请选择</option>
                  <option value="1">男</option>
                  <option value="2">女</option>
                </select>
              </div>
              <div class="form-group">
                <label>身份证号</label>
                <input v-model="userForm.idCard" type="text" />
              </div>
              <div class="form-group">
                <label>职业</label>
                <input v-model="userForm.job" type="text" />
              </div>
              <div class="form-group">
                <label>工作单位</label>
                <input v-model="userForm.company" type="text" />
              </div>
              <div class="form-group">
                <label>月收入(元)</label>
                <input v-model="userForm.monthlyIncome" type="number" step="0.01" />
              </div>
              <div class="form-group">
                <label>最低租房预算(元/月)</label>
                <input v-model="userForm.rentalBudgetMin" type="number" step="0.01" />
              </div>
              <div class="form-group">
                <label>最高租房预算(元/月)</label>
                <input v-model="userForm.rentalBudgetMax" type="number" step="0.01" />
              </div>
              <div class="form-group">
                <label>偏好区域</label>
                <input v-model="userForm.preferredDistricts" type="text" placeholder="用逗号分隔多个区域" />
              </div>
              <div class="form-group full-width">
                <label>租房需求描述</label>
                <textarea v-model="userForm.houseRequirements" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>账户状态 *</label>
                <select v-model="userForm.status" required>
                  <option value="0">禁用</option>
                  <option value="1">正常</option>
                  <option value="2">未激活</option>
                </select>
              </div>
              <div class="form-group">
                <label>信用分</label>
                <input v-model="userForm.creditScore" type="number" min="0" max="100" />
              </div>
              <div class="form-group">
                <label>实名认证</label>
                <select v-model="userForm.isCertified">
                  <option value="0">未认证</option>
                  <option value="1">已认证</option>
                </select>
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

  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

// 响应式数据
const loading = ref(false)
const users = ref([])
const statistics = ref({})
const filterUserType = ref('')
const filterStatus = ref('')
const filterCertified = ref('')
const searchKeyword = ref('')
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const showPassword = ref(false)

// 表单数据
const userForm = reactive({
  username: '',
  phone: '',
  email: '',
  password: '',
  userType: 1,
  realName: '',
  gender: '',
  idCard: '',
  job: '',
  company: '',
  monthlyIncome: null,
  rentalBudgetMin: null,
  rentalBudgetMax: null,
  preferredDistricts: '',
  houseRequirements: '',
  status: 1,
  creditScore: 100,
  isCertified: 0
})

let editingUserId = null

// 加载用户数据
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/admin/user/all`)
    if (response.data.success) {
      let filteredUsers = response.data.users || []
      
      // 应用用户类型筛选
      if (filterUserType.value !== '') {
        filteredUsers = filteredUsers.filter(user => user.userType === parseInt(filterUserType.value))
      }
      
      // 应用状态筛选
      if (filterStatus.value !== '') {
        filteredUsers = filteredUsers.filter(user => user.status === parseInt(filterStatus.value))
      }
      
      // 应用认证状态筛选
      if (filterCertified.value !== '') {
        filteredUsers = filteredUsers.filter(user => user.isCertified === parseInt(filterCertified.value))
      }
      
      // 应用搜索筛选
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        filteredUsers = filteredUsers.filter(user => 
          user.username.toLowerCase().includes(keyword) ||
          (user.realName && user.realName.toLowerCase().includes(keyword)) ||
          user.phone.toLowerCase().includes(keyword) ||
          (user.email && user.email.toLowerCase().includes(keyword))
        )
      }
      
      users.value = filteredUsers
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    users.value = []
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/admin/user/statistics`)
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
    loadUsers()
  }, 500)
}

let searchTimer = null

// 刷新数据
const refreshData = () => {
  loadUsers()
  loadStatistics()
}

// 添加用户
const addUser = async () => {
  try {
    const response = await axios.post(`${API_BASE_URL}/admin/user/create`, userForm)
    if (response.data.success) {
      alert('用户添加成功')
      closeDialog()
      refreshData()
    } else {
      alert('添加失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('添加用户失败:', error)
    alert('添加失败: ' + error.message)
  }
}

// 编辑用户
const editUser = (user) => {
  Object.assign(userForm, user)
  editingUserId = user.id
  showEditDialog.value = true
}

// 更新用户
const updateUser = async () => {
  try {
    const response = await axios.put(`${API_BASE_URL}/admin/user/${editingUserId}`, userForm)
    if (response.data.success) {
      alert('用户信息更新成功')
      closeDialog()
      refreshData()
    } else {
      alert('更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新用户失败:', error)
    alert('更新失败: ' + error.message)
  }
}

// 删除用户
const deleteUser = async (id) => {
  if (confirm('确定要删除这个用户吗？此操作不可恢复。')) {
    try {
      const response = await axios.delete(`${API_BASE_URL}/admin/user/${id}`)
      if (response.data.success) {
        alert('用户删除成功')
        refreshData()
      } else {
        alert('删除失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      alert('删除失败: ' + error.message)
    }
  }
}

// 切换用户状态
const toggleUserStatus = async (id, newStatus) => {
  const action = newStatus === 0 ? '禁用' : '启用'
  if (confirm(`确定要${action}这个用户吗？`)) {
    try {
      const response = await axios.put(`${API_BASE_URL}/admin/user/${id}/status`, { status: newStatus })
      if (response.data.success) {
        alert(`用户${action}成功`)
        refreshData()
      } else {
        alert(`${action}失败: ` + response.data.message)
      }
    } catch (error) {
      console.error(`${action}用户失败:`, error)
      alert(`${action}失败: ` + error.message)
    }
  }
}

// 重置密码
const resetPassword = async (id) => {
  if (confirm('确定要将用户密码重置为 123456 吗？')) {
    try {
      const response = await axios.put(`${API_BASE_URL}/admin/user/${id}/reset-password`)
      if (response.data.success) {
        alert('密码重置成功，新密码为：123456')
        refreshData()
      } else {
        alert('重置密码失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('重置密码失败:', error)
      alert('重置密码失败: ' + error.message)
    }
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
  Object.assign(userForm, {
    username: '',
    phone: '',
    email: '',
    password: '',
    userType: 1,
    realName: '',
    gender: '',
    idCard: '',
    job: '',
    company: '',
    monthlyIncome: null,
    rentalBudgetMin: null,
    rentalBudgetMax: null,
    preferredDistricts: '',
    houseRequirements: '',
    status: 1,
    creditScore: 100,
    isCertified: 0
  })
  editingUserId = null
}

// 提交用户表单
const submitUserForm = () => {
  if (showAddDialog.value) {
    addUser()
  } else if (showEditDialog.value) {
    updateUser()
  }
}

// 获取用户类型文本
const getUserTypeText = (userType) => {
  const typeMap = {
    1: '租客',
    2: '房东',
    3: '租客+房东'
  }
  return typeMap[userType] || '未知'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '禁用',
    1: '正常',
    2: '未激活'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-disabled',
    1: 'status-active',
    2: 'status-inactive'
  }
  return classMap[status] || 'status-unknown'
}

// 获取性别文本
const getGenderText = (gender) => {
  const genderMap = {
    1: '男',
    2: '女'
  }
  return genderMap[gender] || '未知'
}

// 获取信用分样式类
const getCreditScoreClass = (score) => {
  if (score >= 90) return 'credit-excellent'
  if (score >= 80) return 'credit-good'
  if (score >= 60) return 'credit-fair'
  return 'credit-poor'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return ''
  const date = new Date(dateTimeString)
  return date.toLocaleString('zh-CN')
}

// 隐藏身份证号码
const maskIdCard = (idCard) => {
  if (!idCard) return ''
  if (idCard.length <= 8) return idCard
  return idCard.substring(0, 4) + '********' + idCard.substring(idCard.length - 4)
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 页面加载时初始化数据
onMounted(() => {
  loadUsers()
  loadStatistics()
})
</script>

<style scoped>
.user-management {
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

.user-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-container {
  overflow-x: auto;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.user-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.user-table tbody tr:hover {
  background-color: #f8f9fa;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details strong {
  display: block;
  margin-bottom: 4px;
  color: #2c3e50;
}

.user-real-name {
  font-size: 0.9rem;
  color: #6c757d;
}

.user-type {
  font-size: 0.8rem;
  color: #007bff;
  background: #e3f2fd;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
}

.contact-info div {
  margin-bottom: 4px;
  font-size: 0.9rem;
}

.phone {
  font-weight: 600;
  color: #2c3e50;
}

.email, .wechat, .qq {
  color: #6c757d;
  font-size: 0.85rem;
}

.basic-info div,
.rental-info div {
  margin-bottom: 4px;
  font-size: 0.9rem;
}

.preferred-districts,
.requirements {
  font-size: 0.85rem;
  color: #6c757d;
}

.account-status {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  align-self: flex-start;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-disabled {
  background: #f8d7da;
  color: #721c24;
}

.status-inactive {
  background: #fff3cd;
  color: #856404;
}

.status-unknown {
  background: #e2e3e5;
  color: #383d41;
}

.credit-score {
  font-size: 0.85rem;
}

.credit-excellent {
  color: #28a745;
  font-weight: 600;
}

.credit-good {
  color: #17a2b8;
  font-weight: 600;
}

.credit-fair {
  color: #ffc107;
  font-weight: 600;
}

.credit-poor {
  color: #dc3545;
  font-weight: 600;
}

.certification-status span {
  font-size: 0.8rem;
  padding: 2px 6px;
  border-radius: 8px;
}

.certified {
  background: #d4edda;
  color: #155724;
}

.not-certified {
  background: #fff3cd;
  color: #856404;
}

.register-time {
  font-size: 0.75rem;
  color: #6c757d;
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

.disable-btn {
  background: #dc3545;
}

.disable-btn:hover {
  background: #c82333;
}

.enable-btn {
  background: #28a745;
}

.enable-btn:hover {
  background: #218838;
}

.password-btn {
  background: #ffc107;
  color: #212529;
}

.password-btn:hover {
  background: #e0a800;
}

.delete-btn {
  background: #6c757d;
  color: white;
}

.delete-btn:hover {
  background: #5a6268;
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
  position: relative;
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

.password-toggle {
  position: absolute;
  right: 10px;
  top: 35px;
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  font-size: 0.8rem;
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

.password-form {
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
  
  .user-info {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .user-management {
    padding: 10px;
  }
}
</style>
