import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ApiError } from "../../../api/client.js";
import { fetchPerformanceDetails } from "../../../api/performance.js";
import { createTicket, fetchTicket, updateTicket } from "../../../api/ticket.js";
import { buildTicketBody } from "./registerApi.js";
import StepSelect from "./StepSelect.jsx";
import StepDetail from "./StepDetail.jsx";
import StepReview from "./StepReview.jsx";
import ConfirmModal from "../../../components/record/ConfirmModal.jsx";
import { ticketToForm } from "../recordApi.js";
import iconBack from "../../../assets/record/icon-back.svg";
import "./TicketRegister.scss";

const STEPS = ["공연선택", "상세정보", "후기작성"];
const LAST_STEP = 3;
const DETAIL_ERROR = "공연 정보를 불러오지 못했어요";
const SUBMIT_ERROR = "티켓을 등록하지 못했어요";
const UPDATE_ERROR = "티켓을 수정하지 못했어요";
const EDIT_FIRST_STEP = 2;

const INITIAL_FORM = {
  show: null,
  date: "",
  time: "",
  venue: "",
  floor: "",
  row: "",
  seat: "",
  casting: [],
  runtime: "",
  price: "",
  md: "",
  pass: "",
  companion: "",
  recommend: "",
  rating: 0,
  review: "",
  reviewActor: "",
  actorReviews: {},
  pair: [],
  pairReview: "",
  extraPairs: [],
  sessionId: null,
  castList: [],
};

