<template>
  <div class="ar-game-container">
    <!-- 全屏加载遮罩 (防崩溃加载) -->
    <div v-if="isLoading" class="loading-mask">
      <div class="loader">
        <div class="spinner"></div>
        <div class="loading-text">INITIALIZING AR SYSTEMS...</div>
        <div class="sub-text">LOADING NEURAL NETWORKS & ASSETS</div>
        <div class="version-info">MediaPipe v0.4.1646424915</div>
      </div>
    </div>

    <!-- 游戏UI层 -->
    <div class="game-ui" v-show="!isLoading">
      <!-- 分数板 -->
      <div class="score-board">
        <div class="score-label">SCORE</div>
        <div class="score-value">{{ score }}</div>
      </div>
      
      <!-- 状态面板 -->
      <div class="status-panel">
        <div class="status-item">
          <span class="label">SYSTEM:</span>
          <span class="value online">ONLINE</span>
        </div>
        <div class="status-item">
          <span class="label">HAND TRACKING:</span>
          <span class="value" :class="{ 'detected': isHandDetected }">{{ isHandDetected ? 'LOCKED' : 'SEARCHING' }}</span>
        </div>
        <div class="status-item">
          <span class="label">FPS:</span>
          <span class="value">{{ fps }}</span>
        </div>
      </div>

      <!-- 准心 (磁吸辅助) -->
      <div class="crosshair" :class="{ 'locked': isAimLocked }"></div>
      
      <!-- 浮动文字特效容器 -->
      <div class="effects-container" ref="effectsContainer"></div>

      <!-- 退出按钮 -->
      <div class="back-btn" @click="goBack">EXIT SIMULATION</div>
    </div>

    <!-- 视频流 (隐藏，仅作纹理源) -->
    <video ref="videoElement" class="input_video" playsinline style="display: none;"></video>
    
    <!-- Three.js 输出画布 -->
    <canvas ref="canvasElement" class="output_canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as THREE from 'three'

const router = useRouter()
const isLoading = ref(true)
const isHandDetected = ref(false)
const isAimLocked = ref(false)
const score = ref(0)
const fps = ref(0)
const videoElement = ref(null)
const canvasElement = ref(null)
const effectsContainer = ref(null)

// 游戏状态变量
let scene, camera, renderer
let enemies = []
let particles = []
let laserLine = null
let lastTime = 0
let frameCount = 0
let lastFpsTime = 0
let hands = null
let cameraUtils = null
let animationId = null
let lastDetectionTime = 0
const DETECTION_INTERVAL = 50 // 限制AI检测频率 (20ms/次) 以保证渲染性能

// 游戏常量配置
const ENEMY_COUNT = 4
const MP_VERSION = '0.4.1646424915' // 强制版本锁定

// 敌人类型定义 (颜色对应分数)
const ENEMY_TYPES = [
  { color: 0xff0055, score: 100, speed: 0.05 }, // 普通 - 红
  { color: 0x00ffaa, score: 200, speed: 0.07 }, // 快速 - 青
  { color: 0xffaa00, score: 300, speed: 0.04 }, // 高分 - 金
]

const goBack = () => {
  cleanup()
  router.push('/interactive-cube')
}

// 动态加载脚本辅助函数
const loadScript = (src) => {
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = src
    script.onload = resolve
    script.onerror = reject
    document.head.appendChild(script)
  })
}

// 初始化 Three.js 场景
const initThree = () => {
  const width = window.innerWidth
  const height = window.innerHeight

  scene = new THREE.Scene()
  
  // 相机设置
  camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000)
  camera.position.z = 5

  // 渲染器设置
  renderer = new THREE.WebGLRenderer({
    canvas: canvasElement.value,
    alpha: true,
    antialias: true
  })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))

  // 灯光
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)
  const directionalLight = new THREE.DirectionalLight(0xffffff, 1)
  directionalLight.position.set(0, 10, 10)
  scene.add(directionalLight)

  // 视频背景纹理
  const videoTexture = new THREE.VideoTexture(videoElement.value)
  videoTexture.colorSpace = THREE.SRGBColorSpace
  scene.background = videoTexture

  // 初始化激光指示线
  const laserGeometry = new THREE.BufferGeometry().setFromPoints([
    new THREE.Vector3(0, -2, 4), // 假设手的位置
    new THREE.Vector3(0, 0, -5)  // 目标位置
  ])
  const laserMaterial = new THREE.LineBasicMaterial({ 
    color: 0x00ff00, 
    transparent: true, 
    opacity: 0 
  })
  laserLine = new THREE.Line(laserGeometry, laserMaterial)
  scene.add(laserLine)

  // 初始化敌人
  for (let i = 0; i < ENEMY_COUNT; i++) {
    spawnEnemy()
  }
}

