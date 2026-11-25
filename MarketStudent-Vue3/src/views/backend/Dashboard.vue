<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <div class="welcome-info">
        <h1>欢迎回来，<span class="username">{{ userInfo?.realName || userInfo?.username || '管理员' }}</span></h1>
        <p class="date-info">{{ currentDate }} · 云超管家</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" plain size="small" @click="refreshAllData">
          <el-icon><Refresh /></el-icon>
          <span>刷新数据</span>
        </el-button>
        <el-button type="primary" size="small">
          <el-icon><Plus /></el-icon>
          <span>新建商品</span>
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" v-for="(stat, index) in statCards" :key="index">
        <div class="stat-card" :class="`stat-card-${index+1}`">
          <div class="stat-card-content">
            <div class="stat-value-section">
              <h3 class="stat-value">{{ stats[stat.key] || 0 }}</h3>
              <div class="stat-badge" :style="{ backgroundColor: stat.badgeColor }">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
            </div>
            <div class="stat-title">{{ stat.title }}</div>
            <div class="stat-trend">
              <el-icon :color="stat.trend > 0 ? '#10b981' : '#ef4444'">
                <component :is="stat.trend > 0 ? 'ArrowUp' : 'ArrowDown'" />
              </el-icon>
              <span :class="{ 'trend-up': stat.trend > 0, 'trend-down': stat.trend < 0 }">
                {{ Math.abs(stat.trend) }}% 较上月
              </span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <div class="chart-card">
          <div class="chart-card-header">
            <div class="chart-title">
              <h3>采购趋势分析</h3>
              <span class="chart-subtitle">过去7天采购金额统计</span>
            </div>
            <div class="chart-actions">
              <el-select v-model="purchaseTimeRange" size="small" class="time-range-select">
                <el-option label="过去7天" value="7days" />
                <el-option label="过去30天" value="30days" />
                <el-option label="过去90天" value="90days" />
              </el-select>
              <el-button type="primary" size="small" text @click="refreshChartData">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="chart-container" v-loading="loadingCharts">
            <div id="purchaseChart" class="chart"></div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="chart-card-header">
            <div class="chart-title">
              <h3>商品分类占比</h3>
              <span class="chart-subtitle">各分类商品数量占比</span>
            </div>
            <div class="chart-actions">
              <el-button type="primary" size="small" text @click="refreshCategoryData">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="chart-container" v-loading="loadingCharts">
            <div id="categoryChart" class="chart"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24">
        <div class="chart-card">
          <div class="chart-card-header">
            <div class="chart-title">
              <h3>采购与库存对比</h3>
              <span class="chart-subtitle">主要商品类别的采购与库存对比</span>
            </div>
            <div class="chart-actions">
              <el-select v-model="compareTimeRange" size="small" class="time-range-select">
                <el-option label="本月" value="month" />
                <el-option label="本季度" value="quarter" />
                <el-option label="本年" value="year" />
              </el-select>
              <el-button type="primary" size="small" text @click="refreshCompareData">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="chart-container" v-loading="loadingCharts">
            <div id="compareChart" class="chart"></div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount, nextTick, watch } from 'vue'
import { ShoppingCart, Box, Tickets, WarningFilled, Refresh, Plus, RefreshRight, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
// Import only what we need from ECharts instead of the whole library
import * as echarts from 'echarts/core'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  MarkLineComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// Register necessary ECharts components
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  MarkLineComponent,
  BarChart,
  PieChart,
  LineChart,
  CanvasRenderer
])

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const stats = ref({
  products: 0,
  suppliers: 0,
  orders: 0,
  warnings: 0
})

const loading = ref(false)
const loadingCharts = ref(false)
const purchaseTimeRange = ref('7days')
const compareTimeRange = ref('month')
let purchaseChart = null
let categoryChart = null
let compareChart = null

// 卡片数据
const statCards = [
  { 
    title: '商品总数', 
    key: 'products', 
    icon: 'ShoppingCart', 
    badgeColor: 'rgba(59, 130, 246, 0.15)', 
    trend: 5.8
  },
  { 
    title: '供应商数量', 
    key: 'suppliers', 
    icon: 'Box', 
    badgeColor: 'rgba(16, 185, 129, 0.15)', 
    trend: 2.3
  },
  { 
    title: '采购订单', 
    key: 'orders', 
    icon: 'Tickets', 
    badgeColor: 'rgba(245, 158, 11, 0.15)', 
    trend: -1.5
  },
  { 
    title: '库存预警', 
    key: 'warnings', 
    icon: 'WarningFilled', 
    badgeColor: 'rgba(239, 68, 68, 0.15)', 
    trend: 8.4
  }
]

