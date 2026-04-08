<template>
  <div class="driver-page">
    <header class="driver-header">
      <h1>Panel del Conductor</h1>
      <p>Gestiona tu ruta del día y reporta incidencias operativas.</p>
      <div class="gps-status" :class="gpsActivo ? 'gps-on' : 'gps-off'">
        {{ gpsActivo ? '📡 GPS activo — enviando posición en tiempo real' : '📡 GPS inactivo' }}
        <span v-if="gpsError" class="gps-err"> · {{ gpsError }}</span>
      </div>
      <button class="btn-refresh" @click="loadTodayRoute" :disabled="loadingRoute">
        {{ loadingRoute ? 'Actualizando...' : 'Actualizar ruta' }}
      </button>
    </header>

    <section class="card">
      <h2>Ruta de hoy</h2>

      <div v-if="loadingRoute" class="state">Cargando ruta...</div>
      <div v-else-if="routeError" class="state error">{{ routeError }}</div>
      <div v-else-if="!todayRoute" class="state">No tienes una ruta asignada para hoy.</div>

      <div v-else>
        <div class="grid">
          <div><strong>Ruta:</strong> {{ todayRoute.route_nombre }}</div>
          <div><strong>Estado:</strong> {{ todayRoute.estado }}</div>
          <div><strong>Bus:</strong> {{ todayRoute.matricula }} ({{ todayRoute.marca }} {{ todayRoute.modelo }})</div>
          <div><strong>Horario:</strong> {{ todayRoute.horario_inicio }} - {{ todayRoute.horario_fin }}</div>
        </div>

        <div class="finish-route-box">
          <textarea
            v-model="finishSummary"
            rows="2"
            placeholder="Resumen opcional de cierre (incidencias, retrasos, observaciones)"
          ></textarea>
          <button
            class="btn-finish"
            @click="finishRoute"
            :disabled="finishingRoute || !todayRoute || todayRoute.estado === 'FINALIZADA'"
          >
            {{ finishingRoute ? 'Finalizando...' : (todayRoute?.estado === 'FINALIZADA' ? 'Ruta finalizada' : 'Finalizar ruta') }}
          </button>
        </div>
        <p v-if="finishMessage" class="ok">{{ finishMessage }}</p>
        <p v-if="finishError" class="error">{{ finishError }}</p>

        <h3>Paradas</h3>
        <ul class="stops-list" v-if="stops.length">
          <li v-for="stop in stops" :key="stop.id">
            <div class="stop-headline">
              <div>
                <strong>#{{ stop.orden }}</strong> {{ stop.nombre }}
                <span class="muted"> · {{ stop.direccion || 'Sin dirección' }}</span>
              </div>
              <div class="stop-actions">
                <button class="stop-btn arrival" @click="registerCheckin(stop.id, 'ARRIVAL')" :disabled="registeringCheckin || !canRegisterCheckins">
                  Llegada
                </button>
                <button class="stop-btn departure" @click="registerCheckin(stop.id, 'DEPARTURE')" :disabled="registeringCheckin || !canRegisterCheckins">
                  Salida
                </button>
              </div>
            </div>
            <p class="muted" v-if="lastCheckinByStop[stop.id]">
              Último check-in: {{ lastCheckinByStop[stop.id].action === 'ARRIVAL' ? 'Llegada' : 'Salida' }} · {{ formatDate(lastCheckinByStop[stop.id].createdAt) }}
            </p>
          </li>
        </ul>
        <p v-else class="muted">No hay paradas registradas para esta ruta.</p>
        <p v-if="checkinMessage" class="ok">{{ checkinMessage }}</p>
        <p v-if="checkinError" class="error">{{ checkinError }}</p>
      </div>
    </section>

    <section class="card">
      <h2>Reportar incidencia</h2>

      <form @submit.prevent="submitIncident" class="incident-form">
        <label>Tipo</label>
        <select v-model="incident.tipo" required>
          <option value="RETRASO">Retraso</option>
          <option value="MECANICO">Mecánico</option>
          <option value="ACCIDENTE">Accidente</option>
          <option value="CLIMA">Clima</option>
          <option value="OTRO">Otro</option>
        </select>

        <label>Descripción</label>
        <textarea v-model="incident.descripcion" rows="4" required placeholder="Describe qué ha ocurrido"></textarea>

        <div class="inline-fields">
          <div>
            <label>Latitud (opcional)</label>
            <input type="number" step="any" v-model="incident.latitud" />
          </div>
          <div>
            <label>Longitud (opcional)</label>
            <input type="number" step="any" v-model="incident.longitud" />
          </div>
        </div>

        <button class="btn-submit" type="submit" :disabled="submittingIncident || !todayRoute">
          {{ submittingIncident ? 'Enviando...' : 'Enviar incidencia' }}
        </button>

        <p v-if="incidentMessage" class="ok">{{ incidentMessage }}</p>
        <p v-if="incidentError" class="error">{{ incidentError }}</p>
      </form>
    </section>

    <section class="card">
      <h2>Mis incidencias</h2>

      <div v-if="loadingIncidents" class="state">Cargando incidencias...</div>
      <div v-else-if="incidentsError" class="state error">{{ incidentsError }}</div>
      <div v-else-if="incidents.length === 0" class="state">Aún no has reportado incidencias.</div>

      <ul v-else class="incidents-list">
        <li v-for="item in incidents" :key="item.id" class="incident-item">
          <div class="incident-title-row">
            <strong>#{{ item.id }} · {{ item.tipo }}</strong>
            <span :class="['incident-status', item.resuelto ? 'resolved' : 'open']">
              {{ item.resuelto ? 'Resuelta' : 'Abierta' }}
            </span>
          </div>
          <p class="incident-description">{{ item.descripcion }}</p>
          <p class="muted">
            Ruta: {{ item.route_nombre || 'Sin ruta' }} · Bus: {{ item.matricula || 'N/A' }} · Fecha: {{ formatDate(item.createdAt) }}
          </p>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { getApiUrl } from '../utils/api.js'

