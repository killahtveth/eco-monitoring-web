import { render, screen } from "@testing-library/react";
import { test, expect } from "vitest";
import App from "./App";

test("renders the main heading", () => {
  render(<App />);
  // Проверяем, что отрисовался главный заголовок
  expect(
    screen.getByRole("heading", { name: /Экологический мониторинг/i }),
  ).toBeInTheDocument();
});

test("renders the subtitle", () => {
  render(<App />);
  // Проверяем подзаголовок
  expect(screen.getByText(/Текущие показатели/i)).toBeInTheDocument();
});
