import { http } from "./http.js";

export const fetchMySummary = () => http("/api/my/summary");
export const fetchAnalysisReport = () => http("/api/analysis/report"); // 없으면 404
export const postExplore = (body) => http("/api/recommendations/explore", { method: "POST", body });