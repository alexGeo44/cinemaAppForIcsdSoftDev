import React from "react";
import { useAuth } from "../../context/AuthContext";

const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  if (!user) {
    return <div>Δεν είσαι συνδεδεμένος.</div>;
  }

  const { role, username } = user;

  if (role === "ADMIN") {
    return (
      <div>
        <h1>Admin Dashboard</h1>
        <p>Καλωσήρθες, {username}!</p>
        <ul>
          <li>Διαχείριση χρηστών</li>
          <li>Προγράμματα &amp; Προβολές</li>
          <li>Στατιστικά / Αναφορές (αν τα φτιάξουμε)</li>
        </ul>
      </div>
    );
  }

  if (role === "PROGRAMMER") {
    return (
      <div>
        <h1>Programmer Dashboard</h1>
        <p>Καλωσήρθες, {username}!</p>
        <ul>
          <li>Διαχείριση προγραμμάτων</li>
          <li>Διαχείριση προβολών</li>
        </ul>
      </div>
    );
  }

  if (role === "STAFF") {
    return (
      <div>
        <h1>Staff Dashboard</h1>
        <p>Καλωσήρθες, {username}!</p>
        <ul>
          <li>Έκδοση εισιτηρίων</li>
          <li>Κρατήσεις</li>
        </ul>
      </div>
    );
  }

  // default USER (ή οτιδήποτε άλλο)
  return (
    <div>
      <h1>User Dashboard</h1>
      <p>Καλωσήρθες, {username}!</p>
      <p>Δεν έχεις ειδικά δικαιώματα. Μίλα με admin για αναβάθμιση ρόλου.</p>
    </div>
  );
};

export default DashboardPage;
