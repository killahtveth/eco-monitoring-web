import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { test, expect, vi, beforeEach } from "vitest";
import { AuthProvider } from "../context/AuthContext";
import { NotificationsProvider } from "../context/NotificationsContext";
import Notifications from "./Notifications";

beforeEach(() => {
  vi.stubGlobal("fetch", vi.fn());
  localStorage.setItem("token", "fake");
  localStorage.setItem("user", JSON.stringify({ email: "u@e", role: "USER" }));
});

function renderPage() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <NotificationsProvider>
          <Notifications />
        </NotificationsProvider>
      </AuthProvider>
    </MemoryRouter>,
  );
}

test("показывает уведомления из API со статусом «Новое»", async () => {
  window.fetch.mockResolvedValueOnce({
    ok: true,
    json: async () => [
      {
        id: 1,
        parameter: "PM2.5",
        unit: "μg/m³",
        value: 99.0,
        threshold: 15.0,
        createdAt: new Date().toISOString(),
        read: false,
      },
    ],
  });

  renderPage();

  await waitFor(() => {
    expect(screen.getByText("PM2.5")).toBeInTheDocument();
    expect(screen.getByText(/99/)).toBeInTheDocument();
    expect(screen.getByText("Новое")).toBeInTheDocument();
  });
});

test("показывает прочитанные уведомления", async () => {
  window.fetch.mockResolvedValueOnce({
    ok: true,
    json: async () => [
      {
        id: 2,
        parameter: "PM10",
        unit: "μg/m³",
        value: 45.0,
        threshold: 8.0,
        createdAt: new Date().toISOString(),
        read: true,
      },
    ],
  });

  renderPage();

  await waitFor(() => {
    expect(screen.getByText("PM10")).toBeInTheDocument();
    expect(screen.getByText("Прочитано")).toBeInTheDocument();
  });
});

test("показывает пустое состояние", async () => {
  window.fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });
  renderPage();
  await waitFor(() => {
    expect(screen.getByText(/Нет уведомлений/)).toBeInTheDocument();
  });
});
