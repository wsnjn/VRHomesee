<template>
  <div class="virtual-world-container" ref="container">
    <!-- 全息HUD界面 -->
    <div class="hud-overlay">
      <!-- 系统状态面板 -->
      <div class="system-status-panel">
        <div class="status-grid">
          <div class="status-item">
            <div class="status-label">NEURAL_NETWORK</div>
            <div class="status-value active">ONLINE</div>
            <div class="status-bar">
              <div class="bar-fill" style="width: 95%"></div>
            </div>
          </div>
          <div class="status-item">
            <div class="status-label">VISION_PROCESSOR</div>
            <div class="status-value" :class="{ active: handTrackingActive }">
              {{ handTrackingActive ? 'TRACKING' : 'STANDBY' }}
            </div>
            <div class="status-bar">
              <div class="bar-fill" :style="{ width: handTrackingActive ? '85%' : '10%' }"></div>
            </div>
          </div>
          <div class="status-item">
            <div class="status-label">VOICE_INTERFACE</div>
            <div class="status-value" :class="{ active: voiceActive }">
              {{ voiceActive ? 'LISTENING' : 'OFFLINE' }}
            </div>
            <div class="status-bar">
              <div class="bar-fill" :style="{ width: voiceActive ? '75%' : '5%' }"></div>
            </div>
          </div>
          <div class="status-item">
            <div class="status-label">QUANTUM_RENDER</div>
            <div class="status-value active">OPTIMAL</div>
            <div class="status-bar">
              <div class="bar-fill" style="width: 98%"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 手势识别面板 -->
      <div class="gesture-panel" :class="{ active: handTrackingActive }">
        <div class="panel-header">
          <span class="panel-title">GESTURE_INTERFACE</span>
          <div class="panel-indicators">
            <div class="indicator left" :class="{ active: leftHandDetected }">
              L
            </div>
            <div class="indicator right" :class="{ active: rightHandDetected }">
              R
            </div>
          </div>
        </div>
        
        <div class="gesture-display">
          <div class="current-gesture">
            <div class="gesture-icon">{{ currentGestureIcon }}</div>
            <div class="gesture-name">{{ currentGestureName }}</div>
          </div>
          
          <div class="gesture-history">
            <div v-for="(gesture, index) in gestureHistory" :key="index" 
                 class="history-item" :class="gesture.type">
              {{ gesture.icon }}
            </div>
          </div>
        </div>
        
        <div class="gesture-stats">
          <div class="stat">
            <div class="stat-label">CONFIDENCE</div>
            <div class="stat-value">{{ gestureConfidence }}%</div>
          </div>
          <div class="stat">
            <div class="stat-label">LATENCY</div>
            <div class="stat-value">{{ processingLatency }}ms</div>
          </div>
        </div>
      </div>

      <!-- 语音交互面板 -->
      <div class="voice-panel" :class="{ active: voiceActive }">
        <div class="panel-header">
          <span class="panel-title">NEURAL_INTERFACE</span>
          <div class="voice-level" :style="{ height: voiceLevel + '%' }"></div>
        </div>
        
        <div class="voice-display">
          <div class="voice-text" :class="{ listening: isListening }">
            {{ voiceTranscript || 'AWAITING_COMMAND...' }}
          </div>
          <div class="voice-response" v-if="aiResponse">
            {{ aiResponse }}
          </div>
        </div>
        
        <div class="voice-commands">
          <div class="command-list">
            <div class="command-item" v-for="(cmd, index) in availableCommands" :key="index">
              <span class="command-keyword">{{ cmd.keyword }}</span>
              <span class="command-desc">{{ cmd.description }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 3D控制面板 -->
      <div class="control-panel">
        <div class="panel-header">
          <span class="panel-title">QUANTUM_CONTROL</span>
        </div>
        
        <div class="control-grid">
          <div class="control-item" @click="toggleDimensionMode">
            <div class="control-icon">🌌</div>
            <div class="control-label">DIMENSION</div>
            <div class="control-value">{{ dimensionMode ? '4D' : '3D' }}</div>
          </div>
          
          <div class="control-item" @click="toggleGravity">
            <div class="control-icon">⚡</div>
            <div class="control-label">GRAVITY</div>
            <div class="control-value">{{ gravityEnabled ? 'ON' : 'OFF' }}</div>
          </div>
          
          <div class="control-item" @click="toggleTimeFlow">
            <div class="control-icon">⏰</div>
            <div class="control-label">TIME_FLOW</div>
            <div class="control-value">{{ timeFlowReversed ? 'REV' : 'FWD' }}</div>
          </div>
          
          <div class="control-item" @click="toggleParticlePhysics">
            <div class="control-icon">🌀</div>
            <div class="control-label">PHYSICS</div>
            <div class="control-value">{{ quantumPhysics ? 'QUANTUM' : 'CLASSICAL' }}</div>
          </div>
        </div>
      </div>

      <!-- 视觉传感器面板 -->
      <div class="sensor-panel" :class="{ active: handTrackingActive }">
        <div class="panel-header">
          <span class="panel-title">VISUAL_SENSOR</span>
          <div class="sensor-status" :class="{ active: handTrackingActive }"></div>
        </div>
        
        <div class="sensor-feed">
          <canvas ref="canvasElement" width="320" height="240"></canvas>
          <div class="sensor-overlay">
            <div class="scan-line" :style="{ top: scanLinePosition + '%' }"></div>
            <div class="targeting-reticle" v-if="handDetected"></div>
          </div>
        </div>
        
        <div class="sensor-data">
          <div class="data-item">
            <span class="data-label">HANDS_DETECTED</span>
            <span class="data-value">{{ handsDetectedCount }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">TRACKING_QUALITY</span>
            <span class="data-value">{{ trackingQuality }}%</span>
          </div>
        </div>
      </div>

      <!-- 系统控制 -->
      <div class="system-controls">
        <div class="control-btn quantum" @click="quantumShift">
          QUANTUM_SHIFT
        </div>
        <div class="control-btn" :class="{ active: handTrackingActive }" @click="toggleHandTracking">
          {{ handTrackingActive ? 'DISABLE_VISION' : 'ENABLE_VISION' }}
        </div>
        <div class="control-btn" :class="{ active: voiceActive }" @click="toggleVoice">
          {{ voiceActive ? 'MUTE_INTERFACE' : 'ACTIVATE_VOICE' }}
        </div>
        <div class="control-btn emergency" @click="systemReset">
          SYSTEM_RESET
        </div>
      </div>

      <!-- 底部状态栏 -->
      <div class="status-footer">
        <div class="footer-grid">
          <div class="footer-item">
            <span class="footer-label">SYSTEM_TIME</span>
            <span class="footer-value">{{ systemTime }}</span>
          </div>
          <div class="footer-item">
            <span class="footer-label">MEMORY_USAGE</span>
            <span class="footer-value">{{ memoryUsage }}MB</span>
          </div>
          <div class="footer-item">
            <span class="footer-label">ENERGY_LEVEL</span>
            <span class="footer-value">{{ energyLevel }}%</span>
          </div>
          <div class="footer-item">
            <span class="footer-label">DIMENSIONAL_STABILITY</span>
            <span class="footer-value">{{ dimensionalStability }}%</span>
          </div>
        </div>
        
        <div class="exit-portal" @click="$router.push('/')">
          <div class="portal-glow"></div>
          <span>EXIT_SIMULATION</span>
        </div>
      </div>
    </div>
    
    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="quantum-loader">
        <div class="loader-ring"></div>
        <div class="loader-core"></div>
        <div class="loading-text">INITIALIZING_NEURAL_INTERFACE...</div>
        <div class="loading-subtext">CALIBRATING_QUANTUM_FIELDS</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'

const container = ref(null)
const canvasElement = ref(null)
const loading = ref(true)

// 系统状态
const handTrackingActive = ref(false)
const voiceActive = ref(false)
const leftHandDetected = ref(false)
const rightHandDetected = ref(false)
const handDetected = ref(false)
const handsDetectedCount = ref(0)
const trackingQuality = ref(0)
const processingLatency = ref(0)
const scanLinePosition = ref(0)

// 手势识别
const currentGestureIcon = ref('👆')
const currentGestureName = ref('IDLE')
const gestureConfidence = ref(0)
const gestureHistory = ref([])

// 语音交互
const voiceTranscript = ref('')
const aiResponse = ref('')
const voiceLevel = ref(0)
const isListening = ref(false)

// 3D控制
const dimensionMode = ref(false)
const gravityEnabled = ref(true)
const timeFlowReversed = ref(false)
const quantumPhysics = ref(false)

// 系统信息
const systemTime = ref('00:00:00')
const memoryUsage = ref(0)
const energyLevel = ref(100)
const dimensionalStability = ref(99.8)

// Three.js 变量
let scene, camera, renderer, controls
let particleSystem
let animationId
let clock = new THREE.Clock()

// MediaPipe 变量
let hands, canvasCtx, videoElement

// 语音识别
let recognition

// 可用命令列表
const availableCommands = [
  { keyword: 'CREATE', description: '生成新的量子结构' },
  { keyword: 'DESTROY', description: '消除当前结构' },
  { keyword: 'TRANSFORM', description: '形态转换' },
  { keyword: 'ANALYZE', description: '系统分析' },
  { keyword: 'ENHANCE', description: '增强视觉效果' },
  { keyword: 'DIMENSION', description: '切换维度模式' },
  { keyword: 'RESET', description: '系统重置' }
]

onMounted(() => {
  initQuantumSystem()
  updateSystemInfo()
  setInterval(updateSystemInfo, 1000)
  setInterval(updateScanLine, 100)
  
  // 模拟加载
  setTimeout(() => {
    loading.value = false
  }, 2000)
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) {
    renderer.dispose()
    container.value.removeChild(renderer.domElement)
  }
  
  if (videoElement && videoElement.srcObject) {
    const tracks = videoElement.srcObject.getTracks()
    tracks.forEach(track => track.stop())
  }
  
  if (recognition) recognition.stop()
})

const initQuantumSystem = () => {
  // 场景设置
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x000010)
  scene.fog = new THREE.FogExp2(0x000010, 0.001)

  // 相机
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.set(0, 0, 50)

  // 渲染器
  renderer = new THREE.WebGLRenderer({ 
    antialias: true, 
    alpha: true,
    powerPreference: "high-performance"
  })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  container.value.appendChild(renderer.domElement)

  // 控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.autoRotate = true
  controls.autoRotateSpeed = 0.5

  // 创建量子粒子系统
  createQuantumParticles()

  // 环境光
  const ambientLight = new THREE.AmbientLight(0x404040, 0.4)
  scene.add(ambientLight)

  // 方向光
  const directionalLight = new THREE.DirectionalLight(0x00f2ff, 1)
  directionalLight.position.set(50, 50, 50)
  directionalLight.castShadow = true
  scene.add(directionalLight)

  // 开始动画循环
  animate()
}

