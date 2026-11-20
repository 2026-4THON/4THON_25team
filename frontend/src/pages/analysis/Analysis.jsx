import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import AnalysisResult from "./result/AnalysisResult.jsx";
import { ApiError } from "../../api/client.js";
import { fetchReport } from "../../api/analysis.js";
import { toTaste } from "./analysisApi.js";
import "./Analysis.scss";

const HOLES = Array.from({ length: 9 }, (_, i) => i);

const Analysis = () => {
  const navigate = useNavigate();
  // status: "loading" | "empty"(리포트 없음) | "error" | "done"
  const [status, setStatus] = useState("loading");
  const [taste, setTaste] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let cancelled = false;
    fetchReport()
      .then((report) => {
        if (cancelled) return;
        setTaste(toTaste(report));
        setStatus("done");
      })
      .catch((err) => {
        if (cancelled) return;
        const empty = err instanceof ApiError && err.status === 404;
        setStatus(empty ? "empty" : "error");
      });
    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  const handleRetry = () => {
    setStatus("loading");
    setReloadKey((prev) => prev + 1);
  };

  if (status === "done" && taste) {
    return <AnalysisResult taste={taste} />;
  }

  if (status === "loading" || status === "error") {
    return (
      <div className="analysis">
        <header className="analysis__header">
          <h1 className="analysis__title">나의 취향 레포트</h1>
        </header>
        <p className="analysis__status">
          {status === "loading" ? "불러오는 중" : "불러오지 못했어요"}
        </p>
        {status === "error" && (
          <button
            type="button"
            className="analysis__start"
            onClick={handleRetry}
          >
            다시 시도
          </button>
        )}
      </div>
    );
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
