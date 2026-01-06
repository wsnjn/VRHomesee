<!--
  项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
  文件名称：LandlordDashboardStats.vue
  开发者：牛迦楠
  专业：软件工程（中外合作办学）
  学校：东华理工大学
  功能描述：房东仪表盘统计组件，展示总收入、入住率、报修数及到期合约等关键数据
  创建日期：2026-01-06
-->
<template>
  <div class="dashboard-stats-container">
    <div class="stat-card income-card">
      <div class="stat-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="currentColor">
          <path d="M21 18v1c0 1.1-.9 2-2 2H5c-1.11 0-2-.9-2-2V5c0-1.1.89-2 2-2h14c1.1 0 2 .9 2 2v1h-9c-1.11 0-2 .9-2 2v8c0 1.1.89 2 2 2h9zm-9-2h10V8H12v8zm4-2.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/>
        </svg>
      </div>
      <div class="stat-info">
        <h3>本月总收入</h3>
        <p class="stat-value">¥{{ formatCurrency(stats.monthlyIncome) }}</p>
        <span class="stat-trend positive">
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
            <path d="M7 14l5-5 5 5H7z"/>
          </svg>
          租金总额
        </span>
      </div>
    </div>

    <div class="stat-card occupancy-card">
      <div class="stat-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="currentColor">
          <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8h5z"/>
        </svg>
      </div>
      <div class="stat-info">
        <h3>入住率</h3>
        <p class="stat-value">{{ formatPercent(stats.occupationRate) }}</p>
        <span class="stat-sub">{{ stats.rentedHouses }}/{{ stats.totalHouses }} 套已租</span>
      </div>
    </div>

    <div class="stat-card maintenance-card">
      <div class="stat-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="currentColor">
          <path d="M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z"/>
        </svg>
      </div>
      <div class="stat-info">
        <h3>待处理报修</h3>
        <p class="stat-value">{{ stats.pendingMaintenance }}</p>
        <span class="stat-status" :class="{ 'urgent': stats.pendingMaintenance > 0 }">
          {{ stats.pendingMaintenance > 0 ? '需要立即处理' : '暂无待办' }}
        </span>
      </div>
    </div>

    <div class="stat-card lease-card">
      <div class="stat-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="currentColor">
          <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
        </svg>
      </div>
      <div class="stat-info">
        <h3>即将到期</h3>
        <p class="stat-value">{{ stats.expiringLeases }}</p>
        <span class="stat-sub">未来 30 天内</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  stats: {
    type: Object,
    required: true,
    default: () => ({
      monthlyIncome: 0,
      occupationRate: 0,
      rentedHouses: 0,
      totalHouses: 0,
      pendingMaintenance: 0,
      expiringLeases: 0
    })
  }
});

const formatCurrency = (value) => {
  return value ? value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00';
};

const formatPercent = (value) => {
  return value ? (value * 100).toFixed(1) + '%' : '0.0%';
};
</script>

<style scoped>
.dashboard-stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  transition: box-shadow 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

/* 特定卡片样式 - 极简配色 */
.income-card .stat-icon {
  background-color: #e8f4fd;
  color: #1e3a5f;
}

.occupancy-card .stat-icon {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.maintenance-card .stat-icon {
  background-color: #fff3e0;
  color: #e65100;
}

.lease-card .stat-icon {
  background-color: #fce4ec;
  color: #c2185b;
}

.stat-info h3 {
  margin: 0;
  font-size: 13px;
  color: #7f8c8d;
  font-weight: 500;
}

.stat-value {
  margin: 4px 0;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.stat-sub, .stat-trend, .stat-status {
  font-size: 12px;
  color: #95a5a6;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-trend.positive {
  color: #1e3a5f;
  font-weight: 500;
}

.stat-status.urgent {
  color: #e74c3c;
  font-weight: 500;
}
</style>
