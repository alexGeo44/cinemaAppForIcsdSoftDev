import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { programsApi } from "../../api/programs.api";
import type { Screening } from "../../domain/screenings/screening.types";
import type { Program } from "../../domain/programs/program.types";
import { ScreeningCard } from "./ScreeningCard";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";

const PAGE_SIZE = 20;

function normalize(s: string) {
  return s.trim().toLowerCase();
}

export default function ProgramScreeningsPublicPage() {
  const [sp] = useSearchParams();
  const programId = Number(sp.get("programId"));

  const [program, setProgram] = useState<Program | null>(null);

  const [items, setItems] = useState<Screening[]>([]);
  const [q, setQ] = useState("");

  const [offset, setOffset] = useState(0);
  const limit = PAGE_SIZE;

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const load = async (mode: "initial" | "refresh" = "refresh") => {
    if (!Number.isFinite(programId) || programId <= 0) {
      setErr("Missing or invalid programId.");
      setLoading(false);
      return;
    }

    try {
      setErr(null);
      mode === "initial" ? setLoading(true) : setRefreshing(true);

      // 1) program (public/role-aware DTO)
      const p = await programsApi.view(programId);
      setProgram(p.data);

      // 2) screenings timetable (public view)
      // Backend should already filter by access rights (visitors only see announced + scheduled).
      const res = await screeningsApi.byProgram({
        programId,
        offset,
        limit,
        timetable: true, // ✅ sort by start_time / scheduledTime
      });

      setItems(res.data);
    } catch (e: any) {
      setItems([]);
      setProgram(null);
      setErr(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to load program screenings."
      );
    } finally {
      mode === "initial" ? setLoading(false) : setRefreshing(false);
    }
  };

  // reset paging when program changes
  useEffect(() => {
    setOffset(0);
  }, [programId]);

  useEffect(() => {
    load("initial");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [programId, offset]);

  const filtered = useMemo(() => {
    const needle = normalize(q);
    if (!needle) return items;

    return items.filter((x) => {
      const hay = `${x.title ?? ""} ${x.genre ?? ""} ${x.room ?? ""} ${x.scheduledTime ?? ""}`.toLowerCase();
      return hay.includes(needle);
    });
  }, [items, q]);

  const canPrev = offset > 0;
  const canNext = items.length === limit;

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title="Program timetable"
          subtitle={
            Number.isFinite(programId)
              ? `Program #${programId}${program?.name ? ` • ${program.name}` : ""}${program?.state ? ` • ${program.state}` : ""}`
              : "Select a program"
          }
          extra={
            <div className="flex items-center gap-2">
              <Badge color="default">{filtered.length} shown</Badge>
              <Button
                variant="secondary"
                onClick={() => load("refresh")}
                disabled={loading || refreshing}
              >
                {refreshing ? "Refreshing…" : "Refresh"}
              </Button>
            </div>
          }
        />

        <CardSection title="Search">
          <input
            className="w-full rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-sky-500/60"
            placeholder="Search (title / genre / room / time)…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            disabled={loading}
          />
          <div className="mt-2 text-xs text-slate-400">
            Visitors can see only announced programs and scheduled screenings.
          </div>
        </CardSection>

        <CardSection>
          <div className="flex items-center justify-between">
            <div className="text-xs text-slate-400">
              offset <span className="text-slate-200">{offset}</span> • limit{" "}
              <span className="text-slate-200">{limit}</span>
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

          {err && (
            <div className="mt-3 text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
              {err}
            </div>
          )}

          {loading ? (
            <div className="mt-4 text-sm text-slate-400">Loading…</div>
          ) : filtered.length === 0 ? (
            <div className="mt-4 text-sm text-slate-400">No scheduled screenings found.</div>
          ) : (
            <div className="mt-4 space-y-2">
              {filtered.map((s) => (
                <ScreeningCard
                  key={s.id}
                  screening={s}
                  // no actions for visitors:
                  isProgrammer={false}
                  isStaff={false}
                />
              ))}
            </div>
          )}
        </CardSection>
      </Card>
    </div>
  );
}
