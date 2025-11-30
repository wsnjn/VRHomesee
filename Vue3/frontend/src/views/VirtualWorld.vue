<template>
  <div class="virtual-world-container" @mousemove="onMouseMove">
    <!-- 背景摄像头画面 -->
    <div class="video-wrapper">
      <video ref="videoElement" class="bg-video" autoplay playsinline muted></video>
      <div class="video-overlay"></div>
    </div>
    
    <!-- Three.js 渲染层 -->
    <div ref="container" class="three-container"></div>

    <!-- 全息HUD界面 -->
    <div class="hud-overlay" :class="{ 'warp-mode': isWarping }">
      <!-- 顶部状态栏 -->
      <div class="hud-header">
        <div class="hud-title">
          <span class="glitch-text">DEEP_SPACE_ODYSSEY</span>
          <div class="hud-line"></div>
        </div>
        <div class="hud-status">
          <div class="status-item">
            <span class="label">VELOCITY</span>
            <span class="value">{{ (currentSpeed * 299792).toFixed(0) }} km/s</span>
            <div class="bar-container">
              <div class="bar-fill" :style="{ width: (currentSpeed / (maxSpeed * 5) * 100) + '%' }"></div>
            </div>
          </div>
          <div class="status-item">
            <span class="label">COORDINATES</span>
            <span class="value">SEC-{{ currentSector }} [{{ cameraPos.x.toFixed(0) }}, {{ cameraPos.y.toFixed(0) }}, {{ cameraPos.z.toFixed(0) }}]</span>
          </div>
          <div class="status-item">
            <span class="label">WARP_DRIVE</span>
            <span class="value" :class="{ ready: warpCharge >= 100, charging: warpCharge > 0 && warpCharge < 100 }">
              {{ warpCharge >= 100 ? 'READY' : (warpCharge > 0 ? 'CHARGING ' + warpCharge.toFixed(0) + '%' : 'OFFLINE') }}
            </span>
          </div>
        </div>
      </div>

      <!-- 警告/通知中心 -->
      <div class="notification-center" v-if="notification">
        <div class="notification-box" :class="notification.type">
          {{ notification.text }}
        </div>
      </div>

      <!-- 右手跟随信息面板 (扫描结果) -->
      <div 
        v-if="rightHandDetected && !isWarping"
        class="floating-panel"
        :style="{ top: rightHandPos.y + 'px', left: rightHandPos.x + 'px' }"
      >
        <div class="panel-content">
          <div class="panel-header-line">
            <span class="panel-id">SCANNER_V4</span>
            <span class="panel-icon">🔭</span>
          </div>
          
          <div v-if="scannedObject" class="scan-result">
            <div class="object-name">{{ scannedObject.name }}</div>
            <div class="panel-grid">
              <div class="grid-item">
                <span class="label">CLASS</span>
                <span class="value">{{ scannedObject.type }}</span>
              </div>
              <div class="grid-item">
                <span class="label">DIAMETER</span>
                <span class="value">{{ scannedObject.diameter }} km</span>
              </div>
              <div class="grid-item">
                <span class="label">GRAVITY</span>
                <span class="value">{{ scannedObject.gravity }} G</span>
              </div>
              <div class="grid-item">
                <span class="label">DISTANCE</span>
                <span class="value">{{ scannedObject.distance }} km</span>
              </div>
            </div>
            <div class="landing-status">
              <div class="status-row">
                <span>LANDING_ANALYSIS</span>
                <span :class="scannedObject.habitable ? 'success' : 'danger'">
                  {{ scannedObject.habitable ? 'POSSIBLE' : 'HAZARDOUS' }}
                </span>
              </div>
              <div class="prob-bar">
                <div class="prob-fill" :class="scannedObject.habitable ? 'success' : 'danger'" :style="{ width: scannedObject.landingProb + '%' }"></div>
              </div>
            </div>
          </div>
          
          <div v-else class="scan-scanning">
            <div v-if="rightHandGesture === 'FIST'" class="warp-charging-ui">
              <div class="warp-text">WARP DRIVE CHARGING</div>
              <div class="warp-bar">
                <div class="warp-fill" :style="{ width: warpCharge + '%' }"></div>
              </div>
            </div>
            <div v-else>
              SCANNING SECTOR...
              <div class="scanner-line"></div>
            </div>
          </div>
        </div>
        <div class="connecting-line"></div>
      </div>

      <!-- 左手控制指示器 (飞行控制) -->
      <div 
        v-if="leftHandDetected"
        class="control-indicator"
        :style="{ top: leftHandPos.y + 'px', left: leftHandPos.x + 'px' }"
      >
        <div class="pilot-hud">
          <div class="thrust-status" :class="{ active: !isPaused, boost: isBoosting }">
            {{ isPaused ? 'ENGINES_PAUSED' : (isBoosting ? 'BOOST_ENGAGED' : 'CRUISING') }}
          </div>
          <div class="gesture-hint">
            {{ leftHandGesture === 'OK' ? '👌 BOOST' : (leftHandGesture === 'OPEN' ? '🖐️ CRUISE' : '✊ BRAKE') }}
          </div>
        </div>
      </div>

      <!-- 鼠标控制提示 -->
      <div class="mouse-hint" v-if="!leftHandDetected">
        MOUSE STEERING ACTIVE
      </div>

      <!-- 语音反馈 -->
      <div class="voice-feedback" :class="{ active: voiceActive }">
        <div class="voice-wave">
          <div class="bar" v-for="n in 5" :key="n" :style="{ animationDelay: n * 0.1 + 's' }"></div>
        </div>
        <div class="voice-text">{{ voiceTranscript || 'AWAITING_COMMAND...' }}</div>
        <div class="ai-response" v-if="aiResponse">{{ aiResponse }}</div>
      </div>

      <!-- 底部操作栏 -->
      <div class="hud-footer">
        <div class="control-btn" @click="toggleHandTracking">
          {{ handTrackingActive ? 'DISENGAGE_SYSTEMS' : 'ENGAGE_SYSTEMS' }}
        </div>
        <div class="control-btn" @click="toggleVoice">
          {{ voiceActive ? 'MUTE_COMMS' : 'OPEN_COMMS' }}
        </div>
        <div class="control-btn exit" @click="$router.push('/')">
          ABORT_MISSION
        </div>
      </div>
    </div>
    
    <!-- 加载动画 -->
    <div v-if="loading" class="loading-overlay">
      <div class="arc-reactor">
        <div class="circle outer"></div>
        <div class="circle inner"></div>
        <div class="core"></div>
      </div>
      <div class="loading-text">GENERATING PROCEDURAL UNIVERSE...</div>
      <div class="loading-subtext">BUILDING GALAXIES & PLANETARY SYSTEMS</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as THREE from 'three'

