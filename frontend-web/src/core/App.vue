<template>
  <div class="login-container" v-if="!loggedIn">
    <h1>Iniciar Sesión</h1>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="email">Correo electrónico</label>
        <input id="email" v-model="email" type="email" required />
      </div>
      <div class="form-group">
        <label for="password">Contraseña</label>
        <input id="password" v-model="password" type="password" required />
      </div>
      <button type="submit">Entrar</button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </div>
  <div v-else style="background: #f0f0f0; min-height: 100vh;">
    <div style="position: fixed; top: 10px; right: 10px; background: #1976d2; color: white; padding: 15px; border: 2px solid #1565c0; z-index: 9999; font-family: monospace; font-size: 12px;">
      <div style="font-weight: bold; margin-bottom: 10px;">🔍 DEBUG INFO</div>
      <p style="margin: 5px 0;">loggedIn: {{ loggedIn }}</p>
      <p style="margin: 5px 0;">userRole: "{{ userRole }}"</p>
      <p style="margin: 5px 0;">username: {{ username }}</p>
      <p style="margin: 5px 0;">Will render:</p>
      <p style="margin: 5px 0; color: yellow;">{{ userRole === 'ADMIN' ? '✓ AdminPage' : (userRole === 'PROFESOR' ? '✓ ProfesorPage' : (userRole === 'PARENT' ? '✓ ParentPage' : (userRole === 'DRIVER' ? '✓ DriverPage' : '✗ Unknown role')))  }}</p>
    </div>
    <AdminPage v-if="userRole === 'ADMIN'" :username="username" />
    <ProfesorPage v-else-if="userRole === 'PROFESOR'" :username="username" />
    <ParentPage v-else-if="userRole === 'PARENT'" :username="username" />
    <DriverPage v-else-if="userRole === 'DRIVER'" :username="username" />
    <div v-else style="padding: 2rem; background: #ffcccc; border: 2px solid #ff0000; margin: 20px;">
      <h2 style="color: #ff0000;">⚠️ Rol desconocido</h2>
      <p style="color: #ff0000; font-weight: bold;">DEBUG: Valor de userRole: "{{ userRole }}" (length: {{ userRole.length }})</p>
      <p>Este rol no es válido. Los roles esperados son: ADMIN, PROFESOR, PARENT, DRIVER</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AdminPage from '../AdminPage/AdminPage.vue'
import ProfesorPage from '../ProfesorPage/ProfesorPage.vue'
import ParentPage from '../ParentPage/ParentPage.vue'
import DriverPage from '../DriverPage/DriverPage.vue'

const email = ref('')
const password = ref('')
const error = ref('')
const loggedIn = ref(false)
const userRole = ref('')
const username = ref('')

async function handleLogin() {
  error.value = ''
  try {
    // Construir URL del API usando el protocolo y host del navegador
    const apiUrl = `${import.meta.env.VITE_API_URL}/api`
    console.log('Conectando a:', apiUrl)
    
    const response = await fetch(`${apiUrl}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value })
    })
    console.log('Response status:', response.status)
    const data = await response.json()
    console.log('Response data:', data)
    console.log('Before assignment - loggedIn:', loggedIn.value, 'userRole:', userRole.value)
    if (response.ok && data.success) {
      loggedIn.value = true
      username.value = data.user.email || email.value
      userRole.value = data.user.role ? data.user.role.toUpperCase() : ''
      // Guardar usuario en sessionStorage para que ParentPage pueda acceder
      sessionStorage.setItem('user', JSON.stringify(data.user))
      console.log('After assignment - loggedIn:', loggedIn.value, 'userRole:', userRole.value, 'username:', username.value)
      error.value = ''
    } else {
      error.value = data.error || 'Error desconocido.'
    }
  } catch (err) {
    console.error('Error en login:', err)
    error.value = 'No se pudo conectar con el servidor.'
  }
}
</script>

<style scoped>
.login-container {
  max-width: 350px;
  margin: 80px auto;
  padding: 2rem;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: center;
}
.form-group {
  margin-bottom: 1.2rem;
  text-align: left;
}
label {
  display: block;
  margin-bottom: 0.3rem;
  font-weight: 500;
}
input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}
button {
  width: 100%;
  padding: 0.7rem;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 0.5rem;
}
button:hover {
  background: #1565c0;
}
.error {
  color: #d32f2f;
  margin-top: 1rem;
}
</style>
