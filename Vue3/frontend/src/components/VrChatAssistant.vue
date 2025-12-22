<template>
  <!-- 收起时的侧边触发器 - 独立于主容器 -->
  <div v-if="isCollapsed" class="side-trigger" @click="isCollapsed = false">
    <span>AI</span>
  </div>

  <!-- 主聊天容器 -->
  <div class="vr-chat-assistant" :class="{ minimized: isMinimized, collapsed: isCollapsed }">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-title" @click="toggleMinimize">
        <span class="header-text">VR看房助手</span>
      </div>
      <div class="header-actions">
        <button class="action-btn" @click="toggleMinimize">{{ isMinimized ? '展开' : '收起' }}</button>
        <button class="action-btn collapse-btn" @click="isCollapsed = true">→</button>
      </div>
    </div>

    <!-- 聊天内容 -->
    <div v-if="!isMinimized" class="chat-body">
      <div class="messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <p>👋 您好！我是 AI 看房助手。</p>
          <p>您可以问我关于这套房源的任何问题，比如：</p>
          <ul>
            <li>"这个房间看起来多大？"</li>
            <li>"窗户朝向哪边？"</li>
            <li>"装修风格是什么？"</li>
          </ul>
        </div>

        <!-- 消息列表 -->
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message"
          :class="msg.role"
        >
          <div class="message-content" v-if="msg.role === 'user'">{{ msg.content }}</div>
          <div class="message-content html-content" v-else v-html="msg.content"></div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="message assistant loading">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <textarea 
          v-model="inputText" 
          placeholder="输入您的问题..."
          @keydown.enter.exact.prevent="sendMessage"
          :disabled="loading"
        ></textarea>
        <button @click="sendMessage" :disabled="loading || !inputText.trim()" class="send-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  // 房源信息
  roomInfo: {
    type: Object,
    default: () => ({})
  },
  // Three.js 渲染器引用（用于截图）
  renderer: {
    type: Object,
    default: null
  },
  // 当前用户ID
  userId: {
    type: [String, Number],
    default: null
  }
})

const API_BASE = 'https://api.homesee.xyz/api/ai/house-tour'

const isMinimized = ref(false)
const isCollapsed = ref(false)
const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const messagesContainer = ref(null)

// 对话历史（用于上下文）
const conversationHistory = ref([])

// 切换最小化
const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 截取当前 VR 画面
const captureScreenshot = () => {
  console.log('Attempting to capture screenshot...')
  console.log('Renderer prop:', props.renderer)
  
  // 方法1：通过传入的渲染器
  if (props.renderer && props.renderer.domElement) {
    try {
      const canvas = props.renderer.domElement
      console.log('Using renderer canvas:', canvas.width, 'x', canvas.height)
      const dataUrl = canvas.toDataURL('image/jpeg', 0.7)
      console.log('Screenshot size:', Math.round(dataUrl.length / 1024), 'KB')
      return dataUrl
    } catch (e) {
      console.error('Failed to capture from renderer:', e)
    }
  }
  
  // 方法2：直接从DOM查找VR容器中的canvas
  const vrContainer = document.querySelector('.vr-container canvas')
  if (vrContainer) {
    try {
      console.log('Using DOM canvas:', vrContainer.width, 'x', vrContainer.height)
      const dataUrl = vrContainer.toDataURL('image/jpeg', 0.7)
      console.log('Screenshot size:', Math.round(dataUrl.length / 1024), 'KB')
      return dataUrl
    } catch (e) {
      console.error('Failed to capture from DOM canvas:', e)
    }
  }
  
  console.warn('No canvas found for screenshot')
  return null
}

