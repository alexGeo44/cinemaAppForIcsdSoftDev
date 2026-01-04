// src/features/programs/ProgramDetailsPage.tsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { programsApi } from "../../api/programs.api";
import type { Program } from "../../domain/programs/program.types";
import { ProgramState } from "../../domain/programs/program.enums";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { authStore } from "../../auth/auth.store";
import {
  canManageProgramInProgram,
  getProgrammerIds,
  getStaffIds,
  isAdmin,
  type ProgramLike,
} from "../../auth/permissions";

type ProgramDTO = Program & ProgramLike;

const transitions: Record<ProgramState, ProgramState[]> = {
  [ProgramState.CREATED]: [ProgramState.SUBMISSION],
  [ProgramState.SUBMISSION]: [ProgramState.ASSIGNMENT],
  [ProgramState.ASSIGNMENT]: [ProgramState.REVIEW],
  [ProgramState.REVIEW]: [ProgramState.SCHEDULING],
  // NOTE: το enum σου λέγεται FINAL_SUBMISSION αλλά value = "FINAL_PUBLICATION"
  [ProgramState.SCHEDULING]: [ProgramState.FINAL_PUBLICATION],
  [ProgramState.FINAL_PUBLICATION]: [ProgramState.DECISION],
  [ProgramState.DECISION]: [ProgramState.ANNOUNCED],
  [ProgramState.ANNOUNCED]: [],
};

function stateColor(state: ProgramState) {
  switch (state) {
    case ProgramState.ANNOUNCED:
      return "success";
    case ProgramState.DECISION:
    case ProgramState.FINAL_SUBMISSION:
      return "warning";
    default:
      return "default";
  }
}

