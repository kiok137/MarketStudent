<!-- 企业订单物流管理页面 -->
<template>
  <div class="logistics-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="订单编号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNumber">
          <el-input v-model="queryParams.trackingNumber" placeholder="请输入物流单号" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="物流状态" prop="shippingStatus">
          <el-select v-model="queryParams.shippingStatus" placeholder="请选择物流状态" clearable style="width: 200px;">
            <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 物流信息表格 -->
    <el-table v-loading="loading" :data="logisticsList" border :default-sort="{prop: 'logisticsId', order: 'ascending'}">
      <el-table-column label="物流ID" prop="logisticsId" width="80" sortable />
      <el-table-column label="订单编号" prop="orderNo" width="180" />
      <el-table-column label="物流单号" prop="trackingNumber" width="180" />
      <el-table-column label="物流公司" prop="shippingCompany" width="150" />
      <el-table-column label="物流状态" prop="shippingStatus" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.shippingStatus)">
            {{ getStatusText(scope.row.shippingStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发货时间" prop="shippingTime" width="180">
        <template #default="scope">
          <span>{{ formatDate(scope.row.shippingTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="送达时间" prop="deliveryTime" width="180">
        <template #default="scope">
          <span>{{ formatDate(scope.row.deliveryTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作人" prop="operatorName" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button type="primary" link @click="handleView(scope.row)">查看</el-button>
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="success" link @click="handleUpdateStatus(scope.row)" v-if="scope.row.shippingStatus !== 3">更新状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        :total="total"
        :current-page="queryParams.currentPage"
        :page-size="queryParams.pageSize"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>

    <!-- 查看物流详情对话框 -->
    <el-dialog title="物流详情" v-model="viewDialogVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="物流ID">{{ currentLogistics.logisticsId }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentLogistics.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ currentLogistics.trackingNumber }}</el-descriptions-item>
        <el-descriptions-item label="物流公司">{{ currentLogistics.shippingCompany }}</el-descriptions-item>
        <el-descriptions-item label="物流状态">
          <el-tag :type="getStatusType(currentLogistics.shippingStatus)">
            {{ getStatusText(currentLogistics.shippingStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ formatDate(currentLogistics.shippingTime) }}</el-descriptions-item>
        <el-descriptions-item label="送达时间">{{ formatDate(currentLogistics.deliveryTime) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLogistics.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentLogistics.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑物流信息对话框 -->
    <el-dialog title="编辑物流信息" v-model="editDialogVisible" width="600px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="物流单号" prop="trackingNumber">
          <el-input v-model="editForm.trackingNumber" placeholder="请输入物流单号" />
        </el-form-item>
        <el-form-item label="物流公司" prop="shippingCompany">
          <el-input v-model="editForm.shippingCompany" placeholder="请输入物流公司" />
        </el-form-item>
        <el-form-item label="物流状态" prop="shippingStatus">
          <el-select v-model="editForm.shippingStatus" placeholder="请选择物流状态">
            <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 更新物流状态对话框 -->
    <el-dialog title="更新物流状态" v-model="statusDialogVisible" width="500px">
      <el-form :model="statusForm" ref="statusFormRef" label-width="100px">
        <el-form-item label="物流状态" prop="status">
          <el-select v-model="statusForm.status" placeholder="请选择物流状态">
            <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" 
              :disabled="isStatusDisabled(dict.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="statusForm.remark" type="textarea" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatus">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();

// 物流列表数据
const logisticsList = ref([]);
const loading = ref(false);
const total = ref(0);

// 查询参数
const queryParams = reactive({
  orderNo: '',
  trackingNumber: '',
  shippingStatus: null,
  currentPage: 1,
  pageSize: 10
});

// 物流状态选项
const statusOptions = [
  { label: '待发货', value: 0 },
  { label: '已发货', value: 1 },
  { label: '运输中', value: 2 },
  { label: '已签收', value: 3 }
];

// 当前选中的物流信息
const currentLogistics = ref({});

// 查看详情对话框
const viewDialogVisible = ref(false);

// 编辑对话框
const editDialogVisible = ref(false);
const editFormRef = ref(null);
const editForm = ref({});
const editRules = {
  trackingNumber: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
  shippingCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  shippingStatus: [{ required: true, message: '请选择物流状态', trigger: 'change' }]
};

// 状态更新对话框
const statusDialogVisible = ref(false);
const statusFormRef = ref(null);
const statusForm = ref({});

// 加载物流数据
const getLogisticsList = () => {
  loading.value = true;
  request({
    url: '/enterprise/logistics/page',
    method: 'get',
    params: queryParams
  }).then(res => {
    logisticsList.value = res.data.records;
    total.value = res.data.total;
  }).finally(() => {
    loading.value = false;
  });
};

// 搜索按钮操作
const handleQuery = () => {
  queryParams.currentPage = 1;
  getLogisticsList();
};

// 重置按钮操作
const resetQuery = () => {
  queryParams.orderNo = '';
  queryParams.trackingNumber = '';
  queryParams.shippingStatus = null;
  handleQuery();
};

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  getLogisticsList();
};

// 分页页码改变
const handleCurrentChange = (val) => {
  queryParams.currentPage = val;
  getLogisticsList();
};

// 查看物流详情
const handleView = (row) => {
  viewDialogVisible.value = true;
  currentLogistics.value = { ...row };
};

// 编辑物流信息
const handleEdit = (row) => {
  editDialogVisible.value = true;
  editForm.value = { ...row };
};

// 提交编辑
const submitEdit = () => {
  editFormRef.value.validate(valid => {
    if (valid) {
      request({
        url: `/enterprise/logistics/${editForm.value.logisticsId}`,
        method: 'put',
        data: editForm.value
      }).then(() => {
        ElMessage.success('编辑成功');
        editDialogVisible.value = false;
        getLogisticsList();
      });
    }
  });
};

// 更新物流状态
const handleUpdateStatus = (row) => {
  statusDialogVisible.value = true;
  currentLogistics.value = { ...row };
  statusForm.value = {
    status: row.shippingStatus,
    operatorId: userStore.userId,
    remark: ''
  };
};

// 判断某些状态是否应该被禁用
const isStatusDisabled = (status) => {
  // 当前状态小于选项状态，且不是相邻状态
  return status > currentLogistics.value.shippingStatus + 1 || status < currentLogistics.value.shippingStatus;
};

// 提交状态更新
const submitStatus = () => {
  if (!statusForm.value.status) {
    ElMessage.warning('请选择物流状态');
    return;
  }
  
  request({
    url: `/enterprise/logistics/${currentLogistics.value.logisticsId}/status`,
    method: 'put',
    data: statusForm.value
  }).then(() => {
    ElMessage.success('状态更新成功');
    statusDialogVisible.value = false;
    getLogisticsList();
  });
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return '--';
  return new Date(date).toLocaleString();
};

// 根据状态获取文本
const getStatusText = (status) => {
  const option = statusOptions.find(item => item.value === status);
  return option ? option.label : '--';
};

// 根据状态获取标签类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info';
    case 1: return 'warning';
    case 2: return 'primary';
    case 3: return 'success';
    default: return 'info';
  }
};

// 页面加载时获取物流列表
onMounted(() => {
  getLogisticsList();
});
</script>

<style scoped>
.logistics-container {
  padding: 20px;
}
.search-bar {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 