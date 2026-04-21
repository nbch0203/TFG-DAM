<template>
  <div class="gestion-container">
    <h2 class="titulo">Gestión de colegios</h2>

    <div class="mejor-filtros">
      <label>Nombre:
        <input v-model="filtroNombre" placeholder="Buscar colegio" class="input-filtro" />
      </label>
      <button class="crear-btn" @click="abrirCrear">Crear colegio</button>
    </div>

    <div class="tabla-wrapper">
      <table v-if="colegiosFiltrados.length" class="colegio-table mejor-tabla">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Dirección</th>
            <th>Teléfono</th>
            <th>Email</th>
            <th>Activo</th>
            <th>Rutas</th>
            <th>Paradas</th>
            <th>Alumnos</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="school in colegiosFiltrados" :key="school.id">
            <td>{{ school.nombre }}</td>
            <td>{{ school.direccion || '-' }}</td>
            <td>{{ school.telefono || '-' }}</td>
            <td>{{ school.email || '-' }}</td>
            <td>{{ school.activo ? 'Sí' : 'No' }}</td>
            <td>{{ school.routes_count || 0 }}</td>
            <td>{{ school.stops_count || 0 }}</td>
            <td>{{ school.students_count || 0 }}</td>
            <td>
              <button @click="abrirEditar(school)">Editar</button>
              <button @click="eliminarColegio(school)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="sin-resultados">No hay colegios para mostrar.</div>
    </div>

    <div v-if="showForm" class="modal">
      <div class="modal-content vertical-form">
        <h3>{{ editado.id ? 'Editar' : 'Crear' }} colegio</h3>
        <form @submit.prevent="guardarColegio">
          <label>Nombre:</label>
          <input v-model="editado.nombre" required />

          <label>Dirección:</label>
          <input v-model="editado.direccion" />

          <label>Teléfono:</label>
          <input v-model="editado.telefono" />

          <label>Email:</label>
          <input v-model="editado.email" type="email" />

          <label>Activo:</label>
          <select v-model="editado.activo">
            <option :value="1">Sí</option>
            <option :value="0">No</option>
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
import './SchoolManagement.css'
import { computed, onMounted, ref } from 'vue'
import { getApiUrl } from '../utils/api.js'

const schools = ref([])
const filtroNombre = ref('')
const showForm = ref(false)
const editado = ref({})
const error = ref('')

const colegiosFiltrados = computed(() => {
  const term = filtroNombre.value.trim().toLowerCase()
  if (!term) return schools.value
  return schools.value.filter((s) => (s.nombre || '').toLowerCase().includes(term))
})

const cargarColegios = async () => {
  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/colegios`)
    schools.value = response.ok ? await response.json() : []
  } catch {
    schools.value = []
  }
}

const abrirCrear = () => {
  editado.value = { nombre: '', direccion: '', telefono: '', email: '', activo: 1 }
  error.value = ''
  showForm.value = true
}

const abrirEditar = (school) => {
  editado.value = {
    id: school.id,
    nombre: school.nombre || '',
    direccion: school.direccion || '',
    telefono: school.telefono || '',
    email: school.email || '',
    activo: school.activo ? 1 : 0
  }
  error.value = ''
  showForm.value = true
}

const cerrarForm = () => {
  showForm.value = false
  editado.value = {}
  error.value = ''
}

const guardarColegio = async () => {
  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const isEdit = Boolean(editado.value.id)
    const response = await fetch(
      isEdit ? `${apiUrl}/colegios/${editado.value.id}` : `${apiUrl}/colegios`,
      {
        method: isEdit ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre: editado.value.nombre,
          direccion: editado.value.direccion || null,
          telefono: editado.value.telefono || null,
          email: editado.value.email || null,
          activo: Number(editado.value.activo) === 0 ? 0 : 1
        })
      }
    )

    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      throw new Error(data.error || 'No se pudo guardar el colegio')
    }

    cerrarForm()
    await cargarColegios()
  } catch (err) {
    error.value = err?.message || 'No se pudo conectar con el servidor.'
  }
}

const eliminarColegio = async (school) => {
  if (!confirm(`¿Eliminar el colegio ${school.nombre}?`)) return

  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/colegios/${school.id}`, { method: 'DELETE' })

    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      throw new Error(data.error || 'No se pudo eliminar el colegio')
    }

    await cargarColegios()
  } catch (err) {
    error.value = err?.message || 'No se pudo conectar con el servidor.'
  }
}

onMounted(cargarColegios)
</script>
