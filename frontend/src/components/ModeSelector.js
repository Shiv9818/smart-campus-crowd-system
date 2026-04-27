import React from 'react';

const ModeSelector = ({ mode, setMode }) => {
  const modes = ['LIVE', 'SIMULATED'];

  return (
    <div className="mode-selector">
      <label className="selector-label">System Mode</label>
      <div className="mode-btn-group">
        {modes.map((m) => (
          <button
            key={m}
            className={`mode-btn ${mode === m ? 'active' : ''}`}
            onClick={() => setMode(m)}
          >
            {m}
          </button>
        ))}
      </div>
    </div>
  );
};

export default ModeSelector;
