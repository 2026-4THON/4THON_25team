import iconWarning from "../../assets/record/icon-warning.svg";
import "./ConfirmModal.scss";

const ConfirmModal = ({
  title,
  description,
  keepLabel,
  stopLabel,
  onKeep,
  onStop,
}) => (
  <div className="confirm-modal" role="dialog" aria-modal="true">
    <div className="confirm-modal__box">
      <div className="confirm-modal__content">
        <img className="confirm-modal__icon" src={iconWarning} alt="" />
        <div className="confirm-modal__texts">
          <p className="confirm-modal__title">{title}</p>
          {description && (
            <p className="confirm-modal__desc">{description}</p>
          )}
        </div>
        <div className="confirm-modal__buttons">
          <button
            type="button"
            className="confirm-modal__button confirm-modal__button--keep"
            onClick={onKeep}
          >
            {keepLabel}
          </button>
          <button
            type="button"
            className="confirm-modal__button confirm-modal__button--stop"
            onClick={onStop}
          >
            {stopLabel}
          </button>
        </div>
      </div>
    </div>
  </div>
);

export default ConfirmModal;
