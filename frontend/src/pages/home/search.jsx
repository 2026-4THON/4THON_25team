import { useState } from "react";
import { useNavigate } from "react-router-dom";

import "./search.scss";

import closeIcon from "../../assets/home/close.svg"; 
import backIcon from "../../assets/home/back.svg";
import searchIcon from "../../assets/home/search.svg";

//  임시 작품 목록
const WORKS = [
  { id: 1, genre: "뮤지컬", title: "죽음에 관하여" },
  { id: 2, genre: "뮤지컬", title: "팬레터" },
  { id: 3, genre: "연극", title: "팬텀" },
  { id: 4, genre: "뮤지컬", title: "팬텀싱어" },
];

// 시안 기준
const SEED_RECENT = [WORKS[0], WORKS[1]];

const STORAGE_KEY = "daengrog:recent-searches";
const MAX_RECENT = 10;

const loadRecent = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : SEED_RECENT;
  } catch {
    return SEED_RECENT;
  }
};

function Search() {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [recent, setRecent] = useState(loadRecent);

  const keyword = query.trim();
  const results = keyword ? WORKS.filter((w) => w.title.includes(keyword)) : [];

  const saveRecent = (list) => {
    setRecent(list);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
    } catch {
    }
  };

  const addRecent = (work) =>
    saveRecent([work, ...recent.filter((r) => r.id !== work.id)].slice(0, MAX_RECENT));

  const removeRecent = (id) => saveRecent(recent.filter((r) => r.id !== id));

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
