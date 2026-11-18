import { http } from "./http.js";

// GET /api/performances?keyword=  ->  [{id,title,genre,venue,runningMinutes,posterUrl}]
export const searchPerformances = (keyword) =>
  http("/api/performances", { params: { keyword } });