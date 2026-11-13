const SDK_URL = "https://dapi.kakao.com/v2/maps/sdk.js";
const LOAD_TIMEOUT_MS = 10000;

let loadPromise = null;

export const loadKakaoMap = () => {
  const key = import.meta.env.VITE_KAKAO_MAP_KEY;

  if (!key) {
    return Promise.reject(
      new Error("지도 키가 설정되지 않았습니다. (.env.local 확인)"),
    );
  }

  if (loadPromise) {
    return loadPromise;
  }

  loadPromise = new Promise((resolve, reject) => {
    let script = null;
    let settled = false;

    const done = (kakao) => {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      resolve(kakao);
    };

    const fail = () => {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      loadPromise = null;
      script?.remove();
      reject(new Error("sdk-load-failed"));
    };

    const timer = setTimeout(fail, LOAD_TIMEOUT_MS);

    const loadMaps = () => {
      if (typeof window.kakao?.maps?.load !== "function") {
        fail();
        return;
      }
      window.kakao.maps.load(() => done(window.kakao));
    };

    if (window.kakao && window.kakao.maps) {
      loadMaps();
      return;
    }

    script = document.createElement("script");
    script.src = `${SDK_URL}?appkey=${encodeURIComponent(key)}&autoload=false&libraries=services`;
    script.async = true;
    script.onload = loadMaps;
    script.onerror = fail;
    document.head.appendChild(script);
  });

  return loadPromise;
};
