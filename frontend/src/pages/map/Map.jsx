import { useCallback, useEffect, useRef, useState } from "react";
import MarqueeText from "../../components/common/MarqueeText.jsx";
import { loadKakaoMap } from "./kakaoMapLoader.js";
import { MAP_CENTER } from "./mapMock.js";
import {
  fetchVenues,
  fetchVenueShows,
  fetchVenueMyTickets,
} from "../../api/map.js";
import {
  toVenue,
  toShowItem,
  toTicketItem,
  isKoreaPosition,
} from "./mapApi.js";
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

const VENUE_EMPTY_TEXT = {
  now: "진행중인 공연장이 없어요",
  history: "관람한 공연장이 없어요",
};

const SHEET_EMPTY_TEXT = {
  now: "진행중인 공연이 없어요",
  history: "관람 기록이 없어요",
};

const normalizeName = (name) =>
  String(name ?? "")
    .split(" ")
    .join("")
    .toLowerCase();

// 서버 공연장 목록 → 좌표가 한국 범위 안인 공연장만
const toVenueList = (list) =>
  (Array.isArray(list) ? list : [])
    .map(toVenue)
    .filter((venue) => isKoreaPosition(venue.lat, venue.lng));

const Map = () => {
  const mapElRef = useRef(null);
  const mapRef = useRef(null);
  const inputRef = useRef(null);
  const sheetCacheRef = useRef(new globalThis.Map());
  const sheetRequestRef = useRef(0);
  const [status, setStatus] = useState("loading");
  const [tab, setTab] = useState("now");
  const [keyword, setKeyword] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [venues, setVenues] = useState([]);
  const [venuesStatus, setVenuesStatus] = useState("loading");
  const [selectedId, setSelectedId] = useState(null);
  const [sheetItems, setSheetItems] = useState([]);
  const [sheetStatus, setSheetStatus] = useState("idle");

  const isHistory = tab === "history";
  const selectedVenue = venues.find((venue) => venue.id === selectedId);

  const query = keyword.trim().toLowerCase();
  const results = query
    ? venues.filter((venue) =>
        normalizeName(venue.name).includes(normalizeName(query)),
      )
    : [];

  // 공연장 선택: 탭+venueId 키로 캐시를 쓰고, 최신 요청 결과만 반영한다.
  const selectVenue = useCallback(
    async (venueId) => {
      const requestId = sheetRequestRef.current + 1;
      sheetRequestRef.current = requestId;
      setSelectedId(venueId);

      const key = `${tab}:${venueId}`;
      const cached = sheetCacheRef.current.get(key);
      if (cached) {
        setSheetItems(cached);
        setSheetStatus("idle");
        return;
      }

      setSheetItems([]);
      setSheetStatus("loading");
      const isNow = tab === "now";
      try {
        const list = isNow
          ? await fetchVenueShows(venueId)
          : await fetchVenueMyTickets(venueId);
        const items = (Array.isArray(list) ? list : []).map(
          isNow ? toShowItem : toTicketItem,
        );
        sheetCacheRef.current.set(key, items);
        if (sheetRequestRef.current !== requestId) return;
        setSheetItems(items);
        setSheetStatus("idle");
      } catch {
        if (sheetRequestRef.current !== requestId) return;
        setSheetStatus("error");
      }
    },
    [tab],
  );

  // 선택 해제: 진행 중인 시트 요청 결과도 무시한다.
  const clearSelection = () => {
    sheetRequestRef.current += 1;
    setSelectedId(null);
    setSheetItems([]);
    setSheetStatus("idle");
  };

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
          sheetRequestRef.current += 1;
          setSelectedId(null);
          setSheetItems([]);
          setSheetStatus("idle");
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

  // 탭별 공연장 목록. 탭 전환이 빠를 때 이전 요청 결과는 무시한다.
  useEffect(() => {
    let ignore = false;
    fetchVenues(tab === "now" ? "NOW" : "MINE")
      .then((list) => {
        if (ignore) return;
        setVenues(toVenueList(list));
        setVenuesStatus("ready");
      })
      .catch(() => {
        if (ignore) return;
        setVenues([]);
        setVenuesStatus("error");
      });
    return () => {
      ignore = true;
    };
  }, [tab]);

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

    const markers = venues.map((venue) => {
      const isActive = venue.id === selectedId;
      const marker = new kakao.maps.Marker({
        map,
        position: new kakao.maps.LatLng(venue.lat, venue.lng),
        image: isActive ? activeImage : normalImage,
        zIndex: isActive ? 2 : 1,
      });
      kakao.maps.event.addListener(marker, "click", () => selectVenue(venue.id));
      return marker;
    });

    return () => {
      markers.forEach((marker) => marker.setMap(null));
    };
  }, [status, isHistory, selectedId, venues, selectVenue]);

  const handleTabClick = (key) => {
    setKeyword("");
    setSearchOpen(false);
    clearSelection();
    // 같은 탭을 다시 누르면 목록은 다시 부르지 않는다.
    if (key === tab) return;
    setTab(key);
    setVenues([]);
    setVenuesStatus("loading");
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

  const handleResultClick = (venue) => {
    selectVenue(venue.id);
    moveTo({ lat: venue.lat, lng: venue.lng });
    setSearchOpen(false);
    setKeyword(venue.name);
    inputRef.current?.blur();
  };

  let venueNotice = "";
  if (status !== "error") {
    if (venuesStatus === "error") {
      venueNotice = "공연장을 불러오지 못했어요";
    } else if (venuesStatus === "ready" && venues.length === 0) {
      venueNotice = VENUE_EMPTY_TEXT[tab];
    }
  }

  let sheetMessage = "";
  if (sheetStatus === "loading") {
    sheetMessage = "불러오는 중";
  } else if (sheetStatus === "error") {
    sheetMessage = "불러오지 못했어요";
  } else if (sheetItems.length === 0) {
    sheetMessage = SHEET_EMPTY_TEXT[tab];
  }

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
    !sheetMessage && sheetItems.length >= 2 ? "map__sheet--tall" : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div className="map">
      <div className="map__canvas" ref={mapElRef} />

      {status === "error" && (
        <p className="map__notice">지도를 불러올 수 없어요.</p>
      )}

      {venueNotice && <p className="map__notice">{venueNotice}</p>}

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
              results.map((venue) => (
                <li key={venue.id} className="map__search-item">
                  <button
                    type="button"
                    className="map__search-result"
                    onClick={() => handleResultClick(venue)}
                  >
                    {venue.name}
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

      {selectedVenue && (
        <section className={sheetClass}>
          <h2 className="map__sheet-title">
            <MarqueeText text={selectedVenue.name} />
          </h2>
          <div className="map__sheet-line" />
          {sheetMessage ? (
            <p className="map__sheet-message">{sheetMessage}</p>
          ) : (
            <ul className="map__sheet-list">
              {sheetItems.map((show) => (
                <li key={show.id} className="map__show">
                  <div className="map__show-info">
                    <div className="map__show-head">
                      {show.genre && (
                        <span className="map__show-genre">{show.genre}</span>
                      )}
                      <MarqueeText
                        text={show.title}
                        className="map__show-title"
                      />
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
          )}
        </section>
      )}
    </div>
  );
};

export default Map;
