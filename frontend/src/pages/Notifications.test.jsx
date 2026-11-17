import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { test, expect, vi, beforeEach } from "vitest";
import Notifications from "./Notifications";

beforeEach(() => {
  vi.stubGlobal("fetch", vi.fn());
  localStorage.setItem("token", "fake");
  localStorage.setItem("user", JSON.stringify({ email: "u@e", role: "USER" }));
});

test("показывает уведомления из API", async () => {
  window.fetch.mockResolvedValueOnce({
    ok: true,
    json: async () => [
      {
        id: 1,
        parameter: "PM2.5",
        unit: "μg/m³",
        value: 99.0, // <-- Изменили с 20.0 на 99.0, чтобы избежать совпадения с "2026" в дате
        threshold: 15.0,
        createdAt: new Date().toISOString(),
        read: false,
      },
    ],
  });

  render(
    <MemoryRouter>
      <Notifications />
    </MemoryRouter>,
  );

  await waitFor(() => {
    expect(screen.getByText("PM2.5")).toBeInTheDocument();
    expect(screen.getByText(/99/)).toBeInTheDocument(); // <-- Теперь ищем уникальное значение 99
  });
});

test("показывает пустое состояние", async () => {
  window.fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });
  render(
    <MemoryRouter>
      <Notifications />
    </MemoryRouter>,
  );
  await waitFor(() => {
    expect(screen.getByText(/Нет уведомлений/)).toBeInTheDocument();
  });
});
