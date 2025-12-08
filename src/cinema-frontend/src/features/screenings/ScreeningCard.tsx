import { Screening } from "../../domain/screenings/screening.types";
import { Link } from "react-router-dom";

export function ScreeningCard({ screening }: { screening: Screening }) {
  return (
    <div
      style={{
        border: "1px solid #ddd",
        borderRadius: 6,
        padding: "0.5rem 0.75rem",
        marginBottom: "0.5rem",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <div>
          <Link to={`/screenings/${screening.id}`}>
            <strong>{screening.title}</strong>
          </Link>
          <div style={{ fontSize: "0.85rem", opacity: 0.8 }}>
            Program #{screening.programId} • {screening.genre}
          </div>
        </div>
        <div style={{ textAlign: "right" }}>
          <span style={{ fontSize: "0.8rem" }}>State: {screening.state}</span>
          {screening.room && (
            <div style={{ fontSize: "0.8rem" }}>
              Room {screening.room} @ {screening.scheduledTime}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
