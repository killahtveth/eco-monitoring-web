import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { test, expect, vi, beforeEach } from "vitest";
import { AuthProvider } from "./context/AuthContext";
import App from "./App";

beforeEach(() => {
  vi.stubGlobal("fetch", vi.fn());
  localStorage.setItem("token", "fake");
  localStorage.setItem("user", JSON.stringify({ email: "u@e", role: "USER" }));
});

function renderApp() {
  return render(
    <MemoryRouter initialEntries={["/"]}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </MemoryRouter>,
  );
}

function mockFetch(dashboardData) {
  window.fetch.mockImplementation((url) => {
    const body = String(url).includes("/api/notifications")
      ? []
      : dashboardData;
    return Promise.resolve({ ok: true, json: async () => body });
  });
}

test("отображает заголовок и подзаголовок", () => {
  mockFetch([]);
  renderApp();
  expect(
    screen.getByRole("heading", { name: /Экологический мониторинг/i }),
  ).toBeInTheDocument();
  expect(screen.getByText(/Текущие показатели/i)).toBeInTheDocument();
});

test("загружает и отображает карточки", async () => {
  const data = [
    {
      id: 1,
      parameter: { code: "PM2.5", unit: "μg/m³" },
      location: { name: "Ulyanovsk" },
      value: 12.5,
      threshold: 15.0,
      timestamp: new Date().toISOString(),
    },
  ];
  mockFetch(data);
  renderApp();

  await waitFor(() => {
    expect(screen.getByText("PM2.5")).toBeInTheDocument();
    expect(screen.getByText("12.5")).toBeInTheDocument();
  });
});
