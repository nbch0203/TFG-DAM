<template>
  <div class="gestion-container">
    <h2 class="titulo">Gestión de rutas</h2>
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
      <button class="crear-btn" @click="abrirCrear">Crear ruta</button>
    </div>
    <div class="tabla-wrapper">
      <table v-if="rutasFiltradas.length" class="ruta-table mejor-tabla">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Horario</th>
            <th>Colegio</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="route in rutasFiltradas" :key="route.id">
            <td>{{ route.nombre }}</td>
            <td>{{ route.horario_inicio }} - {{ route.horario_fin }}</td>
            <td>{{ route.school_nombre || route.school_id }}</td>
            <td>
              <button @click="abrirEditar(route)">Editar</button>
              <button @click="eliminarRuta(route.id)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="sin-resultados">No hay rutas para mostrar.</div>
    </div>
    <div v-if="showForm" class="modal">
      <div class="modal-content vertical-form">
        <h3>{{ editado.id ? 'Editar' : 'Crear' }} ruta</h3>
        <form @submit.prevent="guardarRuta">
          <label>Nombre:</label>
          <input v-model="editado.nombre" required />
          <label>Horario inicio:</label>
          <input v-model="editado.horario_inicio" required type="time" />
          <label>Horario fin:</label>
          <input v-model="editado.horario_fin" required type="time" />
          <label>Colegio:</label>
          <select v-model="editado.school_id" required>
            <option v-for="school in schools" :key="school.id" :value="school.id">{{ school.nombre }}</option>
          </select>
          <button type="submit">Guardar</button>
          <button type="button" @click="cerrarForm">Cancelar</button>
        </form>
        <p v-if="error" class="error">{{ error }}</p>
      </div>
    </div>

    <section class="assignments-section">
      <h3>Asignaciones diarias (ruta-bus-conductor)</h3>

      <div class="assignment-filters">
        <label>
          Fecha:
          <input type="date" v-model="assignmentFilters.fecha" />
        </label>
        <label>
          Estado:
          <select v-model="assignmentFilters.estado">
            <option value="">Todos</option>
            <option value="PROGRAMADA">PROGRAMADA</option>
            <option value="EN_CURSO">EN_CURSO</option>
            <option value="FINALIZADA">FINALIZADA</option>
            <option value="CANCELADA">CANCELADA</option>
          </select>
        </label>
        <button @click="aplicarFiltrosAsignaciones">Aplicar filtros</button>
        <button @click="limpiarFiltrosAsignaciones">Limpiar</button>
      </div>

      <form class="assignment-form" @submit.prevent="crearAsignacion">
        <select v-model="newAssignment.routeId" required>
          <option disabled value="">Ruta</option>
          <option v-for="route in routes" :key="route.id" :value="String(route.id)">{{ route.nombre }}</option>
        </select>

        <select v-model="newAssignment.busId" required>
          <option disabled value="">Bus</option>
          <option v-for="bus in buses" :key="bus.id" :value="String(bus.id)">{{ bus.matricula }}</option>
        </select>

        <input type="date" v-model="newAssignment.fecha" required />

        <select v-model="newAssignment.driverId">
          <option value="">Conductor (opcional)</option>
          <option v-for="driver in drivers" :key="driver.id" :value="String(driver.id)">
            {{ driver.nombre }} {{ driver.apellidos || '' }}
          </option>
        </select>

        <button type="submit">Crear asignación</button>
      </form>

      <p v-if="assignmentMessage" class="assignment-ok">{{ assignmentMessage }}</p>
      <p v-if="assignmentError" class="error">{{ assignmentError }}</p>

      <div class="tabla-wrapper">
        <table v-if="assignments.length" class="ruta-table mejor-tabla">
          <thead>
            <tr>
              <th>ID</th>
              <th>Fecha</th>
              <th>Ruta</th>
              <th>Bus</th>
              <th>Conductor</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in assignments" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ formatDate(item.fecha) }}</td>
              <td>{{ item.route_nombre }}</td>
              <td>{{ item.matricula }}</td>
              <td>
                <select v-model="assignmentDriverSelection[item.id]" class="input-filtro">
                  <option disabled value="">Seleccionar conductor</option>
                  <option v-for="driver in drivers" :key="driver.id" :value="String(driver.id)">
                    {{ driver.nombre }} {{ driver.apellidos || '' }}
                  </option>
                </select>
                <div class="assignment-current">Actual: {{ item.conductor_nombre || 'Sin asignar' }}</div>
              </td>
              <td>
                <select v-model="assignmentStatusSelection[item.id]" class="input-filtro">
                  <option value="PROGRAMADA">PROGRAMADA</option>
                  <option value="EN_CURSO">EN_CURSO</option>
                  <option value="FINALIZADA">FINALIZADA</option>
                  <option value="CANCELADA">CANCELADA</option>
                </select>
              </td>
              <td>
                <button @click="guardarConductorAsignacion(item.id)">Guardar conductor</button>
                <button @click="guardarEstadoAsignacion(item.id)">Guardar estado</button>
                <button @click="eliminarAsignacion(item.id)">Eliminar</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="sin-resultados">No hay asignaciones diarias registradas.</div>
      </div>
    </section>
  </div>
