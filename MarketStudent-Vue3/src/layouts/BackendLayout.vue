<template>
  <div class="backend-layout">
    <!-- 侧边栏 -->
    <Sidebar :is-collapsed="isCollapsed" />

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <Navbar :is-collapsed="isCollapsed" @toggle-sidebar="toggleSidebar" />

      <!-- 页面内容 -->
      <main class="content">
        <el-card class="page-container">
          <router-view />
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import Sidebar from '@/components/backend/Sidebar.vue'
import Navbar from '@/components/backend/Navbar.vue'

const userStore = useUserStore()
const router = useRouter()
const isCollapsed = ref(false)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const navigateToProfile = () => {
  router.push('/back/profile')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.backend-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: #f0f2f5;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.page-container {
  min-height: calc(100% - 40px);
}
</style>