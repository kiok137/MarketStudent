<template>
  <div class="error-container">
    <div class="error-content">
      <h1>403</h1>
      <h2>权限不足</h2>
      <p>抱歉，您没有权限访问此页面。</p>
      <div class="actions">
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 返回首页 - 根据用户类型跳转不同首页
const goHome = () => {
  if (userStore.isAdmin) {
    router.push('/back/dashboard')
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.error-content {
  text-align: center;
  padding: 2rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-width: 500px;
}

h1 {
  font-size: 6rem;
  margin: 0;
  color: #f56c6c;
}

h2 {
  font-size: 2rem;
  margin: 0.5rem 0;
  color: #303133;
}

p {
  color: #606266;
  margin-bottom: 2rem;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}
</style> 