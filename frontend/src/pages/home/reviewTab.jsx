import { useMemo, useState } from "react";

import "./reviewTab.scss";

import heartIcon from "../../assets/home/heart.svg";
import heartFilledIcon from "../../assets/home/heart-filled.svg";

//  임시 데이터
const REVIEW_TEXT =
  "A 배우가 해진 특유의 진지하고 예민한 분위기를 살려 대사를 이어가는데, B 배우가 세훈 특유의 순수하고 어딘가 엉뚱한 반응으로 받아치는 장면이 너무 좋았다. 특히 B 배우가 대사 끝에 살짝 덧붙인 애드리브에 A 배우가 순간 웃음을 참는 듯한 표정을 지었는데, 그걸 놓치지 않고 C 배우가 자연스럽게 끼어들면서 이어지는 티키타카가 정말 재미있었다";

const MOCK_REVIEWS = [
  { id: 1, author: "LikeLion", content: REVIEW_TEXT, date: "2026-05-09", likes: 1, liked: true, mine: true },
  { id: 2, author: "후기2", content: REVIEW_TEXT, date: "2026-05-09", likes: 1, liked: false, mine: false },
];

const SORTS = [
  { key: "latest", label: "최신순" },
  { key: "likes", label: "좋아요순" },
  { key: "mine", label: "내 리뷰" },
];

const formatDate = (iso) => {
  const [y, m, d] = iso.split("-");
  return `${y.slice(2)} / ${m} / ${d}`;
};

function ReviewTab() {
  const [sort, setSort] = useState("latest");
  const [reviews, setReviews] = useState(MOCK_REVIEWS);

  const visible = useMemo(() => {
    const list = sort === "mine" ? reviews.filter((r) => r.mine) : [...reviews];
    if (sort === "likes") list.sort((a, b) => b.likes - a.likes);
    if (sort === "latest") list.sort((a, b) => b.date.localeCompare(a.date));
    return list;
  }, [reviews, sort]);

  //  API 연동
  const toggleLike = (id) =>
    setReviews((prev) =>
      prev.map((r) =>
        r.id === id
          ? { ...r, liked: !r.liked, likes: r.likes + (r.liked ? -1 : 1) }
          : r
      )
    );

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
          <li className="review-tab__empty">아직 작성된 후기가 없어요</li>
        )}
      </ul>
    </div>
  );
}

export default ReviewTab;
