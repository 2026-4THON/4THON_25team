import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import TicketCard from "../../components/record/TicketCard.jsx";
import { fetchTickets } from "../../api/ticket.js";
import { compareRecords } from "./recordStore.js";
import { toRecordItem } from "./recordApi.js";
import iconAddTicket from "../../assets/record/icon-add-ticket.svg";
import "./Record.scss";

const Record = () => {
  const navigate = useNavigate();
  const [order, setOrder] = useState("latest");
  const [records, setRecords] = useState([]);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    let cancelled = false;
    fetchTickets()
      .then((res) => {
        if (cancelled) return;
        setRecords(Array.isArray(res) ? res.map(toRecordItem) : []);
        setStatus("done");
      })
      .catch(() => {
        if (cancelled) return;
        setStatus("error");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const sorted = [...records].sort((a, b) => compareRecords(a, b, order));

  let message = "";
  if (status === "loading") message = "불러오는 중";
  else if (status === "error") message = "불러오지 못했어요";
  else if (sorted.length === 0) message = "아직 기록이 없어요";

  return (
    <div className="record">
      <header className="record__header">
        <h1 className="record__title">나의 기록</h1>
        <button
          type="button"
          className="record__add"
          onClick={() => navigate("/record/new")}
        >
          <span>추가</span>
          <img src={iconAddTicket} alt="" />
        </button>
      </header>
      <div className="record__bar" />
      <div className="record__sort">
        <button
          type="button"
          className={`record__sort-btn${order === "latest" ? " record__sort-btn--active" : ""}`}
          onClick={() => setOrder("latest")}
        >
          최신순
        </button>
        <span className="record__sort-divider" />
        <button
          type="button"
          className={`record__sort-btn${order === "oldest" ? " record__sort-btn--active" : ""}`}
          onClick={() => setOrder("oldest")}
        >
          오래된 순
        </button>
      </div>
      {message ? (
        <p className="record__status">{message}</p>
      ) : (
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
      )}
    </div>
  );
};

export default Record;
