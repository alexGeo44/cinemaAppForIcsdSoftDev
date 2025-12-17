import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { Screening } from "../../domain/screenings/screening.types";
import { ScreeningState } from "../../domain/screenings/screening.enums";
import { ScreeningCard } from "./ScreeningCard";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Button } from "../../components/ui/Button";
import { Badge } from "../../components/ui/Badge";

type Filter = ScreeningState | "ALL";
const PAGE_SIZE = 20;

function normalize(s: string) {
  return s.trim().toLowerCase();
}

// small debounce hook
function useDebouncedValue<T>(value: T, delayMs: number) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const t = window.setTimeout(() => setDebounced(value), delayMs);
    return () => window.clearTimeout(t);
  }, [value, delayMs]);

  return debounced;
}

export default function ProgramScreeningsPublicPage() {
  const [search] = useSearchParams();
  const nav = useNavigate();

  const programId = useMemo(() => {
    const p = search.get("programId");
    if (!p) return null;
    const n = Number(p);
    return Number.isFinite(n) && n > 0 ? n : null;
  }, [search]);

  const [items, setItems] = useState<Screening[]>([]);
  const [loading, setLoading] = useState(false);

  const [stateFilter, setStateFilter] = useState<Filter>("ALL");

  // search input + debounced version
  const [q, setQ] = useState("");
  const qDebounced = useDebouncedValue(q, 250);

  const [offset, setOffset] = useState(0);
  const limit = PAGE_SIZE;

  const [error, setError] = useState<string | null>(null);

  const states = useMemo(() => Object.values(ScreeningState), []);
  const canPrev = offset > 0;
  const canNext = items.length === limit; // best-effort χωρίς total

  // reset paging when the server-side filters change (programId/state/limit)
  useEffect(() => {
    setOffset(0);
  }, [programId, stateFilter, limit]);

  const fetchData = useCallback(
    async (cancel?: { cancelled: boolean }) => {
      if (!programId) return;

      try {
        setLoading(true);
        setError(null);

        const res = await screeningsApi.byProgram({
          programId,
          state: stateFilter === "ALL" ? undefined : stateFilter,
          offset,
          limit,
        });

        if (cancel?.cancelled) return;
        setItems(res.data);
      } catch (e: any) {
        if (cancel?.cancelled) return;
        setError(
          e?.response?.data?.message ||
            e?.response?.data?.error ||
            e?.message ||
            "Failed to load screenings."
        );
      } finally {
        if (!cancel?.cancelled) setLoading(false);
      }
    },
    [programId, stateFilter, offset, limit]
  );

  useEffect(() => {
    if (!programId) return;
    const c = { cancelled: false };
    fetchData(c);
    return () => {
      c.cancelled = true;
    };
  }, [programId, stateFilter, offset, fetchData]);

  const filtered = useMemo(() => {
    const needle = normalize(qDebounced);
    if (!needle) return items;

    return items.filter((x) => {
      const hay = `${x.title ?? ""} ${x.genre ?? ""} ${x.description ?? ""}`.toLowerCase();
      return hay.includes(needle);
    });
  }, [items, qDebounced]);

  if (!programId) {
    return (
      <div className="max-w-5xl mx-auto p-4 text-slate-300">
        Missing/invalid <span className="text-slate-100 font-medium">programId</span>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title="Screenings"
          subtitle={`Program #${programId}`}
          extra={
            <div className="flex items-center gap-2">
              <Button variant="secondary" onClick={() => fetchData()} disabled={loading}>
                {loading ? "Refreshing…" : "Refresh"}
              </Button>

              <Button
                variant="ghost"
                onClick={() => nav(`/programs/${programId}`)}
                disabled={loading}
              >
                Back to program
              </Button>

              <Badge color={stateFilter === "ALL" ? "default" : "warning"}>
                {stateFilter}
              </Badge>
            </div>
          }
        />

        <CardSection title="Filters">
          <div className="flex flex-col gap-3">
            <div className="flex items-center gap-2">
              <input
                className="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 outline-none focus:ring-2 focus:ring-sky-500/60"
                placeholder="Search (title / genre / description)…"
                value={q}
                onChange={(e) => setQ(e.target.value)}
                disabled={loading}
              />

              <select
                className="shrink-0 rounded-md border border-slate-700 bg-slate-950 px-2 py-2 text-sm text-slate-100 outline-none focus:ring-2 focus:ring-sky-500/60"
                value={stateFilter}
                onChange={(e) => setStateFilter(e.target.value as Filter)}
                disabled={loading}
              >
                <option value="ALL">All</option>
                {states.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex flex-wrap gap-2">
              <Chip active={stateFilter === "ALL"} disabled={loading} onClick={() => setStateFilter("ALL")}>
                All
              </Chip>

              {states.map((s) => (
                <Chip
                  key={s}
                  active={stateFilter === s}
                  disabled={loading}
                  onClick={() => setStateFilter(s)}
                >
                  {s}
                </Chip>
              ))}
            </div>
          </div>
        </CardSection>

        <CardSection title="Results">
          <div className="flex items-center justify-between">
            <div className="text-xs text-slate-400">
              Showing{" "}
              <span className="text-slate-200 font-medium">{filtered.length}</span>{" "}
              item(s)
              <span className="text-slate-500">
                {" "}
                • offset {offset} • limit {limit}
              </span>
            </div>

            <div className="flex items-center gap-2">
              <Button
                variant="ghost"
                onClick={() => setOffset((o) => Math.max(0, o - limit))}
                disabled={loading || !canPrev}
              >
                Prev
              </Button>
              <Button
                variant="ghost"
                onClick={() => setOffset((o) => o + limit)}
                disabled={loading || !canNext}
              >
                Next
              </Button>
            </div>
          </div>

          {loading && <div className="mt-4 text-sm text-slate-400">Loading…</div>}

          {error && (
            <div className="mt-3 rounded-md border border-red-400/30 bg-red-500/10 p-3 text-sm text-red-200">
              {error}
            </div>
          )}

          {!loading && !error && (
            <div className="mt-4 space-y-3">
              {filtered.map((s) => (
                <ScreeningCard key={s.id} screening={s} />
              ))}
              {filtered.length === 0 && (
                <div className="text-slate-300">No screenings found for this program.</div>
              )}
            </div>
          )}
        </CardSection>
      </Card>
    </div>
  );
}

function Chip({
  active,
  disabled,
  onClick,
  children,
}: {
  active?: boolean;
  disabled?: boolean;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className={[
        "text-xs px-2.5 py-1.5 rounded-full border transition",
        "disabled:opacity-60 disabled:cursor-not-allowed",
        active
          ? "border-sky-500/50 bg-sky-500/10 text-sky-200"
          : "border-slate-700 bg-slate-950 text-slate-200 hover:bg-slate-900",
      ].join(" ")}
    >
      {children}
    </button>
  );
}
