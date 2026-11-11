import { useState } from "react";
import { useNavigate } from "react-router-dom";

import "./detail.scss";

import backIcon from "../../assets/home/back.svg";
import starIcon from "../../assets/home/star.svg";
import linkIcon from "../../assets/home/link.svg";
import posterImg from "../../assets/home/poster.png";

// 임시 데이터
const MOCK = {
  title: "팬레터",
  genre: "뮤지컬",
  run: "3연",
  rating: 4.3,
  tags: ["#시대극", "#사색적인", "#갈망"],
  synopsis:
    "1930년대 일제강점기 경성. 작가 지망생 세훈은 동경하던 소설가 김해진에게 편지를 보내고, 그의 답장을 계기로 문인들의 세계에 발을 들이게 된다.",
  period: "2026.00.00 ~2026.00.00",
  venue: "홍익대학교아트센터",
  age: "8세이상",
  runtime: "180분",
  prices: [
    { seat: "R석", price: 50000 },
    { seat: "S석", price: 30000 },
  ],
  tickets: [
    { name: "NOL", url: "" },
    { name: "티켓링크", url: "" },
  ],
  actors: [
    "전동석", "이봄소리", "규현", "박지연", "이지혜",
    "박은태", "김준수", "유승현", "전동석", "이봄소리",
  ],
  videos: [{ youtubeId: "" }, { youtubeId: "" }],
};

const TABS = [
  { key: "info", label: "작품정보" },
  { key: "pair", label: "페어추천" },
  { key: "review", label: "작품후기" },
];

const formatWon = (n) => `${n.toLocaleString("ko-KR")}원`;

function Detail() {
  const navigate = useNavigate();
  const [tab, setTab] = useState("info");
  const [open, setOpen] = useState(false); 

  const d = MOCK;

  return (
    <div className="detail">
      <header className="detail__hero">
        <img className="detail__poster" src={posterImg} alt={`${d.title} 포스터`} />
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
            <span className="detail__pill">{d.genre}</span>
            <span className="detail__pill">{d.run}</span>
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
            <section className="detail__tags">
              {d.tags.map((t) => (
                <span className="detail__tag" key={t}>
                  {t}
                </span>
              ))}
            </section>

            <section className="detail__section">
              <h2 className="detail__label">시놉시스</h2>
              <p className={open ? "detail__synopsis detail__synopsis--open" : "detail__synopsis"}>
                {d.synopsis}
              </p>
              <button
                type="button"
                className="detail__more"
                onClick={() => setOpen((v) => !v)}
              >
                {open ? "접기" : "더보기"}
              </button>
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
                <p className="detail__value">{d.venue}</p>
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
                {d.actors.map((name, i) => (
                  <button
                    key={`${name}-${i}`}
                    type="button"
                    className="detail__actor"
                    onClick={() => {}}
                  >
                    {name}
                  </button>
                ))}
              </div>
            </section>

            <section className="detail__section detail__section--video">
              <h2 className="detail__label">박제 영상</h2>
              <div className="detail__videos">
                {d.videos.map(({ youtubeId }, i) =>
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

        {tab !== "info" && <p className="detail__empty">준비 중이에요</p>}
      </div>
    </div>
  );
}

export default Detail;
