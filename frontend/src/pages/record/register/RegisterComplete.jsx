import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./RegisterComplete.scss";

const HOLES = Array.from({ length: 7 }, (_, i) => i);
const REDIRECT_MS = 2500;

const RegisterComplete = () => {
  const navigate = useNavigate();

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
      <div className="complete__center">
        <div className="complete__logo">로고</div>
        <p className="complete__text">티켓 등록이 완료되었습니다</p>
      </div>
    </div>
  );
};

export default RegisterComplete;
