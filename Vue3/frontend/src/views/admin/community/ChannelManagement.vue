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
              <tr v-for="msg in chatMessages" :key="msg.id">
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
              <tr v-for="post in socialPosts" :key="post.id">
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
import { ref, onMounted, watch } from 'vue';
import axios from 'axios';

const activeTab = ref('chat');
const chatMessages = ref([]);
const socialPosts = ref([]);
const showModal = ref(false);
const modalType = ref(''); // 'mute' or 'ban'
const selectedUserId = ref(null);
const selectedUserName = ref('');
const duration = ref(60);

const API_HOST = "http://localhost:8080";
const FILE_API_HOST = "http://39.108.142.250:8088";

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
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.page-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 24px;
}

.content-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 12px;
}

.tab-btn {
  padding: 8px 16px;
  border: none;
  background: transparent;
  font-size: 1rem;
  color: #64748b;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s;
}

.tab-btn.active {
  background: #3b82f6;
  color: white;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

th {
  font-weight: 600;
  color: #64748b;
  background: #f8fafc;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.content-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preview-img, .preview-video {
  max-width: 100px;
  max-height: 100px;
  border-radius: 4px;
}

.media-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.media-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.media-image img,
.media-video video {
  max-width: 80px;
  max-height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.media-other .file-link {
  color: #3b82f6;
  text-decoration: none;
  font-size: 0.875rem;
  padding: 4px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: #f8fafc;
}

.media-other .file-link:hover {
  background: #e2e8f0;
}

.btn-danger {
  background: #ef4444;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 8px;
}

.btn-warning {
  background: #f59e0b;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
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
  background: white;
  padding: 24px;
  border-radius: 12px;
  width: 400px;
}

.form-group {
  margin: 20px 0;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #64748b;
}

.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-secondary {
  background: #e2e8f0;
  color: #64748b;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}
</style>