// 生成敌人 (飞盘)
const spawnEnemy = () => {
  // 随机选择类型
  const type = ENEMY_TYPES[Math.floor(Math.random() * ENEMY_TYPES.length)]
  
  const geometry = new THREE.CylinderGeometry(0.4, 0.4, 0.05, 32)
  geometry.rotateX(Math.PI / 2) // 旋转成飞盘状
  
  const material = new THREE.MeshPhongMaterial({
    color: type.color,
    emissive: 0x222222,
    shininess: 100
  })
  
  const enemy = new THREE.Mesh(geometry, material)
  
  // 屏幕边缘随机位置
  const angle = Math.random() * Math.PI * 2
  const radius = 4 + Math.random() * 2
  enemy.position.set(
    Math.cos(angle) * radius,
    Math.sin(angle) * radius * 0.8,
    -3 - Math.random() * 4 // 深度
  )
  
  // 设置飞向中心的速度向量
  const target = new THREE.Vector3(0, 0, 2) // 飞向相机前方一点
  const direction = new THREE.Vector3().subVectors(target, enemy.position).normalize()
  
  enemy.userData = {
    velocity: direction.multiplyScalar(type.speed),
    rotationSpeed: {
      x: Math.random() * 0.1,
      y: Math.random() * 0.1,
      z: 0.2 // 飞盘自转
    },
    score: type.score,
    type: type
  }
  
  scene.add(enemy)
  enemies.push(enemy)
}

// 击碎特效
const createExplosion = (position, color) => {
  const particleCount = 15
  const geometry = new THREE.BufferGeometry()
  const positions = []
  const velocities = []
  
  for (let i = 0; i < particleCount; i++) {
    positions.push(position.x, position.y, position.z)
    velocities.push(
      (Math.random() - 0.5) * 0.8,
      (Math.random() - 0.5) * 0.8,
      (Math.random() - 0.5) * 0.8
    )
  }
  
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  
  const material = new THREE.PointsMaterial({
    color: color,
    size: 0.15,
    transparent: true,
    opacity: 1
  })
  
  const particleSystem = new THREE.Points(geometry, material)
  particleSystem.userData = { velocities, life: 1.0 }
  scene.add(particleSystem)
  particles.push(particleSystem)
}

// 浮动文字特效
const showFloatingText = (text, x, y, type) => {
  const el = document.createElement('div')
  el.textContent = text
  el.className = `floating-text ${type}`
  el.style.left = `${x}px`
  el.style.top = `${y}px`
  effectsContainer.value.appendChild(el)
  
  setTimeout(() => {
    el.remove()
  }, 1000)
}

// 初始化 MediaPipe
const initMediaPipe = async () => {
  try {
    // 动态加载指定版本的 MediaPipe 资源
    await loadScript(`https://unpkg.com/@mediapipe/camera_utils/camera_utils.js`)
    await loadScript(`https://unpkg.com/@mediapipe/control_utils/control_utils.js`)
    await loadScript(`https://unpkg.com/@mediapipe/drawing_utils/drawing_utils.js`)
    await loadScript(`https://unpkg.com/@mediapipe/hands@${MP_VERSION}/hands.js`)

    const handsInstance = new window.Hands({
      locateFile: (file) => {
        return `https://unpkg.com/@mediapipe/hands@${MP_VERSION}/${file}`
      }
    })

    handsInstance.setOptions({
      maxNumHands: 1,
      modelComplexity: 1,
      minDetectionConfidence: 0.5,
      minTrackingConfidence: 0.5
    })

    handsInstance.onResults(onResults)
    hands = handsInstance

    const cameraInstance = new window.Camera(videoElement.value, {
      onFrame: async () => {
        // 性能优化：限制检测频率
        const now = performance.now()
        if (now - lastDetectionTime > DETECTION_INTERVAL) {
          try {
            // 防崩溃保护：手势识别循环添加 try-catch
            await handsInstance.send({ image: videoElement.value })
          } catch (e) {
            console.warn('MediaPipe detection error:', e)
          }
          lastDetectionTime = now
        }
      },
      width: 1280,
      height: 720
    })
    
    await cameraInstance.start()
    cameraUtils = cameraInstance
    
    // 只有当一切就绪后才移除 Loading 遮罩
    isLoading.value = false
  } catch (error) {
    console.error('MediaPipe Init Error:', error)
    alert('Failed to initialize AR system. Please check your connection.')
  }
}

