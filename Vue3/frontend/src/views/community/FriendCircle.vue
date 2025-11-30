<template>
  <div class="friend-circle-container">
    <!-- Header -->
    <div class="friend-circle-header">
      <h1>朋友圈</h1>
      <div class="filter-controls">
        <select v-model="feedFilter" class="filter-select">
          <option value="all">全部动态</option>
          <option value="friends">只看朋友</option>
        </select>
      </div>
    </div>

    <!-- Create Post -->
    <div class="create-post-card">
      <div class="input-wrapper">
        <div class="avatar">{{ userState.user?.username?.[0] || 'U' }}</div>
        <textarea 
          v-model="newPostContent" 
          placeholder="分享你的生活..." 
          :disabled="!hasActiveLease"
          @focus="checkLease"
        ></textarea>
      </div>
      
      <div class="media-preview" v-if="mediaPreview">
        <div class="preview-container">
          <img v-if="mediaPreview.type === 'image'" :src="mediaPreview.url" alt="预览图片" />
          <video v-else-if="mediaPreview.type === 'video'" :src="mediaPreview.url" controls></video>
          <audio v-else-if="mediaPreview.type === 'audio'" :src="mediaPreview.url" controls></audio>
          <button class="remove-media" @click="removeMedia">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
      </div>

      <div class="post-actions">
        <div class="media-buttons">
          <input type="file" ref="imageInput" accept="image/*" @change="handleFileUpload($event, 'image')" style="display: none">
          <input type="file" ref="videoInput" accept="video/*" @change="handleFileUpload($event, 'video')" style="display: none">
          <input type="file" ref="audioInput" accept="audio/*" @change="handleFileUpload($event, 'audio')" style="display: none">
          
          <button class="action-btn" @click="triggerUpload('imageInput')" :disabled="!hasActiveLease">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
            <span>图片</span>
          </button>
          <button class="action-btn" @click="triggerUpload('videoInput')" :disabled="!hasActiveLease">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"></polygon><rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect></svg>
            <span>视频</span>
          </button>
          <button class="action-btn" @click="triggerUpload('audioInput')" :disabled="!hasActiveLease">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18V5l12-2v13"></path><circle cx="6" cy="18" r="3"></circle><circle cx="18" cy="16" r="3"></circle></svg>
            <span>音乐</span>
          </button>
        </div>
        <div class="post-settings">
          <select v-model="visibility" class="visibility-select" :disabled="!hasActiveLease">
            <option value="0">公开</option>
            <option value="1">仅好友</option>
          </select>
          <button class="submit-btn" @click="submitPost" :disabled="(!newPostContent && !mediaPreview) || !hasActiveLease || isSubmitting">
            {{ isSubmitting ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
      <div v-if="!hasActiveLease" class="lease-warning">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
        仅限住户发布动态
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
          
          <!-- More Menu for Own Posts -->
          <div class="more-menu-container" v-if="post.userId === currentUserId">
            <button class="more-btn" @click.stop="toggleMenu(post.id)">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"></circle><circle cx="19" cy="12" r="1"></circle><circle cx="5" cy="12" r="1"></circle></svg>
            </button>
            
            <div class="dropdown-menu" v-if="activeMenuId === post.id">
              <button @click="changeVisibility(post)">
                <svg v-if="post.visibility === 1" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                设为{{ post.visibility === 1 ? '公开' : '仅好友' }}
              </button>
              <button class="delete-btn" @click="deletePost(post.id)">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                删除
              </button>
            </div>
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
const newPostContent = ref('')
const currentUserId = userState.user ? userState.user.id : 0
const mediaPreview = ref(null)
const hasActiveLease = ref(false)
const visibility = ref('0') // 0-公开, 1-仅好友
const feedFilter = ref('all') // all, friends

// Refs for file inputs
const imageInput = ref(null)
const videoInput = ref(null)
const audioInput = ref(null)

const isSubmitting = ref(false)
const MAX_FILE_SIZE = 100 * 1024 * 1024 // 100MB

const friends = ref([])

// Computed properties for filtered posts (朋友圈只显示自己和朋友的动态)
const filteredPosts = computed(() => {
  // Now friend.friendId is guaranteed to be the friend's ID
  const friendIds = friends.value.map(f => f.friendId)
  
  return posts.value.filter(post => {
    const isSelf = post.userId === currentUserId
    const isFriend = friendIds.includes(post.userId)
    
    // 核心规则：只显示自己和朋友的动态
    if (!isSelf && !isFriend) return false
    
    // 过滤器逻辑
    if (feedFilter.value === 'friends') {
      return isFriend
    }
    
    return true
  })
})

const fetchFriends = async () => {
  if (!currentUserId) return
  try {
    const res = await fetch(`http://39.108.142.250:8080/api/community/friends/${currentUserId}`)
    const data = await res.json()
    if (data.success) {
      friends.value = data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const checkLease = async () => {
  if (!currentUserId) return
  try {
    const res = await fetch(`http://39.108.142.250:8080/api/admin/tenant/tenant/${currentUserId}`)
    const data = await res.json()
    if (data.success && data.contracts && data.contracts.length > 0) {
      const active = data.contracts.find(c => c.contractStatus === 1 || c.contractStatus === 2)
      hasActiveLease.value = !!active
    } else {
      hasActiveLease.value = false
    }
  } catch (e) {
    console.error(e)
    hasActiveLease.value = false
  }
}

const fetchPosts = async () => {
  try {
    const res = await fetch(`http://39.108.142.250:8080/api/community/posts/with-user-info?userId=${currentUserId}`)
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

const triggerUpload = (refName) => {
  if (refName === 'imageInput') imageInput.value.click()
  if (refName === 'videoInput') videoInput.value.click()
  if (refName === 'audioInput') audioInput.value.click()
}

const handleFileUpload = async (event, type) => {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > MAX_FILE_SIZE) {
    alert(`文件大小不能超过 ${MAX_FILE_SIZE / 1024 / 1024}MB`)
    return
  }

  // Create preview
  const url = URL.createObjectURL(file)
  mediaPreview.value = { type, url, file }
}

const removeMedia = () => {
  mediaPreview.value = null
}

const uploadMediaFile = async (file) => {
  const formData = new FormData()
  formData.append('file', file)

  try {
    // 直接上传到文件服务器
    const FILE_SERVER_HOST = 'http://39.108.142.250:8088'
    const res = await fetch(`${FILE_SERVER_HOST}/api/files/upload`, {
      method: 'POST',
      body: formData
    })

    if (!res.ok) {
      if (res.status === 413) {
        throw new Error('文件过大，服务器拒绝接收')
      }
      throw new Error(`上传失败 (${res.status})`)
    }

    const data = await res.json()
    
    if (data.success) {
      // 文件服务器返回的是 fileName (驼峰)
      return data.fileName || file.name
    }
    throw new Error(data.message || '上传失败')
  } catch (e) {
    console.error('Upload failed', e)
    alert('上传失败: ' + e.message)
    throw e // Re-throw to stop submission
  }
}

const submitPost = async () => {
  if ((!newPostContent.value.trim() && !mediaPreview.value) || !hasActiveLease.value) return
  if (isSubmitting.value) return

  isSubmitting.value = true
  try {
    let mediaUrl = ''
    if (mediaPreview.value) {
      mediaUrl = await uploadMediaFile(mediaPreview.value.file)
    }

    const payload = {
      userId: currentUserId,
      content: newPostContent.value,
      mediaUrls: mediaUrl,
      visibility: parseInt(visibility.value)
    }

    const res = await fetch('http://39.108.142.250:8080/api/community/posts/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    
    if (!res.ok) {
      throw new Error(`发布失败 (${res.status})`)
    }

    const data = await res.json()
    if (data.success) {
      posts.value.unshift(data.data)
      newPostContent.value = ''
      mediaPreview.value = null
      alert('发布成功')
    } else {
      throw new Error(data.message || '发布失败')
    }
  } catch (e) {
    console.error(e)
    alert('发布失败: ' + e.message)
  } finally {
    isSubmitting.value = false
  }
}

const activeMenuId = ref(null)

const toggleMenu = (postId) => {
  activeMenuId.value = activeMenuId.value === postId ? null : postId
}

// Close menu when clicking outside
const closeMenu = () => {
  activeMenuId.value = null
}

const deletePost = async (postId) => {
  if (!confirm('确定要删除这条动态吗？')) return
  
  try {
    const res = await fetch(`http://39.108.142.250:8080/api/community/posts/${postId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: currentUserId })
    })
    
    const data = await res.json()
    if (data.success) {
      posts.value = posts.value.filter(p => p.id !== postId)
      activeMenuId.value = null
    } else {
      alert(data.message || '删除失败')
    }
  } catch (e) {
    console.error(e)
    alert('删除失败')
  }
}

const changeVisibility = async (post) => {
  const newVisibility = post.visibility === 1 ? 0 : 1
  try {
    const res = await fetch(`http://39.108.142.250:8080/api/community/posts/${post.id}/visibility`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        userId: currentUserId,
        visibility: newVisibility 
      })
    })
    
    const data = await res.json()
    if (data.success) {
      post.visibility = newVisibility
      activeMenuId.value = null
    } else {
      alert(data.message || '修改失败')
    }
  } catch (e) {
    console.error(e)
    alert('修改失败')
  }
}

// --- Interaction Logic ---

const toggleLike = async (post) => {
  try {
    const res = await fetch(`http://39.108.142.250:8080/api/community/posts/${post.id}/like`, {
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
    const res = await fetch(`http://39.108.142.250:8080/api/community/posts/${post.id}/comments`)
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
    const res = await fetch(`http://39.108.142.250:8080/api/community/posts/${post.id}/comment`, {
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

// Add click outside listener
onMounted(() => {
  document.addEventListener('click', closeMenu)
})

import { onUnmounted } from 'vue'
onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
})

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

onMounted(() => {
  checkLease()
  fetchFriends()
  fetchPosts()
})
</script>

<style scoped>
.friend-circle-container {
  max-width: 680px;
  margin: 0 auto;
  padding: 16px;
  font-family: 'Inter', sans-serif;
  color: #1f2937;
  min-height: 100vh;
  overflow-y: auto;
}

.friend-circle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.friend-circle-header h1 {
  font-size: 20px;
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

.create-post-card {
  background: white;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 16px;
  border: 1px solid #f3f4f6;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
  font-size: 14px;
}

.create-post-card textarea {
  flex: 1;
  height: 60px;
  border: none;
  resize: none;
  outline: none;
  font-family: inherit;
  font-size: 14px;
  padding: 8px 0;
  background: transparent;
}

.create-post-card textarea::placeholder {
  color: #9ca3af;
}

.media-preview {
  margin-bottom: 15px;
  padding-left: 55px;
}

.preview-container {
  position: relative;
  display: inline-block;
}

.preview-container img, .preview-container video {
  max-width: 100%;
  max-height: 400px;
  width: auto;
  height: auto;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  object-fit: contain;
}

.remove-media {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.remove-media:hover {
  background: rgba(0, 0, 0, 0.8);
}

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f3f4f6;
  padding-left: 55px;
}

.post-settings {
  display: flex;
  align-items: center;
  gap: 12px;
}

.visibility-select {
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.visibility-select:hover:not(:disabled) {
  border-color: #6366f1;
}

.visibility-select:disabled {
  background: #f9fafb;
  color: #9ca3af;
  cursor: not-allowed;
}

.media-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: transparent;
  border: none;
  color: #6b7280;
  padding: 6px 12px;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.action-btn:hover:not(:disabled) {
  background: #f3f4f6;
  color: #6366f1;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn {
  background: #6366f1;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #4f46e5;
}

.submit-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
}

.lease-warning {
  margin-top: 10px;
  padding-left: 55px;
  font-size: 12px;
  color: #ef4444;
  display: flex;
  align-items: center;
  gap: 4px;
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
  margin-bottom: 12px;
}

.avatar-container {
  margin-right: 12px;
}

.post-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}

.visibility-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: auto;
  font-weight: 500;
}

.visibility-badge.public {
  background: #eff6ff;
  color: #3b82f6;
  border: 1px solid #bfdbfe;
}

.visibility-badge.friends-only {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
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

.more-btn {
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
}

.more-btn:hover {
  background: #f3f4f6;
  color: #4b5563;
}

.post-content {
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.5;
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
  padding-top: 10px;
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
.share-btn:hover { color: #10b981; background: #ecfdf5; }

.more-menu-container {
  position: relative;
  margin-left: 8px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  padding: 6px;
  min-width: 140px;
  z-index: 20;
  border: 1px solid #f3f4f6;
  transform-origin: top right;
  animation: scaleIn 0.1s ease-out;
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.dropdown-menu button {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  font-size: 14px;
  color: #4b5563;
  border-radius: 8px;
  transition: all 0.2s;
  font-weight: 500;
}

.dropdown-menu button:hover {
  background: #f3f4f6;
  color: #111827;
}

.dropdown-menu .delete-btn {
  color: #ef4444;
}

.dropdown-menu .delete-btn:hover {
  background: #fef2f2;
  color: #dc2626;
}

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
