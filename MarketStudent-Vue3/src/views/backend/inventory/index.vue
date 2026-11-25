<template>
  <div class="page-container">
    <el-alert
      v-if="lowStockCount > 0"
      :title="`当前有 ${lowStockCount} 个商品库存低于下限`"
      type="warning"
      show-icon
      :closable="false"
      class="stock-alert"
    />
    <el-alert
      v-else
      title="库存状态正常"
      type="success"
      show-icon
      :closable="false"
      class="stock-alert"
    />

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="仅看预警">
          <el-switch v-model="searchForm.lowStock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="warning" plain :loading="aiLoading" @click="handleAiAdvice">AI 补货建议</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="productName" label="商品名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="barcode" label="条码" width="160" show-overflow-tooltip />
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      <el-table-column prop="quantity" label="当前库存" width="110" align="center">
        <template #default="{ row }">
          <el-tag v-if="isLowStock(row)" type="danger" effect="plain">{{ row.quantity ?? 0 }}</el-tag>
          <span v-else>{{ row.quantity ?? 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="minQuantity" label="最低库存" width="100" align="center" />
      <el-table-column prop="maxQuantity" label="最高库存" width="100" align="center" />
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleThreshold(row)">设置阈值</el-button>
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

    <el-dialog v-model="dialogVisible" title="设置库存阈值" width="480px" @closed="resetThresholdForm">
      <el-form ref="formRef" :model="thresholdForm" :rules="thresholdRules" label-width="90px">
        <el-form-item label="商品名称">
          <el-input v-model="thresholdForm.productName" disabled />
        </el-form-item>
        <el-form-item label="最低库存" prop="minQuantity">
          <el-input-number v-model="thresholdForm.minQuantity" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最高库存" prop="maxQuantity">
          <el-input-number v-model="thresholdForm.maxQuantity" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adviceVisible" title="AI 补货建议" width="640px">
      <div v-loading="aiLoading" class="advice-wrap">
        <el-alert
          v-if="adviceItems.length > 0"
          :title="`本次分析覆盖 ${adviceItems.length} 个低于安全库存的商品`"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 12px"
        />
        <div class="advice-text">{{ adviceText }}</div>
      </div>
      <template #footer>
        <el-button @click="adviceVisible = false">关闭</el-button>
        <el-button type="warning" plain :loading="aiLoading" @click="handleAiAdvice">重新生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const aiLoading = ref(false)
const adviceVisible = ref(false)
const adviceText = ref('')
const adviceItems = ref([])
const tableData = ref([])
const lowStockCount = ref(0)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)

// 调用后端 AI 接口，基于低库存商品生成补货建议
const handleAiAdvice = async () => {
  adviceVisible.value = true
  aiLoading.value = true
  adviceText.value = ''
  try {
    await request.get('/ai/stock-advice', null, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        adviceText.value = (res && res.advice) || ''
        adviceItems.value = (res && res.items) || []
      }
    })
  } catch (error) {
    console.error('获取补货建议失败', error)
    adviceText.value = '生成失败，请检查 AI 密钥配置或稍后重试。'
  } finally {
    aiLoading.value = false
  }
}

const searchForm = reactive({
  productName: '',
  lowStock: false
})

const thresholdForm = reactive({
  inventoryId: null,
  productName: '',
  minQuantity: 0,
  maxQuantity: 0
})

const thresholdRules = {
  minQuantity: [{ required: true, message: '请输入最低库存', trigger: 'blur' }],
  maxQuantity: [
    { required: true, message: '请输入最高库存', trigger: 'blur' },
    {
      // 最高库存不能低于最低库存
      validator: (rule, value, callback) => {
        if (value !== null && value !== undefined && Number(value) < Number(thresholdForm.minQuantity)) {
          callback(new Error('最高库存不能低于最低库存'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(() => {
  fetchData()
  fetchLowStockCount()
})

const fetchData = async () => {
  loading.value = true
  try {
    await request.get('/inventory/page', {
      productName: searchForm.productName || undefined,
      lowStock: searchForm.lowStock ? true : undefined,
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
    console.error('获取库存列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchLowStockCount = async () => {
  try {
    await request.get('/inventory/low/count', null, {
      showDefaultMsg: false,
      onSuccess: (res) => { lowStockCount.value = Number(res) || 0 }
    })
  } catch (error) {
    console.error('获取库存预警数量失败', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.productName = ''
  searchForm.lowStock = false
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleThreshold = (row) => {
  thresholdForm.inventoryId = row.inventoryId
  thresholdForm.productName = row.productName
  thresholdForm.minQuantity = row.minQuantity ?? 0
  thresholdForm.maxQuantity = row.maxQuantity ?? 0
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    // 提交前再次校验最高库存与最低库存的关系
    if (Number(thresholdForm.maxQuantity) < Number(thresholdForm.minQuantity)) {
      ElMessage.warning('最高库存不能低于最低库存')
      return
    }
    submitting.value = true
    try {
      await request.put(
        `/inventory/${thresholdForm.inventoryId}/threshold?minQuantity=${thresholdForm.minQuantity}&maxQuantity=${thresholdForm.maxQuantity}`,
        null,
        { successMsg: '库存阈值设置成功' }
      )
      dialogVisible.value = false
      fetchData()
      fetchLowStockCount()
    } catch (error) {
      console.error('设置库存阈值失败', error)
    } finally {
      submitting.value = false
    }
  })
}

const resetThresholdForm = () => {
  if (formRef.value) formRef.value.resetFields()
  Object.assign(thresholdForm, {
    inventoryId: null,
    productName: '',
    minQuantity: 0,
    maxQuantity: 0
  })
}

// 判断库存是否低于最低库存
const isLowStock = (row) => {
  if (row.minQuantity === null || row.minQuantity === undefined) return false
  return Number(row.quantity ?? 0) < Number(row.minQuantity)
}

const formatDateTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.advice-wrap {
  min-height: 90px;
}

.advice-text {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
}

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

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
