import { useEffect, useState } from "react";
import { api } from "../api/client";

export default function Notifications() {
  const [items, setItems] = useState([]);

  const load = () =>
    api("/api/notifications")
      .then(setItems)
      .catch(() => {});

  useEffect(() => {
    load();
  }, []);

  const markRead = async (id) => {
    await api(`/api/notifications/${id}/read`, { method: "POST" });
    load();
  };

  return (
    <div>
      <h1>Уведомления</h1>
      {items.length === 0 && <p>Нет уведомлений</p>}
      {items.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>Показатель</th>
              <th>Значение</th>
              <th>Порог</th>
              <th>Время</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {items.map((n) => (
              <tr key={n.id} style={{ fontWeight: n.read ? 400 : 700 }}>
                <td>{n.parameter}</td>
                <td>
                  {n.value} {n.unit}
                </td>
                <td>
                  {n.threshold} {n.unit}
                </td>
                <td>{new Date(n.createdAt).toLocaleString()}</td>
                <td>
                  {!n.read && (
                    <button onClick={() => markRead(n.id)}>Прочитано</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
