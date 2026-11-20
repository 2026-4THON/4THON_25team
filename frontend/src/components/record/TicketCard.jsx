import ticketOutline from "../../assets/record/ticket-card-outline-a.svg";
import MarqueeText from "../common/MarqueeText.jsx";
import "./TicketCard.scss";

const DOT_COUNT = 7;

const TicketCard = ({ record, onClick }) => {
  return (
    <button type="button" className="ticket-card" onClick={onClick}>
      <img className="ticket-card__outline" src={ticketOutline} alt="" />
      <img
        className="ticket-card__poster"
        src={record.poster}
        alt={`${record.title} 포스터`}
      />
      <span className="ticket-card__no">{record.no}</span>
      <span className="ticket-card__dots" aria-hidden="true">
        {Array.from({ length: DOT_COUNT }, (_, i) => (
          <span key={i} className="ticket-card__dot" />
        ))}
      </span>
      <span className="ticket-card__info">
        <span className="ticket-card__genre">{record.genre}</span>
        <MarqueeText text={record.title} className="ticket-card__title" />
        <span className="ticket-card__meta">
          <span>{record.date}</span>
          <span>{record.time}</span>
        </span>
        <span className="ticket-card__venue">{record.venue}</span>
      </span>
    </button>
  );
};

export default TicketCard;
