/**
 * Get the API URL based on the current window location
 * This ensures the frontend can communicate with the backend
 * regardless of whether it's running in Docker or locally
 */
export function getApiUrl() {
  return `${window.location.protocol}//${window.location.hostname}:3000/api`
}
