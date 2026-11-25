<template>
    <div class="frontend-layout">
      <!-- 顶部导航栏 -->
      <header class="header">
        <nav>
          <router-link to="/">首页</router-link>
          <router-link to="/cart">购物车</router-link>
          <router-link to="/profile" v-if="isLoggedIn">个人中心</router-link>
          <button v-if="isLoggedIn" @click="handleLogout">退出登录</button>
          <router-link v-else to="/login">登录</router-link>
        </nav>
      </header>
  
      <!-- 主要内容区域 -->
      <main class="main-content">
        <router-view />
      </main>
  
      <!-- 页脚 -->
      <footer class="footer">
        <p>&copy; 2023 我的商城</p>
      </footer>
    </div>
  </template>
  
  <script setup>
  import { computed } from 'vue'
  import { useUserStore } from '@/store/user'
  import { useRouter } from 'vue-router'
  
  const userStore = useUserStore()
  const router = useRouter()
  
  const isLoggedIn = computed(() => !!userStore.token)
  
  const handleLogout = () => {
    userStore.clearUserInfo()
    router.push('/login')
  }
  </script>
  
  <style scoped>
  .frontend-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
  
  .header {
    background-color: #333;
    color: white;
    padding: 1rem;
  }
  
  .header nav {
    display: flex;
    gap: 1rem;
  }
  
  .header a {
    color: white;
    text-decoration: none;
  }
  
  .main-content {
    flex: 1;
    padding: 1rem;
  }
  
  .footer {
    background-color: #333;
    color: white;
    text-align: center;
    padding: 1rem;
  }
  </style>