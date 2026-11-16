import { useMemo, useState } from "react";
import { createPortal } from "react-dom";

import "./pairTab.scss";

import heartIcon from "../../assets/home/heart.svg";
import heartFilledIcon from "../../assets/home/heart-filled.svg";

// 임시 데이터
const AI_PAIR = ["전동석", "박은태", "규현"];

const ALL_ACTORS = [
  "전동석", "이봄소리", "규현", "박지연", "이지혜",
  "박은태", "김준수", "유승현",
];

const MAX_PICK = 3; 

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

function PairTab() {
  const [mode, setMode] = useState("ai"); 
  const [pair, setPair] = useState(AI_PAIR); 
  const [sheetOpen, setSheetOpen] = useState(false);
  const [draft, setDraft] = useState(AI_PAIR); 

  const [sort, setSort] = useState("latest");
  const [reviews, setReviews] = useState(MOCK_REVIEWS);

  //  API
  const visible = useMemo(() => {
    const list = sort === "mine" ? reviews.filter((r) => r.mine) : [...reviews];
    if (sort === "likes") list.sort((a, b) => b.likes - a.likes);
    if (sort === "latest") list.sort((a, b) => b.date.localeCompare(a.date));
    return list;
  }, [reviews, sort]);

  const selectAi = () => {
    setMode("ai");
    setPair(AI_PAIR);
    setSheetOpen(false);
  };

  const openManual = () => {
    setMode("manual");
    setDraft(pair);
    setSheetOpen(true);
  };

  const toggleDraft = (name) =>
    setDraft((prev) => {
      if (prev.includes(name)) return prev.filter((n) => n !== name);
      return prev.length < MAX_PICK ? [...prev, name] : prev;
    });

  const confirmDraft = () => {
    setPair(draft);
    setSheetOpen(false);
  };

  //  API 연동
  const toggleLike = (id) =>
    setReviews((prev) =>
      prev.map((r) =>
        r.id === id
          ? { ...r, liked: !r.liked, likes: r.likes + (r.liked ? -1 : 1) }
          : r
      )
    );

  const sheet = sheetOpen && (
    <div className="pair-sheet" onClick={() => setSheetOpen(false)}>
      <div
        className="pair-sheet__panel"
        role="dialog"
        aria-modal="true"
        aria-label="전체 배우진"
        onClick={(e) => e.stopPropagation()}
      >
        <h3 className="pair-sheet__title">전체 배우진</h3>
        <div className="pair-sheet__rule" />

        <div className="pair-sheet__actors">
          {ALL_ACTORS.map((name) => (
            <button
              key={name}
              type="button"
              className={draft.includes(name) ? "pair__chip pair__chip--on" : "pair__chip"}
              onClick={() => toggleDraft(name)}
              aria-pressed={draft.includes(name)}
            >
              {name}
            </button>
          ))}
        </div>

        <button
          type="button"
          className="pair-sheet__done"
          onClick={confirmDraft}
          disabled={draft.length === 0}
        >
          선택 완료
        </button>
      </div>
    </div>
  );

  const host = typeof document !== "undefined" ? document.querySelector(".layout") : null;

  return (
    <div className="pair">
      <h2 className="pair__title">다음과 같은 주연배우 페어를 추천해요!</h2>

      <div className="pair__modes">
        <button
          type="button"
          className={mode === "ai" ? "pair__mode pair__mode--on" : "pair__mode"}
          onClick={selectAi}
        >
          AI 추천
        </button>
        <button
          type="button"
          className={mode === "manual" ? "pair__mode pair__mode--on" : "pair__mode"}
          onClick={openManual}
        >
          직접 선택하기
        </button>
      </div>

      <div className="pair__pair">
        {pair.map((name) => (
          <span className="pair__chip pair__chip--on" key={name}>
            {name}
          </span>
        ))}
      </div>

      <h2 className="pair__subtitle">사람들이 남긴 페어 후기를 살펴보세요</h2>

      <div className="pair__sorts" role="tablist" aria-label="후기 정렬">
        {SORTS.map(({ key, label }) => (
          <button
            key={key}
            type="button"
            role="tab"
            aria-selected={sort === key}
            className={sort === key ? "pair__sort pair__sort--active" : "pair__sort"}
            onClick={() => setSort(key)}
          >
            {label}
          </button>
        ))}
      </div>

      <ul className="pair__list">
        {visible.map((r) => (
          <li className="pair__item" key={r.id}>
            <p className="pair__author">{r.author}</p>
            <p className="pair__content">{r.content}</p>
            <div className="pair__foot">
              <span className="pair__date">관람일 {formatDate(r.date)}</span>
              <button
                type="button"
                className="pair__like"
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
          <li className="pair__empty">아직 작성된 페어 후기가 없어요</li>
        )}
      </ul>

      {sheet && (host ? createPortal(sheet, host) : sheet)}
    </div>
  );
}

export default PairTab;
