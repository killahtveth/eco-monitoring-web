import { useEffect, useState } from "react";
import { api } from "../api/client";

export default function Profile() {
  const [parameters, setParameters] = useState([]);
  const [profile, setProfile] = useState({
    region: "",
    parameterIds: [],
    refreshIntervalSeconds: 60,
    notificationsEnabled: true,
  });
  const [thresholds, setThresholds] = useState({});
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    // Загружаем список всех параметров
    api("/api/parameters")
      .then(setParameters)
      .catch(() => {});

    // Загружаем настройки профиля (регион, выбранные показатели)
    api("/api/profile")
      .then((data) => {
        setProfile({
          region: data.region || "",
          parameterIds: data.parameterIds || [],
          refreshIntervalSeconds: data.refreshIntervalSeconds || 60,
          notificationsEnabled: data.notificationsEnabled ?? true,
        });
      })
      .catch(() => {});

    // Загружаем пороги уведомлений
    api("/api/thresholds")
      .then((rows) => {
        const map = {};
        rows.forEach((r) => {
          map[r.parameterId] = r.threshold;
        });
        setThresholds(map);
      })
      .catch(() => {});
  }, []);

  const saveProfile = async () => {
    await api("/api/profile", {
      method: "PUT",
      body: JSON.stringify(profile),
    });
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const saveThreshold = async (parameterId, value) => {
    await api("/api/thresholds", {
      method: "PUT",
      body: JSON.stringify({ parameterId, threshold: Number(value) }),
    });
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const toggleParameter = (id) => {
    setProfile((prev) => {
      const exists = prev.parameterIds.includes(id);
      return {
        ...prev,
        parameterIds: exists
          ? prev.parameterIds.filter((pid) => pid !== id)
          : [...prev.parameterIds, id],
      };
    });
  };

  return (
    <div>
      <h1>Профиль и настройки</h1>

      {/* Блок 1: Основные настройки (Закрывает Задачу 5 ТЗ) */}
      <div className="card" style={{ marginBottom: 20, padding: 20 }}>
        <h2>Основные настройки</h2>
        <div style={{ marginBottom: 15 }}>
          <label style={{ display: "block", marginBottom: 5 }}>
            Регион мониторинга:
          </label>
          <input
            type="text"
            value={profile.region}
            onChange={(e) => setProfile({ ...profile, region: e.target.value })}
            placeholder="Например: Ульяновская обл."
            style={{ padding: 8, width: "100%", maxWidth: 300 }}
          />
        </div>

        <div style={{ marginBottom: 15 }}>
          <label>
            <input
              type="checkbox"
              checked={profile.notificationsEnabled}
              onChange={(e) =>
                setProfile({
                  ...profile,
                  notificationsEnabled: e.target.checked,
                })
              }
              style={{ marginRight: 8 }}
            />
            Включить уведомления о превышении порогов
          </label>
        </div>

        <div style={{ marginBottom: 15 }}>
          <label style={{ display: "block", marginBottom: 5 }}>
            Отображаемые показатели на дашборде:
          </label>
          <div style={{ display: "flex", gap: 15, flexWrap: "wrap" }}>
            {parameters.map((p) => (
              <label
                key={p.id}
                style={{
                  fontWeight: profile.parameterIds.includes(p.id)
                    ? "bold"
                    : "normal",
                  cursor: "pointer",
                }}
              >
                <input
                  type="checkbox"
                  checked={profile.parameterIds.includes(p.id)}
                  onChange={() => toggleParameter(p.id)}
                  style={{ marginRight: 5 }}
                />
                {p.code}
              </label>
            ))}
          </div>
        </div>

        <button
          onClick={saveProfile}
          style={{ padding: "8px 16px", cursor: "pointer" }}
        >
          Сохранить настройки профиля
        </button>
      </div>

      {/* Блок 2: Пороги уведомлений */}
      <div className="card" style={{ padding: 20 }}>
        <h2>Пороги уведомлений</h2>
        <table style={{ width: "100%", textAlign: "left" }}>
          <thead>
            <tr>
              <th>Показатель</th>
              <th>Ед. изм.</th>
              <th>Порог</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {parameters.map((p) => (
              <tr key={p.id}>
                <td>{p.code}</td>
                <td>{p.unit}</td>
                <td>
                  <input
                    type="number"
                    value={thresholds[p.id] ?? ""}
                    onChange={(e) =>
                      setThresholds({ ...thresholds, [p.id]: e.target.value })
                    }
                    style={{ width: 80, padding: 4 }}
                  />
                </td>
                <td>
                  <button
                    onClick={() => saveThreshold(p.id, thresholds[p.id])}
                    style={{ padding: "4px 8px" }}
                  >
                    Сохранить
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {saved && (
        <p style={{ color: "green", marginTop: 10, fontWeight: "bold" }}>
          ✓ Успешно сохранено
        </p>
      )}
    </div>
  );
}
