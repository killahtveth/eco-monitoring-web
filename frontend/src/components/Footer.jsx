export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="footer">
      <p>© {year} Эко-мониторинг. Все права защищены.</p>
    </footer>
  );
}
