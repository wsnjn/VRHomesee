<template>
  <div class="christmas-tree-container">
    <!-- 背景摄像头画面 -->
    <div class="video-wrapper">
      <video ref="videoElement" class="bg-video" autoplay playsinline muted></video>
      <div class="video-overlay"></div>
    </div>
    
    <!-- Three.js 渲染层 -->
    <div ref="container" class="three-container"></div>

    <!-- HUD界面 -->
    <div class="hud-overlay">
      <div class="hud-header">
        <h1 class="title">INTERACTIVE_TREE</h1>
        <div class="status">
          <div class="status-item">
            <span class="label">STATE</span>
            <span class="value" :class="{ active: isAssembled }">{{ isAssembled ? 'ASSEMBLED' : 'DISPERSED' }}</span>
          </div>
          <div class="status-item">
            <span class="label">GESTURE</span>
            <span class="value">{{ handGesture }}</span>
          </div>
        </div>
      </div>

      <div class="controls-hint">
        <div class="hint-item">
          <span class="icon">✊</span>
          <span class="text">FIST: ASSEMBLE</span>
        </div>
        <div class="hint-item">
          <span class="icon">🖐️</span>
          <span class="text">OPEN: DISPERSE</span>
        </div>
        <div class="hint-item">
          <span class="icon">↔️</span>
          <span class="text">MOVE: ROTATE</span>
        </div>
      </div>

      <div class="back-btn" @click="$router.push('/interactive-cube')">
        RETURN
      </div>
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="loader">Loading Assets...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js'
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js'
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js'

// DOM 引用
const container = ref(null)
const videoElement = ref(null)

// 状态
const loading = ref(true)
const isAssembled = ref(false)
const handGesture = ref('NONE')
const handPos = ref({ x: 0, y: 0 })

// Three.js
let scene, camera, renderer, composer
let particles = []
let photos = []
let animationId
const particleCount = 500
const photoCount = 50

// MediaPipe
let hands
let handTrackingActive = false

onMounted(async () => {
  initThree()
  await initMediaPipe()
  animate()
  loading.value = false
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) renderer.dispose()
  if (composer) composer.dispose()
  if (videoElement.value && videoElement.value.srcObject) {
    videoElement.value.srcObject.getTracks().forEach(t => t.stop())
  }
  if (hands) hands.close()
})

