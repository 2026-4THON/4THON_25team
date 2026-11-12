import { RECORDS, RECORD_DETAIL } from "./recordMock.js";
import { SHOWS } from "./register/registerMock.js";
import defaultPoster from "../../assets/record/poster-sample-1.png";

// 백엔드 연동 전까지 쓰는 임시 저장소. API 가 오면 이 파일의 함수만 교체한다.
const STORAGE_KEY = "4thon-record-store-v1";

const readItems = () => {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
};

const writeItems = (items) => {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  } catch {
    // 저장소를 쓸 수 없으면 저장하지 않는다.
  }
};

const toNumber = (value) => Number(String(value).replace(/\D/g, "")) || 0;
const won = (value) => `${value.toLocaleString("ko-KR")}원`;
const dotDate = (date) => date.split("-").join(".");
const dashDate = (date) => date.split(".").join("-");

const formToItem = (form, id, no) => {
  const price = toNumber(form.price);
  const pass = toNumber(form.pass);
  const md = toNumber(form.md);
  const date = dotDate(form.date);
  const { genre, title } = form.show;

  return {
    list: { id, no, genre, title, date, time: form.time, venue: form.venue },
    detail: {
      id,
      genre,
      title,
      date,
      time: form.time,
      venue: form.venue,
      runningTime: `${toNumber(form.runtime)}분`,
      seat: [
        form.floor && `${form.floor}층`,
        form.row && `${form.row}열`,
        form.seat && `${form.seat}번`,
      ].filter(Boolean),
      seatParts: { floor: form.floor, row: form.row, seat: form.seat },
      companion: form.companion,
      recommend: form.recommend,
      cast: form.casting,
      expenses: [
        { label: "티켓", amount: won(price) },
        { label: "청년 문화 예술 패스", amount: won(pass) },
        { label: "MD", amount: won(md) },
      ],
      total: won(price - pass + md),
      rating: form.rating,
      review: form.review,
      pairActors: form.pair,
      pairReview: form.pairReview,
      actors: form.casting,
      actorReviews: form.actorReviews,
    },
  };
};

const detailToForm = (detail) => {
  const show = SHOWS.find((item) => item.title === detail.title) ?? {
    id: 0,
    genre: detail.genre,
    title: detail.title,
    venue: detail.venue,
    runtime: toNumber(detail.runningTime),
  };
  const parsed = detail.seat.map((part) => part.replace(/[^0-9A-Za-z]/g, ""));
  const parts = detail.seatParts ?? {
    floor: parsed[0] ?? "",
    row: parsed[1] ?? "",
    seat: parsed[2] ?? "",
  };
  const amountOf = (label) => {
    const item = detail.expenses.find((expense) => expense.label === label);
    return item ? String(toNumber(item.amount)) : "";
  };

  return {
    show,
    date: dashDate(detail.date),
    time: detail.time,
    venue: detail.venue,
    floor: parts.floor,
    row: parts.row,
    seat: parts.seat,
    casting: detail.cast,
    runtime: String(toNumber(detail.runningTime)),
    price: amountOf("티켓"),
    md: amountOf("MD"),
    pass: amountOf("청년 문화 예술 패스"),
    companion: detail.companion,
    recommend: detail.recommend ?? "",
    rating: detail.rating,
    review: detail.review,
    reviewActor: detail.cast[0] ?? "",
    actorReviews: detail.actorReviews,
    pair: detail.pairActors,
    pairReview: detail.pairReview,
  };
};

const DELETED_KEY = "4thon-record-deleted-v1";

const readDeleted = () => {
  try {
    const raw = localStorage.getItem(DELETED_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const writeDeleted = (ids) => {
  try {
    localStorage.setItem(DELETED_KEY, JSON.stringify(ids));
  } catch {
    // 저장 실패 시 무시
  }
};

const getRawRecords = () => {
  const items = readItems();
  const seeded = RECORDS.map((record) =>
    items[record.id] ? { ...record, ...items[record.id].list } : record,
  );
  const added = Object.values(items)
    .map((item) => item.list)
    .filter((list) => !RECORDS.some((record) => record.id === list.id))
    .map((list) => ({ ...list, poster: defaultPoster }));
  return [...added, ...seeded];
};

export const getRecords = () => {
  const deleted = readDeleted();
  return getRawRecords()
    .filter((record) => !deleted.includes(record.id))
    .sort((a, b) => a.no - b.no)
    .map((record, index) => ({ ...record, no: index + 1 }));
};

export const getDetail = (id) => readItems()[id]?.detail ?? RECORD_DETAIL;

export const getEditForm = (id) => detailToForm(getDetail(id));

export const saveRecord = (form, id) => {
  const items = readItems();
  const raw = getRawRecords();
  const isEdit = typeof id === "number" && !Number.isNaN(id);
  const nextId = isEdit
    ? id
    : Math.max(0, ...raw.map((record) => record.id), ...readDeleted()) + 1;
  const no = isEdit
    ? (raw.find((record) => record.id === id)?.no ?? nextId)
    : Math.max(0, ...raw.map((record) => record.no)) + 1;

  items[nextId] = formToItem(form, nextId, no);
  writeItems(items);
  return nextId;
};

export const deleteRecord = (id) => {
  const items = readItems();
  delete items[id];
  writeItems(items);
  const deleted = readDeleted();
  if (!deleted.includes(id)) {
    writeDeleted([...deleted, id]);
  }
};
