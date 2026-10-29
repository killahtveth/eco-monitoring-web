import { render, screen } from '@testing-library/react';
import { test, expect } from 'vitest';
import App from './App';

test('renders the main heading', () => {
  render(<App />);
  expect(screen.getByRole('heading', { name: /Get started/i })).toBeInTheDocument();
});

test('renders the counter button', () => {
  render(<App />);
  expect(screen.getByRole('button', { name: /Count is 0/i })).toBeInTheDocument();
});