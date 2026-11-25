const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  
  devServer: {
    // 直连子路径（刷新/粘贴地址）时回退到 index.html，避免 404
    historyApiFallback: true,
    client: {
      overlay: false
    },
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:1234',
        changeOrigin: true,
        ws: true
      }
    }
  }
})
