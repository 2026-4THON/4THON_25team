import "./my.scss";

//  임시 데이터
const MOCK = {
  userId: "아이디",
  analysis: null,
  tickets: [
    { label: "뮤지컬", count: 5 },
    { label: "연극", count: 2 },
    { label: "기타", count: 1 },
  ],
  reviews: [
    { label: "공연", count: 5 },
    { label: "배우", count: 16 },
    { label: "페어", count: 3 },
  ],
  spending: [
    { label: "티켓", amount: 154000 },
    { label: "청년 문화 예술 패스", amount: -110000 },
    { label: "MD", amount: 20000 },
  ],
  totalSpending: 640000,
};

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
  const { userId, analysis, tickets, reviews, spending, totalSpending } = MOCK;

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
