import { useNotifications } from "../context/NotificationsContext";

export default function Notifications() {
  const { items, markRead } = useNotifications();

  return (
    <div>
      <h1>Уведомления</h1>
      {items.length === 0 && <p className="empty">Нет уведомлений</p>}
      {items.length > 0 && (
        <div className="table-wrap">
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
                      className={
                        n.read ? "badge badge--muted" : "badge badge--danger"
                      }
                    >
                      {n.read ? "Прочитано" : "Новое"}
                    </span>
                  </td>
                  <td>{new Date(n.createdAt).toLocaleString()}</td>
                  <td>
                    {!n.read && (
                      <button
                        className="btn-secondary btn-sm"
                        onClick={() => markRead(n.id)}
                      >
                        Отметить прочитанным
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
