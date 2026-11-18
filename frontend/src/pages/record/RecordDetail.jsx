import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getDetail, deleteRecord } from "./recordStore.js";
import iconBack from "../../assets/record/icon-back.svg";
import iconEdit from "../../assets/record/icon-edit.svg";
import iconDelete from "../../assets/record/icon-delete.svg";
import iconCalendarClock from "../../assets/record/icon-calendar-clock.svg";
import iconMap from "../../assets/record/icon-map.svg";
import iconTime from "../../assets/record/icon-time.svg";
import iconCompanion from "../../assets/record/icon-companion-friend.svg";
import iconStarFilled from "../../assets/record/icon-star-filled.svg";
import iconStarEmpty from "../../assets/record/icon-star-empty.svg";
import iconWarning from "../../assets/record/icon-warning.svg";
import "./RecordDetail.scss";

const STAR_COUNT = 5;
const PERFORATION_COUNT = 7;

const RecordDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const data = getDetail(Number(id));
  const [tab, setTab] = useState("info");
  const [selectedActor, setSelectedActor] = useState(data.actors[0]);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);

  const handleDelete = () => {
    deleteRecord(Number(id));
    setIsDeleteOpen(false);
    navigate("/record", { replace: true });
  };

  return (
    <div className="record-detail">
      <header className="record-detail__header">
        <button
          type="button"
          className="record-detail__back"
          onClick={() => navigate("/record")}
        >
          <img src={iconBack} alt="뒤로가기" />
        </button>
        <h1 className="record-detail__title">나의기록</h1>
        <div className="record-detail__actions">
          <button
            type="button"
            className="record-detail__action"
            onClick={() => navigate(`/record/${id}/edit`)}
          >
            <img src={iconEdit} alt="수정" />
          </button>
          <button
            type="button"
            className="record-detail__action"
            onClick={() => setIsDeleteOpen(true)}
          >
            <img src={iconDelete} alt="삭제" />
          </button>
        </div>
      </header>

      <div className="record-detail__tabs">
        <button
          type="button"
          className={`record-detail__tab record-detail__tab--info${tab === "info" ? " record-detail__tab--active" : ""}`}
          onClick={() => setTab("info")}
        >
          관람정보
        </button>
        <button
          type="button"
          className={`record-detail__tab record-detail__tab--review${tab === "review" ? " record-detail__tab--active" : ""}`}
          onClick={() => setTab("review")}
        >
          나의 후기
        </button>
      </div>

      <div className="record-detail__body">
        {tab === "info" ? (
          <>
            <section className="record-detail__section record-detail__section--head">
              <span className="record-detail__genre">{data.genre}</span>
              <h2 className="record-detail__name">{data.title}</h2>
            </section>

            <section className="record-detail__section record-detail__section--info">
              <ul className="record-detail__info-list">
                <li>
                  <img src={iconCalendarClock} alt="" />
                  <span>{data.date}</span>
                  <span>{data.time}</span>
                </li>
                <li>
                  <img src={iconMap} alt="" />
                  <span>{data.venue}</span>
                </li>
                <li>
                  <img src={iconTime} alt="" />
                  <span>{data.runningTime}</span>
                </li>
              </ul>
            </section>

            <section className="record-detail__section record-detail__section--seat">
              <div className="record-detail__seat">
                <span className="record-detail__label">자리</span>
                <span className="record-detail__seat-value">
                  {data.seat.map((s) => (
                    <span key={s}>{s}</span>
                  ))}
                </span>
              </div>
              <span className="record-detail__seat-divider" />
              <div className="record-detail__companion">
                <span className="record-detail__label">동행자</span>
                <span className="record-detail__companion-value">
                  <span>{data.companion}</span>
                  <img src={iconCompanion} alt="" />
                </span>
              </div>
            </section>

            <section className="record-detail__section record-detail__section--cast">
              <span className="record-detail__label record-detail__cast-label">캐스팅</span>
              <ul className="record-detail__chips">
                {data.cast.map((name) => (
                  <li key={name} className="record-detail__chip">
                    {name}
                  </li>
                ))}
              </ul>
            </section>

            <section className="record-detail__section record-detail__section--expense">
              <ul className="record-detail__expense-list">
                {data.expenses.map((item) => (
                  <li key={item.label}>
                    <span>{item.label}</span>
                    <span>{item.amount}</span>
                  </li>
                ))}
              </ul>
              <span className="record-detail__expense-divider" />
              <div className="record-detail__total">
                <span>총 지출</span>
                <span>{data.total}</span>
              </div>
            </section>
          </>
        ) : (
          <>
            <section className="record-detail__section record-detail__section--general">
              <div className="record-detail__heading">
                <h3>관극 후기</h3>
              </div>
              <div className="record-detail__stars">
                {Array.from({ length: STAR_COUNT }, (_, i) => (
                  <img
                    key={i}
                    src={i < data.rating ? iconStarFilled : iconStarEmpty}
                    alt=""
                  />
                ))}
              </div>
              <p className="record-detail__text">{data.review}</p>
            </section>

            <section className="record-detail__section record-detail__section--pair">
              <div className="record-detail__heading">
                <h3>페어 후기</h3>
              </div>
              <ul className="record-detail__chips">
                {data.pairActors.map((name) => (
                  <li
                    key={name}
                    className="record-detail__chip record-detail__chip--outline"
                  >
                    {name}
                  </li>
                ))}
              </ul>
              <p className="record-detail__text">{data.pairReview}</p>
            </section>

            <section className="record-detail__section record-detail__section--actor">
              <div className="record-detail__heading-row">
                <div className="record-detail__heading">
                  <h3>배우 후기</h3>
                </div>
                <span className="record-detail__hint">
                  이름을 눌러 내가 남긴 배우별 후기를 확인하세요
                </span>
              </div>
              <ul className="record-detail__chips">
                {data.actors.map((name) => (
                  <li key={name}>
                    <button
                      type="button"
                      className={`record-detail__chip record-detail__chip--button${selectedActor === name ? " record-detail__chip--selected" : ""}`}
                      onClick={() => setSelectedActor(name)}
                    >
                      {name}
                    </button>
                  </li>
                ))}
              </ul>
              <p className="record-detail__text">
                {data.actorReviews[selectedActor] ?? "남긴 후기가 없습니다."}
              </p>
            </section>
          </>
        )}
      </div>

      <div className="record-detail__perforation" aria-hidden="true">
        {Array.from({ length: PERFORATION_COUNT }, (_, i) => (
          <span key={i} className="record-detail__hole" />
        ))}
      </div>

      {isDeleteOpen && (
        <div className="record-detail__overlay">
          <div className="record-detail__modal" role="dialog" aria-modal="true">
            <img
              className="record-detail__modal-icon"
              src={iconWarning}
              alt=""
            />
            <p className="record-detail__modal-title">
              해당 티켓을 삭제하시겠습니까?
            </p>
            <p className="record-detail__modal-desc">
              삭제한 티켓은 복구할 수 없습니다.
            </p>
            <div className="record-detail__modal-buttons">
              <button
                type="button"
                className="record-detail__modal-keep"
                onClick={() => setIsDeleteOpen(false)}
              >
                유지하기
              </button>
              <button
                type="button"
                className="record-detail__modal-delete"
                onClick={handleDelete}
              >
                삭제하기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RecordDetail;
