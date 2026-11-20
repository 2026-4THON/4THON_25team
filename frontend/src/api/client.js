// 공통 fetch 클라이언트: 쿠키 포함 + CSRF 토큰 자동 첨부
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

const readCookie = (name) => {
  const hit = document.cookie.split("; ").find((c) => c.startsWith(`${name}=`));
  return hit ? decodeURIComponent(hit.slice(name.length + 1)) : null;
};

// 서버는 XSRF-TOKEN 쿠키 값을 X-XSRF-TOKEN 헤더로 받는다 (쿠키가 없으면 /auth/csrf 로 받아옴)
export async function loadCsrf(force = false) {
  let token = readCookie("XSRF-TOKEN");
  if (!token || force) {
    const res = await fetch(`${BASE_URL}/auth/csrf`, { credentials: "include" });
    if (!res.ok) throw new Error(`CSRF 조회 실패 (${res.status})`);
    const body = await res.json();
    token = readCookie("XSRF-TOKEN") ?? body.token;
  }
  return { headerName: "X-XSRF-TOKEN", token };
}

export class ApiError extends Error {
  constructor(status, message, data) {
    super(message);
    this.status = status;
    this.data = data;
  }
}

// 401 발생 시 호출할 콜백 (auth.js에서 등록)
let onUnauthorized = null;
export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn;
}

export async function request(path, { method = "GET", params, body } = {}) {
  const url = new URL(`${BASE_URL}${path}`, window.location.origin);
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== "") url.searchParams.set(k, v);
    });
  }

  const headers = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (method !== "GET") {
    const { headerName, token } = await loadCsrf();
    headers[headerName] = token;
  }

    const res = await fetch(url, {
    method,
    headers,
    credentials: "include",
    redirect: "manual", // 로그인 리다이렉트를 따라가지 않음
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // 로그인 안 된 요청이 리다이렉트로 돌아온 경우 → 401로 처리
  if (res.type === "opaqueredirect") {
    if (onUnauthorized) onUnauthorized();
    throw new ApiError(401, "로그인이 필요해요");
  }

  if (res.status === 204) return null

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    if (res.status === 401 && onUnauthorized) onUnauthorized();
    throw new ApiError(res.status, data?.message ?? `요청 실패 (${res.status})`, data);
  }
  return data;
}

export const get = (path, params) => request(path, { params });
export const post = (path, body) => request(path, { method: "POST", body });
export const put = (path, body) => request(path, { method: "PUT", body });
export const del = (path) => request(path, { method: "DELETE" });