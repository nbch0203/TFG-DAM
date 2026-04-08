<template>
  <div class="profesor-dashboard">
    <header class="dashboard-header">
      <h1>Bienvenido, {{ username }}</h1>
      <p>Panel de control del profesor</p>
    </header>

    <section class="dashboard-content">
      <div class="welcome-card">
        <h2>¡Hola de nuevo!</h2>
        <p>
          Desde este panel puedes gestionar tus comunicaciones con la administración.
          Usa el menú lateral para acceder a las diferentes opciones disponibles.
        </p>
      </div>

      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">📨</div>
          <h3>Enviar Mensajes</h3>
          <p>Comunica problemas, errores o solicita ayuda a la administración de forma rápida y sencilla.</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">📋</div>
          <h3>Historial</h3>
          <p>Consulta todos los mensajes que has enviado y su estado en todo momento.</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">⚙️</div>
          <h3>Configuración</h3>
          <p>Gestiona tus preferencias y configuración personal del sistema.</p>
        </div>
      </div>

      <div class="quick-stats">
        <div class="stat-box">
          <span class="stat-icon">✉️</span>
          <div class="stat-info">
            <p class="stat-label">Mensajes Enviados</p>
            <p class="stat-value">{{ statistics.sent }}</p>
          </div>
        </div>
        <div class="stat-box">
          <span class="stat-icon">✓</span>
          <div class="stat-info">
            <p class="stat-label">Resueltos</p>
            <p class="stat-value">{{ statistics.resolved }}</p>
          </div>
        </div>
        <div class="stat-box">
          <span class="stat-icon">⏱️</span>
          <div class="stat-info">
            <p class="stat-label">En Progreso</p>
            <p class="stat-value">{{ statistics.pending }}</p>
          </div>
        </div>
      </div>

      <div class="info-box">
        <h3>💡 Recomendaciones</h3>
        <ul>
          <li>Sé específico al describir los problemas para una solución más rápida</li>
          <li>Incluye pasos para reproducir errores cuando sea posible</li>
          <li>Marca como crítica si el problema requiere atención urgente</li>
          <li>Consulta regularmente tus mensajes para las respuestas de administración</li>
        </ul>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getApiUrl } from '../utils/api.js'

const props = defineProps({ username: String })

const statistics = ref({
  sent: 0,
  resolved: 0,
  pending: 0
})

onMounted(async () => {
  try {
    const apiUrl = getApiUrl()
    const senderName = props.username || 'Profesor'
    const response = await fetch(`${apiUrl}/messages/mine?senderName=${encodeURIComponent(senderName)}`)
    
    if (response.ok) {
      const messages = await response.json()
      statistics.value.sent = messages.length
      statistics.value.resolved = messages.filter(m => m.status === 'resuelto').length
      statistics.value.pending = messages.filter(m => m.status === 'nuevo' || m.status === 'en_progreso').length
    }
  } catch (error) {
    console.error('Error al cargar estadísticas:', error)
  }
})
</script>

<style scoped>
.profesor-dashboard {
  padding: 2rem;
  background: #f5f5f5;
  min-height: 100vh;
}

.dashboard-header {
  background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%);
  color: white;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dashboard-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 600;
}

.dashboard-header p {
  margin: 0.5rem 0 0 0;
  font-size: 1rem;
  opacity: 0.95;
}

.dashboard-content {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.welcome-card h2 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1.5rem;
}

.welcome-card p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.feature-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.1rem;
}

.feature-card p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
  line-height: 1.5;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-box {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2rem;
}

.stat-info {
  flex: 1;
}

.stat-label {
  margin: 0;
  color: #666;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  margin: 0.5rem 0 0 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: #1976d2;
}

.info-box {
  background: #e3f2fd;
  border-left: 4px solid #1976d2;
  padding: 1.5rem;
  border-radius: 4px;
}

.info-box h3 {
  margin: 0 0 1rem 0;
  color: #1565c0;
  font-size: 1.1rem;
}

.info-box ul {
  margin: 0;
  padding: 0 0 0 1.5rem;
}

.info-box li {
  margin-bottom: 0.5rem;
  color: #555;
  line-height: 1.6;
}

.info-box li:last-child {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .profesor-dashboard {
    padding: 1rem;
  }

  .dashboard-header {
    padding: 1.5rem;
  }

  .dashboard-header h1 {
    font-size: 1.5rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }
}
</style>
