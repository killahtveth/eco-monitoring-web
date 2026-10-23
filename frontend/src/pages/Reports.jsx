import { useEffect, useState } from "react";
import { api } from "../api/client";

function toIso(d) {
  return d.toISOString().slice(0, 19);
}

export default function Reports() {
  const [from, setFrom] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() - 7);
    return toIso(d);
  });
  const [to, setTo] = useState(() => toIso(new Date()));
  const [rows, setRows] = useState([]);
  const [sortKey, setSortKey] = useState("parameter");
  const [sortAsc, setSortAsc] = useState(true);

  const load = () => {
    api(`/api/reports?from=${from}&to=${to}`)
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
        <button onClick={load}>Сформировать</button>
      </div>

      {sorted.length === 0 && <p>Нет данных за период</p>}

      {sorted.length > 0 && (
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
      )}
    </div>
  );
}
