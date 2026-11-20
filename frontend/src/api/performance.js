import { get } from "./client.js";

// 응답: [{id, title, genre, venue, runningMinutes, posterUrl}]
export const searchPerformances = (keyword = "") => get("/api/performances", { keyword });

// 응답: {performanceId, dates:["YYYY-MM-DD"]}
export const fetchPerformanceDates = (id) => get(`/api/performances/${id}/dates`);

// 응답: [{id, startsAt}]
export const fetchPerformanceSessions = (id, date) => get(`/api/performances/${id}/sessions`, { date });

// 응답: {performance, sessionId, castings:[{id,name}]}
export const fetchPerformanceDetails = (id, sessionId) => get(`/api/performances/${id}/details`, { sessionId });
