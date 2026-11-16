import { getColor } from "../utils/color";

export default function MeasurementCard({ m }) {
  const parameter = m.parameter || {};
  const location = m.location || {};
  return (
    <div
      className="card"
      style={{ borderTop: `5px solid ${getColor(m.value, m.threshold)}` }}
    >
      <h2>{parameter.code || "—"}</h2>
      <div className="value-row">
        <span className="value">{m.value}</span>
        <span className="unit">{parameter.unit}</span>
      </div>
      <p className="threshold">
        Порог: {m.threshold} {parameter.unit}
      </p>
      <p className="time">{location.name || ""}</p>
      <p className="time">
        Обновлено: {new Date(m.timestamp).toLocaleTimeString()}
      </p>
    </div>
  );
}