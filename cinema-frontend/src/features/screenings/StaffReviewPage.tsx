// src/features/screenings/StaffReviewPage.tsx
import { useEffect, useMemo, useState } from "react";
import { Navigate } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { programsApi } from "../../api/programs.api";
import type { Screening } from "../../domain/screenings/screening.types";
import { ScreeningCard } from "./ScreeningCard";
import { authStore } from "../../auth/auth.store";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";

type ProgramStateMap = Record<number, string | undefined>;

export default function StaffReviewPage() {
  const user = authStore((s) => s.user);

  const [items, setItems] = useState<Screening[]>([]);
  const [programStates, setProgramStates] = useState<ProgramStateMap>({});

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = async (mode: "initial" | "refresh" = "refresh") => {
    if (!user) return;

    try {
      setError(null);
      mode === "initial" ? setLoading(true) : setRefreshing(true);

      // 1) load assigned screenings
      const res = await screeningsApi.byStaff({ offset: 0, limit: 50 });
      const data = res.data ?? [];
      setItems(data);

      // 2) load program states for each unique programId
      const uniqueProgramIds = Array.from(
        new Set(data.map((s) => s.programId).filter((x) => typeof x === "number" && x > 0))
      );

      const statesEntries = await Promise.all(
        uniqueProgramIds.map(async (pid) => {
          try {
            const p = await programsApi.view(pid);
            const st = (p.data as any)?.state as string | undefined;
            return [pid, st] as const;
          } catch {
            return [pid, undefined] as const;
          }
        })
      );

      const nextMap: ProgramStateMap = {};
      for (const [pid, st] of statesEntries) nextMap[pid] = st;
      setProgramStates(nextMap);
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to load assigned screenings."
      );
      setItems([]);
      setProgramStates({});
    } finally {
      mode === "initial" ? setLoading(false) : setRefreshing(false);
    }
  };

  useEffect(() => {
    if (!user) return;
    load("initial");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  const assignedOnly = useMemo(() => {
    if (!user) return [];
    return items.filter((s) => s.staffMemberId != null && s.staffMemberId === user.id);
  }, [items, user]);

  if (!user) return <Navigate to="/login" replace />;

  return (
    <div className="max-w-4xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title="Review queue"
          subtitle="Screenings assigned to you as STAFF."
          extra={
            <div className="flex items-center gap-2">
              <Badge color="default">{assignedOnly.length} items</Badge>
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

        {error && (
          <div className="mb-3 text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
            {error}
          </div>
        )}

        <CardSection title="Assigned screenings">
          {loading ? (
            <div className="text-sm text-slate-400">Loading…</div>
          ) : assignedOnly.length === 0 ? (
            <div className="text-sm text-slate-400">No screenings assigned.</div>
          ) : (
            <div className="space-y-3">
              {assignedOnly.map((s) => (
                <ScreeningCard
                  key={s.id}
                  screening={s}
                  currentUserId={user.id}
                  isStaff={true}
                  programState={programStates[s.programId]} // ✅ THE FIX
                  onChanged={() => load("refresh")}
                />
              ))}
            </div>
          )}
        </CardSection>
      </Card>
    </div>
  );
}
