/**
 * 文件名: main.js
 * 作者: 牛迦楠
 * 学校: 东华理工大学
 * 专业: 软件工程（中外合作办学）
 * 课题: 融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 创建日期: 2026-01-06
 * 描述: Vue应用入口文件，初始化Vue应用实例并挂载到DOM
 */

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)

app.mount('#app')
