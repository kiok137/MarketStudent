import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'
import { h } from 'vue'

// 静态路由
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forget',
    name: 'ForgetPassword',
    component: () => import('@/views/auth/ForgetPassword.vue'),
    meta: {
      title: '找回密码',
      requiresAuth: false
    }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '权限不足' }
  }
]

// 基础后台路由
export const backendBaseRoutes = {
  path: '/back',
  name: 'back',
  component: BackendLayout,
  redirect: '/back/dashboard',
  children: [
    {
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/backend/Dashboard.vue'),
      meta: { title: '仪表盘', icon: 'dashboard' }
    }
  ]
}

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/views/HomeView.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { 
          title: '首页',
          requiresAuth: false
        }
      },
    ]
  }
]

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...constantRoutes,
    backendBaseRoutes,
  ]
})

// 标记是否已添加404路由
let has404Route = false

// 动态加载组件
export function loadComponent(component) {
  try {
    // 处理路径中的斜杠，转换为实际的文件路径
    let path = component
    // 去掉开头的/，如果存在
    if (path.startsWith('/')) {
      path = path.substring(1)
    }
    // 确保路径不包含重复的斜杠
    path = path.replace(/\/+/g, '/')
    // 动态导入组件
    return () => import(`@/views/backend/${path}.vue`)
  } catch (error) {
    console.error('Component loading error:', error)
    return {
      render() {
        return h('div', { style: { padding: '20px', color: '#f56c6c' } }, '组件加载失败')
      }
    }
  }
}

// 生成动态路由
export function generateAsyncRoutes(menuItems, parentPath = '') {
  if (!menuItems || !menuItems.length) {
    console.warn('No menus provided for route generation')
    return []
  }

  const routes = []
  
  menuItems.forEach(menu => {
    if (!menu.path) {
      console.warn('Invalid menu item: missing path', menu)
      return
    }
    // 规范化路径，对嵌套路由做特殊处理
    let path = menu.path
    // 移除开头的斜杠，避免嵌套路由路径错误
    if (path.startsWith('/')) {
      path = path.substring(1)
    }
    // 如果是子路由，并且路径包含父路由路径，则需要去掉重复部分
    if (parentPath && path.startsWith(parentPath)) {
      path = path.substring(parentPath.length)
      // 确保子路由路径不以斜杠开头
      if (path.startsWith('/')) {
        path = path.substring(1)
      }
    }
    
    // 构建路由对象
    const route = {
      path,
      name: menu.name,
      component: menu.component ? loadComponent(menu.component) : null,
      meta: {
        title: menu.name,
        icon: menu.icon,
        hidden: menu.hidden === 1
      }
    }

    // 处理子菜单
    if (menu.children && menu.children.length > 0) {
      // 计算当前路径，用于处理子路由
      const currentPath = path.endsWith('/') ? path : path + '/'
      
      // 生成子路由，传递父路径
      route.children = generateAsyncRoutes(menu.children, currentPath)
    }

    routes.push(route)
  })

  return routes
}

// 初始化动态路由
export function initDynamicRoutes() {
  const userStore = useUserStore()
  if (!userStore.isLoggedIn ) {
    return false
  }

  try {
    // 先移除之前可能存在的404路由
    if (has404Route) {
      router.removeRoute('catch-all')
      has404Route = false
    }

    // 从Pinia store获取菜单数据
    let menus = userStore.menus || []
    
    // 使用默认菜单（如果没有菜单数据）
    if (!menus || menus.length === 0) {
      menus = [
        {
          path: '/back/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/backend/Dashboard.vue'),
          meta: { title: '仪表盘', icon: 'Odometer' }
        }
      ]
    }
    
    // 重新格式化菜单数据，移除前导斜杠
    const formattedMenus = menus.map(menu => {
      // 确保路径不以 / 开头
      let menuPath = menu.path;
      if (menuPath.startsWith('/')) {
        menuPath = menuPath.substring(1);
      }
      
      // 处理子菜单
      const children = menu.children ? menu.children.map(child => {
        // 子菜单路径处理
        let childPath = child.path;
        if (childPath.startsWith('/')) {
          childPath = childPath.substring(1);
        }
        
        return {
          ...child,
          path: childPath
        };
      }) : [];
      
      return {
        ...menu,
        path: menuPath,
        children
      };
    });
    
    // 生成路由配置
    const dynamicRoutes = generateAsyncRoutes(formattedMenus);
    
    // 确保back路由存在
    const backRouteExists = router.hasRoute('back')
    if (!backRouteExists) {
      // 创建一个新的后台布局路由
      router.addRoute({
        path: '/back',
        name: 'back',
        component: BackendLayout,
        redirect: '/back/dashboard',
        children: [
          {
            path: 'dashboard',
            name: 'Dashboard',
            component: () => import('@/views/backend/Dashboard.vue'),
            meta: { title: '仪表盘', icon: 'Odometer' }
          }
        ]
      })
    }
    
    // 将动态路由添加到路由器中
    dynamicRoutes.forEach(route => {
      try {
        if (!router.hasRoute(route.name)) {
          router.addRoute('back', route)
        }
      } catch (e) {
        console.error(`Error adding route ${route.name}:`, e)
      }
    })
    
    // 添加404路由在最后
    router.addRoute({
      path: '/:pathMatch(.*)*',
      name: 'catch-all',
      redirect: '/404'
    })
    has404Route = true
    
    return true
  } catch (error) {
    console.error('Error initializing dynamic routes:', error)
    return false
  }
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 超市管理系统`
  }

  const userStore = useUserStore()
  
  // 首页不需要重定向到登录页面，直接放行
  if (to.path === '/') {
    next()
    return
  }
  
  // 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    console.log('需要登录权限')
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 检查是否需要管理员权限
  if (to.matched.some(record => record.meta.requiresAdmin) && !userStore.isAdmin) {
    next('/403')
    return
  }

  // 已登录用户的路由控制
  if (userStore.isLoggedIn) {
    // 处理登录页面访问
    if (to.path === '/login' || to.path === '/register') {

        next('/back/dashboard')

      return
    }


      // 强制在每次导航时初始化动态路由（解决刷新问题）
      if (to.path.startsWith('/back') && to.matched.length === 0) {
        const initialized = initDynamicRoutes()
        if (initialized) {
          // 重定向到同一个URL，此时路由应该已注册
          next({ path: to.fullPath, replace: true })
          return
        }
      
    }

  } else {
    // 未登录用户访问后台
    if (to.path.startsWith('/back')) {
      console.log('未登录用户访问后台')
      next('/login')
      return
    }
  }

  // 处理未匹配到的路由
  if (to.matched.length === 0) {
    // 如果是管理员，尝试初始化路由后再判断
    if (userStore.isAdmin && to.path !== '/404') {
      const initialized = initDynamicRoutes()
      if (initialized) {
        next({ path: to.fullPath, replace: true })
        return
      }
    } else {
      console.log('userStore.isAdmin',userStore.roleId)
      next('/404')
      return
    }
  }

  next()
})

export default router
