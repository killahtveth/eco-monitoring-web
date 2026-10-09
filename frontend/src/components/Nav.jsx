import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Nav() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <nav className="nav">
      <Link to="/">Дашборд</Link>
      {user && (
        <>
          <Link to="/history">История</Link>
          <Link to="/map">Карта</Link>
          <Link to="/reports">Отчёты</Link>
          <Link to="/notifications">Уведомления</Link>
          <Link to="/profile">Профиль</Link>
        </>
      )}
      {user?.role === "ADMIN" && <Link to="/admin">Источники</Link>}
      <span style={{ marginLeft: "auto" }}>
        {user ? (
          <button onClick={() => { logout(); navigate("/login"); }}>
            Выйти ({user.email})
          </button>
        ) : (
          <Link to="/login">Войти</Link>
        )}
      </span>
    </nav>
  );
}