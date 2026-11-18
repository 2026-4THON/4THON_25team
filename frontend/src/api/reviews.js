import { get, post, del } from "./client.js";

// 작품 상세 (헤더의 장르/제목 표시용)
export const fetchShow = (performanceId) => get(`/api/shows/${performanceId}`);

// 배우별 후기 목록
// sort: latest | likes, mineOnly: 내 후기만
// → { content:[{reviewId,authorName,content,viewedAt,likeCount,likedByMe,...}], page,size,totalElements,totalPages,first,last }
export const fetchActorReviews = (performanceId, actorName, { sort = "latest", mineOnly = false, page = 0, size = 50 } = {}) =>
  get(`/api/shows/${performanceId}/actors/reviews`, { actorName, sort, mineOnly, page, size });

// 좋아요 등록 / 취소 → { reviewId, likeCount, likedByMe }
export const likeReview = (reviewId) => post(`/api/reviews/${reviewId}/likes`);
export const unlikeReview = (reviewId) => del(`/api/reviews/${reviewId}/likes`);