const createQuantumParticles = () => {
  const particleCount = 5000
  const positions = new Float32Array(particleCount * 3)
  const colors = new Float32Array(particleCount * 3)
  const sizes = new Float32Array(particleCount)

  const color = new THREE.Color()

  for (let i = 0; i < particleCount; i++) {
    // 球体分布
    const theta = Math.random() * Math.PI * 2
    const phi = Math.acos(Math.random() * 2 - 1)
    const radius = 20 + Math.random() * 10

    const x = radius * Math.sin(phi) * Math.cos(theta)
    const y = radius * Math.sin(phi) * Math.sin(theta)
    const z = radius * Math.cos(phi)

    positions[i * 3] = x
    positions[i * 3 + 1] = y
    positions[i * 3 + 2] = z

    // 颜色 - 量子蓝色调
    const hue = 0.6 + Math.random() * 0.2
    color.setHSL(hue, 0.8, 0.6)
    colors[i * 3] = color.r
    colors[i * 3 + 1] = color.g
    colors[i * 3 + 2] = color.b

    // 大小
    sizes[i] = Math.random() * 2 + 0.5
  }

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
  geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1))

  // 创建粒子材质
  const material = new THREE.PointsMaterial({
    size: 2,
    vertexColors: true,
    transparent: true,
    opacity: 0.8,
    sizeAttenuation: true,
    blending: THREE.AdditiveBlending
  })

  particleSystem = new THREE.Points(geometry, material)
  scene.add(particleSystem)
}

