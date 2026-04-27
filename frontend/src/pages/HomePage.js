import React from 'react';
import { useNavigate } from 'react-router-dom';
import { LayoutDashboard, Activity, Clock, Shield } from 'lucide-react';

const features = [
  {
    icon: <Activity size={28} />,
    title: 'Real-Time Monitoring',
    desc: 'Live crowd counts updated every 15 seconds across all campus locations.',
  },
  {
    icon: <Clock size={28} />,
    title: 'Smart Waiting Time',
    desc: 'Location-aware waiting time estimation using seat-based and queue-based models.',
  },
  {
    icon: <LayoutDashboard size={28} />,
    title: 'Predictive Analytics',
    desc: 'Blended ML-style prediction using historical averages and live trend signals.',
  },
  {
    icon: <Shield size={28} />,
    title: 'Best Time Guidance',
    desc: 'Deterministic best-hour recommendation derived from 30-day historical patterns.',
  },
];

const HomePage = () => {
  const navigate = useNavigate();

  return (
    <div className="page home-page">

      {/* Hero */}
      <section className="hero">
        <div className="hero-eyebrow">
          <span className="hero-dot" />
          Smart Campus — Crowd Intelligence System
        </div>
        <h1 className="hero-title">
          Know Your Campus,<br />
          <span className="hero-accent">Before You Go.</span>
        </h1>
        <p className="hero-sub">
          Real-time crowd monitoring, predictive analytics, and waiting-time estimation
          for Library, Canteen, and Fees Office — powered by Spring Boot and React.
        </p>
        <div className="hero-actions">
          <button className="btn-primary" onClick={() => navigate('/dashboard')}>
            <LayoutDashboard size={18} />
            Get Started
          </button>
          <button className="btn-ghost" onClick={() => navigate('/about')}>
            Learn How It Works
          </button>
        </div>
      </section>

      {/* Features */}
      <section className="features-section">
        <h2 className="section-heading">What It Does</h2>
        <div className="features-grid">
          {features.map((f, i) => (
            <div className="feature-card" key={i}>
              <div className="feature-icon">{f.icon}</div>
              <h3 className="feature-title">{f.title}</h3>
              <p className="feature-desc">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Locations strip */}
      <section className="locations-strip">
        <h2 className="section-heading">Monitored Locations</h2>
        <div className="locations-row">
          {['📚 Library', '🍽️ Canteen', '🏦 Fees Office'].map((loc) => (
            <div className="location-chip" key={loc}>{loc}</div>
          ))}
        </div>
      </section>

    </div>
  );
};

export default HomePage;
