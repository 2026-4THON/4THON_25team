import { VS_GROUPS } from "../analysisData.js";
import "./StepVs.scss";

const StepVs = ({ vs, onPick }) => {
  return (
    <div className="step-vs">
      {VS_GROUPS.map((group) => (
        <section key={group.id} className="step-vs__group">
          <h2 className="step-vs__subtitle">{group.title}</h2>
          <div className="step-vs__pairs">
            {group.pairs.map(([left, right], i) => {
              const key = `${group.id}-${i}`;
              const renderOption = (text) => {
                const selected = vs[key] === text;
                return (
                  <button
                    type="button"
                    className={`step-vs__option${selected ? " step-vs__option--selected" : ""}`}
                    aria-pressed={selected}
                    onClick={() => onPick(key, text)}
                  >
                    {text}
                  </button>
                );
              };
              return (
                <div key={key} className="step-vs__pair">
                  {renderOption(left)}
                  <span className="step-vs__vs">VS</span>
                  {renderOption(right)}
                </div>
              );
            })}
          </div>
        </section>
      ))}
    </div>
  );
};

export default StepVs;