export default function ProgramDetailsPage() {
  const { id } = useParams();
  const nav = useNavigate();
  const user = authStore((s) => s.user);

  const programId = useMemo(() => {
    const n = Number(id);
    return Number.isFinite(n) && n > 0 ? n : null;
  }, [id]);

  const [program, setProgram] = useState<ProgramDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [busyState, setBusyState] = useState<ProgramState | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Add programmer/staff
  const [addProgrammerId, setAddProgrammerId] = useState("");
  const [addStaffId, setAddStaffId] = useState("");
  const [busyAdd, setBusyAdd] = useState<"programmer" | "staff" | null>(null);
  const [roleErr, setRoleErr] = useState<string | null>(null);

  const refresh = async () => {
    if (!programId) return;
    const res = await programsApi.view(programId);
    setProgram(res.data as any);
  };

  useEffect(() => {
    if (!programId) {
      setLoading(false);
      setProgram(null);
      setError("Missing/invalid program id.");
      return;
    }

    setLoading(true);
    setError(null);

    programsApi
      .view(programId)
      .then((res) => setProgram(res.data as any))
      .catch((e: any) => {
        setProgram(null);
        setError(e?.response?.data?.message || e?.message || "Failed to load program.");
      })
      .finally(() => setLoading(false));
  }, [programId]);

  const perms = useMemo(() => {
    const uid = user?.id ?? null;
    const role = user?.role;

    const p = (program as unknown as ProgramLike | null) ?? null;

    // programmer-of-this-program (για edit/state/add team κλπ)
    const isProgrammerHere = !!uid && canManageProgramInProgram(p, uid);

    // ✅ ΤΟ ΣΗΜΑΝΤΙΚΟ FIX:
    // ΜΠΛΟΚ ΜΟΝΟ ΓΙΑ CREATOR, ΟΧΙ ΓΙΑ ΟΛΟΥΣ ΤΟΥΣ programmers.
    const isCreatorHere = !!uid && typeof p?.creatorUserId === "number" && p.creatorUserId === uid;

    // ✅ RULE YOU WANT:
    // - any logged-in NON-ADMIN can create screening
    // - BUT if CREATOR of THIS program => cannot
    // (οι added programmers θα βλέπουν κουμπί)
    const canCreateScreeningHere = !!uid && !isAdmin(role) && !isCreatorHere;

    return {
      uid,
      isAdminUser: isAdmin(role),
      isProgrammerHere,
      isCreatorHere,
      canCreateScreeningHere,
      programmers: getProgrammerIds(p),
      staff: getStaffIds(p),
    };
  }, [user?.id, user?.role, program]);

  const handleChangeState = async (next: ProgramState) => {
    if (!programId) return;
    if (!user) return;

    try {
      setError(null);
      setBusyState(next);
      await programsApi.changeState(programId, next);
      await refresh();
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to change program state."
      );
    } finally {
      setBusyState(null);
    }
  };

  const addProgrammer = async () => {
    if (!program) return;

    const n = Number(addProgrammerId);
    if (!Number.isFinite(n) || n <= 0) return setRoleErr("Δώσε έγκυρο userId για PROGRAMMER.");

    try {
      setRoleErr(null);
      setBusyAdd("programmer");
      await programsApi.addProgrammer(program.id, n);
      setAddProgrammerId("");
      await refresh();
    } catch (e: any) {
      setRoleErr(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to add programmer."
      );
    } finally {
      setBusyAdd(null);
    }
  };

  const addStaff = async () => {
    if (!program) return;

    const n = Number(addStaffId);
    if (!Number.isFinite(n) || n <= 0) return setRoleErr("Δώσε έγκυρο userId για STAFF.");

    try {
      setRoleErr(null);
      setBusyAdd("staff");
      await programsApi.addStaff(program.id, n);
      setAddStaffId("");
      await refresh();
    } catch (e: any) {
      setRoleErr(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to add staff."
      );
    } finally {
      setBusyAdd(null);
    }
  };

  const goToScreenings = () => {
    if (!program) return;
    if (perms.isProgrammerHere) nav(`/programmer/screenings?programId=${program.id}`);
    else nav(`/screenings/by-program?programId=${program.id}`);
  };

  if (loading) {
    return <div className="max-w-5xl mx-auto p-4 text-slate-300">Loading…</div>;
  }

  if (!program) {
    return (
      <div className="max-w-5xl mx-auto p-4 text-slate-300">
        Program not found.
        {error && <div className="mt-2 text-xs text-rose-300">{error}</div>}
      </div>
    );
  }

  const state = program.state as ProgramState;
  const possible = transitions[state] ?? [];

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <Card>
        <CardHeader title={program.name} subtitle={`Program #${program.id}`} />

        {error && (
          <div className="mb-3 text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
            {error}
          </div>
        )}

        <CardSection title="Status">
          <Badge color={stateColor(state)}>{state}</Badge>
        </CardSection>

        <CardSection title="Description">
          <p className="text-sm text-slate-200 whitespace-pre-wrap">
            {program.description || "No description."}
          </p>
        </CardSection>

        <CardSection title="Dates">
          <p className="text-sm text-slate-200">
            {program.startDate || "—"} &rarr; {program.endDate || "—"}
          </p>
        </CardSection>

        <CardSection title="Team">
          <div className="text-xs text-slate-300 space-y-1">
            <div>
              <span className="text-slate-500">Programmers:</span>{" "}
              {perms.programmers.length ? perms.programmers.join(", ") : "—"}
            </div>
            <div>
              <span className="text-slate-500">Staff:</span>{" "}
              {perms.staff.length ? perms.staff.join(", ") : "—"}
            </div>

            {process.env.NODE_ENV !== "production" && user && (
              <div className="mt-2 text-[11px] text-slate-500">
                debug: uid={perms.uid} admin={String(perms.isAdminUser)} programmerHere=
                {String(perms.isProgrammerHere)} creatorHere={String(perms.isCreatorHere)} canCreateScreening=
                {String(perms.canCreateScreeningHere)}
              </div>
            )}
          </div>
        </CardSection>

        <CardSection title="Actions">
          <div className="flex flex-wrap gap-2 items-center">
            {/* ✅ SHOW: any logged-in non-admin, except CREATOR of this program */}
            {user && perms.canCreateScreeningHere && (
              <Button
                variant="secondary"
                onClick={() => nav(`/screenings/new?programId=${program.id}`)}
                disabled={busyState !== null}
              >
                New screening
              </Button>
            )}

            {/* Explain block ONLY for CREATOR */}
            {user && perms.isCreatorHere && !perms.canCreateScreeningHere && (
              <div className="text-xs text-slate-500">
                Ως <b>CREATOR</b> αυτού του program δεν επιτρέπεται να υποβάλεις screening στο ίδιο σου το πρόγραμμα.
              </div>
            )}

            {/* Programmer-of-this-program only actions */}
            {user && perms.isProgrammerHere && (
              <>
                <Button
                  variant="secondary"
                  onClick={() => nav(`/programs/${program.id}/edit`)}
                  disabled={busyState !== null}
                >
                  Edit program
                </Button>

                {possible.map((st) => (
                  <Button
                    key={st}
                    variant="primary"
                    onClick={() => handleChangeState(st)}
                    disabled={busyState !== null}
                  >
                    {busyState === st ? "Changing…" : `Change to ${st}`}
                  </Button>
                ))}
              </>
            )}

            <Button variant="ghost" onClick={goToScreenings}>
              View screenings
            </Button>
          </div>

          {/* Add staff/programmer: only programmer-of-this-program */}
          {user && perms.isProgrammerHere && (
            <div className="mt-4 border-t border-slate-800 pt-4 space-y-3">
              {roleErr && (
                <div className="text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
                  {roleErr}
                </div>
              )}

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div className="flex items-end gap-2">
                  <div className="flex-1">
                    <label className="block text-[11px] text-slate-500 mb-1">
                      Add PROGRAMMER (userId)
                    </label>
                    <input
                      value={addProgrammerId}
                      onChange={(e) => setAddProgrammerId(e.target.value)}
                      className="w-full rounded-md border border-slate-700 bg-slate-950 px-2 py-2 text-sm text-slate-100"
                      placeholder="e.g. 7"
                      disabled={busyAdd !== null}
                    />
                  </div>
                  <Button variant="secondary" onClick={addProgrammer} disabled={busyAdd !== null}>
                    {busyAdd === "programmer" ? "Adding…" : "Add"}
                  </Button>
                </div>

                <div className="flex items-end gap-2">
                  <div className="flex-1">
                    <label className="block text-[11px] text-slate-500 mb-1">
                      Add STAFF (userId)
                    </label>
                    <input
                      value={addStaffId}
                      onChange={(e) => setAddStaffId(e.target.value)}
                      className="w-full rounded-md border border-slate-700 bg-slate-950 px-2 py-2 text-sm text-slate-100"
                      placeholder="e.g. 12"
                      disabled={busyAdd !== null}
                    />
                  </div>
                  <Button variant="secondary" onClick={addStaff} disabled={busyAdd !== null}>
                    {busyAdd === "staff" ? "Adding…" : "Add"}
                  </Button>
                </div>
              </div>
            </div>
          )}
        </CardSection>
      </Card>
    </div>
  );
}
