<template>
  <div class="gestion-container">
    <h2 class="titulo">Gestión de autobuses</h2>
    <div class="mejor-filtros">
      <button class="crear-btn" @click="showForm = true">Crear autobús</button>
      <button class="action-btn delete-btn" :disabled="selected.length === 0" @click="abrirEliminacionMultiple">🗑️ Eliminar seleccionados</button>
    </div>
    <p v-if="assignmentMessage" class="ok-message">{{ assignmentMessage }}</p>
    <div class="tabla-wrapper">
      <table v-if="buses.length" class="bus-table mejor-tabla">
      <thead>
        <tr>
          <th><input type="checkbox" :checked="todosSeleccionados" @change="toggleSeleccionTodos($event)" /></th>
          <th>ID</th>
          <th>Matrícula</th>
          <th>Marca</th>
          <th>Modelo</th>
          <th>Año</th>
          <th>Capacidad</th>
          <th>Color</th>
          <th>Estado</th>
          <th>Conductor</th>
          <th>Última revisión</th>
          <th>Próxima revisión</th>
          <th>Creado</th>
          <th>Actualizado</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="cargando">
          <td colspan="15">Cargando autobuses...</td>
        </tr>
        <tr v-else-if="buses.length === 0">
          <td colspan="15">No hay autobuses registrados.</td>
        </tr>
        <tr v-else v-for="bus in buses" :key="bus.id">
          <td><input type="checkbox" :value="bus.id" v-model="selected" /></td>
          <td>{{ bus.id }}</td>
          <td class="nowrap">{{ bus.matricula }}</td>
          <td class="nowrap">{{ bus.marca }}</td>
          <td class="nowrap">{{ bus.modelo }}</td>
          <td>{{ bus.anio }}</td>
          <td>{{ bus.capacidad }}</td>
          <td class="nowrap">{{ bus.color }}</td>
          <td><span :class="'estado-tag ' + bus.estado.toLowerCase()">{{ bus.estado }}</span></td>
          <td>
            <div class="driver-assign-cell">
              <select v-model="busDriverSelection[bus.id]" class="driver-select">
                <option value="">Sin asignar</option>
                <option v-for="driver in drivers" :key="driver.id" :value="String(driver.id)">
                  {{ driver.nombre }} {{ driver.apellidos || '' }}
                </option>
              </select>
              <button
                class="action-btn edit-btn"
                @click="asignarConductorBus(bus.id)"
                :disabled="assignmentLoading[bus.id]"
              >
                {{ assignmentLoading[bus.id] ? 'Guardando...' : 'Guardar' }}
              </button>
            </div>
            <div class="driver-current nowrap">
              Actual: {{ bus.conductor_nombre ? bus.conductor_nombre + ' (' + bus.conductor_email + ')' : 'Sin asignar' }}
            </div>
          </td>
          <td class="nowrap">{{ bus.ultima_revision || '-' }}</td>
          <td class="nowrap">{{ bus.proxima_revision || '-' }}</td>
          <td class="nowrap">{{ bus.created_at ? new Date(bus.created_at).toLocaleDateString() : '-' }}</td>
          <td class="nowrap">{{ bus.updated_at ? new Date(bus.updated_at).toLocaleDateString() : '-' }}</td>
        </tr>
      </tbody>
    </table>
    <div v-else class="sin-resultados">No hay autobuses registrados.</div>
    </div>
    <!-- Formulario para crear autobús -->
    <div v-if="showForm" class="modal">
      <div class="modal-content">
        <h3>Crear nuevo autobús</h3>
        <form @submit.prevent="crearBus">
          <label>Matrícula:</label>
          <input v-model="nuevo.matricula" type="text" required />
          <label>Marca:</label>
          <input v-model="nuevo.marca" type="text" required />
          <label>Modelo:</label>
          <input v-model="nuevo.modelo" type="text" required />
          <label>Año:</label>
          <input v-model="nuevo.anio" type="number" required />
          <label>Capacidad:</label>
          <input v-model="nuevo.capacidad" type="number" required />
          <label>Color:</label>
          <input v-model="nuevo.color" type="text" required />
          <label>Estado:</label>
          <select v-model="nuevo.estado" required>
            <option value="ACTIVO">Activo</option>
            <option value="MANTENIMIENTO">Mantenimiento</option>
            <option value="INACTIVO">Inactivo</option>
          </select>
          <button type="submit">Crear</button>
          <button type="button" @click="showForm = false">Cancelar</button>
        </form>
        <p v-if="error" class="error">{{ error }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import './BusManagement.css'
import { ref, onMounted, computed } from 'vue'
import { getApiUrl } from '../utils/api.js'

const buses = ref([])
const drivers = ref([])
const showForm = ref(false)
const editForm = ref(false)
const nuevo = ref({ matricula: '', marca: '', modelo: '', anio: '', capacidad: '', color: '', estado: 'ACTIVO' })
const editado = ref({})
const error = ref('')
const cargando = ref(true)
const selected = ref([])
const busDriverSelection = ref({})
const assignmentLoading = ref({})
const assignmentMessage = ref('')

// Propiedades y métodos faltantes
const todosSeleccionados = computed(() => selected.value.length === buses.value.length && buses.value.length > 0)

function toggleSeleccionTodos(event) {
  if (event.target.checked) {
    selected.value = buses.value.map(b => b.id)
  } else {
    selected.value = []
  }
}

function abrirEliminacionMultiple() {
  if (selected.value.length === 0) return
  if (confirm(`¿Eliminar ${selected.value.length} autobús(es)?`)) {
    Promise.all(selected.value.map(id => eliminarBus(id)))
    selected.value = []
  }
}

async function cargarBuses() {
  cargando.value = true
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/buses`)
    if (res.ok) {
      buses.value = await res.json()
      const mapping = {}
      buses.value.forEach((bus) => {
        mapping[bus.id] = bus.driver_id ? String(bus.driver_id) : ''
      })
      busDriverSelection.value = mapping
    } else {
      buses.value = []
    }
  } catch {
    buses.value = []
  } finally {
    cargando.value = false
  }
}

async function cargarConductores() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/drivers`)
    if (res.ok) {
      drivers.value = await res.json()
    } else {
      drivers.value = []
    }
  } catch {
    drivers.value = []
  }
}