// DOM 引用
const container = ref(null)
const videoElement = ref(null)

// 状态
const loading = ref(true)
const handTrackingActive = ref(false)
const voiceActive = ref(false)
const leftHandDetected = ref(false)
const rightHandDetected = ref(false)
const leftHandPos = ref({ x: 0, y: 0 })
const rightHandPos = ref({ x: 0, y: 0 })
const leftHandGesture = ref('OPEN') // OPEN, FIST, OK
const rightHandGesture = ref('OPEN') // OPEN, FIST
const scannedObject = ref(null)
const voiceTranscript = ref('')
const aiResponse = ref('')
const notification = ref(null)

// 飞行物理
const cameraPos = ref({ x: 0, y: 0, z: 0 })
const currentSpeed = ref(0)
const baseSpeed = 0.5 // 标准巡航速度
const boostSpeed = 5.0 // OK手势加速
const maxSpeed = baseSpeed
const isPaused = ref(false)
const isBoosting = ref(false)
const isWarping = ref(false)
const warpCharge = ref(0)
const currentSector = ref(1)

// 转向控制
const mousePos = ref({ x: 0, y: 0 })
const targetRotation = ref({ x: 0, y: 0 })

// Three.js
let scene, camera, renderer
let stars, galaxyPoints
let planets = [], asteroids = []
let raycaster = new THREE.Raycaster()
let mouse = new THREE.Vector2()
let animationId

