<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="请选择供应商" clearable style="width: 180px">
            <el-option v-for="item in supplierList" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">新增评价</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="supplierName" label="供应商" min-width="160" show-overflow-tooltip />
      <el-table-column label="采购单号" width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.orderNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="配送速度" width="145" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.deliverySpeedScore || 0" disabled :max="5" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="商品质量" width="145" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.qualityScore || 0" disabled :max="5" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="服务态度" width="145" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.serviceScore || 0" disabled :max="5" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="价格" width="145" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.priceScore || 0" disabled :max="5" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="总分" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getTotalScoreType(row.totalScore)">{{ row.totalScore ?? 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评价内容" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.comment || '-' }}</template>
      </el-table-column>
      <el-table-column label="评价时间" width="170">
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

    <el-dialog v-model="dialogVisible" title="新增评价" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option v-for="item in supplierList" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="采购订单ID" prop="orderId">
          <el-input-number v-model="form.orderId" :min="1" :step="1" :precision="0" controls-position="right" placeholder="关联采购订单ID，可不填" style="width: 100%" />
        </el-form-item>
        <el-form-item label="配送速度" prop="deliverySpeedScore">
          <el-rate v-model="form.deliverySpeedScore" :max="5" />
        </el-form-item>
        <el-form-item label="商品质量" prop="qualityScore">
          <el-rate v-model="form.qualityScore" :max="5" />
        </el-form-item>
        <el-form-item label="服务态度" prop="serviceScore">
          <el-rate v-model="form.serviceScore" :max="5" />
        </el-form-item>
        <el-form-item label="价格" prop="priceScore">
          <el-rate v-model="form.priceScore" :max="5" />
        </el-form-item>
        <el-form-item label="评价内容" prop="comment">
          <el-input v-model="form.comment" type="textarea" :rows="3" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <div class="total-score">总分：{{ formTotalScore }} / 20</div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const supplierList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  supplierId: ''
})

const form = reactive({
  supplierId: null,
  orderId: null,
  deliverySpeedScore: 0,
  qualityScore: 0,
  serviceScore: 0,
  priceScore: 0,
  comment: ''
})

// 总分由四项评分实时累加得出，仅用于展示，提交时由后端计算
const formTotalScore = computed(() => {
  return (form.deliverySpeedScore || 0) + (form.qualityScore || 0) + (form.serviceScore || 0) + (form.priceScore || 0)
})

const rules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  deliverySpeedScore: [{ required: true, type: 'number', min: 1, max: 5, message: '请完成配送速度评分', trigger: 'change' }],
  qualityScore: [{ required: true, type: 'number', min: 1, max: 5, message: '请完成商品质量评分', trigger: 'change' }],
  serviceScore: [{ required: true, type: 'number', min: 1, max: 5, message: '请完成服务态度评分', trigger: 'change' }],
  priceScore: [{ required: true, type: 'number', min: 1, max: 5, message: '请完成价格评分', trigger: 'change' }]
}

onMounted(() => {
  fetchData()
  fetchSuppliers()
})

const fetchData = async () => {
  loading.value = true
  try {
    await request.get('/supplier/evaluation/page', {
      supplierId: searchForm.supplierId || undefined,
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
    console.error('获取评价列表失败', error)
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

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.supplierId = ''
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
      const payload = {
        supplierId: form.supplierId,
        orderId: form.orderId || null,
        deliverySpeedScore: form.deliverySpeedScore,
        qualityScore: form.qualityScore,
        serviceScore: form.serviceScore,
        priceScore: form.priceScore,
        comment: form.comment
      }
      await request.post('/supplier/evaluation/add', payload, { successMsg: '评价提交成功' })
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('提交评价失败', error)
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
  Object.assign(form, {
    supplierId: null,
    orderId: null,
    deliverySpeedScore: 0,
    qualityScore: 0,
    serviceScore: 0,
    priceScore: 0,
    comment: ''
  })
}

const getTotalScoreType = (score) => {
  const value = Number(score) || 0
  if (value >= 16) return 'success'
  if (value >= 10) return 'warning'
  return 'danger'
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

.total-score {
  padding-right: 12px;
  text-align: right;
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
}
</style>
