<template>
  <div class="dashboard">
    <h1 style="padding: 10px; color: black;">
      Panel de Administración
    </h1>
    <p>Bienvenido al sistema de gestión de transporte escolar.</p>
    
    <div class="stats-grid">
      <div class="stat-card">
        <h3>Autobuses</h3>
        <p class="stat-value">{{ buses }}</p>
      </div>
      <div class="stat-card">
        <h3>Usuarios</h3>
        <p class="stat-value">{{ users }}</p>
      </div>
      <div class="stat-card">
        <h3>Alumnos</h3>
        <p class="stat-value">{{ students }}</p>
      </div>
      <div class="stat-card">
        <h3>Rutas</h3>
        <p class="stat-value">{{ routes }}</p>
      </div>
    </div>
    
    
    <ServerStatusCharts />
  </div>
  <div class="info-section">
      <h2>Acciones rápidas</h2>
      <ul>
        <li>Utiliza el menú lateral para acceder a diferentes secciones de gestión</li>
        <li>Puedes crear, editar y eliminar elementos desde cada sección</li>
        <li>Todos los cambios se guardan automáticamente en la base de datos</li>
      </ul>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getApiUrl } from '../utils/api.js'
import ServerStatusCharts from './ServerStatusCharts.vue'

const buses = ref(0)
const users = ref(0)
const students = ref(0)
const routes = ref(0)

async function cargarEstadisticas() {
  try {
    const apiUrl = getApiUrl()
    
    const [busesRes, usersRes, studentsRes, routesRes] = await Promise.all([
      fetch(`${apiUrl}/buses`).then(r => r.json()).catch(() => []),
      fetch(`${apiUrl}/users`).then(r => r.json()).catch(() => []),
      fetch(`${apiUrl}/students`).then(r => r.json()).catch(() => []),
      fetch(`${apiUrl}/routes`).then(r => r.json()).catch(() => [])
    ])
    
    buses.value = (busesRes || []).length
    users.value = (usersRes || []).length
    students.value = (studentsRes || []).length
    routes.value = (routesRes || []).length
  } catch {
    // Error silencioso
  }
}

onMounted(cargarEstadisticas)
</script>

<style scoped>
.dashboard {
  padding: 2rem;
}

.dashboard h1 {
  color: #1976d2;
  margin-bottom: 1rem;
}

.dashboard > p {
  color: #666;
  margin-bottom: 2rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.stat-card h3 {
  margin: 0;
  color: #333;
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  margin: 0;
  font-size: 2rem;
  font-weight: bold;
  color: #1976d2;
}

.info-section {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 1.5rem;
  margin-top: 2rem;
}

.info-section h2 {
  color: #1976d2;
  margin-top: 0;
}

.info-section ul {
  margin: 0;
  padding-left: 1.5rem;
  color: #555;
}

.info-section li {
  margin-bottom: 0.5rem;
}
</style>
