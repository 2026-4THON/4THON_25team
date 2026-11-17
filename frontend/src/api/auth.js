import { get, loadCsrf } from "./client.js";

// 구글 로그인 시작 경로 (백엔드 설정에 맞게 .env에서 변경 가능)
export const GOOGLE_LOGIN_PATH =
  import.meta.env.VITE_GOOGLE_LOGIN_PATH ?? "/oauth2/authorization/google";

export const fetchMe = () => get("/auth/me");

export async function requestLogout() {
  const { headerName, token } = await loadCsrf(true);
  const res = await fetch("/logout", {
    method: "POST",
    credentials: "include",
    headers: { [headerName]: token },
  });
  if (!res.ok && res.status !== 302) throw new Error(`로그아웃 실패 (${res.status})`);
}