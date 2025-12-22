<template>
  <div class="agent-test-container">
    <div class="config-panel">
      <h2>ModelScope AI 测试</h2>
      
      <div class="form-group">
        <label>API Key</label>
        <input v-model="apiKey" type="password" placeholder="ms-..." />
      </div>
      
      <div class="form-group">
        <label>Base URL</label>
        <input v-model="baseUrl" placeholder="/api/ai" />
      </div>
      
      <div class="form-group">
        <label>Model</label>
        <select v-model="selectedModel">
          <optgroup label="视觉模型 (Vision)">
            <option value="Qwen/Qwen3-VL-8B-Instruct">Qwen3-VL-8B (推荐)</option>
            <option value="Qwen/Qwen3-VL-30B-A3B-Thinking">Qwen3-VL-30B-Thinking</option>
            <option value="Qwen/Qwen3-VL-235B-A22B-Instruct">Qwen3-VL-235B</option>
            <option value="Qwen/QVQ-72B-Preview">QVQ-72B-Preview</option>
          </optgroup>
          <optgroup label="文本模型 (Text)">
            <option value="deepseek-ai/DeepSeek-V3.2">DeepSeek-V3.2</option>
            <option value="Qwen/Qwen3-235B-A22B-Instruct">Qwen3-235B</option>
            <option value="Qwen/Qwen3-32B-Instruct">Qwen3-32B</option>
          </optgroup>
        </select>
      </div>
    </div>

    <div class="chat-panel">
      <div class="messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          发送一条消息开始测试...
        </div>
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message"
          :class="msg.role"
        >
          <div class="message-content">
            <div class="role-label">{{ msg.role === 'user' ? 'You' : 'AI' }}</div>
            <div class="text">{{ msg.content }}</div>
            <div v-if="msg.image" class="image-preview">
              <img :src="msg.image" alt="Uploaded" />
            </div>
            <div v-if="msg.error" class="error-text">
              {{ msg.error }}
            </div>
          </div>
        </div>
        <div v-if="loading" class="message assistant loading">
          Thinking...
        </div>
      </div>

      <div class="input-area">
        <div v-if="selectedImage" class="image-preview-mini">
          <img :src="selectedImage" />
          <button @click="clearImage" class="remove-btn">×</button>
        </div>
        
        <div class="input-controls">
          <label class="upload-btn">
            📷
            <input type="file" accept="image/*" @change="handleFileSelect" hidden />
          </label>
          <textarea 
            v-model="inputFilter" 
            placeholder="输入消息..." 
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <button @click="sendMessage" :disabled="loading || (!inputFilter && !selectedImage)">
            发送
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'

const apiKey = ref('ms-41438d67-36a2-46d9-a206-309db6e1ac81')
const baseUrl = ref('https://api.homesee.xyz/api/ai') // Use backend API domain
const selectedModel = ref('Qwen/Qwen3-VL-8B-Instruct')
const inputFilter = ref('')
const selectedImage = ref(null)
const loading = ref(false)
const messages = ref([])
const messagesContainer = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}


// Compress image to stay under 10MB limit
const compressImage = (file, maxSizeMB = 5) => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height
        
        // Scale down large images
        const maxDimension = 2048
        if (width > maxDimension || height > maxDimension) {
          if (width > height) {
            height = (height / width) * maxDimension
            width = maxDimension
          } else {
            width = (width / height) * maxDimension
            height = maxDimension
          }
        }
        
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        
        // Compress with quality adjustment
        let quality = 0.8
        let result = canvas.toDataURL('image/jpeg', quality)
        
        // Further reduce quality if still too large
        while (result.length > maxSizeMB * 1024 * 1024 && quality > 0.1) {
          quality -= 0.1
          result = canvas.toDataURL('image/jpeg', quality)
        }
        
        console.log(`Image compressed: ${(result.length / 1024 / 1024).toFixed(2)}MB, quality: ${quality}`)
        resolve(result)
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