const animate = () => {
  animationId = requestAnimationFrame(animate)

  const time = clock.getElapsedTime()
  const delta = clock.getDelta()

  // 更新粒子系统
  if (particleSystem) {
    const positions = particleSystem.geometry.attributes.position.array
    const originalPositions = particleSystem.geometry.attributes.position.originalPositions || 
                             [...positions]

    if (!particleSystem.geometry.attributes.position.originalPositions) {
      particleSystem.geometry.attributes.position.originalPositions = [...positions]
    }

    for (let i = 0; i < positions.length; i += 3) {
      const originalX = originalPositions[i]
      const originalY = originalPositions[i + 1]
      const originalZ = originalPositions[i + 2]

      // 量子波动效果
      const waveX = Math.sin(time * 0.5 + originalX * 0.1) * 0.5
      const waveY = Math.cos(time * 0.3 + originalY * 0.1) * 0.5
      const waveZ = Math.sin(time * 0.7 + originalZ * 0.1) * 0.5

      positions[i] = originalX + waveX
      positions[i + 1] = originalY + waveY
      positions[i + 2] = originalZ + waveZ
    }

    particleSystem.geometry.attributes.position.needsUpdate = true
    particleSystem.rotation.y += delta * 0.1
  }

  controls.update()
  renderer.render(scene, camera)
}

