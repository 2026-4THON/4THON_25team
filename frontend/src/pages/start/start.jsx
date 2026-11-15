import { useNavigate } from "react-router-dom";

import "./start.scss";
import { login } from "../../auth.js";

import logo from "../../assets/start/logo.svg";
import ornament from "../../assets/start/ornament.svg"; 

function Start() {
  const navigate = useNavigate();

  const handleGoogle = () => {
    login();
    navigate("/", { replace: true });
  };

  return (
    <div className="start">
      <div className="start__band start__band--top" />

      <div className="start__stage">
        <div className="start__gap start__gap--top">
          <div className="start__line start__line--top" />
        </div>

        <div className="start__content">
          <div className="start__ring" />
          <div className="start__halo" />
          <div className="start__logo">
            <img src={logo} alt="댕로그 로고" />
          </div>

          <div className="start__copy">
            <p className="start__tagline">기록이 모여 발견으로 이어지는</p>
            <p className="start__desc">대학로 공연 추천 서비스</p>
          </div>

          <h1 className="start__title">댕로그</h1>

          <img className="start__ornament" src={ornament} alt="" />

          <button type="button" className="start__google" onClick={handleGoogle}>
            구글로 시작하기
          </button>
        </div>

        <div className="start__gap start__gap--bottom">
          <div className="start__line start__line--bottom" />
        </div>
      </div>

      <div className="start__band start__band--bottom" />
    </div>
  );
}

export default Start;
