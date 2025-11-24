<template>
  <div class="community-container">
    <div class="sidebar">
      <div class="menu-item" :class="{ active: currentTab === 'chat' }" @click="currentTab = 'chat'">
        💬 聊天
      </div>
      <div class="menu-item" :class="{ active: currentTab === 'feed' }" @click="currentTab = 'feed'">
        📱 朋友圈
      </div>
    </div>
    
    <div class="content-area">
      <ChatArea v-if="currentTab === 'chat'" />
      <DailyFeed v-else-if="currentTab === 'feed'" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatArea from './ChatArea.vue'
import DailyFeed from './DailyFeed.vue'

const currentTab = ref('chat')
</script>

<style scoped>
.community-container {
  display: flex;
  height: calc(100vh - 60px); /* Adjust based on navbar height */
  background: #f5f5f5;
}

.sidebar {
  width: 200px;
  background: white;
  border-right: 1px solid #e0e0e0;
  padding: 20px 0;
}

.menu-item {
  padding: 15px 20px;
  cursor: pointer;
  transition: background 0.2s;
  font-weight: 500;
}

.menu-item:hover {
  background: #f0f0f0;
}

.menu-item.active {
  background: #e3f2fd;
  color: #1976d2;
  border-right: 3px solid #1976d2;
}

.content-area {
  flex: 1;
  overflow: hidden;
}
</style>
