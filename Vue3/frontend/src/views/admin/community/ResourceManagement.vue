<template>
  <div class="resource-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>资源管理中心</h1>
      <p>管理社区共享资源，支持多种文件类型</p>
    </div>

    <!-- 上传区域 -->
    <div class="upload-section">
      <div class="upload-card">
        <div class="upload-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="17 8 12 3 7 8"></polyline>
            <line x1="12" y1="3" x2="12" y2="15"></line>
          </svg>
        </div>
        <div class="upload-content">
          <h3>上传文件</h3>
          <p>支持图片、文档、视频等多种格式</p>
          <input type="file" @change="handleFileUpload" class="file-input" id="fileInput" />
          <label for="fileInput" class="upload-btn">选择文件</label>
        </div>
      </div>
    </div>

    <!-- 文件分类导航 -->
    <div class="category-nav">
      <div class="nav-tabs">
        <button 
          v-for="category in categories" 
          :key="category.type"
          :class="['nav-tab', { active: activeCategory === category.type }]"
          @click="activeCategory = category.type"
        >
          <span class="tab-icon">
            <svg-icon :type="category.icon" />
          </span>
          <span class="tab-text">{{ category.name }}</span>
          <span class="tab-count" v-if="getCategoryCount(category.type) > 0">
            {{ getCategoryCount(category.type) }}
          </span>
        </button>
      </div>
    </div>

    <!-- 文件列表 -->
    <div class="file-content">
      <div class="content-header">
        <h2>{{ getActiveCategoryName() }}</h2>
        <button @click="getFileList" class="refresh-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M23 4v6h-6"></path>
            <path d="M1 20v-6h6"></path>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10"></path>
            <path d="M20.49 15a9 9 0 0 1-14.85 3.36L1 14"></path>
          </svg>
          刷新
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>正在加载文件...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="getFilteredFiles().length === 0" class="empty-state">
        <div class="empty-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
            <polyline points="10 9 9 9 8 9"></polyline>
          </svg>
        </div>
        <h3>暂无文件</h3>
        <p>点击上方按钮上传文件</p>
      </div>

      <!-- 文件网格 -->
      <div v-else class="file-grid">
        <div 
          v-for="fileName in getFilteredFiles()" 
          :key="fileName" 
          class="file-card"
          :class="getFileTypeClass(fileName)"
        >
          <div class="file-preview">
            <div class="file-icon">
              <svg-icon :type="getFileIcon(fileName)" />
            </div>
          </div>
          <div class="file-info">
            <div class="file-name" :title="fileName">{{ truncateFileName(fileName) }}</div>
            <div class="file-type">{{ getFileTypeName(fileName) }}</div>
          </div>
          <div class="file-actions">
            <button @click="downloadFile(fileName)" class="action-btn download" title="下载">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
            </button>
            <button @click="copyLink(getDownloadUrl(fileName))" class="action-btn copy" title="复制链接">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
              </svg>
            </button>
            <button @click="deleteFile(fileName)" class="action-btn delete" title="删除">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from "axios";
import { ref, onMounted, computed } from "vue";
import SvgIcon from "@/components/SvgIcon.vue";

const fileList = ref([]);
const loading = ref(false);
const activeCategory = ref('all');

// API 配置
const API_HOST = "http://39.108.142.250:8088";
const API_ENDPOINTS = {
  UPLOAD: `${API_HOST}/api/files/upload`,
  LIST: `${API_HOST}/api/files/list`,
  DOWNLOAD: (fileName) => `${API_HOST}/api/files/download/${fileName}`,
  DELETE: (fileName) => `${API_HOST}/api/files/delete/${fileName}`
};

// 文件类型分类
const categories = [
  { type: 'all', name: '全部文件', icon: 'folder' },
  { type: 'image', name: '图片', icon: 'image' },
  { type: 'document', name: '文档', icon: 'file-text' },
  { type: 'video', name: '视频', icon: 'video' },
  { type: 'audio', name: '音频', icon: 'music' },
  { type: 'other', name: '其他', icon: 'paperclip' }
];

// 文件类型映射
const fileTypes = {
  image: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'],
  document: ['pdf', 'doc', 'docx', 'txt', 'xls', 'xlsx', 'ppt', 'pptx'],
  video: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'],
  audio: ['mp3', 'wav', 'ogg', 'flac', 'aac']
};

