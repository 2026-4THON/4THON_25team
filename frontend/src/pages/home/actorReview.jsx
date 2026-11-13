import { useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import "./actorReview.scss";

import backIcon from "../../assets/home/back.svg";
import heartIcon from "../../assets/home/heart.svg"; 
import heartFilledIcon from "../../assets/home/heart-filled.svg"; 

// TODO: API 연동 후 교체 (지금은 시안 기준 임시 데이터)
const MOCK_WORK = { genre: "뮤지컬", title: "팬레터" };

const MOCK_REVIEWS = [
  {
    id: 1,
    author: "LikeLion",
    content: "배우에 대한 후기 1",
    date: "2026-05-09",
    likes: 1,
    liked: true,
    mine: true,
  },
  {
    id: 2,
    author: "LikeLion",
    content: "배우에 대한 후기 1",
    date: "2026-05-09",
    likes: 1,
    liked: true,
    mine: false,
  },
  {
    id: 3,
    author: "후기2",
    content: "배우에 대한 후기2\nㅇ\nㅇ\nㅇ\nㅇ",
    date: "2026-05-09",
    likes: 1,
    liked: false,
    mine: false,
  },
];

const SORTS = [
  { key: "latest", label: "최신순" },
  { key: "likes", label: "좋아요순" },
  { key: "mine", label: "내 리뷰" },
];

// "2026-05-09" -> "26 / 05 / 09"
const formatDate = (iso) => {
  const [y, m, d] = iso.split("-");
  return `${y.slice(2)} / ${m} / ${d}`;
};

function ActorReview() {
  const navigate = useNavigate();
  const { name } = useParams(); // /detail/:id/actor/:name
  const actorName = decodeURIComponent(name ?? "");

  const [sort, setSort] = useState("latest");
  const [reviews, setReviews] = useState(MOCK_REVIEWS);

  const visible = useMemo(() => {
    const list = sort === "mine" ? reviews.filter((r) => r.mine) : [...reviews];
    if (sort === "likes") list.sort((a, b) => b.likes - a.likes);
    if (sort === "latest") list.sort((a, b) => b.date.localeCompare(a.date));
    return list;
  }, [reviews, sort]);

  // TODO: 좋아요 API 연동
  const toggleLike = (id) =>
    setReviews((prev) =>
      prev.map((r) =>
        r.id === id
          ? { ...r, liked: !r.liked, likes: r.likes + (r.liked ? -1 : 1) }
          : r
      )
    );

  return (
    <div className="actor-review">
      <header className="actor-review__header">
        <button
          type="button"
          className="actor-review__back"
          onClick={() => navigate(-1)}
          aria-label="뒤로가기"
        >
          <img src={backIcon} alt="" />
        </button>
        <h1 className="actor-review__heading">배우 후기</h1>
      </header>

      <section className="actor-review__top">
        <div className="actor-review__work">
          <span className="actor-review__genre">{MOCK_WORK.genre}</span>
          <span className="actor-review__work-title">{MOCK_WORK.title}</span>
        </div>

        <div className="actor-review__actor">
          <p className="actor-review__actor-name">{actorName}</p>
        </div>
      </section>

      <div className="actor-review__sorts" role="tablist" aria-label="후기 정렬">
        {SORTS.map(({ key, label }) => (
          <button
            key={key}
            type="button"
            role="tab"
            aria-selected={sort === key}
            className={
              sort === key
                ? "actor-review__sort actor-review__sort--active"
                : "actor-review__sort"
            }
            onClick={() => setSort(key)}
          >
            {label}
          </button>
        ))}
      </div>

      <ul className="actor-review__list">
        {visible.map((r) => (
          <li className="actor-review__item" key={r.id}>
            <p className="actor-review__author">{r.author}</p>
            <p className="actor-review__content">{r.content}</p>
            <div className="actor-review__foot">
              <span className="actor-review__date">관람일 {formatDate(r.date)}</span>
              <button
                type="button"
                className="actor-review__like"
                onClick={() => toggleLike(r.id)}
                aria-pressed={r.liked}
                aria-label="좋아요"
              >
                <img src={r.liked ? heartFilledIcon : heartIcon} alt="" />
                {r.likes}
              </button>
            </div>
          </li>
        ))}

        {visible.length === 0 && (
          <li className="actor-review__empty">아직 작성된 후기가 없어요</li>
        )}
      </ul>
    </div>
  );
}

export default ActorReview;
