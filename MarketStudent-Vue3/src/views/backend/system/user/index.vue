<template>
  <div class="user-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleId" placeholder="请选择角色" clearable>
            <el-option v-for="role in roles" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option :value="0" label="禁用" />
            <el-option :value="1" label="启用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUsers">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">添加用户</el-button>
      <el-button type="danger" :disabled="selectedUsers.length === 0" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="userList"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
      :default-sort="{prop: 'userId', order: 'ascending'}"
    >
      <el-table-column type="selection" width="55"  />
      <el-table-column prop="userId" label="ID" width="80" sortable />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="电话" width="150" />
      <el-table-column prop="role.roleName" label="角色" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" style="width: 200px;">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button 
            type="success" 
            size="small" 
            v-if="scope.row.status === 0"
            @click="handleStatusChange(scope.row, 1)"
          >
            启用
          </el-button>
          <el-button 
            type="warning" 
            size="small" 
            v-else
            @click="handleStatusChange(scope.row, 0)"
          >
            禁用
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="500px"
      @closed="resetForm"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" placeholder="请选择状态">
            <el-option :value="0" label="禁用" />
            <el-option :value="1" label="启用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 数据列表
const userList = ref([])
const roles = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedUsers = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  roleId: '',
  status: ''
})

// 用户表单
const userForm = reactive({
  userId: null,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  roleId: '',
  status: 1
})

// 表单校验规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 初始化
onMounted(() => {
  fetchUsers()
  fetchRoles()
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    const res = await request.get('/user/page', params,{
      onSuccess: (res) => {
        userList.value = res.records||[]
        total.value = res.total||0
      },
      onError: (error) => {
        console.error('获取用户列表失败', error)
        ElMessage.error('获取用户列表失败')
      }
    })
    
  } catch (error) {
    console.error('获取用户列表失败', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const fetchRoles = async () => {
  try {
    await request.get('/role/all',null,{

      onSuccess: (res ) => {
        roles.value = res||[]
      },
      onError: (error) => {
        console.error('获取角色列表失败', error)
        ElMessage.error('获取角色列表失败')
      }
    })
  } catch (error) {
    console.error('获取角色列表失败', error)
    ElMessage.error('获取角色列表失败')
  }
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString()
}

// 搜索用户
const searchUsers = () => {
  currentPage.value = 1
  fetchUsers()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchUsers()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchUsers()
}

// 处理多选变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 处理添加用户
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 处理编辑用户
const handleEdit = (row) => {
  isEdit.value = true
  
  // 复制用户数据到表单
  Object.keys(userForm).forEach(key => {
    if (key in row) {
      userForm[key] = row[key]
    }
  })
  
  dialogVisible.value = true
}

// 处理删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/user/delete/${row.userId}`,{
        successMsg:'删除成功',
        onSuccess: () => {
          fetchUsers()
        },
        onError: (error) => {
          console.error('删除用户失败', error)
          ElMessage.error('删除用户失败')
        }
      })
    } catch (error) {
      console.error('删除用户失败', error)
      ElMessage.error('删除用户失败')
    }
  }).catch(() => {})
}

// 处理批量删除
const handleBatchDelete = () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedUsers.value.length} 个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedUsers.value.map(user => user.userId)
      await request.delete('/user/deleteBatch?ids='+ids,{
        successMsg:'批量删除成功',
        onSuccess: () => {
          fetchUsers()
        },
        onError: (error) => {
          console.error('批量删除用户失败', error)
          ElMessage.error('批量删除用户失败')
        }
      })
    } catch (error) {
      console.error('批量删除用户失败', error)
      ElMessage.error('批量删除用户失败')
    }
  }).catch(() => {})
}

// 处理用户状态变更
const handleStatusChange = async (row, status) => {
  try {
      await request.put(`/user/${row.userId}`, { status },{
        successMsg:`用户${status === 1 ? '启用' : '禁用'}成功`,
      onSuccess: () => {
       
        fetchUsers()
      },
      onError: (error) => {
        console.error('修改用户状 态失败', error)
        ElMessage.error('修改用户状态失败')
      }
    })
  } catch (error) {
    console.error('修改用户状态失败', error)
    ElMessage.error('修改用户状态失败')
  }
}

// 提交表单
const submitForm = async () => {
  userFormRef.value.validate(async valid => {
    if (!valid) return
    
    try {
      if (isEdit.value) {
        // 编辑用户
        await request.put(`/user/${userForm.userId}`, userForm,{
          successMsg:'编辑成功',
          onSuccess: () => {
           
            dialogVisible.value = false
            fetchUsers()
          },
          
        })
      } else {
        // 添加用户
        await request.post('/user/add', userForm,{
          successMsg:'添加成功',
          onSuccess: () => {
           
            dialogVisible.value = false
            fetchUsers()
          },
          onError: (error) => {
            console.error('添加用户失败', error)
            ElMessage.error('添加用户失败')
          }
        })
      }
      
      dialogVisible.value = false
      fetchUsers()
    } catch (error) {
      console.error('操作失败', error)
      ElMessage.error('操作失败')
    }
  })
}

// 重置表单
const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  
  Object.keys(userForm).forEach(key => {
    if (key === 'status') {
      userForm[key] = 1
    } else {
      userForm[key] = key === 'userId' ? null : ''
    }
  })
}
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 