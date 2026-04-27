import React from 'react';
import LocationSelector from '../components/LocationSelector';
import ModeSelector from '../components/ModeSelector';
import StatsCard from '../components/StatsCard';
import SystemInfo from '../components/SystemInfo';
import TrendChart from '../components/TrendChart';
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
          value={crowdData?.count ?? '...'}
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
          value={crowdData ? `${crowdData.waitingTime} mins` : '...'}
          icon={<Timer size={20} />}
        />
      </div>

      {/* Historical Trend Chart */}
      <TrendChart location={location} />

      {/* System Info */}
      <SystemInfo mode={mode} />

    </div>
  );
};

export default DashboardPage;
