import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Home, TrendingUp, Info } from 'lucide-react';

const Navbar = () => {
  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <span className="navbar-dot" />
        <span className="navbar-title">Smart Campus Crowd Intelligence</span>
      </div>

      <div className="navbar-links">
        <NavLink to="/" end className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <Home size={16} />
          Home
        </NavLink>
        <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <LayoutDashboard size={16} />
          Dashboard
        </NavLink>
        <NavLink to="/prediction" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <TrendingUp size={16} />
          Prediction
        </NavLink>
        <NavLink to="/about" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <Info size={16} />
          About
        </NavLink>
      </div>

      <span className="navbar-badge">LIVE</span>
    </nav>
  );
};

export default Navbar;
