import { useEffect, useMemo, useState } from "react";
import { screeningsApi } from "../../api/screenings.api";
import type { Screening } from "../../domain/screenings/screening.types";
import { ScreeningCard } from "./ScreeningCard";
import { authStore } from "../../auth/auth.store";
import { ScreeningState } from "../../domain/screenings/screening.enums";
import { Navigate } from "react-router-dom";

function mergeById(listA: Screening[], listB: Screening[]) {
  const m = new Map<number, Screening>();
  for (const x of listA) m.set(x.id, x);
  for (const x of listB) m.set(x.id, x);
  return Array.from(m.values()).sort((a, b) => (b.id ?? 0) - (a.id ?? 0));
}

export default function MyScreeningsPage() {
  const user = authStore((s) => s.user);

  const [items, setItems] = useState<Screening[]>([]);
  const [stateFilter, setStateFilter] = useState<ScreeningState | "ALL">("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const states = useMemo(() => Object.values(ScreeningState), []);

  useEffect(() => {
    if (!user) return;

    let alive = true;
    setLoading(true);
    setError(null);

    (async () => {
      try {
        // Αν ζητάμε ALL, πολλά backends δεν επιστρέφουν CREATED (drafts) by default.
        // Άρα φέρνουμε και τα δύο και τα κάνουμε merge.
        if (stateFilter === "ALL") {
          const [normalRes, draftsRes] = await Promise.all([
            screeningsApi.bySubmitter({
              submitterId: user.id,
              state: undefined, // backend πιθανόν επιστρέφει non-drafts
              offset: 0,
              limit: 50,
            }),
            screeningsApi.bySubmitter({
              submitterId: user.id,
              state: ScreeningState.CREATED, // drafts
              offset: 0,
              limit: 50,
            }),
          ]);

          if (!alive) return;
          setItems(mergeById(normalRes.data ?? [], draftsRes.data ?? []));
          return;
        }

        // συγκεκριμένο state
        const res = await screeningsApi.bySubmitter({
          submitterId: user.id,
          state: stateFilter,
          offset: 0,
          limit: 50,
        });

        if (!alive) return;
        setItems(res.data ?? []);
      } catch (e: any) {
        if (!alive) return;
        setItems([]);
        setError(e?.response?.data?.message || e?.message || "Failed to load screenings.");
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [user?.id, stateFilter]);

  if (!user) return <Navigate to="/login" replace />;

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-xl font-semibold text-slate-100">My screenings</h1>

        <label className="text-sm text-slate-200">
          State:&nbsp;
          <select
            className="ml-2 rounded-md bg-slate-900/60 border border-slate-700 px-2 py-1"
            value={stateFilter}
            onChange={(e) => setStateFilter(e.target.value as any)}
          >
            <option value="ALL">All</option>
            {states.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error && (
        <div className="text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-sm text-slate-400">Loading…</div>
      ) : (
        <div className="space-y-3">
          {items.map((s) => (
            <ScreeningCard key={s.id} screening={s} currentUserId={user.id} />
          ))}
          {items.length === 0 && <div className="text-sm text-slate-400">No screenings found.</div>}
        </div>
      )}
    </div>
  );
}
