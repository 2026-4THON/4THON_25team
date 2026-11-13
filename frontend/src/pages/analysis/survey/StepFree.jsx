import "./StepFree.scss";

const StepFree = ({ value, onChange }) => {
  return (
    <div className="step-free">
      <h2 className="step-free__subtitle">4. 자유롭게 작성해주세요</h2>
      <textarea
        className="step-free__textarea"
        value={value}
        placeholder="세부적인 취향을 적어주시면 좀더 정확한 분석이 가능해요"
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
};

export default StepFree;
