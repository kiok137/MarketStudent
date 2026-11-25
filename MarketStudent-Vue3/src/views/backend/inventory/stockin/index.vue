<template>
  <div class="page-container">
    <el-alert
      title="新增入库单后，对应商品库存会自动增加"
      type="info"
      show-icon
      :closable="false"
      class="stock-alert"
    />

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="入库单号">
          <el-input v-model="searchForm.stockInNo" placeholder="请输入入库单号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">新增入库</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="stockInNo" label="入库单号" width="180" show-overflow-tooltip />
      <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="quantity" label="入库数量" width="100" align="center" />
      <el-table-column label="批次号" width="140">
        <template #default="{ row }">{{ row.batchNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="关联采购单" width="170" show-overflow-tooltip>
        <template #default="{ row }">{{ row.orderNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="生产日期" width="120" align="center">
        <template #default="{ row }">{{ row.productionDate || '-' }}</template>
      </el-table-column>
      <el-table-column label="过期日期" width="120" align="center">
        <template #default="{ row }">{{ row.expiryDate || '-' }}</template>
      </el-table-column>
      <el-table-column prop="username" label="操作人" width="120" show-overflow-tooltip />
      <el-table-column label="入库时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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

    <el-dialog v-model="dialogVisible" title="新增入库单" width="620px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="form.productId" placeholder="请选择商品" filterable clearable style="width: 100%">
                <el-option v-for="item in productList" :key="item.productId" :label="item.productName" :value="item.productId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="1" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号">
              <el-input v-model="form.batchNo" placeholder="如 B20250101" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购订单ID">
              <el-input-number v-model="form.orderId" :min="1" :step="1" controls-position="right" placeholder="无关联可留空" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产日期">
              <el-date-picker v-model="form.productionDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择生产日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="过期日期">
              <el-date-picker v-model="form.expiryDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择过期日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const productList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  stockInNo: ''
})

const form = reactive({
  productId: null,
  quantity: 1,
  batchNo: '',
  orderId: null,
  productionDate: '',
  expiryDate: '',
  remark: ''
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }]
}

onMounted(() => {
  fetchData()
  fetchProducts()
})

const fetchData = async () => {
  loading.value = true
  try {
    await request.get('/inventory/stockin/page', {
      stockInNo: searchForm.stockInNo || undefined,
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
    console.error('获取入库列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchProducts = async () => {
  try {
    await request.get('/product/all', null, {
      showDefaultMsg: false,
      onSuccess: (res) => { productList.value = res || [] }
    })
  } catch (error) {
    console.error('获取商品列表失败', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.stockInNo = ''
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleAdd = () => {
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      // 可选字段为空时传 null，保持后端字段类型一致
      const payload = {
        productId: form.productId,
        quantity: form.quantity,
        batchNo: form.batchNo || null,
        orderId: form.orderId || null,
        productionDate: form.productionDate || null,
        expiryDate: form.expiryDate || null,
        remark: form.remark || null
      }
      await request.post('/inventory/stockin/add', payload, { successMsg: '入库单创建成功' })
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('入库失败', error)
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
  Object.assign(form, {
    productId: null,
    quantity: 1,
    batchNo: '',
    orderId: null,
    productionDate: '',
    expiryDate: '',
    remark: ''
  })
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

.stock-alert {
  margin-bottom: 16px;
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
</style>
