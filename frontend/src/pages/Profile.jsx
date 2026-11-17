import { useEffect, useState } from "react";
import { api } from "../api/client";

export default function Profile() {
  const [parameters, setParameters] = useState([]);
  const [thresholds, setThresholds] = useState({});
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    api("/api/parameters")
      .then(setParameters)
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

  const saveThreshold = async (parameterId, value) => {
    await api("/api/thresholds", {
      method: "PUT",
      body: JSON.stringify({ parameterId, threshold: Number(value) }),
    });
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div>
      <h1>Профиль</h1>
      <h2>Пороги уведомлений</h2>
      <table>
        <thead>
          <tr>
            <th>Показатель</th>
            <th>Ед.</th>
            <th>Порог</th>
            <th />
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
                />
              </td>
              <td>
                <button onClick={() => saveThreshold(p.id, thresholds[p.id])}>
                  Сохранить
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {saved && <p style={{ color: "green" }}>Сохранено</p>}
    </div>
  );
}