// MediaPipe
let hands

// 语音
let recognition

onMounted(async () => {
  initThree()
  animate()
  setTimeout(() => { loading.value = false }, 3000)
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) renderer.dispose()
  if (videoElement.value && videoElement.value.srcObject) {
    const tracks = videoElement.value.srcObject.getTracks()
    tracks.forEach(t => t.stop())
  }
  if (recognition) recognition.stop()
})

// --- 程序化生成工具 ---
const createPlanetTexture = (type) => {
  const canvas = document.createElement('canvas')
  canvas.width = 2048 // 更高分辨率
  canvas.height = 1024
  const ctx = canvas.getContext('2d')
  
  // 基础颜色
  let baseColor, noiseColor
  if (type === 'terran') { baseColor = '#1a237e'; noiseColor = '#4caf50'; }
  else if (type === 'desert') { baseColor = '#e65100'; noiseColor = '#ffcc80'; }
  else if (type === 'ice') { baseColor = '#e0f7fa'; noiseColor = '#ffffff'; }
  else if (type === 'gas') { baseColor = '#311b92'; noiseColor = '#b388ff'; }
  else { baseColor = '#212121'; noiseColor = '#757575'; }

  ctx.fillStyle = baseColor
  ctx.fillRect(0, 0, 2048, 1024)

  // 细节噪点
  for (let i = 0; i < 10000; i++) {
    const x = Math.random() * 2048
    const y = Math.random() * 1024
    const r = Math.random() * 10 + 2
    ctx.beginPath()
    ctx.arc(x, y, r, 0, Math.PI * 2)
    ctx.fillStyle = noiseColor
    ctx.globalAlpha = 0.05
    ctx.fill()
  }
  
  return new THREE.CanvasTexture(canvas)
}

const createGalaxy = () => {
  const particleCount = 50000 // 更多星星
  const geometry = new THREE.BufferGeometry()
  const positions = new Float32Array(particleCount * 3)
  const colors = new Float32Array(particleCount * 3)
  const color = new THREE.Color()

  for (let i = 0; i < particleCount; i++) {
    const i3 = i * 3
    const radius = Math.random() * 100000 + 20000 // 真正巨大的星系
    const spinAngle = radius * 0.0001 // 较慢的自转以适应规模
    const branchAngle = (i % 5) * ((Math.PI * 2) / 5)
    
    const randomX = (Math.random() - 0.5) * 2000
    const randomY = (Math.random() - 0.5) * 1000
    const randomZ = (Math.random() - 0.5) * 2000

    positions[i3] = Math.cos(branchAngle + spinAngle) * radius + randomX
    positions[i3 + 1] = randomY
    positions[i3 + 2] = Math.sin(branchAngle + spinAngle) * radius + randomZ

    const mixedColor = color.setHSL(0.6 + Math.random() * 0.2, 0.8, 0.8)
    colors[i3] = color.r
    colors[i3 + 1] = color.g
    colors[i3 + 2] = color.b
  }

  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))

  const material = new THREE.PointsMaterial({
    size: 2,
    vertexColors: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    transparent: true,
    opacity: 0.6
  })

  const galaxy = new THREE.Points(geometry, material)
  galaxy.position.z = -5000 // 非常远
  return galaxy
}

