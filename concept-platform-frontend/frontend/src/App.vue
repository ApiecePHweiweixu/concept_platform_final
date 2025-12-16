<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <div class="app-bg"></div>
    <RouterView />
  </div>
</template>

<style>
.app-shell {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.app-bg {
  position: fixed;
  inset: 0;
  background:
    radial-gradient(1200px circle at 20% 10%, rgba(97, 160, 255, 0.18), transparent 35%),
    radial-gradient(900px circle at 80% 0%, rgba(47, 106, 230, 0.14), transparent 40%),
    radial-gradient(1200px circle at 50% 80%, rgba(29, 101, 241, 0.12), transparent 45%),
    linear-gradient(180deg, #f8fbff 0%, #eef2fb 100%);
  pointer-events: none;
  z-index: 0;
}

.app-shell > :not(.app-bg) {
  position: relative;
  z-index: 1;
}
</style>
