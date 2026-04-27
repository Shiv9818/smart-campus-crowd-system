import React from 'react';
import LocationSelector from '../components/LocationSelector';
import StatsCard from '../components/StatsCard';
import { Zap, Timer, TrendingUp, Clock, Info } from 'lucide-react';

/* Explanation of each prediction metric */
const MODEL_CARDS = [
  {
    icon: <Zap size={22} />,
    title: 'Prediction Formula',
    body: 'prediction = (0.7 × current crowd) + (0.3 × historical avg) + trend adjustment',
    note: 'Clamped 0–120 and bounded within ±30 of the current count.',
  },
  {
    icon: <TrendingUp size={22} />,
    title: 'Trend Adjustment',
    body: 'Consecutively rising readings → +5 to +10. Consecutively falling → −5 to −10.',
    note: 'Based on the last 3 simulation ticks.',
  },
  {
    icon: <Timer size={22} />,
    title: 'Waiting Time — Library / Canteen',
    body: 'waitingTime = max(0, crowd − 60) ÷ 5 minutes',
    note: 'Capacity: 60 seats. Service rate: 5 people/min.',
  },
  {
    icon: <Clock size={22} />,
    title: 'Waiting Time — Fees Office',
    body: 'waitingTime = crowd × 2 minutes',
    note: 'Single-server queue. 2 min per person.',
  },
];

const PredictionPage = ({
  location, setLocation,
  crowdData,
}) => {
  return (
    <div className="page prediction-page">

      {/* Page header */}
      <div className="page-header">
        <h1 className="page-title">
          <Zap size={28} className="page-title-icon" />
          Prediction & Waiting Time
        </h1>
        <p className="page-subtitle">
          Understand how the system estimates next-hour crowd levels and waiting times for each location.
        </p>
      </div>

      {/* Location picker */}
      <div className="controls-panel controls-panel--slim">
        <LocationSelector location={location} setLocation={setLocation} />
      </div>

      {/* Live prediction metrics */}
      <div className="pred-metrics">
        <StatsCard
          label="Prediction (next hour)"
          value={crowdData?.prediction ?? '...'}
          icon={<Zap size={20} />}
        />
        <StatsCard
          label="Estimated Waiting Time"
          value={crowdData ? `${crowdData.waitingTime} mins` : '...'}
          icon={<Timer size={20} />}
        />
        <StatsCard
          label="Current Trend"
          value={crowdData?.trend ?? '...'}
          icon={<TrendingUp size={20} />}
        />
        <StatsCard
          label="Best Time to Visit"
          value={crowdData?.bestTime ?? '...'}
          icon={<Clock size={20} />}
        />
      </div>

      {/* Model explanation cards */}
      <section className="model-section">
        <div className="model-section-header">
          <Info size={18} />
          <h2>How the Models Work</h2>
        </div>
        <div className="model-grid">
          {MODEL_CARDS.map((m, i) => (
            <div className="model-card" key={i}>
              <div className="model-card-icon">{m.icon}</div>
              <h3 className="model-card-title">{m.title}</h3>
              <p className="model-card-body">{m.body}</p>
              <p className="model-card-note">{m.note}</p>
            </div>
          ))}
        </div>
      </section>

    </div>
  );
};

export default PredictionPage;
