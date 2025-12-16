<template>
  <div class="channel-management">
    <div class="page-header">
      <h2>频道管理</h2>
    </div>

    <div class="content-card">
      <div class="tabs">
        <button 
          :class="['tab-btn', { active: activeTab === 'chat' }]"
          @click="activeTab = 'chat'"
        >
          聊天管理
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'community' }]"
          @click="activeTab = 'community'"
        >
          社区管理
        </button>
      </div>

      <!-- 聊天管理 -->
      <div v-if="activeTab === 'chat'" class="tab-content">
        <!-- 搜索筛选 -->
        <div class="search-filters">
          <div class="filter-item">
            <label>发送者:</label>
            <input type="text" v-model="chatFilters.sender" placeholder="搜索发送者" />
          </div>
          <div class="filter-item">
            <label>内容:</label>
            <input type="text" v-model="chatFilters.content" placeholder="搜索内容" />
          </div>
          <div class="filter-item">
            <label>接收方:</label>
            <input type="text" v-model="chatFilters.receiver" placeholder="搜索接收方" />
          </div>
          <button class="clear-btn" @click="clearChatFilters">清空</button>
        </div>
        
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>发送者</th>
                <th>内容</th>
                <th>类型</th>
                <th>接收方</th>
                <th>时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="msg in filteredChatMessages" :key="msg.id">
                <td>
                  <div class="user-info">
                    <span>{{ msg.senderName }}</span>
                  </div>
                </td>
                <td class="content-cell">
                  <div v-if="msg.msgType === 0">{{ msg.content }}</div>
                  <div v-else-if="msg.msgType === 1">
                    <img :src="msg.content" class="preview-img" />
                  </div>
                  <div v-else-if="msg.msgType === 2">
                    <video :src="msg.content" class="preview-video" controls></video>
                  </div>
                </td>
                <td>{{ getMsgType(msg.msgType) }}</td>
                <td>{{ getReceiverInfo(msg) }}</td>
                <td>{{ formatDate(msg.createdTime) }}</td>
                <td>
                  <button @click="deleteChatMessage(msg.id)" class="btn-danger">删除</button>
                  <button @click="openMuteModal(msg.senderId, msg.senderName)" class="btn-warning">禁言</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 社区管理 -->
      <div v-if="activeTab === 'community'" class="tab-content">
        <!-- 搜索筛选 -->
        <div class="search-filters">
          <div class="filter-item">
            <label>发布者:</label>
            <input type="text" v-model="communityFilters.publisher" placeholder="搜索发布者" />
          </div>
          <div class="filter-item">
            <label>内容:</label>
            <input type="text" v-model="communityFilters.content" placeholder="搜索内容" />
          </div>
          <button class="clear-btn" @click="clearCommunityFilters">清空</button>
        </div>
        
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>发布者</th>
                <th>内容</th>
                <th>媒体</th>
                <th>可见性</th>
                <th>时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="post in filteredSocialPosts" :key="post.id">
                <td>
                  <div class="user-info">
                    <span>{{ post.userName }}</span>
                  </div>
                </td>
                <td class="content-cell">{{ post.content }}</td>
                <td>
                  <div class="media-preview" v-if="getMediaFiles(post.mediaUrls).length > 0">
                    <div v-for="(fileName, index) in getMediaFiles(post.mediaUrls)" :key="index" class="media-item">
                      <div v-if="getFileType(fileName) === 'image'" class="media-image">
                        <img :src="getFileDownloadUrl(fileName)" :alt="fileName" class="preview-img" />
                      </div>
                      <div v-else-if="getFileType(fileName) === 'video'" class="media-video">
                        <video :src="getFileDownloadUrl(fileName)" class="preview-video" controls preload="metadata"></video>
                      </div>
                      <div v-else class="media-other">
                        <a :href="getFileDownloadUrl(fileName)" target="_blank" class="file-link">
                          {{ fileName }}
                        </a>
                      </div>
                    </div>
                  </div>
                  <span v-else>无</span>
                </td>
                <td>{{ post.visibility === 0 ? '公开' : '仅好友' }}</td>
                <td>{{ formatDate(post.createdTime) }}</td>
                <td>
                  <button @click="deleteSocialPost(post.id)" class="btn-danger">删除</button>
                  <button @click="openBanModal(post.userId, post.userName)" class="btn-warning">禁发动态</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 禁言/禁发动态弹窗 -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal">
        <h3>{{ modalType === 'mute' ? '禁言用户' : '禁止发布动态' }} - {{ selectedUserName }}</h3>
        <div class="form-group">
          <label>时长 (分钟):</label>
          <input type="number" v-model="duration" min="1" />
        </div>
        <div class="modal-actions">
          <button @click="closeModal" class="btn-secondary">取消</button>
          <button @click="confirmAction" class="btn-primary">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import axios from 'axios';

