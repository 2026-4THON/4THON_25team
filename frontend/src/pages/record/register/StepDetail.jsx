import { useState } from "react";
import CastSheet from "./CastSheet.jsx";
import { getShowCast } from "./registerMock.js";
import iconSmileGood from "../../../assets/register/icon-smile-good.svg";
import iconSmileNormal from "../../../assets/register/icon-smile-normal.svg";
import iconSmileBad from "../../../assets/register/icon-smile-bad.svg";
import "./StepDetail.scss";

const COMPANIONS = ["혼자", "연인", "친구", "가족"];

const RECOMMENDS = [
  { value: "good", label: "추천", icon: iconSmileGood },
  { value: "normal", label: "보통", icon: iconSmileNormal },
  { value: "bad", label: "비추천", icon: iconSmileBad },
];

const onlyDigits = (value) => value.replace(/\D/g, "");

const sanitizeRow = (value) => {
  const letters = value.match(/[A-Za-z]/g);
  if (letters) return letters[letters.length - 1].toUpperCase();
  return onlyDigits(value);
};

const UnitInput = ({
  value,
  unit,
  size,
  onChange,
  sanitize = onlyDigits,
  inputMode = "numeric",
}) => (
  <div className="step-detail__unit">
    <input
      className={`step-detail__input step-detail__input--${size}`}
      type="text"
      inputMode={inputMode}
      value={value}
      onChange={(e) => onChange(sanitize(e.target.value))}
    />
    <span className="step-detail__unit-text">{unit}</span>
  </div>
);

const StepDetail = ({ form, onChange }) => {
  const [castOpen, setCastOpen] = useState(false);

  const toggleOption = (key, value) => {
    onChange({ [key]: form[key] === value ? "" : value });
  };

  const handleCastDone = (casting) => {
    onChange({ casting });
    setCastOpen(false);
  };

  return (
    <div className="step-detail">
      <section className="step-detail__field">
        <p className="step-detail__label">공연장을 입력해주세요</p>
        <input
          className="step-detail__input step-detail__input--full"
          type="text"
          value={form.venue}
          onChange={(e) => onChange({ venue: e.target.value })}
        />
      </section>

      <section className="step-detail__field">
        <p className="step-detail__label">좌석을 입력해주세요</p>
        <div className="step-detail__seats">
          <UnitInput
            value={form.floor}
            unit="층"
            size="seat"
            onChange={(floor) => onChange({ floor })}
          />
          <UnitInput
            value={form.row}
            unit="열"
            size="seat"
            sanitize={sanitizeRow}
            inputMode="text"
            onChange={(row) => onChange({ row })}
          />
          <UnitInput
            value={form.seat}
            unit="번"
            size="seat"
            onChange={(seat) => onChange({ seat })}
          />
        </div>
      </section>

      <section className="step-detail__field">
        <p className="step-detail__label">캐스팅을 입력해주세요</p>
        <div className="step-detail__chips">
          {form.casting.map((name) => (
            <span key={name} className="step-detail__chip">
              {name}
            </span>
          ))}
          <button
            type="button"
            className="step-detail__chip step-detail__chip--add"
            onClick={() => setCastOpen(true)}
          >
            +
          </button>
        </div>
      </section>

      <div className="step-detail__row">
        <section className="step-detail__field step-detail__field--half">
          <p className="step-detail__label">러닝타임</p>
          <UnitInput
            value={form.runtime}
            unit="분"
            size="wide"
            onChange={(runtime) => onChange({ runtime })}
          />
        </section>
        <section className="step-detail__field step-detail__field--half">
          <p className="step-detail__label">티켓가격</p>
          <UnitInput
            value={form.price}
            unit="원"
            size="wide"
            onChange={(price) => onChange({ price })}
          />
        </section>
      </div>

      <div className="step-detail__row">
        <section className="step-detail__field step-detail__field--half">
          <p className="step-detail__label">MD 구매</p>
          <UnitInput
            value={form.md}
            unit="원"
            size="wide"
            onChange={(md) => onChange({ md })}
          />
        </section>
        <section className="step-detail__field step-detail__field--pass">
          <p className="step-detail__label">청년 문화패스 사용</p>
          <UnitInput
            value={form.pass}
            unit="원"
            size="wide"
            onChange={(pass) => onChange({ pass })}
          />
        </section>
      </div>

      <section className="step-detail__field">
        <p className="step-detail__label">누구와 동행했나요?</p>
        <div className="step-detail__options">
          {COMPANIONS.map((name) => (
            <button
              key={name}
              type="button"
              className={`step-detail__option step-detail__option--companion${form.companion === name ? " step-detail__option--selected" : ""}`}
              onClick={() => toggleOption("companion", name)}
            >
              {name}
            </button>
          ))}
        </div>
      </section>

      <section className="step-detail__field">
        <p className="step-detail__label">동행에 대해 추천하나요?</p>
        <div className="step-detail__options">
          {RECOMMENDS.map((item) => (
            <button
              key={item.value}
              type="button"
              className={`step-detail__option step-detail__option--recommend${form.recommend === item.value ? " step-detail__option--selected" : ""}`}
              onClick={() => toggleOption("recommend", item.value)}
            >
              <span>{item.label}</span>
              <img src={item.icon} alt="" />
            </button>
          ))}
        </div>
      </section>

      {castOpen && (
        <CastSheet
          cast={getShowCast(form.show)}
          selected={form.casting}
          onDone={handleCastDone}
          onClose={() => setCastOpen(false)}
        />
      )}
    </div>
  );
};

export default StepDetail;
