import { useEffect, useState } from "react";
import type { ProgramResponse } from "../types";
import { programsApi } from "../api/programsApi";
import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function ProgramListPage() {
  const [programs, setPrograms] = useState<ProgramResponse[]>([]);
  const [name, setName] = useState("");
  const { user } = useAuth();

  const load = async () => {
    const res = await programsApi.search(name ? { name } : {});
    setPrograms(res);
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <div>
      <div className="page-header">
        <h2>Programs</h2>
        {user && user.role !== "ADMIN" && (
          <Link to="/programs/new" className="button-link">
            + New Program
          </Link>
        )}
      </div>

      <div className="toolbar">
        <input
          placeholder="Search by name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <button onClick={load}>Search</button>
      </div>

      <div className="list">
        {programs.map((p) => (
          <Link key={p.id} to={`/programs/${p.id}`} className="list-item">
            <div className="list-item-main">
              <strong>{p.name}</strong>
              <span>{p.description}</span>
            </div>
            <div className="list-item-meta">
              <span>
                {p.startDate} – {p.endDate}
              </span>
              <span className="badge">{p.state}</span>
            </div>
          </Link>
        ))}
        {programs.length === 0 && <p>No programs found.</p>}
      </div>
    </div>
  );
}
