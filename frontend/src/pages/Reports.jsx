import { useEffect, useState } from "react";
import { api } from "../api/client";
import { toInstantIso, toLocalInput } from "../utils/datetime";

export default function Reports() {
  const [from, setFrom] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() - 7);
    return toLocalInput(d);
  });
  const [to, setTo] = useState(() => toLocalInput(new Date()));
  const [rows, setRows] = useState([]);
  const [sortKey, setSortKey] = useState("parameter");
  const [sortAsc, setSortAsc] = useState(true);

  const query = () =>
    `from=${encodeURIComponent(toInstantIso(from))}&to=${encodeURIComponent(toInstantIso(to))}`;

  const load = () => {
    api(`/api/reports?${query()}`)
      .then(setRows)
      .catch(() => setRows([]));
  };

  useEffect(() => {
    load();
  }, []);

  const sorted = [...rows].sort((a, b) => {
    const aV = a[sortKey],
      bV = b[sortKey];
    if (aV < bV) return sortAsc ? -1 : 1;
    if (aV > bV) return sortAsc ? 1 : -1;
    return 0;
  });

  const onSort = (key) => {
    if (key === sortKey) setSortAsc(!sortAsc);
    else {
      setSortKey(key);
      setSortAsc(true);
    }
  };

  return (
    <div>
      <h1>Отчёты</h1>
      <div className="toolbar">
        <input
          type="datetime-local"
          value={from}
          onChange={(e) => setFrom(e.target.value)}
        />
        <input
          type="datetime-local"
          value={to}
          onChange={(e) => setTo(e.target.value)}
        />
        <button className="btn-primary" onClick={load}>
          Сформировать
        </button>
      </div>

      {sorted.length === 0 && <p className="empty">Нет данных за период</p>}

      {sorted.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th onClick={() => onSort("parameter")}>Показатель</th>
                <th onClick={() => onSort("unit")}>Ед.</th>
                <th onClick={() => onSort("min")}>Min</th>
                <th onClick={() => onSort("max")}>Max</th>
                <th onClick={() => onSort("avg")}>Avg</th>
                <th onClick={() => onSort("exceeded")}>Превышений</th>
                <th onClick={() => onSort("count")}>Записей</th>
              </tr>
            </thead>
            <tbody>
              {sorted.map((r) => (
                <tr key={r.parameter}>
                  <td>{r.parameter}</td>
                  <td>{r.unit}</td>
                  <td>{r.min.toFixed(2)}</td>
                  <td>{r.max.toFixed(2)}</td>
                  <td>{r.avg.toFixed(2)}</td>
                  <td>{r.exceeded}</td>
                  <td>{r.count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <button
        className="btn-secondary mt"
        onClick={() => {
          const token = localStorage.getItem("token");
          fetch(`http://localhost:8080/api/reports/export.xlsx?${query()}`, {
            headers: token ? { Authorization: `Bearer ${token}` } : {},
          })
            .then((r) => r.blob())
            .then((blob) => {
              const url = URL.createObjectURL(blob);
              const a = document.createElement("a");
              a.href = url;
              a.download = `report-${Date.now()}.xlsx`;
              a.click();
              URL.revokeObjectURL(url);
            });
        }}
      >
        Скачать Excel
      </button>
    </div>
  );
}
