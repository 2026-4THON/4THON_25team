import { useState } from "react";
import iconSearch from "../../../assets/register/icon-search.svg";
import iconChevronLeft from "../../../assets/register/icon-chevron-left.svg";
import iconChevronRight from "../../../assets/register/icon-chevron-right.svg";
import {
  SHOWS,
  SHOW_PERIOD,
  SHOW_TIMES,
  INITIAL_MONTH,
} from "./registerMock.js";
import "./StepSelect.scss";

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

const toDateKey = (year, month, day) =>
  `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;

const getDayStatus = (dateKey) => {
  if (
    dateKey >= SHOW_PERIOD.start &&
    dateKey <= SHOW_PERIOD.end &&
    !SHOW_PERIOD.closedDates.includes(dateKey)
  ) {
    return "show";
  }
  return "none";
};

const StepSelect = ({ form, onChange }) => {
  const [query, setQuery] = useState("");
  const [view, setView] = useState(() =>
    form.date
      ? {
          year: Number(form.date.slice(0, 4)),
          month: Number(form.date.slice(5, 7)),
        }
      : INITIAL_MONTH,
  );

  const keyword = query.trim();
  const results = keyword
    ? SHOWS.filter((show) => show.title.includes(keyword))
    : [];
  const hasShow = Boolean(form.show);

  const handleSelectShow = (show) => {
    onChange({ show, date: "", time: "" });
    setQuery("");
  };

  const handleResetShow = () => {
    onChange({ show: null, date: "", time: "" });
  };

  const moveMonth = (delta) => {
    setView(({ year, month }) => {
      const next = new Date(year, month - 1 + delta, 1);
      return { year: next.getFullYear(), month: next.getMonth() + 1 };
    });
  };

  const firstWeekday = new Date(view.year, view.month - 1, 1).getDay();
  const daysInMonth = new Date(view.year, view.month, 0).getDate();
  const cells = [
    ...Array(firstWeekday).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];

  return (
    <div className="step-select">
      <section className="step-select__section">
        <p className="step-select__label">관람 작품을 입력해주세요</p>
        <div className="step-select__search">
          <div className="step-select__search-row">
            {hasShow ? (
              <button
                type="button"
                className="step-select__selected"
                onClick={handleResetShow}
              >
                <span className="step-select__genre">{form.show.genre}</span>
                <span className="step-select__show-title">
                  {form.show.title}
                </span>
              </button>
            ) : (
              <input
                className="step-select__input"
                type="text"
                value={query}
                placeholder="공연을 검색해주세요"
                onChange={(e) => setQuery(e.target.value)}
              />
            )}
            <img
              className="step-select__search-icon"
              src={iconSearch}
              alt=""
            />
          </div>
          {results.length > 0 && (
            <ul className="step-select__results">
              {results.map((show) => (
                <li key={show.id}>
                  <button
                    type="button"
                    className="step-select__result"
                    onClick={() => handleSelectShow(show)}
                  >
                    <span className="step-select__genre">{show.genre}</span>
                    <span className="step-select__show-title">
                      {show.title}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>

      {hasShow && (
        <section className="step-select__section">
          <p className="step-select__label">관람 일자를 선택해주세요</p>
          <div className="step-select__calendar">
            <div className="step-select__calendar-head">
              <button
                type="button"
                className="step-select__arrow"
                onClick={() => moveMonth(-1)}
              >
                <img src={iconChevronLeft} alt="이전 달" />
              </button>
              <span className="step-select__month">
                <span>{view.year}년</span>
                <span>{view.month}월</span>
              </span>
              <button
                type="button"
                className="step-select__arrow"
                onClick={() => moveMonth(1)}
              >
                <img src={iconChevronRight} alt="다음 달" />
              </button>
            </div>
            <div className="step-select__weekdays">
              {WEEKDAYS.map((day) => (
                <span key={day}>{day}</span>
              ))}
            </div>
            <div className="step-select__days">
              {cells.map((day, i) => {
                if (!day) {
                  return <span key={`empty-${i}`} />;
                }
                const dateKey = toDateKey(view.year, view.month, day);
                const status = getDayStatus(dateKey);
                const selected = form.date === dateKey;
                return (
                  <button
                    key={dateKey}
                    type="button"
                    className={`step-select__day step-select__day--${status}${selected ? " step-select__day--selected" : ""}`}
                    onClick={() => onChange({ date: dateKey, time: "" })}
                  >
                    <span>{day}</span>
                  </button>
                );
              })}
            </div>
          </div>
        </section>
      )}

      {hasShow && form.date && (
        <section className="step-select__section">
          <p className="step-select__label">관람 시간을 선택해주세요</p>
          <div className="step-select__times">
            {SHOW_TIMES.map((time) => (
              <button
                key={time}
                type="button"
                className={`step-select__time${form.time === time ? " step-select__time--selected" : ""}`}
                onClick={() => onChange({ time })}
              >
                {time.replace(":", " : ")}
              </button>
            ))}
          </div>
        </section>
      )}
    </div>
  );
};

export default StepSelect;
