<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="auth-header">
        <div class="logo">🎬</div>
        <h1 class="title">找回密码</h1>
        <div class="subtitle">PASSWORD RECOVERY</div>
      </div>
      <el-form :model="resetForm" :rules="rules" ref="resetFormRef" class="auth-form">
        <el-form-item prop="phone">
          <el-input 
            v-model="resetForm.phone"
            :prefix-icon="Phone"
            placeholder="请输入手机号">
            <template #append>
              <el-button 
                :disabled="!resetForm.phone || cooldown > 0" 
                @click="sendVerificationCode"
                class="send-code-btn">
                {{ cooldown > 0 ? `${cooldown}s` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="verificationCode">
          <el-input 
            v-model="resetForm.verificationCode"
            :prefix-icon="Key"
            placeholder="请输入验证码">
          </el-input>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input 
            v-model="resetForm.newPassword"
            :prefix-icon="Lock"
            type="password"
            placeholder="请输入新密码">
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="resetForm.confirmPassword"
            :prefix-icon="Lock"
            type="password"
            placeholder="请确认新密码">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleReset" class="auth-button">
            重置密码
          </el-button>
        </el-form-item>
        <div class="auth-link">
          想起密码了？<router-link to="/login">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Lock, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const resetFormRef = ref(null)
const loading = ref(false)
const cooldown = ref(0)

const resetForm = reactive({
  phone: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度必须为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const sendVerificationCode = async () => {
  if (!resetForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  try {
    await request.get(`/sms/code/${resetForm.phone}`)
    ElMessage.success('验证码已发送')
    cooldown.value = 60
    const timer = setInterval(() => {
      cooldown.value--
      if (cooldown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

const handleReset = () => {
  resetFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        await request.post('/user/forget', {
          phone: resetForm.phone,
          newPassword: resetForm.newPassword,
          verificationCode: resetForm.verificationCode
        },{
         successMessage: '密码重置成功',
        })
        router.push('/login')
      } catch (error) {
        console.error('密码重置失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/auth.scss';

.send-code-btn {
  width: 100px;
  padding: 0;
}

.el-input-group__append {
  padding: 0;
}
</style> 