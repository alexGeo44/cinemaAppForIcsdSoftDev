import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { programsApi } from "../../api/programs.api";
import { Program } from "../../domain/programs/program.types";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { authStore } from "../../auth/auth.store";
import { BaseRole } from "../../domain/auth/auth.types";

function stateColor(state: Program["state"]) {
  switch (state) {
    case "ACTIVE":
      return "success";
    case "CANCELLED":
      return "danger";
    case "ARCHIVED":
      return "warning";
    default:
      return "default";
  }
}

export default function ProgramListPage() {
  const [programs, setPrograms] = useState<Program[]>([]);
  const [loading, setLoading] = useState(true);
  const nav = useNavigate();
  const user = authStore((s) => s.user);

  // μόνο PROGRAMMER ή ADMIN μπορούν να δημιουργούν programs
  const canCreateProgram =
    user &&
    (user.role === BaseRole.PROGRAMMER || user.role === BaseRole.ADMIN);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const res = await programsApi.search({
          name: undefined,
          programState: undefined,
          from: undefined,
          to: undefined,
          offset: 0,
          limit: 50,
        });
        setPrograms(res.data);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) {
    return <div className="max-w-5xl mx-auto">Loading…</div>;
  }

  return (
    <div className="max-w-5xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title="Programs"
          subtitle="Όλα τα προγράμματα του φεστιβάλ / cinema."
          extra={
            canCreateProgram && (
              <Button onClick={() => nav("/programs/new")}>
                New program
              </Button>
            )
          }
        />

        <CardSection>
          {programs.length === 0 ? (
            <div className="text-sm text-slate-500">
              Δεν υπάρχουν προγράμματα ακόμα.
            </div>
          ) : (
            <ul className="divide-y">
              {programs.map((p) => (
                <li
                  key={p.id}
                  className="py-3 flex items-center justify-between cursor-pointer hover:bg-slate-50 px-2 rounded-md"
                  onClick={() => nav(`/programs/${p.id}`)}
                >
                  <div>
                    <div className="font-medium text-sm">{p.name}</div>
                    <div className="text-xs text-slate-500">
                      {p.startDate || "—"} → {p.endDate || "—"}
                    </div>
                  </div>
                  <Badge color={stateColor(p.state)}>{p.state}</Badge>
                </li>
              ))}
            </ul>
          )}
        </CardSection>
      </Card>
    </div>
  );
}
