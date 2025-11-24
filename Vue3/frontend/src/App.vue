<script setup>
import { onMounted } from 'vue'
import { userState } from './state/user'
import ThreeModelViewer from './views/ThreeModelViewer.vue'

onMounted(() => {
  userState.loadFromStorage()
})
</script>

<template>
  <RouterView />
  <ThreeModelViewer />
</template>

<style scoped></style>
