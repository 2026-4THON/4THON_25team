import { RECORDS, RECORD_DETAIL } from "./recordMock.js";
import { SHOWS } from "./register/registerMock.js";
import defaultPoster from "../../assets/record/poster-sample-1.png";

// 백엔드 연동 전까지 쓰는 임시 저장소. API 가 오면 이 파일의 함수만 교체한다.
const STORAGE_KEY = "4thon-record-store-v1";

const isPlainObject = (value) =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const readItems = () => {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    const parsed = raw ? JSON.parse(raw) : {};
    return isPlainObject(parsed) ? parsed : {};
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
    .map((item) => item?.list)
    .filter(isPlainObject)
    .filter((list) => !RECORDS.some((record) => record.id === list.id))
    .map((list) => ({ ...list, poster: defaultPoster }));
  return [...added, ...seeded];
};

// 관극 날짜(YYYY.MM.DD)+시간(HH:mm)을 숫자로 바꾼다. 시간이 없으면 00:00 으로 본다.
export const getShowtimeValue = (record) => {
  const [year, month, day] = String(record.date ?? "")
    .split(".")
    .map(Number);
  const [hour, minute] = String(record.time || "00:00")
    .split(":")
    .map(Number);
  const dateValue = (year || 0) * 10000 + (month || 0) * 100 + (day || 0);
  return dateValue * 10000 + (hour || 0) * 100 + (minute || 0);
};

// 정렬 기준: 관극 날짜+시간, 같으면 order가 oldest면 id 오름차순, latest면 id 내림차순. order 는 "oldest" | "latest".
export const compareRecords = (a, b, order = "oldest") => {
  const diff = getShowtimeValue(a) - getShowtimeValue(b);
  if (diff !== 0) return order === "latest" ? -diff : diff;
  return order === "latest" ? b.id - a.id : a.id - b.id;
};

// 번호(no)는 저장값을 쓰지 않고 관극 날짜+시간 오름차순 순위로 계산한다.
export const getRecords = () => {
  const deleted = readDeleted();
  return getRawRecords()
    .filter((record) => !deleted.includes(record.id))
    .sort((a, b) => compareRecords(a, b))
    .map((record, index) => ({ ...record, no: index + 1 }));
};

const toArray = (value) => (Array.isArray(value) ? value : []);
const toObject = (value) => (isPlainObject(value) ? value : {});

// 저장된 detail 에 필드가 빠져 있어도 화면(RecordDetail)과 수정 폼(detailToForm)이
// 죽지 않도록 .map/[0]/속성 접근을 하는 필드만 RECORD_DETAIL 과 같은 타입의 기본값으로 채운다.
export const getDetail = (id) => {
  const detail = readItems()[id]?.detail;
  if (!isPlainObject(detail)) return RECORD_DETAIL;
  return {
    ...detail,
    date: typeof detail.date === "string" ? detail.date : "",
    seat: toArray(detail.seat),
    cast: toArray(detail.cast),
    actors: toArray(detail.actors),
    pairActors: toArray(detail.pairActors),
    expenses: toArray(detail.expenses),
    actorReviews: toObject(detail.actorReviews),
  };
};

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