// 获取文件类型
const getFileType = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase();
  for (const [type, exts] of Object.entries(fileTypes)) {
    if (exts.includes(ext)) {
      return type;
    }
  }
  return 'other';
};

// 获取文件图标
const getFileIcon = (fileName) => {
  const type = getFileType(fileName);
  const icons = {
    image: 'image',
    document: 'file-text',
    video: 'video',
    audio: 'music',
    other: 'paperclip'
  };
  return icons[type] || 'paperclip';
};

// 获取文件类型名称
const getFileTypeName = (fileName) => {
  const type = getFileType(fileName);
  const names = {
    image: '图片',
    document: '文档',
    video: '视频',
    audio: '音频',
    other: '其他'
  };
  return names[type] || '其他';
};

// 获取文件类型CSS类
const getFileTypeClass = (fileName) => {
  return `file-type-${getFileType(fileName)}`;
};

// 获取分类文件数量
const getCategoryCount = (category) => {
  if (category === 'all') return fileList.value.length;
  return fileList.value.filter(file => getFileType(file) === category).length;
};

// 获取当前分类名称
const getActiveCategoryName = () => {
  const category = categories.find(cat => cat.type === activeCategory.value);
  return category ? category.name : '全部文件';
};

// 获取过滤后的文件列表
const getFilteredFiles = () => {
  if (activeCategory.value === 'all') return fileList.value;
  return fileList.value.filter(file => getFileType(file) === activeCategory.value);
};

// 截断文件名
const truncateFileName = (fileName) => {
  if (fileName.length > 20) {
    return fileName.substring(0, 17) + '...';
  }
  return fileName;
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
.resource-management {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: #f8fafc;
  min-height: 100vh;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 1.1rem;
  color: #64748b;
}

/* 上传区域 */
.upload-section {
  margin-bottom: 32px;
}

.upload-card {
  background: white;
  border: 2px dashed #e2e8f0;
  border-radius: 16px;
  padding: 40px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-card:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.upload-icon {
  margin-bottom: 16px;
  color: #64748b;
}

.upload-content h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.upload-content p {
  color: #64748b;
  margin-bottom: 20px;
}

.file-input {
  display: none;
}

.upload-btn {
  display: inline-block;
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s ease;
}

.upload-btn:hover {
  background: #2563eb;
}

/* 分类导航 */
.category-nav {
  margin-bottom: 24px;
}

.nav-tabs {
  display: flex;
  gap: 8px;
  background: white;
  padding: 8px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #64748b;
}

.nav-tab:hover {
  background: #f1f5f9;
  color: #334155;
}

.nav-tab.active {
  background: #3b82f6;
  color: white;
}

.tab-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

/* 文件内容区域 */
.file-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.content-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: #e2e8f0;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #64748b;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #64748b;
}

.empty-icon {
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #475569;
}

/* 文件网格 */
.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.file-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.file-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  border-color: #3b82f6;
}

.file-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

.file-icon {
  font-size: 3rem;
  line-height: 1;
}

.file-icon svg {
  width: 48px;
  height: 48px;
}

.file-info {
  text-align: center;
  margin-bottom: 16px;
}

.file-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
  word-break: break-all;
}

.file-type {
  font-size: 0.875rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.file-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #64748b;
  background: #f8fafc;
}

.action-btn:hover {
  transform: scale(1.1);
}

.action-btn.download:hover {
  background: #10b981;
  color: white;
}

.action-btn.copy:hover {
  background: #3b82f6;
  color: white;
}

.action-btn.delete:hover {
  background: #ef4444;
  color: white;
}

/* 文件类型颜色 */
.file-type-image {
  border-left: 4px solid #10b981;
}

.file-type-document {
  border-left: 4px solid #3b82f6;
}

.file-type-video {
  border-left: 4px solid #f59e0b;
}

.file-type-audio {
  border-left: 4px solid #8b5cf6;
}

.file-type-other {
  border-left: 4px solid #64748b;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .resource-management {
    padding: 16px;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .nav-tabs {
    flex-wrap: wrap;
  }
  
  .file-grid {
    grid-template-columns: 1fr;
  }
  
  .upload-card {
    padding: 24px;
  }
}
</style>
