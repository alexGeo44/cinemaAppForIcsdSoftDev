import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, Navigate } from "react-router-dom";
import { Card, CardHeader } from "../../components/ui/Card";
import { ProgramForm, ProgramFormValues } from "./ProgramForm";
import { programsApi } from "../../api/programs.api";
import type { Program } from "../../domain/programs/program.types";
import { ProgramState } from "../../domain/programs/program.types";
import { authStore } from "../../auth/auth.store";

export default function ProgramEditPage() {
  const { id } = useParams();
  const nav = useNavigate();
  const user = authStore((s) => s.user);

  const programId = Number(id);

  const [program, setProgram] = useState<Program | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!Number.isFinite(programId)) return;

    let alive = true;

    (async () => {
      try {
        setLoading(true);
        setError(null);

        const res = await programsApi.view(programId);
        if (!alive) return;
        setProgram(res.data);
      } catch (e: any) {
        if (!alive) return;
        setProgram(null);
        setError(
          e?.response?.data?.message ||
            e?.response?.data?.error ||
            e?.message ||
            "Failed to load program."
        );
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [programId]);

  const programmerIds = (program as any)?.programmerIds as number[] | undefined;

  const isProgrammer = useMemo(() => {
    if (!user || !program) return false;
    return Array.isArray(programmerIds) && programmerIds.includes(user.id);
  }, [user, program, programmerIds]);

  // ✅ Spec: all updates must be completed before ANNOUNCED
  const canEditByState = program?.state !== ProgramState.ANNOUNCED;

  if (!user) return <Navigate to="/login" replace />;

  if (!Number.isFinite(programId)) {
    return (
      <div className="max-w-5xl mx-auto p-4 text-sm text-slate-300">
        Missing/invalid program id.
      </div>
    );
  }

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto p-4 text-sm text-slate-300">
        Loading…
      </div>
    );
  }

  if (!program) {
    return (
      <div className="max-w-5xl mx-auto p-4 text-sm text-slate-300">
        Program not found.
        {error && <div className="mt-2 text-xs text-rose-300">{error}</div>}
      </div>
    );
  }

  // ✅ enforce program-scoped permission (backend likely redacts programmerIds for outsiders)
  if (!isProgrammer) return <Navigate to="/forbidden" replace />;

  const handleSubmit = async (values: ProgramFormValues) => {
    if (busy) return;
    if (!canEditByState) {
      setError("This program is ANNOUNCED and cannot be edited.");
      return;
    }

    setError(null);

    const name = values.name?.trim() ?? "";
    const description = values.description?.trim() ?? "";
    const startDate = values.startDate?.trim() ?? "";
    const endDate = values.endDate?.trim() ?? "";

    if (!name) return setError("Name is required.");
    if (!description) return setError("Description is required.");
    if (!startDate) return setError("Start date is required.");
    if (!endDate) return setError("End date is required.");
    if (endDate < startDate) return setError("End date cannot be before start date.");

    try {
      setBusy(true);

      await programsApi.update(program.id, {
        name,
        description,
        startDate,
        endDate,
      });

      nav(`/programs/${program.id}`, { replace: true });
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to update program."
      );
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto">
      <Card>
        <CardHeader title="Edit program" subtitle={`Program #${program.id}`} />

        {!canEditByState && (
          <div className="mb-3 text-xs text-amber-200 border border-amber-500/30 bg-amber-500/10 rounded-md px-3 py-2">
            This program is <b>ANNOUNCED</b> and is locked. Editing is not allowed.
          </div>
        )}

        {error && (
          <div className="mb-3 text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
            {error}
          </div>
        )}

        <ProgramForm
          initial={program}
          onSubmit={handleSubmit}
          submitLabel={busy ? "Saving…" : "Save changes"}
          disabled={busy || !canEditByState}
        />
      </Card>
    </div>
  );
}
