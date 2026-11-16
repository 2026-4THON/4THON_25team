import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getRecords } from "../recordStore.js";
import completeLogo from "../../../assets/record/complete-logo.png";
import completeSparkle from "../../../assets/record/complete-sparkle.svg";
import "./RegisterComplete.scss";

const HOLES = Array.from({ length: 7 }, (_, i) => i);
const REDIRECT_MS = 2000;

const RegisterComplete = () => {
  const navigate = useNavigate();
  const [count] = useState(() => getRecords().length);

  const goRecord = () => navigate("/record", { replace: true });

  useEffect(() => {
    const timer = setTimeout(() => {
      navigate("/record", { replace: true });
    }, REDIRECT_MS);
    return () => clearTimeout(timer);
  }, [navigate]);

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
      <p className="complete__count">{count}번째 티켓</p>
    </div>
  );
};

export default RegisterComplete;
