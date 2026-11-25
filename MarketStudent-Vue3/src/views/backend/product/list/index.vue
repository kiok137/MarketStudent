<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width: 160px">
            <el-option v-for="item in categoryList" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option :value="1" label="上架" />
            <el-option :value="0" label="下架" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">新增商品</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="productId" label="ID" width="70" align="center" />
      <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="110" />
      <el-table-column prop="barcode" label="条码" width="140" />
      <el-table-column prop="unit" label="单位" width="70" align="center" />
      <el-table-column prop="retailPrice" label="零售价" width="100" align="right">
        <template #default="{ row }">￥{{ formatMoney(row.retailPrice) }}</template>
      </el-table-column>
      <el-table-column prop="costPrice" label="成本价" width="100" align="right">
        <template #default="{ row }">￥{{ formatMoney(row.costPrice) }}</template>
      </el-table-column>
      <el-table-column prop="inventory" label="库存" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.inventory > 0 ? 'success' : 'danger'" effect="plain">{{ row.inventory ?? 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="warning" size="small" @click="handleStatusChange(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="640px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="item in categoryList" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品条码" prop="barcode">
              <el-input v-model="form.barcode" placeholder="请输入商品条码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如：瓶 / 袋 / 箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="零售价格" prop="retailPrice">
              <el-input-number v-model="form.retailPrice" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价格" prop="costPrice">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品描述">
              <div class="ai-desc-wrap">
                <div class="ai-toolbar">
                  <el-input
                    v-model="form.features"
                    size="small"
                    placeholder="卖点关键词（可选），如：550ml、整箱24瓶、家庭装"
                    style="flex: 1; margin-right: 8px"
                  />
                  <el-button size="small" type="warning" plain :loading="aiLoading" @click="handleAiGenerate">
                    AI 生成文案
                  </el-button>
                </div>
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="4"
                  placeholder="点击「AI 生成文案」自动撰写，也可以手动输入"
                />
              </div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const aiLoading = ref(false)
const tableData = ref([])
const categoryList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  productName: '',
  categoryId: '',
  status: ''
})

const form = reactive({
  productId: null,
  productName: '',
  categoryId: '',
  barcode: '',
  unit: '瓶',
  retailPrice: 0,
  costPrice: 0,
  status: 1,
  features: '',
  description: ''
})

const rules = {
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }],
  retailPrice: [{ required: true, message: '请输入零售价格', trigger: 'blur' }],
  costPrice: [{ required: true, message: '请输入成本价格', trigger: 'blur' }]
}

onMounted(() => {
  fetchData()
  fetchCategories()
})

const fetchData = async () => {
  loading.value = true
  try {
    await request.get('/product/page', {
      productName: searchForm.productName || undefined,
      categoryId: searchForm.categoryId || undefined,
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
    console.error('获取商品列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    await request.get('/category/all', null, {
      showDefaultMsg: false,
      onSuccess: (res) => { categoryList.value = res || [] }
    })
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.productName = ''
  searchForm.categoryId = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.keys(form).forEach((key) => {
    if (row[key] !== undefined && row[key] !== null) {
      form[key] = row[key]
    }
  })
  dialogVisible.value = true
}

// 调用后端 AI 接口生成商品文案
const handleAiGenerate = async () => {
  if (!form.productName) {
    ElMessage.warning('请先填写商品名称')
    return
  }
  const category = categoryList.value.find((item) => item.categoryId === form.categoryId)
  aiLoading.value = true
  try {
    await request.post('/ai/product-description', {
      productName: form.productName,
      categoryName: category ? category.categoryName : '',
      features: form.features,
      tone: '简洁',
      wordLimit: 120
    }, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        form.description = res.description || ''
        ElMessage.success('文案已生成，可自行修改')
      }
    })
  } catch (error) {
    console.error('生成商品文案失败', error)
  } finally {
    aiLoading.value = false
  }
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = { ...form }
      if (isEdit.value) {
        await request.put(`/product/${form.productId}`, payload, { successMsg: '商品更新成功' })
      } else {
        delete payload.productId
        await request.post('/product/add', payload, { successMsg: '商品创建成功' })
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('保存商品失败', error)
    } finally {
      submitting.value = false
    }
  })
}

const handleStatusChange = async (row) => {
  const target = row.status === 1 ? 0 : 1
  try {
    await request.put(`/product/status/${row.productId}?status=${target}`, null, {
      successMsg: target === 1 ? '商品已上架' : '商品已下架'
    })
    fetchData()
  } catch (error) {
    console.error('修改状态失败', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除商品「${row.productName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/product/delete/${row.productId}`, { successMsg: '删除成功' })
      fetchData()
    } catch (error) {
      console.error('删除商品失败', error)
    }
  }).catch(() => {})
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
  Object.assign(form, {
    productId: null,
    productName: '',
    categoryId: '',
    barcode: '',
    unit: '瓶',
    retailPrice: 0,
    costPrice: 0,
    status: 1,
    features: '',
    description: ''
  })
}

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
.ai-desc-wrap {
  width: 100%;
}

.ai-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

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
