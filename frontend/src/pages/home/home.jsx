import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import "./home.scss";

import searchIcon from "../../assets/home/search.svg";
import posterSample from "../../assets/home/poster.png";
import { fetchHomeRecommendations, fetchPopular } from "../../api/recommend.js";

const GENRES = ["전체", "뮤지컬", "연극", "기타"];

// 화면 라벨 <-> 서버 값 변환
const GENRE_PARAM = { 전체: "ALL", 뮤지컬: "MUSICAL", 연극: "PLAY", 기타: "ETC" };
const GENRE_LABEL = { MUSICAL: "뮤지컬", PLAY: "연극", ETC: "기타" };
const SCOPE_PARAM = { 진행중: "ONGOING", 통합: "ALL" }; // 토글 -> 추천 API scope
const STATUS_LABEL = { ONGOING: "진행중", UPCOMING: "예정", ENDED: "완료" };
const STATUS_CLASS = { 진행중: "live", 예정: "soon", 완료: "done" };

// 서버 응답 -> 화면에서 쓰는 모양으로 변환
const toCard = (it) => ({
  id: it.performanceId,
  title: it.title,
  genre: GENRE_LABEL[it.genre] ?? "기타",
  status: STATUS_LABEL[it.status] ?? "완료",
  poster: it.posterUrl || posterSample,
  reason: it.reason ?? "",
});

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
  const [recGenre, setRecGenre] = useState("전체");
  const [recMode, setRecMode] = useState("통합");
  const [popGenre, setPopGenre] = useState("전체");
  const [popMode, setPopMode] = useState("진행중");

  // key: 어떤 조건으로 받은 결과인지 (현재 조건과 다르면 로딩 중으로 판단)
  const [rec, setRec] = useState({ key: null, data: null, error: false });
  const [pop, setPop] = useState({ key: null, data: null, error: false });
  const navigate = useNavigate();

  // 추천: 장르/토글이 바뀔 때마다 서버에서 다시 조회
  useEffect(() => {
    let cancelled = false;
    const key = `${recGenre}|${recMode}`;
    fetchHomeRecommendations(GENRE_PARAM[recGenre], SCOPE_PARAM[recMode])
      .then((data) => !cancelled && setRec({ key, data, error: false }))
      .catch(() => !cancelled && setRec({ key, data: null, error: true }));
    return () => {
      cancelled = true;
    };
  }, [recGenre, recMode]);

  // 인기: 장르가 바뀔 때마다 조회 (진행중/통합은 아래에서 상태로 걸러냄)
  useEffect(() => {
    let cancelled = false;
    const key = popGenre;
    fetchPopular(GENRE_PARAM[popGenre])
      .then((data) => !cancelled && setPop({ key, data, error: false }))
      .catch(() => !cancelled && setPop({ key, data: null, error: true }));
    return () => {
      cancelled = true;
    };
  }, [popGenre]);

  const recLoading = rec.key !== `${recGenre}|${recMode}`;
  const popLoading = pop.key !== popGenre;

  const recommended = (rec.data?.items ?? []).map(toCard).slice(0, 5); // 1~5순위까지만
  const popular = (pop.data ?? [])
    .map(toCard)
    .filter((it) => popMode === "통합" || it.status === "진행중");

  // 검색 API 연동
  const handleSearch = (e) => {
    e.preventDefault();
  };

  return (
    <div className="home">
      <form className="home__search" onClick={() => navigate("/search")}>
        <input
          className="home__search-input"
          type="text"
          placeholder="공연을 검색해보세요"
          readOnly
        />
        <button type="button" className="home__search-btn" aria-label="검색">
          <img src={searchIcon} alt="" />
        </button>
      </form>

      <section className="home__section home__section--rec">
        <h2 className="home__title">취향 분석을 바탕으로 추천하는 공연</h2>
        <p className="home__desc">
          {rec.data?.hasReport === false
            ? "취향 분석을 하면 맞춤 공연을 추천해 드려요"
            : rec.data?.briefing ?? ""}
        </p>

        <div className="home__filter">
          <GenreChips value={recGenre} onChange={setRecGenre} />
          <ModeToggle value={recMode} onChange={setRecMode} />
        </div>

        <div className="home__rec-list">
          {recLoading && <p className="home__desc">불러오는 중...</p>}
          {!recLoading && rec.error && (
            <p className="home__desc">추천 공연을 불러오지 못했어요</p>
          )}
          {!recLoading && !rec.error && recommended.length === 0 && (
            <p className="home__desc">추천할 공연이 아직 없어요</p>
          )}
          {!recLoading &&
            recommended.map((item, i) => (
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
          {popLoading && <p className="home__desc">불러오는 중...</p>}
          {!popLoading && pop.error && (
            <p className="home__desc">인기 공연을 불러오지 못했어요</p>
          )}
          {!popLoading && !pop.error && popular.length === 0 && (
            <p className="home__desc">표시할 공연이 없어요</p>
          )}
          {!popLoading &&
            popular.map((item, i) => (
              <PosterCard key={item.id} item={item} rank={i + 1} small />
            ))}
        </div>
      </section>
    </div>
  );
}

export default Home;