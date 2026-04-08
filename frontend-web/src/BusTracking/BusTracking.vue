
<template>
  <div class="bus-tracking-layout">
    <div class="bus-list-panel">
      <h2>Autobuses</h2>
      <div v-if="cargando" class="tracking-loading">Cargando autobuses...</div>
      <ul v-else class="bus-list">
        <li v-for="bus in buses" :key="bus.id" :class="{selected: bus.id === selectedBusId}" @click="selectedBusId = bus.id">
          <div class="matricula">{{ bus.matricula }}</div>
          <div class="conductor">{{ bus.conductor_nombre || 'Sin asignar' }}</div>
        </li>
      </ul>
    </div>
    <div class="bus-details-panel">
      <h2 v-if="selectedBus">Detalles del autobús</h2>
      <div v-if="selectedBus">
        <p><b>Matrícula:</b> {{ selectedBus.matricula }}</p>
        <p><b>Conductor:</b> {{ selectedBus.conductor_nombre || 'Sin asignar' }}</p>

        <!-- Aquí puedes añadir más detalles si lo deseas -->
        <div class="bus-map-container">
          <div id="bus-map" style="height: 300px; width: 100%; border-radius: 10px; margin-top: 1.2rem;"></div>
          <div v-if="!hasLocation" class="no-location">Sin localización disponible para este autobús.</div>
        </div>
      </div>
      <div v-else class="no-bus-selected">Selecciona un autobús para ver detalles.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { io } from 'socket.io-client'
import { getApiUrl } from '../utils/api.js'

const buses = ref([])
const cargando = ref(true)
const selectedBusId = ref(null)

// Socket.io
let socket = null

const selectedBus = computed(() => buses.value.find(b => b.id === selectedBusId.value))
const hasLocation = computed(() => selectedBus.value && selectedBus.value.lat && selectedBus.value.lon)

async function cargarBuses() {
  cargando.value = true
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/buses`)
    if (res.ok) {
      buses.value = await res.json()
    } else {
      buses.value = []
    }
  } catch {
    buses.value = []
  } finally {
    cargando.value = false
  }
}

// Actualiza la posición de un bus en el array buses
function actualizarPosicionBus(data) {
  // data: { id, lat, lon, ... }
  const idx = buses.value.findIndex(b => b.id === data.id)
  if (idx !== -1) {
    buses.value[idx] = { ...buses.value[idx], ...data }
  }
}

let map = null
let marker = null

function showMap(lat, lon) {
  if (!window.L) return;
  if (!map) {
    map = window.L.map('bus-map').setView([lat, lon], 15)
    window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(map)
    marker = window.L.marker([lat, lon]).addTo(map)
  } else {
    map.setView([lat, lon], 15)
    marker.setLatLng([lat, lon])
  }
}

watch(selectedBus, (bus) => {
  setTimeout(() => {
    if (bus && bus.lat && bus.lon) {
      showMap(bus.lat, bus.lon)
    } else if (map) {
      map.remove()
      map = null
      marker = null
    }
  }, 100)
})

onMounted(() => {
  cargarBuses()
  // Cargar Leaflet si no está presente
  if (!window.L) {
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = 'https://unpkg.com/leaflet/dist/leaflet.css'
    document.head.appendChild(link)
    const script = document.createElement('script')
    script.src = 'https://unpkg.com/leaflet/dist/leaflet.js'
    document.body.appendChild(script)
  }

  // Conectar a WebSocket para ubicaciones en tiempo real
  const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:3000';
  socket = io(wsUrl)
  socket.on('bus-location', (data) => {
    // data: { id, lat, lon, ... }
    actualizarPosicionBus(data)
    // Si el bus actualizado es el seleccionado, actualiza el mapa
    if (selectedBus.value && data.id === selectedBus.value.id && data.lat && data.lon) {
      showMap(data.lat, data.lon)
    }
  })
})

onBeforeUnmount(() => {
  if (socket) {
    socket.disconnect()
    socket = null
  }
})
</script>

<style scoped>
.bus-tracking-layout {
  display: flex;
  gap: 2.5rem;
  min-height: 60vh;
}
.bus-list-panel {
  min-width: 320px;
  max-width: 340px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.07);
  padding: 1.5rem 1.2rem;
  height: fit-content;
}
.bus-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.bus-list li {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  padding: 0.7rem 1rem;
  border-radius: 7px;
  margin-bottom: 0.5rem;
  cursor: pointer;
  border: 1.5px solid transparent;
  transition: background 0.18s, border 0.18s;
}
.bus-list li.selected {
  background: #e3f2fd;
  border: 1.5px solid #1976d2;
}
.bus-list li:hover {
  background: #f1f8ff;
}
.matricula {
  font-weight: 600;
  font-size: 1.08rem;
}
.conductor {
  font-size: 0.97rem;
  color: #1976d2;
}
.bus-details-panel {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.07);
  padding: 2rem 2.5rem;
  min-width: 260px;
  height: fit-content;
}
.no-bus-selected {
  color: #888;
  font-style: italic;
  margin-top: 2rem;
}
.tracking-loading {
  color: #1976d2;
  font-weight: 500;
  padding: 1.5rem 0;
  text-align: center;
}
</style>
