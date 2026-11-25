<template>
  <div class="role-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="searchForm.description" placeholder="请输入描述" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchRoles">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">添加角色</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="roleList"
      border
      style="width: 100%"
      :default-sort="{prop: 'roleId', order: 'ascending'}"
    >
      <el-table-column prop="roleId" label="ID" width="80" sortable />
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="success" size="small" @click="handlePermission(scope.row)">权限设置</el-button>
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

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '添加角色'"
      width="500px"
      @closed="resetForm"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 角色权限设置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="权限设置"
      width="600px"
      @closed="resetPermissionForm"
    >
      <div v-if="currentRole">
        <div class="role-info">
          <h4>角色: {{ currentRole.roleName }}</h4>
          <p>描述: {{ currentRole.description }}</p>
        </div>
        
        <div class="menu-tree">
          <h4>菜单权限</h4>
          <el-tree
            ref="menuTree"
            :data="menuList"
            :props="{
              label: 'name',
              children: 'children'
            }"
            node-key="id"
            show-checkbox
            default-expand-all
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermission">保存</el-button>
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
const roleList = ref([])
const menuList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEdit = ref(false)
const roleFormRef = ref(null)
const menuTree = ref(null)
const currentRole = ref(null)

// 搜索表单
const searchForm = reactive({
  roleName: '',
  description: ''
})

// 角色表单
const roleForm = reactive({
  roleId: null,
  roleName: '',
  description: ''
})

// 表单校验规则
const roleRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 初始化
onMounted(() => {
  fetchRoles()
  fetchMenus()
})

// 获取角色列表
const fetchRoles = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      currentPage: currentPage.value,
      pageSize: pageSize.value
    }
    
    const res = await request.get('/role/page', params,{
      showDefaultMsg: false,
      onSuccess: (data) => {
        if (data) {
          roleList.value = data.records || []
          total.value = data.total || 0
        }
      }
    })
  } catch (error) {
    console.error('获取角色列表失败', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 获取菜单列表
const fetchMenus = async () => {
  try {
    const res = await request.get('/menu')
    menuList.value = res
  } catch (error) {
    console.error('获取菜单列表失败', error)
    ElMessage.error('获取菜单列表失败')
  }
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString()
}

// 搜索角色
const searchRoles = () => {
  currentPage.value = 1
  fetchRoles()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchRoles()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchRoles()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchRoles()
}

// 处理添加角色
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 处理编辑角色
const handleEdit = (row) => {
  isEdit.value = true
  
  // 复制角色数据到表单
  Object.keys(roleForm).forEach(key => {
    if (key in row) {
      roleForm[key] = row[key]
    }
  })
  
  dialogVisible.value = true
}

// 处理删除角色
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该角色吗？删除后相关用户的角色将失效', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/role/${row.roleId}`)
      ElMessage.success('删除成功')
      fetchRoles()
    } catch (error) {
      console.error('删除角色失败', error)
      ElMessage.error('删除角色失败')
    }
  }).catch(() => {})
}

// 处理权限设置
const handlePermission = async (row) => {
  currentRole.value = row
  
  // 加载已分配的菜单ID
  try {
    const menuIds = await request.get(`/role/${row.roleId}/menuIds`)
    
    // 打开对话框后设置选中状态
    permissionDialogVisible.value = true
    
    // 等待DOM更新
    setTimeout(() => {
      menuTree.value.setCheckedKeys(menuIds)
    }, 100)
  } catch (error) {
    console.error('获取角色菜单失败', error)
    ElMessage.error('获取角色菜单失败')
  }
}

// 保存权限设置
const savePermission = async () => {
  try {
    if (!currentRole.value) return
    
    const roleId = currentRole.value.roleId
    const menuIds = menuTree.value.getCheckedKeys()
    
    await request.post(`/role/${roleId}/menus`, menuIds)
    ElMessage.success('权限设置成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('保存权限设置失败', error)
    ElMessage.error('保存权限设置失败')
  }
}

// 提交表单
const submitForm = async () => {
  roleFormRef.value.validate(async valid => {
    if (!valid) return
    
    try {
      if (isEdit.value) {
        // 编辑角色
        await request.put(`/role/${roleForm.roleId}`, roleForm)
        ElMessage.success('编辑成功')
      } else {
        // 添加角色
        await request.post('/role', roleForm)
        ElMessage.success('添加成功')
      }
      
      dialogVisible.value = false
      fetchRoles()
    } catch (error) {
      console.error('操作失败', error)
      ElMessage.error('操作失败')
    }
  })
}

// 重置表单
const resetForm = () => {
  if (roleFormRef.value) {
    roleFormRef.value.resetFields()
  }
  
  Object.keys(roleForm).forEach(key => {
    roleForm[key] = key === 'roleId' ? null : ''
  })
}

// 重置权限表单
const resetPermissionForm = () => {
  currentRole.value = null
}
</script>

<style scoped>
.role-container {
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

.role-info {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.menu-tree {
  margin-top: 20px;
}
</style> 