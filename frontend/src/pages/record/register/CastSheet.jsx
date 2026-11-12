import { useState } from "react";
import { CAST_LIST } from "./registerMock.js";
import "./CastSheet.scss";

const CastSheet = ({ selected, onDone, onClose }) => {
  const [draft, setDraft] = useState(selected);

  const toggle = (name) => {
    setDraft((prev) =>
      prev.includes(name) ? prev.filter((item) => item !== name) : [...prev, name],
    );
  };

  const handleDone = () => {
    onDone(CAST_LIST.filter((name) => draft.includes(name)));
  };

  return (
    <div className="cast-sheet" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="cast-sheet__panel" onClick={(e) => e.stopPropagation()}>
        <div className="cast-sheet__content">
          <h2 className="cast-sheet__title">전체 배우진</h2>
          <span className="cast-sheet__divider" />
          <div className="cast-sheet__list">
            {CAST_LIST.map((name) => (
              <button
                key={name}
                type="button"
                className={`cast-sheet__chip${draft.includes(name) ? " cast-sheet__chip--selected" : ""}`}
                onClick={() => toggle(name)}
              >
                {name}
              </button>
            ))}
          </div>
        </div>
        <button type="button" className="cast-sheet__done" onClick={handleDone}>
          선택 완료
        </button>
      </div>
    </div>
  );
};

export default CastSheet;
