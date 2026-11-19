import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import "./detail.scss";
import PairTab from "./pairTab.jsx";
import ReviewTab from "./reviewTab.jsx";
import { fetchShowDetail, secureUrl } from "../../api/showdetail.js";

import backIcon from "../../assets/home/back.svg";
import starIcon from "../../assets/home/star.svg";
import linkIcon from "../../assets/home/link.svg";
import posterImg from "../../assets/home/poster.png";

const GENRE_LABEL = { MUSICAL: "뮤지컬", PLAY: "연극", ETC: "기타" };
const STATUS_LABEL = { ONGOING: "진행중", UPCOMING: "예정", ENDED: "완료" };

const TABS = [
  { key: "info", label: "작품정보" },
  { key: "pair", label: "페어추천" },
  { key: "review", label: "작품후기" },
];

const formatWon = (n) => `${Number(n).toLocaleString("ko-KR")}원`;
const formatDot = (iso) => (iso ? iso.replaceAll("-", ".") : "");

const toView = (d) => ({
  title: d.title,
  genre: GENRE_LABEL[d.genre] ?? d.kopisGenre ?? "",
  status: STATUS_LABEL[d.status] ?? "",
  rating: d.rating?.average != null ? Number(d.rating.average).toFixed(1) : "-",
  tags: (d.keywords ?? [])
    .map((k) => (typeof k === "string" ? k : k?.keyword ?? k?.name ?? ""))
    .filter(Boolean)
    .map((k) => (k.startsWith("#") ? k : `#${k}`)),
  synopsis: d.synopsis ?? "",
  period: d.startDate
    ? `${formatDot(d.startDate)} ~ ${d.openRun && !d.endDate ? "" : formatDot(d.endDate)}`.trim()
    : "",
  venue: d.venue?.name ?? "",
  age: d.ageText ?? "",
  runtime: d.runtimeText ?? (d.runningMinutes ? `${d.runningMinutes}분` : ""),
  prices: (d.prices ?? []).map((p) => ({ seat: p.grade, price: p.price })),
  tickets: (d.bookingLinks ?? []).map((b) => ({ name: b.name, url: b.url })),
  actors: (d.cast ?? []).map((a) => ({ id: a.actorId, name: a.name })),
  videos: (d.videoIds ?? []).slice(0, 2),
  poster: secureUrl(d.posterUrl) || posterImg,
});