</template>
<script setup>
import './RouteManagement.css'
import { ref, onMounted, computed } from 'vue'
import { getApiUrl } from '../utils/api.js'
const routes = ref([])
const schools = ref([])
const buses = ref([])
const drivers = ref([])
const assignments = ref([])
const showForm = ref(false)
const editado = ref({})
const error = ref('')
const filtroSchool = ref("")
const filtroNombre = ref("")
const assignmentError = ref('')
const assignmentMessage = ref('')
const assignmentDriverSelection = ref({})
const assignmentStatusSelection = ref({})
const assignmentFilters = ref({
  fecha: '',
  estado: ''
})
const newAssignment = ref({
  routeId: '',
  busId: '',
  fecha: '',
  driverId: ''
})
const rutasFiltradas = computed(() => {
  return routes.value.filter(r =>
    (!filtroSchool.value || String(r.school_id) === String(filtroSchool.value)) &&
    (!filtroNombre.value || r.nombre.toLowerCase().includes(filtroNombre.value.toLowerCase()))
  )
})
async function cargarRutas() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/routes`)
    routes.value = await res.json()
  } catch { routes.value = [] }
}
async function cargarBuses() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/buses`)
    buses.value = res.ok ? await res.json() : []
  } catch {
    buses.value = []
  }
}
async function cargarConductores() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/drivers`)
    drivers.value = res.ok ? await res.json() : []
  } catch {
    drivers.value = []
  }
}
async function cargarAsignaciones() {
  try {
    const apiUrl = getApiUrl()
    const query = new URLSearchParams()
    if (assignmentFilters.value.fecha) query.set('fecha', assignmentFilters.value.fecha)
    if (assignmentFilters.value.estado) query.set('estado', assignmentFilters.value.estado)

    const requestUrl = query.toString()
      ? `${apiUrl}/route-assignments?${query.toString()}`
      : `${apiUrl}/route-assignments`

    const res = await fetch(requestUrl)
    assignments.value = res.ok ? await res.json() : []
    const driverMap = {}
    const statusMap = {}
    assignments.value.forEach((item) => {
      driverMap[item.id] = item.driver_id ? String(item.driver_id) : ''
      statusMap[item.id] = item.estado || 'PROGRAMADA'
    })
    assignmentDriverSelection.value = driverMap
    assignmentStatusSelection.value = statusMap
  } catch {
    assignments.value = []
  }
}

async function aplicarFiltrosAsignaciones() {
  await cargarAsignaciones()
}

async function limpiarFiltrosAsignaciones() {
  assignmentFilters.value = { fecha: '', estado: '' }
  await cargarAsignaciones()
}
async function cargarColegios() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/colegios`)
    schools.value = await res.json()
  } catch { schools.value = [] }
}
function abrirEditar(route) {
  editado.value = { ...route }
  showForm.value = true
  error.value = ''
}
function abrirCrear() {
  editado.value = {
    nombre: '',
    horario_inicio: '',
    horario_fin: '',
    school_id: filtroSchool.value || ''
  }
  showForm.value = true
  error.value = ''
}
function cerrarForm() {
  showForm.value = false
  editado.value = {}
  error.value = ''
}
async function guardarRuta() {
  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const method = editado.value.id ? 'PUT' : 'POST';
    const url = editado.value.id ? `${apiUrl}/routes/${editado.value.id}` : `${apiUrl}/routes`;
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editado.value)
    })
    if (res.ok) {
      cerrarForm()
      await cargarRutas()
    } else {
      const data = await res.json()
      error.value = data.error || 'Error al guardar ruta.'
    }
  } catch { error.value = 'No se pudo conectar con el servidor.' }
}
async function eliminarRuta(id) {
  if (!confirm('¿Eliminar ruta?')) return
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/routes/${id}`, { method: 'DELETE' })
    if (res.ok) await cargarRutas()
  } catch {}
}
async function crearAsignacion() {
  assignmentError.value = ''
  assignmentMessage.value = ''

  try {
    const apiUrl = getApiUrl()
    const createRes = await fetch(`${apiUrl}/route-assignments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        routeId: Number(newAssignment.value.routeId),
        busId: Number(newAssignment.value.busId),
        fecha: newAssignment.value.fecha
      })
    })

    const createData = await createRes.json().catch(() => ({}))
    if (!createRes.ok) {
      throw new Error(createData.error || 'No se pudo crear la asignación')
    }

    if (newAssignment.value.driverId) {
      const assignRes = await fetch(`${apiUrl}/route-assignments/${createData.assignmentId}/driver`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ driverId: Number(newAssignment.value.driverId) })
      })

      if (!assignRes.ok) {
        const assignData = await assignRes.json().catch(() => ({}))
        throw new Error(assignData.error || 'Asignación creada pero no se pudo vincular conductor')
      }
    }

    assignmentMessage.value = 'Asignación diaria creada correctamente.'
    newAssignment.value = { routeId: '', busId: '', fecha: '', driverId: '' }
    await cargarAsignaciones()
  } catch (err) {
    assignmentError.value = err?.message || 'Error al crear asignación'
  }
}

