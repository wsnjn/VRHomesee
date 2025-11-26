<template>
  <div class="container">
    <h2>文件管理</h2>

    <!-- 上传文件 -->
    <div class="upload-section">
      <input type="file" @change="handleFileUpload" />
      <button @click="getFileList" class="refresh-btn">刷新列表</button>
    </div>

    <div class="file-list">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="fileList.length === 0" class="empty">暂无文件</div>
      <ul v-else>
        <li v-for="fileName in fileList" :key="fileName" class="file-item">
          <div class="file-info">
            <span class="file-name">{{ fileName }}</span>
          </div>
          <div class="file-actions">
            <button @click="downloadFile(fileName)">下载</button>
            <button @click="copyLink(getDownloadUrl(fileName))">复制链接</button>
            <button @click="deleteFile(fileName)">删除</button>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import axios from "axios";
import { ref, onMounted } from "vue";

const fileList = ref([]);
const loading = ref(false);

// API 配置
const API_HOST = "http://39.108.142.250:8088";
const API_ENDPOINTS = {
  UPLOAD: `${API_HOST}/api/files/upload`,
  LIST: `${API_HOST}/api/files/list`,
  DOWNLOAD: (fileName) => `${API_HOST}/api/files/download/${fileName}`,
  DELETE: (fileName) => `${API_HOST}/api/files/delete/${fileName}`
};

// 获取下载链接
const getDownloadUrl = (fileName) => {
  return API_ENDPOINTS.DOWNLOAD(fileName);
};

// 上传文件
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  try {
    loading.value = true;
    await axios.post(API_ENDPOINTS.UPLOAD, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    alert('上传成功');
    // 清空input
    event.target.value = '';
    await getFileList();
  } catch (error) {
    console.error('上传失败:', error);
    alert('上传失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

// 获取文件列表
const getFileList = async () => {
  try {
    loading.value = true;
    const res = await axios.get(API_ENDPOINTS.LIST);
    // 后端返回的是字符串数组 ["file1.jpg", "file2.png"]
    fileList.value = res.data;
  } catch (error) {
    console.error('获取列表失败:', error);
    alert('获取列表失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

// 下载文件
const downloadFile = (fileName) => {
  window.open(API_ENDPOINTS.DOWNLOAD(fileName));
};

// 删除文件
const deleteFile = async (fileName) => {
  if (!confirm(`确定要删除 ${fileName} 吗？`)) return;
  
  try {
    await axios.delete(API_ENDPOINTS.DELETE(fileName));
    await getFileList();
  } catch (error) {
    console.error('删除失败:', error);
    alert('删除失败');
  }
};

// 复制链接
const copyLink = (url) => {
  navigator.clipboard.writeText(url).then(() => {
    alert('链接已复制');
  }).catch(() => {
    alert('复制失败');
  });
};

// 初始化加载
onMounted(() => {
  getFileList();
});
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.upload-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.refresh-btn {
  padding: 5px 15px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.file-list ul {
  list-style: none;
  padding: 0;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.file-item:hover {
  background-color: #f9f9f9;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.file-name {
  font-weight: bold;
  color: #333;
}

.file-meta {
  font-size: 0.85em;
  color: #999;
}

.file-actions button {
  margin-left: 10px;
  padding: 5px 10px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.file-actions button:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>
