import React, { useState, useEffect, useCallback } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import axios from 'axios';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import DashboardPage from './pages/DashboardPage';
import PredictionPage from './pages/PredictionPage';
import AboutPage from './pages/AboutPage';
import './styles.css';

const API_BASE_URL = 'https://smart-campus-backend-azf3.onrender.com/api';

function App() {
  const [location, setLocation] = useState('Library');
  const [mode, setMode] = useState('SIMULATED');
  const [crowdData, setCrowdData] = useState(null);

  const [loading, setLoading] = useState(true);

  const fetchAllData = useCallback(async (isSilent = false) => {
    try {
      if (!isSilent) setLoading(true);
      const encodedLocation = encodeURIComponent(location);
      const mainRes = await axios.get(`${API_BASE_URL}/crowd/current?location=${encodedLocation}`);

      if (isSilent) console.log("Polling...");
      
      setCrowdData(mainRes.data);
      

    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  }, [location]);

  const updateMode = async (newMode) => {
    try {
      console.log(`[Mode Switch] Switching to ${newMode}`);
      await axios.post(`${API_BASE_URL}/mode/${newMode.toLowerCase()}?location=${encodeURIComponent(location)}`);
      setMode(newMode);
    } catch (error) {
      console.error('Error updating mode:', error);
    }
  };

  useEffect(() => {
    // Initial call for any mode or location change
    fetchAllData();

    // Setup polling ONLY if mode is SIMULATED
    let interval = null;
    if (mode === 'SIMULATED') {
      console.log("Starting 15s polling for SIMULATED mode...");
      interval = setInterval(() => {
        fetchAllData(true); // Silent update for polling
      }, 15000);
    }

    return () => {
      if (interval) {
        console.log("Clearing polling interval");
        clearInterval(interval);
      }
    };
  }, [mode, location, fetchAllData]);

  // Shared props passed into pages that need live data
  const sharedProps = {
    location, setLocation,
    mode, setMode: updateMode,
    crowdData,
    loading,
  };

  return (
    <BrowserRouter>
      <div className="app-container">
        <Navbar />
        <main className="app-main">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/dashboard" element={<DashboardPage {...sharedProps} />} />
            <Route path="/prediction" element={<PredictionPage {...sharedProps} />} />
            <Route path="/about" element={<AboutPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
