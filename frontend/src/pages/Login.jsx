import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);

  const submit = async (e) => {
    e.preventDefault();
    try {
      await login(email, password);
      navigate("/");
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="auth-page">
      <h1>Вход</h1>
      <form onSubmit={submit}>
        <input
          type="email" placeholder="Email"
          value={email} onChange={(e) => setEmail(e.target.value)} required
        />
        <input
          type="password" placeholder="Пароль"
          value={password} onChange={(e) => setPassword(e.target.value)} required
        />
        <button type="submit">Войти</button>
        {error && <p style={{ color: "red" }}>{error}</p>}
      </form>
      <p>
        Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
      </p>
    </div>
  );
}