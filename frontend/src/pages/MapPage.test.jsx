import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { test, expect, vi, beforeEach } from "vitest";
import MapPage from "./MapPage";

// Мокаем react-leaflet — для unit-теста достаточно проверить, что данные пришли
vi.mock("react-leaflet", () => ({
  MapContainer: ({ children }) => <div data-testid="map">{children}</div>,
  TileLayer: () => null,
  Marker: ({ children, position }) => (
    <div data-testid="marker" data-lat={position[0]} data-lon={position[1]}>
      {children}
    </div>
  ),
  Popup: ({ children }) => <div data-testid="popup">{children}</div>,
}));

vi.mock("leaflet", () => ({
  default: { Icon: { Default: { prototype: {}, mergeOptions: vi.fn() } } },
}));

beforeEach(() => {
  vi.stubGlobal("fetch", vi.fn());
  localStorage.setItem("token", "fake");
  localStorage.setItem("user", JSON.stringify({ email: "u@e", role: "USER" }));
});

test("рендерит маркеры по локациям из API", async () => {
  window.fetch.mockResolvedValueOnce({
    ok: true,
    json: async () => [
      {
        id: 1,
        name: "Центр",
        latitude: 54.3,
        longitude: 48.4,
        measurements: [
          {
            id: 1,
            value: 12.5,
            threshold: 15.0,
            parameter: { code: "PM2.5", unit: "μg/m³" },
          },
        ],
      },
    ],
  });

  render(
    <MemoryRouter>
      <MapPage />
    </MemoryRouter>,
  );

  await waitFor(() => {
    expect(screen.getAllByTestId("marker")).toHaveLength(1);
    expect(screen.getByText(/PM2.5/)).toBeInTheDocument();
    expect(screen.getByText(/12.5/)).toBeInTheDocument();
  });
});
