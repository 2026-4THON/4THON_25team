// 서버 응답 ↔ 등록 화면 form 변환 함수 모음

export const GENRE_LABEL = { PLAY: "연극", MUSICAL: "뮤지컬", ETC: "기타" };

// 서버 공연 {id,title,genre,venue,runningMinutes,posterUrl} → 화면용 show
export const toShow = (p) => ({
  id: p.id,
  genre: GENRE_LABEL[p.genre] ?? "기타",
  title: p.title,
  venue: p.venue,
  runtime: p.runningMinutes,
  posterUrl: p.posterUrl,
});

// "2025-01-04T14:00:00" → "14:00"
export const toTime = (startsAt) => startsAt.slice(11, 16);

export const COMPANION_CODE = { 혼자: "ALONE", 연인: "PARTNER", 친구: "FRIEND", 가족: "FAMILY" };

export const RECOMMEND_CODE = { good: "RECOMMEND", normal: "NORMAL", bad: "NOT_RECOMMEND" };

const toAmount = (value) => Number(String(value ?? "").replace(/\D/g, "")) || 0;

const hasText = (value) => typeof value === "string" && value.trim() !== "";

// 배우 이름 배열 → castList 에서 찾은 id 배열 (없는 이름은 제외)
const toActorIds = (names, castList) =>
  names
    .map((name) => castList.find((actor) => actor.name === name)?.id)
    .filter((actorId) => actorId !== undefined);

// form → createTicket 요청 body
export const buildTicketBody = (form) => {
  const castList = Array.isArray(form.castList) ? form.castList : [];
  const actorReviews = Object.entries(form.actorReviews ?? {})
    .filter(([, content]) => hasText(content))
    .map(([name, content]) => ({ actorId: toActorIds([name], castList)[0], content }))
    .filter((review) => review.actorId !== undefined);
  const pairIds = toActorIds(form.pair ?? [], castList);
  const pairReviews = [
    ...(pairIds.length >= 2 && hasText(form.pairReview)
      ? [{ actorIds: pairIds, content: form.pairReview }]
      : []),
    ...(Array.isArray(form.extraPairs) ? form.extraPairs : []),
  ];

  return {
    performanceId: form.show.id,
    sessionId: form.sessionId,
    seatFloor: Number(form.floor),
    seatRow: Number(form.row),
    seatNumber: Number(form.seat),
    castingActorIds: toActorIds(form.casting, castList),
    ticketPrice: toAmount(form.price),
    culturePassAmount: toAmount(form.pass),
    mdPrice: toAmount(form.md),
    companion: COMPANION_CODE[form.companion] ?? "ALONE",
    companionRating: RECOMMEND_CODE[form.recommend] ?? "NORMAL",
    rating: Number(form.rating),
    performanceReview: form.review || "",
    actorReviews,
    pairReviews,
  };
};
