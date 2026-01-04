import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { programsApi } from "../../api/programs.api";
import type { Program } from "../../domain/programs/program.types";
import { ProgramState } from "../../domain/programs/program.types";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { authStore } from "../../auth/auth.store";
import { BaseRole } from "../../domain/auth/auth.types";
import { normalizeRole } from "../../auth/role";

function stateColor(state: ProgramState) {
  switch (state) {
    case ProgramState.ANNOUNCED:
      return "success";

    case ProgramState.DECISION:
    case ProgramState.FINAL_PUBLICATION:
      return "warning";

    default:
      return "default";
  }
}

function normalize(s: string) {
  return s.trim().toLowerCase();
}

export default function ProgramListPage() {
  const nav = useNavigate();

  const user = authStore((s) => s.user);
  const bootstrapped = authStore((s) => s.bootstrapped);

  const baseRole = normalizeRole(user?.role);
  const showCreateButton = bootstrapped && !!user && baseRole === BaseRole.USER;

  const [programs, setPrograms] = useState<Program[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const [q, setQ] = useState("");

  const loadPrograms = async (mode: "initial" | "refresh" = "initial") => {
    const setBusy = mode === "initial" ? setLoading : setRefreshing;

    try {
      setErr(null);
      setBusy(true);

      const res = await programsApi.search({
        offset: 0,
        limit: 50,
      });

      setPrograms(res.data as any);
    } catch (e: any) {
      setErr(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Αποτυχία φόρτωσης προγραμμάτων."
      );
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => {
    let cancelled = false;

    (async () => {
      if (cancelled) return;
      await loadPrograms("initial");
    })();

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filtered = useMemo(() => {
    const needle = normalize(q);
    if (!needle) return programs;

    return programs.filter((p) => {
      const hay = `${p.name ?? ""} ${p.description ?? ""} ${p.startDate ?? ""} ${p.endDate ?? ""}`.toLowerCase();
      return hay.includes(needle);
    });
  }, [programs, q]);

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title="Programs"
          subtitle="Όλα τα προγράμματα του festival / cinema."
          extra={
            <div className="flex items-center gap-2">
              <Button
                variant="secondary"
                onClick={() => loadPrograms("refresh")}
                disabled={loading || refreshing}
              >
                {refreshing ? "Refreshing…" : "Refresh"}
              </Button>

              {showCreateButton ? (
                <Button onClick={() => nav("/programs/new")}>New program</Button>
              ) : null}
            </div>
          }
        />

        <CardSection title="Search">
          <input
            className="w-full rounded-md border border-slate-700 bg-slate-950 text-slate-100 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-sky-500/60 disabled:opacity-60"
            placeholder="Search by name/description/date…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            disabled={loading}
          />
          <div className="mt-2 text-xs text-slate-500">
            {user
              ? baseRole === BaseRole.ADMIN
                ? "ADMIN βλέπει μόνο user-management. Τα cinema actions δεν είναι διαθέσιμα."
                : "Ως USER μπορείς να δημιουργήσεις program ή να κάνεις submissions."
              : "Ως VISITOR βλέπεις μόνο ό,τι επιτρέπει το backend (π.χ. announced programs)."}
          </div>
        </CardSection>

        <CardSection>
          {loading ? (
            <div className="text-sm text-slate-300">Loading…</div>
          ) : (
            <>
              {err && (
                <div className="mb-3 text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
                  {err}
                </div>
              )}

              {filtered.length === 0 ? (
                <div className="text-sm text-slate-400">
                  Δεν υπάρχουν προγράμματα που να ταιριάζουν.
                </div>
              ) : (
                <ul className="divide-y divide-slate-800">
                  {filtered.map((p) => (
                    <li key={p.id}>
                      <button
                        type="button"
                        onClick={() => nav(`/programs/${p.id}`)}
                        className="w-full text-left py-3 px-2 rounded-lg hover:bg-slate-800/60 transition flex items-center justify-between gap-3"
                      >
                        <div className="min-w-0">
                          <div className="font-medium text-slate-50 truncate">
                            {p.name}
                          </div>

                          <div className="text-xs text-slate-400 mt-1">
                            {p.startDate || "—"} → {p.endDate || "—"}
                          </div>
                        </div>

                        <div className="shrink-0">
                          <Badge color={stateColor(p.state)}>{p.state}</Badge>
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
        </CardSection>
      </Card>
    </div>
  );
}
