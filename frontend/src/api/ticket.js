import { get, post, put, del } from "./client.js";

// body 형태 (createTicket / updateTicket 공통):
// {performanceId, sessionId, seatFloor, seatRow, seatNumber, castingActorIds:[id], ticketPrice, culturePassAmount, mdPrice,
//  companion:"ALONE|PARTNER|FRIEND|FAMILY", companionRating:"RECOMMEND|NORMAL|NOT_RECOMMEND", rating, performanceReview,
//  actorReviews:[{actorId, content}], pairReviews:[{actorIds:[id], content}]}

// 응답: [{id, ticketNumber, performanceId, title, genre, venue, posterUrl, viewedAt}]
export const fetchTickets = (sort = "latest") => get("/api/tickets", { sort });

// 응답: {id, ticketNumber, performanceId, sessionId, title, genre, venue, posterUrl, runningMinutes, viewedAt, seatFloor, seatRow, seatNumber, companion, companionRating, castings:[{id,name}], ticketPrice, culturePassAmount, mdPrice, totalExpense, rating, reviews:[{id,type,content,actors:[{id,name}]}]}
export const fetchTicket = (id) => get(`/api/tickets/${id}`);

// 응답: {id, ticketNumber, message}
export const createTicket = (body) => post("/api/tickets", body);

// 응답: {id, ticketNumber, message}
export const updateTicket = (id, body) => put(`/api/tickets/${id}`, body);

// 응답: 204(null)
export const deleteTicket = (id) => del(`/api/tickets/${id}`);
