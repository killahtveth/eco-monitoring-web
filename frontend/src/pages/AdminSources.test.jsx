import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { test, expect, vi, beforeEach } from "vitest";
import AdminSources from "./AdminSources";

beforeEach(() => {
  vi.stubGlobal("fetch", vi.fn());
  localStorage.setItem("token", "fake");
  localStorage.setItem("user", JSON.stringify({ email: "a@e", role: "ADMIN" }));
});

test("рендерит список источников", async () => {
  window.fetch.mockResolvedValueOnce({
    ok: true,
    json: async () => [
      {
        id: 1,
        name: "Open-Meteo Ulyanovsk",
        type: "OPEN_METEO",
        url: "54.3,48.4",
        status: "ACTIVE",
        active: true,
      },
    ],
  });

  render(
    <MemoryRouter>
      <AdminSources />
    </MemoryRouter>,
  );

  await waitFor(() => {
    expect(screen.getByText("Open-Meteo Ulyanovsk")).toBeInTheDocument();
    expect(screen.getByText("ACTIVE")).toBeInTheDocument();
  });
});
