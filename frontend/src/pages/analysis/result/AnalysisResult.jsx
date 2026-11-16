import { useNavigate } from "react-router-dom";
import { CATEGORIES, VS_GROUPS } from "../analysisData.js";
import iconEdit from "../../../assets/analysis/icon-edit.svg";
import iconBook from "../../../assets/analysis/icon-cat-book.svg";
import iconVideo from "../../../assets/analysis/icon-cat-video.svg";
import iconComic from "../../../assets/analysis/icon-cat-comic.svg";
import iconMusic from "../../../assets/analysis/icon-cat-music.svg";
import iconTheater from "../../../assets/analysis/icon-cat-theater.svg";
import "./AnalysisResult.scss";

const HOLES = Array.from({ length: 9 }, (_, i) => i);
const DEFAULT_SUMMARY = "취향을 분석했어요";

const CATEGORY_ICONS = {
  book: iconBook,
  video: iconVideo,
  comic: iconComic,
  music: iconMusic,
  theater: iconTheater,
};

const isPlainObject = (value) =>
  Boolean(value) && typeof value === "object" && !Array.isArray(value);

const toStringList = (value) =>
  Array.isArray(value) ? value.filter((v) => typeof v === "string") : [];

const BulletList = ({ items }) => (
  <ul className="result__bullets">
    {items.map((item, i) => (
      <li key={`${item}-${i}`} className="result__bullet">
        {item}
      </li>
    ))}
  </ul>
);

const SectionHead = ({ title }) => (
  <div className="result__section-head">
    <h2 className="result__section-title">{title}</h2>
    <span className="result__section-line" />
  </div>
);

const AnalysisResult = ({ taste }) => {
  const navigate = useNavigate();

  const summary =
    typeof taste.summary === "string" && taste.summary.trim()
      ? taste.summary
      : DEFAULT_SUMMARY;
  const genres = toStringList(taste.genres);
  const moods = toStringList(taste.moods);
  const favorites = Array.isArray(taste.favorites)
    ? taste.favorites.filter(isPlainObject)
    : [];
  const vs = isPlainObject(taste.vs) ? taste.vs : {};
  const freeText = typeof taste.freeText === "string" ? taste.freeText : "";

  const vsBoxes = VS_GROUPS.map((group) => ({
    id: group.id,
    items: group.pairs
      .map((_, i) => vs[`${group.id}-${i}`])
      .filter((text) => typeof text === "string" && text),
  })).filter((box) => box.items.length > 0);

  return (
    <div className="result">
      <div className="result__scroll">
        <section className="result__section result__section--taste">
          <SectionHead title="장르 & 분위기" />
          <div className="result__box result__box--split">
            <div className="result__col">
              <BulletList items={genres} />
            </div>
            <div className="result__col">
              <BulletList items={moods} />
            </div>
          </div>
        </section>

        <section className="result__section result__section--favorites">
          <SectionHead title="인생작" />
          <ul className="result__favorites">
            {favorites.map((favorite, i) => {
              const category = CATEGORIES.find(
                (c) => c.key === favorite.category,
              );
              return (
                <li key={`${favorite.title}-${i}`} className="result__favorite">
                  {category && (
                    <span className="result__badge">
                      <span className="result__badge-icon">
                        <img src={CATEGORY_ICONS[category.key]} alt="" />
                      </span>
                      <span className="result__badge-label">
                        {category.label}
                      </span>
                    </span>
                  )}
                  <span className="result__favorite-title">
                    {typeof favorite.title === "string" ? favorite.title : ""}
                  </span>
                </li>
              );
            })}
          </ul>
        </section>

        <section className="result__section result__section--vs">
          <SectionHead title="캐릭터 성향" />
          <div className="result__stack">
            {vsBoxes.map((box) => (
              <div key={box.id} className="result__box">
                <BulletList items={box.items} />
              </div>
            ))}
          </div>
        </section>

        <section className="result__section result__section--free">
          <SectionHead title="자유 서술" />
          <p className="result__free">{freeText}</p>
        </section>
      </div>

      <header className="result__header">
        <h1 className="result__title">나의 취향 레포트</h1>
        <button
          type="button"
          className="result__edit"
          aria-label="취향 분석 수정"
          onClick={() => navigate("/analysis/survey")}
        >
          <img src={iconEdit} alt="" />
        </button>
      </header>
      <div className="result__band">
        <p className="result__summary">{summary}</p>
      </div>
      <div className="result__holes result__holes--top">
        {HOLES.map((i) => (
          <span key={i} className="result__hole" />
        ))}
      </div>
      <div className="result__holes result__holes--bottom">
        {HOLES.map((i) => (
          <span key={i} className="result__hole" />
        ))}
      </div>
    </div>
  );
};

export default AnalysisResult;
