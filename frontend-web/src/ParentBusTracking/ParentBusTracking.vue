<template>
  <div class="tracking-container">
    <h1>📍 Seguimiento de Autobuses</h1>
    
    <div v-if="cargando" class="loading">
      <span class="spinner"></span>
      <p>Cargando información de autobuses...</p>
    </div>

    <div v-else-if="autobusesDelPadre.length" class="buses-info">
      <!-- Selector de autobús -->
      <div class="selector-container">
        <label for="bus-selector">Selecciona un autobús:</label>
        <select id="bus-selector" v-model="autobusPadreSeleccionado" class="bus-selector">
          <option v-for="bus in autobusesDelPadre" :key="bus.id" :value="bus.id">
            {{ bus.matricula }} - {{ bus.marca }} {{ bus.modelo }}
          </option>
        </select>
      </div>

      <!-- Información del autobús seleccionado -->
      <div v-if="autobusPadre" class="bus-card">
        <div class="bus-header">
          <h2>{{ autobusPadre.matricula }}</h2>
          <span class="badge" :class="estadoBus">{{ estadoBus }}</span>
        </div>
        
        <div class="bus-details">
          <div class="detail-item">
            <strong>Vehículo:</strong>
            <span>{{ autobusPadre.marca }} {{ autobusPadre.modelo }} ({{ autobusPadre.anio }})</span>
          </div>
          <div class="detail-item">
            <strong>Capacidad:</strong>
            <span>{{ autobusPadre.capacidad }} pasajeros</span>
          </div>
          <div class="detail-item">
            <strong>Conductor:</strong>
            <span>{{ autobusPadre.conductor_nombre || 'Sin asignar' }}</span>
          </div>
          <div class="detail-item">
            <strong>Estado:</strong>
            <span>{{ autobusPadre.estado }}</span>
          </div>
        </div>
      </div>

      <!-- Mapa -->
      <div class="map-container">
        <div class="map-header">
          <h3>Ubicación en tiempo real</h3>
          <p v-if="tiempoActualizacion" class="update-time">
            Última actualización: {{ tiempoActualizacion }}
          </p>
        </div>
        
        <div id="bus-map" class="map" style="height: 400px; width: 100%; border-radius: 8px; margin-top: 1rem;"></div>
        
        <div v-if="!autobusPadre.lat || !autobusPadre.lon" class="no-location">
          <p>⚠️ No hay localización disponible en este momento</p>
        </div>

        <div class="location-info">
          <p><strong>Latitud:</strong> {{ autobusPadre.lat?.toFixed(6) || 'N/A' }}</p>
          <p><strong>Longitud:</strong> {{ autobusPadre.lon?.toFixed(6) || 'N/A' }}</p>
        </div>
      </div>

      <!-- Paradas de tus hijos -->
      <div class="children-stops">
        <h3>📍 Paradas de tus hijos</h3>
        <div v-if="hijosDelPadre.length" class="stops-list">
          <div v-for="hijo in hijosDelPadre" :key="hijo.id" class="stop-item">
            <div class="stop-info">
              <strong>{{ hijo.nombre }} {{ hijo.apellidos }}</strong>
              <p class="stop-location">{{ hijo.stop_nombre }}</p>
              <p class="stop-address">{{ hijo.stop_direccion }}</p>
            </div>
            <button class="ver-btn" @click="mostrarParadaEnMapa(hijo)">Ver en mapa</button>
          </div>
        </div>
        <div v-else class="no-children">
          <p>No hay hijos con paradas asignadas</p>
        </div>
      </div>

      <!-- Información en tiempo real -->
      <div class="eta-container">
        <h3>📊 Estadísticas en tiempo real</h3>
        <div class="stats-grid">
          <div class="stat">
            <strong>Autobuses conectados:</strong>
            <p>{{ autobusesDelPadre.length }}</p>
          </div>
          <div class="stat">
            <strong>Autobús en movimiento:</strong>
            <p>{{ autobusPadre.lat && autobusPadre.lon ? 'Sí' : 'No' }}</p>
          </div>
          <div class="stat">
            <strong>Estado del conductor:</strong>
            <p>{{ autobusPadre.conductor_nombre ? 'Activo' : 'Sin asignar' }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="no-buses">
      <p>⚠️ No tienes autobuses asignados o no hay información disponible.</p>
      <p>Por favor, contacta con la escuela para confirmar la asignación de tus hijos.</p>
    </div>

    <div class="info-section">
      <h3>ℹ️ Información útil</h3>
      <ul>
        <li>✅ La información se actualiza en tiempo real</li>
        <li>✅ Puedes ver múltiples autobuses si tus hijos usan diferentes</li>
        <li>✅ En caso de emergencia, contacta directamente al conductor</li>
        <li>✅ Las coordenadas se muestran con precisión GPS</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { getApiUrl } from '../utils/api.js'

const autobusesDelPadre = ref([])
const hijosDelPadre = ref([])
const autobusPadreSeleccionado = ref(null)
const cargando = ref(true)
const tiempoActualizacion = ref('')
let mapa = null
let marcador = null
let marcadoresParadas = []
let intervalo = null
let parentId = null

const autobusPadre = computed(() => {
  if (!autobusPadreSeleccionado.value) return autobusesDelPadre.value[0] || null
  return autobusesDelPadre.value.find(b => b.id === autobusPadreSeleccionado.value)
})

const estadoBus = computed(() => {
  if (!autobusPadre.value) return 'desconocido'
  if (autobusPadre.value.lat && autobusPadre.value.lon) return 'en-movimiento'
  return 'detenido'
})

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
      if (autobusesDelPadre.value.length > 0) {
        autobusPadreSeleccionado.value = autobusesDelPadre.value[0].id
      }
    }
    
    const childrenRes = await fetch(`${apiUrl}/parent/${parentId}/children`)
    if (childrenRes.ok) {
      hijosDelPadre.value = await childrenRes.json()
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
      // Only move the marker — do NOT re-center so the user can freely pan/zoom
      marcador.setLatLng([lat, lon])
    }
  }, 200)
}