const updateSystemInfo = () => {
  const now = new Date()
  systemTime.value = now.toLocaleTimeString('en-US', {
    hour12: false
  })
  memoryUsage.value = Math.floor(Math.random() * 500 + 100)
  energyLevel.value = Math.max(0, energyLevel.value - 0.1)
  dimensionalStability.value = 99.8 + Math.random() * 0.4 - 0.2
}

const updateScanLine = () => {
  scanLinePosition.value = (scanLinePosition.value + 2) % 100
}

// 手势识别功能
const toggleHandTracking = async () => {
  if (handTrackingActive.value) {
    // 停止追踪
    if (videoElement && videoElement.srcObject) {
      const tracks = videoElement.srcObject.getTracks()
      tracks.forEach(track => track.stop())
    }
    handTrackingActive.value = false
    leftHandDetected.value = false
    rightHandDetected.value = false
    handDetected.value = false
    handsDetectedCount.value = 0
    trackingQuality.value = 0
  } else {
    // 开始追踪
    await initMediaPipe()
    handTrackingActive.value = true
  }
}

const initMediaPipe = async () => {
  // 动态加载 MediaPipe Hands 库
  if (!window.Hands) {
    try {
      // 加载 MediaPipe Hands 库
      const script = document.createElement('script')
      script.src = 'https://cdn.jsdelivr.net/npm/@mediapipe/hands/hands.js'
      script.onload = () => {
        console.log('MediaPipe Hands loaded successfully')
        initializeHands()
      }
      script.onerror = () => {
        console.error('Failed to load MediaPipe Hands')
        alert('无法加载 MediaPipe Hands 库，请检查网络连接')
        handTrackingActive.value = false
      }
      document.head.appendChild(script)
    } catch (err) {
      console.error('Error loading MediaPipe:', err)
      alert('加载 MediaPipe 时出错: ' + err.message)
      handTrackingActive.value = false
    }
    return
  }
  
  initializeHands()
}

const initializeHands = async () => {
  try {
    hands = new window.Hands({
      locateFile: (file) => {
        return `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}`
      }
    })
  
    hands.setOptions({
      maxNumHands: 2,
      modelComplexity: 1,
      minDetectionConfidence: 0.7,
      minTrackingConfidence: 0.5
    })
  
    hands.onResults(onHandResults)
  
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ 
        video: { width: 320, height: 240 } 
      })
      
      videoElement = document.createElement('video')
      videoElement.srcObject = stream
      await videoElement.play()
      
      canvasCtx = canvasElement.value.getContext('2d')
      
      // 开始处理循环
      const processVideo = async () => {
        if (!handTrackingActive.value) return
        
        if (videoElement.readyState === 4) {
          await hands.send({ image: videoElement })
        }
        requestAnimationFrame(processVideo)
      }
      
      processVideo()
    } catch (err) {
      console.error('Error accessing camera:', err)
      alert('Error accessing camera: ' + err.message)
      handTrackingActive.value = false
    }
  } catch (err) {
    console.error('Error initializing MediaPipe Hands:', err)
    alert('初始化 MediaPipe Hands 时出错: ' + err.message)
    handTrackingActive.value = false
  }
}

const onHandResults = (results) => {
  const startTime = performance.now()
  
  // 绘制到画布
  if (canvasCtx) {
    canvasCtx.save()
    canvasCtx.clearRect(0, 0, canvasElement.value.width, canvasElement.value.height)
    canvasCtx.drawImage(results.image, 0, 0, canvasElement.value.width, canvasElement.value.height)
    
    if (results.multiHandLandmarks) {
      for (const landmarks of results.multiHandLandmarks) {
        // 绘制手部骨架
        window.drawConnectors(canvasCtx, landmarks, window.HAND_CONNECTIONS,
          { color: '#00f2ff', lineWidth: 2 })
        window.drawLandmarks(canvasCtx, landmarks, 
          { color: '#ff0000', lineWidth: 1, radius: 3 })
      }
    }
    canvasCtx.restore()
  }

  // 处理手势识别
  if (results.multiHandLandmarks && results.multiHandLandmarks.length > 0) {
    handsDetectedCount.value = results.multiHandLandmarks.length
    handDetected.value = true
    trackingQuality.value = 85 + Math.random() * 15
    
    // 检测左右手
    leftHandDetected.value = results.multiHandedness?.some(hand => hand.label === 'Left') || false
    rightHandDetected.value = results.multiHandedness?.some(hand => hand.label === 'Right') || false
    
    // 分析手势
    analyzeGestures(results.multiHandLandmarks[0])
  } else {
    handDetected.value = false
    leftHandDetected.value = false
    rightHandDetected.value = false
    handsDetectedCount.value = 0
    trackingQuality.value = 0
    currentGestureName.value = 'IDLE'
    currentGestureIcon.value = '👆'
    gestureConfidence.value = 0
  }
  
  processingLatency.value = Math.round(performance.now() - startTime)
}

