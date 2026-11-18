import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import StepSelect from "./StepSelect.jsx";
import StepDetail from "./StepDetail.jsx";
import StepReview from "./StepReview.jsx";
import ConfirmModal from "../../../components/record/ConfirmModal.jsx";
import { getEditForm, saveRecord } from "../recordStore.js";
import iconBack from "../../../assets/record/icon-back.svg";
import "./TicketRegister.scss";

const STEPS = ["공연선택", "상세정보", "후기작성"];
const LAST_STEP = 3;

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
};

const TicketRegister = ({ mode = "create" }) => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = mode === "edit";
  const [step, setStep] = useState(1);
  const [form, setForm] = useState(() =>
    isEdit ? getEditForm(Number(id)) : INITIAL_FORM,
  );
  const [leaveOpen, setLeaveOpen] = useState(false);

  const handleChange = (patch) => {
    setForm((prev) => {
      const next = { ...prev, ...patch };
      if ("show" in patch) {
        next.venue = "";
        next.runtime = "";
      }
      return next;
    });
  };

  const handlePrev = () => {
    if (step <= 1) return;
    setStep((prev) => prev - 1);
  };

  let canNext = true;
  if (step === 1) {
    canNext = Boolean(form.show && form.date && form.time);
  }

  const handleNext = () => {
    if (!canNext) return;
    if (step === LAST_STEP) {
      if (isEdit) {
        saveRecord(form, Number(id));
        navigate(`/record/${id}`, { replace: true });
      } else {
        saveRecord(form);
        navigate("/record/new/complete", { replace: true });
      }
      return;
    }
    if (step === 1) {
      setForm((prev) => ({
        ...prev,
        venue: prev.venue || prev.show.venue,
        runtime: prev.runtime || String(prev.show.runtime),
      }));
    }
    setStep((prev) => prev + 1);
  };

  const handleStop = () => {
    navigate(isEdit ? `/record/${id}` : "/record");
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
            onClick={() => setLeaveOpen(true)}
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
        {step === 1 && <StepSelect form={form} onChange={handleChange} />}
        {step === 2 && <StepDetail form={form} onChange={handleChange} />}
        {step === 3 && <StepReview form={form} onChange={handleChange} />}
      </div>

      <div
        className={`register__footer${step === LAST_STEP ? " register__footer--submit" : ""}`}
      >
        {step > 1 && (
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
