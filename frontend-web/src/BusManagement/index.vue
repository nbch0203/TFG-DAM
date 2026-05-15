<script setup>
// Archivo proxy: solo reusa el componente real de gestión de autobuses.
// Esto sirve para importar la vista desde un punto más corto y limpio.
import BusManagement from './BusManagement.vue';
</script>
<template>
  <!-- Aquí solo se pinta el componente principal -->
  <BusManagement />
</template>
