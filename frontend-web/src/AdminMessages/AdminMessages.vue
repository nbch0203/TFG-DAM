<template>
  <div class="admin-messages">
    <!-- Encabezado -->
    <header class="messages-header">
      <div class="header-content">
        <h1>Centro de Mensajería</h1>
        <p>Gestiona tickets de errores y mensajes del sistema</p>
       
      </div>
      <div class="header-stats">
        <div class="stat">
          <span class="stat-value">{{ totalMessages }}</span>
          <span class="stat-label">Total</span>
        </div>
        <div class="stat">
          <span class="stat-value unread">{{ unreadCount }}</span>
          <span class="stat-label">Sin leer</span>
        </div>
        <div class="stat">
          <span class="stat-value critical">{{ criticalCount }}</span>
          <span class="stat-label">Críticos</span>
        </div>
      </div>
    </header>

    <!-- Filtros -->
    <section class="filters-section">
      <div class="filter-group">
        <label>Estado:</label>
        <select v-model="filters.status" @change="applyFilters">
          <option value="">Todos</option>
          <option value="nuevo">Nuevo</option>
          <option value="abierto">Abierto</option>
          <option value="en_progreso">En progreso</option>
          <option value="resuelto">Resuelto</option>
          <option value="cerrado">Cerrado</option>
        </select>
      </div>

      <div class="filter-group">
        <label>Prioridad:</label>
        <select v-model="filters.priority" @change="applyFilters">
          <option value="">Todas</option>
          <option value="baja">Baja</option>
          <option value="media">Media</option>
          <option value="alta">Alta</option>
          <option value="crítica">Crítica</option>
        </select>
      </div>

      <div class="filter-group">
        <label>Tipo:</label>
        <select v-model="filters.type" @change="applyFilters">
          <option value="">Todos</option>
          <option value="error">Error</option>
          <option value="advertencia">Advertencia</option>
          <option value="info">Información</option>
          <option value="soporte">Soporte</option>
        </select>
      </div>

      <button @click="resetFilters" class="btn-reset">Limpiar filtros</button>
       <button class="boton-refresh" @click="loadMessages" :disabled="loading">
          {{ loading ? 'Actualizando...' : 'Actualizar' }}
        </button>
    </section>

    <!-- Contenido principal -->
    <section class="messages-content">
      <!-- Lista de mensajes -->
      <aside class="messages-list">
        <div v-if="loading" class="loading">
          <p>Cargando mensajes...</p>
        </div>

        <div v-else-if="filteredMessages.length === 0" class="empty-state">
          <p>No hay mensajes que mostrar</p>
        </div>

        <div v-else class="messages-scroll">
          <div
            v-for="message in filteredMessages"
            :key="message.id"
            :class="['message-item', { active: selectedMessage?.id === message.id, unread: !message.read }]"
            @click="selectMessage(message)"
          >
            <div class="message-item-header">
              <span :class="['priority-badge', message.priority]">{{ message.priority }}</span>
              <span :class="['status-badge', message.status]">{{ message.status }}</span>
            </div>
            <h4>{{ message.subject }}</h4>
            <p>{{ message.senderName || 'Sistema' }}</p>
            <span class="message-date">{{ formatDate(message.createdAt) }}</span>
          </div>
        </div>
      </aside>

      <!-- Panel de detalle -->
      <main class="message-detail">
        <div v-if="!selectedMessage" class="empty-detail">
          <p>Selecciona un mensaje para ver los detalles</p>
        </div>

        <div v-else class="detail-content">
          <!-- Encabezado del ticket -->
          <div class="detail-header">
            <div class="detail-title">
              <h2>{{ selectedMessage.subject }}</h2>
              <span :class="['status-badge', selectedMessage.status]">
                {{ selectedMessage.status }}
              </span>
              <span :class="['priority-badge', selectedMessage.priority]">
                {{ selectedMessage.priority }}
              </span>
            </div>

            <div class="detail-actions">
              <button
                @click="markAsRead"
                v-if="!selectedMessage.read"
                class="btn btn-primary"
              >
                Marcar como leído
              </button>
              <button
                @click="toggleInProgressStatus"
                v-if="selectedMessage.status !== 'resuelto'"
                class="btn btn-secondary"
              >
                {{ selectedMessage.status === 'en_progreso' ? 'Quitar en progreso' : 'Tomar acción' }}
              </button>
              <button
                @click="toggleResolvedStatus"
                class="btn btn-success">
                {{ selectedMessage.status === 'resuelto' ? 'Desmarcar resuelto' : 'Marcar como resuelto' }}
              </button>
              <button @click="deleteMessage" class="btn btn-danger">Eliminar</button>
            </div>
          </div>

          <!-- Información del ticket -->
          <div class="detail-info">
            <div class="info-row">
              <label>De:</label>
              <span>{{ selectedMessage.senderName || 'Sistema' }}</span>
            </div>
            <div class="info-row">
              <label>Tipo:</label>
              <span>{{ selectedMessage.type }}</span>
            </div>
            <div class="info-row">
              <label>Fecha:</label>
              <span>{{ formatDateTime(selectedMessage.createdAt) }}</span>
            </div>
            <div class="info-row">
              <label>ID del ticket:</label>
              <span class="ticket-id">#{{ selectedMessage.id }}</span>
            </div>
          </div>

          <!-- Contenido del mensaje -->
          <div class="detail-body">
            <h3>Descripción</h3>
            <div class="message-body">{{ selectedMessage.content }}</div>

            <div v-if="selectedMessage.errorDetails" class="error-details">
              <h4>Detalles técnicos</h4>
              <pre>{{ JSON.stringify(selectedMessage.errorDetails, null, 2) }}</pre>
            </div>
          </div>

          <!-- Notas -->
          <div class="detail-notes">
            <h3>Notas del administrador</h3>
            <textarea
              v-model="noteText"
              placeholder="Añade notas sobre este ticket..."
              class="notes-input"
            ></textarea>
            <button @click="addNote" class="btn btn-secondary">Guardar nota</button>

            <div v-if="selectedMessage.notes && selectedMessage.notes.length > 0" class="notes-list">
              <div v-for="(note, idx) in selectedMessage.notes" :key="idx" class="note-item">
                <p><strong>{{ note.authorName || note.author_name || note.author || 'Administrador' }}</strong></p>
                <p>{{ note.text }}</p>
                <span class="note-date">{{ formatDate(note.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getApiUrl } from '../utils/api.js'

// Estado reactivo
const messages = ref([])
const selectedMessage = ref(null)
const loading = ref(true)
const noteText = ref('')
const currentUserDisplayName = ref('Administrador')

const filters = ref({
  status: '',
  priority: '',
  type: ''
})

// Computed properties
const totalMessages = computed(() => messages.value.length)
const unreadCount = computed(() => messages.value.filter(m => !m.read).length)
const criticalCount = computed(() => messages.value.filter(m => m.priority === 'crítica').length)

const filteredMessages = computed(() => {
  return messages.value.filter(msg => {
    let match = true
    if (filters.value.status) match = match && msg.status === filters.value.status
    if (filters.value.priority) match = match && msg.priority === filters.value.priority
    if (filters.value.type) match = match && msg.type === filters.value.type
    return match
  })
})

const normalizeDateValue = (value) => {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : value
}

const normalizeMessage = (rawMessage) => {
  const createdAt = normalizeDateValue(rawMessage.createdAt || rawMessage.created_at)

  return {
    ...rawMessage,
    senderName: rawMessage.senderName || rawMessage.sender_name || 'Sistema',
    createdAt,
    updatedAt: rawMessage.updatedAt || rawMessage.updated_at || null,
    errorDetails: rawMessage.errorDetails || rawMessage.error_details || null,
    read: rawMessage.read === 1 ? true : !!rawMessage.read,
    notes: Array.isArray(rawMessage.notes)
      ? rawMessage.notes.map(note => ({
          ...note,
          authorName: note.authorName || note.author_name || note.author || 'Administrador',
          createdAt: normalizeDateValue(note.createdAt || note.created_at) || new Date().toISOString()
        }))
      : []
  }
}

const normalizeMessages = (rawMessages = []) => rawMessages.map(normalizeMessage)

// Métodos
const loadMessages = async () => {
  const selectedMessageId = selectedMessage.value?.id || null

  try {
    loading.value = true
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/admin/messages`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    if (response.ok) {
      const rawMessages = await response.json()
      messages.value = normalizeMessages(rawMessages)
      // Ordena por fecha más reciente
      messages.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))

      // Conserva seleccionado el ticket si sigue existiendo tras recargar
      if (selectedMessageId) {
        selectedMessage.value = messages.value.find(m => m.id === selectedMessageId) || null
      }
    } else {
      console.error('Error al cargar mensajes:', response.status)
      // Cargar datos de demo si falla
      loadDemoMessages()
    }
  } catch (error) {
    console.error('Error al cargar mensajes:', error)
    loadDemoMessages()
  } finally {
    loading.value = false
  }
}

const loadDemoMessages = () => {
  // Datos de demostración
  messages.value = [
    {
      id: 1,
      subject: 'Error en módulo de autobuses',
      content: 'Se ha detectado un error en la carga de la lista de autobuses. El sistema devuelve un error 500.',
      senderName: 'Sistema',
      status: 'abierto',
      priority: 'alta',
      type: 'error',
      read: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
      notes: [],
      errorDetails: {
        endpoint: '/api/buses',
        statusCode: 500,
        message: 'Database connection timeout'
      }
    },
    {
      id: 2,
      subject: 'Advertencia de uso de memoria',
      content: 'El servidor backend está usando más del 80% de memoria disponible.',
      senderName: 'Monitor del sistema',
      status: 'nuevo',
      priority: 'crítica',
      type: 'advertencia',
      read: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 15).toISOString(),
      notes: []
    },
    {
      id: 3,
      subject: 'Solicitud de soporte: Problema con login',
      content: 'Un usuario reporta que no puede iniciar sesión con su cuenta de email. El error es "Usuario no encontrado".',
      senderName: 'juan.perez@example.com',
      status: 'en_progreso',
      priority: 'media',
      type: 'soporte',
      read: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
      notes: [
        {
          text: 'Se verificó el usuario en la base de datos. Cuenta existe pero no validada.',
          createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString()
        }
      ]
    }
  ]
}

const selectMessage = (message) => {
  selectedMessage.value = message
  noteText.value = ''
  // Marcar como leído automáticamente
  if (!message.read) {
    markAsRead()
  }
}

const markAsRead = async () => {
  if (!selectedMessage.value) return

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/admin/messages/${selectedMessage.value.id}/read`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    if (response.ok) {
      selectedMessage.value.read = true
      const msg = messages.value.find(m => m.id === selectedMessage.value.id)
      if (msg) msg.read = true
    }
  } catch (error) {
    console.error('Error al marcar como leído:', error)
  }
}

const changeStatus = async (newStatus) => {
  if (!selectedMessage.value) return

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/admin/messages/${selectedMessage.value.id}/status`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ status: newStatus })
    })

    if (response.ok) {
      selectedMessage.value.status = newStatus
      const msg = messages.value.find(m => m.id === selectedMessage.value.id)
      if (msg) msg.status = newStatus
    }
  } catch (error) {
    console.error('Error al cambiar estado:', error)
  }
}

const toggleResolvedStatus = async () => {
  if (!selectedMessage.value) return

  const nextStatus = selectedMessage.value.status === 'resuelto' ? 'en_progreso' : 'resuelto'
  await changeStatus(nextStatus)
}

const toggleInProgressStatus = async () => {
  if (!selectedMessage.value) return

  const nextStatus = selectedMessage.value.status === 'en_progreso' ? 'abierto' : 'en_progreso'
  await changeStatus(nextStatus)
}

const addNote = async () => {
  if (!noteText.value.trim() || !selectedMessage.value) return

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/admin/messages/${selectedMessage.value.id}/notes`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        text: noteText.value,
        authorName: currentUserDisplayName.value
      })
    })

    if (response.ok) {
      const newNote = {
        text: noteText.value,
        authorName: currentUserDisplayName.value,
        createdAt: new Date().toISOString()
      }
      if (!selectedMessage.value.notes) selectedMessage.value.notes = []
      selectedMessage.value.notes.push(newNote)
      noteText.value = ''
    }
  } catch (error) {
    console.error('Error al guardar nota:', error)
  }
}

