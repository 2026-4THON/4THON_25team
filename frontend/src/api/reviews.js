import { http } from "./http.js";

// opts: { sort: "latest" | (좋아요순 값은 스웨거 확인), mineOnly, page, size }
// 응답: { content:[{reviewId,type,authorName,content,viewedAt,actors,likeCount,likedByMe}], page,size,totalElements,totalPages,first,last }
export const fetchShowReviews = (id, opts = {}) =>
  http(`/api/shows/${id}/reviews`, { params: opts });
export const fetchPairReviews = (id, actorIds, opts = {}) =>
  http(`/api/shows/${id}/pairs/reviews`, { params: { actorIds, ...opts } });
export const fetchActorReviews = (id, actorName, opts = {}) =>
  http(`/api/shows/${id}/actors/reviews`, { params: { actorName, ...opts } });

// 응답: { reviewId, likeCount, likedByMe }
export const likeReview = (reviewId) => http(`/api/reviews/${reviewId}/likes`, { method: "POST" });
export const unlikeReview = (reviewId) => http(`/api/reviews/${reviewId}/likes`, { method: "DELETE" });

// 페어 추천: { status: RECOMMENDED|INSUFFICIENT_EVIDENCE|NO_PAIR_REVIEWS, actorIds, actors:[{actorId,name}], reason }
export const fetchPairRecommendation = (id) => http(`/api/recommendations/pair/${id}`);