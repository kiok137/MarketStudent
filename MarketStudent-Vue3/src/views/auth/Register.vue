<template>
  <div class="register-page">
  <div class="register-container">
      <div class="register-card">
        <div class="card-left">
          <div class="brand">
            <div class="logo-icon">☁️</div>
            <div class="logo-text">
              <h1>CLOUDMART</h1>
              <div class="slogan">让库存，比云更轻</div>
        </div>
          </div>
          
          <div class="hero-image">
            <img src="@/assets/images/register-illustration.svg" alt="Register" onerror="this.src='https://cdn.jsdelivr.net/gh/slidevjs/slidev/assets/logo-dark.png';this.onerror=null;" />
          </div>
          
          <div class="feature-badges">
            <div class="badge">智能预测</div>
            <div class="badge">云端同步</div>
            <div class="badge">移动管理</div>
          </div>
        </div>
        
        <div class="card-right">
          <h2>创建账号</h2>
          <p class="subtitle">加入云超管家，开始智能库存管理</p>
        
        <el-form 
          :model="registerForm" 
          :rules="rules" 
          ref="registerFormRef"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="registerForm.username"
              :prefix-icon="User"
              placeholder="用户名"
            />
          </el-form-item>
          
            <div class="form-row">
              <el-form-item prop="password" class="half-width">
            <el-input 
              v-model="registerForm.password"
              :prefix-icon="Lock"
              type="password"
              placeholder="密码"
                  show-password
            />
          </el-form-item>
          
              <el-form-item prop="confirmPassword" class="half-width">
            <el-input 
              v-model="registerForm.confirmPassword"
              :prefix-icon="Lock"
              type="password"
              placeholder="确认密码"
                  show-password
            />
          </el-form-item>
            </div>
          
          <div class="form-row">
            <el-form-item prop="email" class="half-width">
              <el-input 
                v-model="registerForm.email"
                :prefix-icon="Message"
                  placeholder="电子邮箱"
              />
            </el-form-item>
            
            <el-form-item prop="phone" class="half-width">
              <el-input 
                v-model="registerForm.phone"
                :prefix-icon="Phone"
                  placeholder="手机号码"
              />
            </el-form-item>
          </div>
          
          <el-form-item prop="name">
            <el-input 
              v-model="registerForm.name"
              :prefix-icon="UserFilled"
                placeholder="姓名（选填）"
            />
          </el-form-item>
            
            <div class="terms-agreement">
              <el-checkbox v-model="agreeTerms">我已阅读并同意<a href="#" class="link">服务条款</a>和<a href="#" class="link">隐私政策</a></el-checkbox>
            </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              @click="handleRegister" 
              class="register-button"
                :disabled="!agreeTerms"
            >
                创建账号
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <p>已有账号？<router-link to="/login" class="link">立即登录</router-link></p>
          </div>
        </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message, Phone, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const agreeTerms = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  name: '',
  roleCode: 'USER', // 默认注册为普通用户
})

const validatePass2 = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
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
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须在3到20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6到20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  name: [
    { required: false }
  ]
}

const handleRegister = () => {
  if (!agreeTerms.value) {
    ElMessage.warning('请先同意服务条款和隐私政策')
    return
  }
  
  registerFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...registerData } = registerForm
        await request.post("/user/add", registerData, {
          successMsg: "注册成功",
          showDefaultMsg: true,
          onSuccess: () => {
            router.push('/login')
          }
        })
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9fafb;
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 1000px;
}

.register-card {
  display: flex;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
}

.card-left {
  flex: 1;
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  color: white;
  padding: 40px;
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 40px;
  margin-right: 16px;
}

.logo-text h1 {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
  margin: 0;
}

.slogan {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 4px;
}

.hero-image {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20px 0;
}

.hero-image img {
  max-width: 100%;
  max-height: 240px;
  object-fit: contain;
}

.feature-badges {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.badge {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(5px);
}

.card-right {
  flex: 1.2;
  padding: 50px 40px;
  overflow-y: auto;
}

.card-right h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 8px;
}

.subtitle {
  color: #718096;
  font-size: 16px;
  margin-bottom: 30px;
}

.register-form .el-form-item {
  margin-bottom: 18px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.half-width {
  flex: 1;
}

.register-form .el-input {
  height: 50px;
}

.register-form .el-input__wrapper {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 0 15px;
  transition: all 0.3s;
}

.register-form .el-input__wrapper:hover,
.register-form .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
}

.register-form .el-input__inner {
  font-size: 15px;
}

.terms-agreement {
  margin: 10px 0 24px;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(90deg, #1e40af, #3b82f6);
  border: none;
  transition: all 0.3s;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.register-button:disabled {
  opacity: 0.7;
  background: linear-gradient(90deg, #1e40af, #3b82f6);
  border: none;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
}

.form-footer p {
  font-size: 14px;
  color: #718096;
}

.link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

/* 让element-plus图标垂直居中 */
.el-input__prefix {
  display: flex;
  align-items: center;
}

/* 响应式调整 */
@media (max-width: 900px) {
  .register-card {
    flex-direction: column;
  }
  
  .card-left {
    padding: 30px;
  }
  
  .hero-image {
    margin: 10px 0;
  }
  
  .hero-image img {
    max-height: 160px;
  }
  
  .card-right {
    padding: 40px 30px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style> 