// 백엔드 연동 전까지 쓰는 임시 저장소. API 가 오면 이 파일의 함수만 교체한다.
// 저장 형식: { done, summary, genres, moods, favorites, vs, freeText, updatedAt }
const KEY = "4thon-taste-v1";

export const getTaste = () => {
  try {
    const raw = window.localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export const saveTaste = (data) => {
  try {
    window.localStorage.setItem(KEY, JSON.stringify(data));
  } catch {
    // 저장소를 쓸 수 없으면 저장하지 않는다.
  }
};

export const clearTaste = () => {
  try {
    window.localStorage.removeItem(KEY);
  } catch {
    // 저장소를 쓸 수 없으면 무시한다.
  }
};

// AI 연동 시 교체: 지금은 첫 분위기와 첫 장르로 만든 임시 한줄 요약이다.
export const buildMockSummary = ({ genres, moods }) => {
  const genre = Array.isArray(genres) ? genres[0] : "";
  const mood = Array.isArray(moods) ? moods[0] : "";
  if (!genre || !mood) return "취향을 분석했어요";
  return `${mood} 분위기와 ${genre} 장르의\n작품을 선호해요`;
};
