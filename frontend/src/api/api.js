// 백엔드 호출은 전부 이 파일을 거칩니다.
// 개발: vite proxy 로 /api → 백엔드 / 배포(같은 도메인): 상대경로 그대로
const BASE = import.meta.env.VITE_API_BASE ?? "";

let csrfCache = null;
async function getCsrf() {
  if (csrfCache) return csrfCache;
  try {
    const res = await fetch(`${BASE}/auth/csrf`, { credentials: "include" });
    if (res.ok) csrfCache = await res.json(); // { headerName, token }
  } catch {
    /* csrf 가 꺼져 있으면 무시 */
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

export async function request(path, { method = "GET", params, body } = {}) {
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

// ---------- 화면용 변환 ----------
export const GENRE_LABEL = { MUSICAL: "뮤지컬", PLAY: "연극", ETC: "기타" };
export const GENRE_CODE = { 전체: "ALL", 뮤지컬: "MUSICAL", 연극: "PLAY", 기타: "ETC" };
export const STATUS_LABEL = { UPCOMING: "예정", ONGOING: "진행중", ENDED: "완료" };

// ---------- 홈 ----------
export const getHomeRecommendations = (genre = "ALL", scope = "ALL") =>
  request("/api/recommendations/home", { params: { genre, scope } });
export const getPopular = (genre = "ALL") =>
  request("/api/recommendations/popular", { params: { genre } });

// ---------- 검색 ----------
export const searchPerformances = (keyword) =>
  request("/api/performances", { params: { keyword } });

// ---------- 후기 ----------
export const getShowReviews = (id, opts = {}) =>
  request(`/api/shows/${id}/reviews`, { params: opts }); // sort, mineOnly, page, size
export const getPairReviews = (id, actorIds, opts = {}) =>
  request(`/api/shows/${id}/pairs/reviews`, { params: { actorIds, ...opts } });
export const getActorReviews = (id, actorName, opts = {}) =>
  request(`/api/shows/${id}/actors/reviews`, { params: { actorName, ...opts } });
export const likeReview = (reviewId) =>
  request(`/api/reviews/${reviewId}/likes`, { method: "POST" });
export const unlikeReview = (reviewId) =>
  request(`/api/reviews/${reviewId}/likes`, { method: "DELETE" });

// ---------- 추천 ----------
export const getPairRecommendation = (id) => request(`/api/recommendations/pair/${id}`);
export const postExplore = (body) =>
  request("/api/recommendations/explore", { method: "POST", body });

// ---------- 마이 / 분석 ----------
export const getMySummary = () => request("/api/my/summary");
export const getAnalysisReport = () => request("/api/analysis/report");