import { useCallback, useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { useParams } from "react-router-dom";

import "./pairTab.scss";
import {
  fetchPairRecommendation,
  fetchPairReviews,
  likeReview,
  unlikeReview,
} from "../../api/reviews.js";

// 배우 후기 페이지에서 쓴 하트 아이콘을 그대로 사용해요
import heartIcon from "../../assets/home/heart.svg";
import heartFilledIcon from "../../assets/home/heart-filled.svg";

const MAX_PICK = 3; // 직접 선택 시 고를 수 있는 배우 수 (시안 기준 3명)

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

// actors: 작품 전체 출연진 [{id, name}] — 상세 화면에서 castings 를 내려주세요 (직접 선택 시트에 사용)
function PairTab({ actors = [] }) {
  const { id } = useParams(); // /detail/:id

  const [mode, setMode] = useState("ai"); // "ai" | "manual"
  const [ai, setAi] = useState({ status: "LOADING", pair: [], reason: "" });
  const [pair, setPair] = useState([]); // 화면에 보이는 페어 [{id, name}]
  const [sheetOpen, setSheetOpen] = useState(false);
  const [draft, setDraft] = useState([]); // 시트에서 고르는 중인 배우 [{id, name}]

  const [sort, setSort] = useState("latest");
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  // AI 페어 추천 (처음 요청 때 서버가 만들어 저장해 둠)
  useEffect(() => {
    let alive = true;
    fetchPairRecommendation(id)
      .then((d) => {
        if (!alive) return;
        const list = (d?.actors ?? []).map((a) => ({ id: a.actorId, name: a.name }));
        setAi({ status: d?.status ?? "NO_PAIR_REVIEWS", pair: list, reason: d?.reason ?? "" });
        setPair(list);
        setDraft(list);
      })
      .catch(() => alive && setAi({ status: "ERROR", pair: [], reason: "" }));
    return () => {
      alive = false;
    };
  }, [id]);

  // 선택된 페어 + 정렬이 바뀔 때마다 페어 후기 조회
  const pairKey = pair.map((a) => a.id).join(",");
  useEffect(() => {
    if (pair.length === 0) {
      setReviews([]);
      return;
    }
    let alive = true;
    setLoading(true);
    setError(false);
    const opts =
      sort === "mine" ? { sort: "latest", mineOnly: true, size: 50 } : { sort, size: 50 };
    fetchPairReviews(id, pair.map((a) => a.id), opts)
      .then((d) => alive && setReviews((d?.content ?? []).map(toReview)))
      .catch(() => alive && (setReviews([]), setError(true)))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, pairKey, sort]);

  const visible = reviews;

  const selectAi = () => {
    setMode("ai");
    setPair(ai.pair);
    setSheetOpen(false);
  };

  const openManual = () => {
    setMode("manual");
    setDraft(pair);
    setSheetOpen(true);
  };

  const toggleDraft = (actor) =>
    setDraft((prev) => {
      if (prev.some((a) => a.id === actor.id)) return prev.filter((a) => a.id !== actor.id);
      return prev.length < MAX_PICK ? [...prev, actor] : prev;
    });

  const confirmDraft = () => {
    setPair(draft);
    setSheetOpen(false);
  };

  // 좋아요: 서버 응답(likeCount, likedByMe)을 그대로 반영
  const toggleLike = useCallback(async (r) => {
    try {
      const res = await (r.liked ? unlikeReview(r.id) : likeReview(r.id));
      setReviews((prev) =>
        prev.map((x) => (x.id === r.id ? { ...x, likes: res.likeCount, liked: res.likedByMe } : x))
      );
    } catch {
      /* 401 등은 일단 무시 */
    }
  }, []);

  // 시트는 .layout(390x844 틀) 전체를 덮도록 그 안으로 옮겨 그려요
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
          {actors.map((actor) => {
            const on = draft.some((a) => a.id === actor.id);
            return (
              <button
                key={actor.id}
                type="button"
                className={on ? "pair__chip pair__chip--on" : "pair__chip"}
                onClick={() => toggleDraft(actor)}
                aria-pressed={on}
              >
                {actor.name}
              </button>
            );
          })}
          {actors.length === 0 && <p className="pair__empty">출연진 정보를 불러오지 못했어요</p>}
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
        {pair.map((a) => (
          <span className="pair__chip pair__chip--on" key={a.id}>
            {a.name}
          </span>
        ))}
      </div>
      {mode === "ai" && ai.status === "LOADING" && <p className="pair__reason">추천을 만드는 중...</p>}
      {mode === "ai" && ai.status === "ERROR" && <p className="pair__reason">추천을 불러오지 못했어요</p>}
      {mode === "ai" && ai.status === "NO_PAIR_REVIEWS" && (
        <p className="pair__reason">아직 페어 후기가 없어서 추천할 수 없어요</p>
      )}
      {mode === "ai" && ai.status === "INSUFFICIENT_EVIDENCE" && (
        <p className="pair__reason">내 후기가 부족해서 후기가 가장 많은 조합을 보여드려요</p>
      )}
      {mode === "ai" && ai.status === "RECOMMENDED" && ai.reason && (
        <p className="pair__reason">{ai.reason}</p>
      )}

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

        {loading && <li className="pair__empty">불러오는 중...</li>}
        {!loading && error && <li className="pair__empty">후기를 불러오지 못했어요</li>}
        {!loading && !error && visible.length === 0 && (
          <li className="pair__empty">아직 작성된 페어 후기가 없어요</li>
        )}
      </ul>

      {sheet && (host ? createPortal(sheet, host) : sheet)}
    </div>
  );
}

export default PairTab;