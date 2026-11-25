<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="请选择供应商" clearable style="width: 180px">
            <el-option v-for="item in supplierList" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option :value="0" label="待审核" />
            <el-option :value="1" label="已审核" />
            <el-option :value="2" label="已收货" />
            <el-option :value="3" label="已取消" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">新建采购订单</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" min-width="170" show-overflow-tooltip />
      <el-table-column prop="supplierName" label="供应商" min-width="140" show-overflow-tooltip />
      <el-table-column prop="username" label="下单人" width="110" />
      <el-table-column label="订单金额" width="120" align="right">
        <template #default="{ row }">￥{{ formatMoney(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="270" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleDetail(row)">查看明细</el-button>
          <el-button v-if="row.status === 0" link type="warning" size="small" @click="handleApprove(row)">审核</el-button>
          <el-button v-if="row.status === 1" link type="warning" size="small" @click="handleReceive(row)">收货</el-button>
          <el-button v-if="row.status === 0 || row.status === 1" link type="warning" size="small" @click="handleCancel(row)">取消</el-button>
          <el-button v-if="row.status !== 2" link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        background
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="fetchData"
      />
    </div>

    <!-- 新建采购订单 -->
    <el-dialog v-model="dialogVisible" title="新建采购订单" width="900px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable style="width: 100%">
                <el-option v-for="item in supplierList" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="请输入备注" clearable />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="detail-header">
        <span class="detail-title">采购明细</span>
        <el-button type="primary" plain size="small" @click="handleAddDetail">添加明细</el-button>
      </div>

      <el-table :data="form.detailList" border size="small" style="width: 100%">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <el-select v-model="row.productId" placeholder="请选择商品" filterable style="width: 100%" @change="handleProductChange(row)">
              <el-option v-for="item in productList" :key="item.productId" :label="item.productName" :value="item.productId" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="140" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :precision="0" :step="1" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="单价" width="150" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.price" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120" align="right">
          <template #default="{ row }">￥{{ formatMoney(getDetailAmount(row)) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="handleRemoveDetail($index)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <span class="table-empty">暂无明细，请点击「添加明细」</span>
        </template>
      </el-table>

      <div class="total-bar">
        <span>合计金额：</span>
        <span class="total-amount">￥{{ formatMoney(totalAmount) }}</span>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 订单明细查看 -->
    <el-dialog v-model="detailVisible" title="采购订单明细" width="800px">
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="订单号">{{ detailOrder.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detailOrder.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailOrder.status)">{{ getStatusLabel(detailOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(detailOrder.createdAt) || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="detailOrder.detailList || []" border stripe size="small" style="width: 100%">
          <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="productUnit" label="单位" width="70" align="center" />
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column label="单价" width="100" align="right">
            <template #default="{ row }">￥{{ formatMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="110" align="right">
            <template #default="{ row }">￥{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="已收货数量" width="110" align="center">
            <template #default="{ row }">{{ row.receivedQuantity ?? 0 }}</template>
          </el-table-column>
        </el-table>

        <div class="total-bar">
          <span>合计金额：</span>
          <span class="total-amount">￥{{ formatMoney(detailOrder.totalAmount) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const detailLoading = ref(false)
const tableData = ref([])
const supplierList = ref([])
const productList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailOrder = ref({})
const formRef = ref(null)

// 订单状态映射
const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已审核', type: 'primary' },
  2: { label: '已收货', type: 'success' },
  3: { label: '已取消', type: 'info' }
}

const searchForm = reactive({
  orderNo: '',
  supplierId: '',
  status: ''
})

const form = reactive({
  supplierId: '',
  remark: '',
  detailList: []
})

const rules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
}

// 合计金额：所有明细小计之和
const totalAmount = computed(() =>
  form.detailList.reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.price || 0), 0)
)

onMounted(() => {
  fetchData()
  fetchSuppliers()
  fetchProducts()
})

