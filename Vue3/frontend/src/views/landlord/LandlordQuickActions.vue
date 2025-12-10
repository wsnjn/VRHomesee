<template>
  <div class="quick-actions-container">
    <div class="section-header">
      <h2>快捷操作</h2>
    </div>
    <div class="actions-grid">
      <button class="action-btn primary" @click="$emit('navigate', '/landlord/add')">
        <div class="icon-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
          </svg>
        </div>
        <span>发布房源</span>
      </button>

      <button class="action-btn" @click="$emit('navigate', '/landlord/maintenance')">
        <div class="icon-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z"/>
          </svg>
        </div>
        <span>处理报修</span>
        <span v-if="pendingCount > 0" class="badge">{{ pendingCount }}</span>
      </button>

      <button class="action-btn" @click="$emit('navigate', '/landlord/matching')">
        <div class="icon-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
          </svg>
        </div>
        <span>租客匹配</span>
      </button>

      <button class="action-btn" @click="$emit('navigate', '/landlord/tenants')">
        <div class="icon-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M20 0H4v2h16V0zm4 4H0v2h24V4zm-4 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-8 2.75c1.24 0 2.25 1.01 2.25 2.25s-1.01 2.25-2.25 2.25S9.75 14.24 9.75 13s1.01-2.25 2.25-2.25zM17 20H7v-1.5c0-1.67 3.33-2.5 5-2.5s5 .83 5 2.5V20z"/>
          </svg>
        </div>
        <span>租客名录</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  pendingCount: {
    type: Number,
    default: 0
  }
});

defineEmits(['navigate']);
</script>

<style scoped>
.quick-actions-container {
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 16px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.action-btn {
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  transition: all 0.2s ease;
  position: relative;
  outline: none;
}

.action-btn:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
  transform: translateY(-2px);
}

.action-btn.primary {
  background: linear-gradient(135deg, #6366F1 0%, #4F46E5 100%);
  border: none;
  color: white;
}

.action-btn.primary .icon-wrapper {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.action-btn.primary span {
  color: white;
}

.action-btn.primary:hover {
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  background: #F1F5F9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #64748B;
  transition: all 0.2s ease;
}

.action-btn span {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #EF4444;
  color: white;
  font-size: 12px;
  font-weight: 700;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  border: 2px solid white;
}
</style>
