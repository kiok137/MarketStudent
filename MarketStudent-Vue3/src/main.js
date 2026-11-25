import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { initDynamicRoutes } from './router'
import { useUserStore } from './store/user'
// 导入 Element Plus
import ElementPlus from 'element-plus'
// 导入自定义主题色配置
import './styles/element-variables.scss'
// import 'element-plus/dist/index.css'
// 导入 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 导入初始化样式
import './assets/global.css'
import '@/assets/styles/menu.scss'
// 中文
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化用户认证状态和路由
const initApp = async () => {
  // 确保pinia store已准备好
  const userStore = useUserStore()
  
  // 初始化用户认证状态
  if (userStore.token) {
    try {
      // 初始化用户认证
      await userStore.initAuth()
      
      // 初始化动态路由
      initDynamicRoutes()
    } catch (error) {
      console.error('Failed to initialize authentication:', error)
    }
  }
  
  // 挂载应用
  app.mount('#app')
}
window.ResizeObserver = class _NewResizeObserver extends ResizeObserver {
  constructor(callback) {
    super(() => window.requestAnimationFrame(() => callback.apply(this, arguments)));
  }
}
// 启动应用
initApp()
