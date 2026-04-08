<template>
  <div class="profesor-layout">
    <aside class="sidebar">
      <nav>
        <ul>
          <li :class="{active: page==='dashboard'}" @click="setPage('dashboard')">Dashboard</li>
          <li :class="{active: page==='messages'}" @click="setPage('messages')">Enviar Mensaje</li>
          <li :class="{active: page==='requests'}" @click="setPage('requests')">Mis Solicitudes</li>
        </ul>
      </nav>
    </aside>
    <main class="content">
      <div v-if="componentError" class="error-message">
        <h2>Error al cargar el componente</h2>
        <p>{{ componentError }}</p>
      </div>
      <component v-else :is="currentComponent" :username="username" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onErrorCaptured, onMounted } from 'vue'
import ProfesorDashboard from './ProfesorDashboard.vue'
import ProfesorMessages from './ProfesorMessages.vue'
import ProfesorRequests from './ProfesorRequests.vue'

const props = defineProps({ username: String })
const page = ref(localStorage.getItem('profesorPage') || 'dashboard')
const componentError = ref('')

onMounted(() => {
  console.log('ProfesorPage: Mounted, current page =', page.value)
})

const setPage = (val) => {
  page.value = val
  componentError.value = ''
  localStorage.setItem('profesorPage', val)
}

onErrorCaptured((err, instance, info) => {
  console.error('Error en componente:', err, info)
  componentError.value = `${info}: ${err?.message || 'Error desconocido'}`
  return false
})

const currentComponent = computed(() => {
  try {
    switch(page.value) {
      case 'dashboard': return ProfesorDashboard
      case 'messages': return ProfesorMessages
      case 'requests': return ProfesorRequests
      default: return ProfesorDashboard
    }
  } catch (err) {
    console.error('Error al cargar componente:', err)
    componentError.value = err?.message || 'Error al cargar el componente'
    return null
  }
})
</script>

<style scoped>
.profesor-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

/* Sidebar */
.sidebar {
  width: 250px;
  background: linear-gradient(135deg, #1565c0 0%, #0d47a1 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  height: 100vh;
  overflow-y: auto;
}

.sidebar nav ul {
  list-style: none;
  margin: 0;
  padding: 1rem 0;
}

.sidebar nav li {
  padding: 1rem 1.5rem;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;
  font-weight: 500;
}

.sidebar nav li:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.sidebar nav li.active {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border-left-color: #fff;
}

/* Content */
.content {
  flex: 1;
  margin-left: 250px;
  overflow-y: auto;
}

.error-message {
  padding: 2rem;
  background: #ffebee;
  color: #c62828;
  border-radius: 8px;
  margin: 2rem;
}

.error-message h2 {
  margin: 0 0 1rem 0;
}

/* Responsive */
@media (max-width: 768px) {
  .profesor-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
  }

  .sidebar nav ul {
    display: flex;
    flex-wrap: wrap;
  }

  .sidebar nav li {
    flex: 1;
    min-width: 150px;
    text-align: center;
    padding: 1rem 0.5rem;
    border-left: none;
    border-bottom: 4px solid transparent;
  }

  .sidebar nav li.active {
    border-left: none;
    border-bottom-color: #fff;
  }

  .content {
    margin-left: 0;
  }
}
</style>
