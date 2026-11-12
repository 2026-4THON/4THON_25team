const SDK_URL = "https://dapi.kakao.com/v2/maps/sdk.js";

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
    if (window.kakao && window.kakao.maps) {
      window.kakao.maps.load(() => resolve(window.kakao));
      return;
    }

    const script = document.createElement("script");
    script.src = `${SDK_URL}?appkey=${encodeURIComponent(key)}&autoload=false&libraries=services`;
    script.async = true;
    script.onload = () => {
      window.kakao.maps.load(() => resolve(window.kakao));
    };
    script.onerror = () => {
      loadPromise = null;
      script.remove();
      reject(new Error("지도 SDK를 불러오지 못했습니다."));
    };
    document.head.appendChild(script);
  });

  return loadPromise;
};
