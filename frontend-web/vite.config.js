import { fileURLToPath, URL } from 'node:url'
import fs from 'fs'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue()
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    watch: {
      // En Docker sobre Windows/macOS, los eventos fs pueden no propagarse bien.
      usePolling: true,
      interval: 120
    },
    hmr: {
  host: 'schoolsafetrack.work.gd',
  protocol: 'wss',
  port: 443,
  clientPort: 443
},
proxy: {
  '/api': {
    target: 'http://backend:3000',
    changeOrigin: true,
    secure: false,
    rewrite: (path) => path
  }
}
  }
})
