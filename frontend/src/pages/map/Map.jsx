import { useEffect, useRef, useState } from "react";
import { loadKakaoMap } from "./kakaoMapLoader.js";
import { MAP_CENTER, THEATERS } from "./mapMock.js";
import { getRecords } from "../record/recordStore.js";
import iconPin from "../../assets/map/icon-pin.svg";
import iconPinSelected from "../../assets/map/icon-pin-selected.svg";
import iconPinHistory from "../../assets/map/icon-pin-history.svg";
import iconPinHistorySelected from "../../assets/map/icon-pin-history-selected.svg";
import iconSearch from "../../assets/map/icon-search.svg";
import iconTheater from "../../assets/map/icon-theater.svg";
import iconHistory from "../../assets/map/icon-history.svg";
import "./Map.scss";

const TABS = [
  { key: "now", label: "현재 공연중", icon: iconTheater },
  { key: "history", label: "나의 관람 기록", icon: iconHistory },
];

const SEARCH_TEXT = {
  now: {
    placeholder: "공연장을 검색해보세요",
    empty: "해당하는 극장이 없습니다",
  },
  history: {
    placeholder: "내가 관람한 공연장 찾기",
    empty: "관람한 내역이 없는 극장입니다",
  },
};

const NOW_THEATERS = THEATERS.filter((theater) => theater.shows.length > 0);

const normalizeName = (name) =>
  String(name ?? "")
    .split(" ")
    .join("")
    .toLowerCase();

const venueKey = (venue) => venue.trim().split(/\s+/).join(" ").toLowerCase();

const recordTime = (record) => `${record.date ?? ""} ${record.time ?? ""}`;

const isKoreaPosition = (lat, lng) =>
  Number.isFinite(lat) &&
  Number.isFinite(lng) &&
  lat >= 33 &&
  lat <= 39 &&
  lng >= 124 &&
  lng <= 132;

const readRecords = () => {
  try {
    const records = getRecords();
    return Array.isArray(records) ? records : [];
  } catch {
    return [];
  }
};

const findShowTags = (title) => {
  if (typeof title !== "string") {
    return [];
  }
  const key = venueKey(title);
  if (!key) {
    return [];
  }
  const show = THEATERS.flatMap((theater) => theater.shows).find(
    (item) => venueKey(item.title) === key,
  );
  return show ? show.tags : [];
};

const readVenues = () => {
  const groups = {};
  readRecords().forEach((record) => {
    if (typeof record.venue !== "string") {
      return;
    }
    const key = venueKey(record.venue);
    if (!key) {
      return;
    }
    if (!groups[key]) {
      groups[key] = { key, name: record.venue, records: [] };
    }
    groups[key].records.push({ ...record, tags: findShowTags(record.title) });
  });
  return Object.values(groups).map((venue) => ({
    ...venue,
    records: [...venue.records].sort((a, b) =>
      recordTime(b).localeCompare(recordTime(a)),
    ),
  }));
};

const toHistoryItem = (record) => ({
  id: record.id,
  genre: record.genre,
  title: record.title,
  period: record.time ? `${record.date} ${record.time}` : record.date,
  tags: record.tags,
});

