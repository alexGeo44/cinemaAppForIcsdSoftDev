import { useEffect, useState } from "react";
import { screeningsApi } from "../../api/screenings.api";
import { Screening } from "../../domain/screenings/screening.types";
import { ScreeningCard } from "./ScreeningCard";
import { authStore } from "../../auth/auth.store";
import { ScreeningState } from "../../domain/screenings/screening.enums";

export default function MyScreeningsPage() {
  const user = authStore((s) => s.user);
  const [items, setItems] = useState<Screening[]>([]);
  const [stateFilter, setStateFilter] = useState<ScreeningState | "ALL">("ALL");

  useEffect(() => {
    if (!user) return;
    screeningsApi
      .bySubmitter({
        submitterId: user.id,
        state: stateFilter === "ALL" ? undefined : stateFilter,
        offset: 0,
        limit: 50,
      })
      .then((res) => setItems(res.data));
  }, [user, stateFilter]);

  if (!user) return <div>Must be logged in</div>;

  return (
    <div>
      <h1>My screenings</h1>

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
        {items.length === 0 && <div>No screenings found.</div>}
      </div>
    </div>
  );
}
