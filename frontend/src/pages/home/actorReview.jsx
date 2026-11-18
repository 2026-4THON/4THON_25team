import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";

import "./actorReview.scss";
import { fetchActorReviews, likeReview, unlikeReview } from "../../api/reviews.js";

import backIcon from "../../assets/home/back.svg";
import heartIcon from "../../assets/home/heart.svg"; 
import heartFilledIcon from "../../assets/home/heart-filled.svg"; 

const SORTS = [
  { key: "latest", label: "최신순" },
  { key: "likes", label: "좋아요순" },
  { key: "mine", label: "내 리뷰" },
];

// 서버 후기 -> 화면용
const toReview = (r) => ({
  id: r.reviewId,
  author: r.authorName,
  content: r.content,
  date: r.viewedAt,
  likes: r.likeCount,
  liked: r.likedByMe,
});

// "2026-05-09" -> "26 / 05 / 09"
const formatDate = (iso) => {
  if (!iso) return "";
  const [y, m, d] = iso.split("-");
  return `${y.slice(2)} / ${m} / ${d}`;
};

function ActorReview() {
  const navigate = useNavigate();
  const { name } = useParams(); // /detail/:id/actor/:name
  const actorName = decodeURIComponent(name ?? "");
  const { state } = useLocation();
  const work = state?.work ?? { genre: "", title: "" }; // 상세에서 navigate 할 때 넘겨주세요
  const { id } = useParams();

  const [sort, setSort] = useState("latest");
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  // 정렬(최신/좋아요/내 리뷰)이 바뀔 때마다 서버에서 다시 조회
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
  }, [id, sort, actorName]);

  // 좋아요: 서버 응답(likeCount, likedByMe)을 그대로 반영
  const toggleLike = useCallback(async (r) => {
    try {
      const res = await (r.liked ? unlikeReview(r.id) : likeReview(r.id));
      setReviews((prev) =>
        prev.map((x) => (x.id === r.id ? { ...x, likes: res.likeCount, liked: res.likedByMe } : x))
      );
    } catch {
      /* 401(로그인 필요) 등은 일단 무시 */
    }
  }, []);

  const visible = reviews;

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
          <span className="actor-review__genre">{work.genre}</span>
          <span className="actor-review__work-title">{work.title}</span>
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
        {!loading && !error && visible.length === 0 && (
          <li className="actor-review__empty">아직 작성된 후기가 없어요</li>
        )}
      </ul>
    </div>
  );
}

export default ActorReview;