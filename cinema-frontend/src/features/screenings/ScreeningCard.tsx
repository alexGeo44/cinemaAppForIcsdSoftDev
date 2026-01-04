import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import type { Screening } from "../../domain/screenings/screening.types";
import { screeningsApi } from "../../api/screenings.api";
import { authStore } from "../../auth/auth.store";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";

// ✅ normalize helpers
const norm = (v: any) => String(v ?? "").trim().toUpperCase();

/**
 * ✅ KEY FIX:
 * Public DTO δεν έχει state.
 * Αν υπάρχει scheduledTime, τότε για visitor το screening είναι πρακτικά SCHEDULED.
 */
function deriveState(screening: Screening): string {
  const raw = (screening as any).state;
  if (raw != null && String(raw).trim() !== "") return norm(raw);
  if ((screening as any).scheduledTime) return "SCHEDULED";
  return "";
}

function badgeColor(stateRaw?: any): "default" | "success" | "danger" | "warning" {
  const s = norm(stateRaw);
  switch (s) {
    case "SUBMITTED":
    case "REVIEWED":
    case "FINAL_SUBMITTED":
      return "warning";
    case "SCHEDULED":
    case "APPROVED":
      return "success";
    case "REJECTED":
      return "danger";
    case "CREATED":
    default:
      return "default";
  }
}

type BusyAction =
  | null
  | "submit"
  | "withdraw"
  | "assign"
  | "review"
  | "approve"
  | "finalSubmit"
  | "schedule"
  | "reject";

type ProgramPhase =
  | "CREATED"
  | "SUBMISSION"
  | "ASSIGNMENT"
  | "REVIEW"
  | "SCHEDULING"
  | "FINAL_PUBLICATION"
  | "DECISION"
  | "ANNOUNCED"
  | string;

