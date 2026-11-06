import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { test, expect, vi, beforeEach } from "vitest";
import Reports from "./Reports";

beforeEach(() => {
  vi.stubGlobal("fetch", vi.fn());
  localStorage.setItem("token", "fake");
  localStorage.setItem("user", JSON.stringify({ email: "u@e", role: "USER" }));
});

test("рендерит строки отчёта", async () => {
  window.fetch.mockResolvedValueOnce({
    ok: true,
    json: async () => [
      {
        parameter: "PM2.5",
        unit: "μg/m³",
        min: 10,
        max: 30,
        avg: 20,
        exceeded: 2,
        count: 3,
      },
    ],
  });

  render(
    <MemoryRouter>
      <Reports />
    </MemoryRouter>,
  );

  await waitFor(() => {
    expect(screen.getByText("PM2.5")).toBeInTheDocument();
    expect(screen.getByText("10.00")).toBeInTheDocument();
    expect(screen.getByText("30.00")).toBeInTheDocument();
  });

  const url = decodeURIComponent(window.fetch.mock.calls[0][0]);
  expect(url).toMatch(
    /\/api\/reports\?from=\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z&to=\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/,
  );
});

test("показывает пустое состояние", async () => {
  window.fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });
  render(
    <MemoryRouter>
      <Reports />
    </MemoryRouter>,
  );
  await waitFor(() => {
    expect(screen.getByText(/Нет данных за период/)).toBeInTheDocument();
  });
});

test("есть кнопка экспорта", async () => {
  window.fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });
  render(
    <MemoryRouter>
      <Reports />
    </MemoryRouter>,
  );
  expect(screen.getByText(/Скачать Excel/)).toBeInTheDocument();
});
