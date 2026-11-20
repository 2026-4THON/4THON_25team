import { useEffect, useState } from "react";
import iconSearch from "../../../assets/register/icon-search.svg";
import iconChevronLeft from "../../../assets/register/icon-chevron-left.svg";
import iconChevronRight from "../../../assets/register/icon-chevron-right.svg";
import {
  searchPerformances,
  fetchPerformanceDates,
  fetchPerformanceSessions,
} from "../../../api/performance.js";
import MarqueeText from "../../../components/common/MarqueeText.jsx";
import { toShow, toTime } from "./registerApi.js";
import "./StepSelect.scss";

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const SEARCH_DELAY_MS = 300;
const LOAD_ERROR = "불러오지 못했어요";
const EMPTY_DATES = new Set();

const toDateKey = (year, month, day) =>
  `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;

const toMinutes = (time) => {
  const [hour, minute] = time.split(":").map(Number);
  return (hour || 0) * 60 + (minute || 0);
};

const toMonth = (dateKey) => ({
  year: Number(dateKey.slice(0, 4)),
  month: Number(dateKey.slice(5, 7)),
});

const getTodayKey = () => {
  const today = new Date();
  return toDateKey(today.getFullYear(), today.getMonth() + 1, today.getDate());
};

// 월 비교용 숫자 (year*12 + month)
const toMonthIndex = ({ year, month }) => year * 12 + month;

// 날짜 목록 기준 첫 표시 월: 오늘 이하 날짜 중 가장 늦은 날짜 → 가장 이른 날짜 → 없으면 null
// ("YYYY-MM-DD" 문자열 비교만 사용, 시간대 변환 없음)
const getMonthFromDates = (dates) => {
  const sorted = [...dates].sort();
  if (sorted.length === 0) return null;
  const todayKey = getTodayKey();
  const past = sorted.filter((date) => date <= todayKey);
  return toMonth(past.length > 0 ? past[past.length - 1] : sorted[0]);
};

const EMPTY_DATE_STATE = { showId: null, dates: EMPTY_DATES, error: false };
const EMPTY_SESSION_STATE = { key: "", items: [], error: false };

const StepSelect = ({ form, onChange }) => {
  const [query, setQuery] = useState("");
  // 보이는 달. form.date 가 있으면 그 달로 시작하고, 없으면 날짜 목록 도착 시 계산한다.
  const [view, setView] = useState(() =>
    form.date ? toMonth(form.date) : null,
  );
  const [search, setSearch] = useState({ keyword: "", items: [], error: false });
  const [dateState, setDateState] = useState(EMPTY_DATE_STATE);
  const [sessionState, setSessionState] = useState(EMPTY_SESSION_STATE);

  const keyword = query.trim();
  const hasShow = Boolean(form.show);
  const showId = form.show?.id ?? null;
  const date = form.date;
  const sessionKey = showId !== null && date ? `${showId}|${date}` : "";

  // 검색: 300ms 디바운스, 최신 요청 결과만 반영
  useEffect(() => {
    if (!keyword) return undefined;
    let ignore = false;
    const timer = setTimeout(() => {
      searchPerformances(keyword)
        .then((list) => {
          if (!ignore) {
            setSearch({ keyword, items: (list ?? []).map(toShow), error: false });
          }
        })
        .catch(() => {
          if (!ignore) setSearch({ keyword, items: [], error: true });
        });
    }, SEARCH_DELAY_MS);
    return () => {
      ignore = true;
      clearTimeout(timer);
    };
  }, [keyword]);

  // 선택한 공연의 관람 가능 날짜. 도착하면 보이는 달을 다시 계산한다.
  // (form.date 가 있어 이미 정해진 달은 유지, 이전 공연의 늦은 응답은 ignore 로 무시)
  useEffect(() => {
    if (showId === null) return undefined;
    let ignore = false;
    fetchPerformanceDates(showId)
      .then((res) => {
        if (ignore) return;
        const dates = new Set(res?.dates ?? []);
        setDateState({ showId, dates, error: false });
        setView((prev) => prev ?? getMonthFromDates(dates));
      })
      .catch(() => {
        if (!ignore) setDateState({ showId, dates: EMPTY_DATES, error: true });
      });
    return () => {
      ignore = true;
    };
  }, [showId]);

  // 선택한 날짜의 회차
  useEffect(() => {
    if (!sessionKey) return undefined;
    let ignore = false;
    fetchPerformanceSessions(showId, date)
      .then((list) => {
        if (ignore) return;
        const items = (list ?? []).map((session) => ({
          id: session.id,
          time: toTime(session.startsAt),
        }));
        setSessionState({ key: sessionKey, items, error: false });
      })
      .catch(() => {
        if (!ignore) setSessionState({ key: sessionKey, items: [], error: true });
      });
    return () => {
      ignore = true;
    };
  }, [sessionKey, showId, date]);

  const searchDone = keyword !== "" && search.keyword === keyword;
  const results = searchDone ? search.items : [];
  const searchError = searchDone && search.error;

  const datesLoaded = showId !== null && dateState.showId === showId;
  const datesLoading = showId !== null && !datesLoaded;
  const availableDates = datesLoaded ? dateState.dates : EMPTY_DATES;
  const datesError = datesLoaded && dateState.error;

  const sessionsLoaded = sessionKey !== "" && sessionState.key === sessionKey;
  const sessionsLoading = sessionKey !== "" && !sessionsLoaded;
  const sessions = sessionsLoaded ? sessionState.items : [];
  const sessionsError = sessionsLoaded && sessionState.error;

  const hasDates = availableDates.size > 0;
  const sortedDates = [...availableDates].sort();
  const minMonth = hasDates ? toMonth(sortedDates[0]) : null;
  const maxMonth = hasDates ? toMonth(sortedDates[sortedDates.length - 1]) : null;
  const current = view ?? minMonth ?? toMonth(getTodayKey());
  const canPrev = hasDates && toMonthIndex(current) > toMonthIndex(minMonth);
  const canNext = hasDates && toMonthIndex(current) < toMonthIndex(maxMonth);

  // 공연을 바꾸면 날짜·회차 목록과 보이는 달도 비운다.
  const clearShowData = () => {
    setDateState(EMPTY_DATE_STATE);
    setSessionState(EMPTY_SESSION_STATE);
    setView(null);
  };

  const handleSelectShow = (show) => {
    onChange({ show, date: "", time: "", sessionId: null });
    setQuery("");
    clearShowData();
  };

  const handleResetShow = () => {
    onChange({ show: null, date: "", time: "", sessionId: null });
    clearShowData();
  };

  const moveMonth = (delta) => {
    if ((delta < 0 && !canPrev) || (delta > 0 && !canNext)) return;
    const next = new Date(current.year, current.month - 1 + delta, 1);
    setView({ year: next.getFullYear(), month: next.getMonth() + 1 });
  };

  const firstWeekday = new Date(current.year, current.month - 1, 1).getDay();
  const daysInMonth = new Date(current.year, current.month, 0).getDate();
  const cells = [
    ...Array(firstWeekday).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];

  // 수정 화면에서 불러온 시간이 회차에 없어도 선택된 채로 보이게 한다.
  const times =
    form.time && !sessions.some((session) => session.time === form.time)
      ? [...sessions, { id: form.sessionId ?? null, time: form.time }].sort(
          (a, b) => toMinutes(a.time) - toMinutes(b.time),
        )
      : sessions;

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
                <MarqueeText
                  text={form.show.title}
                  className="step-select__show-title"
                />
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
          {!hasShow && results.length > 0 && (
            <ul className="step-select__results">
              {results.map((show) => (
                <li key={show.id}>
                  <button
                    type="button"
                    className="step-select__result"
                    onClick={() => handleSelectShow(show)}
                  >
                    <span className="step-select__genre">{show.genre}</span>
                    <MarqueeText
                      text={show.title}
                      className="step-select__show-title"
                    />
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
        {!hasShow && searchError && (
          <p className="register__error">{LOAD_ERROR}</p>
        )}
      </section>

      {hasShow && (
        <section className="step-select__section">
          <p className="step-select__label">관람 일자를 선택해주세요</p>
          {datesLoading && <p className="step-select__notice">불러오는 중</p>}
          {datesError && <p className="register__error">{LOAD_ERROR}</p>}
          {datesLoaded && !datesError && !hasDates && (
            <p className="step-select__notice">예약 가능한 날짜가 없어요</p>
          )}
          {datesLoaded && hasDates && (
            <div className="step-select__calendar">
              <div className="step-select__calendar-head">
                <button
                  type="button"
                  className={`step-select__arrow${canPrev ? "" : " step-select__arrow--disabled"}`}
                  disabled={!canPrev}
                  onClick={() => moveMonth(-1)}
                >
                  <img src={iconChevronLeft} alt="이전 달" />
                </button>
                <span className="step-select__month">
                  <span>{current.year}년</span>
                  <span>{current.month}월</span>
                </span>
                <button
                  type="button"
                  className={`step-select__arrow${canNext ? "" : " step-select__arrow--disabled"}`}
                  disabled={!canNext}
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
                  const dateKey = toDateKey(current.year, current.month, day);
                  const hasSession = availableDates.has(dateKey);
                  const status = hasSession ? "show" : "none";
                  const selected = date === dateKey;
                  return (
                    <button
                      key={dateKey}
                      type="button"
                      className={`step-select__day step-select__day--${status}${selected ? " step-select__day--selected" : ""}`}
                      disabled={!hasSession}
                      onClick={() => {
                        if (selected) return;
                        onChange({ date: dateKey, time: "", sessionId: null });
                      }}
                    >
                      <span>{day}</span>
                    </button>
                  );
                })}
              </div>
            </div>
          )}
        </section>
      )}

      {hasShow && date && (
        <section className="step-select__section">
          <p className="step-select__label">관람 시간을 선택해주세요</p>
          <div className="step-select__times">
            {times.map((session) => (
              <button
                key={session.time}
                type="button"
                className={`step-select__time${form.time === session.time ? " step-select__time--selected" : ""}`}
                disabled={sessionsLoading}
                onClick={() =>
                  onChange({ time: session.time, sessionId: session.id })
                }
              >
                {session.time.replace(":", " : ")}
              </button>
            ))}
          </div>
          {sessionsError && <p className="register__error">{LOAD_ERROR}</p>}
        </section>
      )}
    </div>
  );
};

export default StepSelect;
