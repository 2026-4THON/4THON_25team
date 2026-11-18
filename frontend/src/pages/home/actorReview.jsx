import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import "./actorReview.scss";

import backIcon from "../../assets/home/back.svg";
import heartIcon from "../../assets/home/heart.svg";
import heartFilledIcon from "../../assets/home/heart-filled.svg";
import playIcon from "../../assets/home/chevron.svg";
import {
  fetchShow,
  fetchActorReviews,
  likeReview,
  unlikeReview,
} from "../../api/reviews.js";

const GENRE_LABEL = { MUSICAL: "뮤지컬", PLAY: "연극", ETC: "기타" };

// 탭 -> 서버 파라미터 (정렬/내 후기 필터는 서버에서 처리)
const SORTS = [
  { key: "latest", label: "최신순", params: { sort: "latest", mineOnly: false } },
  { key: "likes", label: "좋아요순", params: { sort: "likes", mineOnly: false } },
  { key: "mine", label: "내 리뷰", params: { sort: "latest", mineOnly: true } },
];

// "2026-05-09" -> "26 / 05 / 09"
const formatDate = (iso) => {
  if (!iso) return "";
  const [y, m, d] = iso.slice(0, 10).split("-");
  return `${y.slice(2)} / ${m} / ${d}`;
};

function ActorReview() {
  const navigate = useNavigate();
  const { id, name } = useParams(); // /detail/:id/actor/:name
  const actorName = decodeURIComponent(name ?? "");

  const [sort, setSort] = useState("latest");
  const [work, setWork] = useState(null); // { genre, title }
  const [result, setResult] = useState({ key: null, items: [], error: false });

  // 작품 정보(헤더)
  useEffect(() => {
    let cancelled = false;
    fetchShow(id)
      .then((s) => !cancelled && setWork({ genre: s.genre, title: s.title }))
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [id]);

  // 후기 목록: 탭이 바뀔 때마다 서버에서 다시 조회
  useEffect(() => {
    let cancelled = false;
    const key = `${id}|${actorName}|${sort}`;
    const { params } = SORTS.find((s) => s.key === sort);
    fetchActorReviews(id, actorName, params)
      .then((page) => !cancelled && setResult({ key, items: page.content ?? [], error: false }))
      .catch(() => !cancelled && setResult({ key, items: [], error: true }));
    return () => {
      cancelled = true;
    };
  }, [id, actorName, sort]);

  const loading = result.key !== `${id}|${actorName}|${sort}`;

  // 좋아요 등록/취소 후 서버가 돌려준 개수와 상태로 갱신
  const toggleLike = async (review) => {
    try {
      const res = review.likedByMe
        ? await unlikeReview(review.reviewId)
        : await likeReview(review.reviewId);
      setResult((prev) => ({
        ...prev,
        items: prev.items.map((r) =>
          r.reviewId === res.reviewId
            ? { ...r, likeCount: res.likeCount, likedByMe: res.likedByMe }
            : r
        ),
      }));
    } catch (e) {
      console.error("좋아요 처리 실패", e);
    }
  };

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
        <div className="actor-review__work">
          <span className="actor-review__genre">
            {work ? GENRE_LABEL[work.genre] ?? work.genre : ""}
          </span>
          <span className="actor-review__work-title">{work?.title ?? ""}</span>
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
        {loading && <li className="actor-review__empty">불러오는 중...</li>}
        {!loading && result.error && (
          <li className="actor-review__empty">후기를 불러오지 못했어요</li>
        )}

        {!loading &&
          result.items.map((r) => (
            <li className="actor-review__item" key={r.reviewId}>
              <p className="actor-review__author">{r.authorName}</p>
              <p className="actor-review__content">{r.content}</p>
              <div className="actor-review__foot">
                <span className="actor-review__date">관람일 {formatDate(r.viewedAt)}</span>
                <button
                  type="button"
                  className="actor-review__like"
                  onClick={() => toggleLike(r)}
                  aria-pressed={r.likedByMe}
                  aria-label="좋아요"
                >
                  <img src={r.likedByMe ? heartFilledIcon : heartIcon} alt="" />
                  {r.likeCount}
                </button>
              </div>
            </li>
          ))}

        {!loading && !result.error && result.items.length === 0 && (
          <li className="actor-review__empty">아직 작성된 후기가 없어요</li>
        )}
      </ul>
    </div>
  );
}

export default ActorReview;