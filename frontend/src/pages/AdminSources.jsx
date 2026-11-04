import { useEffect, useState } from "react";
import { api } from "../api/client";

const STATUS_CLASS = {
  ACTIVE: "badge badge--success",
  ERROR: "badge badge--danger",
  DISABLED: "badge badge--muted",
};

export default function AdminSources() {
  const [sources, setSources] = useState([]);
  const [form, setForm] = useState({
    name: "",
    type: "OPEN_METEO",
    url: "54.3142,48.4031",
    intervalMinutes: 60,
  });

  const load = () =>
    api("/api/admin/sources")
      .then(setSources)
      .catch(() => {});
  useEffect(() => {
    load();
  }, []);

  const create = async (e) => {
    e.preventDefault();
    await api("/api/admin/sources", {
      method: "POST",
      body: JSON.stringify(form),
    });
    setForm({
      name: "",
      type: "OPEN_METEO",
      url: "54.3142,48.4031",
      intervalMinutes: 60,
    });
    load();
  };

  const toggle = async (id) => {
    await api(`/api/admin/sources/${id}/toggle`, { method: "POST" });
    load();
  };

  const run = async (id) => {
    const r = await api(`/api/admin/sources/${id}/run`, { method: "POST" });
    if (r.lastError) {
      alert(`Ошибка сбора: ${r.lastError}`);
    } else {
      alert(
        r.saved === 0
          ? "Новых данных нет — дубликаты пропущены"
          : `Сохранено измерений: ${r.saved}`,
      );
    }
    load();
  };

  return (
    <div>
      <h1>Источники данных</h1>
      <section className="panel">
        <h2>Новый источник</h2>
        <form onSubmit={create} className="toolbar toolbar--flush">
          <input
            placeholder="Имя"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
          <input
            placeholder="URL или lat,lon"
            value={form.url}
            onChange={(e) => setForm({ ...form, url: e.target.value })}
            required
          />
          <input
            type="number"
            placeholder="Интервал, мин"
            value={form.intervalMinutes}
            onChange={(e) =>
              setForm({ ...form, intervalMinutes: Number(e.target.value) })
            }
          />
          <button type="submit">Добавить</button>
        </form>
      </section>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Имя</th>
              <th>Тип</th>
              <th>URL</th>
              <th>Статус</th>
              <th>Последний сбор</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {sources.map((s) => (
              <tr key={s.id}>
                <td>{s.name}</td>
                <td>{s.type}</td>
                <td>{s.url}</td>
                <td>
                  <span className={STATUS_CLASS[s.status] || "badge"}>
                    {s.status}
                  </span>
                  {s.lastError && <span title={s.lastError}> ⚠</span>}
                </td>
                <td>
                  {s.lastFetchAt
                    ? new Date(s.lastFetchAt).toLocaleString()
                    : "—"}
                </td>
                <td>
                  <div className="row-actions">
                    <button
                      className="btn-secondary btn-sm"
                      onClick={() => toggle(s.id)}
                    >
                      {s.active ? "Отключить" : "Включить"}
                    </button>
                    <button
                      className="btn-primary btn-sm"
                      onClick={() => run(s.id)}
                    >
                      Запустить
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
