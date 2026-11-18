import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { api } from "../api/client";
import { useAuth } from "./AuthContext";

const NotificationsContext = createContext(null);

const POLL_INTERVAL_MS = 20_000;

export function NotificationsProvider({ children }) {
  const { user } = useAuth();
  const [items, setItems] = useState([]);

  const refresh = useCallback(
    () =>
      api("/api/notifications")
        .then(setItems)
        .catch(() => {}),
    [],
  );

  useEffect(() => {
    if (!user) {
      setItems([]);
      return;
    }

    refresh();
    const timer = setInterval(refresh, POLL_INTERVAL_MS);
    return () => clearInterval(timer);
  }, [user, refresh]);

  const markRead = async (id) => {
    await api(`/api/notifications/${id}/read`, { method: "POST" });
    setItems((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n)),
    );
  };

  const unread = items.filter((n) => !n.read).length;

  return (
    <NotificationsContext.Provider value={{ items, unread, refresh, markRead }}>
      {children}
    </NotificationsContext.Provider>
  );
}

export const useNotifications = () => useContext(NotificationsContext);