// --- Three.js 设置 ---
const initThree = () => {
  scene = new THREE.Scene()
  scene.fog = new THREE.FogExp2(0x000000, 0.0002) // 减少雾气以体现广阔感

  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 50000) // 巨大的远裁剪面
  camera.position.set(0, 0, 100)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(window.devicePixelRatio)
  container.value.appendChild(renderer.domElement)

  // 灯光
  const ambientLight = new THREE.AmbientLight(0x808080, 4.0) // 更亮的环境光
  scene.add(ambientLight)
  
  // 太阳光 (定向光以在巨大物体上产生更好的阴影)
  const sunLight = new THREE.DirectionalLight(0xffffff, 5.0)
  sunLight.position.set(1, 0.5, 1)
  scene.add(sunLight)
  
  // 添加微妙的背光用于边缘照明
  const backLight = new THREE.DirectionalLight(0x5050ff, 0.5)
  backLight.position.set(-1, 0, -1)
  scene.add(backLight)

  // 1. 无限星空 (静态背景)
  const starGeo = new THREE.BufferGeometry()
  const starPos = new Float32Array(50000 * 3)
  // 将星星散布在整个星系体积中
  for(let i=0; i<150000; i++) starPos[i] = (Math.random() - 0.5) * 1000000 
  starGeo.setAttribute('position', new THREE.BufferAttribute(starPos, 3))
  stars = new THREE.Points(starGeo, new THREE.PointsMaterial({color: 0xffffff, size: 20})) // 更大的星星以适应距离
  scene.add(stars)

  // 2. 星系 (视觉核心)
  galaxyPoints = createGalaxy()
  scene.add(galaxyPoints)

  // 3. 生成静态宇宙 (固定行星)
  generateUniverse()

  window.addEventListener('resize', onWindowResize)
}

const generateUniverse = () => {
  const types = ['terran', 'desert', 'ice', 'gas', 'lava']
  const universeRadius = 500000 // 巨大规模
  const planetCount = 100 // 主要行星数量
  
  for (let i = 0; i < planetCount; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    // 巨大行星: 1000 - 5000 半径
    const size = Math.random() * 4000 + 1000 
    const geometry = new THREE.SphereGeometry(size, 128, 128)
    const texture = createPlanetTexture(type)
    
    const material = new THREE.MeshStandardMaterial({
      map: texture,
      roughness: 0.6,
      metalness: 0.2,
      emissive: 0x111111,
      emissiveIntensity: 0.2
    })
    
    const planet = new THREE.Mesh(geometry, material)
    
    // 位置: 在星系体积中随机分布
    // 使用圆柱分布形成星系形状
    const r = Math.random() * universeRadius
    const theta = Math.random() * Math.PI * 2
    const y = (Math.random() - 0.5) * 20000 // 较薄的圆盘
    
    planet.position.set(
      r * Math.cos(theta),
      y,
      r * Math.sin(theta)
    )
    
    // 元数据
    planet.userData = {
      name: `PLANET-${Math.floor(Math.random()*90000)+10000}`,
      type: type.toUpperCase(),
      diameter: (size * 12742).toFixed(0),
      atmosphere: Math.random() > 0.5 ? 'BREATHABLE' : 'TOXIC',
      gravity: (Math.random() * 5 + 0.5).toFixed(2),
      habitable: type === 'terran',
      landingProb: type === 'terran' ? Math.floor(Math.random()*40+60) : Math.floor(Math.random()*20)
    }
    
    // 添加粒子光环 (行星周围的星状粒子)
    const auraGeo = new THREE.BufferGeometry()
    const auraCount = 200
    const auraPos = new Float32Array(auraCount * 3)
    
    for(let j=0; j<auraCount; j++) {
      // 行星周围壳层中的随机点
      const r = size * (1.2 + Math.random() * 0.8) // 1.2倍到2.0倍半径
      const theta = Math.random() * Math.PI * 2
      const phi = Math.acos(2 * Math.random() - 1)
      
      auraPos[j*3] = r * Math.sin(phi) * Math.cos(theta)
      auraPos[j*3+1] = r * Math.sin(phi) * Math.sin(theta)
      auraPos[j*3+2] = r * Math.cos(phi)
    }
    
    auraGeo.setAttribute('position', new THREE.BufferAttribute(auraPos, 3))
    const auraMat = new THREE.PointsMaterial({
      color: 0xffffff,
      size: size * 0.05, // 粒子大小随行星缩放
      transparent: true,
      opacity: 0.8,
      blending: THREE.AdditiveBlending
    })
    
    const aura = new THREE.Points(auraGeo, auraMat)
    planet.add(aura) // 附加到行星
    
    scene.add(planet)
    planets.push(planet)
  }
}

