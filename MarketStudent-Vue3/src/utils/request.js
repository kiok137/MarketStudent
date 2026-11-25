import axios from 'axios'
import { ElMessage } from 'element-plus'

// 获取用户Store的函数
// 由于直接导入 pinia store 会导致创建顺序问题，使用函数延迟获取
const getUserStore = () => {
  const { useUserStore } = require('@/store/user')
  return useUserStore()
}

/**
 * 封装的 axios 请求工具
 * 
 * 特性：
 * 1. 自动携带 token
 * 2. 统一的错误处理
 * 3. 支持自定义成功/错误提示
 * 4. 支持自定义成功/错误回调
 * 
 * 配置选项：
 * @param {boolean} showDefaultMsg - 是否显示默认的成功/错误提示，默认为 true
 * @param {string} successMsg - 自定义成功提示消息
 * @param {string} errorMsg - 自定义错误提示消息
 * @param {Function} onSuccess - 成功回调函数，参数为响应数据
 * @param {Function} onError - 错误回调函数，参数为错误信息
 */

// 创建 axios 实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api', // API 的基础URL
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      // 设置token到请求头
      config.headers['token'] = token
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 处理401错误，使用store管理登出逻辑
const handle401Error = () => {
  try {
    const userStore = getUserStore()
    // 使用store的方法清空用户信息，而不是直接操作localStorage
    userStore.clearUserInfo()
    
    // 如果当前不是在首页，才跳转到登录页
    if (window.location.pathname !== '/' && window.location.pathname !== '/login') {
      // 跳转到登录页
      window.location.href = '/login'
    }
  } catch (e) {
    console.error('处理401错误时出现问题:', e)
    // 如果获取store失败，则使用备用方案
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    if (window.location.pathname !== '/' && window.location.pathname !== '/login') {
      window.location.href = '/login'
    }
  }
}

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    const config = response.config || {}
    const showDefaultMsg = config.showDefaultMsg !== false

    // 检查响应状态
    if (res.code === "200") {
      try {
        if (config.successMsg) {
          ElMessage.success(config.successMsg)
        } else if (showDefaultMsg && config.method && config.method.toLowerCase() !== 'get') {
          ElMessage.success('操作成功')
        }
        
        if (typeof config.onSuccess === 'function') {
          config.onSuccess(res.data)
        }
        
        return res.data
      } catch (err) {
        console.error('Success handler error:', err)
        return res.data
      }
    } else {
      try {
        if (res.code === "401") {
          // 使用统一处理401的方法
          handle401Error()
          return Promise.reject(new Error('未登录或登录已过期'))
        }

        if (config.errorMsg) {
          ElMessage.error(config.errorMsg)
        } else if (showDefaultMsg) {
          let errorMessage = res.msg || '请求失败'
          
          switch (res.code) {
            case "401":
              errorMessage = '未登录或登录已过期，请重新登录'
              break
            case "403":
              errorMessage = '没有权限进行此操作'
              break
            case "404":
              errorMessage = '请求的资源不存在'
              break
            case "500":
              errorMessage = '服务器内部错误'
              break
            default:
              errorMessage = res.msg || `请求失败(${res.code})`
          }
          
          ElMessage.error(errorMessage)
        }
        
        if (typeof config.onError === 'function') {
          config.onError(res)
        }
        
        return Promise.reject({
          code: res.code,
          message: res.msg || '请求失败',
          data: res.data,
          type: 'business'
        })
      } catch (err) {
        console.error('Error handler error:', err)
        return Promise.reject(err)
      }
    }
  },
  error => {
    const config = error.config || {}
    
    if (error.response && error.response.status === 401) {
      // 使用统一处理401的方法
      ElMessage.error('未登录或登录已过期，请重新登录')
      handle401Error()
      return Promise.reject(new Error('未登录或登录已过期'))
    }
    
    if (typeof config.onError === 'function') {
      try {
        config.onError(error)
      } catch (err) {
        console.error('Error callback error:', err)
      }
    }
    
    if (config.showDefaultMsg !== false) {
      let message = config.errorMsg || '网络请求失败，请稍后重试'
      
      if (error.response) {
        switch (error.response.status) {
          case 400:
            message = '请求参数错误'
            break
          case 401:
            message = '未授权，请重新登录'
            break
          case 403:
            message = '拒绝访问'
            break
          case 404:
            message = '请求的资源不存在'
            break
          case 408:
            message = '请求超时'
            break
          case 500:
            message = '服务器内部错误'
            break
          case 501:
            message = '服务未实现'
            break
          case 502:
            message = '网关错误'
            break
          case 503:
            message = '服务不可用'
            break
          case 504:
            message = '网关超时'
            break
          default:
            message = error.response.data?.msg || `请求失败(${error.response.status})`
        }
      } else if (error.code === 'ECONNABORTED') {
        message = '请求超时，请检查网络连接'
      } else if (error.message?.includes('Network Error')) {
        message = '网络连接失败，请检查网络设置'
      }
      
      ElMessage({
        message: message,
        type: 'error',
        duration: 5000,
        showClose: true
      })
    }
    
    return Promise.reject({
      code: error.response?.status,
      message: error.message,
      data: error.response?.data,
      type: 'http',
      originalError: error
    })
  }
)

// 扩展请求方法
const request = {
  get(url, params, config = {}) {
    return service.get(url, { params, ...config })
  },
  
  post(url, data, config = {}) {
    return service.post(url, data, config)
  },
  
  put(url, data, config = {}) {
    return service.put(url, data, config)
  },
  
  delete(url, config = {}) {
    return service.delete(url, config)
  }
}

/**
 * 请求方法使用示例：
 * 
 * 1. 基础请求：
 * // GET 请求
 * request.get('/api/users', { page: 1 })
 * 
 * // POST 请求
 * request.post('/api/users', { name: 'Tom', age: 20 })
 * 
 * // PUT 请求
 * request.put('/api/users/1', { name: 'Tom' })
 * 
 * // DELETE 请求
 * request.delete('/api/users/1')
 * 
 * 2. 自定义提示消息：
 * request.post('/api/users', data, {
 *   successMsg: '添加用户成功！',
 *   errorMsg: '添加用户失败，请重试'
 * })
 * 
 * 3. 关闭默认提示：
 * request.post('/api/users', data, {
 *   showDefaultMsg: false
 * })
 * 
 * 4. 使用回调函数：
 * request.post('/api/users', data, {
 *   onSuccess: (data) => {
 *     console.log('请求成功：', data)
 *   },
 *   onError: (error) => {
 *     console.log('请求失败：', error)
 *   }
 * })
 * 
 * 5. 完整示例：
 * request.post('/api/users', data, {
 *   successMsg: '添加成功',
 *   errorMsg: '添加失败',
 *   showDefaultMsg: true,
 *   onSuccess: (data) => {
 *     // 处理成功逻辑
 *   },
 *   onError: (error) => {
 *     // 处理错误逻辑
 *   }
 * })
 */

export default request 