import "./start.scss";
import { loginWithGoogle } from "../../auth.js";

import logo from "../../assets/start/logo.svg";
import ornament from "../../assets/start/ornament.svg";

const DOTS = Array.from({ length: 7 });

function Dots({ position }) {
  return (
    <div className={`start__dots start__dots--${position}`} aria-hidden="true">
      {DOTS.map((_, i) => (
        <span key={i} className="start__dot" />
      ))}
    </div>
  );
}

function Start() {
  return (
    <div className="start">
      <div className="start__page">
        <div className="start__band start__band--top">
          <Dots position="top" />
        </div>

        <div className="start__body">
          <span className="start__rule start__rule--top" />

          <div className="start__hero">
            <div className="start__ring" />
            <div className="start__halo" />

            <div className="start__logo">
              <img src={logo} alt="댕로그 로고" />
            </div>

            <div className="start__copy">
              <p className="start__tagline">{"기록이 모여 발견으로 이어지는 "}</p>
              <p className="start__desc">대학로 공연 추천 서비스</p>
            </div>

            <h1 className="start__title">댕로그</h1>

            <div className="start__ornament" aria-hidden="true">
              <span className="start__ornament-line start__ornament-line--left" />
              <span className="start__ornament-diamond start__ornament-diamond--left" />
              <img className="start__ornament-star" src={ornament} alt="" />
              <span className="start__ornament-diamond start__ornament-diamond--right" />
              <span className="start__ornament-line start__ornament-line--right" />
            </div>

            <button type="button" className="start__google" onClick={loginWithGoogle}>
              구글로 시작하기
            </button>
          </div>

          <span className="start__rule start__rule--bottom" />
        </div>

        <div className="start__band start__band--bottom">
          <Dots position="bottom" />
        </div>
      </div>
    </div>
  );
}

export default Start;