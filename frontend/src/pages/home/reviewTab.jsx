import { useCallback, useEffect, useState } from "react";

import { useParams } from "react-router-dom";

import "./reviewTab.scss";
import { fetchShowReviews, likeReview, unlikeReview } from "../../api/reviews.js";

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

function ReviewTab() {
  const { id } = useParams(); // /detail/:id
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
    fetchShowReviews(id, params)
      .then((d) => alive && setReviews((d?.content ?? []).map(toReview)))
      .catch(() => alive && (setReviews([]), setError(true)))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [id, sort]);

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
    <div className="review-tab">
      <h2 className="review-tab__title">사람들의 관극 후기를 살펴보세요</h2>

      <div className="review-tab__sorts" role="tablist" aria-label="후기 정렬">
        {SORTS.map(({ key, label }) => (
          <button
            key={key}
            type="button"
            role="tab"
            aria-selected={sort === key}
            className={
              sort === key
                ? "review-tab__sort review-tab__sort--active"
                : "review-tab__sort"
            }
            onClick={() => setSort(key)}
          >
            {label}
          </button>
        ))}
      </div>

      <ul className="review-tab__list">
        {visible.map((r) => (
          <li className="review-tab__item" key={r.id}>
            <p className="review-tab__author">{r.author}</p>
            <p className="review-tab__content">{r.content}</p>
            <div className="review-tab__foot">
              <span className="review-tab__date">관람일 {formatDate(r.date)}</span>
              <button
                type="button"
                className="review-tab__like"
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

        {loading && <li className="review-tab__empty">불러오는 중...</li>}
        {!loading && error && <li className="review-tab__empty">후기를 불러오지 못했어요</li>}
        {!loading && !error && visible.length === 0 && (
          <li className="review-tab__empty">아직 작성된 후기가 없어요</li>
        )}
      </ul>
    </div>
  );
}

export default ReviewTab;