const onMouseMove = (event) => {
  // 归一化鼠标位置 -1 到 1
  mousePos.value.x = (event.clientX / window.innerWidth) * 2 - 1
  mousePos.value.y = -(event.clientY / window.innerHeight) * 2 + 1
}

const animate = () => {
  animationId = requestAnimationFrame(animate)
  
  // 飞行物理
  let targetSpeed = 0
  if (!isPaused.value && !isWarping.value) {
    targetSpeed = isBoosting.value ? boostSpeed : baseSpeed
  }
  
  // 平滑加速
  currentSpeed.value += (targetSpeed - currentSpeed.value) * 0.02

  // 6自由度移动 (在静态空间中移动相机)
  if (isWarping.value) {
    // 跃迁向前移动非常快
    const warpVec = new THREE.Vector3(0, 0, -1).applyQuaternion(camera.quaternion)
    camera.position.add(warpVec.multiplyScalar(5000)) // 跃迁速度
    stars.scale.z = 50 // 视觉拖尾
  } else {
    // 正常飞行
    const forward = new THREE.Vector3(0, 0, -1).applyQuaternion(camera.quaternion)
    camera.position.add(forward.multiplyScalar(currentSpeed.value * 100)) // 速度缩放
    stars.scale.z = 1
  }

  // 转向 (鼠标)
  if (!isPaused.value) {
    const targetYaw = mousePos.value.x * -1.5 
    const targetPitch = mousePos.value.y * 1.0
    
    // 平滑旋转
    camera.rotation.y += (targetYaw - camera.rotation.y) * 0.05
    camera.rotation.x += (targetPitch - camera.rotation.x) * 0.05
    camera.rotation.z = -camera.rotation.y * 0.5 // 倾斜
  }
  
  // 旋转行星 (它们保持在原位，只是自转)
  planets.forEach(p => {
    p.rotation.y += 0.0001
  })

  // 更新 UI 相机位置
  cameraPos.value = camera.position

  renderer.render(scene, camera)
}

