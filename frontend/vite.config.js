import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // .env.local 에 VITE_PROXY_TARGET 을 적으면 그 주소로, 없으면 로컬 백엔드로 연결
  const target = env.VITE_PROXY_TARGET || 'http://localhost:8080'

  const proxy = {
    target,
    changeOrigin: true, // Host 헤더를 백엔드 주소로 바꿔서 전달 (Cloud Run 필수)
    secure: true,
    cookieDomainRewrite: 'localhost', // 백엔드가 준 쿠키를 localhost 쿠키로 바꿔 저장
  }

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': proxy,
        '/auth': proxy,
        '/oauth2': proxy, // 구글 로그인 시작
        '/login': proxy, // OAuth 콜백
        '/logout': proxy,
      },
    },
  }
})