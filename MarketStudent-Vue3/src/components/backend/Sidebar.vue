<template>
  <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
    <div class="logo">
      <span class="logo-icon">☁️</span>
      <span class="logo-text" v-show="!isCollapsed">CLOUDMART</span>
    </div>
    <div class="menu-wrapper">
      <el-menu 
        :default-active="activeMenu" 
        :collapse="isCollapsed" 
        :collapse-transition="false" 
        :router="true"
        mode="vertical" 
        class="sidebar-menu"
        text-color="#94a3b8" 
        active-text-color="#38bdf8"
      >
        <template v-for="menu in menus">
          <!-- 没有子菜单 -->
          <el-menu-item 
            v-if="!menu.children?.length" 
            :index="menu.path"
            :key="menu.path"
          >
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <template #title>{{ menu.name }}</template>
          </el-menu-item>
          
          <!-- 有子菜单 -->
          <el-sub-menu 
            v-else 
            :index="menu.path"
            :key="menu.path"
          >
            <template #title>
              <el-icon v-if="menu.icon">
                <component :is="menu.icon" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </template>
            
            <!-- 递归渲染子菜单 -->
            <template v-for="child in menu.children">
              <el-menu-item 
                v-if="!child.children?.length" 
                :index="child.path"
                :key="child.path"
              >
                <el-icon v-if="child.icon">
                  <component :is="child.icon" />
                </el-icon>
                <template #title>{{ child.name }}</template>
              </el-menu-item>
              
              <el-sub-menu 
                v-else 
                :index="child.path"
                :key="child.path"
              >
                <template #title>
                  <el-icon v-if="child.icon">
                    <component :is="child.icon" />
                  </el-icon>
                  <span>{{ child.name }}</span>
                </template>
                
                <el-menu-item 
                  v-for="grandChild in child.children" 
                  :key="grandChild.path" 
                  :index="grandChild.path"
                >
                  <el-icon v-if="grandChild.icon">
                    <component :is="grandChild.icon" />
                  </el-icon>
                  <template #title>{{ grandChild.name }}</template>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>
    <div class="sidebar-footer" v-if="!isCollapsed">
      <div class="slogan">让库存，比云更轻</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const props = defineProps({
  isCollapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 获取菜单数据
const menus = computed(() => {
  const storedMenus = localStorage.getItem('menus')
  try {
    const parsedMenus = storedMenus ? JSON.parse(storedMenus) : []
    console.log('Parsed menus in sidebar:', parsedMenus)
    // 确保所有路径都以 /back 开头
    return parsedMenus.map(menu => ({
      ...menu,
      path: ensurePath(menu.path),
      children: menu.children?.map(child => ({
        ...child,
        path: ensurePath(child.path),
        children: child.children?.map(grandChild => ({
          ...grandChild,
          path: ensurePath(grandChild.path)
        }))
      }))
    }))
  } catch (error) {
    console.error('解析菜单数据失败:', error)
    return []
  }
})

// 确保路径以 /back 开头
const ensurePath = (path) => {
  if (!path) return ''
  
  // 移除开头的斜杠
  let cleanPath = path.startsWith('/') ? path.substring(1) : path
  
  // 确保路径以 back 开头
  return cleanPath.startsWith('back/') ? `/${cleanPath}` : `/back/${cleanPath}`
}

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return ensurePath(meta.activeMenu)
  }
  return path
})

// 组件挂载时检查路由
onMounted(() => {
  // 如果当前路径不在菜单中，且不是特殊路径（如登录页），则跳转到首页或默认页面
  const currentPath = route.path
  if (!isValidPath(currentPath) && !isSpecialPath(currentPath)) {
    router.push('/back/dashboard')
  }
})

// 检查路径是否在菜单中
const isValidPath = (path) => {
  const flatMenus = flattenMenus(menus.value)
  return flatMenus.some(item => item.path === path)
}

// 检查是否是特殊路径（如登录页）
const isSpecialPath = (path) => {
  const specialPaths = ['/login', '/register', '/404']
  return specialPaths.includes(path)
}

// 扁平化菜单数组
const flattenMenus = (menuArray) => {
  const result = []
  menuArray.forEach(menu => {
    result.push(menu)
    if (menu.children?.length) {
      menu.children.forEach(child => {
        result.push(child)
        if (child.children?.length) {
          result.push(...child.children)
        }
      })
    }
  })
  return result
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100vh;
  background: linear-gradient(180deg, #1e40af 0%, #3b82f6 100%);
  display: flex;
  flex-direction: column;
  width: 240px;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1001;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  
  &.is-collapsed {
    width: 64px;
    
    .logo {
      padding: 0;
      justify-content: center;
      
      .logo-icon {
        margin: 0;
      }
    }

    :deep(.el-menu) {
      .el-sub-menu__title span,
      .el-menu-item span {
        display: none;
      }

      .el-menu-item .el-icon,
      .el-sub-menu__title .el-icon {
        margin: 0;
      }
    }
  }
  
  .logo {
    height: 70px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    padding: 0 20px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background: rgba(255, 255, 255, 0.05);
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    
    .logo-icon {
      font-size: 26px;
      margin-right: 12px;
      transition: margin 0.3s;
      color: #fff;
    }
    
    .logo-text {
      color: #ffffff;
      font-size: 20px;
      font-weight: 600;
      letter-spacing: 1px;
      white-space: nowrap;
      transition: opacity 0.3s;
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 12px 0;

    &::-webkit-scrollbar {
      width: 4px;
      height: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 2px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  .sidebar-footer {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 20px;
    color: rgba(255, 255, 255, 0.6);
    font-size: 12px;
    letter-spacing: 0.5px;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
    background: rgba(0, 0, 0, 0.05);
  }

  :deep(.sidebar-menu) {
    border: none;
    background: transparent;

    .el-menu-item,
    .el-sub-menu__title {
      height: 48px;
      line-height: 48px;
      color: rgba(255, 255, 255, 0.8);
      background: transparent;
      margin: 4px 8px;
      border-radius: 8px;
      
      &:hover {
        color: #fff;
        background: rgba(255, 255, 255, 0.12) !important;
      }
    }

    .el-menu-item.is-active {
      color: #ffffff !important;
      background: rgba(255, 255, 255, 0.18) !important;
      font-weight: 600;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        width: 4px;
        height: 100%;
        background: #ffffff;
        border-radius: 0 4px 4px 0;
      }
    }

    .el-sub-menu {
      &.is-opened {
        > .el-sub-menu__title {
          color: #fff;
        }
      }

      .el-menu {
        background: rgba(0, 0, 0, 0.1);
        padding: 4px 0;
        border-radius: 6px;
        margin: 0 8px;
      }
    }

    .el-icon {
      vertical-align: middle;
      width: 24px;
      text-align: center;
      margin-right: 12px;
      color: inherit;
    }

    span {
      vertical-align: middle;
      font-size: 14px;
    }
  }
}
</style> 