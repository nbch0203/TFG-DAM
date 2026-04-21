<template>
  <div class="gestion-container">
    <h2 class="titulo">Gestión de paradas</h2>
    <div class="filtros mejor-filtros">
      <label>Colegio:
        <select v-model="filtroSchool" class="input-filtro">
          <option value="">Todos</option>
          <option v-for="school in schools" :key="school.id" :value="school.id">{{ school.nombre }}</option>
        </select>
      </label>
      <label>Nombre:
        <input v-model="filtroNombre" placeholder="Buscar por nombre" class="input-filtro" />
      </label>
      <button class="crear-btn" @click="abrirCrear">Crear parada</button>
    </div>
    <div class="tabla-wrapper">
      <table v-if="paradasFiltradas.length" class="parada-table mejor-tabla">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Ruta</th>
            <th>Colegio</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="stop in paradasFiltradas" :key="stop.id">
            <td>{{ stop.nombre }}</td>
            <td>{{ stop.route_nombre || stop.route_id }}</td>
            <td>{{ stop.school_nombre || stop.school_id }}</td>
            <td>
              <button @click="abrirEditar(stop)">Editar</button>
              <button @click="eliminarParada(stop.id)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="sin-resultados">No hay paradas para mostrar.</div>
    </div>
    <div v-if="showForm" class="modal">
      <div class="modal-content vertical-form">
        <h3>{{ editado.id ? 'Editar' : 'Crear' }} parada</h3>
        <form @submit.prevent="guardarParada">
          <label>Nombre:</label>
          <input v-model="editado.nombre" required />
          <label>Ruta:</label>
          <select v-model="editado.route_id" required>
            <option disabled value="">Selecciona una ruta</option>
            <option v-for="route in rutasDisponibles" :key="route.id" :value="route.id">
              {{ route.nombre }}
            </option>
          </select>
          <label>Colegio:</label>
          <select v-model="editado.school_id" required>
            <option v-for="school in schools" :key="school.id" :value="school.id">{{ school.nombre }}</option>
          </select>
          <label>Latitud:</label>
          <input v-model="editado.latitud" required type="number" step="any" />
          <label>Longitud:</label>
          <input v-model="editado.longitud" required type="number" step="any" />
          <label>Asignar a estudiantes:</label>
          <select v-model="estudiantesSeleccionados" multiple class="estudiantes-select">
            <option v-if="students.length === 0" disabled value="">
              No hay estudiantes disponibles
            </option>
            <option v-for="student in students" :key="student.id" :value="student.id">
              {{ student.nombre }} {{ student.apellidos }}
            </option>
          </select>
          <small class="ayuda-multiselect">Mantén pulsado Ctrl para seleccionar varios alumnos.</small>
          <button type="submit">Guardar</button>
          <button type="button" @click="cerrarForm">Cancelar</button>
        </form>
        <p v-if="error" class="error">{{ error }}</p>
      </div>
    </div>
  </div>
