import { http } from "./http.js";

const LOCAL_FLAG = "daengrog:logged-in";

export async function logout() {
  try {
    await http("/logout", { method: "POST" });
  } catch {
    return;
  } finally {
    try {
      localStorage.removeItem(LOCAL_FLAG);
    } catch {
      return;
    }
  }
}