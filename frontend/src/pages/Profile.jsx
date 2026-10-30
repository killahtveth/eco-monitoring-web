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

  const parseThreshold = (value) => {
    if (value === "" || value == null) return null;
    const parsed = Number(value);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : NaN;
  };

  const saveThreshold = async (parameterId, value) => {
    const parsed = parseThreshold(value);
    if (Number.isNaN(parsed)) return;

    const result = await api("/api/thresholds", {
      method: "PUT",
      body: JSON.stringify({ parameterId, threshold: parsed }),
    });

    setThresholds((prev) => {
      const next = { ...prev };
      if (result.threshold == null) {
        delete next[parameterId];
      } else {
        next[parameterId] = result.threshold;
      }
      return next;
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
        <p style={{ color: "#666", maxWidth: 720 }}>
          Если порог не указан, используется нормативное значение из справочника
          (колонка «Норматив»). Чтобы сбросить свой порог и вернуться к
          нормативу, очистите поле и нажмите «Сохранить».
        </p>
        <table style={{ width: "100%", textAlign: "left" }}>
          <thead>
            <tr>
              <th>Показатель</th>
              <th>Ед. изм.</th>
              <th>Норматив</th>
              <th>Ваш порог</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {parameters.map((p) => {
              const value = thresholds[p.id] ?? "";
              const invalid =
                value !== "" && value != null && !(Number(value) > 0);

              return (
                <tr key={p.id}>
                  <td>{p.code}</td>
                  <td>{p.unit}</td>
                  <td>
                    {p.defaultThreshold} {p.unit}
                  </td>
                  <td>
                    <input
                      type="number"
                      placeholder={String(p.defaultThreshold)}
                      value={value}
                      onChange={(e) =>
                        setThresholds({ ...thresholds, [p.id]: e.target.value })
                      }
                      style={{ width: 80, padding: 4 }}
                    />
                  </td>
                  <td>
                    <button
                      onClick={() => saveThreshold(p.id, thresholds[p.id])}
                      disabled={invalid}
                      style={{ padding: "4px 8px" }}
                    >
                      Сохранить
                    </button>
                  </td>
                </tr>
              );
            })}
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
