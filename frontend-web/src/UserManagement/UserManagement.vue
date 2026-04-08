<template>
  <div class="gestion-container">
    <h2 class="titulo">Gestión de usuarios</h2>
    <div class="filtros mejor-filtros">
      <label>Rol:
        <select v-model="filtroRol" class="input-filtro">
          <option value="">Todos</option>
          <option value="ADMIN">ADMIN</option>
          <option value="PROFESOR">PROFESOR</option>
          <option value="PARENT">PARENT</option>
          <option value="DRIVER">DRIVER</option>
        </select>
      </label>
      <label>Email:
        <input v-model="filtroEmail" placeholder="Buscar por email" class="input-filtro" />
      </label>
      <button class="crear-btn" @click="showForm = true">Crear usuario</button>
    </div>
    <div class="tabla-wrapper">
      <table v-if="usuariosFiltrados.length" class="usuario-table mejor-tabla">
        <thead>
          <tr>
            <th>Email</th>
            <th>Rol</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in usuariosFiltrados" :key="user.id">
            <td>{{ user.email }}</td>
            <td>{{ user.role }}</td>
            <td>
              <button class="edit-btn" @click="abrirEditarUsuario(user)">Editar</button>
              <button class="delete-btn" @click="abrirEliminarUsuario(user)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="sin-resultados">No hay usuarios para mostrar.</div>
    </div>
    <!-- Modal eliminar usuario -->
    <div v-if="showDeleteForm" class="modal">
      <div class="modal-content vertical-form">
        <h3 class="form-title">Eliminar usuario</h3>
        <p>Para eliminar el usuario <b>{{ eliminado.email }}</b>, escribe su email y la palabra <b>CONFIRMAR</b>:</p>
        <form @submit.prevent="confirmarEliminarUsuario" class="form-vertical">
          <label for="delete-email">Email:</label>
          <input id="delete-email" v-model="deleteConfirm.email" type="email" required />
          <label for="delete-confirm">Escribe CONFIRMAR:</label>
          <input id="delete-confirm" v-model="deleteConfirm.text" type="text" required />
          <div class="form-actions">
            <button type="submit">Eliminar</button>
            <button type="button" @click="showDeleteForm = false">Cancelar</button>
          </div>
        </form>
        <p v-if="deleteError" class="error">{{ deleteError }}</p>
      </div>
    </div>
        <!-- Modal editar usuario -->
        <div v-if="showEditForm" class="modal">
          <div class="modal-content vertical-form">
            <h3 class="form-title">Editar usuario</h3>
            <form @submit.prevent="actualizarUsuario" class="form-vertical">
              <label for="edit-email">Email:</label>
              <input id="edit-email" v-model="editado.email" type="email" required />
              <label for="edit-password">Contraseña (dejar en blanco para no cambiar):</label>
              <input id="edit-password" v-model="editado.password" type="password" />
              <label for="edit-role">Rol:</label>
              <select id="edit-role" v-model="editado.role" required>
                <option value="ADMIN">ADMIN</option>
                <option value="PROFESOR">PROFESOR</option>
                <option value="PARENT">PARENT</option>
                <option value="DRIVER">DRIVER</option>
              </select>
              <div class="form-actions">
                <button type="submit">Guardar</button>
                <button type="button" @click="showEditForm = false">Cancelar</button>
              </div>
            </form>
            <p v-if="editError" class="error">{{ editError }}</p>
          </div>
        </div>
    <!-- Modal de carga -->
    <div v-if="cargando" class="modal">
      <div class="modal-content loading-modal">
        <span class="loader"></span>
        <p style="margin-top:1rem;">Cargando usuarios...</p>
      </div>
    </div>
    <div v-if="showForm" class="modal">
      <div class="modal-content vertical-form">
        <h3 class="form-title">Crear nuevo usuario</h3>
        <form @submit.prevent="crearUsuario" class="form-vertical">
          <label for="email">Email:</label>
          <input id="email" v-model="nuevo.email" type="email" required />
          <label for="password">Contraseña:</label>
          <input id="password" v-model="nuevo.password" type="password" required />
          <label for="role">Rol:</label>
          <select id="role" v-model="nuevo.role" required>
            <option value="ADMIN">ADMIN</option>
            <option value="PROFESOR">PROFESOR</option>
            <option value="PARENT">PARENT</option>
            <option value="DRIVER">DRIVER</option>
          </select>
          <div class="form-actions">
            <button type="submit">Crear</button>
            <button type="button" @click="showForm = false">Cancelar</button>
          </div>
        </form>
        <p v-if="error" class="error">{{ error }}</p>
      </div>
    </div>
  </div>
