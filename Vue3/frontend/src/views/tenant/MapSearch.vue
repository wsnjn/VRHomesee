<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import Navbar from '../../components/Navbar.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const map = ref(null)
const rooms = ref([])
const loading = ref(true)

// Searching & Filtering State
const searchText = ref('')
const activeFilters = ref([])
let allRooms = [] // Store full dataset locally
let markers = [] // Store marker instances for cleanup
let cluster = null // Store cluster instance
let AMapObj = null // Store AMap class reference
let placeSearch = null // AMap PlaceSearch instance

// AMap Key & Security Code
const AMAP_KEY = 'f912b11737cbc1bd7c50a495e2112315'
const SECURITY_CODE = '8f786a32e50cacab3211684f59e10c3c'

window._AMapSecurityConfig = {
  securityJsCode: SECURITY_CODE,
}

const initMap = () => {
  AMapLoader.load({
    key: AMAP_KEY,
    version: '2.0',
    plugins: ['AMap.ToolBar', 'AMap.Scale', 'AMap.Geocoder', 'AMap.MarkerCluster', 'AMap.PlaceSearch'],
  })
    .then((AMap) => {
      AMapObj = AMap // Save for later use in filters
      map.value = new AMap.Map('map-container', {
        viewMode: '3D',
        zoom: 11,
        center: [114.057868, 22.543099], // Default: Shenzhen
        mapStyle: 'amap://styles/whitesmoke',
      })

      map.value.addControl(new AMap.ToolBar())
      map.value.addControl(new AMap.Scale())
      
      // Initialize PlaceSearch
      placeSearch = new AMap.PlaceSearch({
          type: '地铁站',
          pageSize: 1,
          extensions: 'base'
      })

      fetchRooms()
    })
    .catch((e) => {
      console.error(e)
    })
}

const fetchRooms = async () => {
  try {
    console.log('Fetching rooms from API...')
    const response = await fetch('https://api.homesee.xyz/api/rooms/available')
    if (response.ok) {
      const data = await response.json()
      console.log('API Response:', data)
      if (data.success && data.rooms) {
          allRooms = data.rooms
          rooms.value = data.rooms
          console.log(`Loaded ${allRooms.length} rooms`)
          applyFilters()
      } else {
        console.warn('Unexpected API response structure:', data)
      }
    } else {
        console.error('API Error:', response.status)
    }
  } catch (error) {
    console.error('Failed to fetch rooms:', error)
  } finally {
    loading.value = false
  }
}

// Logic to apply search and filters
const applyFilters = () => {
    let result = allRooms
    console.log('Applying filters. Total rooms:', allRooms.length)

    // 1. Search Text
    if (searchText.value) {
        const key = searchText.value.toLowerCase()
        result = result.filter(r => 
            (r.communityName && r.communityName.toLowerCase().includes(key)) ||
            (r.district && r.district.toLowerCase().includes(key)) ||
            (r.street && r.street.toLowerCase().includes(key))
        )
    }

    // 2. Rental Type
    if (activeFilters.value.includes('whole')) {
        result = result.filter(r => r.rentalType === 0)
    }
    if (activeFilters.value.includes('shared')) {
        result = result.filter(r => r.rentalType === 1)
    }
    if (activeFilters.value.includes('single')) {
        result = result.filter(r => r.rentalType === 2)
    }

    // 3. Mock Tags (Subway)
    if (activeFilters.value.includes('subway')) {
        result = result.filter(r => (r.description && r.description.includes('地铁'))) 
    }

    // 4. Sort
    if (activeFilters.value.includes('price_desc')) {
        result = [...result].sort((a, b) => b.rentPrice - a.rentPrice)
    }

    rooms.value = result
    console.log(`Filters applied. Showing ${result.length} rooms`)
    renderMarkers(result)
}

const handleSearch = () => {
    applyFilters()
}

// Renamed from toggleFilter to avoid collisions
const onFilterClick = (tag) => {
    if (activeFilters.value.includes(tag)) {
        activeFilters.value = activeFilters.value.filter(t => t !== tag)
    } else {
        if (['whole', 'shared', 'single'].includes(tag)) {
             activeFilters.value = activeFilters.value.filter(t => !['whole', 'shared', 'single'].includes(t))
        }
        activeFilters.value.push(tag)
    }
    applyFilters()
}

const progressMsg = ref('')
const isProcessing = ref(false)
const processedCount = ref(0)
const totalCount = ref(0)
const btnText = ref('AI 完善数据')

