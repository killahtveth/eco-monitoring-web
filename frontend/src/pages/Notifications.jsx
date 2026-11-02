import { useNotifications } from "../context/NotificationsContext";

export default function Notifications() {
  const { items, markRead } = useNotifications();

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
              <th>Статус</th>
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
                <td>
                  <span
                    style={{
                      color: n.read ? "#888" : "#d4380d",
                      fontWeight: n.read ? 400 : 700,
                    }}
                  >
                    {n.read ? "Прочитано" : "Новое"}
                  </span>
                </td>
                <td>{new Date(n.createdAt).toLocaleString()}</td>
                <td>
                  {!n.read && (
                    <button onClick={() => markRead(n.id)}>
                      Отметить прочитанным
                    </button>
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
