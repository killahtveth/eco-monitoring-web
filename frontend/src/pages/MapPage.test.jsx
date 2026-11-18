import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { test, expect, vi, beforeEach } from "vitest";
import MapPage from "./MapPage";

// Мокаем OpenLayers, чтобы он не пытался рендерить реальный Canvas в jsdom
vi.mock("ol/Map", () => ({
  default: vi.fn(() => ({
    setTarget: vi.fn(),
    getLayers: vi.fn(() => ({
      getArray: vi.fn(() => []),
      removeAt: vi.fn(),
      add: vi.fn(),
    })),
    getOverlays: vi.fn(() => ({
      getArray: vi.fn(() => [{ setPosition: vi.fn() }]),
    })),
    on: vi.fn(),
  })),
}));
vi.mock("ol/View", () => ({ default: vi.fn() }));
vi.mock("ol/layer/Tile", () => ({ default: vi.fn() }));
vi.mock("ol/source/OSM", () => ({ default: vi.fn() }));
vi.mock("ol/layer/Vector", () => ({
  default: vi.fn(() => ({ set: vi.fn() })),
}));
vi.mock("ol/source/Vector", () => ({ default: vi.fn() }));
vi.mock("ol/Feature", () => ({
  default: vi.fn(() => ({
    setStyle: vi.fn(),
    get: vi.fn(),
    getGeometry: vi.fn(() => ({ getCoordinates: vi.fn() })),
  })),
}));
vi.mock("ol/geom/Point", () => ({ default: vi.fn() }));
vi.mock("ol/proj", () => ({ fromLonLat: vi.fn((coords) => coords) }));
vi.mock("ol/style", () => ({
  Style: vi.fn(),
  Circle: vi.fn(),
  Fill: vi.fn(),
  Stroke: vi.fn(),
}));
vi.mock("ol/Overlay", () => ({
  default: vi.fn(() => ({ setPosition: vi.fn() })),
}));

beforeEach(() => {
  vi.stubGlobal("fetch", vi.fn());
  localStorage.setItem("token", "fake");
  localStorage.setItem("user", JSON.stringify({ email: "u@e", role: "USER" }));
});

test("рендерит заголовок и контейнер карты", async () => {
  window.fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });

  render(
    <MemoryRouter>
      <MapPage />
    </MemoryRouter>,
  );

  expect(screen.getByText(/Карта точек мониторинга/i)).toBeInTheDocument();
  expect(screen.getByText(/OpenLayers/i)).toBeInTheDocument();
});
