/**
 * Get the API URL based on the current window location
 * This ensures the frontend can communicate with the backend
 * regardless of whether it's running in Docker or locally
 */
export function getApiUrl() { 
  const env = import.meta.env.VITE_API_URL
  if (env && String(env).trim() !== '') {
    return String(env).replace(/\/+$/, '')
  }
  // Por defecto usar ruta relativa `/api` para que el frontend haga
  // peticiones al mismo origen y NGINX pueda proxyarlas al backend.
  return '/api'
}