const activeTab = ref('chat');
const chatMessages = ref([]);
const socialPosts = ref([]);
const showModal = ref(false);
const modalType = ref(''); // 'mute' or 'ban'
const selectedUserId = ref(null);
const selectedUserName = ref('');
const duration = ref(60);

// 搜索筛选状态
const chatFilters = ref({
  sender: '',
  content: '',
  receiver: ''
});

const communityFilters = ref({
  publisher: '',
  content: ''
});

// 筛选后的聊天消息
const filteredChatMessages = computed(() => {
  return chatMessages.value.filter(msg => {
    const matchSender = !chatFilters.value.sender || 
      (msg.senderName && msg.senderName.toLowerCase().includes(chatFilters.value.sender.toLowerCase()));
    const matchContent = !chatFilters.value.content || 
      (msg.content && msg.content.toLowerCase().includes(chatFilters.value.content.toLowerCase()));
    const matchReceiver = !chatFilters.value.receiver || 
      (getReceiverInfo(msg).toLowerCase().includes(chatFilters.value.receiver.toLowerCase()));
    return matchSender && matchContent && matchReceiver;
  });
});

// 筛选后的社区动态
const filteredSocialPosts = computed(() => {
  return socialPosts.value.filter(post => {
    const matchPublisher = !communityFilters.value.publisher || 
      (post.userName && post.userName.toLowerCase().includes(communityFilters.value.publisher.toLowerCase()));
    const matchContent = !communityFilters.value.content || 
      (post.content && post.content.toLowerCase().includes(communityFilters.value.content.toLowerCase()));
    return matchPublisher && matchContent;
  });
});

// 清空聊天筛选
const clearChatFilters = () => {
  chatFilters.value = { sender: '', content: '', receiver: '' };
};

// 清空社区筛选
const clearCommunityFilters = () => {
  communityFilters.value = { publisher: '', content: '' };
};

const API_HOST = "https://api.homesee.xyz";
const FILE_API_HOST = "https://files.homesee.xyz";

const fetchChatMessages = async () => {
  try {
    const res = await axios.get(`${API_HOST}/api/admin/channel/chat/messages`);
    chatMessages.value = res.data;
  } catch (error) {
    console.error('Failed to fetch chat messages:', error);
  }
};

const fetchSocialPosts = async () => {
  try {
    const res = await axios.get(`${API_HOST}/api/admin/channel/community/posts`);
    socialPosts.value = res.data;
  } catch (error) {
    console.error('Failed to fetch social posts:', error);
  }
};

const deleteChatMessage = async (id) => {
  if (!confirm('确定要删除这条消息吗？')) return;
  try {
    await axios.delete(`${API_HOST}/api/admin/channel/chat/message/${id}`);
    fetchChatMessages();
  } catch (error) {
    alert('删除失败');
  }
};

const deleteSocialPost = async (id) => {
  if (!confirm('确定要删除这条动态吗？')) return;
  try {
    await axios.delete(`${API_HOST}/api/admin/channel/community/post/${id}`);
    fetchSocialPosts();
  } catch (error) {
    alert('删除失败');
  }
};

const openMuteModal = (userId, userName) => {
  selectedUserId.value = userId;
  selectedUserName.value = userName;
  modalType.value = 'mute';
  showModal.value = true;
};

const openBanModal = (userId, userName) => {
  selectedUserId.value = userId;
  selectedUserName.value = userName;
  modalType.value = 'ban';
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  duration.value = 60;
};

const confirmAction = async () => {
  try {
    const endpoint = modalType.value === 'mute' 
      ? `${API_HOST}/api/admin/channel/chat/mute`
      : `${API_HOST}/api/admin/channel/community/ban`;
    
    await axios.post(endpoint, {
      userId: selectedUserId.value,
      duration: duration.value
    });
    
    alert('操作成功');
    closeModal();
  } catch (error) {
    alert('操作失败');
  }
};

const getMsgType = (type) => {
  const types = { 0: '文本', 1: '图片', 2: '视频' };
  return types[type] || '未知';
};

const getReceiverInfo = (msg) => {
  if (msg.groupId && msg.groupId !== -1) {
    return msg.groupId === 1 ? '幸福家园大家庭' : (msg.groupId === 2 ? '租客交流群' : `群组 ${msg.groupId}`);
  }
  return `用户 ${msg.receiverId}`;
};

