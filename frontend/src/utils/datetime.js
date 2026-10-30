function pad(n) {
  return String(n).padStart(2, "0");
}

export function toLocalInput(date) {
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  );
}

export function toInstantIso(localValue) {
  return new Date(localValue).toISOString();
}
