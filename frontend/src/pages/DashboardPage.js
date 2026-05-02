import React from 'react';
import LocationSelector from '../components/LocationSelector';
import ModeSelector from '../components/ModeSelector';
import StatsCard from '../components/StatsCard';
import SystemInfo from '../components/SystemInfo';

import { Users, Activity, TrendingUp, Clock, Zap, Timer } from 'lucide-react';

const DashboardPage = ({
  location, setLocation,
  mode, setMode,
  crowdData, historicalStats,
  historyData, loading
}) => {


  return (
    <div className="page dashboard-page">

      {/* Controls */}
      <div className="controls-panel">
        <LocationSelector location={location} setLocation={setLocation} />
        <ModeSelector mode={mode} setMode={setMode} />
      </div>

      {/* Stats Grid */}
      <div className="stats-grid">
        <StatsCard
          label="Current Crowd"
          value={crowdData ? (crowdData.count === 0 ? '--' : crowdData.count) : '...'}
          icon={<Users size={20} />}
        />
        <StatsCard
          label="Crowd Status"
          value={crowdData?.status ?? '...'}
          status={crowdData?.status}
          icon={<Activity size={20} />}
        />
        <StatsCard
          label="Trend"
          value={crowdData?.trend ?? '...'}
          isTrend={true}
          icon={<TrendingUp size={20} />}
        />
        <StatsCard
          label="Best Time to Visit"
          value={crowdData?.bestTime ?? '...'}
          icon={<Clock size={20} />}
        />
        <StatsCard
          label="Prediction"
          value={crowdData?.prediction ?? '...'}
          icon={<Zap size={20} />}
        />
        <StatsCard
          label="Estimated Waiting Time"
          value={crowdData ? (crowdData.waitingTime === 0 ? '--' : `${crowdData.waitingTime} mins`) : '...'}
          icon={<Timer size={20} />}
        />
      </div>



      {/* System Info */}
      <SystemInfo mode={mode} />

    </div>
  );
};

export default DashboardPage;
