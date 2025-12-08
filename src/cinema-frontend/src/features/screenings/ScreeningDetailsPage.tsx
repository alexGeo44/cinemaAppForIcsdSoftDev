import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { Screening } from "../../domain/screenings/screening.types";
import { ScreeningState } from "../../domain/screenings/screening.enums";
import { authStore } from "../../auth/auth.store";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";

function stateColor(state: ScreeningState) {
  switch (state) {
    case ScreeningState.SUBMITTED:
    case ScreeningState.UNDER_REVIEW:
      return "warning";
    case ScreeningState.ACCEPTED:
    case ScreeningState.SCHEDULED:
    case ScreeningState.COMPLETED:
      return "success";
    case ScreeningState.REJECTED:
    case ScreeningState.CANCELLED:
      return "danger";
    default:
      return "default";
  }
}

export default function ScreeningDetailsPage() {
  const { id } = useParams();
  const [screening, setScreening] = useState<Screening | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    screeningsApi
      .view(Number(id))
      .then((res) => setScreening(res.data))
      .finally(() => setLoading(false));
  }, [id]);

  const refresh = async () => {
    if (!id) return;
    const res = await screeningsApi.view(Number(id));
    setScreening(res.data);
  };

  if (loading) return <div>Loading…</div>;
  if (!screening) return <div>Screening not found.</div>;

  return (
    <div className="max-w-4xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title={screening.title}
          subtitle={`Screening #${screening.id} • Program #${screening.programId}`}
        />

        <CardSection title="Status">
          <div className="flex items-center gap-2">
            <Badge color={stateColor(screening.state)}>
              {screening.state}
            </Badge>
            {screening.room && screening.scheduledTime && (
              <span className="text-xs text-slate-600">
                Room {screening.room} @ {screening.scheduledTime}
              </span>
            )}
          </div>
        </CardSection>

        <CardSection title="Info">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm text-slate-700">
            <div>
              <div className="text-xs text-slate-500 mb-0.5">Genre</div>
              <div>{screening.genre || "—"}</div>
            </div>
            <div>
              <div className="text-xs text-slate-500 mb-0.5">Submitter</div>
              <div>#{screening.submitterId}</div>
            </div>
            <div>
              <div className="text-xs text-slate-500 mb-0.5">Staff handler</div>
              <div>
                {screening.staffMemberId ? `#${screening.staffMemberId}` : "—"}
              </div>
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
          <ScreeningActions screening={screening} onChanged={refresh} />
        </CardSection>
      </Card>
    </div>
  );
}

function ScreeningActions({
  screening,
  onChanged,
}: {
  screening: Screening;
  onChanged: () => void;
}) {
  const user = authStore((s) => s.user);
  const [date, setDate] = useState("");
  const [room, setRoom] = useState("");
  const nav = useNavigate();

  if (!user)
    return <div className="text-xs text-slate-500">Login required.</div>;

  const isSubmitter = user.id === screening.submitterId;
  const isAssignedStaff =
    screening.staffMemberId != null && screening.staffMemberId === user.id;

  const canEdit = isSubmitter && screening.state === ScreeningState.CREATED;
  const canSubmit = isSubmitter && screening.state === ScreeningState.CREATED;
  const canWithdraw =
    isSubmitter && screening.state === ScreeningState.SUBMITTED;
  const canAssign =
    screening.state === ScreeningState.SUBMITTED ||
    screening.state === ScreeningState.UNDER_REVIEW;
  const canStaffReview =
    isAssignedStaff && screening.state === ScreeningState.UNDER_REVIEW;
  const canSchedule =
    screening.state === ScreeningState.ACCEPTED ||
    screening.state === ScreeningState.UNDER_REVIEW;

  const handleSubmit = async () => {
    await screeningsApi.submit(user.id, screening.id);
    await onChanged();
  };

  const handleWithdraw = async () => {
    await screeningsApi.withdraw(user.id, screening.id);
    await onChanged();
  };

  const handleAssign = async () => {
    const idStr = window.prompt("Staff user id:", "");
    const staffId = idStr ? Number(idStr) : NaN;
    if (!staffId) return;
    await screeningsApi.assignHandler(user.id, screening.id, staffId);
    await onChanged();
  };

  const handleAcceptSchedule = async () => {
    const d = date || new Date().toISOString().slice(0, 10);
    const r = room || "Room 1";
    await screeningsApi.acceptAndSchedule(user.id, screening.id, d, r);
    await onChanged();
  };

  const handleReject = async () => {
    await screeningsApi.reject(user.id, screening.id);
    await onChanged();
  };

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-2">
        {canEdit && (
          <Button
            variant="secondary"
            onClick={() => nav(`/screenings/${screening.id}/edit`)}
          >
            Edit details
          </Button>
        )}

        {canSubmit && <Button onClick={handleSubmit}>Submit for review</Button>}

        {canWithdraw && (
          <Button variant="secondary" onClick={handleWithdraw}>
            Withdraw
          </Button>
        )}

        {canAssign && (
          <Button variant="secondary" onClick={handleAssign}>
            Assign staff
          </Button>
        )}

        {canStaffReview && (
          <>
            <Button onClick={handleAcceptSchedule}>Accept & schedule</Button>
            <Button variant="danger" onClick={handleReject}>
              Reject
            </Button>
          </>
        )}
      </div>

      {canSchedule && (
        <div className="grid grid-cols-1 sm:grid-cols-[160px,1fr] gap-3 items-end">
          <Input
            type="date"
            label="Scheduled date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
          <Input
            label="Room"
            value={room}
            onChange={(e) => setRoom(e.target.value)}
          />
        </div>
      )}

      {!canSubmit &&
        !canWithdraw &&
        !canAssign &&
        !canStaffReview &&
        !canSchedule && (
          <div className="text-xs text-slate-500">
            Δεν υπάρχουν διαθέσιμες ενέργειες για το ρόλο σου σε αυτή την
            κατάσταση.
          </div>
        )}
    </div>
  );
}
