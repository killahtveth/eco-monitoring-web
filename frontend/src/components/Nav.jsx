import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useNotifications } from "../context/NotificationsContext";
import Logo from "./Logo";

const navClass = ({ isActive }) =>
  isActive ? "nav-link nav-link--active" : "nav-link";

export default function Nav() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { unread } = useNotifications();

  return (
    <nav className="nav">
      <Link to="/" className="nav-brand">
        <Logo className="nav-logo" />
        <span>Эко-мониторинг</span>
      </Link>
      <div className="nav-links">
        <NavLink to="/" end className={navClass}>
          Дашборд
        </NavLink>
        {user && (
          <>
            <NavLink to="/history" className={navClass}>
              История
            </NavLink>
            <NavLink to="/map" className={navClass}>
              Карта
            </NavLink>
            <NavLink to="/reports" className={navClass}>
              Отчёты
            </NavLink>
            <NavLink to="/notifications" className={navClass}>
              Уведомления{unread > 0 ? ` (${unread})` : ""}
            </NavLink>
            <NavLink to="/profile" className={navClass}>
              Профиль
            </NavLink>
          </>
        )}
        {user?.role === "ADMIN" && (
          <NavLink to="/admin" className={navClass}>
            Источники
          </NavLink>
        )}
      </div>
      <div className="nav-user">
        {user ? (
          <>
            <span className="nav-user__email">{user.email}</span>
            <button
              className="btn-ghost btn-sm"
              onClick={() => {
                logout();
                navigate("/login");
              }}
            >
              Выйти
            </button>
          </>
        ) : (
          <NavLink to="/login" className={navClass}>
            Войти
          </NavLink>
        )}
      </div>
    </nav>
  );
}
