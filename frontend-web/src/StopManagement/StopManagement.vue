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
          <label>Orden:</label>
          <input v-model="editado.orden" required type="number" />
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
const showForm = ref(false)
const editado = ref({})
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
function abrirEditar(stop) {
  editado.value = { ...stop }
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
  showForm.value = true
  error.value = ''
}
function cerrarForm() {
  showForm.value = false
  editado.value = {}
  error.value = ''
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
      cerrarForm()
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
onMounted(() => { cargarParadas(); cargarColegios(); cargarRutas(); })
</script>
