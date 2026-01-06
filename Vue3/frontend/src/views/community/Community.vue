<!--
  项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
  文件名称：Community.vue
  开发者：牛迦楠
  专业：软件工程（中外合作办学）
  学校：东华理工大学
  功能描述：社区模块主入口组件，整合聊天、朋友圈和社区动态Tab页
  创建日期：2026-01-06
-->
<template>
  <div class="page-container">
    <Navbar />
    <div class="page-header-spacer"></div>
    
    <div class="community-layout">
      <!-- 顶部标签导航 -->
      <div class="top-tabs">
        <div class="tab-container">
          <div 
            class="tab-item" 
            :class="{ active: currentTab === 'chat' }" 
            @click="currentTab = 'chat'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
            <span>聊天</span>
          </div>
          <div 
            class="tab-item" 
            :class="{ active: currentTab === 'feed' }" 
            @click="currentTab = 'feed'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
            <span>朋友圈</span>
          </div>
          <div 
            class="tab-item" 
            :class="{ active: currentTab === 'community' }" 
            @click="currentTab = 'community'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            <span>社区动态</span>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area">
        <transition name="fade" mode="out-in">
          <ChatArea v-if="currentTab === 'chat'" key="chat" />
          <FriendCircle v-else-if="currentTab === 'feed'" key="feed" />
          <CommunityPage v-else-if="currentTab === 'community'" key="community" />
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '../../components/Navbar.vue'
import ChatArea from './ChatArea.vue'
import FriendCircle from './FriendCircle.vue'
import CommunityPage from './CommunityPage.vue'

const currentTab = ref('chat')
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #FFFFFF;
  font-family: 'Inter', sans-serif;
}

.page-header-spacer {
  height: 64px; /* 与导航栏匹配 */
  flex-shrink: 0;
}

.community-layout {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-tabs {
  background: white;
  border-bottom: 1px solid #E5E7EB;
  padding: 0 24px;
  box-shadow: none;
  z-index: 10;
}

.tab-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 0; /* 移除间距以使标签连通 */
}

.tab-item {
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6B7280;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  border-right: 1px solid transparent;
  border-left: 1px solid transparent;
  font-size: 0.9rem;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.tab-item:hover {
  background: #F9FAFB;
  color: #111827;
}

.tab-item.active {
  color: #111827;
  background: white;
  font-weight: 700;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #111827;
  border-radius: 0;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  position: relative;
  background: #FFFFFF;
}

/* 过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
