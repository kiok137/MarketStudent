import { defineStore } from 'pinia'
import request from '@/utils/request'


export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    token: localStorage.getItem('token') || '',
    roleId: Number(localStorage.getItem('roleId')) || null,
    menus: JSON.parse(localStorage.getItem('menus')) || [],
    menusLoaded: false
  }),

  getters: {
    // 判断是否登录
    isLoggedIn: (state) => !!state.token,
    // 判断是否是管理员
    isAdmin: (state) => Number(state.roleId) === 1,
    // 判断是否是普通用户
    isUser: (state) => Number(state.roleId) === 2,
    // 判断是否是供应商
    isSupplier: (state) => Number(state.roleId) === 3
  },

  actions: {
    // 更新用户信息
    updateUserInfo(data) {
      if (!data) return
      this.userInfo = data
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    
    setUserInfo(data) {
      if (!data) return
      
      this.userInfo = data
      this.token = data.token
      this.roleId = Number(data.roleId)
      
      // 存储菜单
      if (data.menuList) {
        this.setMenus(data.menuList)
      }
      
      // 存储到 LocalStorage
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      localStorage.setItem('token', this.token || '')
      localStorage.setItem('roleId', this.roleId || '')
    },
    
    clearUserInfo() {
      this.userInfo = null
      this.token = ''
      this.roleId = null
      this.menus = []
      this.menusLoaded = false
      // 清除 LocalStorage
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      localStorage.removeItem('roleId')
      localStorage.removeItem('menus')
    },
    
    setMenus(menus) {
      if (!menus) return
      this.menus = menus
      this.menusLoaded = true
      localStorage.setItem('menus', JSON.stringify(menus))
    },
    
    // 获取用户信息和菜单 - 从localStorage恢复或从服务器获取
    async getUserInfo(forceRefresh = false) {
      // 如果不是强制刷新且已有数据，直接返回缓存的数据
      if (!forceRefresh && this.userInfo && this.token) {
        const userInfo = this.userInfo
        const menus = this.menus
        return { userInfo, menus }
      }
      
      // 如果没有token，则抛出错误但不清空状态
      if (!this.token) {
        throw new Error('No token available')
      }
      
      try {
        // 从服务器获取最新的用户信息
        const res = await request.get('/user/current')
        this.setUserInfo(res)
        return res
      } catch (error) {
        // 只有在明确的身份验证错误(401)时才清除用户信息
        if (error.code === "401" || (error.originalError && error.originalError.response && error.originalError.response.status === 401)) {
          this.clearUserInfo()
        }
        throw error
      }
    },
    
    // 登录
    async login(loginForm) {
      try {
        await request.post('/user/login', loginForm, {
          showDefaultMsg: false,
          onSuccess: (res) => {
            this.setUserInfo(res)
          },
          onError: (error) => {
            this.clearUserInfo()
            throw error
          }
        })

      } catch (error) {
        this.clearUserInfo()
        throw error
      }
    },
    
    // 注册
    async register(registerForm) {
      return await request.post('/user/add', registerForm)
    },

    // 退出登录
    async logout() {
      this.clearUserInfo()
      // try {
      //   await request.post('/user/logout')
      // } catch (error) {
      //   console.error('Logout failed:', error)
      // } finally {
      //   this.clearUserInfo()
      // }
    },
    
    // 检查登录状态
    checkLoginStatus() {
      return !!this.token
    },
    
    // 初始化认证状态 - 在应用启动时调用
    initAuth() {
      if (this.token && this.userInfo) {
        // 已有认证信息，尝试恢复菜单
        const menus = JSON.parse(localStorage.getItem('menus'))
        if (menus && menus.length > 0) {
          this.menus = menus
          this.menusLoaded = true
          return true
        }
        
        // 如果没有菜单数据，尝试从服务器获取
        return this.getUserInfo(true)
          .then(() => true)
          .catch(() => {
            this.clearUserInfo()
            return false
          })
      }
      
      return false
    }
  }
})