const fetchData = async () => {
  loading.value = true
  try {
    await request.get('/purchase/page', {
      orderNo: searchForm.orderNo || undefined,
      supplierId: searchForm.supplierId || undefined,
      status: searchForm.status === '' ? undefined : searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取采购订单列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchSuppliers = async () => {
  try {
    await request.get('/supplier/all', null, {
      showDefaultMsg: false,
      onSuccess: (res) => { supplierList.value = res || [] }
    })
  } catch (error) {
    console.error('获取供应商失败', error)
  }
}

const fetchProducts = async () => {
  try {
    await request.get('/product/all', null, {
      showDefaultMsg: false,
      onSuccess: (res) => { productList.value = res || [] }
    })
  } catch (error) {
    console.error('获取商品失败', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.supplierId = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleAdd = () => {
  dialogVisible.value = true
}

const handleAddDetail = () => {
  form.detailList.push({ productId: '', quantity: 1, price: 0 })
}

const handleRemoveDetail = (index) => {
  form.detailList.splice(index, 1)
}

// 选择商品后自动带出单价（成本价）
const handleProductChange = (row) => {
  const product = productList.value.find((item) => item.productId === row.productId)
  if (product) {
    row.price = Number(product.costPrice) || 0
  }
}

const getDetailAmount = (row) => {
  return Number(row.quantity || 0) * Number(row.price || 0)
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!form.detailList.length) {
      ElMessage.warning('请至少添加一条采购明细')
      return
    }
    const invalid = form.detailList.some((item) => !item.productId || !item.quantity)
    if (invalid) {
      ElMessage.warning('请完善明细中的商品和数量')
      return
    }
    submitting.value = true
    try {
      await request.post('/purchase/add', {
        supplierId: form.supplierId,
        remark: form.remark,
        detailList: form.detailList.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price
        }))
      }, { successMsg: '采购订单创建成功' })
      dialogVisible.value = false
      handleSearch()
    } catch (error) {
      console.error('创建采购订单失败', error)
    } finally {
      submitting.value = false
    }
  })
}

const handleApprove = async (row) => {
  try {
    await request.put(`/purchase/${row.orderId}/approve`, null, { successMsg: '审核成功' })
    fetchData()
  } catch (error) {
    console.error('审核采购订单失败', error)
  }
}

const handleReceive = async (row) => {
  try {
    await request.put(`/purchase/${row.orderId}/receive`, null, { successMsg: '收货成功，库存已更新' })
    fetchData()
  } catch (error) {
    console.error('采购订单收货失败', error)
  }
}

const handleCancel = (row) => {
  ElMessageBox.confirm(`确定要取消采购订单「${row.orderNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/purchase/${row.orderId}/cancel`, null, { successMsg: '订单已取消' })
      fetchData()
    } catch (error) {
      console.error('取消采购订单失败', error)
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除采购订单「${row.orderNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/purchase/delete/${row.orderId}`, { successMsg: '删除成功' })
      fetchData()
    } catch (error) {
      console.error('删除采购订单失败', error)
    }
  }).catch(() => {})
}

const handleDetail = async (row) => {
  detailVisible.value = true
  detailOrder.value = {}
  detailLoading.value = true
  try {
    await request.get(`/purchase/${row.orderId}`, null, {
      showDefaultMsg: false,
      onSuccess: (res) => { detailOrder.value = res || {} }
    })
  } catch (error) {
    console.error('获取采购订单详情失败', error)
  } finally {
    detailLoading.value = false
  }
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
  form.supplierId = ''
  form.remark = ''
  form.detailList = []
}

const getStatusLabel = (status) => statusMap[status]?.label || '-'

const getStatusType = (status) => statusMap[status]?.type || 'info'

const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

const formatDateTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-bar {
  margin-bottom: 16px;
  padding: 18px 18px 0;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.action-bar {
  margin-bottom: 16px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.detail-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.table-empty {
  color: #909399;
  font-size: 13px;
}

.total-bar {
  margin-top: 16px;
  text-align: right;
  font-size: 14px;
  color: #606266;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}
</style>
