// src/features/screenings/ProgramScreeningsPage.tsx
import { useEffect, useMemo, useState } from "react";
import { Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { programsApi } from "../../api/programs.api";
import type { Screening } from "../../domain/screenings/screening.types";
import type { Program } from "../../domain/programs/program.types";
import { ScreeningCard } from "./ScreeningCard";
import { authStore } from "../../auth/auth.store";
import { ScreeningState } from "../../domain/screenings/screening.enums";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";

type Filter = ScreeningState | "ALL";
const PAGE_SIZE = 20;

function normalize(s: string) {
  return s.trim().toLowerCase();
}

function parseId(raw: any): number | null {
  const n = Number(raw);
  return Number.isFinite(n) && n > 0 ? n : null;
}

function parseProgramId(raw: string | null): number | null {
  if (!raw) return null;
  return parseId(raw);
}

function toIdList(arr: any): number[] {
  if (!Array.isArray(arr)) return [];
  return arr
    .map((x) => Number(x))
    .filter((n) => Number.isFinite(n) && n > 0) as number[];
}

function getProgrammerIdsAny(program: any): number[] {
  const a = toIdList(program?.programmerIds);
  const b = toIdList(program?.programmers);
  const creator = parseId(program?.creatorUserId);

  const set = new Set<number>([...a, ...b]);
  if (creator) set.add(creator);

  return Array.from(set);
}

function getStaffIdsAny(program: any): number[] {
  const a = toIdList(program?.staffIds);
  const b = toIdList(program?.staff);
  const c = toIdList(program?.staffMemberIds);

  const set = new Set<number>([...a, ...b, ...c]);
  return Array.from(set);
}

const LAST_PROGRAM_KEY = "cm:lastProgramId:programmerScreenings";

export default function ProgramScreeningsPage() {
  const user = authStore((s) => s.user);
  const nav = useNavigate();
  const [sp] = useSearchParams();

  const programId = useMemo(() => parseProgramId(sp.get("programId")), [sp]);

  const [program, setProgram] = useState<Program | null>(null);
  const [items, setItems] = useState<Screening[]>([]);
  const [stateFilter, setStateFilter] = useState<Filter>("ALL");

  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [q, setQ] = useState("");

  const [offset, setOffset] = useState(0);
  const limit = PAGE_SIZE;

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  // program picker
  const [programIdInput, setProgramIdInput] = useState("");

  const states = useMemo(() => Object.values(ScreeningState), []);

  // ✅ role check AFTER we have program
  const roleFlags = useMemo(() => {
    const uid = user?.id;
    if (!uid || !program) return { isProgrammerHere: false, isStaffHere: false };

    const programmers = getProgrammerIdsAny(program as any);
    const staff = getStaffIdsAny(program as any);

    return {
      isProgrammerHere: programmers.includes(uid),
      isStaffHere: staff.includes(uid),
    };
  }, [user?.id, program]);

  // ✅ if missing programId, try last used
  useEffect(() => {
    if (!user) return;

    if (!programId) {
      const last = parseProgramId(localStorage.getItem(LAST_PROGRAM_KEY));
      if (last) {
        nav(`/programmer/screenings?programId=${last}`, { replace: true });
      } else {
        setLoading(false);
      }
      return;
    }

    localStorage.setItem(LAST_PROGRAM_KEY, String(programId));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id, programId]);

  // ✅ helper: client-side filters for STAFF mode
  const applyClientFilters = (data: Screening[]) => {
    let out = [...data];

    // state
    if (stateFilter !== "ALL") out = out.filter((s) => s.state === stateFilter);

    // title/genre (basic contains)
    const tNeedle = title.trim().toLowerCase();
    if (tNeedle) out = out.filter((s) => (s.title ?? "").toLowerCase().includes(tNeedle));

    const gNeedle = genre.trim().toLowerCase();
    if (gNeedle) out = out.filter((s) => (s.genre ?? "").toLowerCase().includes(gNeedle));

    // from/to: κρατάμε τα inputs αλλά ΔΕΝ τα εφαρμόζουμε αν δεν έχεις date field στο Screening DTO.
    // Αν έχεις π.χ. submittedAt, πες μου να το βάλω.

    return out;
  };

  const load = async (mode: "initial" | "refresh" = "refresh") => {
    if (!user) return;

    if (!programId) {
      setErr("Missing or invalid programId.");
      setProgram(null);
      setItems([]);
      setLoading(false);
      return;
    }

    try {
      setErr(null);
      mode === "initial" ? setLoading(true) : setRefreshing(true);

      // 1) load program FIRST (ώστε να ξέρουμε αν είναι staff/programmer)
      const p = await programsApi.view(programId);
      setProgram(p.data);

      const uid = user.id;
      const programmers = getProgrammerIdsAny(p.data as any);
      const staff = getStaffIdsAny(p.data as any);

      const isProgrammerHere = programmers.includes(uid);
      const isStaffHere = staff.includes(uid);

      // 2) load screenings ανάλογα με ρόλο
      if (isStaffHere && !isProgrammerHere) {
        // ✅ STAFF: πάρε assigned-to-me screenings και κράτα μόνο αυτά του program
        const res = await screeningsApi.byStaff({ offset: 0, limit: 500 });

        // byStaff συνήθως ήδη επιστρέφει μόνο assigned στον user
        // αλλά κάνουμε και extra guard.
        const onlyThisProgram = (res.data || []).filter(
          (s) => Number(s.programId) === Number(programId)
        );

        const filtered = applyClientFilters(onlyThisProgram);

        // client paging
        const paged = filtered.slice(offset, offset + limit);
        setItems(paged);
      } else {
        // ✅ PROGRAMMER: δες όλα του program (server filters)
        const res = await screeningsApi.byProgram({
          programId,
          state: stateFilter === "ALL" ? undefined : stateFilter,
          title: title.trim() || undefined,
          genre: genre.trim() || undefined,
          from: from || undefined,
          to: to || undefined,
          offset,
          limit,
          timetable: false,
        });

        setItems(res.data);
      }
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

  // reset paging when filters change
  useEffect(() => {
    setOffset(0);
  }, [stateFilter, title, genre, from, to]);

  // load when programId exists
  useEffect(() => {
    if (!user) return;
    if (!programId) return;
    load("initial");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id, programId, stateFilter, offset]);

  // local filter on current page (UX only)
  const filtered = useMemo(() => {
    const needle = normalize(q);
    if (!needle) return items;

    return items.filter((x) => {
      const hay = `${x.title ?? ""} ${x.genre ?? ""} ${x.description ?? ""}`.toLowerCase();
      return hay.includes(needle);
    });
  }, [items, q]);

  const canPrev = offset > 0;
  const canNext = items.length === limit;

  if (!user) return <Navigate to="/login" replace />;

  // picker UI if no programId
  if (!programId) {
    return (
      <div className="max-w-5xl mx-auto space-y-4">
        <Card>
          <CardHeader title="Program screenings" subtitle="Select a program" />
          <CardSection title="Choose program">
            <div className="flex flex-col sm:flex-row gap-2 items-start sm:items-end">
              <div className="flex-1">
                <label className="block text-xs text-slate-400 mb-1">Program ID</label>
                <input
                  className="w-full rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-3 py-2 text-sm"
                  placeholder="e.g. 6"
                  value={programIdInput}
                  onChange={(e) => setProgramIdInput(e.target.value)}
                />
              </div>
              <Button
                variant="secondary"
                onClick={() => {
                  const n = parseProgramId(programIdInput.trim());
                  if (!n) return;
                  nav(`/programmer/screenings?programId=${n}`);
                }}
              >
                Open
              </Button>
            </div>

            <div className="mt-3 text-xs text-slate-500">
              Tip: από Program Details πάτα “View screenings” για να σε πάει αυτόματα με programId.
            </div>
          </CardSection>
        </Card>
      </div>
    );
  }

  // ✅ enforce access only AFTER we have program
  if (!loading && program && !roleFlags.isProgrammerHere && !roleFlags.isStaffHere) {
    return <Navigate to="/forbidden" replace />;
  }

  const roleLabel = roleFlags.isProgrammerHere
    ? "PROGRAMMER"
    : roleFlags.isStaffHere
    ? "STAFF"
    : "";

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title={`Program screenings${roleLabel ? ` (${roleLabel})` : ""}`}
          subtitle={`Program #${programId}${(program as any)?.state ? ` • Phase ${(program as any).state}` : ""}`}
          extra={
            <div className="flex items-center gap-2">
              <Badge color="default">{filtered.length} shown</Badge>
              {(program as any)?.state && <Badge color="warning">{(program as any).state}</Badge>}
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

        <CardSection title="Filters">
          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-2 md:flex-row md:items-center">
              <input
                className="w-full rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-sky-500/60"
                placeholder="Quick search (current page)…"
                value={q}
                onChange={(e) => setQ(e.target.value)}
                disabled={loading}
              />

              <select
                className="shrink-0 rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-2 py-2 text-sm outline-none focus:ring-2 focus:ring-sky-500/60"
                value={stateFilter}
                onChange={(e) => setStateFilter(e.target.value as Filter)}
                disabled={loading}
              >
                <option value="ALL">All states</option>
                {states.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-2">
              <input
                className="rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-3 py-2 text-sm"
                placeholder="Film title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                disabled={loading}
              />
              <input
                className="rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-3 py-2 text-sm"
                placeholder="Genre"
                value={genre}
                onChange={(e) => setGenre(e.target.value)}
                disabled={loading}
              />
              <input
                type="date"
                className="rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-3 py-2 text-sm"
                value={from}
                onChange={(e) => setFrom(e.target.value)}
                disabled={loading}
              />
              <input
                type="date"
                className="rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-3 py-2 text-sm"
                value={to}
                onChange={(e) => setTo(e.target.value)}
                disabled={loading}
              />
            </div>
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
            <div className="mt-4 text-sm text-slate-400">No screenings found.</div>
          ) : (
            <div className="mt-4 space-y-2">
              {filtered.map((s) => (
                <ScreeningCard
                  key={s.id}
                  screening={s}
                  currentUserId={user.id}
                  isProgrammer={roleFlags.isProgrammerHere}
                  isStaff={roleFlags.isStaffHere}
                  programState={(program as any)?.state}
                  onChanged={() => load("refresh")}
                  onDeleted={(id) => setItems((prev) => prev.filter((x) => x.id !== id))}
                />
              ))}
            </div>
          )}
        </CardSection>
      </Card>
    </div>
  );
}
