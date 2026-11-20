import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { fetchTickets } from "../../../api/ticket.js";
import completeLogo from "../../../assets/record/complete-logo.png";
import completeSparkle from "../../../assets/record/complete-sparkle.svg";
import "./RegisterComplete.scss";

const HOLES = Array.from({ length: 7 }, (_, i) => i);
const REDIRECT_MS = 2000;

const RegisterComplete = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const fallbackCount = location.state?.ticketNumber;
  // loading: 응답 전(N 숨김) / done: 내 티켓 총 개수 / error: state.ticketNumber 사용
  const [countState, setCountState] = useState({ status: "loading" });

  let count = null;
  if (countState.status === "done") count = countState.total;
  else if (countState.status === "error") count = fallbackCount ?? null;

  const goRecord = () => navigate("/record", { replace: true });

  useEffect(() => {
    const timer = setTimeout(() => {
      navigate("/record", { replace: true });
    }, REDIRECT_MS);
    return () => clearTimeout(timer);
  }, [navigate]);

  useEffect(() => {
    let cancelled = false;
    fetchTickets()
      .then((res) => {
        if (cancelled) return;
        if (Array.isArray(res)) {
          setCountState({ status: "done", total: res.length });
        } else {
          setCountState({ status: "error" });
        }
      })
      .catch(() => {
        if (cancelled) return;
        setCountState({ status: "error" });
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="complete" onClick={goRecord}>
      <span className="complete__band complete__band--top" />
      <span className="complete__band complete__band--bottom" />
      <span className="complete__line complete__line--top" />
      <span className="complete__line complete__line--bottom" />
      <div className="complete__holes complete__holes--top">
        {HOLES.map((i) => (
          <span key={i} className="complete__hole" />
        ))}
      </div>
      <div className="complete__holes complete__holes--bottom">
        {HOLES.map((i) => (
          <span key={i} className="complete__hole" />
        ))}
      </div>
      <span className="complete__ring" />
      <span className="complete__glow" />
      <div className="complete__logo">
        <img className="complete__logo-img" src={completeLogo} alt="" />
      </div>
      <p className="complete__text">티켓 등록이 완료되었습니다</p>
      <img className="complete__sparkle" src={completeSparkle} alt="" />
      {count != null && <p className="complete__count">{count}번째 티켓</p>}
    </div>
  );
};

export default RegisterComplete;