const analyzeGestures = (landmarks) => {
  // 检测捏合手势 (拇指和食指)
  const thumbTip = landmarks[4]
  const indexTip = landmarks[8]
  const pinchDistance = Math.sqrt(
    Math.pow(indexTip.x - thumbTip.x, 2) + 
    Math.pow(indexTip.y - thumbTip.y, 2)
  )
  
  // 检测挥手手势 (手腕移动)
  const wrist = landmarks[0]
  const middleTip = landmarks[12]
  const waveDistance = Math.sqrt(
    Math.pow(middleTip.x - wrist.x, 2) + 
    Math.pow(middleTip.y - wrist.y, 2)
  )
  
  // 检测握拳手势
  const allFingersExtended = [8, 12, 16, 20].every(index => {
    const tip = landmarks[index]
    const base = landmarks[index - 3]
    return Math.abs(tip.y - base.y) > 0.1
  })
  
  if (pinchDistance < 0.05) {
    currentGestureName.value = 'PINCH'
    currentGestureIcon.value = '🤏'
    gestureConfidence.value = 95
    handlePinchGesture()
  } else if (waveDistance > 0.3) {
    currentGestureName.value = 'WAVE'
    currentGestureIcon.value = '👋'
    gestureConfidence.value = 85
    handleWaveGesture()
  } else if (!allFingersExtended) {
    currentGestureName.value = 'FIST'
    currentGestureIcon.value = '✊'
    gestureConfidence.value = 90
    handleFistGesture()
  } else {
    currentGestureName.value = 'OPEN_HAND'
    currentGestureIcon.value = '🖐️'
    gestureConfidence.value = 80
    handleOpenHandGesture()
  }
  
  // 添加到历史记录
  gestureHistory.value.unshift({
    icon: currentGestureIcon.value,
    type: currentGestureName.value.toLowerCase(),
    timestamp: Date.now()
  })
  
  if (gestureHistory.value.length > 5) {
    gestureHistory.value = gestureHistory.value.slice(0, 5)
  }
}

const handlePinchGesture = () => {
  // 捏合手势 - 缩放粒子
  if (particleSystem) {
    particleSystem.scale.multiplyScalar(0.95)
    setTimeout(() => {
      particleSystem.scale.multiplyScalar(1.05)
    }, 200)
  }
}

const handleWaveGesture = () => {
  // 挥手手势 - 旋转加速
  controls.autoRotateSpeed = 2.0
  setTimeout(() => {
    controls.autoRotateSpeed = 0.5
  }, 1000)
}

const handleFistGesture = () => {
  // 握拳手势 - 粒子爆炸效果
  if (particleSystem) {
    const positions = particleSystem.geometry.attributes.position.array
    for (let i = 0; i < positions.length; i += 3) {
      positions[i] += (Math.random() - 0.5) * 2
      positions[i + 1] += (Math.random() - 0.5) * 2
      positions[i + 2] += (Math.random() - 0.5) * 2
    }
    particleSystem.geometry.attributes.position.needsUpdate = true
  }
}

const handleOpenHandGesture = () => {
  // 张开手势 - 重置粒子位置
  if (particleSystem && particleSystem.geometry.attributes.position.originalPositions) {
    const positions = particleSystem.geometry.attributes.position.array
    const originalPositions = particleSystem.geometry.attributes.position.originalPositions
    
    for (let i = 0; i < positions.length; i++) {
      positions[i] = originalPositions[i]
    }
    particleSystem.geometry.attributes.position.needsUpdate = true
  }
}

// 语音交互功能
const toggleVoice = () => {
  if (voiceActive.value) {
    if (recognition) recognition.stop()
    voiceActive.value = false
    isListening.value = false
  } else {
    initVoice()
  }
}