const props = defineProps({ username: String })

const loadingRoute = ref(false)
const routeError = ref('')
const todayRoute = ref(null)
const stops = ref([])
const checkins = ref([])
const lastCheckinByStop = ref({})
const driverId = ref(null)

const submittingIncident = ref(false)
const incidentMessage = ref('')
const incidentError = ref('')
const registeringCheckin = ref(false)
const checkinMessage = ref('')
const checkinError = ref('')
const finishingRoute = ref(false)
const finishSummary = ref('')
const finishMessage = ref('')
const finishError = ref('')
const loadingIncidents = ref(false)
const incidentsError = ref('')
const incidents = ref([])
const incident = ref({
  tipo: 'RETRASO',
  descripcion: '',
  latitud: '',
  longitud: ''
})

// GPS tracking
const gpsActivo = ref(false)
const gpsError = ref('')
let gpsIntervalId = null

function iniciarGPS() {
  if (!navigator.geolocation) {
    gpsError.value = 'Este navegador no soporta geolocalización.'
    return
  }
  if (gpsIntervalId) return
  gpsActivo.value = true
  gpsError.value = ''
  gpsIntervalId = setInterval(() => {
    if (!todayRoute.value?.bus_id || todayRoute.value.estado === 'FINALIZADA') {
      detenerGPS()
      return
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords
        fetch(`${getApiUrl()}/buses/${todayRoute.value.bus_id}/location`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ lat: latitude, lon: longitude })
        }).catch(() => {})
      },
      (err) => { gpsError.value = `GPS: ${err.message}` }
    )
  }, 1000)
}

function detenerGPS() {
  if (gpsIntervalId) {
    clearInterval(gpsIntervalId)
    gpsIntervalId = null
  }
  gpsActivo.value = false
}

const normalizeDate = (value) => {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date.toISOString()
}

const normalizeIncident = (raw) => ({
  ...raw,
  createdAt: normalizeDate(raw.createdAt || raw.created_at),
  updatedAt: normalizeDate(raw.updatedAt || raw.updated_at),
  resuelto: raw.resuelto === 1 || raw.resuelto === true
})

const normalizeCheckin = (raw) => ({
  ...raw,
  createdAt: normalizeDate(raw.createdAt || raw.created_at)
})

const buildLastCheckins = (items = []) => {
  const map = {}
  items.forEach((item) => {
    if (!map[item.stop_id]) {
      map[item.stop_id] = item
    }
  })
  lastCheckinByStop.value = map
}

const canRegisterCheckins = computed(() => {
  if (!todayRoute.value) return false
  return todayRoute.value.estado !== 'FINALIZADA'
})

const formatDate = (dateString) => {
  if (!dateString) return 'Sin fecha'
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return 'Sin fecha'
  return date.toLocaleString('es-ES')
}

