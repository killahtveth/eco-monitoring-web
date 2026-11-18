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
    api("/api/parameters")
      .then(setParameters)
      .catch(() => {});

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

      <section className="panel">
        <h2>Основные настройки</h2>
        <div className="form-field">
          <label className="field-label" htmlFor="region">
            Регион мониторинга:
          </label>
          <input
            id="region"
            className="input-region"
            type="text"
            value={profile.region}
            onChange={(e) => setProfile({ ...profile, region: e.target.value })}
            placeholder="Например: Ульяновская обл."
          />
        </div>

        <label className="check-row">
          <input
            type="checkbox"
            checked={profile.notificationsEnabled}
            onChange={(e) =>
              setProfile({
                ...profile,
                notificationsEnabled: e.target.checked,
              })
            }
          />
          Включить уведомления о превышении порогов
        </label>

        <div className="form-field">
          <span className="field-label">
            Отображаемые показатели на дашборде:
          </span>
          <div className="check-grid">
            {parameters.map((p) => (
              <label
                key={p.id}
                className={
                  profile.parameterIds.includes(p.id) ? "is-selected" : ""
                }
              >
                <input
                  type="checkbox"
                  checked={profile.parameterIds.includes(p.id)}
                  onChange={() => toggleParameter(p.id)}
                />
                {p.code}
              </label>
            ))}
          </div>
        </div>

        <button className="btn-primary" onClick={saveProfile}>
          Сохранить настройки профиля
        </button>
      </section>

      <section className="panel">
        <h2>Пороги уведомлений</h2>
        <p className="hint">
          Если порог не указан, используется нормативное значение из справочника
          (колонка «Норматив»). Чтобы сбросить свой порог и вернуться к
          нормативу, очистите поле и нажмите «Сохранить».
        </p>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Показатель</th>
                <th>Ед. изм.</th>
                <th>Норматив</th>
                <th>Ваш порог</th>
                <th />
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
                        className="input-threshold"
                        type="number"
                        placeholder={String(p.defaultThreshold)}
                        value={value}
                        onChange={(e) =>
                          setThresholds({
                            ...thresholds,
                            [p.id]: e.target.value,
                          })
                        }
                      />
                    </td>
                    <td>
                      <button
                        className="btn-secondary btn-sm"
                        onClick={() => saveThreshold(p.id, thresholds[p.id])}
                        disabled={invalid}
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
      </section>

      {saved && <p className="save-status">✓ Успешно сохранено</p>}
    </div>
  );
}
