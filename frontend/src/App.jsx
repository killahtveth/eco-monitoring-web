import { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [measurements, setMeasurements] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Запрос к нашему Spring Boot бэкенду
    fetch('http://localhost:8080/api/dashboard/current')
      .then((response) => {
        if (!response.ok) throw new Error('Ошибка сети');
        return response.json();
      })
      .then((data) => setMeasurements(data))
      .catch((err) => setError(err.message));
  }, []);

  // Функция для определения цвета карточки
  const getCardColor = (value, threshold) => {
    if (value >= threshold) return '#ff4d4f'; // Красный (превышение)
    if (value >= threshold * 0.8) return '#faad14'; // Желтый (предупреждение)
    return '#52c41a'; // Зеленый (норма)
  };

  return (
    <div className="dashboard">
      <h1>Экологический мониторинг</h1>
      <p className="subtitle">Текущие показатели</p>

      {error && <p style={{ color: 'red' }}>Ошибка: {error}. Запущен ли бэкенд?</p>}

      <div className="cards-container">
        {measurements.map((m) => (
          <div 
            key={m.id} 
            className="card"
            style={{ borderTop: `5px solid ${getCardColor(m.value, m.threshold)}` }}
          >
            <h2>{m.parameter}</h2>
            <div className="value-row">
              <span className="value">{m.value}</span>
              <span className="unit">{m.unit}</span>
            </div>
            <p className="threshold">Порог: {m.threshold} {m.unit}</p>
            <p className="time">Обновлено: {new Date(m.timestamp).toLocaleTimeString()}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;