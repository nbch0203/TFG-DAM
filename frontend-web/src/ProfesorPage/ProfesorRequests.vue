<template>
  <div class="profesor-requests">
    <header class="requests-header">
      <h1>Mis Solicitudes</h1>
      <p>Consulta el estado de los mensajes que has enviado a administración.</p>
      <button class="refresh-btn" @click="loadRequests" :disabled="loading">
        {{ loading ? 'Actualizando...' : 'Actualizar' }}
      </button>
    </header>

    <section class="filters">
      <select v-model="filters.status">
        <option value="">Todos los estados</option>
        <option value="nuevo">Nuevo</option>
        <option value="abierto">Abierto</option>
        <option value="en_progreso">En progreso</option>
        <option value="resuelto">Resuelto</option>
        <option value="cerrado">Cerrado</option>
      </select>
      <select v-model="filters.priority">
        <option value="">Todas las prioridades</option>
        <option value="baja">Baja</option>
        <option value="media">Media</option>
        <option value="alta">Alta</option>
        <option value="crítica">Crítica</option>
      </select>
    </section>

    <section v-if="errorMessage" class="error-box">
      {{ errorMessage }}
    </section>

    <section v-else-if="loading" class="loading-box">
      Cargando solicitudes...
    </section>

    <section v-else-if="filteredRequests.length === 0" class="empty-box">
      No se encontraron solicitudes para los filtros aplicados.
    </section>

    <section v-else class="requests-list">
      <article v-for="request in filteredRequests" :key="request.id" class="request-card">
        <div class="request-top">
          <h3>{{ request.subject }}</h3>
          <div class="badges">
            <span :class="['badge', 'priority', request.priority]">{{ request.priority }}</span>
            <span :class="['badge', 'status', request.status]">{{ request.status }}</span>
          </div>
        </div>

        <p class="request-content">{{ request.content }}</p>

        <div class="request-meta">
          <span><strong>ID:</strong> #{{ request.id }}</span>
          <span><strong>Creado:</strong> {{ formatDate(request.createdAt) }}</span>
          <span><strong>Última actualización:</strong> {{ formatDate(request.updatedAt) }}</span>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getApiUrl } from '../utils/api.js'

const props = defineProps({ username: String })

const loading = ref(true)
const errorMessage = ref('')
const requests = ref([])

const filters = ref({
  status: '',
  priority: ''
})

const normalizeDate = (value) => {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date.toISOString()
}

const normalizeRequest = (raw) => ({
  ...raw,
  createdAt: normalizeDate(raw.createdAt || raw.created_at),
  updatedAt: normalizeDate(raw.updatedAt || raw.updated_at),
  priority: raw.priority || 'media',
  status: raw.status || 'nuevo'
})

const filteredRequests = computed(() => {
  return requests.value.filter((r) => {
    const statusOk = !filters.value.status || r.status === filters.value.status
    const priorityOk = !filters.value.priority || r.priority === filters.value.priority
    return statusOk && priorityOk
  })
})

const loadRequests = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const apiUrl = getApiUrl()
    const senderName = props.username || 'Profesor'
    const response = await fetch(`${apiUrl}/messages/mine?senderName=${encodeURIComponent(senderName)}`)

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.error || 'No se pudo cargar el historial')
    }

    const data = await response.json()
    requests.value = (data || []).map(normalizeRequest)
  } catch (error) {
    console.error('Error al cargar solicitudes:', error)
    errorMessage.value = error?.message || 'Error al cargar solicitudes'
    requests.value = []
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'Sin fecha'
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return 'Sin fecha'
  return date.toLocaleString('es-ES')
}

onMounted(loadRequests)
</script>

<style scoped>
.profesor-requests {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.requests-header {
  background: white;
  border-radius: 10px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  margin-bottom: 1rem;
}

.refresh-btn {
  margin-top: 0.8rem;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0.6rem 1rem;
  cursor: pointer;
}

.refresh-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.filters {
  display: flex;
  gap: 0.8rem;
  margin-bottom: 1rem;
}

.filters select {
  padding: 0.6rem;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.error-box,
.loading-box,
.empty-box {
  background: white;
  border-radius: 10px;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.error-box {
  color: #c62828;
}

.requests-list {
  display: grid;
  gap: 1rem;
}

.request-card {
  background: white;
  border-radius: 10px;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.request-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.request-top h3 {
  margin: 0;
}

.badges {
  display: flex;
  gap: 0.5rem;
}

.badge {
  font-size: 12px;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
}

.priority {
  background: #fff3e0;
  color: #e65100;
}

.status {
  background: #e3f2fd;
  color: #1565c0;
}

.request-content {
  color: #444;
}

.request-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 13px;
  color: #666;
}
</style>