const Map = () => {
  const mapElRef = useRef(null);
  const mapRef = useRef(null);
  const inputRef = useRef(null);
  const coordCacheRef = useRef(new globalThis.Map());
  const [status, setStatus] = useState("loading");
  const [tab, setTab] = useState("now");
  const [keyword, setKeyword] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [selectedKey, setSelectedKey] = useState(null);
  const [venues] = useState(readVenues);
  const [venueCoords, setVenueCoords] = useState({});

  const isHistory = tab === "history";

  let sheet = null;
  if (isHistory) {
    const venue = venues.find((item) => item.key === selectedKey);
    if (venue) {
      sheet = { title: venue.name, items: venue.records.map(toHistoryItem) };
    }
  } else {
    const theater = NOW_THEATERS.find((item) => item.id === selectedId);
    if (theater) {
      sheet = { title: theater.name, items: theater.shows };
    }
  }

  const query = keyword.trim().toLowerCase();
  let results = [];
  if (query && isHistory) {
    results = venues
      .filter((venue) => venue.name.toLowerCase().includes(query))
      .map((venue) => ({ key: venue.key, name: venue.name, venue }));
  } else if (query) {
    results = NOW_THEATERS.filter((theater) =>
      normalizeName(theater.name).includes(query),
    ).map((theater) => ({ key: theater.id, name: theater.name, theater }));
  }

  useEffect(() => {
    let cancelled = false;

    loadKakaoMap()
      .then((kakao) => {
        if (cancelled || !mapElRef.current) {
          return;
        }
        const map = new kakao.maps.Map(mapElRef.current, {
          center: new kakao.maps.LatLng(MAP_CENTER.lat, MAP_CENTER.lng),
          level: MAP_CENTER.level,
        });
        mapRef.current = map;
        kakao.maps.event.addListener(map, "click", () => {
          setSelectedId(null);
          setSelectedKey(null);
        });
        setStatus("ready");
      })
      .catch(() => {
        if (!cancelled) {
          setStatus("error");
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (status !== "ready" || tab !== "history") {
      return undefined;
    }

    const { kakao } = window;
    if (!kakao || !kakao.maps || !kakao.maps.services) {
      return undefined;
    }

    let cancelled = false;
    const { Places, Status } = kakao.maps.services;
    const places = new Places();

    const search = (text) =>
      new Promise((resolve) => {
        try {
          places.keywordSearch(text, (data, searchStatus) => {
            resolve({ status: searchStatus, place: data && data[0] });
          });
        } catch {
          resolve({ status: Status.ERROR, place: null });
        }
      });

    const run = async () => {
      for (const venue of venues) {
        if (cancelled) {
          return;
        }
        if (coordCacheRef.current.has(venue.key)) {
          continue;
        }

        let result = await search(venue.name);
        if (cancelled) {
          return;
        }
        if (result.status === Status.ZERO_RESULT) {
          const words = venue.name.trim().split(/\s+/);
          if (words.length > 1) {
            result = await search(words.slice(0, -1).join(" "));
            if (cancelled) {
              return;
            }
          }
        }
        if (result.status !== Status.OK || !result.place) {
          continue;
        }

        const lat = Number(result.place.y);
        const lng = Number(result.place.x);
        if (!isKoreaPosition(lat, lng)) {
          continue;
        }

        coordCacheRef.current.set(venue.key, { lat, lng });
        setVenueCoords((prev) => ({ ...prev, [venue.key]: { lat, lng } }));
      }
    };

    run();

    return () => {
      cancelled = true;
    };
  }, [status, tab, venues]);

  useEffect(() => {
    if (status !== "ready" || !mapRef.current) {
      return undefined;
    }

    const { kakao } = window;
    const map = mapRef.current;
    const normalImage = new kakao.maps.MarkerImage(
      isHistory ? iconPinHistory : iconPin,
      new kakao.maps.Size(31, 38),
      { offset: new kakao.maps.Point(15, 38) },
    );
    const activeImage = new kakao.maps.MarkerImage(
      isHistory ? iconPinHistorySelected : iconPinSelected,
      new kakao.maps.Size(43, 54),
      { offset: new kakao.maps.Point(21, 54) },
    );

    const pins = isHistory
      ? venues
          .filter((venue) => venueCoords[venue.key])
          .map((venue) => ({
            ...venueCoords[venue.key],
            isActive: venue.key === selectedKey,
            onClick: () => setSelectedKey(venue.key),
          }))
      : NOW_THEATERS.map((theater) => ({
          lat: theater.lat,
          lng: theater.lng,
          isActive: theater.id === selectedId,
          onClick: () => setSelectedId(theater.id),
        }));

    const markers = pins.map((pin) => {
      const marker = new kakao.maps.Marker({
        map,
        position: new kakao.maps.LatLng(pin.lat, pin.lng),
        image: pin.isActive ? activeImage : normalImage,
        zIndex: pin.isActive ? 2 : 1,
      });
      kakao.maps.event.addListener(marker, "click", pin.onClick);
      return marker;
    });

    return () => {
      markers.forEach((marker) => marker.setMap(null));
    };
  }, [status, isHistory, selectedId, selectedKey, venues, venueCoords]);

  const handleTabClick = (key) => {
    setTab(key);
    setKeyword("");
    setSearchOpen(false);
    setSelectedId(null);
    setSelectedKey(null);
  };

  const handleSearchKeyDown = (e) => {
    if (e.key === "Escape") {
      setSearchOpen(false);
      e.currentTarget.blur();
    }
  };

  const moveTo = (position) => {
    const map = mapRef.current;
    if (map && window.kakao && position) {
      map.setLevel(2);
      map.setCenter(new window.kakao.maps.LatLng(position.lat, position.lng));
    }
  };

  const handleResultClick = (result) => {
    if (result.venue) {
      setSelectedKey(result.venue.key);
      moveTo(venueCoords[result.venue.key]);
    } else {
      setSelectedId(result.theater.id);
      moveTo(result.theater);
    }
    setSearchOpen(false);
    setKeyword(result.name);
    inputRef.current?.blur();
  };

  const searchClass = [
    "map__search",
    isHistory ? "map__search--history" : "",
    searchOpen ? "map__search--open" : "",
  ]
    .filter(Boolean)
    .join(" ");

  const sheetClass = [
    "map__sheet",
    isHistory ? "map__sheet--history" : "",
    sheet && sheet.items.length >= 2 ? "map__sheet--tall" : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div className="map">
      <div className="map__canvas" ref={mapElRef} />

      {status === "error" && (
        <p className="map__notice">지도를 불러올 수 없어요.</p>
      )}

      {searchOpen && (
        <div className="map__dim" onClick={() => setSearchOpen(false)} />
      )}

      <div className={searchClass}>
        <div className="map__search-row">
          <input
            ref={inputRef}
            className="map__search-input"
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onFocus={() => setSearchOpen(true)}
            onKeyDown={handleSearchKeyDown}
            placeholder={SEARCH_TEXT[tab].placeholder}
            aria-label="공연장 검색"
          />
          <img className="map__search-icon" src={iconSearch} alt="" />
        </div>
        {searchOpen && query && (
          <ul className="map__search-list">
            {results.length > 0 ? (
              results.map((result) => (
                <li key={result.key} className="map__search-item">
                  <button
                    type="button"
                    className="map__search-result"
                    onClick={() => handleResultClick(result)}
                  >
                    {result.name}
                  </button>
                </li>
              ))
            ) : (
              <li className="map__search-item map__search-item--empty">
                {SEARCH_TEXT[tab].empty}
              </li>
            )}
          </ul>
        )}
      </div>

      <div className={`map__tabs${isHistory ? " map__tabs--history" : ""}`}>
        {TABS.map((item) => (
          <button
            key={item.key}
            type="button"
            className={`map__tab${tab === item.key ? " map__tab--active" : ""}`}
            onClick={() => handleTabClick(item.key)}
          >
            <img src={item.icon} alt="" />
            <span>{item.label}</span>
          </button>
        ))}
      </div>

      {sheet && (
        <section className={sheetClass}>
          <h2 className="map__sheet-title">{sheet.title}</h2>
          <div className="map__sheet-line" />
          <ul className="map__sheet-list">
            {sheet.items.map((show) => (
              <li key={show.id} className="map__show">
                <div className="map__show-info">
                  <div className="map__show-head">
                    <span className="map__show-genre">{show.genre}</span>
                    <span className="map__show-title">{show.title}</span>
                  </div>
                  <p className="map__show-period">{show.period}</p>
                </div>
                {show.tags.length > 0 && (
                  <div className="map__show-tags">
                    {show.tags.map((tag) => (
                      <span key={tag} className="map__show-tag">
                        {tag}
                      </span>
                    ))}
                  </div>
                )}
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
};

export default Map;
