<template>
  <div class="chat-container">
    <Navbar />
    <div class="page-header-spacer"></div>
    <div class="chat-header">
      <div class="header-content">
        <h2>智能租房助手</h2>
        <p>基于 DeepSeek AI为您提供个性化租房建议</p>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']">
        <div class="message-avatar">
          <img v-if="msg.role === 'user'" :src="userAvatar" alt="User">
          <img v-else src="/models/IMG_20251208_160325.png" alt="AI" class="ai-avatar-img">
        </div>
        <div class="message-content">
          <div class="message-bubble">
            <div v-if="msg.content" v-html="msg.content" class="html-content"></div>
            <div v-if="msg.loading" class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
          <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <div class="input-wrapper">
        <textarea 
          v-model="userInput" 
          @keydown.enter.prevent="sendMessage"
          placeholder="请输入您的租房需求，例如：我想在市中心找一套两室一厅..."
          rows="1"
          ref="textarea"
        ></textarea>
        <button @click="sendMessage" :disabled="isLoading || !userInput.trim()" class="send-btn">
          <span v-if="!isLoading">发送</span>
          <span v-else>...</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const messages = ref([])
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const textarea = ref(null)
const currentUser = ref(null)

// API Configuration
const BACKEND_URL = 'https://api.homesee.xyz/api/smart-matching'

// User Avatar
const userAvatar = ref('/models/image/default-avatar.png')

const getAvatarUrl = (avatarName) => {
  if (!avatarName) {
    return '/models/image/default-avatar.png'
  }
  
  // 如果是完整的HTTP URL，直接使用
  if (avatarName.startsWith('http')) {
    return avatarName
  }
  
  // 使用文件服务器获取头像
  const FILE_SERVER_HOST = 'https://files.homesee.xyz'
  return `${FILE_SERVER_HOST}/api/files/download/${avatarName}`
}

onMounted(async () => {
  // Load user info
  const userData = localStorage.getItem('user')
  if (userData) {
    currentUser.value = JSON.parse(userData)
    if (currentUser.value.avatar) {
      userAvatar.value = getAvatarUrl(currentUser.value.avatar)
    }
    
    // Fetch history
    await fetchHistory()
  } else {
    // If not logged in, show greeting
    addMessage('assistant', '您好！我是您的智能租房助手。请先登录以获取更个性化的服务。')
  }
})

const fetchHistory = async () => {
  if (!currentUser.value) return
  try {
    const response = await fetch(`${BACKEND_URL}/history/${currentUser.value.id}`)
    if (response.ok) {
      const history = await response.json()
      messages.value = history.map(h => ({
        role: h.role,
        content: h.content,
        timestamp: new Date(h.createdTime).getTime(),
        loading: false
      }))
      
      if (messages.value.length === 0) {
        addMessage('assistant', `您好${currentUser.value.realName || currentUser.value.username}！我是您的智能租房助手。我已经了解了您的租房偏好，请告诉我您的具体需求，我会为您推荐合适的房源。`)
      } else {
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('Failed to fetch history:', error)
  }
}

// Auto-resize textarea
watch(userInput, () => {
  nextTick(() => {
    if (textarea.value) {
      textarea.value.style.height = 'auto'
      textarea.value.style.height = textarea.value.scrollHeight + 'px'
    }
  })
})

const goBack = () => {
  router.push('/')
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const addMessage = (role, content, loading = false) => {
  messages.value.push({
    role,
    content,
    timestamp: Date.now(),
    loading
  })
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return
  
  if (!currentUser.value) {
    alert('请先登录')
    router.push('/login')
    return
  }

  const content = userInput.value.trim()
  userInput.value = ''
  if (textarea.value) textarea.value.style.height = 'auto'

  // Add user message locally immediately
  addMessage('user', content)

  // Add loading placeholder
  isLoading.value = true
  addMessage('assistant', '', true)
  const loadingMsgIndex = messages.value.length - 1

  try {
    const response = await fetch(`${BACKEND_URL}/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        userId: currentUser.value.id,
        message: content
      })
    })

    const data = await response.json()
    
    // Remove loading message
    messages.value.splice(loadingMsgIndex, 1)
    
    if (data.success) {
      const aiMsg = data.data
      addMessage('assistant', aiMsg.content)
    } else {
      addMessage('assistant', '抱歉，我现在无法回答，请稍后再试。')
    }

  } catch (error) {
    console.error('API Error:', error)
    messages.value.splice(loadingMsgIndex, 1)
    addMessage('assistant', '网络连接出现问题，请检查您的网络设置。')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
  font-family: 'Inter', sans-serif;
}

.chat-header {
  background: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
}

.header-content h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.25rem;
}

.header-content p {
  margin: 0.25rem 0 0;
  color: #7f8c8d;
  font-size: 0.875rem;
}

.back-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
}

.back-btn:hover {
  background: #f8f9fa;
  color: #333;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.message {
  display: flex;
  gap: 1rem;
  max-width: 80%;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-message {
  align-self: flex-start;
}

.message-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.ai-avatar {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
}

.ai-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.message-bubble {
  padding: 1rem;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  line-height: 1.6;
  position: relative;
  overflow-wrap: break-word;
}

.html-content :deep(p) {
  margin: 0 0 0.5rem 0;
}

.html-content :deep(p:last-child) {
  margin-bottom: 0;
}

.html-content :deep(a) {
  color: #007bff;
  text-decoration: none;
  font-weight: 500;
}

.html-content :deep(a:hover) {
  text-decoration: underline;
}

.html-content :deep(strong) {
  color: #2c3e50;
  font-weight: 600;
}

.user-message .message-bubble {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 2px;
}

.ai-message .message-bubble {
  background: white;
  color: #2c3e50;
  border-bottom-left-radius: 2px;
}

.message-time {
  font-size: 0.75rem;
  color: #95a5a6;
  margin: 0 0.5rem;
}

.user-message .message-time {
  text-align: right;
}

.chat-input-area {
  background: white;
  padding: 1.5rem 2rem;
  border-top: 1px solid #e0e0e0;
}

.input-wrapper {
  display: flex;
  gap: 1rem;
  background: #f8f9fa;
  padding: 0.75rem;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  align-items: flex-end;
}

.input-wrapper textarea {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  padding: 0.5rem;
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  max-height: 150px;
  color: #2c3e50;
}

.send-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.5rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
  height: 40px;
}

.send-btn:hover:not(:disabled) {
  background: #5a6fd6;
}

.send-btn:disabled {
  background: #a0a0a0;
  cursor: not-allowed;
}

.typing-indicator span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background-color: #b0b0b0;
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

@media (max-width: 768px) {
  .chat-header {
    padding: 1rem;
  }
  
  .chat-messages {
    padding: 1rem;
  }
  
  .message {
    max-width: 90%;
  }
  
  .chat-input-area {
    padding: 1rem;
  }
}
</style>
