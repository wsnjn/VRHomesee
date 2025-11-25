<template>
  <div class="community-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>社区中心</h2>
      </div>
      <div class="menu-items">
        <div class="menu-item" :class="{ active: currentTab === 'chat' }" @click="currentTab = 'chat'">
          <span class="icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
          </span>
          <span class="label">聊天</span>
        </div>
        <div class="menu-item" :class="{ active: currentTab === 'feed' }" @click="currentTab = 'feed'">
          <span class="icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
          </span>
          <span class="label">朋友圈</span>
        </div>
        <div class="menu-item" :class="{ active: currentTab === 'community' }" @click="currentTab = 'community'">
          <span class="icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
          </span>
          <span class="label">社区动态</span>
        </div>
      </div>
    </div>
    
    <div class="content-area">
      <transition name="fade" mode="out-in">
        <ChatArea v-if="currentTab === 'chat'" key="chat" />
        <FriendCircle v-else-if="currentTab === 'feed'" key="feed" />
        <CommunityPage v-else-if="currentTab === 'community'" key="community" />
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatArea from './ChatArea.vue'
import FriendCircle from './FriendCircle.vue'
import CommunityPage from './CommunityPage.vue'

const currentTab = ref('chat')
</script>

<style scoped>
.community-container {
  display: flex;
  height: calc(100vh - 64px); /* Adjust based on navbar height */
  background: #f8fafc;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  background: white;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.02);
  z-index: 10;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.025em;
}

.menu-items {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #64748b;
  font-weight: 500;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background: #f1f5f9;
  color: #334155;
}

.menu-item.active {
  background: #eff6ff;
  color: #3b82f6;
}

.menu-item .icon {
  font-size: 20px;
}

.content-area {
  flex: 1;
  overflow: auto;
  position: relative;
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
