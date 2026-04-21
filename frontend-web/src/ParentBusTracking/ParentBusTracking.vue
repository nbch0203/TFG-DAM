<template>
  <div class="tracking-container">
    <h1>📍 Seguimiento de mi hijo</h1>
    
    <div v-if="cargando" class="loading">
      <span class="spinner"></span>
      <p>Cargando información...</p>
    </div>

    <div v-else-if="hijosDelPadre.length" class="buses-info">
      <!-- Selector de hijo (único) -->
      <div class="selector-container">
        <label for="child-selector-main">Elige un hijo para seguir:</label>
        <select id="child-selector-main" v-model="hijoSeleccionado" class="bus-selector">
          <option v-for="hijo in hijosDelPadre" :key="hijo.id" :value="hijo.id">
            {{ hijo.nombre }} {{ hijo.apellidos }}
          </option>
        </select>
      </div>

      <!-- Sin autobús asignado -->
      <div v-if="hijoActual && !hijoActual.bus_id" class="no-location" style="margin-top: 2rem;">
        <p>⚠️ {{ hijoActual.nombre }} no tiene un autobús activo asignado en este momento.</p>
      </div>

      <!-- Información del autobús (cuando existe) -->
      <div v-if="autobusPadre" class="bus-card">
        <div class="bus-header">
          <div>
            <h2 style="margin: 0;">{{ autobusPadre.matricula }}</h2>
            <p style="margin: 0.5rem 0 0 0; color: #666; font-size: 14px;">
              Autobús de {{ hijoActual?.nombre }}
            </p>
          </div>
          <span class="badge" :class="estadoBus">{{ estadoBus }}</span>
        </div>
        
        <div class="bus-details">
          <div class="detail-item">
            <strong>Vehículo:</strong>
            <span>{{ autobusPadre.marca }} {{ autobusPadre.modelo }}</span>
          </div>
          <div class="detail-item">
            <strong>Conductor:</strong>
            <span>{{ autobusPadre.conductor_nombre || 'Sin asignar' }}</span>
          </div>
          <div class="detail-item">
            <strong>Parada:</strong>
            <span>{{ hijoActual?.stop_nombres || hijoActual?.stop_nombre || 'No definida' }}</span>
          </div>
        </div>
      </div>

      <!-- Mapa en tiempo real -->
      <div class="map-container" v-if="autobusPadre">
        <div class="map-header">
          <h3>Ubicación en tiempo real</h3>
          <p v-if="tiempoActualizacion" class="update-time">
            📡 {{ tiempoActualizacion }}
          </p>
        </div>

        <div id="bus-map" class="map"></div>
        
        <div v-if="!tieneUbicacionAutobus" class="no-location" style="margin-top: 1rem;">
          <p>⚠️ Buscando ubicación GPS...</p>
        </div>

        <div v-if="tieneUbicacionAutobus" class="location-info">
          <p><strong>Lat:</strong> {{ autobusPadre?.lat?.toFixed(6) }}</p>
          <p><strong>Lon:</strong> {{ autobusPadre?.lon?.toFixed(6) }}</p>
        </div>
      </div>
    </div>

    <div v-else class="no-buses">
      <p>⚠️ No hay hijos registrados.</p>
      <p>Por favor, contacta con la escuela para confirmar la asignación.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { getApiUrl } from '../utils/api.js'

const autobusesDelPadre = ref([])
const hijosDelPadre = ref([])
const hijoSeleccionado = ref(null)
const cargando = ref(true)
const tiempoActualizacion = ref('')
let mapa = null
let marcador = null
let intervalo = null
let parentId = null

const hijoActual = computed(() => {
  if (!hijoSeleccionado.value) return hijosDelPadre.value[0] || null
  return hijosDelPadre.value.find(h => h.id === hijoSeleccionado.value) || null
})

const autobusPadre = computed(() => {
  const busId = hijoActual.value?.bus_id
  if (!busId) return null
  return autobusesDelPadre.value.find(b => b.id === busId) || null
})

function tieneCoordenadasValidas(obj) {
  if (!obj) return false
  const lat = Number(obj.lat)
  const lon = Number(obj.lon)
  return Number.isFinite(lat) && Number.isFinite(lon)
}

const tieneUbicacionAutobus = computed(() => tieneCoordenadasValidas(autobusPadre.value))

const estadoBus = computed(() => {
  if (!autobusPadre.value) return 'desconocido'
  if (tieneUbicacionAutobus.value) return 'en-movimiento'
  return 'detenido'
})

function sincronizarHijoSeleccionado() {
  if (!hijosDelPadre.value.length) {
    hijoSeleccionado.value = null
    return
  }
  const existe = hijosDelPadre.value.some(h => h.id === hijoSeleccionado.value)
  if (!existe) {
    hijoSeleccionado.value = hijosDelPadre.value[0].id
  }
}

async function cargarDatosDelPadre() {
  cargando.value = true
  try {
    const apiUrl = getApiUrl()
    const userStr = sessionStorage.getItem('user')
    if (!userStr) {
      console.error('Usuario no encontrado en sesión')
      return
    }
    
    const user = JSON.parse(userStr)
    parentId = user.id
    
    const busesRes = await fetch(`${apiUrl}/parent/${parentId}/buses`)
    if (busesRes.ok) {
      autobusesDelPadre.value = await busesRes.json()
    }
    
    const childrenRes = await fetch(`${apiUrl}/parent/${parentId}/children`)
    if (childrenRes.ok) {
      hijosDelPadre.value = await childrenRes.json()
      sincronizarHijoSeleccionado()
    }
    
    actualizarTiempo()
    iniciarActualizacionPeriódica()
  } catch (err) {
    console.error('Error al cargar datos:', err)
  } finally {
    cargando.value = false
  }
}

