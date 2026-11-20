// 공통 fetch 래퍼. recommend.js 등 기존 파일은 그대로 두고, 새 api 파일들만 이걸 씁니다.
const BASE = import.meta.env.VITE_API_BASE ?? "";

const readCookie = (name) => {
  const hit = document.cookie.split("; ").find((c) => c.startsWith(`${name}=`));
  return hit ? decodeURIComponent(hit.slice(name.length + 1)) : null;
};

// 서버가 XSRF-TOKEN 쿠키 값을 그대로 헤더로 받는 방식이라 쿠키에서 읽어 보냄
async function getCsrf() {
  let token = readCookie("XSRF-TOKEN");
  if (!token) {
    try {
      const res = await fetch(`${BASE}/auth/csrf`, { credentials: "include" });
      if (res.ok) {
        const body = await res.json(); // { headerName, token }
        token = readCookie("XSRF-TOKEN") || body.token;
      }
    } catch {
      /* csrf 꺼져 있으면 무시 */
    }
  }
  return token ? { headerName: "X-XSRF-TOKEN", token } : null;
}

export class ApiError extends Error {
  constructor(status, data) {
    super(data?.message || `API ${status}`);
    this.status = status;
    this.data = data;
  }
}

export async function http(path, { method = "GET", params, body } = {}) {
  const qs = new URLSearchParams();
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v === undefined || v === null || v === "") return;
    if (Array.isArray(v)) v.forEach((x) => qs.append(k, x));
    else qs.append(k, v);
  });
  const url = `${BASE}${path}${qs.toString() ? `?${qs}` : ""}`;

  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (method !== "GET") {
    const csrf = await getCsrf();
    if (csrf) headers[csrf.headerName] = csrf.token;
  }

  const res = await fetch(url, {
    method,
    headers,
    credentials: "include",
    redirect: "manual", // 비로그인 시 구글 로그인으로 302 되는 걸 따라가지 않음
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // 서버가 로그인 페이지로 리다이렉트 = 로그인 안 된 상태 -> 401 로 취급
  if (res.type === "opaqueredirect") throw new ApiError(401, { message: "로그인이 필요해요" });

  const text = await res.text(); // 빈 응답도 안전하게
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}