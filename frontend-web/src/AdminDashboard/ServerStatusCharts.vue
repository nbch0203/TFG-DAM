<template>
  <section class="status-panel">
    <div class="status-header">
      <div>
        <p class="eyebrow">Salud de infraestructura</p>
        <h2>Estado de contenedores y servidor</h2>
        <p class="muted">Consulta uso de CPU y memoria, además de contadores por estado.</p>
      </div>
      <div class="actions">
        <span v-if="lastUpdated" class="muted">Actualizado {{ lastUpdated }}</span>
        <button class="refresh" :disabled="loading" @click="fetchStatus">
          {{ loading ? 'Actualizando…' : 'Actualizar' }}
        </button>
      </div>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="metrics-grid">
      <div class="metric-card">
        <p class="label">Uptime backend</p>
        <p class="value">{{ serverUptime }}</p>
        <p class="hint">Carga media: {{ loadAverage }}</p>
      </div>
      <div class="metric-card">
        <p class="label">Memoria servidor</p>
        <p class="value">{{ memoryUsage }}</p>
        <p class="hint">Libre {{ freeMemoryMB }} MB</p>
      </div>
      <div class="metric-card" :class="{ danger: !dockerAvailable }">
        <p class="label">Docker</p>
        <p class="value">{{ dockerAvailable ? 'Disponible' : 'No accesible' }}</p>
        <p class="hint">{{ containers.length }} contenedores detectados</p>
      </div>
      <div class="metric-card">
        <p class="label">Estados</p>
        <p class="value">{{ runningCount }} activos</p>
        <p class="hint">{{ stoppedCount }} detenidos / otros</p>
      </div>
    </div>

    <div v-if="!dockerAvailable" class="warning">
      No se pudo leer Docker desde el backend. Revisa permisos o el socket.
    </div>

    <div class="charts-grid" v-if="dockerAvailable && containers.length">
      <div class="chart-card full-width">
        <h3>Estado de contenedores en tiempo real</h3>
        <div class="containers-list">
          <div v-for="container in containers" :key="container.id" class="container-item" :class="`status-${getStatusClass(container.state)}`">
            <div class="status-indicator" :style="{ backgroundColor: getStatusColor(container.state) }"></div>
            <div class="container-info">
              <p class="container-name">{{ container.name }}</p>
              <p class="container-status">{{ container.state || 'Unknown' }}</p>
            </div>
            <div class="container-metrics" v-if="container.cpuPercent !== undefined || container.memUsageMiB !== undefined">
              <span v-if="container.cpuPercent !== undefined" class="metric">CPU: {{ container.cpuPercent }}%</span>
              <span v-if="container.memUsageMiB !== undefined" class="metric">RAM: {{ container.memUsageMiB }} MiB</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-card">
        <h3>CPU por contenedor</h3>
        <Bar :data="cpuChartData" :options="barOptions" />
      </div>
      <div class="chart-card">
        <h3>Memoria por contenedor (MiB)</h3>
        <Bar :data="memoryChartData" :options="barOptions" />
      </div>
    </div>

    <div v-if="dockerAvailable && !containers.length" class="warning">
      No hay contenedores activos en este momento.
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { Bar } from 'vue-chartjs'
import { Chart, BarElement, CategoryScale, Legend, LinearScale, Title, Tooltip } from 'chart.js'
import { getApiUrl } from '../utils/api.js'

Chart.register(BarElement, CategoryScale, LinearScale, Legend, Title, Tooltip)

const loading = ref(false)
const error = ref('')
const statusData = ref(null)
let refreshInterval = null

const containers = computed(() => statusData.value?.docker?.containers || [])
const dockerAvailable = computed(() => statusData.value?.docker?.available || false)
const lastUpdated = computed(() => statusData.value ? new Date(statusData.value.timestamp).toLocaleString() : '')

const runningCount = computed(() => containers.value.filter(c => (c.state || '').toLowerCase() === 'running').length)
const stoppedCount = computed(() => containers.value.length - runningCount.value)

const serverUptime = computed(() => {
  const uptimeSeconds = statusData.value?.server?.uptimeSeconds || 0
  const hours = Math.floor(uptimeSeconds / 3600)
  const minutes = Math.floor((uptimeSeconds % 3600) / 60)
  return `${hours}h ${minutes}m`
})

const loadAverage = computed(() => {
  const load = statusData.value?.server?.loadAvg || []
  return load.slice(0, 2).map(v => v.toFixed(2)).join(' / ')
})

