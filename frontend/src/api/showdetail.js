import { http } from "./http.js";

export const fetchShowDetail = (performanceId) => http(`/api/shows/${performanceId}`);

export const secureUrl = (url) => (url ? url.replace(/^http:\/\//, "https://") : url);