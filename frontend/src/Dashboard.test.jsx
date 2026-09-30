import { render, screen, waitFor } from '@testing-library/react';
import { test, expect, vi, beforeEach } from 'vitest';
import App from './App';

// Мокаем fetch
beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn());
});

test('отображает заголовок и подзаголовок', () => {
  window.fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });
  render(<App />);
  expect(screen.getByRole('heading', { name: /Экологический мониторинг/i })).toBeInTheDocument();
  expect(screen.getByText(/Текущие показатели/i)).toBeInTheDocument();
});

test('загружает и отображает карточки показателей', async () => {
  const mockData = [
    { id: 1, parameter: 'PM2.5', value: 12.5, unit: 'мкг/м³', threshold: 35.0, timestamp: new Date().toISOString() },
    { id: 2, parameter: 'CO2', value: 420, unit: 'ppm', threshold: 400.0, timestamp: new Date().toISOString() },
  ];
  window.fetch.mockResolvedValueOnce({ ok: true, json: async () => mockData });

  render(<App />);

  await waitFor(() => {
    expect(screen.getByText('PM2.5')).toBeInTheDocument();
    expect(screen.getByText('CO2')).toBeInTheDocument();
    expect(screen.getByText('12.5')).toBeInTheDocument();
  });
});