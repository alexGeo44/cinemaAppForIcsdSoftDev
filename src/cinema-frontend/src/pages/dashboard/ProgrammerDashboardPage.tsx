import React from "react";
import { Link } from "react-router-dom";

const ProgrammerDashboard: React.FC = () => {
  return (
    <div>
      <h1>Programmer Dashboard</h1>
      <ul>
        <li><Link to="/programs">Προγραμματισμός ταινιών</Link></li>
        <li><Link to="/screenings">Προβολές προς έλεγχο</Link></li>
      </ul>
    </div>
  );
};

export default ProgrammerDashboard;