const currentDate = computed(() => {
  const date = new Date()
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  })
})

// 获取仪表盘统计数据
const fetchDashboardStats = async () => {
  try {
    loading.value = true
    const response = await request.get('dashboard/stats')
    stats.value = response
  } catch (error) {
    console.error('获取仪表盘数据失败', error)
    ElMessage.error('获取统计数据失败，请稍后重试')
    // 使用默认数据作为备选
    stats.value = {
      products: 1250,
      suppliers: 48,
      orders: 156,
      warnings: 12
    }
  } finally {
    loading.value = false
  }
}

// 获取采购图表数据
const fetchPurchaseChartData = async () => {
  try {
    loadingCharts.value = true
    
    // 从后端获取数据
    const response = await request.get('dashboard/purchases?timeRange=' + purchaseTimeRange.value)
    
    // 初始化图表
    await nextTick()
    const chartDom = document.getElementById('purchaseChart')
    
    if (chartDom) {
      if (purchaseChart) {
        purchaseChart.dispose()
      }
      
      purchaseChart = echarts.init(chartDom)
      const dates = response.map(item => item.date)
      const amounts = response.map(item => item.amount)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          top: 10,
          right: 10,
          bottom: 20,
          left: 40,
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            data: dates,
            axisTick: {
              alignWithLabel: true,
              show: false
            },
            axisLine: {
              lineStyle: {
                color: '#ddd'
              }
            },
            axisLabel: {
              color: '#666'
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '',
            splitLine: {
              lineStyle: {
                type: 'dashed',
                color: '#eee'
              }
            },
            axisLabel: {
              color: '#666',
              formatter: '{value} 元'
            }
          }
        ],
        series: [
          {
            name: '采购金额',
            type: 'bar',
            barWidth: '50%',
            data: amounts,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#3b82f6' },
                { offset: 1, color: '#60a5fa' }
              ]),
              borderRadius: [4, 4, 0, 0]
            }
          }
        ]
      }
      
      purchaseChart.setOption(option)
    }
  } catch (error) {
    console.error('获取采购统计数据失败', error)
    ElMessage.error('获取采购统计数据失败，请稍后重试')
  } finally {
    loadingCharts.value = false
  }
}

// 获取分类图表数据
const fetchCategoryData = async () => {
  try {
    loadingCharts.value = true
    
    // 从后端获取数据
    const response = await request.get('dashboard/categories')
    
    // 初始化图表
    await nextTick()
    const chartDom = document.getElementById('categoryChart')
    
    if (chartDom) {
      if (categoryChart) {
        categoryChart.dispose()
      }
      
      categoryChart = echarts.init(chartDom)
      
      const colorPalette = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4']
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          itemWidth: 10,
          itemHeight: 10,
          itemGap: 10,
          formatter: (name) => {
            const item = response.find(i => i.name === name)
            return `${name} · ${item ? item.value : 0} 个`
          },
          textStyle: {
            color: '#666'
          }
        },
        series: [
          {
            name: '商品分类',
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['40%', '50%'],
            itemStyle: {
              borderRadius: 4,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false
            },
            emphasis: {
              label: {
                show: false
              },
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.2)'
              }
            },
            labelLine: {
              show: false
            },
            data: response.map((item, index) => ({
              value: item.value,
              name: item.name,
              itemStyle: {
                color: colorPalette[index % colorPalette.length]
              }
            }))
          }
        ]
      }
      
      categoryChart.setOption(option)
    }
  } catch (error) {
    console.error('获取分类统计数据失败', error)
    ElMessage.error('获取分类统计数据失败，请稍后重试')
  } finally {
    loadingCharts.value = false
  }
}