const initVoice = () => {
  if (!('webkitSpeechRecognition' in window)) {
    alert('Browser does not support Web Speech API')
    return
  }
  
  recognition = new window.webkitSpeechRecognition()
  recognition.continuous = true
  recognition.interimResults = true
  recognition.lang = 'en-US'
  
  recognition.onstart = () => {
    voiceActive.value = true
    isListening.value = true
  }
  
  recognition.onend = () => {
    isListening.value = false
    if (voiceActive.value) {
      setTimeout(() => recognition.start(), 100)
    }
  }
  
  recognition.onresult = (event) => {
    let finalTranscript = ''
    let interimTranscript = ''
    
    for (let i = event.resultIndex; i < event.results.length; i++) {
      const transcript = event.results[i][0].transcript
      if (event.results[i].isFinal) {
        finalTranscript += transcript
      } else {
        interimTranscript += transcript
      }
    }
    
    voiceTranscript.value = interimTranscript || finalTranscript
    voiceLevel.value = Math.random() * 30 + 10
    
    if (finalTranscript) {
      processVoiceCommand(finalTranscript.trim().toLowerCase())
    }
  }
  
  recognition.start()
}

const processVoiceCommand = (command) => {
  console.log('Processing voice command:', command)
  
  // 模拟AI响应
  const responses = [
    "COMMAND_ACKNOWLEDGED. EXECUTING_QUANTUM_SHIFT.",
    "NEURAL_INTERFACE_ACTIVATED. PROCESSING_REQUEST.",
    "DIMENSIONAL_REALIGNMENT_IN_PROGRESS.",
    "QUANTUM_FIELD_STABILIZED. READY_FOR_NEXT_COMMAND.",
    "SYSTEM_OPTIMIZED. ALL_PARAMETERS_NOMINAL."
  ]
  
  aiResponse.value = responses[Math.floor(Math.random() * responses.length)]
  
  // 处理具体命令
  if (command.includes('create') || command.includes('generate')) {
    quantumShift()
  } else if (command.includes('destroy') || command.includes('eliminate')) {
    systemReset()
  } else if (command.includes('transform') || command.includes('change')) {
    toggleDimensionMode()
  } else if (command.includes('analyze') || command.includes('scan')) {
    analyzeSystem()
  } else if (command.includes('enhance') || command.includes('boost')) {
    enhanceVisuals()
  } else if (command.includes('dimension') || command.includes('4d')) {
    toggleDimensionMode()
  } else if (command.includes('reset') || command.includes('reboot')) {
    systemReset()
  }
  
  setTimeout(() => {
    aiResponse.value = ''
  }, 3000)
}

// 系统控制功能
const toggleDimensionMode = () => {
  dimensionMode.value = !dimensionMode.value
  if (particleSystem) {
    particleSystem.material.opacity = dimensionMode.value ? 0.6 : 0.8
  }
}

const toggleGravity = () => {
  gravityEnabled.value = !gravityEnabled.value
}

const toggleTimeFlow = () => {
  timeFlowReversed.value = !timeFlowReversed.value
  if (particleSystem) {
    particleSystem.rotation.y *= -1
  }
}

const toggleParticlePhysics = () => {
  quantumPhysics.value = !quantumPhysics.value
}

const quantumShift = () => {
  // 量子位移效果
  if (particleSystem) {
    const positions = particleSystem.geometry.attributes.position.array
    for (let i = 0; i < positions.length; i += 3) {
      positions[i] += (Math.random() - 0.5) * 10
      positions[i + 1] += (Math.random() - 0.5) * 10
      positions[i + 2] += (Math.random() - 0.5) * 10
    }
    particleSystem.geometry.attributes.position.needsUpdate = true
  }
  
  // 视觉反馈
  document.body.style.filter = 'hue-rotate(180deg)'
  setTimeout(() => {
    document.body.style.filter = 'none'
  }, 500)
}

const systemReset = () => {
  // 重置系统
  if (particleSystem && particleSystem.geometry.attributes.position.originalPositions) {
    const positions = particleSystem.geometry.attributes.position.array
    const originalPositions = particleSystem.geometry.attributes.position.originalPositions
    
    for (let i = 0; i < positions.length; i++) {
      positions[i] = originalPositions[i]
    }
    particleSystem.geometry.attributes.position.needsUpdate = true
  }
  
  camera.position.set(0, 0, 50)
  controls.reset()
}

const analyzeSystem = () => {
  // 系统分析
  console.log('System analysis initiated')
}

const enhanceVisuals = () => {
  // 增强视觉效果
  if (particleSystem) {
    particleSystem.material.size *= 1.2
    setTimeout(() => {
      particleSystem.material.size /= 1.2
    }, 1000)
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Share+Tech+Mono&display=swap');

.virtual-world-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: #050510;
  overflow: hidden;
  font-family: 'Share Tech Mono', monospace;
  color: #00f2ff;
  z-index: 2000;
}

/* HUD Overlay */
.hud-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  padding: 1rem;
  box-sizing: border-box;
  background: radial-gradient(circle at center, transparent 50%, rgba(0, 10, 30, 0.8) 100%);
}

