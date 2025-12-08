import { useNavigate } from "react-router-dom";
import { Card, CardHeader } from "../../components/ui/Card";
import { ProgramForm, ProgramFormValues } from "./ProgramForm";
import { programsApi } from "../../api/programs.api";
import { authStore } from "../../auth/auth.store";

export default function ProgramCreatePage() {
  const user = authStore((s) => s.user);
  const nav = useNavigate();

  if (!user) return <div>Must be logged in</div>;

  const handleSubmit = async (values: ProgramFormValues) => {
    await programsApi.create(user.id, {
      name: values.name,
      description: values.description,
      startDate: values.startDate || undefined,
      endDate: values.endDate || undefined,
    });
    nav("/programs");
  };

  return (
    <div className="max-w-5xl mx-auto">
      <Card>
        <CardHeader title="Create program" />
        <ProgramForm onSubmit={handleSubmit} submitLabel="Create program" />
      </Card>
    </div>
  );
}
