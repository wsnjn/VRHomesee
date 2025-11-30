<template>
  <div class="interactive-cube-container">
    <div class="cube-header">
      <h1 class="glitch-text" data-text="INTERACTIVE_CUBE">INTERACTIVE_CUBE</h1>
      <div class="subtitle">交互方块 // 多维交互实验场</div>
    </div>

    <div class="modules-grid">
      <!-- 虚拟世界模块卡片 -->
      <div class="module-card" @click="navigateTo('virtual-world')">
        <div class="card-bg"></div>
        <div class="card-content">
          <div class="icon">🌌</div>
          <h3>虚拟世界</h3>
          <p>DEEP SPACE ODYSSEY</p>
          <div class="tags">
            <span>VR</span>
            <span>HAND_TRACKING</span>
            <span>VOICE</span>
          </div>
        </div>
        <div class="card-border"></div>
      </div>

      <!-- 圣诞树模块卡片 -->
      <div class="module-card" @click="navigateTo('christmas-tree')">
        <div class="card-bg"></div>
        <div class="card-content">
          <div class="icon">🎄</div>
          <h3>交互圣诞树</h3>
          <p>INTERACTIVE TREE</p>
          <div class="tags">
            <span>GESTURE</span>
            <span>PARTICLES</span>
            <span>HOLIDAY</span>
          </div>
        </div>
        <div class="card-border"></div>
      </div>
       <div class="module-card locked">
        <div class="card-bg"></div>
        <div class="card-content">
          <div class="icon">🔒</div>
          <h3>COMING SOON</h3>
          <p>更多模组开发中...</p>
        </div>
        <div class="card-border"></div>
      </div>
    </div>

    <div class="back-btn" @click="$router.push('/')">
      RETURN_HOME
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateTo = (route) => {
  router.push(`/interactive-cube/${route}`)
}
</script>

<style scoped>
.interactive-cube-container {
  width: 100vw;
  height: 100vh;
  background: #000;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-family: 'Rajdhani', sans-serif;
  background-image: 
    radial-gradient(circle at 50% 50%, rgba(0, 20, 40, 0.8) 0%, #000 100%),
    linear-gradient(rgba(0, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 100% 100%, 50px 50px, 50px 50px;
}

.cube-header {
  text-align: center;
  margin-bottom: 60px;
  z-index: 2;
}

h1 {
  font-size: 4rem;
  font-weight: 700;
  letter-spacing: 10px;
  margin: 0;
  color: #00f2ff;
  text-shadow: 0 0 20px rgba(0, 242, 255, 0.5);
}

.subtitle {
  font-size: 1.5rem;
  color: #aaa;
  letter-spacing: 5px;
  margin-top: 10px;
}

.modules-grid {
  display: flex;
  gap: 40px;
  z-index: 2;
}

.module-card {
  width: 300px;
  height: 400px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  perspective: 1000px;
}

.module-card:hover {
  transform: translateY(-10px);
}

.card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 20, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 242, 255, 0.2);
  clip-path: polygon(20px 0, 100% 0, 100% calc(100% - 20px), calc(100% - 20px) 100%, 0 100%, 0 20px);
  transition: all 0.3s ease;
}

.module-card:hover .card-bg {
  background: rgba(0, 40, 80, 0.8);
  border-color: #00f2ff;
  box-shadow: 0 0 30px rgba(0, 242, 255, 0.2);
}

.card-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.icon {
  font-size: 4rem;
  margin-bottom: 20px;
  filter: drop-shadow(0 0 10px rgba(0, 242, 255, 0.5));
}

h3 {
  font-size: 1.8rem;
  margin: 0;
  color: #fff;
  letter-spacing: 2px;
}

p {
  color: #00f2ff;
  font-size: 0.9rem;
  letter-spacing: 1px;
  margin-top: 5px;
}

.tags {
  margin-top: 30px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.tags span {
  font-size: 0.7rem;
  padding: 4px 8px;
  background: rgba(0, 242, 255, 0.1);
  border: 1px solid rgba(0, 242, 255, 0.3);
  color: #00f2ff;
}

.module-card.locked {
  opacity: 0.5;
  cursor: not-allowed;
}

.module-card.locked:hover {
  transform: none;
}

.back-btn {
  margin-top: 60px;
  padding: 10px 30px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #aaa;
  cursor: pointer;
  letter-spacing: 2px;
  transition: all 0.3s ease;
  z-index: 2;
}

.back-btn:hover {
  color: #fff;
  border-color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

/* Glitch Effect */
.glitch-text {
  position: relative;
}

.glitch-text::before,
.glitch-text::after {
  content: attr(data-text);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.glitch-text::before {
  left: 2px;
  text-shadow: -1px 0 #ff00c1;
  clip: rect(44px, 450px, 56px, 0);
  animation: glitch-anim 5s infinite linear alternate-reverse;
}

.glitch-text::after {
  left: -2px;
  text-shadow: -1px 0 #00fff9;
  clip: rect(44px, 450px, 56px, 0);
  animation: glitch-anim2 5s infinite linear alternate-reverse;
}

@keyframes glitch-anim {
  0% { clip: rect(35px, 9999px, 11px, 0); }
  20% { clip: rect(68px, 9999px, 96px, 0); }
  40% { clip: rect(12px, 9999px, 58px, 0); }
  60% { clip: rect(84px, 9999px, 3px, 0); }
  80% { clip: rect(55px, 9999px, 26px, 0); }
  100% { clip: rect(9px, 9999px, 73px, 0); }
}

@keyframes glitch-anim2 {
  0% { clip: rect(15px, 9999px, 91px, 0); }
  20% { clip: rect(58px, 9999px, 46px, 0); }
  40% { clip: rect(92px, 9999px, 18px, 0); }
  60% { clip: rect(4px, 9999px, 83px, 0); }
  80% { clip: rect(25px, 9999px, 66px, 0); }
  100% { clip: rect(79px, 9999px, 33px, 0); }
}
</style>
