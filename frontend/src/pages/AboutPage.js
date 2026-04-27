import React from 'react';
import { Database, Server, Cpu, BarChart2, RefreshCw, Shield } from 'lucide-react';

const TECH_STACK = [
  { icon: <Server size={22} />,    label: 'Backend',    value: 'Spring Boot (Java)' },
  { icon: <Database size={22} />,  label: 'Database',   value: 'MySQL' },
  { icon: <Cpu size={22} />,       label: 'Frontend',   value: 'React 18' },
  { icon: <BarChart2 size={22} />, label: 'Charts',     value: 'Chart.js + react-chartjs-2' },
  { icon: <RefreshCw size={22} />, label: 'Polling',    value: '15-second interval (SIMULATED mode)' },
  { icon: <Shield size={22} />,    label: 'Routing',    value: 'React Router v6' },
];

const FLOW_STEPS = [
  { step: '01', title: 'Simulation Engine', body: 'A Spring Boot @Scheduled task runs every 15 seconds and generates crowd counts for Library, Canteen, and Fees Office based on time-of-day and day-of-week multipliers.' },
  { step: '02', title: 'REST API', body: 'The /api/crowd/current endpoint returns the latest crowd count, status, trend, best time, prediction, and waiting time as a single JSON response.' },
  { step: '03', title: 'React Polling', body: 'The React frontend polls the API every 15 seconds in SIMULATED mode and updates all stat cards without a page refresh.' },
  { step: '04', title: 'Waiting Time Calculation', body: 'Library and Canteen use a seat-based model (capacity 60, service rate 5 ppl/min). Fees Office uses a single-server queue model (2 min/person).' },
  { step: '05', title: 'Analytics & Prediction', body: 'Prediction blends 70% current crowd + 30% historical average, adjusted by a trend signal derived from the last 3 readings, clamped within ±30 of current.' },
  { step: '06', title: 'Best Time', body: 'Deterministic — always returns the hour with the lowest average crowd for the current day-of-week, calculated directly from 30 days of historical records.' },
];

const AboutPage = () => {
  return (
    <div className="page about-page">

      {/* Page header */}
      <div className="page-header">
        <h1 className="page-title">About the System</h1>
        <p className="page-subtitle">
          A full-stack crowd intelligence platform built to monitor, predict, and visualise
          campus foot traffic in real time.
        </p>
      </div>

      {/* Tech Stack */}
      <section className="about-section">
        <h2 className="about-section-title">Technology Stack</h2>
        <div className="tech-grid">
          {TECH_STACK.map((t, i) => (
            <div className="tech-card" key={i}>
              <div className="tech-icon">{t.icon}</div>
              <div>
                <div className="tech-label">{t.label}</div>
                <div className="tech-value">{t.value}</div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* How It Works */}
      <section className="about-section">
        <h2 className="about-section-title">How It Works</h2>
        <div className="flow-list">
          {FLOW_STEPS.map((s) => (
            <div className="flow-item" key={s.step}>
              <div className="flow-step-badge">{s.step}</div>
              <div className="flow-content">
                <h3 className="flow-title">{s.title}</h3>
                <p className="flow-body">{s.body}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Project context */}
      <section className="about-section">
        <div className="about-callout">
          <h3>Project Context</h3>
          <p>
            This system was built as an academic project to demonstrate intelligent
            IoT-inspired crowd management for smart campuses. All data in SIMULATED mode
            is generated algorithmically — no physical sensors are required. The architecture
            is designed to be extended with real CCTV-based crowd detection in future iterations.
          </p>
        </div>
      </section>

    </div>
  );
};

export default AboutPage;
