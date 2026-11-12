import { useState } from "react";
import CastSheet from "./CastSheet.jsx";
import iconStarFilled from "../../../assets/register/icon-star-lg-filled.svg";
import iconStarEmpty from "../../../assets/register/icon-star-lg-empty.svg";
import "./StepReview.scss";

const STAR_COUNT = 5;

const StepReview = ({ form, onChange }) => {
  const [pairOpen, setPairOpen] = useState(false);

  const activeActor =
    form.reviewActor && form.casting.includes(form.reviewActor)
      ? form.reviewActor
      : form.casting[0] || "";

  const handleActorReview = (value) => {
    onChange({ actorReviews: { ...form.actorReviews, [activeActor]: value } });
  };

  const handlePairDone = (pair) => {
    onChange({ pair });
    setPairOpen(false);
  };

  return (
    <div className="step-review">
      <section className="step-review__field">
        <p className="step-review__label">공연에 대한 평점을 입력해주세요</p>
        <div className="step-review__stars">
          {Array.from({ length: STAR_COUNT }, (_, i) => (
            <button
              key={i}
              type="button"
              className="step-review__star"
              onClick={() => onChange({ rating: i + 1 })}
            >
              <img
                src={i < form.rating ? iconStarFilled : iconStarEmpty}
                alt={`${i + 1}점`}
              />
            </button>
          ))}
        </div>
      </section>

      <section className="step-review__field">
        <p className="step-review__label">공연에 대한 후기를 작성해주세요</p>
        <textarea
          className="step-review__textarea"
          value={form.review}
          onChange={(e) => onChange({ review: e.target.value })}
        />
      </section>

      <section className="step-review__field">
        <p className="step-review__label">배우에 대한 후기를 작성해주세요</p>
        <div className="step-review__chips">
          {form.casting.map((name) => (
            <button
              key={name}
              type="button"
              className={`step-review__chip step-review__chip--select${activeActor === name ? " step-review__chip--active" : ""}`}
              onClick={() => onChange({ reviewActor: name })}
            >
              {name}
            </button>
          ))}
        </div>
        <textarea
          className="step-review__textarea"
          value={form.actorReviews[activeActor] || ""}
          onChange={(e) => handleActorReview(e.target.value)}
        />
      </section>

      <section className="step-review__field">
        <p className="step-review__label">
          주연배우 페어에 대한 후기를 작성해주세요
        </p>
        <div className="step-review__chips">
          {form.pair.map((name) => (
            <span
              key={name}
              className="step-review__chip step-review__chip--pair"
            >
              {name}
            </span>
          ))}
          <button
            type="button"
            className="step-review__chip step-review__chip--add"
            onClick={() => setPairOpen(true)}
          >
            +
          </button>
        </div>
        <textarea
          className="step-review__textarea"
          value={form.pairReview}
          onChange={(e) => onChange({ pairReview: e.target.value })}
        />
      </section>

      {pairOpen && (
        <CastSheet
          selected={form.pair}
          onDone={handlePairDone}
          onClose={() => setPairOpen(false)}
        />
      )}
    </div>
  );
};

export default StepReview;
