import { get, post, put } from "./client.js";

// body 형태 (createReport / updateReport 공통):
// {genres:[이름], moods:[이름], lifeWorks:[{category, title}], vsChoices:[문구 8개], freeText}

// 응답: {briefing, genres, moods, lifeWorks, vsChoices, freeText, updatedAt} / 없으면 404 (ApiError status 404)
export const fetchReport = () => get("/api/analysis/report");

// 응답: {briefing, genres, moods, lifeWorks, vsChoices, freeText, updatedAt}
export const createReport = (body) => post("/api/analysis/report", body);

// 응답: {briefing, genres, moods, lifeWorks, vsChoices, freeText, updatedAt}
export const updateReport = (body) => put("/api/analysis/report", body);
