import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import StepTaste from "./StepTaste.jsx";
import StepFavorites from "./StepFavorites.jsx";
import StepVs from "./StepVs.jsx";
import StepFree from "./StepFree.jsx";
import LeaveModal from "./LeaveModal.jsx";
import { VS_GROUPS } from "../analysisData.js";
import { ApiError } from "../../../api/client.js";
import {
  fetchReport,
  createReport,
  updateReport,
} from "../../../api/analysis.js";
import { toTaste, toRequest } from "../analysisApi.js";
import "./AnalysisSurvey.scss";

const STEPS = [1, 2, 3, 4];
const LAST_STEP = 4;
const SUBMIT_ERROR = "분석하지 못했어요";

const toggleItem = (list, item) =>
  list.includes(item) ? list.filter((v) => v !== item) : [...list, item];

const isFilled = (favorite) =>
  Boolean(favorite.category && favorite.title.trim());

const AnalysisSurvey = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [hasReport, setHasReport] = useState(false);
  const [step, setStep] = useState(1);
  const [genres, setGenres] = useState([]);
  const [moods, setMoods] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [vs, setVs] = useState({});
  const [freeText, setFreeText] = useState("");
  const [leaveOpen, setLeaveOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const seqRef = useRef(0);
  const submittingRef = useRef(false);

  // 기존 리포트가 있으면 수정용 초기값으로 쓴다. 없으면(404 등) 빈 값.
  useEffect(() => {
    let cancelled = false;
    fetchReport()
      .then((report) => {
        if (cancelled) return;
        const taste = toTaste(report);
        const now = Date.now();
        setGenres(taste.genres);
        setMoods(taste.moods);
        setFavorites(
          taste.favorites.map((f, i) => ({
            id: `${now}-init-${i + 1}`,
            category: f.category ?? "",
            title: f.title ?? "",
          })),
        );
        setVs(taste.vs);
        setFreeText(taste.freeText);
        setHasReport(true);
        setLoading(false);
      })
      .catch(() => {
        if (cancelled) return;
        setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

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
    canNext = freeText.trim() !== "" && !submitting;
  }

  const handlePrev = () => {
    if (step <= 1 || submitting) return;
    setStep((prev) => prev - 1);
  };

  const saveReport = async (body) => {
    if (hasReport) return updateReport(body);
    try {
      return await createReport(body);
    } catch (err) {
      // 이미 리포트가 있으면(409) 수정으로 한 번 더 시도한다.
      if (err instanceof ApiError && err.status === 409) {
        return updateReport(body);
      }
      throw err;
    }
  };

  const handleSubmit = async () => {
    if (submittingRef.current) return;
    submittingRef.current = true;
    setSubmitting(true);
    setError("");
    try {
      await saveReport(
        toRequest({ genres, moods, favorites, vs, freeText }),
      );
      navigate("/analysis", { replace: true });
    } catch (err) {
      setError(
        err instanceof ApiError && err.message ? err.message : SUBMIT_ERROR,
      );
      submittingRef.current = false;
      setSubmitting(false);
    }
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

      {loading ? (
        <p className="survey__status">불러오는 중</p>
      ) : (
        <>
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

          {step === LAST_STEP && error && <p className="survey__error">{error}</p>}

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
            {step === LAST_STEP ? (submitting ? "분석 중..." : "분석하기") : "다음"}
          </button>
        </>
      )}

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
