<template>
  <div class="menu-container">
    <el-menu
      :default-active="activeMenu"
      class="menu"
      :collapse="isCollapse"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <template v-for="menu in menuItems">
        <!-- 有子菜单的情况 -->
        <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="formatPath(menu.path)" :key="menu.id">
          <template #title>
            <el-icon v-if="menu.icon"><component :is="iconMap[menu.icon] || menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </template>
          
          <!-- 递归渲染子菜单 -->
          <template v-for="child in menu.children">
            <el-menu-item 
              v-if="!child.children || child.children.length === 0" 
              :index="formatPath(child.path)"
              :key="child.id"
            >
              <el-icon v-if="child.icon"><component :is="iconMap[child.icon] || child.icon" /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
            
            <el-sub-menu v-else :index="formatPath(child.path)" :key="child.id">
              <template #title>
                <el-icon v-if="child.icon"><component :is="iconMap[child.icon] || child.icon" /></el-icon>
                <span>{{ child.name }}</span>
              </template>
              
              <el-menu-item 
                v-for="grandChild in child.children" 
                :key="grandChild.id" 
                :index="formatPath(grandChild.path)"
              >
                <el-icon v-if="grandChild.icon"><component :is="iconMap[grandChild.icon] || grandChild.icon" /></el-icon>
                <span>{{ grandChild.name }}</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-sub-menu>
        
        <!-- 没有子菜单的情况 -->
        <el-menu-item v-else :index="formatPath(menu.path)" :key="menu.id">
          <el-icon v-if="menu.icon"><component :is="iconMap[menu.icon] || menu.icon" /></el-icon>
          <span>{{ menu.name }}</span>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import axios from 'axios'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const userStore = useUserStore()
const menuItems = ref([])

// 设置图标映射
const iconMap = {}
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  // 转换成小写并添加短横线（PascalCase 转 kebab-case）
  const kebabKey = key.replace(/([A-Z])/g, '-$1').toLowerCase().replace(/^-/, '')
  iconMap[kebabKey] = component
  // 也保留原名
  iconMap[key] = component
}

// 格式化菜单路径
const formatPath = (path) => {
  if (!path) return '';
  
  // 特殊处理仪表盘路径
  if (path === '/dashboard' || path === 'dashboard') {
    return '/back/dashboard';
  }
  
  // 已经包含 /back 的不用再加前缀
  if (path.startsWith('/back')) {
    return path;
  }
  
  // 确保路径以 / 开头
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `/back${normalizedPath}`;
}

// 当前激活的菜单路径
const activeMenu = computed(() => {
  return route.path
})

// 获取用户的菜单
const fetchUserMenus = async () => {
  try {
    if (!userStore.isLoggedIn || !userStore.userId) {
      return
    }
    
    // 使用模拟数据进行测试
    if (process.env.NODE_ENV === 'development') {
      const mockMenus = [
        {
          id: 1,
          name: '仪表盘',
          path: '/dashboard',
          icon: 'Odometer',
          children: []
        },
        {
          id: 2,
          name: '系统管理',
          path: '/system',
          icon: 'Setting',
          children: [
            {
              id: 21,
              name: '用户管理',
              path: '/system/user',
              icon: 'User',
              children: []
            },
            {
              id: 22,
              name: '角色管理',
              path: '/system/role',
              icon: 'Lock',
              children: []
            },
            {
              id: 23,
              name: '菜单管理',
              path: '/system/menu',
              icon: 'Menu',
              children: []
            }
          ]
        }
      ]
      menuItems.value = mockMenus
      return
    }
    
    // 实际环境下从API获取数据
    const response = await axios.get(`/menu/user/${userStore.userId}`)
    if (response.data.code === '0') {
      menuItems.value = response.data.data
    } else {
      console.error('Failed to fetch user menus:', response.data.msg)
    }
  } catch (error) {
    console.error('Error fetching user menus:', error)
  }
}

onMounted(() => {
  fetchUserMenus()
})
</script>

<style scoped>
.menu-container {
  height: 100%;
  background-color: #304156;
}

.menu:not(.el-menu--collapse) {
  width: 210px;
}

.menu {
  height: 100%;
  border-right: none;
}

.el-menu-item.is-active {
  background-color: #263445 !important;
}

/* 修复菜单折叠时的样式 */
.el-menu--vertical .el-menu-item {
  background-color: #1f2d3d;
  color: #bfcbd9;
}

.el-menu--vertical .el-menu-item:hover {
  background-color: #263445 !important;
}

.el-menu--vertical .el-menu-item.is-active {
  color: #409EFF;
}
</style> 