// 手势识别逻辑
let isTriggerPressed = false
const onResults = (results) => {
  if (results.multiHandLandmarks && results.multiHandLandmarks.length > 0) {
    isHandDetected.value = true
    const landmarks = results.multiHandLandmarks[0]
    
    // 关键点索引: 0=手腕, 4=拇指尖, 8=食指尖, 6=食指根, 12=中指尖, 10=中指根
    const thumbTip = landmarks[4]
    const indexTip = landmarks[8]
    const indexPip = landmarks[6]
    const middleTip = landmarks[12]
    const middlePip = landmarks[10]
    
    // 1. 识别"手枪"手势
    // 食指伸直 (尖端高于根部 - 注意Y轴向下增大，所以是小于)
    const isIndexExtended = indexTip.y < indexPip.y
    // 中指弯曲 (尖端低于根部 - Y值更大)
    const isMiddleCurled = middleTip.y > middlePip.y
    
    if (isIndexExtended && isMiddleCurled) {
      // 2. 瞄准逻辑
      // 将食指尖坐标映射到屏幕
      const x = (1 - indexTip.x) * window.innerWidth
      const y = indexTip.y * window.innerHeight
      
      // 磁吸辅助瞄准
      let target = null
      let minDist = 150 // 磁吸半径
      
      enemies.forEach(enemy => {
        const screenPos = enemy.position.clone().project(camera)
        const sx = (screenPos.x * 0.5 + 0.5) * window.innerWidth
        const sy = (-(screenPos.y * 0.5) + 0.5) * window.innerHeight
        
        const dist = Math.hypot(sx - x, sy - y)
        if (dist < minDist) {
          minDist = dist
          target = enemy
        }
      })
      
      // 更新准心位置
      if (target) {
        isAimLocked.value = true
        const screenPos = target.position.clone().project(camera)
        const sx = (screenPos.x * 0.5 + 0.5) * window.innerWidth
        const sy = (-(screenPos.y * 0.5) + 0.5) * window.innerHeight
        updateCrosshair(sx, sy)
        updateLaser(x, y, sx, sy) // 激光吸附
      } else {
        isAimLocked.value = false
        updateCrosshair(x, y)
        updateLaser(x, y, x, y) // 激光直射
      }
      
      // 3. 射击逻辑 (拇指扣动)
      // 拇指尖下压超过食指根部Y坐标
      const isThumbTrigger = thumbTip.y > indexPip.y 
      
      if (isThumbTrigger && !isTriggerPressed) {
        isTriggerPressed = true
        shoot(x, y, target)
      } else if (!isThumbTrigger) {
        isTriggerPressed = false
      }
    } else {
      // 手势不符合，隐藏激光
      if (laserLine) laserLine.material.opacity = 0
    }
  } else {
    isHandDetected.value = false
    isAimLocked.value = false
    if (laserLine) laserLine.material.opacity = 0
  }
}

const updateCrosshair = (x, y) => {
  const crosshair = document.querySelector('.crosshair')
  if (crosshair) {
    crosshair.style.transform = `translate(${x}px, ${y}px)`
  }
}

