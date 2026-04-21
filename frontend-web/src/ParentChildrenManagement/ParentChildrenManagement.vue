<template>
  <div class="children-container">
    <h1>👨‍👧 Mis Hijos</h1>

    <div v-if="cargando" class="loading">
      <span class="spinner"></span>
      <p>Cargando información de tus hijos...</p>
    </div>

    <div v-else-if="hijosDelPadre.length" class="children-list">
      <div v-for="hijo in hijosDelPadre" :key="hijo.id" class="child-card">
        <div class="child-header">
          <div class="child-info">
            <h3>{{ hijo.nombre }} {{ hijo.apellidos }}</h3>
            <p class="curso">{{ hijo.curso || 'Curso no especificado' }}</p>
          </div>
          <button class="ver-detalles-btn" @click="abrirDetalles(hijo)">Ver detalles</button>
        </div>

        <div class="child-details">
          <div class="detail">
            <strong>ID Alumno:</strong>
            <span>{{ hijo.id }}</span>
          </div>
          <div class="detail">
            <strong>Parada habitual:</strong>
            <span>{{ hijo.stop_nombre || 'Sin asignar' }}</span>
          </div>
          <div class="detail">
            <strong>Dirección de parada:</strong>
            <span>{{ hijo.stop_direccion || 'No disponible' }}</span>
          </div>
          <div class="detail">
            <strong>Autobuses disponibles:</strong>
            <span>{{ obtenerResumenAutobuses() }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="errorCarga" class="no-children">
      <p>⚠️ {{ errorCarga }}</p>
      <p>Intenta actualizar la página o inicia sesión de nuevo.</p>
    </div>

    <div v-else class="no-children">
      <p>📭 No hay hijos registrados en el sistema.</p>
      <p>Por favor, contacta con la escuela para registrar a tus hijos.</p>
    </div>

    <!-- Modal de detalles -->
    <div v-if="showModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Detalles de {{ selectedChild?.nombre }}</h2>
          <button class="close-btn" @click="showModal = false">✕</button>
        </div>

        <div class="modal-body">
          <div class="info-grid">
            <div class="info-item">
              <strong>Nombre completo:</strong>
              <p>{{ selectedChild?.nombre }} {{ selectedChild?.apellidos }}</p>
            </div>
            <div class="info-item">
              <strong>Curso:</strong>
              <p>{{ selectedChild?.curso || 'No especificado' }}</p>
            </div>
            <div class="info-item">
              <strong>Parada de recogida:</strong>
              <p>{{ selectedChild?.stop_nombre || 'Sin asignar' }}</p>
            </div>
            <div class="info-item">
              <strong>Autobuses disponibles:</strong>
              <p>{{ obtenerResumenAutobuses() }}</p>
            </div>
            <div class="info-item">
              <strong>Dirección de parada:</strong>
              <p>{{ selectedChild?.stop_direccion || 'No disponible' }}</p>
            </div>
          </div>

          <div class="observations" v-if="selectedChild?.observaciones">
            <h4>Observaciones:</h4>
            <p>{{ selectedChild.observaciones }}</p>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-close" @click="showModal = false">Cerrar</button>
        </div>
      </div>
    </div>

    <!-- Información de ayuda -->
    <div class="help-section">
      <h3>❓ Preguntas frecuentes</h3>
      <div class="faq">
        <div class="faq-item">
          <h4>¿Cómo sé dónde está mi hijo/a?</h4>
          <p>Usa la sección "Seguimiento de autobús" para ver la ubicación en tiempo real del autobús de tu hijo/a.</p>
        </div>
        <div class="faq-item">
          <h4>¿Puedo cambiar la parada de mi hijo/a?</h4>
          <p>Por el momento, los cambios de parada deben gestionarse directamente con la escuela.</p>
        </div>
        <div class="faq-item">
          <h4>¿Qué pasa si mi hijo/a falta un día?</h4>
          <p>Avisa directamente al colegio. La información de asistencia se actualiza en el sistema.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getApiUrl } from '../utils/api.js'

const cargando = ref(true)
const hijosDelPadre = ref([])
const autobusesDelPadre = ref([])
const showModal = ref(false)
const selectedChild = ref(null)
const errorCarga = ref('')

async function cargarDatos() {
  cargando.value = true
  errorCarga.value = ''

  try {
    const apiUrl = getApiUrl()
    const userStr = sessionStorage.getItem('user')

    if (!userStr) {
      throw new Error('No se encontró la sesión del padre.')
    }

    const user = JSON.parse(userStr)
    if (!user?.id) {
      throw new Error('No se encontró el identificador del padre en sesión.')
    }

    const [childrenRes, busesRes] = await Promise.all([
      fetch(`${apiUrl}/parent/${user.id}/children`),
      fetch(`${apiUrl}/parent/${user.id}/buses`)
    ])

    if (!childrenRes.ok) {
      throw new Error('No se pudo obtener la información de tus hijos.')
    }

    hijosDelPadre.value = (await childrenRes.json()) || []
    autobusesDelPadre.value = busesRes.ok ? ((await busesRes.json()) || []) : []
  } catch (err) {
    console.error('Error al cargar datos de hijos:', err)
    errorCarga.value = err?.message || 'No fue posible cargar tus datos en este momento.'
    hijosDelPadre.value = []
    autobusesDelPadre.value = []
  } finally {
    cargando.value = false
  }
}

function obtenerResumenAutobuses() {
  if (!autobusesDelPadre.value.length) return 'Sin autobuses activos'
  return autobusesDelPadre.value.map(b => b.matricula).join(', ')
}

function abrirDetalles(hijo) {
  selectedChild.value = hijo
  showModal.value = true
}

onMounted(cargarDatos)
</script>

<style scoped>
.children-container {
  max-width: 1000px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.children-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.child-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.child-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.child-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.child-info h3 {
  margin: 0;
  color: #333;
  font-size: 1.1rem;
}

.curso {
  margin: 0.5rem 0 0 0;
  color: #667eea;
  font-weight: 500;
  font-size: 14px;
}

.ver-detalles-btn {
  padding: 0.5rem 1rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
  transition: background 0.2s;
}

.ver-detalles-btn:hover {
  background: #5568d3;
}

.child-details {
  display: grid;
  gap: 0.8rem;
}

.detail {
  display: flex;
  justify-content: space-between;
  padding: 0.8rem;
  background: #f9f9f9;
  border-radius: 4px;
  font-size: 14px;
}

.detail strong {
  color: #667eea;
  font-weight: 600;
}

.no-children {
  background: white;
  border-radius: 8px;
  padding: 3rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  color: #666;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 2rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.info-item strong {
  display: block;
  color: #667eea;
  font-size: 12px;
  text-transform: uppercase;
  margin-bottom: 0.5rem;
}

.info-item p {
  margin: 0;
  color: #333;
}

.observations {
  background: #f9f9f9;
  padding: 1.5rem;
  border-radius: 6px;
  border-left: 4px solid #667eea;
}

.observations h4 {
  margin-top: 0;
  color: #667eea;
}

.observations p {
  margin: 0;
  color: #555;
}

.modal-footer {
  padding: 2rem;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.btn-close {
  padding: 0.7rem 1.5rem;
  background: #e0e0e0;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.btn-close:hover {
  background: #d0d0d0;
}

.help-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 2rem;
}

.help-section h3 {
  margin-top: 0;
  color: #667eea;
}

.faq {
  display: grid;
  gap: 1.5rem;
}

.faq-item h4 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1rem;
}

.faq-item p {
  margin: 0;
  color: #666;
  line-height: 1.5;
}
</style>
