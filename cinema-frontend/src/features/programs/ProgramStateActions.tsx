import { useMemo, useState } from "react";
import { Program, ProgramState } from "../../domain/programs/program.types";
import { programsApi } from "../../api/programs.api";
import { authStore } from "../../auth/auth.store";
import { Button } from "../../components/ui/Button";
import { Badge } from "../../components/ui/Badge";

// ✅ 100% match με backend ProgramState enum & transitions
const transitions: Record<ProgramState, ProgramState[]> = {
  CREATED: ["SUBMISSION"],
  SUBMISSION: ["ASSIGNMENT"],
  ASSIGNMENT: ["REVIEW"],
  REVIEW: ["SCHEDULING"],
  SCHEDULING: ["FINAL_PUBLICATION"],
  FINAL_PUBLICATION: ["DECISION"],
  DECISION: ["ANNOUNCED"],
  ANNOUNCED: [],
};

export function ProgramStateActions({
  program,
  onChanged,
}: {
  program: Program;
  onChanged?: () => void;
}) {
  const user = authStore((s) => s.user);

  const [busy, setBusy] = useState<ProgramState | null>(null);
  const [err, setErr] = useState<string | null>(null);

  if (!user) return null;

  const possible = useMemo(() => transitions[program.state] ?? [], [program.state]);

  const change = async (next: ProgramState) => {
    try {
      setErr(null);
      setBusy(next);

      // ✅ στέλνει body { nextState: "FINAL_PUBLICATION" } κλπ
      await programsApi.changeState(program.id, next);

      onChanged?.();
    } catch (e: any) {
      setErr(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to change program state."
      );
    } finally {
      setBusy(null);
    }
  };

  if (possible.length === 0) return null;

  return (
    <div className="mt-4 space-y-2">
      <div className="flex items-center justify-between gap-3">
        <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wide">
          Program actions
        </div>
        <Badge color="default">{program.state}</Badge>
      </div>

      {err && (
        <div className="text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
          {err}
        </div>
      )}

      <div className="flex flex-wrap gap-2">
        {possible.map((st) => (
          <Button
            key={st}
            variant="secondary"
            onClick={() => change(st)}
            disabled={busy !== null}
          >
            {busy === st ? "Changing…" : `Change to ${st}`}
          </Button>
        ))}
      </div>
    </div>
  );
}
