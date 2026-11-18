import { useEffect, useState } from "react";

import "./my.scss";
import { fetchMySummary } from "../../api/my.js";

const DEFAULT_MESSAGE = "나의 취향을 분석 해볼까요?";

const formatWon = (n) => `${n.toLocaleString("ko-KR")}원`;

function SectionTitle({ children }) {
  return (
    <h2 className="my__title">
      <span className="my__rule my__rule--left" />
      <span className="my__title-text">{children}</span>
      <span className="my__rule my__rule--right" />
    </h2>
  );
}

function StatCard({ items }) {
  return (
    <div className="my__stats">
      {items.map(({ label, count }) => (
        <div className="my__stat" key={label}>
          <span className="my__stat-label">{label}</span>
          <span className="my__stat-count">{count}</span>
        </div>
      ))}
    </div>
  );
}

// 서버 응답 -> 화면에서 쓰는 모양으로 변환
function toView(s) {
  const t = s.ticketCounts ?? {};
  const r = s.reviewCounts ?? {};
  const e = s.expenses ?? {};
  return {
    userId: s.nickname,
    analysis: s.tasteBriefing || null, // 없으면 빈 문자열이므로 || 사용
    tickets: [
      { label: "뮤지컬", count: t.musical ?? 0 },
      { label: "연극", count: t.play ?? 0 },
      { label: "기타", count: t.other ?? 0 },
    ],
    reviews: [
      { label: "공연", count: r.performance ?? 0 },
      { label: "배우", count: r.actor ?? 0 },
      { label: "페어", count: r.pair ?? 0 },
    ],
    spending: [
      { label: "티켓", amount: e.ticketTotal ?? 0 },
      // 청년 문화 예술 패스는 차감 금액이라 항상 마이너스로 표시
      { label: "청년 문화 예술 패스", amount: -Math.abs(e.culturePassTotal ?? 0) },
      { label: "MD", amount: e.mdTotal ?? 0 },
    ],
    totalSpending: e.totalSpending ?? 0,
  };
}

function My() {
  const [state, setState] = useState({ data: null, error: false });

  useEffect(() => {
    let cancelled = false;
    fetchMySummary()
      .then((s) => !cancelled && setState({ data: toView(s), error: false }))
      .catch(() => !cancelled && setState({ data: null, error: true }));
    return () => {
      cancelled = true;
    };
  }, []);

  if (state.error) {
    return (
      <main className="my">
        <p className="my__message">마이페이지를 불러오지 못했어요</p>
      </main>
    );
  }
  if (!state.data) {
    return (
      <main className="my">
        <p className="my__message">불러오는 중...</p>
      </main>
    );
  }

  const { userId, analysis, tickets, reviews, spending, totalSpending } = state.data;

  return (
    <main className="my">
      <header className="my__header">
        <p className="my__slogan">대학로 공연 관극 기록엔 ‘댕로그’</p>
      </header>

      <section className="my__profile">
        <p className="my__id">@{userId}</p>
        <p className="my__message">{analysis ?? DEFAULT_MESSAGE}</p>
      </section>

      <section className="my__section">
        <SectionTitle>관람한 티켓</SectionTitle>
        <StatCard items={tickets} />
      </section>

      <section className="my__section">
        <SectionTitle>작성 후기</SectionTitle>
        <StatCard items={reviews} />
      </section>

      <section className="my__spending">
        <ul className="my__spending-list">
          {spending.map(({ label, amount }) => (
            <li className="my__spending-row" key={label}>
              <span>{label}</span>
              <span>{formatWon(amount)}</span>
            </li>
          ))}
        </ul>
        <div className="my__total">
          <span>총 지출</span>
          <span>{formatWon(totalSpending)}</span>
        </div>
      </section>
    </main>
  );
}

export default My;