async function guardarConductorAsignacion(assignmentId) {
  assignmentError.value = ''
  assignmentMessage.value = ''

  const selectedDriver = assignmentDriverSelection.value[assignmentId]
  if (!selectedDriver) {
    assignmentError.value = 'Debes seleccionar un conductor para guardar.'
    return
  }

  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/route-assignments/${assignmentId}/driver`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ driverId: Number(selectedDriver) })
    })

    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      throw new Error(data.error || 'No se pudo asignar el conductor')
    }

    assignmentMessage.value = 'Conductor asignado correctamente.'
    await cargarAsignaciones()
  } catch (err) {
    assignmentError.value = err?.message || 'Error al asignar conductor'
  }
}

async function guardarEstadoAsignacion(assignmentId) {
  assignmentError.value = ''
  assignmentMessage.value = ''

  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/route-assignments/${assignmentId}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ estado: assignmentStatusSelection.value[assignmentId] })
    })

    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      throw new Error(data.error || 'No se pudo actualizar el estado')
    }

    assignmentMessage.value = 'Estado de asignación actualizado.'
    await cargarAsignaciones()
  } catch (err) {
    assignmentError.value = err?.message || 'Error al actualizar estado'
  }
}

async function eliminarAsignacion(assignmentId) {
  if (!confirm('¿Eliminar esta asignación diaria?')) return

  assignmentError.value = ''
  assignmentMessage.value = ''

  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/route-assignments/${assignmentId}`, {
      method: 'DELETE'
    })

    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      throw new Error(data.error || 'No se pudo eliminar la asignación')
    }

    assignmentMessage.value = 'Asignación eliminada correctamente.'
    await cargarAsignaciones()
  } catch (err) {
    assignmentError.value = err?.message || 'Error al eliminar asignación'
  }
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleDateString('es-ES')
}

onMounted(async () => {
  await Promise.all([
    cargarRutas(),
    cargarColegios(),
    cargarBuses(),
    cargarConductores(),
    cargarAsignaciones()
  ])
})
</script>