const TicketRegister = ({ mode = "create" }) => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = mode === "edit";
  // 수정 모드는 공연/날짜/시간을 바꾸지 않으므로 상세정보(2단계)부터 시작한다.
  const firstStep = isEdit ? EDIT_FIRST_STEP : 1;
  const [step, setStep] = useState(firstStep);
  const [form, setForm] = useState(INITIAL_FORM);
  const [editStatus, setEditStatus] = useState(isEdit ? "loading" : "ready");
  const [leaveOpen, setLeaveOpen] = useState(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const pendingRef = useRef(false);

  useEffect(() => {
    if (!isEdit) return undefined;
    let cancelled = false;
    const loadEditForm = async () => {
      try {
        const ticket = await fetchTicket(Number(id));
        const details = await fetchPerformanceDetails(
          ticket.performanceId,
          ticket.sessionId,
        );
        if (cancelled) return;
        setForm(ticketToForm(ticket, details?.castings));
        setEditStatus("ready");
      } catch {
        if (cancelled) return;
        setEditStatus("error");
      }
    };
    loadEditForm();
    return () => {
      cancelled = true;
    };
  }, [isEdit, id]);

  const handleChange = (patch) => {
    setForm((prev) => {
      const next = { ...prev, ...patch };
      if ("show" in patch) {
        next.venue = "";
        next.runtime = "";
      }
      if ("casting" in patch) {
        next.pair = prev.pair.filter((name) => next.casting.includes(name));
      }
      return next;
    });
  };

  const handlePrev = () => {
    if (step <= firstStep || pending) return;
    setError("");
    setStep((prev) => prev - 1);
  };

  let canNext = true;
  if (step === 1) {
    canNext = Boolean(form.show && form.date && form.time && form.sessionId);
  } else if (step === 2) {
    canNext = Boolean(
      form.floor && form.row && form.seat && form.casting.length > 0,
    );
  } else if (step === 3) {
    canNext = Number(form.rating) >= 0.5;
  }
  canNext = canNext && !pending && editStatus === "ready";

  // 진행 중 연타로 중복 호출되지 않게 막는다.
  const runOnce = async (task) => {
    if (pendingRef.current) return;
    pendingRef.current = true;
    setPending(true);
    setError("");
    try {
      await task();
    } finally {
      pendingRef.current = false;
      setPending(false);
    }
  };

  const loadDetails = () =>
    runOnce(async () => {
      try {
        const res = await fetchPerformanceDetails(form.show.id, form.sessionId);
        const performance = res?.performance ?? {};
        setForm((prev) => ({
          ...prev,
          venue: performance.venue ?? "",
          runtime:
            performance.runningMinutes == null
              ? ""
              : String(performance.runningMinutes),
          castList: Array.isArray(res?.castings) ? res.castings : [],
        }));
        setStep(2);
      } catch {
        setError(DETAIL_ERROR);
      }
    });

  const submitTicket = () =>
    runOnce(async () => {
      try {
        const res = await createTicket(buildTicketBody(form));
        navigate("/record/new/complete", {
          replace: true,
          state: { ticketNumber: res?.ticketNumber },
        });
      } catch (err) {
        const serverMessage =
          err instanceof ApiError && err.status === 400 ? err.data?.message : "";
        setError(serverMessage || SUBMIT_ERROR);
      }
    });

  const saveTicket = () =>
    runOnce(async () => {
      try {
        await updateTicket(Number(id), buildTicketBody(form));
        navigate(`/record/${id}`, { replace: true });
      } catch (err) {
        setError(err instanceof ApiError ? err.message : UPDATE_ERROR);
      }
    });

  const handleNext = () => {
    if (!canNext) return;
    if (step === LAST_STEP) {
      if (isEdit) {
        saveTicket();
      } else {
        submitTicket();
      }
      return;
    }
    if (step === 1) {
      loadDetails();
      return;
    }
    setStep((prev) => prev + 1);
  };

  const handleStop = () => {
    navigate(isEdit ? `/record/${id}` : "/record");
  };

  // 수정할 티켓을 불러오지 못했으면 확인 없이 바로 상세로 돌아간다.
  const handleBack = () => {
    if (editStatus !== "ready") {
      handleStop();
      return;
    }
    setLeaveOpen(true);
  };

  return (
    <div className="register">
      <div className="register__top">
        <span
          className={`register__line register__line--first${step > 1 ? " register__line--on" : ""}`}
        />
        <span
          className={`register__line register__line--second${step > 2 ? " register__line--on" : ""}`}
        />
        {STEPS.map((label, i) => (
          <span
            key={label}
            className={`register__pill register__pill--${i + 1}${step >= i + 1 ? " register__pill--active" : ""}`}
          >
            {label}
          </span>
        ))}
        <header className="register__header">
          <button
            type="button"
            className="register__back"
            onClick={handleBack}
          >
            <img src={iconBack} alt="뒤로가기" />
          </button>
          <h1 className="register__title">
            {isEdit ? "티켓 수정" : "티켓 등록"}
          </h1>
        </header>
        <span className="register__corner register__corner--left" />
        <span className="register__corner register__corner--right" />
        <span className="register__divider" />
      </div>

      <div className="register__body">
        {editStatus === "loading" && (
          <p className="register__status">불러오는 중</p>
        )}
        {editStatus === "error" && (
          <p className="register__status">불러오지 못했어요</p>
        )}
        {editStatus === "ready" && (
          <>
            {step === 1 && <StepSelect form={form} onChange={handleChange} />}
            {step === 2 && <StepDetail form={form} onChange={handleChange} />}
            {step === 3 && <StepReview form={form} onChange={handleChange} />}
          </>
        )}
      </div>

      {error && <p className="register__error register__error--footer">{error}</p>}

      <div
        className={`register__footer${step === LAST_STEP ? " register__footer--submit" : ""}`}
      >
        {step > firstStep && (
          <button type="button" className="register__prev" onClick={handlePrev}>
            이전
          </button>
        )}
        <button
          type="button"
          className={`register__next${canNext ? " register__next--enabled" : ""}`}
          onClick={handleNext}
          disabled={!canNext}
        >
          {step === LAST_STEP ? "등록하기" : "다음"}
        </button>
      </div>

      {leaveOpen && (
        <ConfirmModal
          title={
            isEdit
              ? "티켓 수정을 중단하시겠습니까?"
              : "티켓 등록을 중단하시겠습니까?"
          }
          description={isEdit ? "수정사항은 반영되지 않습니다" : undefined}
          keepLabel={isEdit ? "이어서 수정하기" : "이어서 생성하기"}
          stopLabel={isEdit ? "티켓 수정 중단하기" : "티켓 등록 중단하기"}
          onKeep={() => setLeaveOpen(false)}
          onStop={handleStop}
        />
      )}
    </div>
  );
};

export default TicketRegister;