</template>
<script setup>
import './UserManagement.css'
import { ref, computed, onMounted } from 'vue'
import { getApiUrl } from '../utils/api.js'

const showDeleteForm = ref(false)
const eliminado = ref({ id: null, email: '' })
const deleteConfirm = ref({ email: '', text: '' })
const deleteError = ref('')
const users = ref([])
const showForm = ref(false)
const nuevo = ref({ email: '', password: '', role: 'ADMIN' })
const error = ref('')
const cargando = ref(true)
const filtroRol = ref('')
const filtroEmail = ref('')
const showEditForm = ref(false)
const editado = ref({ id: null, email: '', password: '', role: '' })
const editError = ref('')

const usuariosFiltrados = computed(() => {
  return users.value.filter(user => {
    const coincideRol = !filtroRol.value || user.role === filtroRol.value
    const coincideEmail = !filtroEmail.value || user.email.toLowerCase().includes(filtroEmail.value.toLowerCase())
    return coincideRol && coincideEmail
  })
})

function abrirEliminarUsuario(user) {
  eliminado.value = { id: user.id, email: user.email }
  deleteConfirm.value = { email: '', text: '' }
  deleteError.value = ''
  showDeleteForm.value = true
}

async function confirmarEliminarUsuario() {
  deleteError.value = ''
  if (deleteConfirm.value.email !== eliminado.value.email) {
    deleteError.value = 'El email no coincide.'
    return
  }
  if (deleteConfirm.value.text !== 'CONFIRMAR') {
    deleteError.value = 'Debes escribir CONFIRMAR.'
    return
  }
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/users/${eliminado.value.id}`, {
      method: 'DELETE'
    })
    if (res.ok) {
      showDeleteForm.value = false
      cargarUsuarios()
    } else {
      const data = await res.json()
      deleteError.value = data.error || 'Error al eliminar usuario.'
    }
  } catch {
    deleteError.value = 'No se pudo conectar con el servidor.'
  }
}

function abrirEditarUsuario(user) {
  editado.value = { id: user.id, email: user.email, password: '', role: user.role }
  editError.value = ''
  showEditForm.value = true
}

async function actualizarUsuario() {
  editError.value = ''
  try {
    const apiUrl = getApiUrl()
    const payload = { email: editado.value.email, role: editado.value.role }
    if (editado.value.password) payload.password = editado.value.password
    const res = await fetch(`${apiUrl}/users/${editado.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (res.ok) {
      showEditForm.value = false
      cargarUsuarios()
    } else {
      const data = await res.json()
      editError.value = data.error || 'Error al actualizar usuario.'
    }
  } catch {
    editError.value = 'No se pudo conectar con el servidor.'
  }
}

async function cargarUsuarios() {
  cargando.value = true
  const inicio = Date.now()
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/users`)
    users.value = await res.json()
  } catch {
    users.value = []
  } finally {
    const elapsed = Date.now() - inicio;
    const minTime = 2000;
    if (elapsed < minTime) {
      setTimeout(() => { cargando.value = false }, minTime - elapsed);
    } else {
      cargando.value = false;
    }
  }
}

async function crearUsuario() {
  error.value = ''
  try {
    const apiUrl = getApiUrl()
    const res = await fetch(`${apiUrl}/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(nuevo.value)
    })
    if (res.ok) {
      showForm.value = false
      nuevo.value = { email: '', password: '', role: 'ADMIN' }
      cargarUsuarios()
    } else {
      const data = await res.json()
      error.value = data.error || 'Error al crear usuario.'
    }
  } catch {
    error.value = 'No se pudo conectar con el servidor.'
  }
}

onMounted(cargarUsuarios)
</script>
