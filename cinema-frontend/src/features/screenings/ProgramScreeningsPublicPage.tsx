import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { Screening } from "../../domain/screenings/screening.types";
import { ScreeningState } from "../../domain/screenings/screening.enums";
import { ScreeningCard } from "./ScreeningCard";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Button } from "../../components/ui/Button";
import { authStore } from "../../auth/auth.store";

export default function ProgramScreeningsPublicPage() {
  const [search] = useSearchParams();
  const nav = useNavigate();
  const user = authStore((s) => s.user);

  const programId = useMemo(() => {
    const p = search.get("programId");
    if (!p) return null;
    const n = Number(p);
    return Number.isFinite(n) && n > 0 ? n : null;
  }, [search]);

  const [items, setItems] = useState<Screening[]>([]);
  const [loading, setLoading] = useState(false);
  const [stateFilter, setStateFilter] = useState<ScreeningState | "ALL">("ALL");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!programId) return;

    setLoading(true);
    setError(null);

    screeningsApi
      .byProgram({
        programId,
        state: stateFilter === "ALL" ? undefined : stateFilter,
        offset: 0,
        limit: 200,
      })
      .then((res) => setItems(res.data))
      .catch((e: any) => {
        setError(e?.response?.data?.message || e?.message || "Failed to load screenings");
      })
      .finally(() => setLoading(false));
  }, [programId, stateFilter]);

  if (!programId) {
    return <div className="max-w-5xl mx-auto p-4 text-slate-300">Missing/invalid programId</div>;
  }

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <Card>
        <CardHeader title={`Screenings for Program #${programId}`} subtitle="Filtered list (role-aware backend recommended)" />

        <CardSection title="Filters">
          <div className="flex flex-wrap items-center gap-2">
            <label className="text-sm text-slate-200">
              State:&nbsp;
              <select
                className="ml-2 rounded-md border border-slate-700 bg-slate-950 px-2 py-1 text-sm text-slate-100"
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

            <Button variant="secondary" onClick={() => nav(`/programs/${programId}`)}>
              Back to program
            </Button>
          </div>
        </CardSection>

        <CardSection title="Results">
          {loading && <div className="text-slate-300">Loading…</div>}

          {error && (
            <div className="rounded-md border border-red-400/30 bg-red-500/10 p-3 text-sm text-red-200">
              {error}
            </div>
          )}

          {!loading && !error && (
            <div className="space-y-3">
              {items.map((s) => (
                <ScreeningCard key={s.id} screening={s} />
              ))}
              {items.length === 0 && (
                <div className="text-slate-300">No screenings found for this program.</div>
              )}
            </div>
          )}
        </CardSection>
      </Card>
    </div>
  );
}
