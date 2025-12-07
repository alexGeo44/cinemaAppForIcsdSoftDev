import React from "react";
import { Link } from "react-router-dom";

const StaffDashboard: React.FC = () => {
  return (
    <div>
      <h1>Staff Dashboard</h1>
      <ul>
        <li><Link to="/screenings">Προβολές σήμερα</Link></li>
        <li><Link to="/programs">Προγράμματα</Link></li>
      </ul>
    </div>
  );
};

export default StaffDashboard;
