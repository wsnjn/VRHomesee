<template>
  <div class="page-container">
    <Navbar />
    <div class="page-header-spacer"></div>
    
    <div class="community-layout">
      <!-- Top Tab Navigation -->
      <div class="top-tabs">
        <div class="tab-container">
          <div 
            class="tab-item" 
            :class="{ active: currentTab === 'chat' }" 
            @click="currentTab = 'chat'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
            <span>聊天</span>
          </div>
          <div 
            class="tab-item" 
            :class="{ active: currentTab === 'feed' }" 
            @click="currentTab = 'feed'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
            <span>朋友圈</span>
          </div>
          <div 
            class="tab-item" 
            :class="{ active: currentTab === 'community' }" 
            @click="currentTab = 'community'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            <span>社区动态</span>
          </div>
        </div>
      </div>

      <!-- Content Area -->
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
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f8fafc;
}

.page-header-spacer {
  height: 80px;
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
  border-bottom: 1px solid #e2e8f0;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
  z-index: 10;
}

.tab-container {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  gap: 40px;
}

.tab-item {
  padding: 16px 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-weight: 500;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #334155;
}

.tab-item.active {
  color: #3b82f6;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #3b82f6;
  border-radius: 2px 2px 0 0;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  position: relative;
  /* 隐藏滚动条但保持滑动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.content-area::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
