// 공통 fetch 래퍼. recommend.js 등 기존 파일은 그대로 두고, 새 api 파일들만 이걸 씁니다.
const BASE = import.meta.env.VITE_API_BASE ?? "";

let csrfCache = null;
async function getCsrf() {
  if (csrfCache) return csrfCache;
  try {
    const res = await fetch(`${BASE}/auth/csrf`, { credentials: "include" });
    if (res.ok) csrfCache = await res.json(); // { headerName, token }
  } catch {
    /* csrf 꺼져 있으면 무시 */
  }
  return csrfCache;
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
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  const text = await res.text(); // 빈 응답도 안전하게
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}