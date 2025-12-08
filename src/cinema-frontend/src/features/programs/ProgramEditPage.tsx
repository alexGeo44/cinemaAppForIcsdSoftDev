import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Card, CardHeader } from "../../components/ui/Card";
import { ProgramForm, ProgramFormValues } from "./ProgramForm";
import { programsApi } from "../../api/programs.api";
import { Program } from "../../domain/programs/program.types";
import { authStore } from "../../auth/auth.store";

export default function ProgramEditPage() {
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

  if (!user) return <div>Must be logged in</div>;
  if (loading) return <div>Loading…</div>;
  if (!program) return <div>Program not found.</div>;

  const handleSubmit = async (values: ProgramFormValues) => {
    await programsApi.update(program.id, user.id, {
      name: values.name,
      description: values.description,
      startDate: values.startDate || undefined,
      endDate: values.endDate || undefined,
    });
    nav(`/programs/${program.id}`);
  };

  return (
    <div className="max-w-5xl mx-auto">
      <Card>
        <CardHeader title="Edit program" />
        <ProgramForm
          initial={program}
          onSubmit={handleSubmit}
          submitLabel="Save changes"
        />
      </Card>
    </div>
  );
}
