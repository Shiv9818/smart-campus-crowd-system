import React from 'react';
import { ChevronUp, ChevronDown, Minus, ChevronsUp, ChevronsDown } from 'lucide-react';

const StatsCard = ({ label, value, icon, status, isTrend }) => {
  const getStatusClass = () => {
    if (isTrend) {
      const v = value?.toLowerCase() || '';
      if (v.includes('strongly increasing')) return 'trend-strongly-increasing';
      if (v.includes('increasing')) return 'trend-increasing';
      if (v.includes('strongly decreasing')) return 'trend-strongly-decreasing';
      if (v.includes('decreasing')) return 'trend-decreasing';
      return 'trend-stable';
    }

    if (!status) return '';
    const s = status.toUpperCase();
    if (s === 'LOW') return 'status-low';
    if (s === 'MEDIUM' || s === 'MODERATE') return 'status-medium';
    if (s === 'HIGH') return 'status-high';
    return '';
  };

  const getTrendIcon = () => {
    if (!isTrend) return null;
    const v = value?.toLowerCase() || '';
    if (v.includes('strongly increasing')) return <ChevronsUp size={18} className="trend-arrow" />;
    if (v.includes('increasing')) return <ChevronUp size={18} className="trend-arrow" />;
    if (v.includes('strongly decreasing')) return <ChevronsDown size={18} className="trend-arrow" />;
    if (v.includes('decreasing')) return <ChevronDown size={18} className="trend-arrow" />;
    return <Minus size={18} className="trend-arrow" />;
  };

  return (
    <div className="stat-card">
      <div className="stat-header">
        <span className="stat-icon">{icon}</span>
        <span className="stat-label">{label}</span>
      </div>
      <div className={`stat-value ${getStatusClass()}`}>
        {getTrendIcon()}
        {value}
      </div>
    </div>
  );
};

export default StatsCard;