const initThree = () => {
  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.z = 100

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.toneMapping = THREE.ReinhardToneMapping
  container.value.appendChild(renderer.domElement)

  // 后处理 (辉光效果)
  const renderScene = new RenderPass(scene, camera)
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85)
  bloomPass.threshold = 0.8 
  bloomPass.strength = 1.5 
  bloomPass.radius = 0.5
  
  composer = new EffectComposer(renderer)
  composer.addPass(renderScene)
  composer.addPass(bloomPass)

  // 灯光 (亮度减半)
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.5) 
  scene.add(ambientLight)
  const pointLight = new THREE.PointLight(0xffffff, 0.5)
  pointLight.position.set(20, 20, 20)
  scene.add(pointLight)
  
  // 温暖的内部光晕 (减弱)
  const innerLight = new THREE.PointLight(0xffaa00, 1.5, 100)
  innerLight.position.set(0, 0, 0)
  scene.add(innerLight)

  // 创建粒子
  const geometrySphere = new THREE.SphereGeometry(0.5, 16, 16) 
  const geometryCube = new THREE.BoxGeometry(0.8, 0.8, 0.8)
  
  // 星空背景
  const starGeo = new THREE.BufferGeometry()
  const starPos = new Float32Array(3000 * 3)
  for(let i=0; i<3000; i++) {
    starPos[i*3] = (Math.random() - 0.5) * 400
    starPos[i*3+1] = (Math.random() - 0.5) * 400
    starPos[i*3+2] = (Math.random() - 0.5) * 400
  }
  starGeo.setAttribute('position', new THREE.BufferAttribute(starPos, 3))
  const stars = new THREE.Points(starGeo, new THREE.PointsMaterial({color: 0xffffff, size: 0.5, transparent: true, opacity: 0.8}))
  scene.add(stars)

  // 材质
  // 绿/蓝/红: 无发光 (自发光 0.2)
  // 黄色: 发光 (自发光 1.5)
  const matGreen = new THREE.MeshStandardMaterial({ color: 0x00ff00, emissive: 0x00ff00, emissiveIntensity: 0.2, roughness: 0.4, metalness: 0.1 })
  const matBlue = new THREE.MeshStandardMaterial({ color: 0x0088ff, emissive: 0x0088ff, emissiveIntensity: 0.2, roughness: 0.4, metalness: 0.1 })
  const matYellow = new THREE.MeshStandardMaterial({ color: 0xffff00, emissive: 0xffff00, emissiveIntensity: 1.5, roughness: 0.4, metalness: 0.1 })
  const matRed = new THREE.MeshStandardMaterial({ color: 0xff0000, emissive: 0xff0000, emissiveIntensity: 0.2, roughness: 0.4, metalness: 0.1 })
  const matWhiteLine = new THREE.LineBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.8 })

  // 1. 核心和外部粒子
  for (let i = 0; i < particleCount; i++) {
    const theta = Math.random() * Math.PI * 2
    const y = (Math.random() - 0.5) * 100 
    const maxRadius = (50 - y) * 0.4 
    const radius = maxRadius * Math.random() 
    
    const targetX = radius * Math.cos(theta)
    const targetZ = radius * Math.sin(theta)
    const targetY = y

    let material
    const normalizedR = radius / (maxRadius + 0.01)
    
    if (normalizedR < 0.6) {
      material = matGreen
    } else {
      const rand = Math.random()
      if (rand < 0.4) material = matBlue
      else if (rand < 0.8) material = matYellow
      else material = matRed
    }

    const isSphere = Math.random() > 0.5
    const mesh = new THREE.Mesh(isSphere ? geometrySphere : geometryCube, material)
    
    mesh.position.set((Math.random()-0.5)*200, (Math.random()-0.5)*200, (Math.random()-0.5)*200)
    
    mesh.userData = {
      targetPos: new THREE.Vector3(targetX, targetY, targetZ),
      randomPos: mesh.position.clone(),
      velocity: new THREE.Vector3(),
      speed: Math.random() * 0.05 + 0.02
    }
    
    scene.add(mesh)
    particles.push(mesh)
  }

  // 2. 黄色填充球体 (减少到 100)
  const fillerCount = 100 
  for (let i = 0; i < fillerCount; i++) {
    const mesh = new THREE.Mesh(geometrySphere, matYellow)
    mesh.scale.set(1.5, 1.5, 1.5) 
    
    mesh.position.set((Math.random()-0.5)*200, (Math.random()-0.5)*200, (Math.random()-0.5)*200)
    
    const theta = Math.random() * Math.PI * 2
    const y = (Math.random() - 0.5) * 100
    const radius = (50 - y) * 0.4 
    
    const targetX = radius * Math.cos(theta)
    const targetZ = radius * Math.sin(theta)
    const targetY = y
    
    mesh.userData = {
      targetPos: new THREE.Vector3(targetX, targetY, targetZ),
      randomPos: mesh.position.clone(),
      velocity: new THREE.Vector3(),
      speed: Math.random() * 0.05 + 0.02
    }
    scene.add(mesh)
    particles.push(mesh)
  }

  // 3. 短白线
  const lineCount = 50
  const lineGeo = new THREE.BufferGeometry().setFromPoints([
    new THREE.Vector3(0, -3, 0), 
    new THREE.Vector3(0, 3, 0)
  ])
  
  for (let i = 0; i < lineCount; i++) {
    const line = new THREE.Line(lineGeo, matWhiteLine)
    
    line.position.set((Math.random()-0.5)*200, (Math.random()-0.5)*200, (Math.random()-0.5)*200)
    
    const theta = Math.random() * Math.PI * 2
    const y = Math.random() * 50 
    const radius = (50 - y) * 0.4 + 1 
    
    const targetX = radius * Math.cos(theta)
    const targetZ = radius * Math.sin(theta)
    const targetY = y
    
    line.userData = {
      targetPos: new THREE.Vector3(targetX, targetY, targetZ),
      randomPos: line.position.clone(),
      velocity: new THREE.Vector3(),
      speed: Math.random() * 0.05 + 0.02,
      isLine: true 
    }
    
    line.rotation.x = 0.2 
    
    scene.add(line)
    particles.push(line)
  }

  // 创建照片 (外层)
  const textureLoader = new THREE.TextureLoader()
  const photoTexture = textureLoader.load('/models/default-avatar.png') 
  const photoGeo = new THREE.PlaneGeometry(12, 12) // Double size
  const photoMat = new THREE.MeshStandardMaterial({ 
    map: photoTexture, 
    side: THREE.DoubleSide,
    transparent: true,
    opacity: 1.0,
    roughness: 0.8,
    metalness: 0.0,
    emissive: 0x000000 
  })

  const photoCount = 200 // Reduced half

  for (let i = 0; i < photoCount; i++) {
    const mesh = new THREE.Mesh(photoGeo, photoMat)
    
    mesh.position.set(
      (Math.random() - 0.5) * 200,
      (Math.random() - 0.5) * 200,
      (Math.random() - 0.5) * 200
    )
    
    const theta = Math.random() * Math.PI * 2
    const rand = Math.random()
    const y = -50 + (rand * rand) * 100 
    const radius = (50 - y) * 0.4 + 2 
    
    const targetX = radius * Math.cos(theta)
    const targetZ = radius * Math.sin(theta)
    const targetY = y

    mesh.userData = {
      targetPos: new THREE.Vector3(targetX, targetY, targetZ),
      randomPos: mesh.position.clone(),
      velocity: new THREE.Vector3(),
      speed: Math.random() * 0.05 + 0.02
    }
    
    mesh.lookAt(0, y, 0)
    mesh.rotateX(-0.3) // Increased tilt
    
    scene.add(mesh)
    photos.push(mesh)
  }

  // 创建树顶装饰
  const topperGeo = new THREE.CylinderGeometry(2, 2, 10, 32)
  const topperMat = new THREE.MeshStandardMaterial({
    color: 0xffff00,
    emissive: 0xffff00,
    emissiveIntensity: 2.0, 
    roughness: 0.1,
    metalness: 0.5
  })
  const topper = new THREE.Mesh(topperGeo, topperMat)
  
  topper.position.set((Math.random()-0.5)*200, (Math.random()-0.5)*200, (Math.random()-0.5)*200)

  topper.userData = {
    targetPos: new THREE.Vector3(0, 60, 0), 
    randomPos: topper.position.clone(),
    velocity: new THREE.Vector3(),
    speed: 0.05,
    isTopper: true 
  }
  
  scene.add(topper)
  particles.push(topper) 
}

