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

  const groups = new Map();
  items.forEach((m) => {
    const name = m.location?.name || "Без источника";
    if (!groups.has(name)) groups.set(name, []);
    groups.get(name).push(m);
  });

  return (
    <div className="dashboard">
      <h1>Экологический мониторинг</h1>
      <p className="subtitle">Текущие показатели</p>
      {error && <p className="text-error">Ошибка: {error}</p>}
      {items.length === 0 && !error && (
        <p className="empty">Нет свежих данных…</p>
      )}
      <div className="source-groups">
        {[...groups].map(([name, list]) => (
          <section className="source-group" key={name}>
            <h2 className="source-group__title">{name}</h2>
            <div className="cards-container">
              {list.map((m) => (
                <MeasurementCard key={m.id} m={m} />
              ))}
            </div>
          </section>
        ))}
      </div>
    </div>
  );
}
