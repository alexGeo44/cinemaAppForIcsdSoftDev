import { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import type { ProgramResponse, ProgramState } from "../types";
import { programsApi } from "../api/programsApi";
import { useAuth } from "../auth/AuthContext";

const ALL_STATES: ProgramState[] = [
  "CREATED",
  "SUBMISSION",
  "ASSIGNMENT",
  "REVIEW",
  "SCHEDULING",
  "FINAL_PUBLICATION",
  "DECISION",
  "ANNOUNCED",
];

export default function ProgramDetailPage() {
  const { id } = useParams();
  const [program, setProgram] = useState<ProgramResponse | null>(null);
  const [newState, setNewState] = useState<ProgramState>("SUBMISSION");
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!id) return;
    programsApi.get(Number(id)).then(setProgram);
  }, [id]);

  if (!program) return <div>Loading...</div>;

  const isProgrammerOrAdmin =
    user && (user.role === "PROGRAMMER" || user.role === "ADMIN");

  const handleChangeState = async () => {
    if (!id) return;
    try {
      await programsApi.changeState(Number(id), newState);
      const updated = await programsApi.get(Number(id));
      setProgram(updated);
    } catch {
      alert("State change failed");
    }
  };

  const handleDelete = async () => {
    if (!id) return;
    if (!window.confirm("Delete this program?")) return;
    try {
      await programsApi.delete(Number(id));
      navigate("/");
    } catch {
      alert("Delete failed (maybe not CREATED or not programmer)");
    }
  };

  return (
    <div>
      <div className="page-header">
        <h2>{program.name}</h2>
        {isProgrammerOrAdmin && (
          <button className="danger" onClick={handleDelete}>
            Delete
          </button>
        )}
      </div>
      <p>{program.description}</p>
      <p>
        {program.startDate} – {program.endDate}
      </p>
      <p>
        State: <span className="badge">{program.state}</span>
      </p>

      {isProgrammerOrAdmin && (
        <div className="card">
          <h3>Program state</h3>
          <div className="form-inline">
            <select
              value={newState}
              onChange={(e) => setNewState(e.target.value as ProgramState)}
            >
              {ALL_STATES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            <button onClick={handleChangeState}>Change state</button>
          </div>
        </div>
      )}

      {user && (
        <div className="card">
          <h3>Screenings</h3>
          <Link
            to={`/programs/${program.id}/screenings/new`}
            className="button-link"
          >
            + Create screening
          </Link>
          <p>
            (Εδώ μπορείς να βάλεις λίστα screenings με άλλο endpoint /search
            όταν το υλοποιήσεις.)
          </p>
        </div>
      )}
    </div>
  );
}
