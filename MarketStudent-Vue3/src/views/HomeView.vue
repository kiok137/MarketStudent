<template>
  <div class="welcome-page">
    <!-- 顶部横幅区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1>智慧超市管理系统</h1>
        <p>整合供应链全流程的企业级管理解决方案，提升运营效率，赋能业务增长</p>
        <el-button type="primary" size="large" @click="goToBackend">管理中心入口</el-button>
      </div>
    </div>

    <!-- 系统亮点区域 -->
    <div class="features-section">
      <h2>核心优势</h2>
      <div class="features-grid">
        <div class="feature-card">
          <el-icon><Goods /></el-icon>
          <h3>全渠道商品管理</h3>
          <p>精细化商品全生命周期管理，SKU智能分类，一体化价格策略，多维度属性配置</p>
        </div>
        
        <div class="feature-card">
          <el-icon><ShoppingCart /></el-icon>
          <h3>智能供应链协同</h3>
          <p>需求预测驱动采购规划，供应商评估体系，多级审批流程，降低库存资金占用</p>
        </div>
        
        <div class="feature-card">
          <el-icon><Box /></el-icon>
          <h3>精益库存管控</h3>
          <p>实时库存可视化，智能补货预警，批次追溯管理，库存周转分析，降低运营成本</p>
        </div>
        
        <div class="feature-card">
          <el-icon><DataAnalysis /></el-icon>
          <h3>数据驱动经营决策</h3>
          <p>多维度业绩分析，销售趋势预测，供应商绩效评估，可视化商业智能报表</p>
        </div>
      </div>
    </div>

    <!-- 热门商品区域 -->
    <div class="products-section">
      <h2>精选商品展示</h2>
      
      <el-empty v-if="!products.length" description="正在加载商品数据..." />
      
      <div v-else class="product-grid">
        <div v-for="product in products" :key="product.productId" class="product-card">
          <div class="product-image">
            <el-image 
              :src="handleImage(product.imageUrl) || defaultProductImage" 
              fit="cover"
              @click="previewImage(product.imageUrl || defaultProductImage)"
              :preview-src-list="[]"
              @error="() => product.imageUrl = defaultProductImage"
            />
          </div>
          <div class="product-info">
            <h3>{{ product.productName }}</h3>
            <div class="product-meta">
              <el-tag size="small">{{ product.categoryName }}</el-tag>
              <el-tag 
                :type="getInventoryTagType(product.inventory)"
                size="small"
              >
                库存: {{ product.inventory || 0 }}
              </el-tag>
            </div>
            <div class="product-price">
              <span class="price">¥{{ product.retailPrice }}</span>
              <span class="original-price" v-if="product.costPrice < product.retailPrice">¥{{ product.costPrice }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 系统流程区域 -->
    <div class="workflow-section">
      <h2>业务流程闭环</h2>
      <div class="workflow-steps">
        <div class="workflow-step">
          <div class="step-number">01</div>
          <div class="step-content">
            <h3>商品与供应链基础治理</h3>
            <p>构建标准化商品数据体系，精细化分类管理，供应商资质评估与分级</p>
          </div>
        </div>
        
        <div class="workflow-step">
          <div class="step-number">02</div>
          <div class="step-content">
            <h3>采购计划与订单执行</h3>
            <p>基于销售预测的智能采购规划，全流程订单追踪，供应商协同管理平台</p>
          </div>
        </div>
        
        <div class="workflow-step">
          <div class="step-number">03</div>
          <div class="step-content">
            <h3>仓储物流与库存优化</h3>
            <p>精准入库验收，批次与效期管理，库存预警阈值调控，自动化盘点系统</p>
          </div>
        </div>
        
        <div class="workflow-step">
          <div class="step-number">04</div>
          <div class="step-content">
            <h3>经营分析与战略决策</h3>
            <p>多维度销售绩效分析，库存周转效率评估，供应商质量评价，精准决策支持</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 企业直发服务区域 -->
    <div class="enterprise-section">
      <div class="enterprise-content">
        <h2>企业直发解决方案</h2>
        <p>提供B2B企业订单管理与履约平台，实现从采购到配送的全链路可视化，降低中间环节成本，优化供应链效率</p>
        <ul class="enterprise-features">
          <li><el-icon><Check /></el-icon> 企业客户精细化分层管理</li>
          <li><el-icon><Check /></el-icon> 订单全生命周期追踪系统</li>
          <li><el-icon><Check /></el-icon> 物流配送可视化管控平台</li>
          <li><el-icon><Check /></el-icon> 供应商直发协同体系</li>
        </ul>
      </div>
    </div>

    <!-- 底部区域 -->
    <div class="footer">
      <p>© {{ new Date().getFullYear() }} 智慧超市管理系统 | 赋能零售企业数字化转型的专业技术平台</p>
    </div>

    <!-- 图片预览组件 -->
    <el-image-viewer
      v-if="showViewer"
      :url-list="previewList"
      :initial-index="previewIndex"
      :infinite="false"
      :z-index="9999"
      @close="closeViewer"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { 
  Goods, 
  ShoppingCart, 
  Box, 
  DataAnalysis, 
  Check,
  ElImageViewer
} from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
const products = ref([])
const defaultProductImage = '/api/images/default-product.png'
import { useRouter, useRoute } from 'vue-router'
// 图片预览相关
const showViewer = ref(false)
const previewList = ref([])
const previewIndex = ref(0)
const route = useRoute()
const router = useRouter()
// 预览图片方法
const previewImage = (url) => {
  const imageUrl = url.startsWith('/api') ? url : `/api${url}`
  previewList.value = [imageUrl]
  previewIndex.value = 0
  showViewer.value = true
}
const handleImage=(url)=>{
  const imageUrl = url.startsWith('/api') ? url : `/api${url}`
  return imageUrl
}

// 关闭图片预览
const closeViewer = () => {
  showViewer.value = false
}

// 跳转到后台
const goToBackend = () => {
  userStore.token
  const token =userStore.token
  if (token) {
    // 已登录，直接进入后台
    router.push('/back/dashboard')
  } else {
    // 未登录，先转到登录页面
    router.push({
      path: '/login',
      query: { redirect: '/back/dashboard' }
    })
  }
}

// 获取商品数据
const fetchFeaturedProducts = async () => {
  try {
    // 获取热门商品，这里简化为获取所有商品的前6个
    await request.get('/product/all', null, {
      onSuccess: (res) => {
        // 获取前6个商品作为热门商品
        let productList = Array.isArray(res) ? res.slice(0, 6) : []
        
        // 处理图片URL，添加API前缀
        productList = productList.map(product => {
          if (product.imageUrl && !product.imageUrl.startsWith('/api') && !product.imageUrl.startsWith('http')) {
            product.imageUrl = `/api${product.imageUrl}`
          }
          return product
        })
        
        products.value = productList
      },
      onError: (error) => {
        console.error('获取热门商品失败', error)
        // 使用模拟数据作为备选
        products.value = generateMockProducts()
      }
    })
  } catch (error) {
    console.error('获取热门商品失败', error)
    // 使用模拟数据作为备选
    products.value = generateMockProducts()
  }
}

// 生成模拟商品数据
const generateMockProducts = () => {
  return [
  //   {
  //     productId: 1,
  //     productName: '有机鲜牛奶',
  //     categoryName: '乳制品',
  //     retailPrice: 12.8,
  //     costPrice: 8.5,
  //     imageUrl: '/api/images/products/milk.jpeg',
  //     inventory: 100,
  //     status: 1
  //   },
  //   {
  //     productId: 2,
  //     productName: '新鲜水果组合',
  //     categoryName: '生鲜水果',
  //     retailPrice: 35.9,
  //     costPrice: 25.0,
  //     imageUrl: '/api/images/products/fruits.jpeg',
  //     inventory: 50,
  //     status: 1
  //   },
  //   {
  //     productId: 3,
  //     productName: '进口巧克力',
  //     categoryName: '零食糖果',
  //     retailPrice: 25.5,
  //     costPrice: 15.0,
  //     imageUrl: '/api/images/products/chocolate.jpeg',
  //     inventory: 80,
  //     status: 1
  //   },
  //   {
  //     productId: 4,
  //     productName: '特级橄榄油',
  //     categoryName: '粮油调味',
  //     retailPrice: 68.0,
  //     costPrice: 45.0,
  //     imageUrl: '/api/images/products/oliveoil.jpeg',
  //     inventory: 30,
  //     status: 1
  //   },
  //   {
  //     productId: 5,
  //     productName: '防晒霜',
  //     categoryName: '个人护理',
  //     retailPrice: 89.9,
  //     costPrice: 60.0,
  //     imageUrl: '/api/images/products/sunscreen.jpeg',
  //     inventory: 5,
  //     status: 1
  //   },
  //   {
  //     productId: 6,
  //     productName: '婴儿纸尿裤',
  //     categoryName: '母婴用品',
  //     retailPrice: 108.0,
  //     costPrice: 85.0,
  //     imageUrl: '/api/images/products/diapers.jpeg',
  //     inventory: 0,
  //     status: 1
  //   }
  ]
}

// 获取库存标签类型
const getInventoryTagType = (inventory) => {
  if (inventory === 0) return 'danger'
  if (inventory < 10) return 'warning'
  return 'success'
}

// 页面加载时获取数据
onMounted(() => {
  fetchFeaturedProducts()
})
</script>

<style scoped>
.welcome-page {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  color: #333;
}

/* 顶部横幅 - 扁平化风格 */
.hero-section {
  height: 400px;
  background-color: #1976d2;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: white;
  position: relative;
}

.hero-content {
  max-width: 800px;
  padding: 0 20px;
  z-index: 1;
}

.hero-section h1 {
  font-size: 3rem;
  margin-bottom: 16px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.hero-section p {
  font-size: 1.2rem;
  margin-bottom: 32px;
  opacity: 0.9;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

/* 系统亮点区域 - 扁平化卡片 */
.features-section {
  padding: 80px 24px;
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

h2 {
  font-size: 2rem;
  margin-bottom: 48px;
  font-weight: 600;
  color: #1976d2;
  position: relative;
  display: inline-block;
}

h2::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 4px;
  background-color: #1976d2;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  margin-top: 20px;
}

.feature-card {
  padding: 32px 24px;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
  text-align: left;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
}

.feature-card .el-icon {
  font-size: 2.5rem;
  margin-bottom: 16px;
  color: #1976d2;
}

.feature-card h3 {
  font-size: 1.3rem;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.feature-card p {
  color: #666;
  margin: 0;
  line-height: 1.5;
}

/* 热销商品区域 - 扁平风格网格 */
.products-section {
  padding: 80px 24px;
  background-color: #f5f7fa;
  text-align: center;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.product-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s, box-shadow 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.product-image {
  height: 200px;
  cursor: pointer;
}

.product-image .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
}

.product-card:hover .product-image .el-image {
  transform: scale(1.05);
}

.product-info {
  padding: 20px;
  text-align: left;
}

.product-info h3 {
  margin: 0 0 12px 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price {
  font-size: 1.3rem;
  font-weight: 600;
  color: #f56c6c;
}

.original-price {
  font-size: 0.9rem;
  color: #999;
  text-decoration: line-through;
}

/* 工作流程区域 - 简洁时间线风格 */
.workflow-section {
  padding: 80px 24px;
  max-width: 1000px;
  margin: 0 auto;
  text-align: center;
}

.workflow-steps {
  margin-top: 40px;
}

.workflow-step {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 40px;
  text-align: left;
}

.step-number {
  width: 44px;
  height: 44px;
  border-radius: 22px;
  background-color: #1976d2;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: 600;
  flex-shrink: 0;
}

.step-content h3 {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.step-content p {
  margin: 0;
  color: #666;
  line-height: 1.5;
}

/* 企业直发区域 - 简洁区块 */
.enterprise-section {
  padding: 80px 24px;
  background-color: #e3f2fd;
  text-align: center;
}

.enterprise-content {
  max-width: 1200px;
  margin: 0 auto;
}

.enterprise-content p {
  margin-bottom: 32px;
  color: #333;
  font-size: 1.1rem;
}

.enterprise-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.enterprise-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.enterprise-features .el-icon {
  color: #4caf50;
  font-size: 1.2rem;
}

/* 底部区域 */
.footer {
  background-color: #263238;
  color: white;
  padding: 24px;
  text-align: center;
}

.footer p {
  margin: 0;
  opacity: 0.8;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .hero-section {
    height: 320px;
  }
  
  .hero-section h1 {
    font-size: 2.2rem;
  }
  
  .hero-section p {
    font-size: 1rem;
  }
  
  h2 {
    font-size: 1.6rem;
  }
  
  .enterprise-features {
    grid-template-columns: 1fr;
  }
  
  .workflow-step {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}
</style>
