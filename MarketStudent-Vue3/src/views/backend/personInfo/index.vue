<template>
  <div class="personal-info-container">
    <el-card class="personal-info-card">
      <template #header>
        <div class="card-header">
          <span>个人信息管理</span>
          <el-button type="primary" @click="handleEdit" v-if="!isEditing">编辑</el-button>
          <div v-else>
            <el-button type="success" @click="handleSave">保存</el-button>
            <el-button @click="handleCancel">取消</el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="userForm" :disabled="!isEditing" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName"></el-input>
        </el-form-item>
        <el-form-item label="电话号码">
          <el-input v-model="userForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="userForm.email"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="roleName" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-tag :type="userForm.status === 1 ? 'success' : 'danger'">
            {{ userForm.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-form-item>
      </el-form>

      <el-divider content-position="center">修改密码</el-divider>
      
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const userStore = useUserStore()
const isEditing = ref(false)
const userForm = ref({})
const originalUserForm = ref({})
const passwordFormRef = ref(null)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const roleName = computed(() => {
  return userForm.value.role ? userForm.value.role.roleName : ''
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    // 先尝试使用现有的用户信息
    if (userStore.userInfo) {
      userForm.value = userStore.userInfo
      originalUserForm.value = JSON.parse(JSON.stringify(userStore.userInfo))
      return
    }
    
    // 如果没有用户信息，再尝试获取，但不强制刷新避免清空token
    await userStore.getUserInfo(false)
    userForm.value = userStore.userInfo
    originalUserForm.value = JSON.parse(JSON.stringify(userStore.userInfo))
  } catch (error) {
    ElMessage.error('获取个人信息失败')
    // 失败时不清空token，尝试使用现有信息
    if (userStore.userInfo) {
      userForm.value = userStore.userInfo
      originalUserForm.value = JSON.parse(JSON.stringify(userStore.userInfo))
    }
  }
}

// 编辑模式
const handleEdit = () => {
  isEditing.value = true
}

// 保存个人信息
const handleSave = async () => {
  try {
    // 直接调用API更新用户信息
    await request.put(`/user/${userForm.value.userId}`, userForm.value, {
      successMsg: '个人信息更新成功',
      onSuccess: () => {
        // 更新成功后直接更新本地用户信息，不重新请求
        userStore.updateUserInfo(userForm.value)
        isEditing.value = false
      }
    })
  } catch (error) {
    ElMessage.error('更新个人信息失败')
  }
}

// 取消编辑
const handleCancel = () => {
  userForm.value = JSON.parse(JSON.stringify(originalUserForm.value))
  isEditing.value = false
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    
    ElMessageBox.confirm('确定要修改密码吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        const passwordData = {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        }
        
        // 调用API修改密码
        await request.put(`/user/password/${userForm.value.userId}`, passwordData,{
            successMsg:'密码修改成功',
            onSuccess:()=>{
                // 清空密码表单
                passwordForm.oldPassword = ''
                passwordForm.newPassword = ''
                passwordForm.confirmPassword = ''
                passwordFormRef.value.resetFields()
            }
        })
        
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败')
      }
    }).catch(() => {
      // 用户取消操作
    })
  } catch (error) {
    // 表单验证失败
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.personal-info-container {
  padding: 20px;
}

.personal-info-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>