const animate = () => {
  animationId = requestAnimationFrame(animate)
  
  const targetState = isAssembled.value
  
  const rotationSpeed = (handPos.value.x - 0.5) * 0.05
  scene.rotation.y += rotationSpeed

  // 更新粒子
  particles.forEach(p => {
    const target = targetState ? p.userData.targetPos : p.userData.randomPos
    p.position.lerp(target, 0.05)
    
    if (p.userData.isTopper) {
      p.rotation.x = 0
      p.rotation.z = 0
      p.rotation.y += 0.02 
    } else if (p.userData.isLine) {
       // 保持线条固定方向或轻微摆动
    } else {
      p.rotation.x += 0.01
      p.rotation.y += 0.01
    }
  })

  // 更新照片
  photos.forEach(p => {
    const target = targetState ? p.userData.targetPos : p.userData.randomPos
    p.position.lerp(target, 0.05)
    if (targetState) {
      p.lookAt(0, p.position.y, 0)
      p.rotateX(-0.2) 
    } else {
      p.rotation.z += 0.01
    }
  })

  composer.render()
}

// --- MediaPipe ---
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
    maxNumHands: 1,
    modelComplexity: 1,
    minDetectionConfidence: 0.7,
    minTrackingConfidence: 0.5
  })
  
  hands.onResults(onHandResults)
  
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ 
      video: { width: { ideal: 1280 }, height: { ideal: 720 } } 
    })
    videoElement.value.srcObject = stream
    handTrackingActive = true
    
    const processVideo = async () => {
      if (!handTrackingActive) return
      if (videoElement.value && videoElement.value.readyState === 4) {
        await hands.send({ image: videoElement.value })
      }
      requestAnimationFrame(processVideo)
    }
    processVideo()
  } catch (err) {
    console.error(err)
    alert('Camera Error')
  }
}

const onHandResults = (results) => {
  if (results.multiHandLandmarks && results.multiHandLandmarks.length > 0) {
    const landmarks = results.multiHandLandmarks[0]
    
    // 更新手部位置 (用于旋转)
    // MediaPipe x 是 0-1 (左-右)，我们需要中心 0.5
    handPos.value = { x: 1 - landmarks[9].x, y: landmarks[9].y } // 翻转 X
    
    // 检测手势
    if (detectFist(landmarks)) {
      handGesture.value = 'FIST'
      isAssembled.value = true
    } else {
      handGesture.value = 'OPEN'
      isAssembled.value = false
    }
  } else {
    handGesture.value = 'NONE'
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
</script>

<style scoped>
.christmas-tree-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  background: #000;
  overflow: hidden;
}

.video-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.bg-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.2;
  transform: scaleX(-1);
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, transparent 0%, #000 90%);
}

.three-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.hud-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  pointer-events: none;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.hud-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.title {
  font-family: 'Rajdhani', sans-serif;
  font-size: 3rem;
  color: #00ff00;
  text-shadow: 0 0 20px rgba(0, 255, 0, 0.5);
  margin: 0;
}

.status {
  text-align: right;
}

.status-item {
  margin-bottom: 10px;
}

.label {
  font-size: 0.8rem;
  color: #888;
  margin-right: 10px;
}

.value {
  font-family: 'Courier New', monospace;
  font-size: 1.2rem;
  color: #fff;
  font-weight: bold;
}

.value.active {
  color: #00ff00;
  text-shadow: 0 0 10px #00ff00;
}

.controls-hint {
  position: absolute;
  bottom: 40px;
  left: 40px;
  display: flex;
  gap: 30px;
}

.hint-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(0, 20, 0, 0.6);
  padding: 10px 20px;
  border-radius: 30px;
  border: 1px solid rgba(0, 255, 0, 0.3);
}

.icon {
  font-size: 1.5rem;
}

.text {
  color: #00ff00;
  font-family: 'Rajdhani', sans-serif;
  letter-spacing: 1px;
}

.back-btn {
  position: absolute;
  bottom: 40px;
  right: 40px;
  pointer-events: auto;
  padding: 10px 30px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #fff;
  color: #000;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  color: #00ff00;
  font-family: 'Courier New', monospace;
}
</style>
