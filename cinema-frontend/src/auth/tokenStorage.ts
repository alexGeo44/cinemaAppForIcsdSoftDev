// src/auth/tokenStorage.ts
const KEY = "token";

const safeLocalStorage = () => {
try {
    return window.localStorage;
} catch {
return null;
}
};

export const tokenStorage = {
get(): string | null {
    const ls = safeLocalStorage();
    const t = ls?.getItem(KEY);
    return t?.trim() ? t.trim() : null;
  },
  set(token: string) {
    const ls = safeLocalStorage();
    ls?.setItem(KEY, token);
  },
  clear() {
    const ls = safeLocalStorage();
    ls?.removeItem(KEY);
  },
};