const loadIncidents = async () => {
  if (!driverId.value) return

  loadingIncidents.value = true
  incidentsError.value = ''

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/driver/${driverId.value}/incidents`)
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.error || 'No se pudo cargar el historial de incidencias')
    }

    const data = await response.json()
    incidents.value = (data || []).map(normalizeIncident)
  } catch (error) {
    console.error('Error al cargar incidencias:', error)
    incidentsError.value = error?.message || 'Error al cargar incidencias'
    incidents.value = []
  } finally {
    loadingIncidents.value = false
  }
}

const loadTodayRoute = async () => {
  loadingRoute.value = true
  routeError.value = ''

  try {
    const userStr = sessionStorage.getItem('user')
    if (!userStr) {
      throw new Error('No se encontró la sesión del conductor.')
    }

    const user = JSON.parse(userStr)
    if (!user?.id) {
      throw new Error('No se encontró el identificador del conductor.')
    }

    driverId.value = user.id
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/driver/${driverId.value}/today-route`)

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.error || 'No se pudo cargar la ruta del día')
    }

    const data = await response.json()
    todayRoute.value = data.route || null
    stops.value = data.stops || []
    checkins.value = (data.checkins || []).map(normalizeCheckin)
    buildLastCheckins(checkins.value)
    await loadIncidents()

    // Start GPS sending when there is an active route with a bus
    if (todayRoute.value?.bus_id && todayRoute.value.estado !== 'FINALIZADA') {
      iniciarGPS()
    } else {
      detenerGPS()
    }
  } catch (error) {
    console.error('Error al cargar ruta del conductor:', error)
    routeError.value = error?.message || 'Error cargando ruta del día'
    todayRoute.value = null
    stops.value = []
    checkins.value = []
    lastCheckinByStop.value = {}
  } finally {
    loadingRoute.value = false
  }
}

const registerCheckin = async (stopId, action) => {
  checkinMessage.value = ''
  checkinError.value = ''

  if (!todayRoute.value?.assignment_id || !driverId.value) {
    checkinError.value = 'No hay una ruta activa para registrar check-ins.'
    return
  }

  if (!canRegisterCheckins.value) {
    checkinError.value = 'La ruta ya está finalizada y no admite más check-ins.'
    return
  }

  registeringCheckin.value = true

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/driver/checkins`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        routeAssignmentId: todayRoute.value.assignment_id,
        stopId,
        driverId: driverId.value,
        action
      })
    })

    const data = await response.json().catch(() => ({}))
    if (!response.ok) {
      throw new Error(data.error || 'No se pudo registrar el check-in')
    }

    checkinMessage.value = data.message || 'Check-in registrado'
    await loadTodayRoute()
  } catch (error) {
    console.error('Error al registrar check-in:', error)
    checkinError.value = error?.message || 'Error al registrar check-in'
  } finally {
    registeringCheckin.value = false
  }
}

const finishRoute = async () => {
  finishMessage.value = ''
  finishError.value = ''

  if (!todayRoute.value?.assignment_id || !driverId.value) {
    finishError.value = 'No hay una ruta activa para finalizar.'
    return
  }

  if (todayRoute.value.estado === 'FINALIZADA') {
    finishError.value = 'La ruta ya se encuentra finalizada.'
    return
  }

  finishingRoute.value = true

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/driver/finish-route`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        routeAssignmentId: todayRoute.value.assignment_id,
        driverId: driverId.value,
        summary: finishSummary.value
      })
    })

    const data = await response.json().catch(() => ({}))
    if (!response.ok) {
      throw new Error(data.error || 'No se pudo finalizar la ruta')
    }

    finishMessage.value = data.message || 'Ruta finalizada correctamente'
    finishSummary.value = ''
    await loadTodayRoute()
    await loadIncidents()
  } catch (error) {
    console.error('Error al finalizar ruta:', error)
    finishError.value = error?.message || 'Error al finalizar ruta'
  } finally {
    finishingRoute.value = false
  }
}

