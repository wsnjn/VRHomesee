<template>
  <div class="resource-management">
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
        <div class="header-actions">
          <div class="pagination-info">
            共 {{ getFilteredFiles().length }} 个文件，第 {{ currentPage }} 页
          </div>
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
        <p>请联系管理员上传文件</p>
      </div>

      <!-- 文件网格 -->
      <div v-else class="file-grid">
        <div 
          v-for="fileName in getPaginatedFiles()" 
          :key="fileName" 
          class="file-card"
          :class="getFileTypeClass(fileName)"
        >
          <div class="file-preview">
            <div v-if="getFileType(fileName) === 'image'" class="preview-container">
              <img :src="getDownloadUrl(fileName)" :alt="fileName" class="preview-image" loading="lazy" />
            </div>
            <div v-else-if="getFileType(fileName) === 'video'" class="preview-container">
              <video :src="getDownloadUrl(fileName)" class="preview-video" controls preload="metadata"></video>
            </div>
            <div v-else class="file-icon">
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

      <!-- 分页控件 -->
      <div v-if="getFilteredFiles().length > pageSize" class="pagination">
        <button 
          @click="prevPage" 
          :disabled="currentPage === 1" 
          class="pagination-btn prev"
        >
          上一页
        </button>
        <span class="pagination-info">
          第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
        </span>
        <button 
          @click="nextPage" 
          :disabled="currentPage === totalPages" 
          class="pagination-btn next"
        >
          下一页
        </button>
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
const currentPage = ref(1);
const pageSize = 9; // 每页显示9个文件

// API 配置
const API_HOST = "https://files.homesee.xyz";
const API_ENDPOINTS = {
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

// 计算属性
const totalPages = computed(() => {
  return Math.ceil(getFilteredFiles().length / pageSize);
});

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

// 获取分页文件列表
const getPaginatedFiles = () => {
  const filteredFiles = getFilteredFiles();
  const startIndex = (currentPage.value - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  return filteredFiles.slice(startIndex, endIndex);
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

// 获取文件列表
const getFileList = async () => {
  try {
    loading.value = true;
    const res = await axios.get(API_ENDPOINTS.LIST);
    fileList.value = res.data;
    currentPage.value = 1; // 重置到第一页
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

// 分页方法
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

// 监听分类变化时重置页码
const watchCategory = () => {
  currentPage.value = 1;
};

// 初始化加载
onMounted(() => {
  getFileList();
});
</script>

<style scoped>
.resource-management {
  padding: 0;
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  min-height: 100vh;
}

/* 分类导航 */
.category-nav {
  margin-bottom: 16px;
}

.nav-tabs {
  display: flex;
  gap: 0;
  background: #fff;
  border: 1px solid #ddd;
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  border-right: 1px solid #ddd;
}

.nav-tab:last-child {
  border-right: none;
}

.nav-tab:hover {
  background: #f5f5f5;
  color: #333;
}

.nav-tab.active {
  background: #1e3a5f;
  color: #fff;
}

.tab-count {
  background: rgba(255, 255, 255, 0.3);
  padding: 1px 6px;
  font-size: 10px;
  font-weight: 500;
}

/* 文件内容区域 */
.file-content {
  background: #fff;
  border: 1px solid #ddd;
  padding: 16px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ddd;
  flex-wrap: wrap;
  gap: 8px;
}

.content-header h2 {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-info {
  font-size: 11px;
  color: #888;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: transparent;
  border: 1px solid #ddd;
  color: #888;
  cursor: pointer;
  font-size: 12px;
}

.refresh-btn:hover {
  background: #f5f5f5;
  color: #333;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #888;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #ddd;
  border-top: 2px solid #3A6EA5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 8px;
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
  padding: 40px 20px;
  color: #888;
}

.empty-icon {
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #333;
}

/* 文件网格 */
.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 0;
}

.file-card {
  background: #fff;
  border: 1px solid #ddd;
  border-bottom: none;
  padding: 12px;
  position: relative;
  overflow: hidden;
}

.file-card:last-child {
  border-bottom: 1px solid #ddd;
}

.file-card:hover {
  background: #f9f9f9;
}

.file-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 8px;
}

.file-icon {
  font-size: 2rem;
  line-height: 1;
}

.file-icon svg {
  width: 32px;
  height: 32px;
  color: #888;
}

.preview-container {
  width: 100%;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background-color: #f9f9f9;
  border: 1px solid #eee;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-info {
  text-align: left;
  margin-bottom: 8px;
}

.file-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
  word-break: break-all;
  font-size: 12px;
}

.file-type {
  font-size: 10px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.file-actions {
  display: flex;
  justify-content: flex-start;
  gap: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: 1px solid #ddd;
  cursor: pointer;
  color: #888;
  background: transparent;
}

.action-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.action-btn.download:hover {
  background: #2d8a4e;
  color: #fff;
  border-color: #2d8a4e;
}

.action-btn.copy:hover {
  background: #3A6EA5;
  color: #fff;
  border-color: #3A6EA5;
}

.action-btn.delete:hover {
  background: #c00;
  color: #fff;
  border-color: #c00;
}

/* 文件类型颜色 - 底部边框 */
.file-type-image {
  border-bottom: 3px solid #2d8a4e;
}

.file-type-document {
  border-bottom: 3px solid #3A6EA5;
}

.file-type-video {
  border-bottom: 3px solid #c07700;
}

.file-type-audio {
  border-bottom: 3px solid #6b5b95;
}

.file-type-other {
  border-bottom: 3px solid #888;
}

/* 分页控件 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ddd;
}

.pagination-btn {
  padding: 4px 12px;
  border: 1px solid #ddd;
  background: transparent;
  cursor: pointer;
  color: #888;
  font-size: 12px;
}

.pagination-btn:hover:not(:disabled) {
  background: #1e3a5f;
  color: #fff;
  border-color: #1e3a5f;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 11px;
  color: #888;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-tabs {
    flex-wrap: wrap;
  }
  
  .nav-tab {
    padding: 6px 10px;
    font-size: 11px;
  }
  
  .file-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .file-card {
    padding: 10px;
  }
  
  .preview-container {
    height: 80px;
  }
  
  .content-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .file-grid {
    grid-template-columns: 1fr;
  }
  
  .pagination {
    flex-direction: column;
    gap: 8px;
  }
}
</style>