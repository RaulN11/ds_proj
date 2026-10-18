import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Same paths the Traefik gateway will expose later (/auth, /users, /devices).
// For now the dev server proxies each prefix straight to its microservice.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/auth': 'http://localhost:8083',
      '/users': 'http://localhost:8081',
      '/devices': 'http://localhost:8082',
    },
  },
})
