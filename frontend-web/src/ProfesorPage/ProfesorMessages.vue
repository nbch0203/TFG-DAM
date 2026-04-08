<template>
  <div class="profesor-messages">
    <!-- Encabezado -->
    <header class="messages-header">
      <h1>Enviar mensaje a administración</h1>
      <p>Reporta problemas, solicita ayuda o comunica información importante</p>
    </header>

    <!-- Formulario de envío -->
    <section class="messages-content">
      <div class="message-form-container">
        <form @submit.prevent="sendMessage" class="message-form">
          <div class="form-group">
            <label for="subject">Asunto *</label>
            <input
              id="subject"
              v-model="form.subject"
              type="text"
              placeholder="Ej: Problema con módulo de expedientes"
              required
            />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="type">Tipo de mensaje *</label>
              <select id="type" v-model="form.type" required>
                <option value="soporte">Soporte técnico</option>
                <option value="error">Reporte de error</option>
                <option value="advertencia">Advertencia</option>
                <option value="info">Información</option>
              </select>
            </div>

            <div class="form-group">
              <label for="priority">Prioridad *</label>
              <select id="priority" v-model="form.priority" required>
                <option value="baja">Baja</option>
                <option value="media">Media</option>
                <option value="alta">Alta</option>
                <option value="crítica">Crítica</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label for="content">Mensaje *</label>
            <textarea
              id="content"
              v-model="form.content"
              placeholder="Describe el problema o proporciona detalles..."
              rows="6"
              required
            ></textarea>
          </div>

          <div class="form-actions">
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="sending"
            >
              {{ sending ? 'Enviando...' : 'Enviar mensaje' }}
            </button>
            <button
              type="button"
              class="btn btn-secondary"
              @click="resetForm"
            >
              Limpiar
            </button>
          </div>

          <!-- Mensajes de estado -->
          <div v-if="successMessage" class="alert alert-success">
            ✓ {{ successMessage }}
          </div>
          <div v-if="errorMessage" class="alert alert-error">
            ✗ {{ errorMessage }}
          </div>
        </form>
      </div>

      <!-- Información de ayuda -->
      <div class="info-section">
        <h2>¿Necesitas ayuda?</h2>
        <p>
          Todos los mensajes son revisados por administración de forma prioritaria.
          Te responderemos en el menor tiempo posible.
        </p>
        <div class="help-tips">
          <div class="tip">
            <span class="tip-icon">✎</span>
            <p>Sé específico en tu descripción del problema</p>
          </div>
          <div class="tip">
            <span class="tip-icon">🔧</span>
            <p>Incluye los pasos para reproducir el error si aplica</p>
          </div>
          <div class="tip">
            <span class="tip-icon">⚠️</span>
            <p>Marca como crítica si el problema es urgente</p>
          </div>
          <div class="tip">
            <span class="tip-icon">📁</span>
            <p>Los mensajes se guardan en tu historial</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getApiUrl } from '../utils/api.js'

const props = defineProps({ username: String })

// Estado del formulario
const form = ref({
  subject: '',
  content: '',
  type: 'soporte',
  priority: 'media'
})

const sending = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Enviar mensaje
const sendMessage = async () => {
  sending.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const apiUrl = getApiUrl()
    const response = await fetch(`${apiUrl}/messages/send`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        subject: form.value.subject,
        content: form.value.content,
        senderName: props.username || 'Profesor',
        type: form.value.type,
        priority: form.value.priority
      })
    })

    if (response.ok) {
      const data = await response.json()
      successMessage.value = data.message
      resetForm()
      // Limpiar mensaje de éxito después de 5 segundos
      setTimeout(() => {
        successMessage.value = ''
      }, 5000)
    } else {
      const error = await response.json()
      errorMessage.value = error.error || 'Error al enviar el mensaje'
    }
  } catch (error) {
    console.error('Error al enviar mensaje:', error)
    errorMessage.value = 'Error de conexión. Intenta nuevamente.'
  } finally {
    sending.value = false
  }
}

// Limpiar formulario
const resetForm = () => {
  form.value = {
    subject: '',
    content: '',
    type: 'soporte',
    priority: 'media'
  }
}
</script>

<style scoped>
.profesor-messages {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
}

/* Encabezado */
.messages-header {
  background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%);
  color: white;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.messages-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 600;
}

.messages-header p {
  margin: 0.5rem 0 0 0;
  font-size: 1rem;
  opacity: 0.95;
}

/* Contenido */
.messages-content {
  flex: 1;
  padding: 2rem;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

/* Contenedor del formulario */
.message-form-container {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.message-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Grupos de formulario */
.form-group {
  display: flex;
  flex-direction: column;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group label {
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 600;
  font-size: 0.95rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.95rem;
  font-family: inherit;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 150px;
}

/* Botones */
.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #1976d2;
  color: white;
  flex: 1;
}

.btn-primary:hover:not(:disabled) {
  background: #1565c0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(25, 118, 210, 0.3);
}

.btn-primary:disabled {
  background: #90caf9;
  cursor: not-allowed;
}

.btn-secondary {
  background: #e0e0e0;
  color: #333;
  flex: 1;
}

.btn-secondary:hover {
  background: #d0d0d0;
}

/* Alertas */
.alert {
  padding: 1rem;
  border-radius: 4px;
  margin-top: 1rem;
  font-weight: 500;
}

.alert-success {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.alert-error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

/* Sección de información */
.info-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-section h2 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1.3rem;
}

.info-section p {
  margin: 0 0 1.5rem 0;
  color: #666;
  line-height: 1.6;
}

.help-tips {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.tip {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  background: #f5f5f5;
  border-radius: 4px;
}

.tip-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.tip p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
  line-height: 1.4;
}

/* Responsive */
@media (max-width: 768px) {
  .messages-header h1 {
    font-size: 1.5rem;
  }

  .messages-content {
    padding: 1rem;
  }

  .message-form-container,
  .info-section {
    padding: 1.5rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

  .help-tips {
    grid-template-columns: 1fr;
  }
}
</style>
