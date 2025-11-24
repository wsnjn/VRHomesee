<template>
  <div class="model-wrapper" :class="{ 'speaking': isSpeaking }">
    <!-- Cloud Message Bubble -->
    <div v-if="displayMessage" class="cloud-bubble" :class="{ 'fade-out': isFadingOut && !isPermanentMessage }">
      <div class="cloud-content">
        <span>{{ displayMessage }}</span>
        <div v-if="isLoading" class="typing-indicator">
          <span></span><span></span><span></span>
        </div>
      </div>
      <div class="cloud-tail"></div>
    </div>

    <!-- 3D Model Container -->
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

// Three.js refs
let scene, camera, renderer, controls, mixer, animationFrameId

// Speech Recognition
let recognition = null
let isListening = false
const WAKE_WORD = '小花'
const STOP_WORDS = ['好了没你事了', '关闭', '退下', '闭嘴', '停止', '再见']

// API Configuration
const BACKEND_URL = 'http://localhost:8080/api/smart-matching'

const displayMessage = computed(() => currentMessage.value)

onMounted(() => {
  initThree()
  initSpeechRecognition()
  startListening()
})

onBeforeUnmount(() => {
  if (animationFrameId) cancelAnimationFrame(animationFrameId)
  if (renderer) renderer.dispose()
  if (recognition) recognition.stop()
  window.speechSynthesis.cancel()
})

// --- Three.js Initialization ---
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

// --- Speech & AI Logic ---
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
  recognition.continuous = false // Disable continuous for click-to-talk
  recognition.interimResults = false

  recognition.onstart = () => {
    console.log("Speech recognition started")
    isListening = true
    showPermanentMessage("正在聆听...")
  }

  recognition.onresult = async (event) => {
    isListening = false
    const lastResult = event.results[event.results.length - 1]
    const transcript = lastResult[0].transcript.trim()
    console.log('Heard:', transcript)

    if (checkStopCommand(transcript)) {
      stopInteraction()
      return
    }

    await processQuery(transcript)
  }

  recognition.onend = () => {
    console.log("Recognition ended")
    isListening = false
    if (!isLoading.value && !isSpeaking.value) {
        showTemporaryMessage("点击模型开始对话")
    }
  }
  
  recognition.onerror = (event) => {
    console.error("Speech error:", event.error)
    isListening = false
    if (event.error === 'not-allowed') {
      showPermanentMessage("请允许麦克风权限")
    } else {
      showTemporaryMessage("听不清楚，请重试")
    }
  }
}

const toggleListening = () => {
  if (isListening) {
    recognition.stop()
    isListening = false
  } else {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    try {
      recognition.start()
    } catch (e) {
      console.error("Failed to start recognition:", e)
    }
  }
}

const startListening = () => {
    // No-op for auto-start, wait for user click
    showTemporaryMessage("点击模型开始对话")
}

const resetConversationTimeout = () => {
  if (conversationTimeout) clearTimeout(conversationTimeout)
  conversationTimeout = setTimeout(() => {
    inConversation.value = false
    showTemporaryMessage("再见！")
    setTimeout(() => currentMessage.value = '', 2000)
  }, 30000) // 30 seconds timeout
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
  
  // Show thinking state
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
      
      // Parse Action
      const actionMatch = aiContent.match(/<!-- ACTION: (.*?) -->/)
      let cleanContent = aiContent.replace(/<!-- ACTION: .*? -->/, '')
      
      // Strip HTML for TTS
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
  
  window.speechSynthesis.cancel() // Cancel previous
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

/* Cloud Bubble Styles */
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

/* Typing Indicator */
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