const renderMarkers = (roomList) => {
    if (!map.value || !AMapObj) return

    // Clear existing
    if (cluster) {
        cluster.setMap(null)
        cluster = null
    }
    map.value.clearMap()
    markers = []
    
    if (!roomList || roomList.length === 0) {
        return
    }

    // Render what we have
    roomList.forEach((room) => {
        try {
            const dbLat = parseFloat(room.latitude)
            const dbLng = parseFloat(room.longitude)
            
            if (!isNaN(dbLat) && !isNaN(dbLng)) {
                // Valid Data -> Show Exact
                createMarker(room, [dbLng, dbLat])
            } else {
                // Invalid Data -> Random Soft Fallback
                const centerLng = 114.057868 + (Math.random() - 0.5) * 0.1
                const centerLat = 22.543099 + (Math.random() - 0.5) * 0.1
                createMarker(room, [centerLng, centerLat])
            }
        } catch (e) {
            console.warn('Error rendering room:', room, e)
        }
    })
    
    updateCluster()
}

// Persistent Data Completion
const startAICompletion = async () => {
    if (isProcessing.value) return
    isProcessing.value = true
    btnText.value = '准备中...'
    
    // Filter rooms needing completion
    const incompleteRooms = allRooms.filter(r => 
        !r.latitude || !r.longitude || isNaN(parseFloat(r.latitude))
    )
    
    if (incompleteRooms.length === 0) {
        progressMsg.value = '所有房源数据已完善！'
        btnText.value = '✓ 数据已完善'
        setTimeout(() => {
            progressMsg.value = ''
            btnText.value = 'AI 完善数据'
        }, 3000)
        isProcessing.value = false
        return
    }

    const CONCURRENCY = 5
    const queue = [...incompleteRooms]
    totalCount.value = queue.length
    processedCount.value = 0
    let successCount = 0

    const geocoder = new AMapObj.Geocoder({ city: '全国' })

    const processItem = (room) => {
        return new Promise((resolve) => {
            const components = [
                room.city || '',
                room.district || '',
                room.street || '',
                room.communityName || ''
            ].filter(Boolean)
            const address = components.join('')

            geocoder.getLocation(address, async (status, result) => {
                if (status === 'complete' && result.geocodes.length) {
                    const lng = result.geocodes[0].location.lng
                    const lat = result.geocodes[0].location.lat
                    
                    // Update Local
                    room.longitude = lng
                    room.latitude = lat
                    
                    // Persist to Backend
                    try {
                        const res = await fetch(`https://api.homesee.xyz/api/rooms/update/${room.id}`, {
                            method: 'PUT',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(room)
                        })
                        if (res.ok) successCount++
                    } catch (e) {
                        console.error('Update failed for room ' + room.id, e)
                    }
                }
                resolve()
            })
        })
    }

    while (queue.length > 0) {
        const batch = queue.splice(0, CONCURRENCY)
        await Promise.all(batch.map(r => processItem(r)))
        
        processedCount.value += batch.length
        
        // Update both Button and Toast using refs
        const status = `正在完善 ${processedCount.value}/${totalCount.value}`
        progressMsg.value = status
        btnText.value = `… ${status}`
        
        // Refresh map periodically to show progress
        renderMarkers(allRooms) 
        
        await new Promise(r => setTimeout(r, 200))
    }

    isProcessing.value = false
    const finalMsg = `成功修复 ${successCount} 个房源`
    progressMsg.value = finalMsg
    btnText.value = `✓ ${finalMsg}`
    
    setTimeout(() => {
        progressMsg.value = ''
        btnText.value = 'AI 完善数据'
    }, 4000)
    
    // Final refresh
    fetchRooms()
}

const updateCluster = () => {
    if (!map.value || !AMapObj) return
    
    console.log(`Updating cluster with ${markers.length} markers`)

    // DEBUG: Skip cluster, add markers directly to map
    markers.forEach(m => {
        m.setMap(map.value)
    })
    console.log('Markers added directly to map (cluster bypassed)')

    // Fit view to show all markers!
    if (markers.length > 0) {
        map.value.setFitView(markers)
    }
}

const createMarker = (room, position, addToMapNow = false) => {
    if (!AMapObj) return

    const markerContent = `
        <div class="custom-marker">
            <div class="marker-price">¥${room.rentPrice}</div>
            <div class="marker-arrow"></div>
        </div>
    `
    // Add Jitter only for display to avoid perfect overlap
    const displayPos = [
        position[0] + (Math.random() - 0.5) * 0.0001,
        position[1] + (Math.random() - 0.5) * 0.0001
    ]

    const marker = new AMapObj.Marker({
        position: displayPos,
        content: markerContent,
        offset: new AMapObj.Pixel(-24, -30),
        extData: room
    })

    marker.on('click', () => {
        openInfoWindow(marker, room)
    })

    markers.push(marker)
}

