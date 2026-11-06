import { render, screen } from "@testing-library/react";
import { test, expect } from "vitest";
import Footer from "./Footer";

test("рендерит копирайт с текущим годом", () => {
  render(<Footer />);
  const year = new Date().getFullYear();

  expect(screen.getByRole("contentinfo")).toBeInTheDocument();
  expect(
    screen.getByText(`© ${year} Эко-мониторинг. Все права защищены.`),
  ).toBeInTheDocument();
});
