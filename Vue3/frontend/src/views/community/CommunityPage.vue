<template>
  <div class="community-page-container">
    <!-- Header -->
    <div class="community-header">
      <h1>社区动态</h1>
      <div class="filter-controls">
        <select v-model="feedFilter" class="filter-select">
          <option value="all">全部动态</option>
          <option value="popular">热门动态</option>
        </select>
      </div>
    </div>

    <!-- Feed List -->
    <div class="feed-list">
      <div v-for="post in filteredPosts" :key="post.id" class="post-card">
        <div class="post-header">
          <div class="avatar-container">
            <img :src="getAvatarUrl(post.avatar)" alt="头像" class="post-avatar" />
          </div>
          <div class="user-info">
            <span class="username">{{ post.username || `用户 ${post.userId}` }}</span>
            <span class="time">{{ formatDate(post.createdTime) }}</span>
          </div>
          <div class="visibility-badge" :class="post.visibility === 1 ? 'friends-only' : 'public'">
            {{ post.visibility === 1 ? '仅好友' : '公开' }}
          </div>
        </div>
        
        <div class="post-content">
          <p>{{ post.content }}</p>
          <div v-if="post.mediaUrls" class="post-media">
            <template v-if="Array.isArray(getMediaUrls(post.mediaUrls))">
              <div v-for="(mediaUrl, index) in getMediaUrls(post.mediaUrls)" :key="index" class="media-item">
                <img v-if="isImage(mediaUrl)" :src="buildFileUrl(mediaUrl)" :alt="`Post media ${index + 1}`" />
                <video v-else-if="isVideo(mediaUrl)" :src="buildFileUrl(mediaUrl)" controls></video>
                <audio v-else-if="isAudio(mediaUrl)" :src="buildFileUrl(mediaUrl)" controls></audio>
              </div>
            </template>
            <template v-else>
              <img v-if="isImage(post.mediaUrls)" :src="buildFileUrl(post.mediaUrls)" alt="Post media" />
              <video v-else-if="isVideo(post.mediaUrls)" :src="buildFileUrl(post.mediaUrls)" controls></video>
              <audio v-else-if="isAudio(post.mediaUrls)" :src="buildFileUrl(post.mediaUrls)" controls></audio>
            </template>
          </div>
        </div>
        
        <div class="post-footer">
          <button class="footer-btn like-btn" :class="{ 'active': post.liked }" @click="toggleLike(post)">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" :fill="post.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
            <span>{{ post.likeCount > 0 ? post.likeCount : '赞' }}</span>
          </button>
          <button class="footer-btn comment-btn" @click="toggleComments(post)">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>
            <span>{{ post.commentCount > 0 ? post.commentCount : '评论' }}</span>
          </button>
          <button class="footer-btn share-btn" @click="sharePost(post)">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>
            <span>分享</span>
          </button>
        </div>

        <!-- Comments Section -->
        <div class="comments-section" v-if="post.showComments">
          <div class="comment-list" v-if="post.comments && post.comments.length > 0">
            <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
              <span class="comment-user">{{ getFriendName(comment.userId) }}:</span>
              <span class="comment-content">{{ comment.content }}</span>
            </div>
          </div>
          <div class="comment-input-area">
            <input 
              type="text" 
              v-model="post.newComment" 
              placeholder="写下你的评论..." 
              @keyup.enter="submitComment(post)"
            >
            <button @click="submitComment(post)" :disabled="!post.newComment">发送</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

import { userState } from '../../state/user'

const posts = ref([])
const feedFilter = ref('all') // all, popular
const currentUserId = userState.user ? userState.user.id : 0
const friends = ref([])

// Computed properties for filtered posts (社区页面显示所有公开动态)
const filteredPosts = computed(() => {
  let filtered = posts.value.filter(post => {
    // 社区页面只显示公开动态
    return post.visibility === 0
  })
  
  // 如果选择热门动态，可以按点赞数排序
  if (feedFilter.value === 'popular') {
    return filtered.sort((a, b) => b.likeCount - a.likeCount)
  }
  
  return filtered
})

const fetchFriends = async () => {
  if (!currentUserId) return
  try {
    const res = await fetch(`http://localhost:8080/api/community/friends/${currentUserId}`)
    const data = await res.json()
    if (data.success) {
      friends.value = data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchPosts = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/community/posts/with-user-info?userId=${currentUserId}`)
    const data = await res.json()
    if (data.success) {
      posts.value = data.data.map(post => ({
        ...post,
        showComments: false,
        newComment: '',
        comments: [] // Initialize comments array
      }))
    }
  } catch (e) {
    console.error(e)
  }
}

// --- Interaction Logic ---

const toggleLike = async (post) => {
  try {
    const res = await fetch(`http://localhost:8080/api/community/posts/${post.id}/like`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: currentUserId })
    })
    const data = await res.json()
    if (data.success) {
      post.liked = data.isLiked
      post.likeCount += data.isLiked ? 1 : -1
    }
  } catch (e) {
    console.error(e)
  }
}

const toggleComments = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && (!post.comments || post.comments.length === 0)) {
    // Load comments if opening and empty
    await loadComments(post)
  }
}

