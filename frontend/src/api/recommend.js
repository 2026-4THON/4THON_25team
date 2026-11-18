import { get } from "./client.js";

// genre: ALL | MUSICAL | PLAY | ETC / scope: ONGOING | ALL
// → { hasReport, briefing, items:[{performanceId,title,genre,posterUrl,status,startDate,endDate,reason}] }
export const fetchHomeRecommendations = (genre = "ALL", scope = "ONGOING") =>
  get("/api/recommendations/home", { genre, scope });

// → [{ rank, performanceId, title, genre, posterUrl, status }]
export const fetchPopular = (genre = "ALL") =>
  get("/api/recommendations/popular", { genre });