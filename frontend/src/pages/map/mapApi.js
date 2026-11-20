// 지도 서버 응답 → 화면용 데이터 변환 함수 모음

export const GENRE_LABEL = { PLAY: "연극", MUSICAL: "뮤지컬", ETC: "기타" };

// ["시대극", "#고전"] → ["#시대극", "#고전"], 배열이 아니면 []
export const toTags = (keywords) =>
  Array.isArray(keywords)
    ? keywords.map((keyword) => {
        const text = String(keyword);
        return text.startsWith("#") ? text : `#${text}`;
      })
    : [];

// "2025-01-04" → "2025.01.04"
export const toDot = (dateStr) => String(dateStr ?? "").split("-").join(".");

// "2025-01-04T14:00:00" → "2025.01.04 14:00" (문자열 처리만, Date 변환 없음)
const toViewedAt = (viewedAt) => {
  const text = String(viewedAt ?? "");
  const date = toDot(text.slice(0, 10));
  const time = text.slice(11, 16);
  return time ? `${date} ${time}` : date;
};

// 공연장 진행중 공연 → 시트 아이템
export const toShowItem = (s) => ({
  id: s.performanceId,
  genre: GENRE_LABEL[s.genre] ?? "기타",
  title: s.title,
  period: `${toDot(s.startDate)} ~ ${toDot(s.endDate)}`,
  tags: toTags(s.keywords),
});

// 공연장 내 관람 기록 → 시트 아이템 (장르 없음)
export const toTicketItem = (t) => ({
  id: t.ticketId,
  genre: "",
  title: t.title,
  period: toViewedAt(t.viewedAt),
  tags: toTags(t.keywords),
});

// 서버 공연장 → 지도용 공연장
export const toVenue = (v) => ({
  id: v.venueId,
  name: v.name,
  lat: v.latitude,
  lng: v.longitude,
  count: v.count,
});

export const isKoreaPosition = (lat, lng) =>
  Number.isFinite(lat) &&
  Number.isFinite(lng) &&
  lat >= 33 &&
  lat <= 39 &&
  lng >= 124 &&
  lng <= 132;
