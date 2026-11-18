import { get } from "./client.js";

// { nickname, tasteBriefing, ticketCounts{musical,play,other},
//   reviewCounts{performance,actor,pair},
//   expenses{ticketTotal,culturePassTotal,mdTotal,totalSpending} }
export const fetchMySummary = () => get("/api/my/summary");