function mostrarMapa(lat, lon) {
  setTimeout(() => {
    if (!window.L) return
    if (!mapa) {
      mapa = window.L.map('bus-map').setView([lat, lon], 15)
      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap'
      }).addTo(mapa)
      marcador = window.L.marker([lat, lon], {
        icon: window.L.icon({
          iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
          shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowSize: [41, 41]
        })
      }).addTo(mapa)
      marcador.bindPopup(`Autobús ${autobusPadre.value.matricula}`)
    } else {
      if (!marcador) {
        marcador = window.L.marker([lat, lon]).addTo(mapa)
      }
      // Solo move the marker — do NOT re-center so the user can freely pan/zoom
      marcador.setLatLng([lat, lon])
      if (autobusPadre.value) {
        marcador.bindPopup(`Autobús ${autobusPadre.value.matricula}`)
      }
    }
  }, 200)
}

function actualizarTiempo() {
  const ahora = new Date()
  tiempoActualizacion.value = ahora.toLocaleTimeString('es-ES')
}

async function recargarDatos() {
  try {
    const apiUrl = getApiUrl()
    if (!parentId) return
    
    const [busesRes, childrenRes] = await Promise.all([
      fetch(`${apiUrl}/parent/${parentId}/buses`),
      fetch(`${apiUrl}/parent/${parentId}/children`)
    ])
    if (busesRes.ok) {
      autobusesDelPadre.value = await busesRes.json()
    }
    if (childrenRes.ok) {
      hijosDelPadre.value = await childrenRes.json()
      sincronizarHijoSeleccionado()
    }
    
    actualizarTiempo()
  } catch (err) {
    console.error('Error al recargar datos:', err)
  }
}

function iniciarActualizacionPeriódica() {
  // Actualizar cada segundo para ver movimiento en tiempo real
  intervalo = setInterval(() => {
    recargarDatos()
  }, 1000)  // 1 segundo para tiempo real
}

onMounted(() => {
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
  
  cargarDatosDelPadre()
})

onBeforeUnmount(() => {
  if (intervalo) clearInterval(intervalo)
  if (mapa) mapa.remove()
})

watch(() => autobusPadre.value, (newBus) => {
  if (newBus && tieneCoordenadasValidas(newBus)) {
    const lat = Number(newBus.lat)
    const lon = Number(newBus.lon)
    mostrarMapa(lat, lon)
  } else if (marcador && mapa) {
    mapa.removeLayer(marcador)
    marcador = null
  }
}, { immediate: true })

</script>

<style scoped>
.tracking-container {
  width: 100%;
  max-width: none;
  margin: 0;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
  padding: 2rem;
  box-sizing: border-box;
}
.loading { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem; color: #666; }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #667eea; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 1rem; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.selector-container { background: white; padding: 1.5rem; border-radius: 8px; margin-bottom: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.selector-container label { display: block; margin-bottom: 0.8rem; font-weight: 600; color: #333; }
.bus-selector { width: 100%; padding: 0.8rem; border: 2px solid #667eea; border-radius: 6px; font-size: 14px; background: white; cursor: pointer; }

.buses-info {
  display: grid;
  gap: 1.5rem;
  width: 100%;
}

.bus-card { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.bus-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; border-bottom: 2px solid #f0f0f0; padding-bottom: 1rem; gap: 1rem; }
.bus-header h2 { margin: 0; color: #667eea; font-size: 1.3rem; }
.bus-header p { margin: 0; }
.badge { padding: 0.5rem 1rem; border-radius: 20px; font-size: 12px; font-weight: bold; color: white; white-space: nowrap; }
.badge.en-movimiento { background: #4caf50; animation: pulse 2s infinite; }
.badge.detenido { background: #ff9800; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }

.bus-details { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; }
.detail-item { padding: 1rem; background: #f9f9f9; border-radius: 6px; border-left: 4px solid #667eea; }
.detail-item strong { display: block; color: #667eea; font-size: 11px; text-transform: uppercase; margin-bottom: 0.5rem; }
.detail-item span { color: #333; font-size: 14px; }

.map-container { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.map-header { margin-bottom: 1.5rem; }
.map-header h3 { margin: 0 0 0.5rem 0; color: #333; }
.update-time { font-size: 12px; color: #999; margin: 0; }

.map { height: min(62vh, 650px); width: 100%; border-radius: 8px; background: #f0f0f0; }

@media (max-width: 768px) {
  .map {
    height: 420px;
  }
}

.no-location { background: #fff3cd; color: #856404; padding: 1.5rem; border-radius: 6px; text-align: center; margin-top: 1rem; }
.location-info { background: #f9f9f9; padding: 1rem; border-radius: 6px; margin-top: 1rem; font-size: 13px; display: flex; gap: 2rem; }
.location-info p { margin: 0; }

.no-buses { background: white; border-radius: 8px; padding: 3rem; text-align: center; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); color: #666; }

@media (max-width: 768px) {
  .tracking-container {
    padding: 1rem;
    border-radius: 8px;
  }
}
</style>