const getMediaFiles = (mediaUrls) => {
  if (!mediaUrls || mediaUrls === 'null' || mediaUrls === '[]') {
    return [];
  }
  
  try {
    // 尝试解析JSON数组
    const parsed = JSON.parse(mediaUrls);
    if (Array.isArray(parsed)) {
      return parsed;
    }
  } catch (error) {
    // 如果不是JSON，则当作单个文件名处理
    if (typeof mediaUrls === 'string' && mediaUrls.trim() !== '') {
      return [mediaUrls.trim()];
    }
  }
  
  return [];
};

const getFileDownloadUrl = (fileName) => {
  return `${FILE_API_HOST}/api/files/download/${encodeURIComponent(fileName)}`;
};

const getFileType = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase();
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'];
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'];
  
  if (imageExts.includes(ext)) return 'image';
  if (videoExts.includes(ext)) return 'video';
  return 'other';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString();
};

watch(activeTab, (newTab) => {
  if (newTab === 'chat') fetchChatMessages();
  else fetchSocialPosts();
});

onMounted(() => {
  fetchChatMessages();
});
</script>

<style scoped>
.channel-management {
  padding: 0;
  background: #fff;
  min-height: 100vh;
}

.page-header h2 {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ddd;
}

.content-card {
  background: #fff;
  border: 1px solid #ddd;
  padding: 16px;
}

/* 搜索筛选 */
.search-filters {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f9f9f9;
  border: 1px solid #ddd;
  flex-wrap: wrap;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-item label {
  font-size: 12px;
  color: #888;
  white-space: nowrap;
}

.filter-item input {
  padding: 4px 8px;
  border: 1px solid #ddd;
  font-size: 12px;
  min-width: 120px;
}

.filter-item input:focus {
  outline: none;
  border-color: #3A6EA5;
}

.clear-btn {
  padding: 4px 12px;
  border: 1px solid #ddd;
  background: transparent;
  color: #888;
  cursor: pointer;
  font-size: 12px;
}

.clear-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.tabs {
  display: flex;
  gap: 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #ddd;
  padding-bottom: 0;
}

.tab-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-bottom: none;
  background: transparent;
  font-size: 12px;
  color: #888;
  cursor: pointer;
  margin-bottom: -1px;
}

.tab-btn.active {
  background: #1e3a5f;
  color: #fff;
  border-color: #1e3a5f;
}

.tab-btn:hover:not(.active) {
  background: #f5f5f5;
  color: #333;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 8px 12px;
  text-align: left;
  border: 1px solid #ddd;
  font-size: 12px;
}

th {
  font-weight: 500;
  color: #333;
  background: #f5f5f5;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.avatar {
  width: 24px;
  height: 24px;
  object-fit: cover;
}

.content-cell {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preview-img, .preview-video {
  max-width: 80px;
  max-height: 80px;
}

.media-preview {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.media-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.media-image img,
.media-video video {
  max-width: 60px;
  max-height: 60px;
  object-fit: cover;
}

.media-other .file-link {
  color: #3A6EA5;
  text-decoration: none;
  font-size: 11px;
  padding: 2px 6px;
  border: 1px solid #ddd;
  background: transparent;
}

.media-other .file-link:hover {
  background: #f5f5f5;
}

.btn-danger {
  background: transparent;
  color: #c00;
  border: 1px solid #c00;
  padding: 2px 8px;
  cursor: pointer;
  margin-right: 4px;
  font-size: 11px;
}

.btn-danger:hover {
  background: #c00;
  color: #fff;
}

.btn-warning {
  background: transparent;
  color: #c07700;
  border: 1px solid #c07700;
  padding: 2px 8px;
  cursor: pointer;
  font-size: 11px;
}

.btn-warning:hover {
  background: #c07700;
  color: #fff;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  padding: 16px;
  border: 1px solid #ddd;
  width: 360px;
}

.modal h3 {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #ddd;
}

.form-group {
  margin: 12px 0;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  color: #888;
  font-size: 12px;
}

.form-group input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #ddd;
  font-size: 12px;
}

.form-group input:focus {
  outline: none;
  border-color: #3A6EA5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ddd;
}

.btn-secondary {
  background: transparent;
  color: #888;
  border: 1px solid #ddd;
  padding: 4px 12px;
  cursor: pointer;
  font-size: 12px;
}

.btn-secondary:hover {
  background: #f5f5f5;
  color: #333;
}

.btn-primary {
  background: #1e3a5f;
  color: #fff;
  border: 1px solid #1e3a5f;
  padding: 4px 12px;
  cursor: pointer;
  font-size: 12px;
}

.btn-primary:hover {
  background: #152a45;
}
</style>