const updateLaser = (startX, startY, endX, endY) => {
  if (!laserLine) return
  
  // 将屏幕坐标转回世界坐标 (近似)
  const vectorStart = new THREE.Vector3(
    (startX / window.innerWidth) * 2 - 1,
    -(startY / window.innerHeight) * 2 + 1,
    0.5
  ).unproject(camera)
  
  const vectorEnd = new THREE.Vector3(
    (endX / window.innerWidth) * 2 - 1,
    -(endY / window.innerHeight) * 2 + 1,
    0.5 // 射向深处
  ).unproject(camera).multiplyScalar(10) // 延长

  const positions = laserLine.geometry.attributes.position.array
  positions[0] = vectorStart.x; positions[1] = vectorStart.y; positions[2] = vectorStart.z
  positions[3] = vectorEnd.x; positions[4] = vectorEnd.y; positions[5] = vectorEnd.z
  laserLine.geometry.attributes.position.needsUpdate = true
  laserLine.material.opacity = 0.6
}

const shoot = (x, y, target) => {
  // 播放音效 (这里使用简单的Web Audio API模拟，避免加载外部文件失败)
  playShootSound()
  
  if (target) {
    // 命中逻辑
    score.value += target.userData.score
    createExplosion(target.position, target.userData.type.color)
    showFloatingText('HIT!', x, y, 'hit')
    
    // 移除并立即补位
    scene.remove(target)
    enemies = enemies.filter(e => e !== target)
    spawnEnemy()
  } else {
    // 未命中
    showFloatingText('MISS', x, y, 'miss')
  }
}

// 简单的合成音效
const playShootSound = () => {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext
    const ctx = new AudioContext()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    
    osc.type = 'square'
    osc.frequency.setValueAtTime(880, ctx.currentTime)
    osc.frequency.exponentialRampToValueAtTime(110, ctx.currentTime + 0.1)
    
    gain.gain.setValueAtTime(0.1, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.1)
    
    osc.connect(gain)
    gain.connect(ctx.destination)
    
    osc.start()
    osc.stop(ctx.currentTime + 0.1)
  } catch (e) {
    // 忽略音频错误
  }
}

const animate = (time) => {
  requestAnimationFrame(animate)
  
  // FPS 计算
  frameCount++
  if (time - lastFpsTime >= 1000) {
    fps.value = frameCount
    frameCount = 0
    lastFpsTime = time
  }
  
  if (isLoading.value) return
  
  // 更新敌人位置
  enemies.forEach(enemy => {
    enemy.position.add(enemy.userData.velocity)
    enemy.rotation.z += enemy.userData.rotationSpeed.z
    
    // 边界检查：如果飞太近或飞出视野
    if (enemy.position.z > 4 || enemy.position.length() > 15) {
      scene.remove(enemy)
      enemies = enemies.filter(e => e !== enemy)
      spawnEnemy()
    }
  })
  
  // 确保敌人数量恒定
  while (enemies.length < ENEMY_COUNT) {
    spawnEnemy()
  }
  
  // 更新粒子特效
  particles.forEach(p => {
    p.userData.life -= 0.03
    const positions = p.geometry.attributes.position.array
    const velocities = p.userData.velocities
    
    for (let i = 0; i < positions.length; i += 3) {
      positions[i] += velocities[i]
      positions[i+1] += velocities[i+1]
      positions[i+2] += velocities[i+2]
    }
    p.geometry.attributes.position.needsUpdate = true
    p.material.opacity = p.userData.life
    
    if (p.userData.life <= 0) {
      scene.remove(p)
      particles = particles.filter(sys => sys !== p)
    }
  })
  
  renderer.render(scene, camera)
}

const cleanup = () => {
  if (cameraUtils) {
    cameraUtils.stop()
  }
  if (hands) {
    hands.close()
  }
  if (renderer) {
    renderer.dispose()
  }
  const stream = videoElement.value?.srcObject
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
  }
}

onMounted(() => {
  initThree()
  initMediaPipe()
  animate(0)
  
  window.addEventListener('resize', () => {
    if (camera && renderer) {
      camera.aspect = window.innerWidth / window.innerHeight
      camera.updateProjectionMatrix()
      renderer.setSize(window.innerWidth, window.innerHeight)
    }
  })
})

onBeforeUnmount(() => {
  cleanup()
})
</script>