async function asignarConductorBus(busId) {
  assignmentMessage.value = ''
  assignmentLoading.value = { ...assignmentLoading.value, [busId]: true }

  try {
    const apiUrl = getApiUrl()
    const selectedDriver = busDriverSelection.value[busId]
    const payload = {
      driverId: selectedDriver === '' ? null : Number(selectedDriver)
    }

    const res = await fetch(`${apiUrl}/buses/${busId}/driver`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) {
      const data = await res.json().catch(() => ({}))
      throw new Error(data.error || 'No se pudo actualizar la asignación del conductor')
    }

    assignmentMessage.value = 'Asignación de conductor actualizada correctamente.'
    await cargarBuses()
  } catch (err) {
    error.value = err?.message || 'Error al asignar conductor'
  } finally {
    assignmentLoading.value = { ...assignmentLoading.value, [busId]: false }
  }
}

async function crearBus() {
  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/buses`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(nuevo.value)
    })
    if (res.ok) {
      showForm.value = false
      nuevo.value = { matricula: '', marca: '', modelo: '', anio: '', capacidad: '', color: '', estado: 'ACTIVO' }
      cargarBuses()
    } else {
      const data = await res.json()
      error.value = data.error || 'Error al crear autobús.'
    }
  } catch {
    error.value = 'No se pudo conectar con el servidor.'
  }
}

async function eliminarBus(id) {
  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/buses/${id}`, {
      method: 'DELETE'
    })
    if (res.ok) {
      cargarBuses()
    } else {
      const data = await res.json()
      error.value = data.error || 'Error al eliminar autobús.'
    }
  } catch {
    error.value = 'No se pudo conectar con el servidor.'
  }
}

function editarBus(bus) {
  editado.value = { ...bus };
  editForm.value = true;
  error.value = '';
}

async function actualizarBus() {
  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/buses/${editado.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editado.value)
    })
    if (res.ok) {
      editForm.value = false
      cargarBuses()
    } else {
      const data = await res.json()
      error.value = data.error || 'Error al actualizar autobús.'
    }
  } catch {
    error.value = 'No se pudo conectar con el servidor.'
  }
}

onMounted(async () => {
  await Promise.all([cargarBuses(), cargarConductores()])
})
</script>
