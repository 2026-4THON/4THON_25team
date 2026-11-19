import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";

import "./actorReview.scss";
import { fetchActorReviews, likeReview, unlikeReview } from "../../api/reviews.js";

import backIcon from "../../assets/home/play.svg";
import heartIcon from "../../assets/home/heart.svg";
import heartFilledIcon from "../../assets/home/heart-filled.svg";
import playIcon from "../../assets/home/chevron.svg";

const SORTS = [
  { key: "latest", label: "최신순" },
  { key: "likes", label: "좋아요순" },
  { key: "mine", label: "내 리뷰" },
];

const toReview = (r) => ({
  id: r.reviewId,
  author: r.authorName,
  content: r.content,
  date: r.viewedAt,
  likes: r.likeCount,
  liked: r.likedByMe,
});

const formatDate = (iso) => {
  if (!iso) return "";
  const [y, m, d] = iso.split("-");
  return `${y.slice(2)} / ${m} / ${d}`;
};

function ActorReview() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const { id, name } = useParams();
  const actorName = decodeURIComponent(name ?? "");
  const work = state?.work ?? { genre: "", title: "" };

  const [sort, setSort] = useState("latest");
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError(false);
    const params =
      sort === "mine"
        ? { sort: "latest", mineOnly: true, size: 50 }
        : { sort, size: 50 };
    fetchActorReviews(id, actorName, params)
      .then((d) => alive && setReviews((d?.content ?? []).map(toReview)))
      .catch(() => alive && (setReviews([]), setError(true)))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [id, actorName, sort]);

  const toggleLike = useCallback(async (r) => {
    try {
      const res = await (r.liked ? unlikeReview(r.id) : likeReview(r.id));
      setReviews((prev) =>
        prev.map((x) =>
          x.id === r.id ? { ...x, likes: res.likeCount, liked: res.likedByMe } : x
        )
      );
    } catch {
      return;
    }
  }, []);

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
        <button
          type="button"
          className="actor-review__explore"
          onClick={() => navigate("/explore")}
        >
          내 상황에 맞춘 공연 탐색
          <img src={playIcon} alt="" />
        </button>
      </header>

      <section className="actor-review__top">
        {work.title && (
          <div className="actor-review__work">
            {work.genre && <span className="actor-review__genre">{work.genre}</span>}
            <span className="actor-review__work-title">{work.title}</span>
          </div>
        )}

        <div className="actor-review__actor">
          <span className="actor-review__rule" />
          <p className="actor-review__actor-name">{actorName}</p>
          <span className="actor-review__rule" />
        </div>
      </section>

      <div className="actor-review__sorts" role="tablist" aria-label="후기 정렬">
        {SORTS.map(({ key, label }, i) => (
          <span className="actor-review__sort-wrap" key={key}>
            {i > 0 && <span className="actor-review__sort-bar" />}
            <button
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
          </span>
        ))}
      </div>

      <ul className="actor-review__list">
        {reviews.map((r) => (
          <li className="actor-review__item" key={r.id}>
            <p className="actor-review__author">{r.author}</p>
            <p className="actor-review__content">{r.content}</p>
            <div className="actor-review__foot">
              <span className="actor-review__date">관람일 {formatDate(r.date)}</span>
              <button
                type="button"
                className="actor-review__like"
                onClick={() => toggleLike(r)}
                aria-pressed={r.liked}
                aria-label="좋아요"
              >
                <img src={r.liked ? heartFilledIcon : heartIcon} alt="" />
                {r.likes}
              </button>
            </div>
          </li>
        ))}

        {loading && <li className="actor-review__empty">불러오는 중...</li>}
        {!loading && error && <li className="actor-review__empty">후기를 불러오지 못했어요</li>}
        {!loading && !error && reviews.length === 0 && (
          <li className="actor-review__empty">아직 작성된 후기가 없어요</li>
        )}
      </ul>
    </div>
  );
}

export default ActorReview;