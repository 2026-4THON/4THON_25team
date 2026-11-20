import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import "./explore.scss";
import { fetchAnalysisReport, fetchMySummary, postExplore } from "../../api/my.js";

import chevronIcon from "../../assets/home/chevron.svg"; 
import playIcon from "../../assets/home/play.svg"; 

import backIcon from "../../assets/home/back.svg";
import posterSample from "../../assets/home/poster.png";

const COMPANION_PARAM = { 혼자: "ALONE", 연인: "PARTNER", 친구: "FRIEND", 가족: "FAMILY" };
const MODE_PARAM = { report: "REPORT", new: "NEW", any: "ANY" };
const GENRE_LABEL = { MUSICAL: "뮤지컬", PLAY: "연극", ETC: "기타" };

const COMPANIONS = ["혼자", "연인", "친구", "가족"];
const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

const GENRES = [
  "로맨스", "판타지", "미스터리·추리", "코미디", "스릴러", "공포", "역사·시대극",
  "모험·액션", "드라마·휴먼", "음악·예술", "사회·정치", "SF", "성장·청춘", "비극",
];
const MOODS = [
  "밝고 유쾌한", "따뜻하고 감동적인", "어둡고 무거운", "몽환적이고 신비로운",
  "긴장감 있고 몰입감 있는", "비극적이고 슬픈", "여운이 길고 사색적인", "잔잔하고 뭉클한",
];

const makeSources = (analysis) => [
  {
    key: "report",
    title: "나의 취향 분석 레포트 반영하기",
    desc: analysis
      ? "기존에 입력했던 나의 취향 레포트를 그대로 반영해요"
      : "아직 취향 레포트가 없어요. 취향 분석을 먼저 해주세요",
    quote: analysis ? `‘${analysis}’` : undefined,
    disabled: !analysis,
  },
  {
    key: "new",
    title: "새로 설정하기",
    desc: "새로 설정하는 공연 장르는 현재 검색에만 유효하게 적용되고\n나의 취향 레포트에는 저장되지 않아요",
  },
  {
    key: "any",
    title: "아무거나 상관없어요",
    desc: "입력하신 현재 사용자님의 상황에 해당되는\n가장 인기있는 공연을 추천해드려요",
  },
];

const pickItems = (d) => d?.items ?? d?.performances ?? d?.results ?? d?.recommendations ?? [];
const toCard = (it) => ({
  id: it.performanceId ?? it.id,
  title: it.title,
  genre: GENRE_LABEL[it.genre] ?? it.genre ?? "",
  poster: it.posterUrl || posterSample,
});

const TICKET_PATH = (() => {
  const W = 338, H = 118, R = 5;
  return `M${R} 0H${W - R}A${R} ${R} 0 0 0 ${W} ${R}V${H - R}A${R} ${R} 0 0 0 ${W - R} ${H}H${R}A${R} ${R} 0 0 0 0 ${H - R}V${R}A${R} ${R} 0 0 0 ${R} 0Z`;
})();

const STEP_TITLES = {
  situation: ["STEP 1", "자세한 탐색을 위한 상황을 입력해주세요"],
  genre: ["STEP 2", "희망하는 장르를 선택해주세요"],
  taste: ["STEP 2-1", "선호하는 장르와 분위기를 선택해주세요"],
};

const pad = (n) => String(n).padStart(2, "0");
const toIso = (y, m, d) => `${y}-${pad(m + 1)}-${pad(d)}`;
const labelOf = (iso) => {
  const [, m, d] = iso.split("-");
  return `${Number(m)}월 ${Number(d)}일`;
};

