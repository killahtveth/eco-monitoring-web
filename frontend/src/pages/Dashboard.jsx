import { useEffect, useState } from "react";
import { api } from "../api/client";
import MeasurementCard from "../components/MeasurementCard";

export default function Dashboard() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState(null);

  const load = async () => {
    try {
      const data = await api("/api/dashboard/current");
      setItems(data);
      setError(null);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    load();
    const t = setInterval(load, 60_000);
    return () => clearInterval(t);
  }, []);

  return (
    <div className="dashboard">
      <h1>Экологический мониторинг</h1>
      <p className="subtitle">Текущие показатели</p>
      {error && <p style={{ color: "red" }}>Ошибка: {error}</p>}
      {items.length === 0 && !error && <p>Нет свежих данных…</p>}
      <div className="cards-container">
        {items.map((m) => (
          <MeasurementCard key={m.id} m={m} />
        ))}
      </div>
    </div>
  );
}