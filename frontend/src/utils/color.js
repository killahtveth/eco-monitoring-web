export function getColor(value, threshold) {
  if (value == null || threshold == null) return "#999";
  if (value >= threshold) return "#ff4d4f";
  if (value >= threshold * 0.8) return "#faad14";
  return "#52c41a";
}
