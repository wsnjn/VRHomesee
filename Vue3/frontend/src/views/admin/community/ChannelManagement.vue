<template>
  <div class="channel-management">
    <div class="page-header">
      <h2>频道管理</h2>
      <div class="actions">
        <button class="btn-primary">新增频道</button>
      </div>
    </div>
    <div class="content-card">
      <div class="empty-state">
        <p>频道管理功能开发中...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.channel-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content-card {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.empty-state {
  text-align: center;
  color: #909399;
}

.btn-primary {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:hover {
  background-color: #66b1ff;
}
</style>
