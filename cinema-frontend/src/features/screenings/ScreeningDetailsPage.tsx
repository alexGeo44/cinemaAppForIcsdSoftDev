import { useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { programsApi } from "../../api/programs.api";
import type { Screening } from "../../domain/screenings/screening.types";
import { ScreeningState } from "../../domain/screenings/screening.enums";
import type { Program } from "../../domain/programs/program.types";
import { authStore } from "../../auth/auth.store";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import type { ProgramLike } from "../../auth/permissions";

function stateColor(state?: ScreeningState) {
  switch (state) {
    case ScreeningState.SUBMITTED:
      return "warning";
    case ScreeningState.REVIEWED:
    case ScreeningState.APPROVED:
    case ScreeningState.FINAL_SUBMITTED:
      return "secondary";
    case ScreeningState.SCHEDULED:
      return "success";
    case ScreeningState.REJECTED:
      return "danger";
    case ScreeningState.CREATED:
    default:
      return "default";
  }
}

type ProgramDTO = Program & ProgramLike;

export default function ScreeningDetailsPage() {
  const { id } = useParams();
  const nav = useNavigate();

  const [screening, setScreening] = useState<Screening | null>(null);
  const [program, setProgram] = useState<ProgramDTO | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [programError, setProgramError] = useState<string | null>(null);

  const user = authStore((s) => s.user);

  const screeningId = Number(id);

  const load = async () => {
    if (!Number.isFinite(screeningId)) return;

    setLoading(true);
    setError(null);
    setProgramError(null);

    try {
      const sres = await screeningsApi.view(screeningId);
      setScreening(sres.data);

      try {
        const pres = await programsApi.view(sres.data.programId);
        setProgram(pres.data as any);
      } catch (e: any) {
        setProgram(null);
        setProgramError(
          e?.response?.data?.message ||
            "Δεν μπορώ να φορτώσω το program phase (άρα κρύβω phase-actions)."
        );
      }
    } catch (e: any) {
      setScreening(null);
      setProgram(null);
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to load screening."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!Number.isFinite(screeningId)) return;
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [screeningId]);

  if (loading) return <div className="max-w-4xl mx-auto p-4 text-slate-300">Loading…</div>;
  if (error) return <div className="max-w-4xl mx-auto p-4 text-rose-300">{error}</div>;
  if (!screening) return <div className="max-w-4xl mx-auto p-4 text-slate-300">Screening not found.</div>;

  return (
    <div className="max-w-4xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title={screening.title || "(untitled)"}
          subtitle={`Screening #${screening.id} • Program #${screening.programId}`}
          extra={
            <Button variant="secondary" onClick={() => nav(-1)}>
              Back
            </Button>
          }
        />

        <CardSection title="Status">
          <div className="flex flex-wrap items-center gap-2">
            <Badge color={stateColor(screening.state)}>{screening.state ?? "PUBLIC"}</Badge>

            {program?.state ? (
              <span className="text-xs text-slate-600">
                Program phase: <span className="font-medium">{program.state}</span>
              </span>
            ) : (
              <span className="text-xs text-slate-500">Program phase: —</span>
            )}

            {screening.room && screening.scheduledTime && (
              <span className="text-xs text-slate-600">
                • Room {screening.room} @ {screening.scheduledTime}
              </span>
            )}
          </div>

          {programError && (
            <div className="mt-2 text-xs text-amber-200 border border-amber-500/30 bg-amber-500/10 rounded-md px-3 py-2">
              {programError}
            </div>
          )}
        </CardSection>

        <CardSection title="Info">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm text-slate-700">
            <div>
              <div className="text-xs text-slate-500 mb-0.5">Genre</div>
              <div>{screening.genre || "—"}</div>
            </div>

            <div>
              <div className="text-xs text-slate-500 mb-0.5">Submitter</div>
              <div>{screening.submitterId != null ? `#${screening.submitterId}` : "—"}</div>
            </div>

            <div>
              <div className="text-xs text-slate-500 mb-0.5">Staff handler</div>
              <div>{screening.staffMemberId ? `#${screening.staffMemberId}` : "—"}</div>
            </div>

            <div>
              <div className="text-xs text-slate-500 mb-0.5">Timeline</div>
              <div className="text-xs">
                Submitted: {screening.submittedTime || "—"} <br />
                Reviewed: {screening.reviewedTime || "—"}
              </div>
            </div>
          </div>
        </CardSection>

        <CardSection title="Description">
          <p className="text-sm text-slate-700 whitespace-pre-wrap">
            {screening.description || "No description."}
          </p>
        </CardSection>

        <CardSection title="Actions">
          <ScreeningActions screening={screening} program={program} onChanged={load} />
          {!user && <div className="mt-2 text-xs text-slate-500">Για actions χρειάζεται login.</div>}
        </CardSection>
      </Card>
    </div>
  );
}

function ScreeningActions({
  screening,
  program,
  onChanged,
}: {
  screening: Screening;
  program: ProgramDTO | null;
  onChanged: () => Promise<void> | void;
}) {
  const user = authStore((s) => s.user);
  const nav = useNavigate();

  const [date, setDate] = useState("");
  const [room, setRoom] = useState("");
  const [busy, setBusy] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  if (!user) return null;

  const programPhase = program?.state; // may be undefined if program load failed
  const phaseKnown = !!programPhase;
  const inPhase = (phase: string) => phaseKnown && programPhase === phase;

  const perms = useMemo(() => {
    const state = screening.state;

    const isOwner = screening.submitterId != null && user.id === screening.submitterId;

    const isAssignedStaff =
      screening.staffMemberId != null && screening.staffMemberId === user.id;

    // programmer-of-program only if backend included programmerIds
    const isProgrammer =
      !!program && Array.isArray(program.programmerIds) && program.programmerIds.includes(user.id);

    const isFinal = state === ScreeningState.SCHEDULED || state === ScreeningState.REJECTED;

    const canEdit = isOwner && state === ScreeningState.CREATED;
    const canWithdraw = isOwner && state === ScreeningState.CREATED;
    const canSubmit = isOwner && state === ScreeningState.CREATED && inPhase("SUBMISSION");

    const canAssign =
      isProgrammer &&
      state === ScreeningState.SUBMITTED &&
      inPhase("ASSIGNMENT") &&
      screening.staffMemberId == null;

    const canReview = isAssignedStaff && state === ScreeningState.SUBMITTED && inPhase("REVIEW");

    const canApprove = isOwner && state === ScreeningState.REVIEWED && inPhase("SCHEDULING");

    const canFinalSubmit =
      isOwner && state === ScreeningState.APPROVED && inPhase("FINAL_PUBLICATION");

    const canReject =
      isProgrammer &&
      !!state &&
      !isFinal &&
      (inPhase("SCHEDULING") || inPhase("DECISION"));

    const canSchedule =
      isProgrammer && state === ScreeningState.FINAL_SUBMITTED && inPhase("DECISION");

    return {
      phaseKnown,
      canEdit,
      canWithdraw,
      canSubmit,
      canAssign,
      canReview,
      canApprove,
      canFinalSubmit,
      canReject,
      canSchedule,
    };
  }, [user.id, screening, program, programPhase, phaseKnown]);

  const wrap = async (key: string, fn: () => Promise<void>) => {
    try {
      setErr(null);
      setBusy(key);
      await fn();
      await onChanged();
    } catch (e: any) {
      setErr(e?.response?.data?.message || e?.response?.data?.error || e?.message || "Action failed.");
    } finally {
      setBusy(null);
    }
  };

  return (
    <div className="space-y-3">
      {err && (
        <div className="text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
          {err}
        </div>
      )}

      {!perms.phaseKnown && (
        <div className="text-xs text-slate-500">
          Δεν είναι διαθέσιμο το program phase, οπότε κρύβω actions που εξαρτώνται από phases.
        </div>
      )}

      <div className="flex flex-wrap gap-2">
        {perms.canEdit && (
          <Button variant="secondary" onClick={() => nav(`/screenings/${screening.id}/edit`)} disabled={busy !== null}>
            Edit details
          </Button>
        )}

        {perms.canSubmit && (
          <Button onClick={() => wrap("submit", () => screeningsApi.submit(screening.id))} disabled={busy !== null}>
            {busy === "submit" ? "Submitting…" : "Submit"}
          </Button>
        )}

        {perms.canWithdraw && (
          <Button
            variant="secondary"
            onClick={() =>
              wrap("withdraw", async () => {
                await screeningsApi.withdraw(screening.id);
                nav(-1);
              })
            }
            disabled={busy !== null}
          >
            {busy === "withdraw" ? "Withdrawing…" : "Withdraw (delete draft)"}
          </Button>
        )}

        {perms.canAssign && (
          <Button
            variant="secondary"
            onClick={() =>
              wrap("assign", async () => {
                const idStr = window.prompt("Staff user id:", "");
                const staffId = idStr ? Number(idStr) : NaN;
                if (!Number.isFinite(staffId) || staffId <= 0) return;
                await screeningsApi.assignHandler(screening.id, staffId);
              })
            }
            disabled={busy !== null}
          >
            {busy === "assign" ? "Assigning…" : "Assign handler"}
          </Button>
        )}

        {perms.canReview && (
          <Button
            onClick={() =>
              wrap("review", async () => {
                const scoreStr = window.prompt("Score (0-10):", "8");
                if (scoreStr == null) return;
                const score = Number(scoreStr);
                if (!Number.isFinite(score) || score < 0 || score > 10) throw new Error("Score must be between 0 and 10");
                const comments = window.prompt("Comments (optional):", "") || undefined;
                await screeningsApi.review(screening.id, score, comments);
              })
            }
            disabled={busy !== null}
          >
            {busy === "review" ? "Reviewing…" : "Review"}
          </Button>
        )}

        {perms.canApprove && (
          <Button onClick={() => wrap("approve", () => screeningsApi.approve(screening.id))} disabled={busy !== null}>
            {busy === "approve" ? "Approving…" : "Approve (submitter)"}
          </Button>
        )}

        {perms.canFinalSubmit && (
          <Button onClick={() => wrap("final", () => screeningsApi.finalSubmit(screening.id))} disabled={busy !== null}>
            {busy === "final" ? "Final submitting…" : "Final submit"}
          </Button>
        )}

        {perms.canReject && (
          <Button
            variant="danger"
            onClick={() =>
              wrap("reject", async () => {
                const reason = window.prompt("Reason for rejection:", "");
                if (!reason || !reason.trim()) return;
                await screeningsApi.reject(screening.id, reason.trim());
              })
            }
            disabled={busy !== null}
          >
            {busy === "reject" ? "Rejecting…" : "Reject (programmer)"}
          </Button>
        )}

        {perms.canSchedule && (
          <Button
            onClick={() =>
              wrap("schedule", async () => {
                const d = date || new Date().toISOString().slice(0, 10);
                const r = (room || "").trim() || "Room 1";
                await screeningsApi.schedule(screening.id, d, r);
              })
            }
            disabled={busy !== null}
          >
            {busy === "schedule" ? "Scheduling…" : "Schedule (programmer)"}
          </Button>
        )}
      </div>

      {perms.canSchedule && (
        <div className="grid grid-cols-1 sm:grid-cols-[160px,1fr] gap-3 items-end">
          <Input type="date" label="Scheduled date" value={date} onChange={(e) => setDate(e.target.value)} disabled={busy !== null} />
          <Input label="Room" value={room} onChange={(e) => setRoom(e.target.value)} disabled={busy !== null} />
        </div>
      )}

      {!perms.canEdit &&
        !perms.canSubmit &&
        !perms.canWithdraw &&
        !perms.canAssign &&
        !perms.canReview &&
        !perms.canApprove &&
        !perms.canFinalSubmit &&
        !perms.canReject &&
        !perms.canSchedule && (
          <div className="text-xs text-slate-500">
            Δεν υπάρχουν διαθέσιμες ενέργειες για το ρόλο σου σε αυτή την κατάσταση/φάση.
          </div>
        )}
    </div>
  );
}
