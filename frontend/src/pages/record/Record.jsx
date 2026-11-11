import { useState } from "react";
import { useNavigate } from "react-router-dom";
import TicketCard from "../../components/record/TicketCard.jsx";
import { RECORDS } from "./recordMock.js";
import iconAddTicket from "../../assets/record/icon-add-ticket.svg";
import "./Record.scss";

const Record = () => {
  const navigate = useNavigate();
  const [order, setOrder] = useState("latest");

  const sorted = [...RECORDS].sort((a, b) =>
    order === "latest" ? b.no - a.no : a.no - b.no
  );

  return (
    <div className="record">
      <header className="record__header">
        <h1 className="record__title">나의 기록</h1>
        <button type="button" className="record__add">
          <img src={iconAddTicket} alt="" />
          <span>추가</span>
        </button>
      </header>
      <div className="record__bar" />
      <div className="record__sort">
        <button
          type="button"
          className={`record__sort-btn${order === "oldest" ? " record__sort-btn--active" : ""}`}
          onClick={() => setOrder("oldest")}
        >
          오래된 순
        </button>
        <span className="record__sort-divider" />
        <button
          type="button"
          className={`record__sort-btn${order === "latest" ? " record__sort-btn--active" : ""}`}
          onClick={() => setOrder("latest")}
        >
          최신순
        </button>
      </div>
      <ul className="record__list">
        {sorted.map((record) => (
          <li key={record.id}>
            <TicketCard
              record={record}
              onClick={() => navigate(`/record/${record.id}`)}
            />
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Record;
