// 서버 취향 리포트 ↔ 분석 화면 데이터 변환 함수 모음
import { VS_GROUPS } from "./analysisData.js";

const toArray = (value) => (Array.isArray(value) ? value : []);

const DEFAULT_SUMMARY = "취향을 분석했어요";

// 서버 리포트 → 화면용 taste
export const toTaste = (report) => {
  const choices = toArray(report.vsChoices);
  const vs = {};
  VS_GROUPS.forEach((group) => {
    group.pairs.forEach(([left, right], i) => {
      if (choices.includes(left)) vs[`${group.id}-${i}`] = left;
      else if (choices.includes(right)) vs[`${group.id}-${i}`] = right;
    });
  });

  return {
    done: true,
    summary:
      typeof report.briefing === "string" && report.briefing.trim()
        ? report.briefing
        : DEFAULT_SUMMARY,
    genres: toArray(report.genres),
    moods: toArray(report.moods),
    favorites: toArray(report.lifeWorks).map((work) => ({
      category: work.category,
      title: work.title,
    })),
    vs,
    freeText: report.freeText ?? "",
    updatedAt: report.updatedAt,
  };
};

// 화면 입력값 → createReport / updateReport 요청 body
export const toRequest = ({ genres, moods, favorites, vs, freeText }) => ({
  genres,
  moods,
  lifeWorks: toArray(favorites)
    .filter((f) => f.category && String(f.title ?? "").trim())
    .map((f) => ({ category: f.category, title: f.title.trim() })),
  vsChoices: VS_GROUPS.flatMap((group) =>
    group.pairs.map((_, i) => vs[`${group.id}-${i}`]),
  ),
  freeText: String(freeText ?? "").trim(),
});
