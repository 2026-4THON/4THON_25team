import { GENRE_ROWS, MOOD_ROWS } from "../analysisData.js";
import "./StepTaste.scss";

const StepTaste = ({ genres, moods, onToggleGenre, onToggleMood }) => {
  return (
    <div className="step-taste">
      <section className="step-taste__section">
        <h2 className="step-taste__subtitle">1-1. 어떤 장르를 좋아하시나요?</h2>
        <div className="step-taste__genres">
          {GENRE_ROWS.map((row) => (
            <div key={row.join("|")} className="step-taste__genre-row">
              {row.map((genre) => {
                const selected = genres.includes(genre);
                return (
                  <button
                    key={genre}
                    type="button"
                    className={`step-taste__chip${selected ? " step-taste__chip--selected" : ""}`}
                    aria-pressed={selected}
                    onClick={() => onToggleGenre(genre)}
                  >
                    {genre}
                  </button>
                );
              })}
            </div>
          ))}
        </div>
      </section>

      <section className="step-taste__section">
        <h2 className="step-taste__subtitle">
          1-2. 선호하는 분위기를 선택해주세요
        </h2>
        <div className="step-taste__moods">
          {MOOD_ROWS.map((row) => (
            <div key={row.join("|")} className="step-taste__mood-row">
              {row.map((mood) => {
                const selected = moods.includes(mood);
                return (
                  <button
                    key={mood}
                    type="button"
                    className={`step-taste__mood${selected ? " step-taste__mood--selected" : ""}`}
                    aria-pressed={selected}
                    onClick={() => onToggleMood(mood)}
                  >
                    {mood}
                  </button>
                );
              })}
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default StepTaste;