const openInfoWindow = (marker, room) => {
    const basicContent = `
        <div class="info-window" id="info-window-${room.id}">
            <div class="info-header">
                <h4>${room.communityName} ${room.roomNumber || ''}</h4>
                <span class="info-price">¥${room.rentPrice}/月</span>
            </div>
            <div class="info-body">
                <p>${room.bedroomCount || 1}室${room.parlorCount || 1}厅 | ${room.roomArea}㎡ | ${room.direction || '南'}</p>
                <div class="info-tags">
                   ${room.tags ? room.tags.split(',').map(tag => `<span>${tag}</span>`).join('') : '<span>随时看房</span>'}
                </div>
                <div class="ai-analysis" style="margin-top:8px; padding-top:8px; border-top:1px dashed #eee;">
                    <p style="color:#2563EB; font-weight:bold; font-size:12px;"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:4px;"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>AI周边配套分析中...</p>
                </div>
            </div>
            <div class="info-footer">
                <button onclick="window.location.hash = '#/house-tour?houseId=${room.id}'">查看详情</button>
            </div>
        </div>
    `
    
    const infoWindow = new AMapObj.InfoWindow({
        content: basicContent,
        offset: new AMapObj.Pixel(0, -35),
        isCustom: false
    })

    infoWindow.open(map.value, marker.getPosition())

    // Search for multiple POI types - using SVG icons
    const svgIcons = {
        subway: '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><rect x="4" y="3" width="16" height="16" rx="2"/><path d="M12 19v3"/><path d="M8 22h8"/><circle cx="9" cy="13" r="1"/><circle cx="15" cy="13" r="1"/><path d="M7 7h10"/></svg>',
        bus: '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><path d="M8 6v6"/><path d="M16 6v6"/><rect x="4" y="3" width="16" height="14" rx="2"/><path d="M4 11h16"/><circle cx="8" cy="19" r="2"/><circle cx="16" cy="19" r="2"/></svg>',
        hospital: '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2"><path d="M12 6v6"/><path d="M9 9h6"/><rect x="3" y="3" width="18" height="18" rx="2"/></svg>',
        supermarket: '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>'
    }
    const poiTypes = [
        { type: '地铁站', icon: svgIcons.subway, key: 'subway' },
        { type: '公交站', icon: svgIcons.bus, key: 'bus' },
        { type: '医院', icon: svgIcons.hospital, key: 'hospital' },
        { type: '超市', icon: svgIcons.supermarket, key: 'supermarket' }
    ]
    
    const results = {}
    let completed = 0

    poiTypes.forEach(poi => {
        const searcher = new AMapObj.PlaceSearch({
            type: poi.type,
            pageSize: 1,
            extensions: 'base'
        })
        
        searcher.searchNearBy('', marker.getPosition(), 1000, (status, result) => {
            if (status === 'complete' && result.poiList && result.poiList.pois.length > 0) {
                const nearest = result.poiList.pois[0]
                results[poi.key] = {
                    icon: poi.icon,
                    name: nearest.name,
                    distance: nearest.distance
                }
            }
            
            completed++
            
            // All searches done, render results
            if (completed === poiTypes.length) {
                const analysisDiv = document.querySelector(`#info-window-${room.id} .ai-analysis`)
                if (!analysisDiv) return
                
                const entries = Object.values(results)
                if (entries.length > 0) {
                    let html = '<div style="display:flex; flex-wrap:wrap; gap:6px;">'
                    entries.forEach(r => {
                        html += `
                            <div style="display:flex; align-items:center; gap:2px; background:#f3f4f6; padding:2px 6px; border-radius:4px; font-size:11px;">
                                <span>${r.icon}</span>
                                <span style="color:#333; max-width:80px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${r.name}</span>
                                <span style="color:#888;">${r.distance}m</span>
                            </div>
                        `
                    })
                    html += '</div>'
                    analysisDiv.innerHTML = html
                } else {
                    analysisDiv.innerHTML = `<p style="color:#999; font-size:12px;">附近1km暂无配套设施</p>`
                }
            }
        })
    })
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map.value) {
    map.value.destroy()
  }
})
</script>

