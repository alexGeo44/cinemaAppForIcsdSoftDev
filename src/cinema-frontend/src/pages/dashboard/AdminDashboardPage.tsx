import React from "react";
import { Link } from "react-router-dom";

const AdminDashboard: React.FC = () => {
  return (
    <div>
      <h1>Admin Dashboard</h1>
      <ul>
        <li><Link to="/programs">Διαχείριση Προγραμμάτων</Link></li>
        <li><Link to="/screenings">Διαχείριση Προβολών</Link></li>
        <li><Link to="/users">Διαχείριση Χρηστών</Link></li>
      </ul>
    </div>
  );
};

export default AdminDashboard;
