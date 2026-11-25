<template>
  <div class="login-page">
  <div class="login-container">
      <div class="login-card">
        <div class="card-left">
          <div class="brand">
            <div class="logo-icon">☁️</div>
            <div class="logo-text">
              <h1>CLOUDMART</h1>
              <div class="slogan">让库存，比云更轻</div>
      </div>
    </div>
    
          <div class="hero-image">
            <img src="@/assets/images/login-illustration.svg" alt="Login" onerror="this.src='https://cdn.jsdelivr.net/gh/slidevjs/slidev/assets/logo-title.png';this.onerror=null;" />
          </div>
          
          <div class="feature-badges">
            <div class="badge">智能预测</div>
            <div class="badge">云端同步</div>
            <div class="badge">移动管理</div>
          </div>
        </div>
        
        <div class="card-right">
          <h2>欢迎回来</h2>
          <p class="subtitle">登录您的云超管家账号</p>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <a href="#" class="forgot-link">忘记密码?</a>
            </div>
          
          <el-form-item>
            <el-button
              :loading="loading"
              type="primary"
              class="login-button"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p>还没有账号？<router-link to="/register" class="link">立即注册</router-link></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const loginFormRef = ref(null)
const rememberMe = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ]
}

// 登录处理函数
const handleLogin = async () => {
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      loading.value = true
      
      // 清除旧的登录数据
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('roleId')
      localStorage.removeItem('menus')
      
      // 调用登录接口
      const res = await userStore.login(loginForm)
      
      // 登录成功提示
      ElMessage.success('登录成功')
      
      // 根据角色重定向到不同页面
      if (userStore.isAdmin || userStore.isUser) {
        // 如果有重定向参数，则重定向到相应页面
        const redirectPath = route.query.redirect || '/back/dashboard'
        router.push(redirectPath)
      } else {
        // 其他角色重定向到首页
        router.push('/')
      }
    } catch (error) {
      ElMessage.error(error.message || '登录失败，请检查您的用户名和密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9fafb;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 900px;
}

.login-card {
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
  flex: 1;
  padding: 60px 40px;
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
  margin-bottom: 40px;
}

.login-form {
  margin-bottom: 20px;
}

.login-form .el-form-item {
  margin-bottom: 24px;
}

.login-form .el-input {
  height: 50px;
}

.login-form .el-input__wrapper {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 0 15px;
  transition: all 0.3s;
}

.login-form .el-input__wrapper:hover,
.login-form .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
}

.login-form .el-input__inner {
  font-size: 15px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  color: #3b82f6;
  font-size: 14px;
  text-decoration: none;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(90deg, #1e40af, #3b82f6);
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.form-footer {
  text-align: center;
  margin-top: 30px;
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

/* 响应式调整 */
@media (max-width: 768px) {
  .login-card {
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
}
</style> 