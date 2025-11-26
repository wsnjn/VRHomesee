<template>
  <div ref="container" class="earth-container"></div>
</template>

<script>
import * as THREE from 'three';
import { ref, onMounted, onUnmounted } from 'vue';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';

export default {
  name: 'EarthModel',
  setup() {
    const container = ref(null);
    let scene, camera, renderer;
    let earthModel;
    let frameId;

    const initScene = () => {
      if (!container.value) return;

      // 创建场景
      scene = new THREE.Scene();
      scene.background = new THREE.Color(0x1a1a2e);

      // 创建相机 - 拉远视角
      camera = new THREE.PerspectiveCamera(75, container.value.clientWidth / container.value.clientHeight, 0.1, 1000);
      camera.position.set(0, 0, 5);

      // 创建渲染器
      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
      renderer.setSize(container.value.clientWidth, container.value.clientHeight);
      renderer.setPixelRatio(window.devicePixelRatio);
      container.value.appendChild(renderer.domElement);

      // 添加灯光
      const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
      scene.add(ambientLight);

      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
      directionalLight.position.set(5, 3, 5);
      scene.add(directionalLight);

      // 加载地球模型
      loadEarthModel();

      // 开始动画循环
      animate();

      // 窗口大小变化监听
      window.addEventListener('resize', onWindowResize);
    };

    const loadEarthModel = () => {
      const loader = new GLTFLoader();
      
      // 加载地球模型
      loader.load(
        '/models/earth 2.glb',
        (gltf) => {
          earthModel = gltf.scene;
          
          // 调整模型大小和位置
          earthModel.scale.set(1.5, 1.5, 1.5);
          earthModel.position.set(0, 0, 0);
          
          // 添加自转动画
          earthModel.userData.rotationSpeed = 0.002;
          
          scene.add(earthModel);
          console.log('地球模型加载成功');
        },
        (progress) => {
          // 加载进度回调
          console.log('加载进度:', (progress.loaded / progress.total * 100) + '%');
        },
        (error) => {
          console.error('地球模型加载失败:', error);
          // 如果模型加载失败，创建一个简单的地球作为备用
          createFallbackEarth();
        }
      );
    };

    const createFallbackEarth = () => {
      // 创建备用地球（简单的球体）
      const geometry = new THREE.SphereGeometry(2, 32, 32);
      
      // 创建地球纹理材质
      const textureLoader = new THREE.TextureLoader();
      const earthTexture = textureLoader.load('/src/assets/image/1.png'); // 使用现有图片作为备用纹理
      
      const material = new THREE.MeshPhongMaterial({
        map: earthTexture,
        specular: 0x333333,
        shininess: 5
      });
      
      earthModel = new THREE.Mesh(geometry, material);
      earthModel.userData.rotationSpeed = 0.005;
      scene.add(earthModel);
    };

    const animate = () => {
      frameId = requestAnimationFrame(animate);

      // 地球自转
      if (earthModel) {
        earthModel.rotation.y += earthModel.userData.rotationSpeed;
      }

      renderer.render(scene, camera);
    };

    const onWindowResize = () => {
      if (!container.value || !camera || !renderer) return;
      
      camera.aspect = container.value.clientWidth / container.value.clientHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(container.value.clientWidth, container.value.clientHeight);
    };

    onMounted(() => {
      initScene();
    });

    onUnmounted(() => {
      if (frameId) {
        cancelAnimationFrame(frameId);
      }
      if (renderer) {
        renderer.dispose();
      }
      if (scene) {
        scene.clear();
      }
      window.removeEventListener('resize', onWindowResize);
    });

    return {
      container
    };
  }
};
</script>

<style scoped>
.earth-container {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}
</style>
