// 서버 티켓 응답 → 기록 화면/수정 폼 변환 함수 모음
import { GENRE_LABEL } from "./register/registerApi.js";
import defaultPoster from "../../assets/record/poster-sample-1.png";

export const COMPANION_LABEL = { ALONE: "혼자", PARTNER: "연인", FRIEND: "친구", FAMILY: "가족" };

export const RECOMMEND_FORM = { RECOMMEND: "good", NORMAL: "normal", NOT_RECOMMEND: "bad" };

const toArray = (value) => (Array.isArray(value) ? value : []);

const won = (value) => `${(Number(value) || 0).toLocaleString("ko-KR")}원`;

const toText = (value) => (value == null ? "" : String(value));

// "2025-01-04T14:00:00" → "2025.01.04"
export const toDotDate = (viewedAt) => toText(viewedAt).slice(0, 10).split("-").join(".");

// "2025-01-04T14:00:00" → "14:00"
export const toClock = (viewedAt) => toText(viewedAt).slice(11, 16);

// http 포스터는 https 로 바꾸고, 없으면 기본 포스터를 쓴다.
export const toPoster = (url) => {
  if (!url) return defaultPoster;
  return url.startsWith("http://") ? `https://${url.slice("http://".length)}` : url;
};

const reviewsOf = (t, type) => toArray(t.reviews).filter((review) => review.type === type);

const actorNames = (review) => toArray(review.actors).map((actor) => actor.name);

// 목록 응답 한 건 → TicketCard 의 record
export const toRecordItem = (s) => ({
  id: s.id,
  no: s.ticketNumber,
  genre: GENRE_LABEL[s.genre] ?? "기타",
  title: s.title,
  date: toDotDate(s.viewedAt),
  time: toClock(s.viewedAt),
  venue: s.venue,
  poster: toPoster(s.posterUrl),
});

// 상세 응답 → RecordDetail 화면 데이터
export const toDetail = (t) => {
  const cast = toArray(t.castings).map((c) => c.name);
  const actorReviews = {};
  reviewsOf(t, "ACTOR").forEach((review) => {
    const name = actorNames(review)[0];
    if (name) actorReviews[name] = review.content;
  });

  return {
    id: t.id,
    genre: GENRE_LABEL[t.genre] ?? "기타",
    title: t.title,
    date: toDotDate(t.viewedAt),
    time: toClock(t.viewedAt),
    venue: t.venue,
    runningTime: `${t.runningMinutes}분`,
    seat: [`${t.seatFloor}층`, `${t.seatRow}열`, `${t.seatNumber}번`],
    companion: COMPANION_LABEL[t.companion] ?? "혼자",
    cast,
    expenses: [
      { label: "티켓", amount: won(t.ticketPrice) },
      {label: "청년 문화 예술 패스",amount: Number(t.culturePassAmount) > 0 ? `-${won(t.culturePassAmount)}` : won(t.culturePassAmount),},
      { label: "MD", amount: won(t.mdPrice) },
    ],
    total: won(t.totalExpense),
    rating: t.rating,
    review: reviewsOf(t, "PERFORMANCE")[0]?.content ?? "",
    pairs: reviewsOf(t, "PAIR").map((review) => ({
      names: actorNames(review),
      content: review.content,
    })),
    actors: cast,
    actorReviews,
  };
};

// 상세 응답 + 작품 전체 출연진 → 수정 폼 초기값
export const ticketToForm = (t, perfCast) => {
  const pairs = reviewsOf(t, "PAIR");
  const form = toDetail(t);

  return {
    show: {
      id: t.performanceId,
      genre: form.genre,
      title: t.title,
      venue: t.venue,
      runtime: t.runningMinutes,
      posterUrl: t.posterUrl,
    },
    date: toText(t.viewedAt).slice(0, 10),
    time: toClock(t.viewedAt),
    sessionId: t.sessionId,
    venue: t.venue,
    runtime: toText(t.runningMinutes),
    floor: toText(t.seatFloor),
    row: toText(t.seatRow),
    seat: toText(t.seatNumber),
    casting: form.cast,
    castList: toArray(perfCast),
    price: toText(t.ticketPrice),
    pass: toText(t.culturePassAmount),
    md: toText(t.mdPrice),
    companion: form.companion,
    recommend: RECOMMEND_FORM[t.companionRating] ?? "",
    rating: t.rating,
    review: form.review,
    reviewActor: "",
    actorReviews: form.actorReviews,
    pair: pairs[0] ? actorNames(pairs[0]) : [],
    pairReview: pairs[0]?.content ?? "",
    // 두 번째 이후 페어 후기는 수정 화면에서 다루지 않으므로 그대로 보관만 한다.
    extraPairs: pairs.slice(1).map((review) => ({
      actorIds: toArray(review.actors).map((actor) => actor.id),
      content: review.content,
    })),
  };
};
