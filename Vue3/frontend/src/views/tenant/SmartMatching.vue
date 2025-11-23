<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="header-content">
        <h2>智能租房助手</h2>
        <p>基于 DeepSeek AI为您提供个性化租房建议</p>
      </div>
      <button class="back-btn" @click="goBack">返回首页</button>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']">
        <div class="message-avatar">
          <img v-if="msg.role === 'user'" :src="userAvatar" alt="User">
          <div v-else class="ai-avatar">AI</div>
        </div>
        <div class="message-content">
          <div class="message-bubble">
            <p v-if="msg.content">{{ msg.content }}</p>
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

const router = useRouter()
const messages = ref([])
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const textarea = ref(null)
const currentUser = ref(null)

// API Configuration
const API_KEY = 'sk-e2fdf28cd6f34468a5d80ad14e840d77'
const API_URL = 'https://api.deepseek.com/chat/completions'

// User Avatar
const userAvatar = ref('/src/assets/image/default-avatar.png')

onMounted(() => {
  // Load user info
  const userData = localStorage.getItem('user')
  if (userData) {
    currentUser.value = JSON.parse(userData)
    if (currentUser.value.avatar) {
      userAvatar.value = `/src/assets/image/${currentUser.value.avatar}`
    }
  }

  // Initial greeting
  addMessage('assistant', `您好${currentUser.value ? ' ' + (currentUser.value.realName || currentUser.value.username) : ''}！我是您的智能租房助手。请告诉我您的预算、偏好区域或房型要求，我会为您推荐合适的房源。`)
})

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

  const content = userInput.value.trim()
  userInput.value = ''
  if (textarea.value) textarea.value.style.height = 'auto'

  // Add user message
  addMessage('user', content)

  // Add loading placeholder
  isLoading.value = true
  addMessage('assistant', '', true)
  const loadingMsgIndex = messages.value.length - 1

  try {
    // Prepare context
    const userContext = currentUser.value ? 
      `当前用户: ${currentUser.value.realName || currentUser.value.username} (ID: ${currentUser.value.id}, 电话: ${currentUser.value.phone})` : 
      '当前用户: 未登录访客'

    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${API_KEY}`
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: [
          {
            role: 'system',
            content: `你是一个专业的房屋租赁智能助手。${userContext}。
请根据用户的需求推荐房源，或者回答关于租房的问题。
你的回答应该专业、热情、简洁。
如果用户询问房源，请询问他们的预算、位置偏好和户型要求。
不要编造具体的房源ID，而是提供一般性的建议或引导用户去房源列表查看。`
          },
          ...messages.value.filter(m => !m.loading).map(m => ({
            role: m.role,
            content: m.content
          }))
        ],
        stream: false
      })
    })

    const data = await response.json()
    
    // Remove loading message and add actual response
    messages.value.splice(loadingMsgIndex, 1)
    
    if (data.choices && data.choices.length > 0) {
      addMessage('assistant', data.choices[0].message.content)
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
  line-height: 1.5;
  position: relative;
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