const deleteMessage = async () => {
  if (!selectedMessage.value) return

  if (!confirm('¿Estás seguro de que deseas eliminar este mensaje?')) return

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/admin/messages/${selectedMessage.value.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    if (response.ok) {
      messages.value = messages.value.filter(m => m.id !== selectedMessage.value.id)
      selectedMessage.value = null
    }
  } catch (error) {
    console.error('Error al eliminar mensaje:', error)
  }
}

const resetFilters = () => {
  filters.value = { status: '', priority: '', type: '' }
  applyFilters()
}

const applyFilters = () => {
  // Los computed ya manejan el filtrado automáticamente
}

// Utilidades de formato
const formatDate = (dateString) => {
  if (!dateString) return 'Sin fecha'
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return 'Sin fecha'
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return 'Ahora'
  if (diffMins < 60) return `Hace ${diffMins}m`
  if (diffHours < 24) return `Hace ${diffHours}h`
  if (diffDays < 7) return `Hace ${diffDays}d`

  return date.toLocaleDateString('es-ES')
}

const formatDateTime = (dateString) => {
  if (!dateString) return 'Sin fecha'
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return 'Sin fecha'
  return date.toLocaleString('es-ES')
}

const loadCurrentUserDisplayName = () => {
  try {
    const userStr = sessionStorage.getItem('user')
    if (!userStr) return

    const user = JSON.parse(userStr)
    currentUserDisplayName.value = user.nombre || user.name || user.email || 'Administrador'
  } catch (error) {
    console.error('No se pudo leer el usuario de sesión:', error)
  }
}

// Ciclo de vida
onMounted(() => {
  loadCurrentUserDisplayName()
  loadMessages()
})
</script>

<style scoped src="./AdminMessages.css"></style>
