import React from 'react';

const LocationSelector = ({ location, setLocation }) => {
  const locations = [
    'Library',
    'Canteen',
    'Fees Office'
  ];

  return (
    <div className="location-selector">
      <label className="selector-label">Location</label>
      <div className="location-btn-group">
        {locations.map((loc) => (
          <button
            key={loc}
            className={`location-btn ${location === loc ? 'active' : ''}`}
            onClick={() => setLocation(loc)}
          >
            {loc}
          </button>
        ))}
      </div>
    </div>
  );
};

export default LocationSelector;
