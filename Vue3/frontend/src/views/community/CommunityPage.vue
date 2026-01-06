<!--
  项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
  文件名称：CommunityPage.vue
  开发者：牛迦楠
  专业：软件工程（中外合作办学）
  学校：东华理工大学
  功能描述：社区动态页面组件，展示所有公开的社区动态，支持点赞、评论和分享
  创建日期：2026-01-06
-->
<template>
  <div class="community-page-container">
    <!-- 头部 -->
    <div class="community-header">
      <h1>社区动态</h1>
      <div class="filter-controls">
        <select v-model="feedFilter" class="filter-select">
          <option value="all">全部动态</option>
          <option value="popular">热门动态</option>
        </select>
      </div>
    </div>

    <!-- 动态列表 -->
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
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" :fill="post.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
            <span>{{ post.likeCount > 0 ? post.likeCount : '点赞' }}</span>
          </button>
          <button class="footer-btn comment-btn" @click="toggleComments(post)">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>
            <span>{{ post.commentCount > 0 ? post.commentCount : '评论' }}</span>
          </button>
          <button class="footer-btn share-btn" @click="sharePost(post)">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>
            <span>分享</span>
          </button>
        </div>

        <!-- 评论区域 -->
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

// 过滤后的动态计算属性 (社区页面显示所有公开动态)
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
    const res = await fetch(`https://api.homesee.xyz/api/community/friends/${currentUserId}`)
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
    const res = await fetch(`https://api.homesee.xyz/api/community/posts/with-user-info?userId=${currentUserId}`)
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

// --- 交互逻辑 ---

const toggleLike = async (post) => {
  try {
    const res = await fetch(`https://api.homesee.xyz/api/community/posts/${post.id}/like`, {
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
    // 如果打开且为空则加载评论
    await loadComments(post)
  }
}

const loadComments = async (post) => {
  try {
    const res = await fetch(`https://api.homesee.xyz/api/community/posts/${post.id}/comments`)
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
    const res = await fetch(`https://api.homesee.xyz/api/community/posts/${post.id}/comment`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        userId: currentUserId,
        content: post.newComment 
      })
    })
    const data = await res.json()
    if (data.success) {
      // 将新评论添加到列表
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
  // 简单的复制链接模拟
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
  
  if (avatarName.startsWith('http')) {
    return avatarName
  }
  
  const FILE_SERVER_HOST = 'https://files.homesee.xyz'
  return `${FILE_SERVER_HOST}/api/files/download/${avatarName}`
}

const buildFileUrl = (filename) => {
  if (!filename) return ''
  
  if (filename.startsWith('http')) {
    return filename
  }
  
  const FILE_SERVER_HOST = 'https://files.homesee.xyz'
  return `${FILE_SERVER_HOST}/api/files/download/${filename}`
}

onMounted(() => {
  fetchFriends()
  fetchPosts()
})
</script>

<style scoped>
.community-page-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
  font-family: 'Inter', sans-serif;
  color: #111827;
  min-height: 100vh;
  background-color: #FFFFFF;
}

.community-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 24px;
  border-bottom: 1px solid #111827;
}

.community-header h1 {
  font-size: 1.5rem;
  font-weight: 800;
  color: #111827;
  margin: 0;
  text-transform: uppercase;
}

.filter-select {
  padding: 8px 32px 8px 16px;
  border: 1px solid #E5E7EB;
  border-radius: 0;
  background: white;
  color: #111827;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 600;
  text-transform: uppercase;
}

.filter-select:hover {
  border-color: #111827;
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.post-card {
  background: white;
  border-radius: 0;
  padding: 24px;
  box-shadow: none;
  border: 1px solid #E5E7EB;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.post-header .avatar-container {
  margin-right: 16px;
  flex-shrink: 0;
}

.post-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%; /* Circular */
  object-fit: cover;
  background: #F3F4F6;
  border: 1px solid #F3F4F6;
}

.visibility-badge {
  padding: 2px 8px;
  border-radius: 0;
  font-size: 0.7rem;
  font-weight: 600;
  margin-left: 12px;
  text-transform: uppercase;
  border: 1px solid;
}

.visibility-badge.public {
  background: white;
  color: #111827;
  border-color: #111827;
}

.visibility-badge.friends-only {
  background: white;
  color: #D97706;
  border-color: #D97706;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 700;
  color: #111827;
  font-size: 1rem;
}

.time {
  font-size: 0.75rem;
  color: #9CA3AF;
  font-family: 'JetBrains Mono', monospace;
  margin-top: 2px;
}

.post-content {
  margin-bottom: 24px;
  font-size: 1rem;
  line-height: 1.6;
  color: #1f2937;
}

.post-content p {
  margin: 0 0 16px 0;
}

.post-media img, .post-media video {
  max-width: 100%;
  max-height: 400px;
  border-radius: 0;
  margin-top: 12px;
  object-fit: contain;
  border: 1px solid #E5E7EB;
}

.post-media audio {
  width: 100%;
  max-width: 400px;
  margin-top: 12px;
  border-radius: 0;
}

.media-item {
  margin-bottom: 12px;
}

.media-item:last-child {
  margin-bottom: 0;
}

.post-footer {
  display: flex;
  justify-content: flex-start;
  gap: 24px;
  border-top: 1px solid #E5E7EB;
  padding-top: 16px;
}

.footer-btn {
  background: none;
  border: none;
  color: #6B7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 0;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.2s;
  text-transform: uppercase;
}

.footer-btn:hover {
  background: #F3F4F6;
  color: #111827;
}

.like-btn.active { color: #EF4444; }
.like-btn.active:hover { background: #fee2e2; }

/* 评论样式 */
.comments-section {
  background: #F9FAFB;
  padding: 16px;
  margin-top: 16px;
  border: 1px solid #E5E7EB;
  border-radius: 0;
}

.comment-list {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  font-size: 0.9rem;
  line-height: 1.5;
}

.comment-user {
  font-weight: 700;
  color: #111827;
  margin-right: 8px;
}

.comment-content {
  color: #374151;
}

.comment-input-area {
  display: flex;
  gap: 0; /* Connected input */
}

.comment-input-area input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #111827;
  border-right: none;
  border-radius: 0;
  font-size: 0.9rem;
  outline: none;
  background: white;
}

.comment-input-area button {
  background: #111827;
  color: white;
  border: 1px solid #111827;
  padding: 0 24px;
  border-radius: 0;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  text-transform: uppercase;
}

.comment-input-area button:hover:not(:disabled) {
  background: black;
}

.comment-input-area button:disabled {
  background: #9CA3AF;
  border-color: #9CA3AF;
  cursor: not-allowed;
}
</style>
