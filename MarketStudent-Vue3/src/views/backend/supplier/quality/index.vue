<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="请选择供应商" clearable style="width: 180px">
            <el-option v-for="item in supplierList" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="质量等级">
          <el-select v-model="searchForm.qualityLevel" placeholder="请选择质量等级" clearable style="width: 140px">
            <el-option :value="1" label="优" />
            <el-option :value="2" label="良" />
            <el-option :value="3" label="中" />
            <el-option :value="4" label="差" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">新增质量评价</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="qualityId" label="ID" width="70" align="center" />
      <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="supplierName" label="供应商" min-width="140" show-overflow-tooltip />
      <el-table-column prop="batchNo" label="批次号" width="150" show-overflow-tooltip />
      <el-table-column label="质量等级" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getLevelType(row.qualityLevel)">{{ getLevelLabel(row.qualityLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="评价内容" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userName" label="评价人" width="110" />
      <el-table-column label="评价时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增质量评价" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width: 100%">
            <el-option v-for="item in productList" :key="item.productId" :label="item.productName" :value="item.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" filterable style="width: 100%">
            <el-option v-for="item in supplierList" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="form.batchNo" placeholder="请输入批次号" clearable />
        </el-form-item>
        <el-form-item label="质量等级" prop="qualityLevel">
          <el-radio-group v-model="form.qualityLevel">
            <el-radio :value="1">优</el-radio>
            <el-radio :value="2">良</el-radio>
            <el-radio :value="3">中</el-radio>
            <el-radio :value="4">差</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="form.comment" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
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
import { ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const productList = ref([])
const supplierList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)

// 质量等级映射
const levelMap = {
  1: { label: '优', type: 'success' },
  2: { label: '良', type: 'primary' },
  3: { label: '中', type: 'warning' },
  4: { label: '差', type: 'danger' }
}

const searchForm = reactive({
  supplierId: '',
  qualityLevel: ''
})

const form = reactive({
  productId: '',
  supplierId: '',
  batchNo: '',
  qualityLevel: 1,
  comment: ''
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  batchNo: [
    { required: true, message: '请输入批次号', trigger: 'blur' },
    { max: 50, message: '批次号长度不能超过 50 个字符', trigger: 'blur' }
  ],
  qualityLevel: [{ required: true, message: '请选择质量等级', trigger: 'change' }]
}

onMounted(() => {
  fetchData()
  fetchProducts()
  fetchSuppliers()
})

const fetchData = async () => {
  loading.value = true
  try {
    await request.get('/quality/page', {
      supplierId: searchForm.supplierId || undefined,
      qualityLevel: searchForm.qualityLevel === '' ? undefined : searchForm.qualityLevel,
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
    console.error('获取质量评价列表失败', error)
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
    console.error('获取商品失败', error)
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
  searchForm.qualityLevel = ''
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
      await request.post('/quality/add', {
        productId: form.productId,
        supplierId: form.supplierId,
        batchNo: form.batchNo,
        qualityLevel: form.qualityLevel,
        comment: form.comment
      }, { successMsg: '质量评价添加成功' })
      dialogVisible.value = false
      handleSearch()
    } catch (error) {
      console.error('新增质量评价失败', error)
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除商品「${row.productName}」批次「${row.batchNo}」的质量评价吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/quality/delete/${row.qualityId}`, { successMsg: '删除成功' })
      fetchData()
    } catch (error) {
      console.error('删除质量评价失败', error)
    }
  }).catch(() => {})
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
  Object.assign(form, {
    productId: '',
    supplierId: '',
    batchNo: '',
    qualityLevel: 1,
    comment: ''
  })
}

const getLevelLabel = (level) => levelMap[level]?.label || '-'

const getLevelType = (level) => levelMap[level]?.type || 'info'

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
</style>