function Detail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [tab, setTab] = useState("info");
  const [open, setOpen] = useState(false);
  const [state, setState] = useState({ loading: true, error: "", data: null });

  useEffect(() => {
    let alive = true;
    setState({ loading: true, error: "", data: null });
    setTab("info");
    fetchShowDetail(id)
      .then((d) => alive && setState({ loading: false, error: "", data: toView(d) }))
      .catch((e) => {
        console.error("[detail] 요청 실패", e);
        if (alive) setState({ loading: false, error: String(e.status ?? "error"), data: null });
      });
    return () => {
      alive = false;
    };
  }, [id]);

  const d = state.data;

  if (state.loading || !d) {
    return (
      <div className="detail">
        <button
          type="button"
          className="detail__back"
          onClick={() => navigate(-1)}
          aria-label="뒤로가기"
        >
          <img src={backIcon} alt="" />
        </button>
        <p className="detail__empty detail__empty--light">
          {state.loading ? "불러오는 중..." : `작품 정보를 불러오지 못했어요 (${state.error})`}
        </p>
      </div>
    );
  }

  const videoSlots = [0, 1].map((i) => d.videos[i] ?? "");

  return (
    <div className="detail">
      <header className="detail__hero">
        <img className="detail__poster" src={d.poster} alt={`${d.title} 포스터`} />
        <div className="detail__shade" />

        <button
          type="button"
          className="detail__back"
          onClick={() => navigate(-1)}
          aria-label="뒤로가기"
        >
          <img src={backIcon} alt="" />
        </button>

        <div className="detail__info">
          <h1 className="detail__title">{d.title}</h1>
          <div className="detail__meta">
            {d.genre && <span className="detail__pill">{d.genre}</span>}
            {d.status && <span className="detail__pill">{d.status}</span>}
            <span className="detail__pill">
              <img src={starIcon} alt="" />
              {d.rating}
            </span>
          </div>
        </div>
      </header>

      <nav className="detail__tabs" aria-label="작품 상세 메뉴">
        {TABS.map(({ key, label }) => (
          <button
            key={key}
            type="button"
            className={tab === key ? "detail__tab detail__tab--active" : "detail__tab"}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
      </nav>

      <div className="detail__body">
        {tab === "info" && (
          <>
            {d.tags.length > 0 && (
              <section className="detail__tags">
                {d.tags.map((t) => (
                  <span className="detail__tag" key={t}>
                    {t}
                  </span>
                ))}
              </section>
            )}

            <section className="detail__section">
              <h2 className="detail__label">시놉시스</h2>
              {d.synopsis ? (
                <>
                  <p className={open ? "detail__synopsis detail__synopsis--open" : "detail__synopsis"}>
                    {d.synopsis}
                  </p>
                  <button type="button" className="detail__more" onClick={() => setOpen((v) => !v)}>
                    {open ? "접기" : "더보기"}
                  </button>
                </>
              ) : (
                <p className="detail__synopsis">등록된 시놉시스가 없어요</p>
              )}
            </section>

            <section className="detail__section detail__grid">
              <div>
                <h2 className="detail__label">공연기간</h2>
                <p className="detail__value">{d.period}</p>
              </div>
              <div>
                <h2 className="detail__label">관람 연령</h2>
                <p className="detail__value">{d.age}</p>
              </div>
              <div>
                <h2 className="detail__label">공연장</h2>
                <p className="detail__value">
                  {d.venue.replace(/\)\s*\(/g, ")\n(").split("\n").map((part, i) => (
                    <span key={i} className="detail__venue-part">
                      {part}{" "}
                    </span>
                  ))}
                </p>
              </div>
              <div>
                <h2 className="detail__label">러닝타임</h2>
                <p className="detail__value">{d.runtime}</p>
              </div>
            </section>

            <section className="detail__section detail__section--price">
              <h2 className="detail__label">가격</h2>
              <ul className="detail__prices">
                {d.prices.map(({ seat, price }) => (
                  <li key={seat}>
                    <span>{seat}</span>
                    <span>{formatWon(price)}</span>
                  </li>
                ))}
              </ul>
            </section>

            <section className="detail__section">
              <h2 className="detail__label">예매처 이동</h2>
              <div className="detail__tickets">
                {d.tickets.map(({ name, url }) => (
                  <a
                    key={name}
                    className="detail__ticket"
                    href={url || undefined}
                    target="_blank"
                    rel="noreferrer"
                  >
                    {name}
                    <img src={linkIcon} alt="" />
                  </a>
                ))}
              </div>
            </section>

            <section className="detail__section">
              <div className="detail__label-row">
                <h2 className="detail__label">전체 배우진</h2>
                <span className="detail__hint">프로필을 눌러 배우별 후기를 확인하세요</span>
              </div>
              <div className="detail__actors">
                {d.actors.map(({ id: actorId, name }) => (
                  <button
                    key={actorId}
                    type="button"
                    className="detail__actor"
                    onClick={() =>
                      navigate(`/detail/${id}/actor/${encodeURIComponent(name)}`, {
                        state: { work: { genre: d.genre, title: d.title } },
                      })
                    }
                  >
                    {name}
                  </button>
                ))}
              </div>
            </section>

            <section className="detail__section detail__section--video">
              <h2 className="detail__label">박제 영상</h2>
              <div className="detail__videos">
                {videoSlots.map((youtubeId, i) =>
                  youtubeId ? (
                    <iframe
                      key={youtubeId}
                      className="detail__video"
                      src={`https://www.youtube.com/embed/${youtubeId}`}
                      title={`박제 영상 ${i + 1}`}
                      allowFullScreen
                    />
                  ) : (
                    <div className="detail__video detail__video--empty" key={i}>
                      영상{i + 1}
                    </div>
                  )
                )}
              </div>
            </section>
          </>
        )}

        {tab === "pair" && <PairTab actors={d.actors} />}
        {tab === "review" && <ReviewTab />}
      </div>
    </div>
  );
}

export default Detail;