const submitIncident = async () => {
  incidentMessage.value = ''
  incidentError.value = ''

  if (!todayRoute.value?.assignment_id || !driverId.value) {
    incidentError.value = 'Debes tener una ruta asignada para reportar incidencias.'
    return
  }

  submittingIncident.value = true

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/driver/incidents`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        routeAssignmentId: todayRoute.value.assignment_id,
        driverId: driverId.value,
        tipo: incident.value.tipo,
        descripcion: incident.value.descripcion,
        latitud: incident.value.latitud === '' ? null : Number(incident.value.latitud),
        longitud: incident.value.longitud === '' ? null : Number(incident.value.longitud)
      })
    })

    const data = await response.json().catch(() => ({}))
    if (!response.ok) {
      throw new Error(data.error || 'No se pudo enviar la incidencia')
    }

    incidentMessage.value = data.message || 'Incidencia enviada correctamente'
    incident.value = { tipo: 'RETRASO', descripcion: '', latitud: '', longitud: '' }
    await loadIncidents()
  } catch (error) {
    console.error('Error al enviar incidencia:', error)
    incidentError.value = error?.message || 'Error al enviar incidencia'
  } finally {
    submittingIncident.value = false
  }
}

onMounted(loadTodayRoute)

onBeforeUnmount(detenerGPS)
</script>

<style scoped>
.driver-page {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.driver-header {
  background: linear-gradient(135deg, #37474f 0%, #263238 100%);
  color: #fff;
  border-radius: 10px;
  padding: 1.5rem;
  margin-bottom: 1rem;
}

.btn-refresh {
  margin-top: 0.8rem;
  border: none;
  border-radius: 6px;
  background: #00acc1;
  color: #fff;
  padding: 0.6rem 1rem;
  cursor: pointer;
}

.gps-status {
  margin-top: 0.5rem;
  font-size: 13px;
  padding: 0.3rem 0.7rem;
  border-radius: 20px;
  display: inline-block;
}

.gps-on {
  background: rgba(0, 200, 83, 0.25);
  color: #a5d6a7;
}

.gps-off {
  background: rgba(255, 255, 255, 0.12);
  color: #b0bec5;
}

.gps-err {
  color: #ef9a9a;
}

.btn-refresh:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.card {
  background: #fff;
  border-radius: 10px;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  margin-bottom: 1rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0.8rem;
  margin-bottom: 1rem;
}

.finish-route-box {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.7rem;
  margin-bottom: 0.9rem;
}

.finish-route-box textarea {
  border: 1px solid #d6d6d6;
  border-radius: 6px;
  padding: 0.6rem;
  font-family: inherit;
  resize: vertical;
}

.btn-finish {
  border: none;
  border-radius: 6px;
  background: #2f3e46;
  color: #fff;
  padding: 0.7rem 1rem;
  cursor: pointer;
  font-weight: 600;
}

.btn-finish:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.stops-list {
  padding-left: 1rem;
}

.stop-headline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.8rem;
}

.stop-actions {
  display: flex;
  gap: 0.5rem;
}

.stop-btn {
  border: none;
  border-radius: 6px;
  color: #fff;
  font-size: 12px;
  padding: 0.35rem 0.6rem;
  cursor: pointer;
}

.stop-btn.arrival {
  background: #2e7d32;
}

.stop-btn.departure {
  background: #ef6c00;
}

.stop-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.muted {
  color: #666;
}

.state {
  color: #37474f;
}

.incidents-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0.7rem;
}

.incident-item {
  border: 1px solid #ececec;
  border-radius: 8px;
  padding: 0.8rem;
  background: #fafafa;
}

.incident-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.6rem;
}

.incident-status {
  font-size: 12px;
  border-radius: 999px;
  padding: 0.2rem 0.5rem;
}

.incident-status.open {
  background: #fff3e0;
  color: #ef6c00;
}

.incident-status.resolved {
  background: #e8f5e9;
  color: #2e7d32;
}

.incident-description {
  margin: 0.4rem 0;
}

.error {
  color: #c62828;
}

.ok {
  color: #2e7d32;
}

.incident-form {
  display: grid;
  gap: 0.6rem;
}

.inline-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.8rem;
}

.incident-form input,
.incident-form select,
.incident-form textarea {
  border: 1px solid #d6d6d6;
  border-radius: 6px;
  padding: 0.6rem;
}

.btn-submit {
  border: none;
  border-radius: 6px;
  background: #1565c0;
  color: #fff;
  padding: 0.7rem 1rem;
  cursor: pointer;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .driver-page {
    padding: 1rem;
  }

  .finish-route-box {
    grid-template-columns: 1fr;
  }

  .inline-fields {
    grid-template-columns: 1fr;
  }
}
</style>