// 发送消息
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  conversationHistory.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    // 截取当前 VR 画面
    const screenshot = captureScreenshot()

    console.log('Sending message to AI...', { userId: props.userId, text })

    // 调用 AI 接口
    const response = await axios.post(`${API_BASE}/chat`, {
      userId: props.userId, // 传递用户ID用于存储历史
      message: text,
      roomInfo: props.roomInfo,
      history: conversationHistory.value.slice(-10), // 保留最近10条历史
      screenshot: screenshot
    })

    if (response.data.success) {
      const aiMessage = response.data.message
      messages.value.push({ role: 'assistant', content: aiMessage })
      conversationHistory.value.push({ role: 'assistant', content: aiMessage })
    } else {
      messages.value.push({ 
        role: 'assistant', 
        content: '抱歉，我暂时无法回答您的问题。请稍后再试。' 
      })
    }
  } catch (error) {
    console.error('AI Chat Error:', error)
    messages.value.push({ 
      role: 'assistant', 
      content: '网络错误，请检查网络连接后重试。' 
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
/* 工业极简风格 - Industrial Minimal Design */
.vr-chat-assistant {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 360px;
  max-height: 500px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 0; /* 直角设计 */
  display: flex;
  flex-direction: column;
  z-index: 1000;
  transition: transform 0.3s ease, opacity 0.3s ease;
  font-family: 'Inter', system-ui, sans-serif;
}

/* 收起状态 */
.vr-chat-assistant.collapsed {
  transform: translateX(calc(100% + 20px));
  opacity: 0;
  pointer-events: none;
}

/* 最小化状态 */
.vr-chat-assistant.minimized {
  max-height: 50px;
}

/* 侧边触发器 */
.side-trigger {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 100px;
  background: #111827;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1001;
  border-radius: 0;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 2px;
  transition: background 0.2s;
}

.side-trigger:hover {
  background: #1F2937;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #111827;
  color: #FFFFFF;
  border-bottom: 1px solid #E5E7EB;
  border-radius: 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  flex: 1;
}

.header-icon {
  font-size: 16px;
}

.header-text {
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.2);
  color: #FFFFFF;
  padding: 4px 12px;
  border-radius: 0;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: background 0.2s;
}

.action-btn:hover {
  background: rgba(255,255,255,0.1);
}

.collapse-btn {
  font-size: 16px;
  padding: 4px 8px;
}

/* 聊天内容区 */
.chat-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  max-height: 450px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  max-height: 320px;
  background: #FFFFFF;
}

/* 欢迎消息 - 工业极简风格 */
.welcome-message {
  background: #F9FAFB;
  padding: 16px;
  border: 1px solid #E5E7EB;
  border-radius: 0;
  font-size: 13px;
  color: #111827;
  line-height: 1.6;
}

.welcome-message p {
  margin: 0 0 8px 0;
  font-weight: 600;
}

.welcome-message ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.welcome-message li {
  margin: 6px 0;
  color: #6B7280;
}

/* 消息气泡 - 直角设计 */
.message {
  margin-bottom: 12px;
  max-width: 75%;
}

.message.user {
  margin-left: auto;
}

.message.user .message-content {
  background: #4B5563;
  color: #FFFFFF;
   border-radius: 0;
  border: none;
}

.message.assistant .message-content {
  background: #F3F4F6;
  color: #111827;
  border-radius: 0;
  border: 1px solid #E5E7EB;
}

.message-content {
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.6;
}

/* HTML 内容样式 */
.html-content :deep(p) {
  margin: 0 0 8px 0;
}

.html-content :deep(p:last-child) {
  margin-bottom: 0;
}

.html-content :deep(ul), .html-content :deep(ol) {
  margin: 4px 0 8px 0;
  padding-left: 20px;
}

.html-content :deep(li) {
  margin-bottom: 4px;
}

.html-content :deep(strong) {
  color: #111827;
  font-weight: 600;
}

/* 加载动画 */
.message.loading .typing-indicator {
  display: flex;
  gap: 4px;
  padding: 10px 14px;
  background: #F3F4F6;
  border: 1px solid #E5E7EB;
  border-radius: 0;
  width: fit-content;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #111827;
  border-radius: 50%;
  animation: bounce 1.4s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

/* 输入区 - 工业极简风格 */
.input-area {
  display: flex;
  gap: 0;
  padding: 0;
  border-top: 1px solid #E5E7EB;
}

.input-area textarea {
  flex: 1;
  border: none;
  border-right: 1px solid #E5E7EB;
  border-radius: 0;
  padding: 6px 12px;
  resize: none;
  height: 24px;
  font-size: 13px;
  font-family: inherit;
  outline: none;
  background: #FFFFFF;
  color: #111827;
}

.input-area textarea::placeholder {
  color: #9CA3AF;
}

.input-area textarea:focus {
  background: #F9FAFB;
  border-right-color: #111827;
}

.send-btn {
  background: #111827;
  border: none;
  border-radius: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
}

.send-btn:hover {
  background: #1F2937;
}

.send-btn:disabled {
  background: #9CA3AF;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 480px) {
  .vr-chat-assistant {
    width: calc(100% - 20px);
    right: 10px;
    bottom: 10px;
  }
  
  .side-trigger {
    width: 32px;
    height: 80px;
    font-size: 12px;
  }
}
</style>
