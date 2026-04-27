import React from 'react';

const StatsCard = ({ label, value, icon, status }) => {
  const getStatusClass = () => {
    if (!status) return '';
    const s = status.toUpperCase();
    if (s === 'LOW') return 'status-low';
    if (s === 'MEDIUM' || s === 'MODERATE') return 'status-medium';
    if (s === 'HIGH') return 'status-high';
    return '';
  };

  return (
    <div className="stat-card">
      <div className="stat-header">
        <span className="stat-icon">{icon}</span>
        <span className="stat-label">{label}</span>
      </div>
      <div className={`stat-value ${getStatusClass()}`}>
        {value}
      </div>
    </div>
  );
};

export default StatsCard;
