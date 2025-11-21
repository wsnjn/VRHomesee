<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">🏠</div>
        <div class="stat-info">
          <h3>{{ statistics.totalHouses || 0 }}</h3>
          <p>总房屋数</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <h3>{{ statistics.rentedHouses || 0 }}</h3>
          <p>已出租</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏰</div>
        <div class="stat-info">
          <h3>{{ statistics.availableHouses || 0 }}</h3>
          <p>待出租</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-info">
          <h3>{{ statistics.monthlyIncome || 0 }}</h3>
          <p>月收入(元)</p>
        </div>
      </div>
    </div>

    <div class="charts-section">
      <div class="chart-card">
        <h3>房屋状态分布</h3>
        <div class="chart-placeholder">
          <p>房屋状态分布图表</p>
          <div v-if="statistics.houseStatus" class="status-list">
            <div class="status-item">
              <span class="status-dot status-rented"></span>
              <span>已出租: {{ statistics.houseStatus.rented || 0 }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot status-vacant"></span>
              <span>待出租: {{ statistics.houseStatus.vacant || 0 }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot status-maintenance"></span>
              <span>维修中: {{ statistics.houseStatus.maintenance || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <h3>近期到期租约</h3>
        <div class="expiring-list">
          <div 
            v-for="contract in expiringContracts" 
            :key="contract.id"
            class="expiring-item"
          >
            <div class="contract-info">
              <strong>{{ contract.houseName }}</strong>
              <span>租客: {{ contract.tenantName }}</span>
              <span>到期: {{ formatDate(contract.endDate) }}</span>
            </div>
            <span class="days-left">
              {{ calculateDaysLeft(contract.endDate) }}天
            </span>
          </div>
          <div v-if="expiringContracts.length === 0" class="no-data">
            暂无即将到期的租约
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  statistics: {
    type: Object,
    default: () => ({})
  },
  expiringContracts: {
    type: Array,
    default: () => []
  }
})

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
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  color: #2c3e50;
}

.stat-info p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.chart-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.chart-card h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.chart-placeholder {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6c757d;
  border: 2px dashed #e9ecef;
  border-radius: 8px;
  padding: 1rem;
}

.status-list {
  width: 100%;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-rented { background-color: #28a745; }
.status-vacant { background-color: #ffc107; }
.status-maintenance { background-color: #dc3545; }

.expiring-list {
  max-height: 300px;
  overflow-y: auto;
}

.expiring-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #f8f9fa;
  transition: background-color 0.3s;
}

.expiring-item:hover {
  background-color: #f8f9fa;
}

.contract-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.contract-info strong {
  color: #2c3e50;
}

.contract-info span {
  color: #6c757d;
  font-size: 0.9rem;
}

.days-left {
  background: #fff3cd;
  color: #856404;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.no-data {
  text-align: center;
  color: #6c757d;
  padding: 2rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
