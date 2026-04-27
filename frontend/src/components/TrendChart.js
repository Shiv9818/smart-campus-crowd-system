import React, { useState, useEffect } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import axios from 'axios';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const API_BASE_URL = 'http://localhost:5000/api';

// Map hour number → display label
const HOUR_LABELS = {
  8: '8 AM', 9: '9 AM', 10: '10 AM', 11: '11 AM',
  12: '12 PM', 13: '1 PM', 14: '2 PM', 15: '3 PM',
  16: '4 PM', 17: '5 PM', 18: '6 PM', 19: '7 PM',
};

const CHART_OPTIONS = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    mode: 'index',
    intersect: false,
  },
  plugins: {
    legend: {
      display: true,
      position: 'top',
      labels: {
        color: '#94a3b8',
        font: { family: 'Outfit', size: 13, weight: '500' },
        boxWidth: 12,
        padding: 20,
      },
    },
    tooltip: {
      backgroundColor: 'rgba(15, 23, 42, 0.95)',
      borderColor: 'rgba(59, 130, 246, 0.3)',
      borderWidth: 1,
      titleColor: '#f8fafc',
      bodyColor: '#94a3b8',
      padding: 12,
      cornerRadius: 10,
      titleFont: { family: 'Outfit', size: 13, weight: '600' },
      bodyFont: { family: 'Outfit', size: 12 },
      callbacks: {
        label: (ctx) => ` Avg crowd: ${ctx.parsed.y} people`,
      },
    },
  },
  scales: {
    x: {
      grid: {
        color: 'rgba(255, 255, 255, 0.04)',
        drawTicks: false,
      },
      ticks: {
        color: '#64748b',
        font: { family: 'Outfit', size: 12 },
        padding: 8,
      },
      border: { color: 'rgba(255,255,255,0.06)' },
    },
    y: {
      grid: {
        color: 'rgba(255, 255, 255, 0.04)',
        drawTicks: false,
      },
      ticks: {
        color: '#64748b',
        font: { family: 'Outfit', size: 12 },
        padding: 10,
        stepSize: 20,
      },
      border: { color: 'rgba(255,255,255,0.06)', dash: [4, 4] },
      beginAtZero: true,
    },
  },
};

const TrendChart = ({ location }) => {
  const [trendData, setTrendData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(false);

    const fetchTrend = async () => {
      try {
        const encoded = encodeURIComponent(location);
        const res = await axios.get(`${API_BASE_URL}/crowd/trend?location=${encoded}`);
        console.log('[TrendChart] API response for', location, ':', res.data);

        if (!cancelled) {
          setTrendData(Array.isArray(res.data) ? res.data : []);
        }
      } catch (err) {
        console.error('[TrendChart] Failed to fetch trend data:', err);
        if (!cancelled) setError(true);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchTrend();
    return () => { cancelled = true; };
  }, [location]); // re-fetch ONLY when location changes — no auto-refresh

  const hasData = trendData.length > 0 && trendData.some(d => d.avg > 0);

  const chartData = {
    labels: trendData.map(d => HOUR_LABELS[d.hour] ?? `${d.hour}:00`),
    datasets: [
      {
        label: `${location} — 10-day avg crowd`,
        data: trendData.map(d => d.avg),
        borderColor: 'rgba(59, 130, 246, 0.9)',
        backgroundColor: 'rgba(59, 130, 246, 0.08)',
        pointBackgroundColor: 'rgba(59, 130, 246, 0.9)',
        pointBorderColor: 'rgba(255,255,255,0.2)',
        pointBorderWidth: 1,
        pointRadius: 4,
        pointHoverRadius: 7,
        pointHoverBackgroundColor: '#60a5fa',
        tension: 0.4,
        fill: true,
        borderWidth: 2,
      },
    ],
  };

  return (
    <div className="trend-chart-card">
      <div className="trend-chart-header">
        <span className="trend-chart-icon">📈</span>
        <div>
          <h2 className="trend-chart-title">Historical Crowd Trend</h2>
          <p className="trend-chart-subtitle">
            Average crowd per hour over the last 10 days — <strong>{location}</strong>
          </p>
        </div>
      </div>

      <div className="trend-chart-body">
        {loading && (
          <div className="trend-chart-empty">
            <div className="trend-spinner" />
            <span>Loading trend data…</span>
          </div>
        )}

        {!loading && error && (
          <div className="trend-chart-empty">
            <span className="trend-empty-icon">⚠️</span>
            <span>Failed to load trend data. Is the backend running?</span>
          </div>
        )}

        {!loading && !error && !hasData && (
          <div className="trend-chart-empty">
            <span className="trend-empty-icon">📭</span>
            <span>No Data Available for <strong>{location}</strong></span>
            <span className="trend-empty-hint">Historical records will appear once 10 days of data exist.</span>
          </div>
        )}

        {!loading && !error && hasData && (
          <div className="trend-chart-canvas-wrapper">
            <Line data={chartData} options={CHART_OPTIONS} />
          </div>
        )}
      </div>
    </div>
  );
};

export default TrendChart;
