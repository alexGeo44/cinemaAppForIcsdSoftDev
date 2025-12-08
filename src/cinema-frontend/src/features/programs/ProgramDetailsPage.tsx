import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { programsApi } from "../../api/programs.api";
import { Program, ProgramState } from "../../domain/programs/program.types";
import { Button } from "../../components/ui/Button";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { authStore } from "../../auth/auth.store";

const transitions: Record<ProgramState, ProgramState[]> = {
  DRAFT: [ProgramState.ACTIVE, ProgramState.CANCELLED],
  ACTIVE: [ProgramState.ARCHIVED, ProgramState.CANCELLED],
  ARCHIVED: [],
  CANCELLED: [],
  SUBMISSION: [],
  REVIEW: [],
  SCHEDULING: [],
  FINALIZED: [],
};

function stateColor(state: ProgramState) {
  switch (state) {
    case ProgramState.ACTIVE:
      return "success";
    case ProgramState.CANCELLED:
      return "danger";
    case ProgramState.ARCHIVED:
      return "warning";
    default:
      return "default";
  }
}

export default function ProgramDetailsPage() {
  const { id } = useParams();
  const [program, setProgram] = useState<Program | null>(null);
  const [loading, setLoading] = useState(true);
  const user = authStore((s) => s.user);
  const nav = useNavigate();

  useEffect(() => {
    if (!id) return;
    programsApi
      .view(Number(id))
      .then((res) => setProgram(res.data))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div>Loading…</div>;
  if (!program) return <div>Program not found.</div>;

  const possible = transitions[program.state] || [];
  const actorId = user?.id ?? 0;

  const handleChangeState = async (next: ProgramState) => {
    if (!user) return;
    await programsApi.changeState(program.id, next, actorId);
    const res = await programsApi.view(program.id);
    setProgram(res.data);
  };

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <Card>
        <CardHeader title={program.name} subtitle={`Program #${program.id}`} />

        <CardSection title="Status">
          <Badge color={stateColor(program.state)}>{program.state}</Badge>
        </CardSection>

        <CardSection title="Description">
          <p className="text-sm text-slate-700 whitespace-pre-wrap">
            {program.description || "No description."}
          </p>
        </CardSection>

        <CardSection title="Dates">
          <p className="text-sm text-slate-700">
            {program.startDate || "—"} &rarr; {program.endDate || "—"}
          </p>
        </CardSection>

        <CardSection title="Actions">
          <div className="flex flex-wrap gap-2">
            {user && (
              <>
                <Button
                  variant="secondary"
                  onClick={() =>
                    nav(`/screenings/new?programId=${program.id}`)
                  }
                >
                  New screening
                </Button>

                <Button
                  variant="secondary"
                  onClick={() => nav(`/programs/${program.id}/edit`)}
                >
                  Edit program
                </Button>
              </>
            )}

            {user &&
              possible.map((st) => (
                <Button
                  key={st}
                  variant="primary"
                  onClick={() => handleChangeState(st)}
                >
                  Change to {st}
                </Button>
              ))}

            <Button
              variant="ghost"
              onClick={() =>
                nav(`/programmer/screenings?programId=${program.id}`)
              }
            >
              View screenings
            </Button>
          </div>
        </CardSection>
      </Card>
    </div>
  );
}
