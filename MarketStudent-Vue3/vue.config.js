const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  
  devServer: {
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
