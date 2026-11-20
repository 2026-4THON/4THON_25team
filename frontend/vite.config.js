import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // .env.local 에 VITE_PROXY_TARGET 을 적으면 그 주소로, 없으면 로컬 백엔드로 연결
  const target = env.VITE_PROXY_TARGET || 'http://localhost:8080'

  const proxy = {
    target,
    changeOrigin: true,
    secure: true,
    cookieDomainRewrite: 'localhost',
    configure: (p) => {
      p.on('proxyReq', (req) => {
        // POST/PUT/DELETE 는 브라우저가 Origin(localhost:5173)을 붙여서 서버 CORS 검사에 막힘 → 서버 주소로 교체
        if (req.getHeader('origin')) req.setHeader('origin', target)

        if (!env.VITE_DEV_COOKIE) return
        // 브라우저 쿠키(XSRF-TOKEN 등)는 유지하고 JSESSIONID 만 개발용 값으로 교체
        const rest = (req.getHeader('cookie') || '')
          .split(/;\s*/)
          .filter((c) => c && !c.startsWith('JSESSIONID='))
          .join('; ')
        req.setHeader('cookie', rest ? `${env.VITE_DEV_COOKIE}; ${rest}` : env.VITE_DEV_COOKIE)
      })
    },
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