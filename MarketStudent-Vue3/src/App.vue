<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/store/user'
import { initDynamicRoutes } from '@/router'
import { onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// 当用户登录状态变化时，初始化动态路由
watch(() => userStore.isLoggedIn, async (isLoggedIn) => {
  if (isLoggedIn && !userStore.isUser) {
    console.log('User logged in, initializing dynamic routes')
    try {
      await nextTick()
      initDynamicRoutes()
      // 触发一次路由刷新
      const currentRoute = router.currentRoute.value
      if (currentRoute.path.startsWith('/back')) {
        await router.replace(currentRoute.fullPath)
      }
    } catch (error) {
      console.error('Error initializing routes on login:', error)
    }
  }
})

// 监听localStorage变化，处理跨标签页登录状态同步
const handleStorageChange = (event) => {
  if (event.key === 'token') {
    // token发生变化
    if (!event.newValue && userStore.token) {
      // token被清空，且当前有token（在其他标签页登出）
      console.log('Token was cleared in another tab')
      userStore.clearUserInfo()
      if (router.currentRoute.value.path.startsWith('/back')) {
        router.push('/login')
      }
    } else if (event.newValue && !userStore.token) {
      // token被设置，且当前无token（在其他标签页登录）
      console.log('Token was set in another tab')
      // 刷新页面以获取最新的用户状态
      window.location.reload()
    }
  }
}

// 应用初始化时，如果用户已登录，则加载动态路由
onMounted(async () => {
  if (userStore.isLoggedIn && !userStore.isUser) {
    console.log('App mounted, user already logged in, initializing dynamic routes')
    try {
      await nextTick()
      initDynamicRoutes()
    } catch (error) {
      console.error('Error initializing routes on mount:', error)
    }
  }
  
  // 添加localStorage事件监听
  window.addEventListener('storage', handleStorageChange)
})

// 组件卸载前移除事件监听
onBeforeUnmount(() => {
  window.removeEventListener('storage', handleStorageChange)
})
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f7fa;
}

#app {
  height: 100vh;
}
</style>