.hud-overlay > * {
  pointer-events: auto;
}

/* System Status Panel */
.system-status-panel {
  position: absolute;
  top: 1rem;
  left: 1rem;
  background: rgba(0, 10, 30, 0.8);
  border: 1px solid rgba(0, 242, 255, 0.3);
  border-radius: 4px;
  padding: 1rem;
  backdrop-filter: blur(5px);
}

.status-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.status-label {
  font-size: 0.7rem;
  color: rgba(0, 242, 255, 0.6);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.status-value {
  font-size: 0.9rem;
  color: #fff;
}

.status-value.active {
  color: #00ff00;
  text-shadow: 0 0 5px #00ff00;
}

.status-bar {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #00f2ff, #00ff00);
  transition: width 0.3s ease;
}

/* Gesture Panel */
.gesture-panel {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(0, 10, 30, 0.8);
  border: 1px solid rgba(0, 242, 255, 0.3);
  border-radius: 4px;
  padding: 1rem;
  backdrop-filter: blur(5px);
  opacity: 0.5;
  transform: scale(0.9);
  transition: all 0.3s ease;
}

.gesture-panel.active {
  opacity: 1;
  transform: scale(1);
  border-color: #00f2ff;
  box-shadow: 0 0 15px rgba(0, 242, 255, 0.2);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 242, 255, 0.2);
  padding-bottom: 0.5rem;
}

.panel-title {
  font-size: 0.8rem;
  color: #00f2ff;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.panel-indicators {
  display: flex;
  gap: 0.5rem;
}

.indicator {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  color: rgba(255, 255, 255, 0.5);
}

.indicator.active {
  background: #00f2ff;
  color: #000;
  box-shadow: 0 0 10px #00f2ff;
}

.gesture-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.current-gesture {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.gesture-icon {
  font-size: 2rem;
}

.gesture-name {
  font-size: 0.9rem;
  color: #fff;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.gesture-history {
  display: flex;
  gap: 0.5rem;
}

.history-item {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.gesture-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
}

.stat-label {
  font-size: 0.7rem;
  color: rgba(0, 242, 255, 0.6);
}

.stat-value {
  font-size: 0.9rem;
  color: #fff;
}

/* Voice Panel */
.voice-panel {
  position: absolute;
  bottom: 5rem;
  left: 1rem;
  background: rgba(0, 10, 30, 0.8);
  border: 1px solid rgba(0, 242, 255, 0.3);
  border-radius: 4px;
  padding: 1rem;
  backdrop-filter: blur(5px);
  opacity: 0.5;
  transform: scale(0.9);
  transition: all 0.3s ease;
  width: 300px;
}

.voice-panel.active {
  opacity: 1;
  transform: scale(1);
  border-color: #00f2ff;
  box-shadow: 0 0 15px rgba(0, 242, 255, 0.2);
}

.voice-level {
  width: 4px;
  background: #00f2ff;
  border-radius: 2px;
  transition: height 0.1s ease;
}

.voice-display {
  margin-bottom: 1rem;
}

.voice-text {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  min-height: 1.5rem;
  border-bottom: 1px solid rgba(0, 242, 255, 0.2);
  padding-bottom: 0.5rem;
}

.voice-text.listening {
  color: #00ff00;
  text-shadow: 0 0 5px #00ff00;
}

.voice-response {
  font-size: 0.8rem;
  color: #00f2ff;
  margin-top: 0.5rem;
  font-style: italic;
}

.command-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.command-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.3rem 0;
  border-bottom: 1px solid rgba(0, 242, 255, 0.1);
}

.command-keyword {
  color: #00f2ff;
  font-weight: bold;
  font-size: 0.8rem;
}

.command-desc {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.7rem;
}

/* Control Panel */
.control-panel {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 10, 30, 0.8);
  border: 1px solid rgba(0, 242, 255, 0.3);
  border-radius: 4px;
  padding: 1rem;
  backdrop-filter: blur(5px);
}

.control-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.control-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  border: 1px solid rgba(0, 242, 255, 0.3);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.control-item:hover {
  border-color: #00f2ff;
  background: rgba(0, 242, 255, 0.1);
}

.control-icon {
  font-size: 1.5rem;
}

.control-label {
  font-size: 0.8rem;
  color: #00f2ff;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.control-value {
  font-size: 0.9rem;
  color: #fff;
}

