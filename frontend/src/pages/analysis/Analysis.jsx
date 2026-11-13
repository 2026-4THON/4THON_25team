import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AnalysisResult from "./result/AnalysisResult.jsx";
import { getTaste } from "./analysisStore.js";
import "./Analysis.scss";

const HOLES = Array.from({ length: 9 }, (_, i) => i);

const Analysis = () => {
  const navigate = useNavigate();
  const [taste] = useState(() => getTaste());

  if (taste?.done === true) {
    return <AnalysisResult taste={taste} />;
  }

  return (
    <div className="analysis">
      <header className="analysis__header">
        <h1 className="analysis__title">나의 취향 레포트</h1>
      </header>
      <div className="analysis__band">
        <p className="analysis__band-text">나의 취향을 분석하러 갈까요?</p>
      </div>
      <div className="analysis__holes analysis__holes--top">
        {HOLES.map((i) => (
          <span key={i} className="analysis__hole" />
        ))}
      </div>
      <div className="analysis__holes analysis__holes--bottom">
        {HOLES.map((i) => (
          <span key={i} className="analysis__hole" />
        ))}
      </div>
      <button
        type="button"
        className="analysis__start"
        onClick={() => navigate("/analysis/survey")}
      >
        분석 시작하기
      </button>
    </div>
  );
};

export default Analysis;