</template>
<script setup>
import './StopManagement.css'
import { ref, onMounted, computed } from 'vue'
import { getApiUrl } from '../utils/api.js'
const stops = ref([])
const schools = ref([])
const routes = ref([])
const students = ref([])
const showForm = ref(false)
const editado = ref({})
const estudiantesSeleccionados = ref([])
const error = ref('')
const filtroSchool = ref("")
const filtroNombre = ref("")
const rutasDisponibles = computed(() => {
  if (!editado.value.school_id) return routes.value
  return routes.value.filter((route) => String(route.school_id) === String(editado.value.school_id))
})
const paradasFiltradas = computed(() => {
  return stops.value.filter(s =>
    (!filtroSchool.value || String(s.school_id) === String(filtroSchool.value)) &&
    (!filtroNombre.value || s.nombre.toLowerCase().includes(filtroNombre.value.toLowerCase()))
  )
})
async function cargarParadas() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/stops`)
    stops.value = await res.json()
  } catch { stops.value = [] }
}
async function cargarColegios() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/colegios`)
    schools.value = await res.json()
  } catch { schools.value = [] }
}
async function cargarRutas() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/routes`)
    routes.value = res.ok ? await res.json() : []
  } catch {
    routes.value = []
  }
}
async function cargarEstudiantes() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/students`)
    students.value = res.ok ? await res.json() : []
  } catch {
    students.value = []
  }
}
async function abrirEditar(stop) {
  editado.value = { ...stop }
  estudiantesSeleccionados.value = []
  // Cargar estudiantes asignados a esta parada
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/stops/${stop.id}/students`)
    if (res.ok) {
      const asignados = await res.json()
      estudiantesSeleccionados.value = asignados.map(s => s.student_id)
    }
  } catch {}
  showForm.value = true
  error.value = ''
}
function abrirCrear() {
  editado.value = {
    nombre: '',
    route_id: '',
    school_id: filtroSchool.value || '',
    latitud: '',
    longitud: '',
    orden: 1
  }
  estudiantesSeleccionados.value = []
  showForm.value = true
  error.value = ''
}
function cerrarForm() {
  showForm.value = false
  editado.value = {}
  error.value = ''
}

// ── Haversine distance (km) between two {lat, lng/longitud} points ──────────
function haversine(p1, p2) {
  const R = 6371
  const lat1 = p1.lat ?? p1.latitud
  const lng1 = p1.lng ?? p1.longitud
  const lat2 = p2.lat ?? p2.latitud
  const lng2 = p2.lng ?? p2.longitud
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1 * Math.PI / 180) *
    Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLng / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

// ── Nearest-neighbour reorder (greedy) ──────────────────────────────────────
// origen: optional {lat, lng} starting point; defaults to the first stop in the list.
// Returns a new array with the `orden` field reassigned 1…N.

function reordenarParadas(paradas, origen = null) {
  // Skip reorder if fewer than 2 stops or no coordinates available
  const conCoordenadas = paradas.filter(p => (p.lat ?? p.latitud) != null && (p.lng ?? p.longitud) != null)
  if (conCoordenadas.length < 2) {
    return paradas.map((p, i) => ({ ...p, orden: i + 1 }))
  }

  const pendientes = [...conCoordenadas]
  const ruta = []

  let actual = origen ?? pendientes.shift()
  if (!origen) ruta.push(actual)

  while (pendientes.length > 0) {
    let minDist = Infinity
    let idx = -1
    pendientes.forEach((p, i) => {
      const d = haversine(actual, p)
      if (d < minDist) { minDist = d; idx = i }
    })
    actual = pendientes.splice(idx, 1)[0]
    ruta.push(actual)
  }

  return ruta.map((p, i) => ({ ...p, orden: i + 1 }))
}

// ── After saving a stop, reorder all stops in the same route ────────────────
async function reordenarRuta(routeId) {
  const apiUrl = getApiUrl()
  // Fetch all stops for this route
  const res = await fetch(`${apiUrl}/stops`)
  const todas = await res.json()
  const deEstaRuta = todas.filter(s => String(s.route_id) === String(routeId))
  if (deEstaRuta.length < 2) return

  const reordenadas = reordenarParadas(deEstaRuta)

  // Persist the new orden for each stop
  await Promise.all(
    reordenadas.map(p =>
      fetch(`${apiUrl}/stops/${p.id}/orden`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ orden: p.orden })
      })
    )
  )
}

async function guardarParada() {
  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const method = editado.value.id ? 'PUT' : 'POST';
    const url = editado.value.id ? `${apiUrl}/stops/${editado.value.id}` : `${apiUrl}/stops`;
    const payload = {
      ...editado.value,
      route_id: Number(editado.value.route_id),
      school_id: Number(editado.value.school_id),
      latitud: editado.value.latitud === '' ? null : Number(editado.value.latitud),
      longitud: editado.value.longitud === '' ? null : Number(editado.value.longitud),
      orden: editado.value.orden === '' ? 1 : Number(editado.value.orden)
    }
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (res.ok) {
      const stopData = await res.json()
      const stopId = editado.value.id || stopData.id
      
      // Guardar asignaciones de estudiantes a la parada
      if (estudiantesSeleccionados.value.length > 0) {
        await fetch(`${apiUrl}/stops/${stopId}/assign-students`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ student_ids: estudiantesSeleccionados.value })
        })
      }
      
      showForm.value = false
      // Reorder all stops in the affected route before refreshing the list
      await reordenarRuta(editado.value.route_id)
      await cargarParadas()
    } else {
      const data = await res.json()
      error.value = data.error || 'Error al guardar parada.'
    }
  } catch { error.value = 'No se pudo conectar con el servidor.' }
}
async function eliminarParada(id) {
  if (!confirm('¿Eliminar parada?')) return
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/stops/${id}`, { method: 'DELETE' })
    if (res.ok) await cargarParadas()
  } catch {}
}
onMounted(() => { cargarParadas(); cargarColegios(); cargarRutas(); cargarEstudiantes(); })
</script>
