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

    <!-- AI 看房助手 -->
    <VrChatAssistant 
      :room-info="roomInfo"
      :renderer="vrRenderer"
      :user-id="currentUserId"
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import VrHouseTour from '../../components/VrHouseTour.vue'
import VrChatAssistant from '../../components/VrChatAssistant.vue'
import { houseTourData as fullHouseTourData } from '../../data/houseTourData.js'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const vrHouseTour = ref(null)
const showControls = ref(true)
const currentSceneKey = ref('1')
const houseTourData = ref(fullHouseTourData)
const loading = ref(true)

const API_BASE_URL = 'https://api.homesee.xyz/api'

// 房源信息（传递给AI助手）
const roomInfo = ref({})

// 当前用户ID
const getUserId = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      return user.id
    }
  } catch (e) {
    console.error('Failed to parse user from localStorage', e)
  }
  return null
}
const currentUserId = ref(getUserId())

// VR渲染器引用（用于截图）
const vrRenderer = ref(null)

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
            // 确保使用HTTPS防止混合内容错误
            url: ensureHttps(scene.imageUrl),
            thumb: ensureHttps(scene.imageUrl) // Use same image for thumb for now
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

// 获取房屋详情
const fetchHouseDetail = async (houseId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/rooms/${houseId}`)
    if (response.data.success && response.data.room) {
      const room = response.data.room
      roomInfo.value = {
        title: `${room.communityName} ${room.buildingUnit || ''} ${room.roomNumber || ''}`,
        price: room.rentPrice,
        area: room.roomArea,
        layout: room.layout || (room.roomNumber + '室'),
        address: `${room.city}${room.district}${room.street}${room.communityName}`,
        description: room.description,
        // 原始字段也传一份，方便后端灵活取用
        rentPrice: room.rentPrice,
        communityName: room.communityName,
        roomArea: room.roomArea
      }
    }
  } catch (error) {
    console.error('Failed to fetch house details:', error)
  }
}

// 确保URL使用HTTPS
// 确保URL使用HTTPS，并替换不安全的源
const ensureHttps = (url) => {
  if (!url) return ''
  
  try {
    // 如果已经是完整URL
    if (url.startsWith('http')) {
      const urlObj = new URL(url)
      // 强制使用文件服务器域名
      return `https://files.homesee.xyz${urlObj.pathname}${urlObj.search}`
    }
    // 如果是相对路径，直接拼接
    return `https://files.homesee.xyz${url.startsWith('/') ? '' : '/'}${url}`
  } catch (e) {
    // 解析失败，回退到原始逻辑
    return url
  }
}

onMounted(() => {
  const houseId = route.query.houseId
  if (houseId) {
    loadVrScenes()
    fetchHouseDetail(houseId)
  } else {
    alert('未指定房屋')
    router.push('/')
  }
  
  // 延迟获取渲染器引用
  setTimeout(() => {
    if (vrHouseTour.value && vrHouseTour.value.getRenderer) {
      vrRenderer.value = vrHouseTour.value.getRenderer()
    }
  }, 1000)
})
</script>

<style scoped>
/* 工业极简风格 - Industrial Minimal Design */
.house-tour-page {
  position: relative;
  height: 100vh;
  overflow: hidden;
  font-family: 'Inter', system-ui, sans-serif;
}

/* 顶部导航栏 */
.navbar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background-color: #111827;
  color: #FFFFFF;
  z-index: 100;
  border-bottom: 1px solid #E5E7EB;
}

.nav-brand h2 {
  margin: 0;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.nav-links {
  display: flex;
  gap: 0;
  align-items: center;
}

.nav-link {
  color: #FFFFFF;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 0;
  transition: background-color 0.2s;
  cursor: pointer;
  font-size: 14px;
  border: 1px solid transparent;
  background: transparent;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
}

.control-btn {
  background-color: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  cursor: pointer;
  font-size: 14px;
  color: #FFFFFF;
}

.control-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 场景选择面板 */
.scene-panel {
  position: absolute;
  top: 60px;
  right: 20px;
  background: #FFFFFF;
  padding: 16px;
  border-radius: 0;
  border: 1px solid #E5E7EB;
  z-index: 90;
  max-width: 280px;
}

.scene-panel h3 {
  margin: 0 0 12px 0;
  color: #111827;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.scene-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.scene-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 0;
  cursor: pointer;
  transition: background-color 0.2s;
  border: 1px solid transparent;
  background: #FFFFFF;
}

.scene-item:hover {
  background-color: #F9FAFB;
  border-color: #E5E7EB;
}

.scene-item.active {
  background-color: #F9FAFB;
  border-color: #111827;
  border-left-width: 3px;
}

.scene-thumb {
  width: 48px;
  height: 48px;
  border-radius: 0;
  object-fit: cover;
  border: 1px solid #E5E7EB;
}

.scene-title {
  font-size: 13px;
  color: #111827;
  font-weight: 500;
}

.scene-item.active .scene-title {
  font-weight: 600;
}

.instructions {
  position: absolute;
  bottom: 80px;
  left: 20px;
  background: #FFFFFF;
  padding: 16px;
  border-radius: 0;
  border: 1px solid #E5E7EB;
  z-index: 90;
  max-width: 250px;
}

.instructions h3 {
  margin: 0 0 8px 0;
  color: #111827;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.instructions ul {
  margin: 0;
  padding-left: 1.2rem;
  color: #374151;
  font-size: 13px;
  line-height: 1.5;
}

.instructions li {
  margin-bottom: 0.3rem;
}
</style>