/* Sensor Panel */
.sensor-panel {
  position: absolute;
  bottom: 5rem;
  right: 1rem;
  background: rgba(0, 10, 30, 0.8);
  border: 1px solid rgba(0, 242, 255, 0.3);
  border-radius: 4px;
  padding: 1rem;
  backdrop-filter: blur(5px);
  opacity: 0.5;
  transform: scale(0.9);
  transition: all 0.3s ease;
}

.sensor-panel.active {
  opacity: 1;
  transform: scale(1);
  border-color: #00f2ff;
  box-shadow: 0 0 15px rgba(0, 242, 255, 0.2);
}

.sensor-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #555;
}

.sensor-status.active {
  background: #00ff00;
  box-shadow: 0 0 5px #00ff00;
}

.sensor-feed {
  position: relative;
  width: 240px;
  height: 180px;
  background: #000;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 1rem;
}

.sensor-feed canvas {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sensor-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.scan-line {
  position: absolute;
  left: 0;
  width: 100%;
  height: 2px;
  background: #00f2ff;
  box-shadow: 0 0 10px #00f2ff;
}

.targeting-reticle {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  border: 2px solid #00ff00;
  border-radius: 50%;
  box-shadow: 0 0 10px #00ff00;
}

.sensor-data {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.data-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
}

.data-label {
  font-size: 0.7rem;
  color: rgba(0, 242, 255, 0.6);
}

.data-value {
  font-size: 0.9rem;
  color: #fff;
}

/* System Controls */
.system-controls {
  position: absolute;
  bottom: 1rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 1rem;
  z-index: 1000;
}

.control-btn {
  padding: 0.8rem 1.5rem;
  background: rgba(0, 242, 255, 0.1);
  border: 1px solid #00f2ff;
  border-radius: 4px;
  color: #00f2ff;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.control-btn:hover {
  background: #00f2ff;
  color: #000;
  box-shadow: 0 0 15px #00f2ff;
}

.control-btn.active {
  background: #00f2ff;
  color: #000;
}

.control-btn.quantum {
  border-color: #ff00ff;
  color: #ff00ff;
  background: rgba(255, 0, 255, 0.1);
}

.control-btn.quantum:hover {
  background: #ff00ff;
  color: #000;
  box-shadow: 0 0 15px #ff00ff;
}

.control-btn.emergency {
  border-color: #ff4757;
  color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.control-btn.emergency:hover {
  background: #ff4757;
  color: #fff;
  box-shadow: 0 0 15px #ff4757;
}

/* Status Footer */
.status-footer {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  right: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2rem;
}

.footer-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
}

.footer-label {
  font-size: 0.7rem;
  color: rgba(0, 242, 255, 0.6);
}

.footer-value {
  font-size: 0.9rem;
  color: #fff;
}

.exit-portal {
  position: relative;
  padding: 0.8rem 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(0, 0, 0, 0.5);
}

.exit-portal:hover {
  border-color: #00f2ff;
  color: #00f2ff;
  text-shadow: 0 0 5px #00f2ff;
}

.portal-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 4px;
  background: radial-gradient(circle at center, rgba(0, 242, 255, 0.2), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.exit-portal:hover .portal-glow {
  opacity: 1;
}

/* Loading Overlay */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #000;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 3000;
}

.quantum-loader {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.loader-ring {
  width: 80px;
  height: 80px;
  border: 4px solid transparent;
  border-top: 4px solid #00f2ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loader-core {
  width: 40px;
  height: 40px;
  background: #00f2ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f2ff;
  animation: pulse 2s ease-in-out infinite;
}

.loading-text {
  font-size: 1.2rem;
  letter-spacing: 3px;
  animation: blink 0.5s infinite;
}

.loading-subtext {
  font-size: 0.8rem;
  color: rgba(0, 242, 255, 0.6);
  letter-spacing: 2px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.7; }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* Responsive */
@media (max-width: 768px) {
  .hud-overlay {
    padding: 0.5rem;
  }
  
  .system-status-panel,
  .gesture-panel,
  .voice-panel,
  .sensor-panel {
    position: relative;
    top: auto;
    left: auto;
    right: auto;
    bottom: auto;
    margin-bottom: 1rem;
    width: 100%;
  }
  
  .control-panel {
    position: relative;
    top: auto;
    left: auto;
    transform: none;
    margin: 1rem 0;
  }
  
  .system-controls {
    position: relative;
    bottom: auto;
    left: auto;
    transform: none;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .status-footer {
    position: relative;
    bottom: auto;
    flex-direction: column;
    gap: 1rem;
  }
  
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }
}
</style>
