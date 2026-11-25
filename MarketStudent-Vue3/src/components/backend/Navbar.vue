<template>
  <div class="navbar">
    <div class="left-menu">
      <el-icon class="hamburger" :size="20" @click="$emit('toggle-sidebar')">
        <component :is="isCollapsed ? Expand : Fold" />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/back/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <div class="right-menu">
      <div class="right-menu-item" @click="toggleFullScreen">
        <el-icon :size="18">
          <component :is="isFullscreen ? Aim : FullScreen" />
        </el-icon>
      </div>
      
      <el-dropdown trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="32" :src="userInfo?.avatar" class="user-avatar">
            {{ userInfo?.realName?.charAt(0)?.toUpperCase() || userInfo?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="user-info">
          <span class="user-name">{{ userInfo?.realName || userInfo?.username }}</span>
            <span class="user-role">{{ userInfo?.roleName || '用户' }}</span>
          </div>
          <el-icon class="dropdown-icon">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleProfile">
              <el-icon><User /></el-icon>
              个人信息
            </el-dropdown-item>
        
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Expand, Fold, ArrowDown, User, SwitchButton, FullScreen, Aim, Setting } from '@element-plus/icons-vue'

const props = defineProps({
  isCollapsed: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle-sidebar'])

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isFullscreen = ref(false)

const userInfo = computed(() => userStore.userInfo)

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}

document.addEventListener('fullscreenchange', () => {
  isFullscreen.value = !!document.fullscreenElement
})

const handleProfile = () => {
  router.push('/back/personal-info')
}

const goToSettings = () => {
  router.push('/back/settings')
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  box-shadow: 0 1px 20px rgba(0, 0, 0, 0.05);
  z-index: 1000;

  .left-menu {
    display: flex;
    align-items: center;
    gap: 20px;

    .hamburger {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 8px;
      color: #6b7280;
      height: 36px;
      width: 36px;
      transition: all 0.2s;
      
      &:hover {
        background: #f3f4f6;
        color: #3b82f6;
      }
    }

    :deep(.el-breadcrumb__inner) {
      color: #6b7280;
      font-size: 14px;
      line-height: 32px;
      
      &.is-link {
        color: #9ca3af;
        font-weight: 500;
        
        &:hover {
          color: #3b82f6;
        }
      }
    }
  }

  .right-menu {
    display: flex;
    align-items: center;
    gap: 10px;

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #6b7280;
      border-radius: 8px;
      transition: all 0.2s;
      height: 36px;
      width: 36px;
      
      &:hover {
        background: #f3f4f6;
        color: #3b82f6;
      }
    }
    
    .avatar-wrapper {
      display: flex;
      align-items: center;
      padding: 4px 8px 4px 4px;
      margin-left: 8px;
      cursor: pointer;
      border-radius: 24px;
      transition: all 0.2s;
      border: 1px solid transparent;
      
      &:hover {
        background: #f3f4f6;
        border-color: #e5e7eb;
      }
      
      .user-avatar {
        border: 2px solid #f1f5f9;
        background: linear-gradient(135deg, #1e40af, #3b82f6);
        color: white;
        font-weight: 600;
      }
      
      .user-info {
        display: flex;
        flex-direction: column;
        margin: 0 8px;
        line-height: 1.2;
        
        .user-name {
        font-size: 14px;
          font-weight: 500;
          color: #374151;
      }

        .user-role {
          font-size: 12px;
          color: #9ca3af;
        }
      }

      .dropdown-icon {
        color: #9ca3af;
        display: flex;
        align-items: center;
        font-size: 12px;
      }
    }
  }

  :deep(.el-dropdown-menu) {
    padding: 6px;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: none;
  }

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    height: 40px;
    border-radius: 8px;
    margin: 2px 0;
    
    &:hover {
      background: #f3f4f6;
      color: #3b82f6;
    }
    
    &.is-disabled {
      opacity: 0.5;
    }
    
    .el-icon {
      margin-right: 4px;
      display: flex;
      align-items: center;
    }
  }
}
</style> 