<template>
  <div class="map-search-container">
    <Navbar />
    <div class="map-wrapper">
        <div id="map-container"></div>
        
        <!-- AI Progress Bar -->
        <div v-if="progressMsg" class="ai-progress-toast">
            <div class="spinner"></div>
            <span>{{ progressMsg }}</span>
        </div>

        <!-- Quick Filter Overlay -->
        <div class="search-overlay">
            <div class="search-actions-row">
                <div class="search-bar">
                    <input 
                        type="text" 
                        v-model="searchText"
                        @keyup.enter="handleSearch"
                        placeholder="输入区域、地铁、小区名找房..." 
                    />
                    <button class="search-btn" @click="handleSearch">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                    </button>
                </div>
                <button class="ai-btn" @click="startAICompletion" :disabled="isProcessing">
                    {{ btnText }}
                </button>
            </div>

            <div class="filter-tags">
                <button :class="{ active: activeFilters.includes('whole') }" @click="onFilterClick('whole')">整租</button>
                <button :class="{ active: activeFilters.includes('shared') }" @click="onFilterClick('shared')">合租</button>
                <button :class="{ active: activeFilters.includes('single') }" @click="onFilterClick('single')">单间</button>
                <button :class="{ active: activeFilters.includes('subway') }" @click="onFilterClick('subway')">近地铁</button>
                <button :class="{ active: activeFilters.includes('price_desc') }" @click="onFilterClick('price_desc')">价格 &darr;</button>
            </div>
        </div>
    </div>
  </div>
</template>

<style>
/* Global styles for AMap custom elements */
.custom-marker {
    background-color: #2563EB;
    color: white;
    padding: 6px 12px;
    border-radius: 20px;
    font-weight: bold;
    font-size: 14px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.3);
    position: relative;
    white-space: nowrap;
    transition: transform 0.2s;
    cursor: pointer;
}

.custom-marker:hover {
    transform: scale(1.1);
    background-color: #1D4ED8;
    z-index: 999;
}

.marker-arrow {
    position: absolute;
    bottom: -6px;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-top: 6px solid #2563EB;
}

.custom-marker:hover .marker-arrow {
    border-top-color: #1D4ED8;
}

.info-window {
    padding: 8px;
    min-width: 240px;
    font-family: 'Inter', sans-serif;
}

.info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.info-header h4 {
    margin: 0;
    font-size: 16px;
    color: #111827;
}

.info-price {
    color: #2563EB;
    font-weight: 700;
    font-size: 16px;
}

.info-body p {
    color: #6B7280;
    font-size: 13px;
    margin: 4px 0;
}

.info-tags span {
    display: inline-block;
    background: #F3F4F6;
    color: #4B5563;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
    margin-right: 4px;
}

.info-footer {
    margin-top: 12px;
    text-align: right;
}

.info-footer button {
    background: #111827;
    color: white;
    border: none;
    padding: 6px 16px;
    font-size: 12px;
    cursor: pointer;
    border-radius: 2px;
}

.info-footer button:hover {
    background: #374151;
}
</style>

<style scoped>
.map-search-container {
    height: 100vh;
    display: flex;
    flex-direction: column;
}

.map-wrapper {
    flex: 1;
    position: relative;
}

#map-container {
    width: 100%;
    height: 100%;
}

.ai-progress-toast {
    position: absolute;
    top: 90px;
    right: 20px;
    background: rgba(17, 24, 39, 0.9);
    color: white;
    padding: 8px 16px;
    border-radius: 20px;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 8px;
    z-index: 101;
    backdrop-filter: blur(4px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    animation: slideIn 0.3s ease-out;
}

.spinner {
    width: 12px;
    height: 12px;
    border: 2px solid rgba(255,255,255,0.3);
    border-top-color: white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

@keyframes slideIn {
    from { transform: translateY(-10px); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
}

.search-overlay {
    position: absolute;
    top: 88px; /* Correctly clears navbar */
    left: 20px;
    z-index: 100;
    background: white;
    padding: 16px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    min-width: 360px;
}

.search-actions-row {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
}

.search-bar {
    display: flex;
    gap: 8px;
    flex: 1;
}

.search-bar input {
    flex: 1;
    padding: 10px;
    border: 1px solid #E5E7EB;
    border-radius: 4px;
    outline: none;
    font-size: 0.9rem;
}

.search-btn {
    background: #111827;
    color: white;
    border: none;
    width: 40px;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
}

.ai-btn {
    background: #2563EB; /* Royal Blue */
    color: white;
    border: none;
    padding: 0 12px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.2s;
    white-space: nowrap;
}

.ai-btn:hover {
    background: #1D4ED8;
}

.ai-btn:disabled {
    background: #9CA3AF;
    cursor: not-allowed;
}

.filter-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.filter-tags button {
    background: white;
    border: 1px solid #E5E7EB;
    color: #4B5563;
    padding: 4px 12px;
    border-radius: 16px;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s;
}

.filter-tags button:hover {
    border-color: #111827;
    color: #111827;
}

.filter-tags button.active {
    background: #111827;
    color: white;
    border-color: #111827;
}
</style>
