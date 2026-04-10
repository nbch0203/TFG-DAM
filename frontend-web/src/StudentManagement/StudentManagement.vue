<template>
  <div class="gestion-container">
    <h2 class="titulo">Gestión de alumnos</h2>
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
      <button class="crear-btn" @click="abrirCrear">Crear alumno</button>
    </div>
    <div class="tabla-wrapper">
      <table v-if="alumnosFiltrados.length" class="alumno-table mejor-tabla">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Apellidos</th>
            <th>Colegio</th>
            <th>Padre</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="alumno in alumnosFiltrados" :key="alumno.id">
            <td>{{ alumno.nombre }}</td>
            <td>{{ alumno.apellidos }}</td>
            <td>{{ alumno.school_nombre || alumno.school_id }}</td>
            <td>{{ alumno.parent_nombre }} {{ alumno.parent_apellidos }}</td>
            <td>
              <button @click="abrirEditar(alumno)">Editar</button>
              <button @click="eliminarAlumno(alumno.id)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="sin-resultados">No hay alumnos para mostrar.</div>
    </div>
    <!-- Fin tabla alumnos -->
    <div v-if="showForm" class="modal">
      <div class="modal-content vertical-form">
        <h3>{{ editado.id ? 'Editar' : 'Crear' }} alumno</h3>
        <form @submit.prevent="guardarAlumno">
          <label>Nombre:</label>
          <input v-model="editado.nombre" required />
          <label>Apellidos:</label>
          <input v-model="editado.apellidos" required />
          <label>Curso:</label>
          <input v-model="editado.curso" />
          <label>Colegio:</label>
          <select v-model="editado.school_id" required>
            <option v-for="school in schools" :key="school.id" :value="school.id">{{ school.nombre }}</option>
          </select>
          <label>Parada principal:</label>
          <select v-model="editado.stop_id">
            <option value="">Sin parada asignada</option>
            <option v-for="stop in paradasDisponibles" :key="stop.id" :value="stop.id">
              {{ stop.nombre }}
            </option>
          </select>
          <label>Padre:</label>
          <select v-model="editado.parent_id" required>
            <option disabled value="">Selecciona un padre</option>
            <option v-for="parent in parents" :key="parent.id" :value="parent.id">
              {{ parent.nombre }} {{ parent.apellidos }} ({{ parent.email }})
            </option>
          </select>
          <button type="submit">Guardar</button>
          <button type="button" @click="cerrarForm">Cancelar</button>
        </form>
        <p v-if="error" class="error">{{ error }}</p>
      </div>
    </div>
  </div>
</template>
<script setup>
import './StudentManagement.css'
import { ref, onMounted, computed } from 'vue'
import { getApiUrl } from '../utils/api.js'
const students = ref([])
const schools = ref([])
const stops = ref([])
const parents = ref([])
const showForm = ref(false)
const editado = ref({})
const error = ref('')
const filtroSchool = ref("")
const filtroNombre = ref("")
const filtroCurso = ref("")
const paradasDisponibles = computed(() => {
  if (!editado.value.school_id) return stops.value
  return stops.value.filter((stop) => String(stop.school_id) === String(editado.value.school_id))
})
const alumnosFiltrados = computed(() => {
  return students.value.filter(s =>
    (!filtroSchool.value || String(s.school_id) === String(filtroSchool.value)) &&
    (!filtroNombre.value || s.nombre.toLowerCase().includes(filtroNombre.value.toLowerCase())) &&
    (!filtroCurso.value || (s.curso || '').toLowerCase().includes(filtroCurso.value.toLowerCase()))
  )
})
async function cargarAlumnos() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/students`)
    students.value = await res.json()
  } catch { students.value = [] }
}
async function cargarColegios() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/colegios`)
    schools.value = await res.json()
  } catch { schools.value = [] }
}
async function cargarParadas() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/stops`)
    stops.value = res.ok ? await res.json() : []
  } catch { stops.value = [] }
}
async function cargarPadres() {
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/parents`)
    parents.value = res.ok ? await res.json() : []
  } catch { parents.value = [] }
}
function abrirEditar(student) {
  editado.value = {
    ...student,
    stop_id: student.stop_id_effective || student.stop_id || ''
  }
  showForm.value = true
  error.value = ''
}
function abrirCrear() {
  editado.value = {
    nombre: '',
    apellidos: '',
    curso: '',
    school_id: '',
    stop_id: '',
    parent_id: ''
  }
  showForm.value = true
  error.value = ''
}
function cerrarForm() {
  showForm.value = false
  editado.value = {}
  error.value = ''
}
async function guardarAlumno() {
  error.value = ''
  try {
    const stopId = Number(editado.value.stop_id)
    const tieneParada = Number.isInteger(stopId) && stopId > 0

    const apiUrl = getApiUrl()
    const method = editado.value.id ? 'PUT' : 'POST'
    const url = editado.value.id ? `${apiUrl}/students/${editado.value.id}` : `${apiUrl}/students`;
    const payload = {
      ...editado.value,
      parent_id: Number(editado.value.parent_id),
      school_id: Number(editado.value.school_id)
    }
    if (tieneParada) {
      payload.stop_id = stopId
      payload.stop_ids = [stopId]
    } else {
      payload.stop_id = null
      payload.stop_ids = []
    }
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (res.ok) {
      showForm.value = false
      await cargarAlumnos()
    } else {
      const data = await res.json()
      error.value = data.error || 'Error al guardar alumno.'
    }
  } catch { error.value = 'No se pudo conectar con el servidor.' }
}
async function eliminarAlumno(id) {
  if (!confirm('¿Eliminar alumno?')) return
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/students/${id}`, { method: 'DELETE' })
    if (res.ok) await cargarAlumnos()
  } catch {}
}
onMounted(() => { cargarAlumnos(); cargarColegios(); cargarParadas(); cargarPadres(); })
</script>
