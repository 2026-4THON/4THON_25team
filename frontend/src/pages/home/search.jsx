import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import "./search.scss";
import { searchPerformances } from "../../api/search.js";

const GENRE_LABEL = { MUSICAL: "뮤지컬", PLAY: "연극", ETC: "기타" };

import closeIcon from "../../assets/home/close.svg"; 
import backIcon from "../../assets/home/back.svg";
import searchIcon from "../../assets/home/search.svg";


const STORAGE_KEY = "daengrog:recent-searches";
const MAX_RECENT = 10;

const loadRecent = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
};

function Search() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const [query, setQuery] = useState(state?.q ?? "");
  const [results, setResults] = useState([]);
  const [recent, setRecent] = useState(loadRecent);

  const keyword = query.trim();

  // 입력 중이면 검색 결과(0.3초 디바운스), 비어 있으면 최근 검색
  useEffect(() => {
    if (!keyword) {
      setResults([]);
      return;
    }
    let alive = true;
    const t = setTimeout(() => {
      searchPerformances(keyword)
        .then((list) =>
          alive &&
          setResults((list ?? []).map((p) => ({ id: p.id, title: p.title, genre: GENRE_LABEL[p.genre] ?? "기타" })))
        )
        .catch(() => alive && setResults([]));
    }, 300);
    return () => {
      alive = false;
      clearTimeout(t);
    };
  }, [keyword]);

  const saveRecent = (list) => {
    setRecent(list);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
    } catch {
      // 저장이 막혀 있어도 화면은 그대로 동작해요
    }
  };

  const addRecent = (work) =>
    saveRecent([work, ...recent.filter((r) => r.id !== work.id)].slice(0, MAX_RECENT));

  const removeRecent = (id) => saveRecent(recent.filter((r) => r.id !== id));

  // 검색 결과를 누르면 최근 검색에 쌓고 작품 상세로 이동
  const openWork = (work) => {
    addRecent(work);
    navigate(`/detail/${work.id}`);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (results[0]) addRecent(results[0]);
  };

  return (
    <div className="search">
      <button type="button" className="search__back" onClick={() => navigate(-1)} aria-label="뒤로가기">
        <img src={backIcon} alt="" />
      </button>

      <form className="search__bar" onSubmit={handleSubmit}>
        <input
          className="search__input"
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="검색어를 입력하세요"
          autoFocus
        />
        <button type="submit" className="search__submit" aria-label="검색">
          <img src={searchIcon} alt="" />
        </button>
      </form>

      {keyword ? (
        // ---------- 입력 중: 자동완성 결과 ----------
        <ul className="search__list search__list--results">
          {results.map((w) => (
            <li className="search__item" key={w.id}>
              <button type="button" className="search__row" onClick={() => openWork(w)}>
                <span className="search__lead">
                  <span className="search__chip">{w.genre}</span>
                </span>
                <span className="search__title">{w.title}</span>
              </button>
            </li>
          ))}
          {results.length === 0 && <li className="search__empty">검색 결과가 없어요</li>}
        </ul>
      ) : (
        // ---------- 기본: 최근 검색 ----------
        <>
          <h2 className="search__heading">최근 검색</h2>
          <ul className="search__list">
            {recent.map((w) => (
              <li className="search__item" key={w.id}>
                <button type="button" className="search__row" onClick={() => setQuery(w.title)}>
                  <span className="search__lead">
                    <span className="search__chip">{w.genre}</span>
                  </span>
                  <span className="search__title">{w.title}</span>
                </button>
                <button
                  type="button"
                  className="search__delete"
                  onClick={() => removeRecent(w.id)}
                  aria-label={`${w.title} 최근 검색 삭제`}
                >
                  <img src={closeIcon} alt="" />
                </button>
              </li>
            ))}
            {recent.length === 0 && <li className="search__empty">최근 검색 내역이 없어요</li>}
          </ul>
        </>
      )}
    </div>
  );
}

export default Search;