const memoryUsage = computed(() => {
  const total = statusData.value?.server?.totalMemMB || 0
  const free = statusData.value?.server?.freeMemMB || 0
  const used = Math.max(total - free, 0)
  const pct = total ? ((used / total) * 100).toFixed(1) : '0'
  return `${used} MB / ${total} MB (${pct}%)`
})

const freeMemoryMB = computed(() => statusData.value?.server?.freeMemMB || 0)

const cpuChartData = computed(() => ({
  labels: containers.value.map(c => c.name),
  datasets: [
    {
      label: 'CPU %',
      data: containers.value.map(c => c.cpuPercent || 0),
      backgroundColor: '#1976d2'
    }
  ]
}))

const memoryChartData = computed(() => ({
  labels: containers.value.map(c => c.name),
  datasets: [
    {
      label: 'Memoria MiB',
      data: containers.value.map(c => c.memUsageMiB || 0),
      backgroundColor: '#ff9800'
    }
  ]
}))

const barOptions = {
  indexAxis: 'y',
  maintainAspectRatio: false,
  responsive: true,
  scales: {
    x: { beginAtZero: true },
    y: { ticks: { font: { size: 11 } } }
  },
  plugins: {
    legend: { display: false },
    tooltip: { callbacks: { label: ctx => `${ctx.parsed.x || ctx.parsed.y} ${ctx.dataset.label.includes('CPU') ? '%' : 'MiB'}` } }
  }
}

function getStatusColor(state) {
  const s = (state || '').toLowerCase()
  const colors = {
    'running': '#4caf50',     // Verde
    'paused': '#ff9800',      // Naranja
    'exited': '#f44336',      // Rojo
    'stopped': '#f44336',     // Rojo
    'dead': '#9c27b0',        // Púrpura
    'created': '#2196f3'      // Azul
  }
  return colors[s] || '#9e9e9e'  // Gris por defecto
}

function getStatusClass(state) {
  const s = (state || '').toLowerCase()
  return s || 'unknown'
}

async function fetchStatus() {
  loading.value = true
  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/system/status`)
    if (!response.ok) throw new Error('No se pudo obtener el estado')
    statusData.value = await response.json()
  } catch (err) {
    error.value = err.message || 'Error al cargar el estado'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatus()
  refreshInterval = setInterval(fetchStatus, 5000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.status-panel {
  margin-top: 2rem;
  padding: 1.5rem;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.eyebrow {
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 0.75rem;
  color: #1976d2;
  margin: 0 0 0.1rem 0;
}

h2 {
  margin: 0;
  color: #0f172a;
}

.muted {
  color: #64748b;
  margin: 0;
}

.actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.refresh {
  background: #1976d2;
  color: #fff;
  border: none;
  padding: 0.6rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.refresh:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.refresh:not(:disabled):hover {
  background: #125ea9;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.metric-card {
  padding: 1rem;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.metric-card.danger {
  background: #fff5f5;
  border-color: #fecdd3;
}

.label {
  margin: 0 0 0.3rem 0;
  color: #64748b;
  font-size: 0.9rem;
}

.value {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
  color: #0f172a;
}

.hint {
  margin: 0.1rem 0 0 0;
  color: #94a3b8;
  font-size: 0.85rem;
}

.warning {
  padding: 0.9rem 1rem;
  border-radius: 10px;
  border: 1px dashed #f97316;
  background: #fff7ed;
  color: #9a3412;
}

.error {
  color: #b91c1c;
  margin: 0;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.chart-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  height: 400px;
  max-height: 400px;
  overflow: hidden;
}

.chart-card.full-width {
  grid-column: 1 / -1;
  height: auto;
  max-height: none;
  overflow: visible;
}

.chart-card h3 {
  margin: 0 0 0.8rem 0;
  color: #0f172a;
  font-size: 0.95rem;
  flex-shrink: 0;
}

.chart-card canvas {
  flex: 1;
  min-height: 0;
}

.containers-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
}

.container-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 10px;
  background: white;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.container-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #cbd5e1;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
}

.container-info {
  flex: 1;
  min-width: 0;
}

.container-name {
  margin: 0;
  font-weight: 600;
  color: #0f172a;
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.container-status {
  margin: 0.2rem 0 0 0;
  color: #64748b;
  font-size: 0.8rem;
  text-transform: capitalize;
}

.container-metrics {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  text-align: right;
  flex-shrink: 0;
}

.metric {
  font-size: 0.75rem;
  color: #475569;
  font-weight: 500;
}
</style>