const loadComments = async (post) => {
  try {
    const res = await fetch(`http://localhost:8080/api/community/posts/${post.id}/comments`)
    const data = await res.json()
    if (data.success) {
      post.comments = data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) return
  
  try {
    const res = await fetch(`http://localhost:8080/api/community/posts/${post.id}/comment`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        userId: currentUserId,
        content: post.newComment 
      })
    })
    const data = await res.json()
    if (data.success) {
      // Add new comment to list
      if (!post.comments) post.comments = []
      post.comments.push(data.data)
      post.commentCount++
      post.newComment = ''
    }
  } catch (e) {
    console.error(e)
    alert('评论失败')
  }
}

const sharePost = (post) => {
  // Simple copy link simulation
  const link = `${window.location.origin}/#/community?post=${post.id}`
  navigator.clipboard.writeText(link).then(() => {
    alert('链接已复制到剪贴板')
  }).catch(() => {
    alert('复制失败')
  })
}

const getFriendName = (userId) => {
  if (userId === currentUserId) return '我'
  const friend = friends.value.find(f => f.friendId === userId)
  return friend ? (friend.realName || friend.username) : `用户${userId}`
}

const formatDate = (str) => {
  if (!str) return ''
  const date = new Date(str)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

const isImage = (url) => {
  return url && (url.match(/\.(jpeg|jpg|gif|png)$/i) != null || url.includes('unsplash'))
}

const isVideo = (url) => {
  return url && url.match(/\.(mp4|webm|ogg|mov)$/i) != null
}

const getMediaUrls = (mediaUrls) => {
  if (!mediaUrls) return []
  try {
    // Try to parse as JSON array
    const parsed = JSON.parse(mediaUrls)
    if (Array.isArray(parsed)) {
      return parsed
    }
  } catch (e) {
    // If not JSON, treat as single URL
    return mediaUrls ? [mediaUrls] : []
  }
  return []
}

const isAudio = (url) => {
  return url && url.match(/\.(mp3|wav|aac|flac|m4a)$/i) != null
}

const getAvatarUrl = (avatarName) => {
  if (!avatarName) {
    return '/models/image/default-avatar.png'
  }
  
  // 如果是完整的HTTP URL，直接使用
  if (avatarName.startsWith('http')) {
    return avatarName
  }
  
  // 使用文件服务器获取头像
  const FILE_SERVER_HOST = 'http://39.108.142.250:8088'
  return `${FILE_SERVER_HOST}/api/files/download/${avatarName}`
}

// 构建完整的文件URL
const buildFileUrl = (filename) => {
  if (!filename) return ''
  
  // 如果是完整的HTTP URL，直接使用
  if (filename.startsWith('http')) {
    return filename
  }
  
  // 使用文件服务器获取文件
  const FILE_SERVER_HOST = 'http://39.108.142.250:8088'
  return `${FILE_SERVER_HOST}/api/files/download/${filename}`
}

onMounted(() => {
  fetchFriends()
  fetchPosts()
})
</script>

<style scoped>
.community-page-container {
  max-width: 680px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Inter', sans-serif;
  color: #1f2937;
  min-height: 100vh;
  overflow-y: auto;
}

.community-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.community-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.filter-controls {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-select:hover {
  border-color: #6366f1;
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  border: 1px solid #f3f4f6;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.post-header .avatar-container {
  margin-right: 12px;
  flex-shrink: 0;
}

.post-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  object-fit: cover;
  background: #f3f4f6;
}

.visibility-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-left: 8px;
}

.visibility-badge.public {
  background: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

.visibility-badge.friends-only {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fcd34d;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 600;
  color: #111827;
}

.time {
  font-size: 12px;
  color: #9ca3af;
}

.post-content {
  margin-bottom: 15px;
  font-size: 15px;
  line-height: 1.6;
  color: #374151;
}

.post-content p {
  margin: 0 0 12px 0;
}

.post-media img, .post-media video {
  max-width: 100%;
  max-height: 400px;
  width: auto;
  height: auto;
  border-radius: 12px;
  margin-top: 8px;
  object-fit: contain;
}

.post-media audio {
  width: 100%;
  max-width: 400px;
  margin-top: 8px;
  border-radius: 8px;
}

.media-item {
  margin-bottom: 8px;
}

.media-item:last-child {
  margin-bottom: 0;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #f3f4f6;
  padding-top: 12px;
}

.footer-btn {
  flex: 1;
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.footer-btn:hover {
  background: #f3f4f6;
}

.like-btn.active { color: #ef4444; }
.like-btn:hover { color: #ef4444; background: #fef2f2; }
.comment-btn:hover { color: #3b82f6; background: #eff6ff; }
.share-btn:hover { color: #10b981; background: #ecfdf5; }

/* Comments Styles */
.comments-section {
  background: #f9fafb;
  padding: 12px;
  margin-top: 12px;
  border-radius: 8px;
}

.comment-list {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-item {
  font-size: 13px;
  line-height: 1.4;
}

.comment-user {
  font-weight: 600;
  color: #4b5563;
  margin-right: 6px;
}

.comment-content {
  color: #374151;
}

.comment-input-area {
  display: flex;
  gap: 8px;
}

.comment-input-area input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}

.comment-input-area input:focus {
  border-color: #6366f1;
}

.comment-input-area button {
  background: #6366f1;
  color: white;
  border: none;
  padding: 0 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.comment-input-area button:hover:not(:disabled) {
  background: #4f46e5;
}

.comment-input-area button:disabled {
  background: #e5e7eb;
  cursor: not-allowed;
}
</style>