export function ScreeningCard({
  screening,
  programState,
  currentUserId,
  isProgrammer = false,
  isStaff = false,
  onChanged,
  onDeleted,
}: {
  screening: Screening;
  programState?: ProgramPhase;
  currentUserId?: number;
  isProgrammer?: boolean;
  isStaff?: boolean;
  onChanged?: () => void;
  onDeleted?: (id: number) => void;
}) {
  const user = authStore((s) => s.user);
  const nav = useNavigate();

  const [busy, setBusy] = useState<BusyAction>(null);
  const [err, setErr] = useState<string | null>(null);

  const [staffId, setStaffId] = useState("");
  const [score, setScore] = useState("8");
  const [comments, setComments] = useState("");
  const [scheduleDate, setScheduleDate] = useState("");
  const [room, setRoom] = useState("");

  const userId = currentUserId ?? user?.id;

  // ✅ normalized values (IMPORTANT)
  // FIX: stateN τώρα θα γίνει SCHEDULED στο public αν έχει scheduledTime
  const stateN = deriveState(screening);
  const phaseN = norm(programState);

  const phaseKnown = phaseN.length > 0;
  const inPhase = (phase: ProgramPhase) => phaseKnown && phaseN === norm(phase);
  const inPhaseOrUnknown = (phase: ProgramPhase) => !phaseKnown || phaseN === norm(phase);

  const perms = useMemo(() => {
    const isOwner = !!userId && (screening as any).submitterId === userId;

    const isAssignedStaff =
      !!userId &&
      (screening as any).staffMemberId != null &&
      (screening as any).staffMemberId === userId;

    // --- OWNER (submitter) ---
    const canEdit = isOwner && stateN === "CREATED";
    const canSubmit = isOwner && stateN === "CREATED";
    const canWithdraw = isOwner && stateN === "CREATED";

    // --- PROGRAMMER ---
    const canAssign =
      isProgrammer &&
      stateN === "SUBMITTED" &&
      inPhase("ASSIGNMENT") &&
      (screening as any).staffMemberId == null;

    const canSchedule = isProgrammer && stateN === "FINAL_SUBMITTED" && inPhase("DECISION");

    const canReject =
      isProgrammer &&
      !!stateN &&
      stateN !== "SCHEDULED" &&
      stateN !== "REJECTED" &&
      (inPhase("SCHEDULING") || inPhase("DECISION"));

    // --- STAFF ---
    const staffCanReview =
      isStaff && isAssignedStaff && stateN === "SUBMITTED" && inPhaseOrUnknown("REVIEW");

    const staffCanApprove =
      isStaff && isAssignedStaff && stateN === "REVIEWED" && inPhaseOrUnknown("SCHEDULING");

    const staffCanFinalSubmit =
      isStaff && isAssignedStaff && stateN === "APPROVED" && inPhaseOrUnknown("FINAL_PUBLICATION");

    const staffCanSchedule =
      isStaff && isAssignedStaff && stateN === "FINAL_SUBMITTED" && inPhaseOrUnknown("DECISION");

    const staffCanReject =
      isStaff &&
      isAssignedStaff &&
      !!stateN &&
      stateN !== "SCHEDULED" &&
      stateN !== "REJECTED" &&
      (inPhaseOrUnknown("REVIEW") ||
        inPhaseOrUnknown("SCHEDULING") ||
        inPhaseOrUnknown("DECISION") ||
        inPhaseOrUnknown("ANNOUNCED"));

    const canApprove = isOwner && stateN === "REVIEWED" && inPhaseOrUnknown("SCHEDULING");
    const canFinalSubmit = isOwner && stateN === "APPROVED" && inPhaseOrUnknown("FINAL_PUBLICATION");

    return {
      phaseKnown,
      isOwner,
      isAssignedStaff,
      canEdit,
      canSubmit,
      canWithdraw,
      canAssign,

      staffCanReview,
      staffCanApprove,
      staffCanFinalSubmit,
      staffCanSchedule,
      staffCanReject,

      canApprove,
      canFinalSubmit,

      canSchedule,
      canReject,
    };
  }, [
    userId,
    (screening as any).submitterId,
    (screening as any).staffMemberId,
    stateN,
    phaseN,
    phaseKnown,
    isProgrammer,
    isStaff,
  ]);

  const handle = async (
    action: Exclude<BusyAction, null>,
    fn: () => Promise<any>,
    opts?: { skipOnChanged?: boolean }
  ) => {
    try {
      setErr(null);
      setBusy(action);
      await fn();
      if (!opts?.skipOnChanged) onChanged?.();
    } catch (e: any) {
      setErr(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Action failed."
      );
    } finally {
      setBusy(null);
    }
  };

  const submit = () => handle("submit", () => screeningsApi.submit((screening as any).id));

  const withdraw = () =>
    handle(
      "withdraw",
      async () => {
        await screeningsApi.withdraw((screening as any).id);
        if (onDeleted) onDeleted((screening as any).id);
        else if (onChanged) onChanged();
        else nav(-1);
      },
      { skipOnChanged: true }
    );

  const assign = () => {
    const n = Number(staffId);
    if (!Number.isFinite(n) || n <= 0) {
      setErr("Δώσε έγκυρο staff user id.");
      return;
    }
    return handle("assign", () => screeningsApi.assignHandler((screening as any).id, n));
  };

  const review = () => {
    const sc = Number(score);
    if (!Number.isFinite(sc) || sc < 0 || sc > 10) {
      setErr("Score πρέπει να είναι 0–10.");
      return;
    }
    return handle("review", () =>
      screeningsApi.review((screening as any).id, sc, comments?.trim() || undefined)
    );
  };

  const approve = () => handle("approve", () => screeningsApi.approve((screening as any).id));
  const finalSubmit = () =>
    handle("finalSubmit", () => screeningsApi.finalSubmit((screening as any).id));

  const schedule = () => {
    const d = scheduleDate || new Date().toISOString().slice(0, 10);
    const r = room.trim() || "Room 1";
    return handle("schedule", () => screeningsApi.schedule((screening as any).id, d, r));
  };

  const reject = () =>
    handle("reject", async () => {
      const reason = window.prompt("Reason for rejection:", "");
      if (!reason || !reason.trim()) return;
      await screeningsApi.reject((screening as any).id, reason.trim());
    });

  const disabled = busy !== null;

  const showRoleActions =
    perms.canAssign ||
    perms.staffCanReview ||
    perms.staffCanApprove ||
    perms.staffCanFinalSubmit ||
    perms.staffCanSchedule ||
    perms.staffCanReject ||
    perms.canApprove ||
    perms.canFinalSubmit ||
    perms.canSchedule ||
    perms.canReject;

  return (
    <div className="border border-slate-800 rounded-xl p-4 bg-slate-950/80">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <Link
            to={`/screenings/${(screening as any).id}`}
            className="text-slate-50 font-semibold hover:underline"
          >
            {(screening as any).title || "(untitled)"}
          </Link>

          <div className="text-xs text-slate-400 mt-1">
            Program #{(screening as any).programId}
            {(screening as any).genre ? ` • ${(screening as any).genre}` : ""}
            {programState ? ` • Phase ${programState}` : ""}
          </div>

          {(screening as any).description && (
            <div className="text-sm text-slate-200 mt-2 line-clamp-2 whitespace-pre-wrap">
              {(screening as any).description}
            </div>
          )}

          {err && (
            <div className="mt-3 text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
              {err}
            </div>
          )}
        </div>

        <div className="text-right min-w-[260px]">
          <div className="flex items-center justify-end gap-2">
            <span className="text-xs text-slate-400">State</span>
            <Badge color={badgeColor(stateN)}>{stateN || "PUBLIC"}</Badge>
          </div>

          {(screening as any).room && (screening as any).scheduledTime && (
            <div className="text-xs text-slate-400 mt-2">
              Room <span className="text-slate-200">{(screening as any).room}</span> @{" "}
              <span className="text-slate-200">{(screening as any).scheduledTime}</span>
            </div>
          )}

          <div className="mt-3 flex flex-wrap gap-2 justify-end">
            <Button
              variant="secondary"
              onClick={() => nav(`/screenings/${(screening as any).id}`)}
              disabled={disabled}
            >
              View
            </Button>

            {perms.canEdit && (
              <Button
                variant="secondary"
                onClick={() => nav(`/screenings/${(screening as any).id}/edit`)}
                disabled={disabled}
              >
                Edit
              </Button>
            )}

            {perms.canSubmit && (
              <Button onClick={submit} disabled={disabled}>
                {busy === "submit" ? "Submitting…" : "Submit"}
              </Button>
            )}

            {perms.canWithdraw && (
              <Button variant="danger" onClick={withdraw} disabled={disabled}>
                {busy === "withdraw" ? "Withdrawing…" : "Withdraw"}
              </Button>
            )}
          </div>

          {showRoleActions && (
            <div className="mt-4 border-t border-slate-800 pt-3 space-y-3">
              {perms.canAssign && (
                <div className="flex items-end gap-2 justify-end">
                  <div className="flex flex-col items-end">
                    <label className="text-[11px] text-slate-500">Staff id</label>
                    <input
                      value={staffId}
                      onChange={(e) => setStaffId(e.target.value)}
                      className="w-28 rounded-md border border-slate-700 bg-slate-950 px-2 py-1 text-sm text-slate-100"
                      placeholder="e.g. 7"
                      disabled={disabled}
                    />
                  </div>
                  <Button variant="secondary" onClick={assign} disabled={disabled}>
                    {busy === "assign" ? "Assigning…" : "Assign"}
                  </Button>
                </div>
              )}

              {perms.staffCanReview && (
                <div className="space-y-2">
                  <div className="flex items-end gap-2 justify-end">
                    <div className="flex flex-col items-end">
                      <label className="text-[11px] text-slate-500">Score (0–10)</label>
                      <input
                        value={score}
                        onChange={(e) => setScore(e.target.value)}
                        className="w-24 rounded-md border border-slate-700 bg-slate-950 px-2 py-1 text-sm text-slate-100"
                        disabled={disabled}
                      />
                    </div>
                    <Button onClick={review} disabled={disabled}>
                      {busy === "review" ? "Reviewing…" : "Review"}
                    </Button>
                  </div>

                  <textarea
                    value={comments}
                    onChange={(e) => setComments(e.target.value)}
                    placeholder="Comments (optional)…"
                    className="w-full rounded-md border border-slate-700 bg-slate-950 px-2 py-2 text-sm text-slate-100"
                    disabled={disabled}
                    rows={2}
                  />
                </div>
              )}

              {(perms.canApprove || perms.staffCanApprove) && (
                <div className="flex justify-end">
                  <Button onClick={approve} disabled={disabled}>
                    {busy === "approve" ? "Approving…" : "Approve"}
                  </Button>
                </div>
              )}

              {(perms.canFinalSubmit || perms.staffCanFinalSubmit) && (
                <div className="flex justify-end">
                  <Button onClick={finalSubmit} disabled={disabled}>
                    {busy === "finalSubmit" ? "Submitting…" : "Final submit"}
                  </Button>
                </div>
              )}

              {(perms.canSchedule || perms.staffCanSchedule) && (
                <div className="flex items-end gap-2 justify-end">
                  <div className="flex flex-col items-end">
                    <label className="text-[11px] text-slate-500">Date</label>
                    <input
                      type="date"
                      value={scheduleDate}
                      onChange={(e) => setScheduleDate(e.target.value)}
                      className="w-36 rounded-md border border-slate-700 bg-slate-950 px-2 py-1 text-sm text-slate-100"
                      disabled={disabled}
                    />
                  </div>
                  <div className="flex flex-col items-end">
                    <label className="text-[11px] text-slate-500">Room</label>
                    <input
                      value={room}
                      onChange={(e) => setRoom(e.target.value)}
                      className="w-36 rounded-md border border-slate-700 bg-slate-950 px-2 py-1 text-sm text-slate-100"
                      placeholder="Room 1"
                      disabled={disabled}
                    />
                  </div>
                  <Button onClick={schedule} disabled={disabled}>
                    {busy === "schedule" ? "Scheduling…" : "Schedule"}
                  </Button>
                </div>
              )}

              {(perms.canReject || perms.staffCanReject) && (
                <div className="flex justify-end">
                  <Button variant="danger" onClick={reject} disabled={disabled}>
                    {busy === "reject" ? "Rejecting…" : "Reject"}
                  </Button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