<style scoped>
.ar-game-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #000;
  font-family: 'Rajdhani', sans-serif;
}

/* Loading Mask */
.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #000;
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loader {
  text-align: center;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(0, 242, 255, 0.2);
  border-top-color: #00f2ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 30px;
  box-shadow: 0 0 20px rgba(0, 242, 255, 0.3);
}

.loading-text {
  color: #00f2ff;
  font-size: 1.8rem;
  letter-spacing: 6px;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.5);
}

.sub-text {
  color: #666;
  font-size: 0.9rem;
  letter-spacing: 3px;
  margin-bottom: 5px;
}

.version-info {
  color: #444;
  font-size: 0.7rem;
  font-family: monospace;
}

/* Game UI */
.game-ui {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  pointer-events: none;
}

.score-board {
  position: absolute;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
}

.score-label {
  color: #00f2ff;
  font-size: 1rem;
  letter-spacing: 4px;
  margin-bottom: 5px;
}

.score-value {
  color: #fff;
  font-size: 4rem;
  font-weight: 700;
  line-height: 1;
  text-shadow: 0 0 30px rgba(0, 242, 255, 0.6);
}

.status-panel {
  position: absolute;
  top: 30px;
  left: 30px;
  background: rgba(0, 10, 20, 0.8);
  padding: 20px;
  border: 1px solid rgba(0, 242, 255, 0.2);
  border-left: 4px solid #00f2ff;
  border-radius: 0 10px 10px 0;
  backdrop-filter: blur(5px);
}

.status-item {
  display: flex;
  justify-content: space-between;
  width: 220px;
  margin-bottom: 8px;
  font-size: 0.9rem;
  letter-spacing: 1px;
}

.label {
  color: #888;
}

.value {
  color: #fff;
  font-weight: bold;
}

.value.online {
  color: #00ff00;
  text-shadow: 0 0 5px #00ff00;
}

.value.detected {
  color: #00ff00;
}

/* Crosshair */
.crosshair {
  position: absolute;
  top: 0;
  left: 0;
  width: 40px;
  height: 40px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  transition: width 0.15s, height 0.15s, border-color 0.15s, box-shadow 0.15s;
}

.crosshair::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 4px;
  height: 4px;
  background: #fff;
  transform: translate(-50%, -50%);
  border-radius: 50%;
}

.crosshair.locked {
  width: 70px;
  height: 70px;
  border-color: #00ff00;
  border-width: 3px;
  box-shadow: 0 0 20px rgba(0, 255, 0, 0.5), inset 0 0 10px rgba(0, 255, 0, 0.2);
  background: rgba(0, 255, 0, 0.05);
}

.back-btn {
  position: absolute;
  bottom: 40px;
  right: 40px;
  padding: 12px 30px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  background: rgba(0, 0, 0, 0.6);
  cursor: pointer;
  pointer-events: auto;
  letter-spacing: 3px;
  font-size: 0.9rem;
  transition: all 0.3s;
  backdrop-filter: blur(5px);
}

.back-btn:hover {
  background: rgba(0, 242, 255, 0.2);
  border-color: #00f2ff;
  box-shadow: 0 0 20px rgba(0, 242, 255, 0.3);
}

.output_canvas {
  width: 100%;
  height: 100%;
  display: block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>

<style>
/* Global styles for dynamic elements */
.floating-text {
  position: absolute;
  font-family: 'Rajdhani', sans-serif;
  font-weight: 900;
  font-size: 2.5rem;
  pointer-events: none;
  animation: floatUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
  text-shadow: 0 0 10px rgba(0,0,0,0.5);
  transform: translate(-50%, -50%);
  z-index: 20;
}

.floating-text.hit {
  color: #00ffff;
  -webkit-text-stroke: 1px rgba(0, 255, 255, 0.5);
}

.floating-text.miss {
  color: #ff3333;
  font-size: 1.5rem;
  opacity: 0.8;
}

@keyframes floatUp {
  0% { transform: translate(-50%, -50%) scale(0.5); opacity: 0; }
  20% { transform: translate(-50%, -50%) scale(1.2); opacity: 1; }
  100% { transform: translate(-50%, -120px) scale(1); opacity: 0; }
}
</style>
