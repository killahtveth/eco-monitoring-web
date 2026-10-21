import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { test, expect, vi, beforeEach } from "vitest";
import History from "./History";

beforeEach(() => {
  vi.stubGlobal("fetch", vi.fn());
  localStorage.setItem("token", "fake");
  localStorage.setItem("user", JSON.stringify({ email: "u@e", role: "USER" }));
});

test("показывает пустое состояние без выбора", () => {
  window.fetch.mockResolvedValue({ ok: true, json: async () => [] });
  render(
    <MemoryRouter>
      <History />
    </MemoryRouter>,
  );
  expect(
    screen.getByRole("heading", { name: /История измерений/i }),
  ).toBeInTheDocument();
});

test("загружает локации и параметры", async () => {
  window.fetch
    .mockResolvedValueOnce({
      ok: true,
      json: async () => [{ id: 1, name: "Ulyanovsk" }],
    })
    .mockResolvedValueOnce({
      ok: true,
      json: async () => [{ id: 10, code: "PM2.5", unit: "μg/m³" }],
    });

  render(
    <MemoryRouter>
      <History />
    </MemoryRouter>,
  );

  await waitFor(() => {
    expect(screen.getByText("Ulyanovsk")).toBeInTheDocument();
    expect(screen.getByText(/PM2.5/)).toBeInTheDocument();
  });
});
