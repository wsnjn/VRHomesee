<template>
  <div class="model-wrapper" :class="{ 'speaking': isSpeaking }">
    <!-- 云朵消息气泡 -->
    <div v-if="displayMessage" class="cloud-bubble" :class="{ 'fade-out': isFadingOut && !isPermanentMessage }">
      <div class="cloud-content">
        <span>{{ displayMessage }}</span>
        <div v-if="isLoading" class="typing-indicator">
          <span></span><span></span><span></span>
        </div>
      </div>
      <div class="cloud-tail"></div>
    </div>

    <!-- 3D 模型容器 -->
    <div ref="modelContainer" class="model-container" @click="handleWakeUp"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'
import { userState } from '../state/user'

const router = useRouter()
const modelContainer = ref(null)
const currentMessage = ref('')
const isFadingOut = ref(false)
const isSpeaking = ref(false)
const isLoading = ref(false)
const isPermanentMessage = ref(false)

// Three.js 引用
let scene, camera, renderer, controls, mixer, animationFrameId

// 语音识别
let recognition = null
let isListening = false
const WAKE_WORD = '小花'
const STOP_WORDS = ['好了没你事了', '关闭', '退下', '闭嘴', '停止', '再见']

// API 配置
const BACKEND_URL = 'https://api.homesee.xyz/api/smart-matching'

const displayMessage = computed(() => currentMessage.value)

onMounted(() => {
  initThree()
  initSpeechRecognitionWithFallback()
  startListening()
})

onBeforeUnmount(() => {
  if (animationFrameId) cancelAnimationFrame(animationFrameId)
  if (renderer) renderer.dispose()
  if (recognition) recognition.stop()
  window.speechSynthesis.cancel()
})

// --- Three.js 初始化 ---
const initThree = () => {
  scene = new THREE.Scene()
  
  camera = new THREE.PerspectiveCamera(75, 150 / 150, 0.1, 1000)
  camera.position.set(0, 1, 3)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(150, 150)
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.setClearColor(0x000000, 0)
  
  if (modelContainer.value) {
    modelContainer.value.appendChild(renderer.domElement)
  }

  const ambientLight = new THREE.AmbientLight(0xffffff, 0.7)
  scene.add(ambientLight)

  const directionalLight = new THREE.DirectionalLight(0xffffff, 1.0)
  directionalLight.position.set(2, 2, 2)
  scene.add(directionalLight)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.enableZoom = false
  controls.enablePan = false

  const loader = new GLTFLoader()
  loader.load(
    "/models/cute_home_robot.glb",
    (gltf) => {
      const model = gltf.scene
      model.scale.set(0.5, 0.5, 0.5)
      scene.add(model)

      if (gltf.animations && gltf.animations.length > 0) {
        mixer = new THREE.AnimationMixer(model)
        gltf.animations.forEach(clip => {
          mixer.clipAction(clip).play()
        })
      }
      animate()
    },
    undefined,
    (error) => console.error('Model load failed:', error)
  )
}

const animate = () => {
  animationFrameId = requestAnimationFrame(animate)
  if (mixer) mixer.update(0.01)
  controls.update()
  renderer.render(scene, camera)
}

// --- 语音 & AI 逻辑 ---
const inConversation = ref(false)
let conversationTimeout = null

const initSpeechRecognition = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) {
    console.warn("Browser does not support Speech Recognition")
    showPermanentMessage("您的浏览器不支持语音功能")
    return
  }

  recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.continuous = false // 禁用连续模式，用于点击对话
  recognition.interimResults = true // 启用临时结果用于调试
  recognition.maxAlternatives = 3 // 获取多个备选结果

  recognition.onstart = () => {
    console.log("语音识别已启动")
    console.log("正在监听麦克风...")
    isListening = true
    showPermanentMessage("正在聆听...")
  }

  recognition.onresult = async (event) => {
    isListening = false
    
    const lastResult = event.results[event.results.length - 1]
    const transcript = lastResult[0].transcript.trim()
    const confidence = lastResult[0].confidence
    
    // 只显示最终结果，减少控制台噪音
    if (lastResult.isFinal) {
      console.log("=== 语音识别完成 ===")
      console.log('识别结果:', transcript)
      console.log('置信度:', confidence)
      
      // 检查是否获取到有效语音
      if (!transcript || transcript === "？？？" || transcript === "..." || transcript.length < 1) {
        console.log("未检测到有效语句或识别失败")
        showTemporaryMessage("未检测到语音，请重试")
        return
      }

      if (checkStopCommand(transcript)) {
        stopInteraction()
        return
      }

      await processQuery(transcript)
    } else {
      // 实时显示识别进度（可选）
      console.log("识别中:", transcript)
    }
  }

  recognition.onend = () => {
    console.log("语音识别已结束")
    isListening = false
    if (!isLoading.value && !isSpeaking.value) {
        showTemporaryMessage("点击模型开始对话")
    }
  }
  
  recognition.onerror = (event) => {
    console.error("Speech error:", event.error)
    isListening = false
    
    // 更详细的错误处理
    switch (event.error) {
      case 'not-allowed':
        showPermanentMessage("请允许麦克风权限")
        break
      case 'network':
        showTemporaryMessage("网络连接失败，请检查网络")
        break
      case 'audio-capture':
        showTemporaryMessage("无法访问麦克风，请检查设备")
        break
      case 'no-speech':
        showTemporaryMessage("没有检测到语音，请重试")
        break
      case 'aborted':
        showTemporaryMessage("语音识别被中断")
        break
      case 'service-not-allowed':
        showPermanentMessage("语音服务不可用")
        break
      default:
        showTemporaryMessage("语音识别失败，请重试")
    }
  }
}

