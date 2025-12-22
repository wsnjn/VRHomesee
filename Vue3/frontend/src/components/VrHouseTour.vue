<template>
  <div class="vr-house-tour">
    <div ref="container" class="vr-container"></div>
    
    <!-- Loading Overlay -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p class="loading-text">加载全景图中...</p>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: loadProgress + '%' }"></div>
        </div>
        <p class="progress-text">{{ loadProgress }}%</p>
      </div>
    </div>
    
    <div class="scene-info">
      <h3>{{ currentScene?.scene?.title || '未知场景' }}</h3>
    </div>
  </div>
</template>

<script>
import * as THREE from 'three';
import { ref, onMounted, onUnmounted, reactive, toRefs } from 'vue';

export default {
  name: 'VrHouseTour',
  props: {
    naviData: {
      type: Array,
      required: true,
      default: () => []
    },
    initialSceneKey: {
      type: String,
      default: null
    }
  },
  setup(props) {
    const container = ref(null);
    let scene, camera, renderer, controls;
    let sphere;
    const mouse = new THREE.Vector2();
    const raycaster = new THREE.Raycaster();
    
    const state = reactive({
      currentScene: null,
      currentSceneIndex: 0,
      overlays: [],
      isLoading: false,
      loadProgress: 0
    });

    // 初始化场景
    const initScene = () => {
      if (!container.value) return;

      // 创建场景
      scene = new THREE.Scene();
      scene.background = new THREE.Color(0x222222);

      // 创建渲染器 - preserveDrawingBuffer 用于截图
      renderer = new THREE.WebGLRenderer({ 
        antialias: true,
        preserveDrawingBuffer: true  // 重要：允许截图
      });
      renderer.setSize(container.value.clientWidth, container.value.clientHeight);
      renderer.setPixelRatio(window.devicePixelRatio);
      container.value.appendChild(renderer.domElement);

      // 创建相机 - 直接位于球体内部
      camera = new THREE.PerspectiveCamera(75, container.value.clientWidth / container.value.clientHeight, 0.1, 1000);
      camera.position.set(0, -20, -1);

      // 添加环境光
      const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
      scene.add(ambientLight);

      // 设置初始场景
      setInitialScene();

      // 添加控制器
      setupOrbitControls();

      // 添加坐标轴辅助线
      const axesHelper = new THREE.AxesHelper(150);
      scene.add(axesHelper);

      // 开始动画循环
      animate();

      // 窗口大小变化监听
      window.addEventListener('resize', onWindowResize);
    };

    // 设置控制器
    const setupOrbitControls = () => {
      try {
        controls = {
          update: () => {},
          reset: () => {
            camera.position.set(0, -20, -1);
            camera.lookAt(0, 0, 0);
          }
        };
        
        let isMouseDown = false;
        let previousMousePosition = { x: 0, y: 0 };
        
        const onMouseDown = (event) => {
          isMouseDown = true;
          previousMousePosition = { x: event.clientX, y: event.clientY };
        };
        
        const onMouseMove = (event) => {
          if (!isMouseDown) return;
          
          const deltaX = event.clientX - previousMousePosition.x;
          const deltaY = event.clientY - previousMousePosition.y;
          
          if (sphere) {
            sphere.rotation.y += deltaX * 0.01;
            sphere.rotation.x += deltaY * 0.01;
          }
          
          previousMousePosition = { x: event.clientX, y: event.clientY };
        };
        
        const onMouseUp = () => {
          isMouseDown = false;
        };
        
        if (container.value) {
          container.value.addEventListener('mousedown', onMouseDown);
          container.value.addEventListener('mousemove', onMouseMove);
          container.value.addEventListener('mouseup', onMouseUp);
          
          // 触摸支持
          container.value.addEventListener('touchstart', (e) => {
            if (e.touches.length === 1) {
              onMouseDown({ clientX: e.touches[0].clientX, clientY: e.touches[0].clientY });
            }
          });
          container.value.addEventListener('touchmove', (e) => {
            if (e.touches.length === 1) {
              onMouseMove({ clientX: e.touches[0].clientX, clientY: e.touches[0].clientY });
            }
          });
          container.value.addEventListener('touchend', onMouseUp);
        }
        
        console.log('鼠标控制器已配置');
      } catch (error) {
        console.error('控制器设置失败:', error);
      }
    };

    // 设置初始场景
    const setInitialScene = () => {
      if (props.naviData.length === 0) return;

      let initialSceneIndex = 0;
      if (props.initialSceneKey) {
        initialSceneIndex = props.naviData.findIndex(item => 
          item.scene.photo_key === props.initialSceneKey
        );
      } else {
        initialSceneIndex = props.naviData.findIndex(item => 
          item.scene.is_main === 1
        );
      }

      if (initialSceneIndex === -1) initialSceneIndex = 0;
      
      state.currentSceneIndex = initialSceneIndex;
      state.currentScene = props.naviData[initialSceneIndex];
      
      loadScene(state.currentScene);
    };

    // 加载场景
    const loadScene = (sceneData) => {
      if (sphere) scene.remove(sphere);

      const { scene: sceneInfo } = sceneData;
      
      if (sceneInfo.sphereSource && sceneInfo.sphereSource.thumb) {
        loadSphereTexture(sceneInfo.sphereSource.thumb);
      }

      if (sceneInfo.rotation) {
        if (controls) {
          controls.reset();
        }
      }

      renderOverlays(sceneData.overlays || []);
    };

    // 加载球面贴图（带进度追踪）
    const loadSphereTexture = (textureUrl) => {
      state.isLoading = true;
      state.loadProgress = 0;
      
      const textureLoader = new THREE.TextureLoader();
      textureLoader.load(
        textureUrl, 
        // onLoad
        (texture) => {
          const geometry = new THREE.SphereGeometry(100, 50, 50);
          geometry.scale(-1, 1, 1);
          
          const material = new THREE.MeshBasicMaterial({
            map: texture,
            side: THREE.DoubleSide,
            color: 0xffffff
          });
          
          sphere = new THREE.Mesh(geometry, material);
          scene.add(sphere);

          console.log('球体网格创建完成');
          
          state.loadProgress = 100;
          setTimeout(() => {
            state.isLoading = false;
          }, 300);
        }, 
        // onProgress
        (xhr) => {
          if (xhr.lengthComputable) {
            state.loadProgress = Math.round((xhr.loaded / xhr.total) * 100);
          } else {
            state.loadProgress = Math.min(state.loadProgress + 10, 90);
          }
        },
        // onError
        (error) => {
          console.error('全景图纹理加载失败:', error);
          state.isLoading = false;
        }
      );
    };

    // 渲染导航点
    const renderOverlays = (overlays) => {
      state.overlays.forEach(overlay => {
        if (overlay.mesh) scene.remove(overlay.mesh);
      });
      
      state.overlays = [];

      overlays.forEach(overlay => {
        createOverlay(overlay);
      });
    };

    // 创建导航点
    const createOverlay = (overlayData) => {
      const geometry = new THREE.SphereGeometry(5, 16, 16);
      const material = new THREE.MeshBasicMaterial({ 
        color: 0xff0000,
        transparent: true,
        opacity: 0.7
      });
      
      const mesh = new THREE.Mesh(geometry, material);
      
      const phi = (90 - overlayData.y) * Math.PI / 180;
      const theta = (overlayData.x + 180) * Math.PI / 180;
      
      mesh.position.x = 400 * Math.sin(phi) * Math.cos(theta);
      mesh.position.y = 400 * Math.cos(phi);
      mesh.position.z = 400 * Math.sin(phi) * Math.sin(theta);
      
      mesh.userData = {
        type: 'overlay',
        nextSceneKey: overlayData.next_photo_key,
        title: overlayData.title
      };
      
      scene.add(mesh);
      state.overlays.push({
        ...overlayData,
        mesh: mesh
      });
    };

    // 检查交点（点击导航点）
    const checkIntersections = (event) => {
      const rect = container.value.getBoundingClientRect();
      mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
      mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
      
      raycaster.setFromCamera(mouse, camera);
      
      const intersects = raycaster.intersectObjects(
        state.overlays.map(overlay => overlay.mesh)
      );
      
      if (intersects.length > 0) {
        const overlayMesh = intersects[0].object;
        const overlayData = state.overlays.find(overlay => overlay.mesh === overlayMesh);
        
        if (overlayData && overlayData.next_photo_key) {
          walkTo(overlayData.next_photo_key);
        }
      }
    };

    // 跳转到其他场景
    const walkTo = (sceneKey) => {
      const newSceneIndex = props.naviData.findIndex(item => 
        item.scene.photo_key === sceneKey
      );
      
      if (newSceneIndex !== -1) {
        state.currentSceneIndex = newSceneIndex;
        state.currentScene = props.naviData[newSceneIndex];
        loadScene(state.currentScene);
      }
    };

    // 窗口大小改变事件
    const onWindowResize = () => {
      if (!container.value || !camera || !renderer) return;
      
      camera.aspect = container.value.clientWidth / container.value.clientHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(container.value.clientWidth, container.value.clientHeight);
    };

    // 动画循环
    const animate = () => {
      requestAnimationFrame(animate);
      
      if (controls) {
        controls.update();
      }
      
      renderer.render(scene, camera);
    };

    // 添加点击事件监听
    const setupClickListeners = () => {
      if (!container.value) return;
      container.value.addEventListener('click', checkIntersections);
    };

    onMounted(() => {
      initScene();
      setupClickListeners();
    });

    onUnmounted(() => {
      if (renderer) {
        renderer.dispose();
      }
      if (scene) {
        scene.clear();
      }
      if (container.value) {
        container.value.removeEventListener('click', checkIntersections);
      }
      window.removeEventListener('resize', onWindowResize);
    });

    return {
      container,
      ...toRefs(state),
      walkTo,
      // 暴露渲染器获取方法（用于截图）
      getRenderer: () => renderer
    };
  }
};
</script>

<style scoped>
.vr-house-tour {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.vr-container {
  width: 100%;
  height: 100%;
}

/* Loading Overlay Styles */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(17, 24, 39, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.loading-content {
  text-align: center;
  color: white;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top-color: #3B82F6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 16px;
  margin-bottom: 16px;
  color: #E5E7EB;
}

.progress-bar {
  width: 200px;
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  overflow: hidden;
  margin: 0 auto 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #60A5FA);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  color: #9CA3AF;
}

.scene-info {
  position: absolute;
  top: 20px;
  left: 20px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px 15px;
  border-radius: 5px;
  z-index: 10;
}

.scene-info h3 {
  margin: 0;
  font-size: 16px;
}
</style>