function mostrarParadaEnMapa(hijo) {
  if (hijo.latitud && hijo.longitud) {
    setTimeout(() => {
      if (!mapa) {
        mapa = window.L.map('bus-map').setView([hijo.latitud, hijo.longitud], 16)
        window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '© OpenStreetMap'
        }).addTo(mapa)
      } else {
        mapa.setView([hijo.latitud, hijo.longitud], 16)
      }
      marcadoresParadas.forEach(m => mapa.removeLayer(m))
      marcadoresParadas = []
      const markerParada = window.L.marker([hijo.latitud, hijo.longitud], {
        icon: window.L.icon({
          iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
          shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowSize: [41, 41]
        })
      }).addTo(mapa)
      markerParada.bindPopup(`${hijo.nombre} - ${hijo.stop_nombre}`)
      marcadoresParadas.push(markerParada)
    }, 200)
  }
}

function actualizarTiempo() {
  const ahora = new Date()
  tiempoActualizacion.value = ahora.toLocaleTimeString('es-ES')
}

async function recargarDatos() {
  try {
    const apiUrl = getApiUrl()
    if (!parentId) return
    
    const busesRes = await fetch(`${apiUrl}/parent/${parentId}/buses`)
    if (busesRes.ok) {
      autobusesDelPadre.value = await busesRes.json()
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
  cargarDatosDelPadre()
  // iniciarActualizacionPeriódica() is already called inside cargarDatosDelPadre
})

onBeforeUnmount(() => {
  if (intervalo) clearInterval(intervalo)
  if (mapa) mapa.remove()
})

watch(() => autobusPadre.value, (newBus) => {
  if (newBus) {
    const lat = newBus.lat || 40.4168
    const lon = newBus.lon || -3.7038
    mostrarMapa(lat, lon)
  }
}, { immediate: true })
</script>

<style scoped>
.tracking-container { max-width: 1200px; }
.loading { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem; color: #666; }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #667eea; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 1rem; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.selector-container { background: white; padding: 1.5rem; border-radius: 8px; margin-bottom: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.selector-container label { display: block; margin-bottom: 0.8rem; font-weight: 600; color: #333; }
.bus-selector { width: 100%; padding: 0.8rem; border: 2px solid #667eea; border-radius: 6px; font-size: 14px; background: white; cursor: pointer; }
.buses-info { display: grid; gap: 2rem; }
.bus-card { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.bus-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; border-bottom: 2px solid #f0f0f0; padding-bottom: 1rem; }
.bus-header h2 { margin: 0; color: #667eea; font-size: 1.5rem; }
.badge { padding: 0.5rem 1rem; border-radius: 20px; font-size: 12px; font-weight: bold; color: white; }
.badge.en-movimiento { background: #4caf50; animation: pulse 2s infinite; }
.badge.detenido { background: #ff9800; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }
.bus-details { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; }
.detail-item { padding: 1rem; background: #f9f9f9; border-radius: 6px; border-left: 4px solid #667eea; }
.detail-item strong { display: block; color: #667eea; font-size: 12px; text-transform: uppercase; margin-bottom: 0.5rem; }
.map-container { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.map-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.map-header h3 { margin: 0; color: #333; }
.update-time { font-size: 12px; color: #999; margin: 0; }
.no-location { background: #fff3cd; color: #856404; padding: 1.5rem; border-radius: 6px; margin-top: 1rem; text-align: center; }
.location-info { background: #f9f9f9; padding: 1rem; border-radius: 6px; margin-top: 1rem; font-size: 14px; }
.location-info p { margin: 0.5rem 0; }
.children-stops { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.children-stops h3 { margin-top: 0; color: #333; margin-bottom: 1.5rem; }
.stops-list { display: grid; gap: 1rem; }
.stop-item { padding: 1rem; background: #f9f9f9; border-radius: 6px; border-left: 4px solid #667eea; display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
.stop-info { flex: 1; }
.stop-info strong { display: block; color: #333; margin-bottom: 0.3rem; }
.stop-location { margin: 0.3rem 0; color: #667eea; font-weight: 500; font-size: 14px; }
.stop-address { margin: 0.3rem 0; color: #666; font-size: 13px; }
.ver-btn { padding: 0.6rem 1.2rem; background: #667eea; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px; white-space: nowrap; }
.ver-btn:hover { background: #5568d3; }
.no-children { text-align: center; color: #999; padding: 1rem; }
.eta-container { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; margin-top: 1.5rem; }
.stat { padding: 1.5rem; background: #f9f9f9; border-radius: 6px; border: 1px solid #e0e0e0; text-align: center; }
.stat strong { display: block; color: #667eea; font-size: 12px; text-transform: uppercase; margin-bottom: 0.5rem; }
.stat p { margin: 0; font-size: 1.5rem; font-weight: bold; color: #333; }
.no-buses { background: white; border-radius: 8px; padding: 3rem; text-align: center; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); color: #666; }
.info-section { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); margin-top: 2rem; }
.info-section h3 { margin-top: 0; color: #667eea; }
.info-section ul { margin: 0; padding-left: 2rem; color: #666; }
.info-section li { margin-bottom: 0.8rem; }
</style>