// 备用语音识别方案 - 使用更简单的API
const initFallbackSpeechRecognition = () => {
  console.log("Using fallback speech recognition")
  showTemporaryMessage("使用备用语音识别")
  
  // 这里可以添加第三方语音识别API的集成
  // 例如：百度语音识别、讯飞语音识别等
}

// 手动输入备选方案
const showManualInput = () => {
  const userInput = prompt("语音识别不可用，请输入您的问题：")
  if (userInput && userInput.trim()) {
    processQuery(userInput.trim())
  }
}

// 改进的语音识别初始化
const initSpeechRecognitionWithFallback = () => {
  try {
    initSpeechRecognition()
  } catch (error) {
    console.error("Speech recognition init failed:", error)
    showTemporaryMessage("语音识别初始化失败，使用手动输入")
    // 如果语音识别失败，提供手动输入选项
    setTimeout(() => {
      showManualInput()
    }, 2000)
  }
}

const toggleListening = () => {
  if (isListening) {
    console.log("停止语音识别")
    recognition.stop()
    isListening = false
  } else {
    console.log("开始语音识别")
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    try {
      recognition.start()
      console.log("语音识别已启动，等待用户说话...")
    } catch (e) {
      console.error("启动语音识别失败:", e)
      showTemporaryMessage("语音识别启动失败，请检查麦克风权限")
    }
  }
}

const startListening = () => {
    // 自动启动无操作，等待用户点击
    showTemporaryMessage("点击模型开始对话")
}

const resetConversationTimeout = () => {
  if (conversationTimeout) clearTimeout(conversationTimeout)
  conversationTimeout = setTimeout(() => {
    inConversation.value = false
    showTemporaryMessage("再见！")
    setTimeout(() => currentMessage.value = '', 2000)
  }, 30000) // 30秒超时
}

const checkStopCommand = (text) => {
  return STOP_WORDS.some(word => text.includes(word))
}

const stopInteraction = () => {
  window.speechSynthesis.cancel()
  isSpeaking.value = false
  inConversation.value = false
  if (recognition) recognition.stop()
  showTemporaryMessage("好的，再见！")
}

const handleWakeUp = () => {
  toggleListening()
}

const processQuery = async (text) => {
  resetConversationTimeout()
  
  // 显示思考状态
  isLoading.value = true
  showPermanentMessage("正在思考...")
  
  try {
    const userId = userState.user ? userState.user.id : 0
    
    const response = await fetch(`${BACKEND_URL}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, message: text })
    })

    const data = await response.json()
    isLoading.value = false
    
    if (data.success) {
      const aiContent = data.data.content
      
      // 解析动作
      const actionMatch = aiContent.match(/<!-- ACTION: (.*?) -->/)
      let cleanContent = aiContent.replace(/<!-- ACTION: .*? -->/, '')
      
      // 去除 HTML 标签用于语音合成
      const ttsText = cleanContent.replace(/<[^>]*>/g, '')
      
      showTemporaryMessage(cleanContent)
      speak(ttsText)

      if (actionMatch) {
        showPermanentMessage("正在跳转...")
        setTimeout(() => {
             try {
              const action = JSON.parse(actionMatch[1])
              handleAction(action)
            } catch (e) {
              console.error("Failed to parse action", e)
            }
        }, 1500)
      }
    } else {
        showTemporaryMessage("抱歉，我出错了")
        speak("抱歉，我出错了")
    }
  } catch (error) {
    console.error("AI Error:", error)
    isLoading.value = false
    showTemporaryMessage("网络连接失败")
    speak("网络连接失败")
  }
}

const handleAction = (action) => {
  if (action.type === 'navigate') {
    console.log("Navigating to:", action.path)
    router.push(action.path)
  }
}

const speak = (text) => {
  if (!window.speechSynthesis) return
  
  window.speechSynthesis.cancel() // 取消之前的语音
  isSpeaking.value = true
  
  const utter = new SpeechSynthesisUtterance(text)
  utter.lang = "zh-CN"
  utter.rate = 1.1
  
  utter.onend = () => {
    isSpeaking.value = false
  }
  
  window.speechSynthesis.speak(utter)
}

const showTemporaryMessage = (text) => {
  isPermanentMessage.value = false
  currentMessage.value = text.replace(/<[^>]*>/g, '').substring(0, 50) + (text.length > 50 ? '...' : '')
  isFadingOut.value = false
  
  setTimeout(() => {
    if (!isPermanentMessage.value) {
        isFadingOut.value = true
        setTimeout(() => {
            if (isFadingOut.value && !isPermanentMessage.value) currentMessage.value = ''
        }, 500)
    }
  }, 6000)
}

const showPermanentMessage = (text) => {
  isPermanentMessage.value = true
  isFadingOut.value = false
  currentMessage.value = text
}

</script>

<style scoped>
.model-wrapper {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.model-container {
  width: 150px;
  height: 150px;
  cursor: pointer;
  transition: transform 0.3s;
}

.model-wrapper.speaking .model-container {
  transform: scale(1.1);
}

/* 云朵气泡样式 */
.cloud-bubble {
  position: absolute;
  bottom: 140px;
  background: white;
  padding: 15px;
  border-radius: 20px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  max-width: 200px;
  min-width: 100px;
  font-size: 14px;
  color: #333;
  opacity: 1;
  transition: opacity 0.5s;
  pointer-events: none;
  border: 2px solid #e0e0e0;
  text-align: center;
}

.cloud-bubble.fade-out {
  opacity: 0;
}

.cloud-tail {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 20px;
  background: white;
  border-bottom: 2px solid #e0e0e0;
  border-right: 2px solid #e0e0e0;
  clip-path: polygon(0 0, 100% 100%, 100% 0);
  transform: translateX(-50%) rotate(45deg);
}

.cloud-content {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 打字指示器 */
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
</style>
