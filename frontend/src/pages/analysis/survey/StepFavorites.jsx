import { useEffect, useRef, useState } from "react";
import { CATEGORIES } from "../analysisData.js";
import iconBook from "../../../assets/analysis/icon-cat-book.svg";
import iconVideo from "../../../assets/analysis/icon-cat-video.svg";
import iconComic from "../../../assets/analysis/icon-cat-comic.svg";
import iconMusic from "../../../assets/analysis/icon-cat-music.svg";
import iconTheater from "../../../assets/analysis/icon-cat-theater.svg";
import "./StepFavorites.scss";

const CATEGORY_ICONS = {
  book: iconBook,
  video: iconVideo,
  comic: iconComic,
  music: iconMusic,
  theater: iconTheater,
};

const Triangle = ({ up = false }) => (
  <svg width="11" height="9" viewBox="0 0 11 9" fill="none" aria-hidden="true">
    <path d={up ? "M0 9H11L5.5 0Z" : "M0 0H11L5.5 9Z"} fill="#fff" />
  </svg>
);

const StepFavorites = ({ favorites, onChange, createId }) => {
  const [openId, setOpenId] = useState(null);
  const panelRef = useRef(null);

  useEffect(() => {
    if (openId === null) return undefined;
    const handleMouseDown = (e) => {
      if (panelRef.current && !panelRef.current.contains(e.target)) {
        setOpenId(null);
      }
    };
    document.addEventListener("mousedown", handleMouseDown);
    return () => document.removeEventListener("mousedown", handleMouseDown);
  }, [openId]);

  const updateItem = (id, patch) => {
    onChange(
      favorites.map((item) => (item.id === id ? { ...item, ...patch } : item)),
    );
  };

  const handleAdd = () => {
    onChange([...favorites, { id: createId(), category: "", title: "" }]);
  };

  const handleRemove = (id) => {
    if (openId === id) setOpenId(null);
    onChange(favorites.filter((item) => item.id !== id));
  };

  const handlePick = (id, category) => {
    updateItem(id, { category });
    setOpenId(null);
  };

  return (
    <div className="step-favorites">
      <h2 className="step-favorites__subtitle">
        2. 기존에 좋아했던 나의 인생작을 등록해주세요
      </h2>
      <div className="step-favorites__list">
        {favorites.map((item) => {
          const category = CATEGORIES.find((c) => c.key === item.category);
          const isOpen = openId === item.id;
          return (
            <div
              key={item.id}
              className={`step-favorites__row${isOpen ? " step-favorites__row--open" : ""}`}
            >
              <button
                type="button"
                className={`step-favorites__trigger${category ? " step-favorites__trigger--selected" : ""}`}
                aria-haspopup="listbox"
                aria-expanded={isOpen}
                onClick={() => setOpenId(item.id)}
              >
                {category ? (
                  <>
                    <span className="step-favorites__icon">
                      <img src={CATEGORY_ICONS[category.key]} alt="" />
                    </span>
                    <span className="step-favorites__category-label">
                      {category.label}
                    </span>
                  </>
                ) : (
                  <>
                    <span className="step-favorites__placeholder">카테고리</span>
                    <Triangle />
                  </>
                )}
              </button>
              <input
                className="step-favorites__input"
                type="text"
                value={item.title}
                placeholder="제목을 입력해주세요"
                onChange={(e) => updateItem(item.id, { title: e.target.value })}
              />
              <button
                type="button"
                className="step-favorites__remove"
                aria-label="삭제"
                onClick={() => handleRemove(item.id)}
              >
                <svg width="6" height="6" viewBox="0 0 6 6" fill="none" aria-hidden="true">
                  <path
                    d="M0.75 0.75L5.25 5.25M5.25 0.75L0.75 5.25"
                    stroke="#aeaeae"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                  />
                </svg>
              </button>
              {isOpen && (
                <div className="step-favorites__panel" ref={panelRef}>
                  <button
                    type="button"
                    className="step-favorites__panel-head"
                    onClick={() => setOpenId(null)}
                  >
                    <span className="step-favorites__placeholder">카테고리</span>
                    <Triangle up />
                  </button>
                  <ul className="step-favorites__options" role="listbox">
                    {CATEGORIES.map((option) => (
                      <li key={option.key}>
                        <button
                          type="button"
                          role="option"
                          aria-selected={item.category === option.key}
                          className="step-favorites__option"
                          onClick={() => handlePick(item.id, option.key)}
                        >
                          <span className="step-favorites__icon">
                            <img src={CATEGORY_ICONS[option.key]} alt="" />
                          </span>
                          <span className="step-favorites__category-label">
                            {option.label}
                          </span>
                        </button>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          );
        })}
        <button
          type="button"
          className="step-favorites__add"
          aria-label="인생작 추가"
          onClick={handleAdd}
        >
          +
        </button>
      </div>
    </div>
  );
};

export default StepFavorites;