const handleFileSelect = async (event) => {
  const file = event.target.files[0]
  if (file) {
    // Compress if larger than 2MB
    if (file.size > 2 * 1024 * 1024) {
      selectedImage.value = await compressImage(file)
    } else {
      const reader = new FileReader()
      reader.onload = (e) => {
        selectedImage.value = e.target.result
      }
      reader.readAsDataURL(file)
    }
  }
}

const clearImage = () => {
  selectedImage.value = null
}

const sendMessage = async () => {
  if ((!inputFilter.value && !selectedImage.value) || loading.value) return

  const userMessage = {
    role: 'user',
    content: inputFilter.value,
    image: selectedImage.value
  }
  
  messages.value.push(userMessage)
  const currentInput = inputFilter.value
  const currentImage = selectedImage.value
  
  inputFilter.value = ''
  selectedImage.value = null
  loading.value = true
  scrollToBottom()

  try {
    // Build message content - use array format for vision, string for text
    let messageContent
    
    if (currentImage) {
      // Vision model: use array format
      messageContent = []
      if (currentInput) {
        messageContent.push({ type: 'text', text: currentInput })
      }
      messageContent.push({
        type: 'image_url',
        image_url: {
          url: currentImage
        }
      })
    } else {
      // Text-only: use simple string format
      messageContent = currentInput
    }

    const payload = {
      model: selectedModel.value,
      messages: [
        {
          role: 'user',
          content: messageContent
        }
      ],
      max_tokens: 2048,
      stream: false  // ModelScope requires this parameter
    }

    const response = await axios.post(`${baseUrl.value}/chat/completions`, payload, {
      headers: {
        'Authorization': `Bearer ${apiKey.value}`,
        'Content-Type': 'application/json'
      }
    })

    const aiResponse = response.data.choices[0].message.content
    messages.value.push({
      role: 'assistant',
      content: aiResponse
    })

  } catch (error) {
    console.error('API Error:', error)
    let errorMessage = '请求失败'
    if (error.response) {
      errorMessage = `Error ${error.response.status}: ${JSON.stringify(error.response.data)}`
    } else {
      errorMessage = error.message
    }
    
    messages.value.push({
      role: 'assistant',
      content: '请求出错',
      error: errorMessage
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.agent-test-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  gap: 20px;
  height: 90vh;
  box-sizing: border-box;
}

.config-panel {
  width: 300px;
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  height: 100%;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  height: 100%;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f9f9f9;
}

.message {
  margin-bottom: 20px;
  max-width: 80%;
}

.message.user {
  margin-left: auto;
}

.message.user .message-content {
  background: #007bff;
  color: white;
  border-radius: 12px 12px 0 12px;
}

.message.assistant .message-content {
  background: white;
  border: 1px solid #ddd;
  border-radius: 12px 12px 12px 0;
}

.message-content {
  padding: 12px;
  position: relative;
}

.role-label {
  font-size: 0.8em;
  opacity: 0.7;
  margin-bottom: 4px;
}

.image-preview img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 4px;
  margin-top: 8px;
}

.error-text {
  color: #ff4444;
  font-family: monospace;
  background: rgba(255, 0, 0, 0.1);
  padding: 8px;
  margin-top: 8px;
  border-radius: 4px;
  font-size: 0.9em;
  white-space: pre-wrap;
}

.input-area {
  padding: 20px;
  border-top: 1px solid #ddd;
  background: white;
}

.input-controls {
  display: flex;
  gap: 10px;
}

.upload-btn {
  font-size: 1.5em;
  cursor: pointer;
  padding: 0 10px;
  display: flex;
  align-items: center;
}

textarea {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: none;
  height: 40px;
}

button {
  padding: 0 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.image-preview-mini {
  position: relative;
  display: inline-block;
  margin-bottom: 10px;
}

.image-preview-mini img {
  height: 60px;
  border-radius: 4px;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  padding: 0;
  background: red;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
