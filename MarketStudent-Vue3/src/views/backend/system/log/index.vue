<!-- 系统日志管理页面 -->
<template>
  <div class="log-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="操作类型" prop="operationType">
          <el-select v-model="queryParams.operationType" placeholder="请选择操作类型" clearable style="width: 200px;">
            <el-option v-for="dict in operationTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块" prop="operationModule">
          <el-input v-model="queryParams.operationModule" placeholder="请输入操作模块" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="IP地址" prop="ipAddress">
          <el-input v-model="queryParams.ipAddress" placeholder="请输入IP地址" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px;">
            <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮栏 -->
    <div class="action-bar">
      <el-button type="danger" icon="Delete" @click="handleClearLog">清空日志</el-button>
    </div>

    <!-- 系统日志表格 -->
    <el-table v-loading="loading" :data="logList" border :default-sort="{prop: 'logId', order: 'ascending'}">
      <el-table-column label="日志ID" prop="logId" width="80" sortable />
      <el-table-column label="用户" width="120">
        <template #default="scope">
          <span>{{ scope.row.username || '未知用户' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" prop="operationType" width="100">
        <template #default="scope">
          <el-tag :type="getOperationTypeTag(scope.row.operationType)">
            {{ getOperationTypeText(scope.row.operationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作模块" prop="operationModule" width="120" />
      <el-table-column label="操作描述" prop="operationDesc" width="180" />
      <el-table-column label="IP地址" prop="ipAddress" width="120" />
      <el-table-column label="操作时间" prop="operationTime" width="180">
        <template #default="scope">
          <span>{{ formatDate(scope.row.operationTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="执行时长" prop="executionTime" width="100">
        <template #default="scope">
          <span>{{ scope.row.executionTime ? `${scope.row.executionTime}ms` : '--' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="primary" link @click="handleView(scope.row)">查看详情</el-button>
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

    <!-- 日志详情对话框 -->
    <el-dialog title="日志详情" v-model="viewDialogVisible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog.logId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentLog.userId }}
          <span v-if="currentLog.username">({{ currentLog.username }})</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(currentLog.operationType)">
            {{ getOperationTypeText(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.operationModule }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentLog.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDate(currentLog.operationTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ currentLog.executionTime ? `${currentLog.executionTime}ms` : '--' }}</el-descriptions-item>
        <el-descriptions-item label="设备信息" :span="2">{{ currentLog.deviceInfo || '--' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="params-container">
            {{ currentLog.requestParams || '--' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="异常信息" :span="2" v-if="currentLog.status === 0">
          <div class="exception-container">
            {{ currentLog.exceptionInfo || '--' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

// 日志列表数据
const logList = ref([]);
const loading = ref(false);
const total = ref(0);
const dateRange = ref([]);

// 查询参数
const queryParams = reactive({
  username: '',
  operationType: '',
  operationModule: '',
  ipAddress: '',
  status: '',
  startTime: '',
  endTime: '',
  currentPage: 1,
  pageSize: 10
});

// 操作类型选项
const operationTypeOptions = [
  { label: '登录', value: 0 },
  { label: '登出', value: 1 },
  { label: '新增', value: 2 },
  { label: '删除', value: 3 },
  { label: '修改', value: 4 },
  { label: '查询', value: 5 },
  { label: '其他', value: 6 }
];

// 状态选项
const statusOptions = [
  { label: '失败', value: 0 },
  { label: '成功', value: 1 }
];

// 当前选中的日志
const currentLog = ref({});

// 查看详情对话框
const viewDialogVisible = ref(false);

// 加载日志数据
const getLogList = () => {
  loading.value = true;
  
  // 处理日期范围
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startTime = dateRange.value[0];
    queryParams.endTime = dateRange.value[1];
  } else {
    queryParams.startTime = null;
    queryParams.endTime = null;
  }
  
  request.get('/systemLog/page', queryParams)
    .then(res => {
      logList.value = res.records;
      total.value = res.total;
    })
    .finally(() => {
      loading.value = false;
    });
};

// 搜索按钮操作
const handleQuery = () => {
  queryParams.currentPage = 1;
  getLogList();
};

// 重置按钮操作
const resetQuery = () => {
  queryParams.username = '';
  queryParams.operationType = '';
  queryParams.operationModule = '';
  queryParams.ipAddress = '';
  queryParams.status = '';
  dateRange.value = [];
  handleQuery();
};

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  getLogList();
};

// 分页页码改变
const handleCurrentChange = (val) => {
  queryParams.currentPage = val;
  getLogList();
};

// 查看日志详情
const handleView = (row) => {
  loading.value = true;
  request.get(`/systemLog/${row.logId}`)
    .then(res => {
      currentLog.value = res;
      viewDialogVisible.value = true;
    })
    .finally(() => {
      loading.value = false;
    });
};

// 清空日志
const handleClearLog = () => {
  ElMessageBox.confirm('确认要清空所有系统日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loading.value = true;
    request.delete('/systemLog/clear')
      .then(() => {
        ElMessage.success('清空日志成功');
        getLogList();
      })
      .catch(() => {
        ElMessage.error('清空日志失败');
      })
      .finally(() => {
        loading.value = false;
      });
  }).catch(() => {
    // 用户取消操作
  });
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return '--';
  return new Date(date).toLocaleString();
};

// 获取操作类型文本
const getOperationTypeText = (type) => {
  const option = operationTypeOptions.find(item => item.value === type);
  return option ? option.label : '--';
};

// 获取操作类型标签样式
const getOperationTypeTag = (type) => {
  switch (type) {
    case 0: return 'info';     // 登录
    case 1: return 'warning';  // 登出
    case 2: return 'success';  // 新增
    case 3: return 'danger';   // 删除
    case 4: return 'primary';  // 修改
    case 5: return '';         // 查询
    case 6: return 'info';     // 其他
    default: return '';
  }
};

// 页面加载时获取日志列表
onMounted(() => {
  getLogList();
});
</script>

<style scoped>
.log-container {
  padding: 20px;
}
.search-bar {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
.params-container, .exception-container {
  max-height: 150px;
  overflow-y: auto;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}
.exception-container {
  color: #f56c6c;
}
</style> 