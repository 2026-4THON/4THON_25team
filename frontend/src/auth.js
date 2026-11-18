import { useSyncExternalStore } from "react";
import { fetchMe, requestLogout, GOOGLE_LOGIN_PATH } from "./api/auth.js";
import { setUnauthorizedHandler } from "./api/client.js";

// 모듈 단위 로그인 상태 저장소 (기존 isLoggedIn() 호출부를 그대로 유지하기 위함)
let user = null;
const listeners = new Set();

function setUser(next) {
  user = next;
  listeners.forEach((l) => l());
}

export const getUser = () => user;
export const isLoggedIn = () => user !== null;

// main.jsx에서 렌더 전에 1회 호출: 세션 쿠키로 로그인 여부 확인
export async function initAuth() {
  try {
    setUser(await fetchMe());
  } catch {
    setUser(null);
  }
}

// 구글 로그인 시작 (백엔드 OAuth로 이동 → 완료 후 새로고침되며 initAuth 재실행)
export function loginWithGoogle() {
  window.location.href = GOOGLE_LOGIN_PATH;
}

export async function logout() {
  try {
    await requestLogout();
  } finally {
    setUser(null);
  }
}

// 어떤 API든 401이면 로그인 상태 해제 → RequireAuth가 /start로 보냄
setUnauthorizedHandler(() => setUser(null));

// 컴포넌트에서 사용: const user = useUser();
export function useUser() {
  return useSyncExternalStore(
    (cb) => {
      listeners.add(cb);
      return () => listeners.delete(cb);
    },
    () => user
  );
}