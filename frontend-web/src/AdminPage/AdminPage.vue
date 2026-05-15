<template>
  <!--
    Vista principal del panel de administrador.
    - `aside.sidebar`: menú de navegación para cambiar secciones.
    - `main.content`: área donde se monta el componente elegido dinámicamente.
  -->
  <div class="admin-layout">
    <aside class="sidebar">
      <nav>
        <ul>
          <!-- Menú de navegación: cada item cambia la página actual -->
          <li :class="{active: page==='dashboard'}" @click="setPage('dashboard')">Dashboard</li>
          <li :class="{active: page==='users'}" @click="setPage('users')">Gestión de usuarios</li>
          <li :class="{active: page==='buses'}" @click="setPage('buses')">Gestión de autobuses</li>
          <li :class="{active: page==='schools'}" @click="setPage('schools')">Gestión de colegios</li>
          <li :class="{active: page==='students'}" @click="setPage('students')">Gestión de alumnos</li>
          <li :class="{active: page==='routes'}" @click="setPage('routes')">Gestión de rutas</li>
          <li :class="{active: page==='stops'}" @click="setPage('stops')">Gestión de paradas</li>
          <li :class="{active: page==='messages'}" @click="setPage('messages')">Mensajería</li>
        </ul>
      </nav>
    </aside>
    <main class="content">
      <!-- Si hay error al cargar el componente se muestra aquí -->
      <div v-if="componentError" class="error-message">
        <h2>Error al cargar el componente</h2>
        <p>{{ componentError }}</p>
      </div>
      <!-- `component` con :is permite montar componentes dinámicos según `currentComponent` -->
      <component v-else :is="currentComponent" />
    </main>
  </div>
</template>
<script setup>
/*
  AdminPage: lógica para el panel de administración.
  - `page` guarda la sección actual (dashboard, buses, etc.).
  - `currentComponent` devuelve el componente que se debe renderizar.
  - Usamos `localStorage` para recordar la página seleccionada entre recargas.
*/
import { ref, computed, onErrorCaptured, onMounted } from 'vue'
import AdminDashboard from '../AdminDashboard/AdminDashboard.vue'
import BusManagement from '../BusManagement/BusManagement.vue'
import UserManagement from '../UserManagement/UserManagement.vue'
import SchoolManagement from '../SchoolManagement/SchoolManagement.vue'
import StudentManagement from '../StudentManagement/StudentManagement.vue'
import RouteManagement from '../RouteManagement/RouteManagement.vue'
import StopManagement from '../StopManagement/StopManagement.vue'
import AdminMessages from '../AdminMessages/AdminMessages.vue'

// Props: recibimos el username desde el padre si hace falta
const props = defineProps({ username: String })

// Estado reactivo: página actual y errores
const page = ref(localStorage.getItem('adminPage') || 'dashboard')
const componentError = ref('')

// Logs sencillos para desarrollo (útiles para entender el flujo)
console.log('AdminPage: Component loaded at ' + new Date().toISOString())

onMounted(() => {
  console.log('AdminPage: Mounted, current page =', page.value)
})

// Cambia la página y guarda en localStorage
const setPage = (val) => {
  page.value = val
  componentError.value = ''
  localStorage.setItem('adminPage', val)
}

// Capturamos errores de los hijos y mostramos mensaje
onErrorCaptured((err, instance, info) => {
  console.error('Error en componente:', err, info)
  componentError.value = `${info}: ${err?.message || 'Error desconocido'}`
  return false
})

// Computed que elige qué componente montar según `page`
const currentComponent = computed(() => {
  try {
    switch(page.value) {
      case 'dashboard': return AdminDashboard
      case 'buses': return BusManagement
      case 'users': return UserManagement
      case 'schools': return SchoolManagement
      case 'students': return StudentManagement
      case 'routes': return RouteManagement
      case 'stops': return StopManagement
      case 'messages': return AdminMessages
      default: return AdminDashboard
    }
  } catch (err) {
    // En caso de error, mostramos información útil
    console.error('Error al cargar componente:', err)
    componentError.value = err?.message || 'Error al cargar el componente'
    return null
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  width: 100vw;
}
.sidebar {
  width: 240px;
  background: #1976d2;
  color: #fff;
  padding-top: 2rem;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
}
.sidebar ul {
  list-style: none;
  padding: 0;
}
.sidebar li {
  padding: 1rem 2rem;
  cursor: pointer;
  transition: background 0.2s;
}
.sidebar li.active, .sidebar li:hover {
  background: #1565c0;
}
.content {
  margin-left: 240px;
  flex: 1;
  min-height: 100vh;
  padding: 2rem 3vw 2rem 3vw;
  background: transparent;
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
