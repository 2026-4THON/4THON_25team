const KEY = "daengrog:logged-in";

export const isLoggedIn = () => {
  try {
    return localStorage.getItem(KEY) === "1";
  } catch {
    return false;
  }
};

// 구글 로그인 연동 후에는 토큰을 저장하고 확인하는 방식으로 변경 필요
export const login = () => {
  try {
    localStorage.setItem(KEY, "1");
  } catch {

  }
};

export const logout = () => {
  try {
    localStorage.removeItem(KEY);
  } catch {

  }
};