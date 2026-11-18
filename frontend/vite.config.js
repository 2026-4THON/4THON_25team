import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const BACKEND = 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': BACKEND,
      '/auth': BACKEND,
      '/oauth2': BACKEND, // 구글 로그인 시작 (Spring Security 기본 경로 가정)
      '/login': BACKEND, // OAuth 콜백 (Spring Security 기본 경로 가정)
      '/logout': BACKEND,
    },
  },
})