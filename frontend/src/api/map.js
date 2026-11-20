import { get } from "./client.js";

// 응답: [{venueId, name, address, latitude, longitude, count}]
export const fetchVenues = (mode = "NOW", keyword) => get("/api/map/venues", { mode, keyword });

// 응답: [{performanceId, title, genre, posterUrl, startDate, endDate, keywords}]
export const fetchVenueShows = (venueId) => get(`/api/map/venues/${venueId}/shows`);

// 응답: [{ticketId, performanceId, title, viewedAt, keywords}]
export const fetchVenueMyTickets = (venueId) => get(`/api/map/venues/${venueId}/my-tickets`);
