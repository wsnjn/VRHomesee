<template>
  <div class="house-tour-page">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-brand">
        <h2>VR漫游看房</h2>
      </div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">返回首页</router-link>
        <button @click="toggleControls" class="nav-link control-btn">
          {{ showControls ? '隐藏控制' : '显示控制' }}
        </button>
      </div>
    </nav>

    <!-- 漫游看房组件 -->
    <VrHouseTour 
      :navi-data="houseTourData" 
      :initial-scene-key="initialSceneKey"
      ref="vrHouseTour"
    />

    <!-- 场景选择面板 -->
    <div v-if="showControls" class="scene-panel">
      <h3>场景选择</h3>
      <div class="scene-list">
        <div 
          v-for="scene in houseTourData" 
          :key="scene.scene.photo_key"
          class="scene-item"
          :class="{ active: currentSceneKey === scene.scene.photo_key }"
          @click="switchScene(scene.scene.photo_key)"
        >
          <img :src="getSceneThumb(scene)" :alt="scene.scene.title" class="scene-thumb">
          <span class="scene-title">{{ scene.scene.title }}</span>
        </div>
      </div>
    </div>

    <!-- 操作说明 -->
    <div class="instructions">
      <h3>操作说明</h3>
      <ul>
        <li>鼠标拖拽：旋转视角</li>
        <li>点击红色导航点：切换场景</li>
        <li>使用下方控制按钮：左右旋转、重置视角</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import VrHouseTour from '../../components/VrHouseTour.vue'
import { houseTourData as fullHouseTourData } from '../../data/houseTourData.js'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const vrHouseTour = ref(null)
const showControls = ref(true)
const currentSceneKey = ref('1')
const houseTourData = ref(fullHouseTourData)
const loading = ref(true)

const API_BASE_URL = 'http://localhost:8080/api'

// 计算当前场景
const currentScene = computed(() => {
  return houseTourData.value.find(scene => scene.scene.photo_key === currentSceneKey.value)
})

// 初始场景键
const initialSceneKey = ref('1')

// 切换场景
const switchScene = (sceneKey) => {
  currentSceneKey.value = sceneKey
  if (vrHouseTour.value) {
    // 调用组件的 walkTo 方法
    vrHouseTour.value.walkTo(sceneKey)
  }
}

// 获取场景缩略图
const getSceneThumb = (scene) => {
  return scene.scene.sphereSource?.thumb || ''
}

// 切换控制面板显示
const toggleControls = () => {
  showControls.value = !showControls.value
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 加载VR场景数据
const loadVrScenes = async () => {
  const houseId = route.query.houseId
  if (!houseId) {
    alert('未指定房屋')
    router.push('/')
    return
  }
  try {
    const response = await axios.get(`${API_BASE_URL}/vr-scenes/${houseId}`)
    if (response.data.success && response.data.data.length > 0) {
      const scenes = response.data.data
      
      // 转换数据格式适配 VrHouseTour 组件
      houseTourData.value = scenes.map(scene => ({
        scene: {
          photo_key: scene.id.toString(),
          title: scene.sceneName,
          sphereSource: {
            // Ensure URL is correct. If it's relative to src/assets, we might need to handle it.
            // But for now, let's use the path stored in DB.
            url: scene.imageUrl,
            thumb: scene.imageUrl // Use same image for thumb for now
          },
          // Add empty levels to prevent errors if component expects it
          levels: []
        }
      }))
      // 设置初始场景
      initialSceneKey.value = houseTourData.value[0].scene.photo_key
      currentSceneKey.value = initialSceneKey.value
    } else {
      // No scenes found
      alert('该房屋暂无VR全景图')
      // router.push('/') // Optional: redirect or show empty state
    }
  } catch (error) {
    console.error('Failed to load VR scenes:', error)
    alert('加载VR场景失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadVrScenes()
})
</script>

<style scoped>
.house-tour-page {
  position: relative;
  height: 100vh;
  overflow: hidden;
}

.navbar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: rgba(44, 62, 80, 0.8);
  color: white;
  z-index: 100;
  backdrop-filter: blur(10px);
}

.nav-brand h2 {
  margin: 0;
  color: white;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
  cursor: pointer;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.control-btn {
  background-color: #007bff;
  border: none;
  cursor: pointer;
  font-size: 1rem;
}

.control-btn:hover {
  background-color: #0056b3;
}

.scene-panel {
  position: absolute;
  top: 80px;
  right: 20px;
  background: rgba(255, 255, 255, 0.9);
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 90;
  backdrop-filter: blur(10px);
  max-width: 300px;
}

.scene-panel h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.scene-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.scene-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.scene-item:hover {
  background-color: rgba(0, 123, 255, 0.1);
}

.scene-item.active {
  background-color: rgba(0, 123, 255, 0.2);
  border: 1px solid #007bff;
}

.scene-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.scene-title {
  font-size: 0.9rem;
  color: #333;
  font-weight: 500;
}

.instructions {
  position: absolute;
  bottom: 80px;
  left: 20px;
  background: rgba(255, 255, 255, 0.9);
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 90;
  backdrop-filter: blur(10px);
  max-width: 250px;
}

.instructions h3 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.instructions ul {
  margin: 0;
  padding-left: 1.2rem;
  color: #555;
  font-size: 0.9rem;
  line-height: 1.4;
}

.instructions li {
  margin-bottom: 0.3rem;
}
</style>