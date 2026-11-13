import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import StepTaste from "./StepTaste.jsx";
import StepFavorites from "./StepFavorites.jsx";
import StepVs from "./StepVs.jsx";
import StepFree from "./StepFree.jsx";
import LeaveModal from "./LeaveModal.jsx";
import { VS_GROUPS } from "../analysisData.js";
import { getTaste, saveTaste, buildMockSummary } from "../analysisStore.js";
import "./AnalysisSurvey.scss";

const STEPS = [1, 2, 3, 4];
const LAST_STEP = 4;

const toggleItem = (list, item) =>
  list.includes(item) ? list.filter((v) => v !== item) : [...list, item];

const isFilled = (favorite) =>
  Boolean(favorite.category && favorite.title.trim());

const isPlainObject = (value) =>
  Boolean(value) && typeof value === "object" && !Array.isArray(value);

const toStringList = (value) =>
  Array.isArray(value) ? value.filter((v) => typeof v === "string") : [];

// 분석 완료된 저장값이 있으면 수정용 초기값으로 쓴다. 없으면 빈 값.
const loadInitial = () => {
  const saved = getTaste();
  const done = isPlainObject(saved) && saved.done === true;
  const now = Date.now();
  return {
    genres: done ? toStringList(saved.genres) : [],
    moods: done ? toStringList(saved.moods) : [],
    favorites:
      done && Array.isArray(saved.favorites)
        ? saved.favorites.filter(isPlainObject).map((f, i) => ({
            id: `${now}-init-${i + 1}`,
            category: typeof f.category === "string" ? f.category : "",
            title: typeof f.title === "string" ? f.title : "",
          }))
        : [],
    vs: done && isPlainObject(saved.vs) ? saved.vs : {},
    freeText: done && typeof saved.freeText === "string" ? saved.freeText : "",
  };
};

const AnalysisSurvey = () => {
  const navigate = useNavigate();
  const [initial] = useState(loadInitial);
  const [step, setStep] = useState(1);
  const [genres, setGenres] = useState(initial.genres);
  const [moods, setMoods] = useState(initial.moods);
  const [favorites, setFavorites] = useState(initial.favorites);
  const [vs, setVs] = useState(initial.vs);
  const [freeText, setFreeText] = useState(initial.freeText);
  const [leaveOpen, setLeaveOpen] = useState(false);
  const seqRef = useRef(0);

  const createId = () => {
    seqRef.current += 1;
    return `${Date.now()}-${seqRef.current}`;
  };

  let canNext = false;
  if (step === 1) {
    canNext = genres.length > 0 && moods.length > 0;
  } else if (step === 2) {
    canNext = favorites.some(isFilled);
  } else if (step === 3) {
    canNext = VS_GROUPS.every((group) =>
      group.pairs.every((_, i) => Boolean(vs[`${group.id}-${i}`])),
    );
  } else if (step === LAST_STEP) {
    canNext = freeText.trim() !== "";
  }

  const handlePrev = () => {
    if (step <= 1) return;
    setStep((prev) => prev - 1);
  };

  const handleSubmit = () => {
    saveTaste({
      done: true,
      summary: buildMockSummary({ genres, moods }),
      genres,
      moods,
      favorites: favorites
        .filter(isFilled)
        .map((f) => ({ category: f.category, title: f.title.trim() })),
      vs,
      freeText: freeText.trim(),
      updatedAt: new Date().toISOString(),
    });
    navigate("/analysis");
  };

  const handleNext = () => {
    if (!canNext) return;
    if (step === LAST_STEP) {
      handleSubmit();
      return;
    }
    setStep((prev) => prev + 1);
  };

  return (
    <div className="survey">
      <header className="survey__header">
        <button
          type="button"
          className="survey__back"
          aria-label="뒤로"
          onClick={() => setLeaveOpen(true)}
        >
          <svg width="10" height="18" viewBox="0 0 10 18" fill="none">
            <path
              d="M9 1L1 9L9 17"
              stroke="#0078d4"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
        <h1 className="survey__title">나의 취향 레포트</h1>
      </header>

      {STEPS.slice(1).map((num) => (
        <span
          key={`line-${num}`}
          className={`survey__line survey__line--${num - 1}${num <= step ? " survey__line--on" : ""}`}
        />
      ))}
      {STEPS.map((num) => (
        <span
          key={num}
          className={`survey__pill survey__pill--${num}${num <= step ? " survey__pill--active" : ""}`}
        >
          {num}
        </span>
      ))}

      <span className="survey__divider" />

      <div className="survey__body">
        {step === 1 && (
          <StepTaste
            genres={genres}
            moods={moods}
            onToggleGenre={(genre) => setGenres((prev) => toggleItem(prev, genre))}
            onToggleMood={(mood) => setMoods((prev) => toggleItem(prev, mood))}
          />
        )}
        {step === 2 && (
          <StepFavorites
            favorites={favorites}
            onChange={setFavorites}
            createId={createId}
          />
        )}
        {step === 3 && (
          <StepVs
            vs={vs}
            onPick={(key, text) => setVs((prev) => ({ ...prev, [key]: text }))}
          />
        )}
        {step === 4 && <StepFree value={freeText} onChange={setFreeText} />}
      </div>

      {step > 1 && (
        <button type="button" className="survey__prev" onClick={handlePrev}>
          이전
        </button>
      )}
      <button
        type="button"
        className={`survey__next${step === LAST_STEP ? " survey__next--submit" : ""}${canNext ? " survey__next--enabled" : ""}`}
        onClick={handleNext}
        disabled={!canNext}
      >
        {step === LAST_STEP ? "분석하기" : "다음"}
      </button>

      {leaveOpen && (
        <LeaveModal
          onKeep={() => setLeaveOpen(false)}
          onStop={() => navigate("/analysis")}
        />
      )}
    </div>
  );
};

export default AnalysisSurvey;
