<template>
  <div class="speech-test">
    <h1>语音识别测试页面</h1>
    
    <div class="test-section">
      <h2>语音识别状态</h2>
      <div class="status">
        <p>浏览器支持语音识别: <span :class="browserSupport ? 'supported' : 'not-supported'">{{ browserSupport ? '是' : '否' }}</span></p>
        <p>麦克风权限: <span :class="micPermission ? 'granted' : 'denied'">{{ micPermission ? '已授权' : '未授权' }}</span></p>
        <p>当前状态: <span class="status-text">{{ statusText }}</span></p>
      </div>
    </div>

    <div class="test-section">
      <h2>语音识别测试</h2>
      <button @click="startTest" :disabled="isListening" class="test-btn">
        {{ isListening ? '正在聆听...' : '开始语音测试' }}
      </button>
      
      <div v-if="testResult" class="result">
        <h3>识别结果:</h3>
        <p class="transcript">{{ testResult.transcript }}</p>
        <p class="confidence">置信度: {{ testResult.confidence }}</p>
      </div>
    </div>

    <div class="test-section">
      <h2>调试信息</h2>
      <div class="debug-info">
        <p>语音识别API: {{ speechAPI }}</p>
        <p>语言设置: {{ language }}</p>
        <p>错误信息: {{ errorMessage || '无' }}</p>
      </div>
    </div>

    <div class="test-section">
      <h2>手动测试</h2>
      <input v-model="manualInput" placeholder="手动输入测试文本" class="manual-input" />
      <button @click="testManualInput" class="test-btn">测试手动输入</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const browserSupport = ref(false)
const micPermission = ref(false)
const statusText = ref('未初始化')
const isListening = ref(false)
const testResult = ref(null)
const speechAPI = ref('未知')
const language = ref('zh-CN')
const errorMessage = ref('')
const manualInput = ref('')

let recognition = null

onMounted(() => {
  checkBrowserSupport()
  checkMicrophonePermission()
})

const checkBrowserSupport = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  browserSupport.value = !!SpeechRecognition
  speechAPI.value = window.SpeechRecognition ? 'SpeechRecognition' : window.webkitSpeechRecognition ? 'webkitSpeechRecognition' : '不支持'
  
  if (browserSupport.value) {
    initRecognition()
  } else {
    statusText.value = '浏览器不支持语音识别'
  }
}

const checkMicrophonePermission = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    micPermission.value = true
    stream.getTracks().forEach(track => track.stop())
  } catch (error) {
    micPermission.value = false
    errorMessage.value = `麦克风权限错误: ${error.message}`
  }
}

const initRecognition = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  recognition = new SpeechRecognition()
  
  recognition.lang = 'zh-CN'
  recognition.continuous = false
  recognition.interimResults = true
  recognition.maxAlternatives = 3

  recognition.onstart = () => {
    isListening.value = true
    statusText.value = '正在聆听...'
    errorMessage.value = ''
  }

  recognition.onresult = (event) => {
    console.log("语音识别结果:", event)
    
    const lastResult = event.results[event.results.length - 1]
    const transcript = lastResult[0].transcript.trim()
    const confidence = lastResult[0].confidence
    
    testResult.value = {
      transcript,
      confidence: (confidence * 100).toFixed(2) + '%'
    }
    
    statusText.value = '识别完成'
  }

  recognition.onend = () => {
    isListening.value = false
    if (!testResult.value) {
      statusText.value = '未检测到语音'
    }
  }

  recognition.onerror = (event) => {
    isListening.value = false
    statusText.value = '识别错误'
    errorMessage.value = `错误: ${event.error}`
    console.error('语音识别错误:', event)
  }
}

const startTest = () => {
  if (!browserSupport.value) {
    errorMessage.value = '浏览器不支持语音识别'
    return
  }
  
  if (!micPermission.value) {
    errorMessage.value = '请先授权麦克风权限'
    return
  }
  
  testResult.value = null
  errorMessage.value = ''
  
  try {
    recognition.start()
  } catch (error) {
    errorMessage.value = `启动失败: ${error.message}`
  }
}

const testManualInput = () => {
  if (manualInput.value.trim()) {
    testResult.value = {
      transcript: manualInput.value,
      confidence: '100% (手动输入)'
    }
    manualInput.value = ''
  }
}
</script>

<style scoped>
.speech-test {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.test-section {
  margin: 30px 0;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #f9f9f9;
}

.status p {
  margin: 10px 0;
  font-size: 16px;
}

.supported, .granted {
  color: green;
  font-weight: bold;
}

.not-supported, .denied {
  color: red;
  font-weight: bold;
}

.status-text {
  color: #666;
  font-style: italic;
}

.test-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  margin: 10px 0;
}

.test-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.test-btn:hover:not(:disabled) {
  background: #0056b3;
}

.result {
  margin-top: 20px;
  padding: 15px;
  background: white;
  border-radius: 6px;
  border-left: 4px solid #007bff;
}

.transcript {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.confidence {
  color: #666;
  font-style: italic;
}

.debug-info {
  background: white;
  padding: 15px;
  border-radius: 6px;
  font-family: monospace;
}

.manual-input {
  width: 100%;
  padding: 10px;
  margin: 10px 0;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}
</style>
