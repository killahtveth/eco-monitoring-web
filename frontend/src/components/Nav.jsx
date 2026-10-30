import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function Nav() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (!user) {
      setUnread(0);
      return;
    }

    let active = true;
    const load = () =>
      api("/api/notifications")
        .then((rows) => {
          if (active) setUnread(rows.filter((n) => !n.read).length);
        })
        .catch(() => {});

    load();
    const timer = setInterval(load, 60_000);
    return () => {
      active = false;
      clearInterval(timer);
    };
  }, [user]);

  return (
    <nav className="nav">
      <Link to="/">Дашборд</Link>
      {user && (
        <>
          <Link to="/history">История</Link>
          <Link to="/map">Карта</Link>
          <Link to="/reports">Отчёты</Link>
          <Link to="/notifications">
            Уведомления{unread > 0 ? ` (${unread})` : ""}
          </Link>
          <Link to="/profile">Профиль</Link>
        </>
      )}
      {user?.role === "ADMIN" && <Link to="/admin">Источники</Link>}
      <span style={{ marginLeft: "auto" }}>
        {user ? (
          <button
            onClick={() => {
              logout();
              navigate("/login");
            }}
          >
            Выйти ({user.email})
          </button>
        ) : (
          <Link to="/login">Войти</Link>
        )}
      </span>
    </nav>
  );
}
