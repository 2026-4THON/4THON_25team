import { useEffect } from "react";
import iconWarning from "../../../assets/analysis/icon-warning.svg";
import "./LeaveModal.scss";

const LeaveModal = ({ onKeep, onStop }) => {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape") onKeep();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [onKeep]);

  return (
    <div
      className="leave-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="leave-modal-title"
      onClick={onKeep}
    >
      <div className="leave-modal__card" onClick={(e) => e.stopPropagation()}>
        <div className="leave-modal__content">
          <img className="leave-modal__icon" src={iconWarning} alt="" />
          <p id="leave-modal-title" className="leave-modal__title">
            분석을 중단하시겠습니까?
          </p>
          <p className="leave-modal__desc">입력사항은 반영되지 않습니다</p>
          <div className="leave-modal__actions">
            <button
              type="button"
              className="leave-modal__btn leave-modal__btn--keep"
              onClick={onKeep}
            >
              이어서 수정하기
            </button>
            <button
              type="button"
              className="leave-modal__btn leave-modal__btn--stop"
              onClick={onStop}
            >
              취향 레포트 분석 중단하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LeaveModal;
