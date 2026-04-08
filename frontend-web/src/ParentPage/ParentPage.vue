<template>
  <div class="parent-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h3>👨‍👩‍👧‍👦 Panel Padre</h3>
      </div>
      <nav>
        <ul>
          <li :class="{active: page==='tracking'}" @click="setPage('tracking')">📍 Seguimiento de autobús</li>
          <li :class="{active: page==='children'}" @click="setPage('children')">👨‍👧 Mis hijos</li>
        </ul>
      </nav>
      <div class="sidebar-footer">
        <p style="font-size: 12px; color: #ccc; margin: 20px 0 0 0;">{{ username }}</p>
      </div>
    </aside>
    <main class="content">
      <div v-if="componentError" class="error-message">
        <h2>Error al cargar el componente</h2>
        <p>{{ componentError }}</p>
      </div>
      <component v-else :is="currentComponent" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onErrorCaptured, onMounted } from 'vue'
import ParentBusTracking from '../ParentBusTracking/ParentBusTracking.vue'
import ParentChildrenManagement from '../ParentChildrenManagement/ParentChildrenManagement.vue'

const props = defineProps({ username: String })
const page = ref(localStorage.getItem('parentPage') || 'tracking')
const componentError = ref('')

console.log('ParentPage: Component loaded')

onMounted(() => {
  console.log('ParentPage: Mounted, current page =', page.value)
})

const setPage = (val) => {
  page.value = val
  componentError.value = ''
  localStorage.setItem('parentPage', val)
}

onErrorCaptured((err, instance, info) => {
  console.error('Error en componente:', err, info)
  componentError.value = `${info}: ${err?.message || 'Error desconocido'}`
  return false
})

const currentComponent = computed(() => {
  try {
    switch(page.value) {
      case 'tracking': return ParentBusTracking
      case 'children': return ParentChildrenManagement
      default: return ParentBusTracking
    }
  } catch (err) {
    console.error('Error al cargar componente:', err)
    componentError.value = err?.message || 'Error al cargar el componente'
    return null
  }
})
</script>

<style scoped>
.parent-layout {
  display: flex;
  min-height: 100vh;
  width: 100vw;
}

.sidebar {
  width: 240px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding-top: 2rem;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 0 2rem 2rem 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.sidebar ul {
  list-style: none;
  padding: 0;
  flex: 1;
}

.sidebar li {
  padding: 1rem 2rem;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.sidebar li:hover {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar li.active {
  background: rgba(255, 255, 255, 0.2);
  border-left-color: #fff;
  font-weight: bold;
}

.sidebar-footer {
  padding: 1rem 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.content {
  margin-left: 240px;
  flex: 1;
  min-height: 100vh;
  padding: 2rem 3vw 2rem 3vw;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.error-message {
  padding: 2rem;
  background: #ffebee;
  border: 1px solid #f48fb1;
  border-radius: 4px;
  color: #c2185b;
}

.error-message h2 {
  margin-top: 0;
}
</style>
