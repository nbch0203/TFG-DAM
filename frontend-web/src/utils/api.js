/**
 * Get the API URL based on the current window location
 * This ensures the frontend can communicate with the backend
 * regardless of whether it's running in Docker or locally
 */
export function getApiUrl() { 
  return import.meta.env.VITE_API_URL || 'http://localhost:3000'
}
