import { useState } from "react";
import { useNavigate } from "react-router-dom";

import "./home.scss";

import searchIcon from "../../assets/home/search.svg";
import posterSample from "../../assets/home/poster.png";

// 임시 데이터
const ANALYSIS = "비극적인 서사와 깊은 감정선으로 여운을 남기는 작품을 선호해요";
const REASON = "추천해주는 이유추천해주는\n이유추천해주는 이유";

const RECOMMENDED = [
  { id: 1, title: "팬레터", genre: "뮤지컬", status: "진행중", poster: posterSample, reason: REASON },
  { id: 2, title: "팬레터", genre: "뮤지컬", status: "예정", poster: posterSample, reason: REASON },
  { id: 3, title: "팬레터", genre: "뮤지컬", status: "완료", poster: posterSample, reason: REASON },
  { id: 4, title: "팬레터", genre: "뮤지컬", status: "진행중", poster: posterSample, reason: REASON },
  { id: 5, title: "팬레터", genre: "뮤지컬", status: "진행중", poster: posterSample, reason: REASON },
];

const POPULAR = Array.from({ length: 9 }, (_, i) => ({
  id: i + 1,
  title: "팬레터",
  genre: "뮤지컬",
  status: "진행중",
  poster: posterSample,
}));

const GENRES = ["전체", "뮤지컬", "연극", "기타"];
const STATUS_CLASS = { 진행중: "live", 예정: "soon", 완료: "done" };


// 장르 + 토글(진행중 / 통합: 진행작+예정작+과거작) 필터
const filterItems = (items, genre, mode) =>
  items.filter(
    (it) =>
      (genre === "전체" || it.genre === genre) &&
      (mode === "통합" || it.status === "진행중")
  );

function GenreChips({ value, onChange }) {
  return (
    <div className="home__chips">
      {GENRES.map((g) => (
        <button
          key={g}
          type="button"
          className={value === g ? "home__chip home__chip--active" : "home__chip"}
          onClick={() => onChange(g)}
        >
          {g}
        </button>
      ))}
    </div>
  );
}

// 진행중 <-> 통합 토글
function ModeToggle({ value, onChange }) {
  const isAll = value === "통합";
  return (
    <button
      type="button"
      className={isAll ? "home__toggle home__toggle--all" : "home__toggle home__toggle--live"}
      onClick={() => onChange(isAll ? "진행중" : "통합")}
      aria-pressed={isAll}
    >
      <span className="home__knob">{value}</span>
    </button>
  );
}

function PosterCard({ item, rank, small = false, showReason = false }) {
  const navigate = useNavigate();

  return (
    <button
      type="button"
      className={small ? "poster poster--small" : "poster"}
      onClick={() => navigate(`/detail/${item.id}`)}
    >
      <div className="poster__box">
        <img className="poster__img" src={item.poster} alt={`${item.title} 포스터`} />
        <span className="poster__rank">{rank}</span>
        <span className={`poster__badge poster__badge--${STATUS_CLASS[item.status]}`}>
          {item.status}
        </span>
        <span className="poster__genre">{item.genre}</span>
        <p className="poster__title">{item.title}</p>
      </div>
      {showReason && <p className="poster__reason">{item.reason}</p>}
    </button>
  );
}

function Home() {
  const [query, setQuery] = useState("");
  const [recGenre, setRecGenre] = useState("전체");
  const [recMode, setRecMode] = useState("통합");
  const [popGenre, setPopGenre] = useState("전체");
  const [popMode, setPopMode] = useState("진행중");

  const recommended = filterItems(RECOMMENDED, recGenre, recMode).slice(0, 5); // 1~5순위까지만
  const popular = filterItems(POPULAR, popGenre, popMode);

  // 검색 API 연동
  const handleSearch = (e) => {
    e.preventDefault();
  };

  return (
    <div className="home">
      <form className="home__search" onSubmit={handleSearch}>
        <input
          className="home__search-input"
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="진행중인 공연 검색"
        />
        <button type="submit" className="home__search-btn" aria-label="검색">
          <img src={searchIcon} alt="" />
        </button>
      </form>

      <section className="home__section home__section--rec">
        <h2 className="home__title">취향 분석을 바탕으로 추천하는 공연</h2>
        <p className="home__desc">{ANALYSIS}</p>

        <div className="home__filter">
          <GenreChips value={recGenre} onChange={setRecGenre} />
          <ModeToggle value={recMode} onChange={setRecMode} />
        </div>

        <div className="home__rec-list">
          {recommended.map((item, i) => (
            <PosterCard key={item.id} item={item} rank={i + 1} showReason />
          ))}
        </div>
      </section>

      <section className="home__section home__section--pop">
        <h2 className="home__title">지금 가장 인기 있는 공연</h2>

        <div className="home__filter">
          <GenreChips value={popGenre} onChange={setPopGenre} />
          <ModeToggle value={popMode} onChange={setPopMode} />
        </div>

        <div className="home__grid">
          {popular.map((item, i) => (
            <PosterCard key={item.id} item={item} rank={i + 1} small />
          ))}
        </div>
      </section>
    </div>
  );
}

export default Home;
