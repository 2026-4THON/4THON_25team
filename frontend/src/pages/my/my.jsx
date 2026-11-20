import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import "./my.scss";
import { fetchMySummary } from "../../api/my.js";
import { logout } from "../../api/logout.js";

// 서버 응답(/api/my/summary) -> 화면용
const toView = (d) => ({
  userId: d.nickname,
  analysis: d.tasteBriefing ? d.tasteBriefing : null, // 없으면 빈 문자열 -> null
  tickets: [
    { label: "뮤지컬", count: d.ticketCounts?.musical ?? 0 },
    { label: "연극", count: d.ticketCounts?.play ?? 0 },
    { label: "기타", count: d.ticketCounts?.other ?? 0 },
  ],
  reviews: [
    { label: "공연", count: d.reviewCounts?.performance ?? 0 },
    { label: "배우", count: d.reviewCounts?.actor ?? 0 },
    { label: "페어", count: d.reviewCounts?.pair ?? 0 },
  ],
  spending: [
    { label: "티켓", amount: d.expenses?.ticketTotal ?? 0 },
    // 서버는 양수로 주고, 화면에서는 차감(-)으로 보여줘요. 이미 음수면 그대로 둬요
    { label: "청년 문화 예술 패스", amount: -Math.abs(d.expenses?.culturePassTotal ?? 0) },
    { label: "MD", amount: d.expenses?.mdTotal ?? 0 },
  ],
  totalSpending: d.expenses?.totalSpending ?? 0,
});

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

function My() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let alive = true;
    fetchMySummary()
      .then((d) => alive && setData(toView(d)))
      .catch((e) => {
        console.error("[my] 요청 실패", e);
        if (alive) setError(String(e.status ?? "error"));
      });
    return () => {
      alive = false;
    };
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate("/start", { replace: true });
  };

  if (error) {
    return (
      <main className="my">
        <p className="my__message">마이페이지를 불러오지 못했어요 ({error})</p>
      </main>
    );
  }
  if (!data) {
    return (
      <main className="my">
        <p className="my__message">불러오는 중...</p>
      </main>
    );
  }

  const { userId, analysis, tickets, reviews, spending, totalSpending } = data;

  return (
    <main className="my">
      <header className="my__header">
        <p className="my__slogan">대학로 공연 관극 기록엔 ‘댕로그’</p>
        <button type="button" className="my__logout" onClick={handleLogout}>
          로그아웃
        </button>
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