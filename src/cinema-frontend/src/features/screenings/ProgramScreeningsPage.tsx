import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { Screening } from "../../domain/screenings/screening.types";
import { ScreeningCard } from "./ScreeningCard";
import { ScreeningState } from "../../domain/screenings/screening.enums";

export default function ProgramScreeningsPage() {
  const [search] = useSearchParams();
  const programIdParam = search.get("programId");
  const programId = programIdParam ? Number(programIdParam) : null;

  const [items, setItems] = useState<Screening[]>([]);
  const [stateFilter, setStateFilter] = useState<ScreeningState | "ALL">("ALL");

  useEffect(() => {
    if (!programId) return;
    screeningsApi
      .byProgram({
        programId,
        state: stateFilter === "ALL" ? undefined : stateFilter,
        offset: 0,
        limit: 100,
      })
      .then((res) => setItems(res.data));
  }, [programId, stateFilter]);

  if (!programId) return <div>Missing programId</div>;

  return (
    <div>
      <h1>Screenings for Program #{programId}</h1>

      <label>
        State:&nbsp;
        <select
          value={stateFilter}
          onChange={(e) => setStateFilter(e.target.value as any)}
        >
          <option value="ALL">All</option>
          {Object.values(ScreeningState).map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </label>

      <div style={{ marginTop: "1rem" }}>
        {items.map((s) => (
          <ScreeningCard key={s.id} screening={s} />
        ))}
        {items.length === 0 && <div>No screenings for this program.</div>}
      </div>
    </div>
  );
}
