<!--
  项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
  文件名称：LandlordDataCharts.vue
  开发者：牛迦楠
  专业：软件工程（中外合作办学）
  学校：东华理工大学
  功能描述：房东数据分析图表组件，集成租金构成、房屋状态分布及收入趋势等可视化分析
  创建日期：2026-01-06
-->
<template>
  <div class="data-charts-container">
    <h2 class="section-title">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M3 3v18h18"/>
        <path d="M18 17V9"/>
        <path d="M13 17V5"/>
        <path d="M8 17v-3"/>
      </svg>
      数据分析
    </h2>
    
    <div class="charts-grid">
      <!-- 租金构成饼图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 2a10 10 0 0 1 10 10"/>
              <path d="M12 12L12 2"/>
              <path d="M12 12L20.5 7"/>
            </svg>
            租金构成
          </h3>
          <span class="chart-subtitle">各房源租金占比</span>
        </div>
        <div ref="rentPieChart" class="chart-body"></div>
      </div>
      
      <!-- 房屋状态分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12h4l3 9 4-18 3 9h4"/>
            </svg>
            房屋状态分布
          </h3>
          <span class="chart-subtitle">当前各状态房源数量</span>
        </div>
        <div ref="statusBarChart" class="chart-body"></div>
      </div>
      
      <!-- 收入趋势（模拟数据） -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
              <polyline points="17 6 23 6 23 12"/>
            </svg>
            收入趋势
          </h3>
          <span class="chart-subtitle">近6个月租金收入</span>
        </div>
        <div ref="trendLineChart" class="chart-body"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  houses: {
    type: Array,
    default: () => []
  },
  contracts: {
    type: Array,
    default: () => []
  },
  statistics: {
    type: Object,
    default: () => ({})
  }
})

const rentPieChart = ref(null)
const statusBarChart = ref(null)
const trendLineChart = ref(null)

let pieInstance = null
let barInstance = null
let lineInstance = null

// 初始化租金占比饼图
const initRentPieChart = () => {
  if (!rentPieChart.value) return
  
  if (pieInstance) {
    pieInstance.dispose()
  }
  
  pieInstance = echarts.init(rentPieChart.value)
  
  // 从合同数据生成饼图数据
  const activeContracts = props.contracts.filter(c => c.contractStatus === 1 || c.contractStatus === 2)
  
  const pieData = activeContracts.map(c => ({
    name: c.communityName || `房源${c.roomId}`,
    value: parseFloat(c.monthlyRent) || 0
  }))
  
  // 如果没有数据，用示例数据
  const data = pieData.length > 0 ? pieData : [
    { name: '暂无数据', value: 1 }
  ]
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { fontSize: 11 }
    },
    color: ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: data
    }]
  }
  
  pieInstance.setOption(option)
}

// 初始化房屋状态柱状图
const initStatusBarChart = () => {
  if (!statusBarChart.value) return
  
  if (barInstance) {
    barInstance.dispose()
  }
  
  barInstance = echarts.init(statusBarChart.value)
  
  const stats = props.statistics
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['可租', '已租', '下架', '预租'],
      axisLabel: { fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      data: [
        { value: stats.availableHouses || 0, itemStyle: { color: '#10B981' } },
        { value: stats.rentedHouses || 0, itemStyle: { color: '#3B82F6' } },
        { value: stats.offlineHouses || 0, itemStyle: { color: '#9CA3AF' } },
        { value: stats.preRentHouses || 0, itemStyle: { color: '#F59E0B' } }
      ],
      label: {
        show: true,
        position: 'top',
        fontSize: 12,
        fontWeight: 'bold'
      }
    }]
  }
  
  barInstance.setOption(option)
}

// 初始化收入趋势折线图
const initTrendLineChart = () => {
  if (!trendLineChart.value) return
  
  if (lineInstance) {
    lineInstance.dispose()
  }
  
  lineInstance = echarts.init(trendLineChart.value)
  
  // 生成过去6个月的标签
  const months = []
  const now = new Date()
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${d.getMonth() + 1}月`)
  }
  
  // 计算当前月收入（作为基准）
  const currentIncome = props.contracts
    .filter(c => c.contractStatus === 1 || c.contractStatus === 2)
    .reduce((sum, c) => sum + (parseFloat(c.monthlyRent) || 0), 0)
  
  // 模拟历史数据（基于当前收入浮动）
  const incomeData = months.map((_, i) => {
    if (i === 5) return currentIncome
    // 模拟历史波动
    return Math.round(currentIncome * (0.85 + Math.random() * 0.3))
  })
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: ¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months,
      axisLabel: { fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [{
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        color: '#3B82F6',
        width: 3
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
          { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
        ])
      },
      itemStyle: {
        color: '#3B82F6',
        borderColor: '#fff',
        borderWidth: 2
      },
      data: incomeData
    }]
  }
  
  lineInstance.setOption(option)
}

// 窗口大小变化时重绘
const handleResize = () => {
  pieInstance?.resize()
  barInstance?.resize()
  lineInstance?.resize()
}

onMounted(() => {
  nextTick(() => {
    initRentPieChart()
    initStatusBarChart()
    initTrendLineChart()
  })
  
  window.addEventListener('resize', handleResize)
})

// 监听数据变化
watch(() => [props.houses, props.contracts, props.statistics], () => {
  nextTick(() => {
    initRentPieChart()
    initStatusBarChart()
    initTrendLineChart()
  })
}, { deep: true })
</script>

<style scoped>
.data-charts-container {
  margin-top: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 12px;
}

.section-title svg {
  color: #3B82F6;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.chart-header {
  margin-bottom: 8px;
}

.chart-header h3 {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin: 0;
}

.chart-header svg {
  color: #6B7280;
}

.chart-subtitle {
  font-size: 11px;
  color: #9CA3AF;
}

.chart-body {
  height: 180px;
  width: 100%;
}

@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
