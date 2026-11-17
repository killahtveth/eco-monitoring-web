import { useEffect, useState } from "react";
import { api } from "../api/client";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

const RANGES = { "24h": 1, "7d": 7, "30d": 30 };

export default function History() {
  const [locations, setLocations] = useState([]);
  const [parameters, setParameters] = useState([]);
  const [locId, setLocId] = useState("");
  const [paramId, setParamId] = useState("");
  const [range, setRange] = useState("24h");
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api("/api/locations")
      .then(setLocations)
      .catch(() => {});
    api("/api/parameters")
      .then(setParameters)
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!locId || !paramId) return;
    const to = new Date();
    const from = new Date(to.getTime() - RANGES[range] * 86400000);
    setLoading(true);
    api(
      `/api/history?locationId=${locId}&parameterId=${paramId}` +
        `&from=${from.toISOString()}&to=${to.toISOString()}`,
    )
      .then((rows) =>
        setData(
          rows.map((r) => ({
            ts: new Date(r.ts).toLocaleString(),
            value: r.value,
          })),
        ),
      )
      .catch(() => setData([]))
      .finally(() => setLoading(false));
  }, [locId, paramId, range]);

  return (
    <div>
      <h1>История измерений</h1>
      <div className="toolbar">
        <select value={locId} onChange={(e) => setLocId(e.target.value)}>
          <option value="">Локация…</option>
          {locations.map((l) => (
            <option key={l.id} value={l.id}>
              {l.name}
            </option>
          ))}
        </select>
        <select value={paramId} onChange={(e) => setParamId(e.target.value)}>
          <option value="">Показатель…</option>
          {parameters.map((p) => (
            <option key={p.id} value={p.id}>
              {p.code} ({p.unit})
            </option>
          ))}
        </select>
        <select value={range} onChange={(e) => setRange(e.target.value)}>
          <option value="24h">Сутки</option>
          <option value="7d">Неделя</option>
          <option value="30d">Месяц</option>
        </select>
      </div>

      {loading && <p>Загрузка…</p>}
      {!loading && data.length === 0 && locId && paramId && (
        <p>Нет данных за период</p>
      )}

      {data.length > 0 && (
        <ResponsiveContainer width="100%" height={420}>
          <LineChart data={data}>
            <CartesianGrid stroke="#eee" />
            <XAxis dataKey="ts" hide />
            <YAxis />
            <Tooltip />
            <Line
              type="monotone"
              dataKey="value"
              stroke="#52c41a"
              dot={false}
            />
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}