function Explore() {
  const navigate = useNavigate();
  const today = new Date();

  const [step, setStep] = useState("situation"); // situation → genre → (taste) → result

  // STEP 1
  const [who, setWho] = useState(null);
  const [dates, setDates] = useState([]);
  const [cursor, setCursor] = useState({ y: today.getFullYear(), m: today.getMonth() });
  const [budget, setBudget] = useState(0); // 만원 단위, 0 = 제한 없음
  const [wish, setWish] = useState("");

  // STEP 2 / 2-1
  const [source, setSource] = useState(null);
  const [genres, setGenres] = useState([]);
  const [moods, setMoods] = useState([]);

  const [analysis, setAnalysis] = useState(null);
  const [nickname, setNickname] = useState("");
  const [result, setResult] = useState({ loading: false, error: "", status: "", items: [], summary: "" });
  const SOURCES = useMemo(() => makeSources(analysis), [analysis]);

  useEffect(() => {
    fetchAnalysisReport()
      .then((d) => setAnalysis(d?.briefing || null))
      .catch(() => setAnalysis(null));
    fetchMySummary()
      .then((d) => setNickname(d?.nickname ?? ""))
      .catch(() => {});
  }, []);

  const cells = useMemo(() => {
    const first = new Date(cursor.y, cursor.m, 1).getDay();
    const days = new Date(cursor.y, cursor.m + 1, 0).getDate();
    return [...Array(first).fill(null), ...Array.from({ length: days }, (_, i) => i + 1)];
  }, [cursor]);

  const moveMonth = (diff) =>
    setCursor(({ y, m }) => {
      const d = new Date(y, m + diff, 1);
      return { y: d.getFullYear(), m: d.getMonth() };
    });

  const toggleIn = (setter) => (value) =>
    setter((prev) => (prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]));
  const toggleDate = toggleIn(setDates);
  const toggleGenre = toggleIn(setGenres);
  const toggleMood = toggleIn(setMoods);

  const canNext = {
    situation: Boolean(who) && dates.length > 0,
    genre: Boolean(source),
    taste: genres.length > 0 && moods.length > 0,
  }[step];

  const goNext = () => {
    if (!canNext) return;
    if (step === "situation") setStep("genre");
    else if (step === "genre") {
      if (source === "new") setStep("taste");
      else runExplore();
    } else if (step === "taste") runExplore();
  };

  const runExplore = () => {
    setStep("result");
    setResult({ loading: true, error: "", status: "", items: [], summary: "" });
    const body = {
      companion: COMPANION_PARAM[who],
      dates: [...dates].sort(),
      wish: wish.trim(),
      mode: MODE_PARAM[source],
      genres: source === "new" ? genres : [],
      moods: source === "new" ? moods : [],
    };
    if (budget > 0) body.budget = budget * 10000;
    postExplore(body)
      .then((d) =>
        setResult({
          loading: false,
          error: "",
          status: d?.status ?? "",
          items: pickItems(d).slice(0, 4).map(toCard),
          summary: d?.briefing ?? d?.summary ?? d?.reason ?? "",
        })
      )
      .catch((e) => {
        console.error("[explore] 요청 실패", e);
        setResult({ loading: false, error: String(e.status ?? "error"), status: "", items: [], summary: "" });
      });
  };

  const goPrev = () => {
    if (step === "genre") setStep("situation");
    else if (step === "taste") setStep("genre");
  };

  const handleBack = () => (step === "situation" ? navigate(-1) : goPrev());

  if (step === "result") {
    const summary =
      result.summary ||
      (source === "report"
        ? analysis
        : source === "new"
          ? `${genres.join("·")} 장르와 ${moods.join(", ")} 분위기를 바탕으로 골라봤어요`
          : "조건에 맞는 가장 인기있는 공연을 추천해드려요");

    return (
      <div className="explore-result">
        <h2 className="explore-result__title">현재 @{nickname} 님에게 딱 맞는 공연은!</h2>
        <p className="explore-result__summary">{summary}</p>

        <div className="explore-result__chips">
          <span className="explore-result__chip">{who}</span>
          {budget > 0 && <span className="explore-result__chip">~{budget} 만원</span>}
        </div>
        <div className="explore-result__chips">
          {[...dates].sort().map((iso) => (
            <span className="explore-result__chip" key={iso}>
              {labelOf(iso)}
            </span>
          ))}
        </div>

        <div className="explore-result__grid">
          {result.loading && <p className="explore-result__summary">추천 공연을 찾는 중...</p>}
          {!result.loading && result.error && (
            <p className="explore-result__summary">추천을 불러오지 못했어요 ({result.error})</p>
          )}
          {!result.loading && !result.error && result.items.length === 0 && (
            <p className="explore-result__summary">
              {result.status === "NO_MATCH" ? "조건에 맞는 공연이 없어요" : "추천할 공연이 없어요"}
            </p>
          )}
          {result.items.map((item, i) => (
            <button
              key={item.id}
              type="button"
              className="xcard"
              onClick={() => navigate(`/detail/${item.id}`)}
            >
              <img className="xcard__img" src={item.poster} alt={`${item.title} 포스터`} />
              <span className="xcard__rank">{i + 1}</span>
              <span className="xcard__genre">{item.genre}</span>
              <p className="xcard__title">{item.title}</p>
            </button>
          ))}
        </div>

        <button type="button" className="explore-result__home" onClick={() => navigate("/")}>
          홈으로 돌아가기
          <img src={chevronIcon} alt="" />
        </button>
      </div>
    );
  }

  const [stepLabel, stepText] = STEP_TITLES[step];

  return (
    <div className="explore">
      <header className="explore__header">
        <button type="button" className="explore__back" onClick={handleBack} aria-label="뒤로가기">
          <img src={backIcon} alt="" />
        </button>
        <h1 className="explore__heading">내 상황에 맞춘 공연 탐색</h1>
      </header>

      <div className="explore__body">
        <h2 className={step === "taste" ? "explore__step explore__step--tight" : "explore__step"}>
          <span>{stepLabel}</span>
          <span>{stepText}</span>
        </h2>

        {/* STEP 1: 상황 입력 */}
        {step === "situation" && (
          <div className="explore__form">
            <section className="explore__section">
              <p className="explore__q">누구와 함께 가나요?</p>
              <div className="explore__who">
                {COMPANIONS.map((c) => (
                  <button
                    key={c}
                    type="button"
                    className={who === c ? "explore__who-chip explore__who-chip--on" : "explore__who-chip"}
                    onClick={() => setWho(c)}
                    aria-pressed={who === c}
                  >
                    {c}
                  </button>
                ))}
              </div>
            </section>

            <section className="explore__section">
              <div className="explore__q-row">
                <p className="explore__q">관람 희망 날짜는 언제인가요?</p>
                <span className="explore__hint">희망 일자를 모두 선택해주세요</span>
              </div>

              <div className="explore__cal">
                <div className="explore__cal-head">
                  <button type="button" className="explore__cal-move explore__cal-move--prev" onClick={() => moveMonth(-1)} aria-label="이전 달">
                    <img src={chevronIcon} alt="" />
                  </button>
                  <span className="explore__cal-title">
                    <span>{cursor.y}년</span>
                    <span>{cursor.m + 1}월</span>
                  </span>
                  <button type="button" className="explore__cal-move" onClick={() => moveMonth(1)} aria-label="다음 달">
                    <img src={chevronIcon} alt="" />
                  </button>
                </div>

                <div className="explore__cal-week">
                  {WEEKDAYS.map((w) => (
                    <span key={w}>{w}</span>
                  ))}
                </div>

                <div className="explore__cal-days">
                  {cells.map((d, i) =>
                    d === null ? (
                      <span key={`e${i}`} />
                    ) : (
                      <button
                        key={d}
                        type="button"
                        className={
                          dates.includes(toIso(cursor.y, cursor.m, d))
                            ? "explore__day explore__day--on"
                            : "explore__day"
                        }
                        onClick={() => toggleDate(toIso(cursor.y, cursor.m, d))}
                        aria-pressed={dates.includes(toIso(cursor.y, cursor.m, d))}
                      >
                        {d}
                      </button>
                    )
                  )}
                </div>
              </div>
            </section>

            <section className="explore__section">
              <p className="explore__q">1인 티켓 예산은 얼마까지 생각하나요?</p>
              <div className="explore__budget">
                <p className="explore__budget-value">
                  <strong>{budget}</strong>
                  <span>만원</span>
                </p>
                <input
                  className="explore__range"
                  type="range"
                  min={0}
                  max={20}
                  step={1}
                  value={budget}
                  onChange={(e) => setBudget(Number(e.target.value))}
                  style={{ "--ratio": budget / 20 }} 
                  aria-label="1인 티켓 예산(만원)"
                />
                <span className="explore__budget-min">0원</span>
                <span className="explore__budget-max">20만원</span>
              </div>
            </section>

            <section className="explore__section">
              <p className="explore__q">구체적인 희망사항이 있나요?</p>
              <textarea
                className="explore__wish"
                value={wish}
                onChange={(e) => setWish(e.target.value)}
              />
            </section>
          </div>
        )}

        {step === "genre" && (
          <div className="explore__options">
            {SOURCES.map(({ key, title, desc, quote, disabled }) => (
              <button
                key={key}
                type="button"
                className={source === key ? "explore__option explore__option--on" : "explore__option"}
                onClick={() => setSource(key)}
                aria-pressed={source === key}
                disabled={disabled}
                style={disabled ? { opacity: 0.4 } : undefined}
              >
                <span className="explore__option-box">
                    <svg className="explore__ticket" viewBox="0 0 338 118" aria-hidden="true">
                        <path d={TICKET_PATH} />
                    </svg>
                </span>
                <span className="explore__option-body">
                  <span className="explore__option-title">{title}</span>
                  <span className="explore__option-desc">{desc}</span>
                  {quote && <span className="explore__option-quote">{quote}</span>}
                </span>
              </button>
            ))}
          </div>
        )}

        {step === "taste" && (
          <div className="explore__form">
            <section className="explore__section">
              <p className="explore__q">1-1. 어떤 장르를 좋아하시나요?</p>
              <div className="explore__genres">
                {GENRES.map((g) => (
                  <button
                    key={g}
                    type="button"
                    className={genres.includes(g) ? "explore__pick explore__pick--on" : "explore__pick"}
                    onClick={() => toggleGenre(g)}
                    aria-pressed={genres.includes(g)}
                  >
                    {g}
                  </button>
                ))}
              </div>
            </section>

            <section className="explore__section">
              <p className="explore__q">1-2. 선호하는 분위기를 선택해주세요</p>
              <div className="explore__moods">
                {MOODS.map((m) => (
                  <button
                    key={m}
                    type="button"
                    className={moods.includes(m) ? "explore__mood explore__mood--on" : "explore__mood"}
                    onClick={() => toggleMood(m)}
                    aria-pressed={moods.includes(m)}
                  >
                    {m}
                  </button>
                ))}
              </div>
            </section>
          </div>
        )}
      </div>

      <div className={step === "situation" ? "explore__nav explore__nav--first" : "explore__nav"}>
        {step !== "situation" && (
          <button type="button" className="explore__nav-btn" onClick={goPrev}>
            이전
          </button>
        )}
        <button
          type="button"
          className={canNext ? "explore__nav-btn" : "explore__nav-btn explore__nav-btn--off"}
          onClick={goNext}
          disabled={!canNext}
        >
          다음
        </button>
      </div>
    </div>
  );
}

export default Explore;