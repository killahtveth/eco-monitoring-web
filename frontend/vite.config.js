import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: "./src/setupTests.js",
    coverage: {
      provider: "v8",
      reporter: ["text", "html", "lcov"],
      include: ["src/**/*.{js,jsx}"],
      exclude: [
            "node_modules/",
            "src/setupTests.js",
            "src/main.jsx",           // точка входа — не тестируется
            "src/api/client.js",      // сетевой клиент — мокируется
            "**/*.test.{js,jsx}",     // сами тесты
            "**/*.config.{js,jsx}",   // конфиги
        ],
    },
  },
});