const onWindowResize = () => {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

// --- 交互逻辑 ---
const toggleHandTracking = async () => {
  if (handTrackingActive.value) {
    if (videoElement.value.srcObject) {
      videoElement.value.srcObject.getTracks().forEach(t => t.stop())
    }
    handTrackingActive.value = false
  } else {
    await initMediaPipe()
    handTrackingActive.value = true
  }
}

const initMediaPipe = async () => {
  if (!window.Hands) {
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/@mediapipe/hands/hands.js'
    script.onload = () => initializeHands()
    document.head.appendChild(script)
  } else {
    initializeHands()
  }
}

const initializeHands = async () => {
  hands = new window.Hands({
    locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}`
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
      video: { width: { ideal: 1920 }, height: { ideal: 1080 } } 
    })
    videoElement.value.srcObject = stream
    
    const processVideo = async () => {
      if (!handTrackingActive.value) return
      if (videoElement.value && videoElement.value.readyState === 4) {
        await hands.send({ image: videoElement.value })
      }
      requestAnimationFrame(processVideo)
    }
    processVideo()
  } catch (err) {
    console.error(err)
    alert('无法访问摄像头')
  }
}

const onHandResults = (results) => {
  // 移除画布绘制逻辑以隐藏骨架
  
  leftHandDetected.value = false
  rightHandDetected.value = false
  scannedObject.value = null

  if (results.multiHandLandmarks) {
    for (const [index, landmarks] of results.multiHandLandmarks.entries()) {
      const label = results.multiHandedness[index].label
      const x = (1 - landmarks[8].x) * window.innerWidth
      const y = landmarks[8].y * window.innerHeight
      
      if (label === 'Left') {
        leftHandDetected.value = true
        leftHandPos.value = { x, y }
        handleLeftHand(landmarks)
      } else {
        rightHandDetected.value = true
        rightHandPos.value = { x, y }
        handleRightHand(landmarks)
      }
    }
  }
}

const handleLeftHand = (landmarks) => {
  // 手势检测
  const isFist = detectFist(landmarks)
  const isOK = detectOK(landmarks)
  
  if (isOK) {
    leftHandGesture.value = 'OK'
    isBoosting.value = true
    isPaused.value = false
  } else if (isFist) {
    leftHandGesture.value = 'FIST'
    isBoosting.value = false
    isPaused.value = true
  } else {
    leftHandGesture.value = 'OPEN'
    isBoosting.value = false
    isPaused.value = false
  }
}

const handleRightHand = (landmarks) => {
  const isFist = detectFist(landmarks)
  rightHandGesture.value = isFist ? 'FIST' : 'OPEN'
  
  if (isFist) {
    if (warpCharge.value < 100) {
      warpCharge.value += 1
    } else {
      if (!isWarping.value) engageWarp()
    }
  } else {
    warpCharge.value = 0
    handleScanning(landmarks)
  }
}

const detectFist = (landmarks) => {
  const tips = [8, 12, 16, 20]
  let foldedCount = 0
  tips.forEach(tipIdx => {
    if (landmarks[tipIdx].y > landmarks[tipIdx - 2].y) foldedCount++
  })
  return foldedCount >= 3
}

const detectOK = (landmarks) => {
  // 拇指尖 (4) 靠近食指尖 (8)
  const thumb = landmarks[4]
  const index = landmarks[8]
  const dist = Math.sqrt(Math.pow(thumb.x - index.x, 2) + Math.pow(thumb.y - index.y, 2))
  
  // 其他手指伸展
  const middle = landmarks[12].y < landmarks[10].y
  const ring = landmarks[16].y < landmarks[14].y
  
  return dist < 0.05 && middle && ring
}

const handleScanning = (landmarks) => {
  const handX = (1 - landmarks[8].x) * 2 - 1
  const handY = -(landmarks[8].y) * 2 + 1
  
  mouse.set(handX, handY)
  raycaster.setFromCamera(mouse, camera)
  
  const intersects = raycaster.intersectObjects(planets)
  if (intersects.length > 0) {
    const object = intersects[0].object
    scannedObject.value = {
      ...object.userData,
      distance: Math.round(object.position.distanceTo(camera.position))
    }
    object.material.emissiveIntensity = 0.8
  } else {
    planets.forEach(p => p.material.emissiveIntensity = 0.0)
  }
}

const engageWarp = () => {
  if (isWarping.value) return
  isWarping.value = true
  showNotification('SHORT-RANGE JUMP INITIATED', 'success')
  
  setTimeout(() => {
    isWarping.value = false
    warpCharge.value = 0
    currentSector.value++
    
    // 在稍前方生成新星系
    generateSolarSystem(camera.position.z - 2000)
    showNotification(`ARRIVED AT SECTOR ${currentSector.value}`, 'info')
  }, 2000)
}

const showNotification = (text, type) => {
  notification.value = { text, type }
  setTimeout(() => notification.value = null, 3000)
}

// --- 语音 ---
const toggleVoice = () => {
  if (voiceActive.value) {
    recognition.stop()
    voiceActive.value = false
  } else {
    initVoice()
  }
}

const initVoice = () => {
  if (!('webkitSpeechRecognition' in window)) return
  recognition = new window.webkitSpeechRecognition()
  recognition.continuous = true
  recognition.interimResults = true
  recognition.lang = 'zh-CN'
  recognition.onstart = () => { voiceActive.value = true }
  recognition.onresult = (event) => {
    let final = ''
    for (let i = event.resultIndex; i < event.results.length; i++) {
      if (event.results[i].isFinal) final += event.results[i][0].transcript
      else voiceTranscript.value = event.results[i][0].transcript
    }
    if (final) {
      voiceTranscript.value = final
      processCommand(final)
    }
  }
  recognition.start()
}

const processCommand = (cmd) => {
  if (cmd.includes('跃迁') || cmd.includes('跳跃')) {
    if (warpCharge.value >= 100) engageWarp()
    else aiResponse.value = '能量不足，请右手握拳充能'
  } else if (cmd.includes('着陆')) {
    if (scannedObject.value && scannedObject.value.habitable) {
      aiResponse.value = `正在着陆 ${scannedObject.value.name}...`
    } else {
      aiResponse.value = '目标不可着陆'
    }
  } else {
    aiResponse.value = '指令无法执行'
  }
  setTimeout(() => aiResponse.value = '', 3000)
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
  background: #000;
  overflow: hidden;
  font-family: 'Share Tech Mono', monospace;
  color: #00f2ff;
}

.video-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background: #000;
}

.bg-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.15; /* 显著降低不透明度 */
  filter: grayscale(80%) contrast(1.2); /* 去饱和并增加对比度 */
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, rgba(0,0,0,0.2) 0%, #000 90%); /* 强烈的晕影 */
  pointer-events: none;
}

.three-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none; /* 如果需要，允许点击穿透，但 OrbitControls 需要事件。实际上 OrbitControls 附加到 domElement。我们可能需要调整 z-index 或 pointer-events */
}
/* OrbitControls 修复：它需要接收事件。 */
.three-container {
  pointer-events: auto; 
}

.hud-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  pointer-events: none;
}

.hud-overlay > * {
  pointer-events: auto;
}

/* Header */
.hud-header {
  position: absolute;
  top: 2rem;
  left: 2rem;
}

.hud-title {
  font-size: 2rem;
  font-weight: bold;
  letter-spacing: 4px;
  text-shadow: 0 0 10px #00f2ff;
}

.hud-line {
  width: 200px;
  height: 2px;
  background: #00f2ff;
  margin-top: 5px;
  box-shadow: 0 0 5px #00f2ff;
}

.hud-status {
  margin-top: 1rem;
  display: flex;
  gap: 2rem;
}

.status-item {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 0.7rem;
  color: rgba(0, 242, 255, 0.7);
}

.value {
  font-size: 1rem;
  color: #fff;
}

.value.active {
  color: #00ff00;
  text-shadow: 0 0 5px #00ff00;
}

/* Floating Panel (Right Hand) */
.floating-panel {
  position: absolute;
  transform: translate(20px, -50%); /* Offset from hand */
  width: 200px;
  background: rgba(0, 20, 40, 0.7);
  border: 1px solid rgba(0, 242, 255, 0.5);
  border-radius: 8px;
  padding: 1rem;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(0, 242, 255, 0.2);
}

.panel-header-line {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 242, 255, 0.3);
  padding-bottom: 5px;
  margin-bottom: 10px;
}

.panel-id {
  font-size: 0.8rem;
  color: #00f2ff;
}

.panel-grid {
  display: grid;
  gap: 8px;
}

.grid-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
}

.panel-graph {
  display: flex;
  align-items: flex-end;
  height: 30px;
  gap: 2px;
  margin-top: 10px;
}

.graph-line {
  flex: 1;
  background: rgba(0, 242, 255, 0.5);
}

/* Warp Bar */
.warp-charging-ui {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.warp-text {
  color: #ff9f43;
  font-weight: bold;
  animation: pulse 0.5s infinite;
}

.warp-bar {
  width: 150px;
  height: 10px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid #ff9f43;
  border-radius: 5px;
  overflow: hidden;
}

.warp-fill {
  height: 100%;
  background: #ff9f43;
  box-shadow: 0 0 10px #ff9f43;
}

/* Notifications */
.notification-center {
  position: absolute;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
}

.notification-box {
  padding: 1rem 2rem;
  background: rgba(0, 10, 30, 0.9);
  border: 1px solid #00f2ff;
  color: #fff;
  font-size: 1.2rem;
  text-align: center;
  border-radius: 4px;
  box-shadow: 0 0 20px rgba(0, 242, 255, 0.3);
  animation: slideDown 0.5s;
}

.notification-box.success { border-color: #2ed573; color: #2ed573; box-shadow: 0 0 20px rgba(46, 213, 115, 0.3); }
.notification-box.warning { border-color: #ff9f43; color: #ff9f43; }
.notification-box.danger { border-color: #ff4757; color: #ff4757; }

@keyframes slideDown {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

/* Pilot HUD */
.pilot-hud {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.attitude-indicator {
  width: 100px;
  height: 100px;
  border: 2px solid rgba(0, 242, 255, 0.5);
  border-radius: 50%;
  position: relative;
  overflow: hidden;
  background: rgba(0, 242, 255, 0.1);
}

.horizon-line {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 2px;
  background: #00f2ff;
}

.center-cross {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  border: 2px solid #fff;
  transform: translate(-50%, -50%);
  border-radius: 50%;
}

.thrust-status {
  font-size: 0.8rem;
  color: #ff4757;
  font-weight: bold;
}

.thrust-status.active {
  color: #2ed573;
}

.gesture-hint {
  font-size: 0.7rem;
  color: rgba(255, 255, 255, 0.6);
  background: rgba(0, 0, 0, 0.5);
  padding: 2px 5px;
  border-radius: 3px;
}

/* Mouse Hint */
.mouse-hint {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(0, 242, 255, 0.5);
  font-size: 0.8rem;
  letter-spacing: 2px;
  pointer-events: none;
}

.thrust-status.boost {
  color: #ff9f43;
  text-shadow: 0 0 10px #ff9f43;
  animation: pulse 0.2s infinite;
}

@keyframes shake {
  0% { transform: translate(1px, 1px) rotate(0deg); }
  10% { transform: translate(-1px, -2px) rotate(-1deg); }
  20% { transform: translate(-3px, 0px) rotate(1deg); }
  30% { transform: translate(3px, 2px) rotate(0deg); }
  40% { transform: translate(1px, -1px) rotate(1deg); }
  50% { transform: translate(-1px, 2px) rotate(-1deg); }
  60% { transform: translate(-3px, 1px) rotate(0deg); }
  70% { transform: translate(3px, 1px) rotate(-1deg); }
  80% { transform: translate(-1px, -1px) rotate(1deg); }
  90% { transform: translate(1px, 2px) rotate(0deg); }
  100% { transform: translate(1px, -2px) rotate(-1deg); }
}

/* Voice Feedback */
.voice-feedback {
  position: absolute;
  bottom: 6rem;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.voice-feedback.active {
  opacity: 1;
}

.voice-wave {
  display: flex;
  justify-content: center;
  gap: 3px;
  height: 20px;
  margin-bottom: 5px;
}

.bar {
  width: 3px;
  background: #00f2ff;
  animation: wave 1s infinite;
}

.ai-response {
  color: #00ff00;
  font-size: 0.9rem;
  margin-top: 5px;
}

/* Footer */
.hud-footer {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 1rem;
}

.control-btn {
  padding: 0.8rem 2rem;
  border: 1px solid #00f2ff;
  background: rgba(0, 242, 255, 0.1);
  color: #00f2ff;
  cursor: pointer;
  clip-path: polygon(10% 0, 100% 0, 100% 70%, 90% 100%, 0 100%, 0 30%);
  transition: all 0.3s;
}

.control-btn:hover {
  background: #00f2ff;
  color: #000;
}

.control-btn.exit {
  border-color: #ff4757;
  color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.control-btn.exit:hover {
  background: #ff4757;
  color: #fff;
}

/* Loading */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #000;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.arc-reactor {
  position: relative;
  width: 100px;
  height: 100px;
  margin-bottom: 2rem;
}

.circle {
  position: absolute;
  border-radius: 50%;
  border: 2px solid #00f2ff;
  box-shadow: 0 0 10px #00f2ff;
}

.outer {
  top: 0; left: 0; width: 100%; height: 100%;
  border-style: dashed;
  animation: spin 5s linear infinite;
}

.inner {
  top: 20%; left: 20%; width: 60%; height: 60%;
  animation: spin 3s linear infinite reverse;
}

.core {
  position: absolute;
  top: 40%; left: 40%; width: 20%; height: 20%;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f2ff, 0 0 40px #fff;
  animation: pulse 2s infinite;
}

@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes pulse { 50% { transform: scale(1.1); opacity: 0.8; } }
@keyframes wave { 
  0%, 100% { height: 20%; } 
  50% { height: 100%; } 
}
</style>
