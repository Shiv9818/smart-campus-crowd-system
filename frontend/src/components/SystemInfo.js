import React from 'react';

const SystemInfo = ({ mode }) => {
  return (
    <div className="system-info-panel">
      <div className="info-item">
        <label>System Mode</label>
        <span>{mode}</span>
      </div>
      <div className="info-item">
        <label>Update Interval</label>
        <span>15 Seconds</span>
      </div>
      <div className="info-item">
        <label>Data Source</label>
        <span>Simulation</span>
      </div>
      <div className="info-item">
        <label>Backend</label>
        <span>Spring Boot</span>
      </div>
      <div className="info-item">
        <label>Database</label>
        <span>MySQL</span>
      </div>
    </div>
  );
};

export default SystemInfo;