// 获取比较图表数据
const fetchCompareData = async () => {
  try {
    loadingCharts.value = true
    
    // 从后端获取数据
    let response;
    try {
      response = await request.get('dashboard/compare?timeRange=' + compareTimeRange.value);
    } catch (apiError) {
      console.warn('比较数据API不存在，使用默认数据', apiError);
      // 使用默认数据作为备选
      response = [
       
      ];
    }
    
    // 初始化图表
    await nextTick()
    const chartDom = document.getElementById('compareChart')
    
    if (chartDom) {
      if (compareChart) {
        compareChart.dispose()
      }
      
      compareChart = echarts.init(chartDom)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['采购额', '库存量'],
          right: 10,
          top: 10
        },
        grid: {
          top: 50,
          right: 20,
          bottom: 20,
          left: 60,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: response.map(item => item.category),
          axisTick: {
            alignWithLabel: true,
            show: false
          },
          axisLine: {
            lineStyle: {
              color: '#ddd'
            }
          },
          axisLabel: {
            color: '#666',
            interval: 0,
            rotate: response.length > 5 ? 30 : 0
          }
        },
        yAxis: {
          type: 'value',
          splitLine: {
            lineStyle: {
              type: 'dashed',
              color: '#eee'
            }
          },
          axisLabel: {
            color: '#666'
          }
        },
        series: [
          {
            name: '采购额',
            type: 'bar',
            barWidth: '30%',
            data: response.map(item => item.sales),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#3b82f6' },
                { offset: 1, color: '#60a5fa' }
              ]),
              borderRadius: [4, 4, 0, 0]
            }
          },
          {
            name: '库存量',
            type: 'bar',
            barWidth: '30%',
            data: response.map(item => item.stock),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#10b981' },
                { offset: 1, color: '#34d399' }
              ]),
              borderRadius: [4, 4, 0, 0]
            }
          }
        ]
      }
      
      compareChart.setOption(option)
    }
  } catch (error) {
    console.error('获取比较数据失败', error)
    ElMessage.error('获取比较数据失败，请稍后重试')
  } finally {
    loadingCharts.value = false
  }
}

// 刷新图表数据
const refreshChartData = () => {
  fetchPurchaseChartData()
}

const refreshCategoryData = () => {
  fetchCategoryData()
}

const refreshCompareData = () => {
  fetchCompareData()
}

const refreshAllData = () => {
  fetchDashboardStats()
  fetchPurchaseChartData()
  fetchCategoryData()
  fetchCompareData()
}

// 窗口大小变化时重新调整图表大小
const handleResize = () => {
  if (purchaseChart) {
    purchaseChart.resize()
  }
  if (categoryChart) {
    categoryChart.resize()
  }
  if (compareChart) {
    compareChart.resize()
  }
}

// 清理图表实例
const disposeCharts = () => {
  if (purchaseChart) {
    purchaseChart.dispose()
    purchaseChart = null
  }
  if (categoryChart) {
    categoryChart.dispose()
    categoryChart = null
  }
  if (compareChart) {
    compareChart.dispose()
    compareChart = null
  }
}

// 监听时间范围变化，重新获取数据
watch(purchaseTimeRange, (newValue) => {
  fetchPurchaseChartData()
})

watch(compareTimeRange, (newValue) => {
  fetchCompareData()
})

onMounted(async () => {
  fetchDashboardStats()
  
  // 延迟加载图表，确保DOM已渲染
  setTimeout(() => {
    fetchPurchaseChartData()
    fetchCategoryData()
    fetchCompareData()
  }, 300)
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-info h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.username {
  color: #3b82f6;
}

.date-info {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 6px;
  height: 100%;
  background: #3b82f6;
  border-radius: 0 3px 3px 0;
}

.stat-card-1::before {
  background: #3b82f6;
}

.stat-card-2::before {
  background: #10b981;
}

.stat-card-3::before {
  background: #f59e0b;
}

.stat-card-4::before {
  background: #ef4444;
}

.stat-value-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.stat-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
}

.stat-title {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 16px;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 13px;
  gap: 4px;
}

.trend-up {
  color: #10b981;
}

.trend-down {
  color: #ef4444;
}

.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 20px;
  height: 100%;
}

.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.chart-subtitle {
  font-size: 13px;
  color: #64748b;
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-range-select {
  width: 100px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.chart {
  width: 100%;
  height: 100%;
}

:deep(.el-empty) {
  padding: 40px 0;
}
</style> 