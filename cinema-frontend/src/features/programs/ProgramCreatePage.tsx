import { useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { Card, CardHeader } from "../../components/ui/Card";
import { ProgramForm, ProgramFormValues } from "./ProgramForm";
import { programsApi } from "../../api/programs.api";
import { authStore } from "../../auth/auth.store";
import { BaseRole } from "../../domain/auth/auth.types";
import { normalizeRole } from "../../auth/role";

export default function ProgramCreatePage() {
  const nav = useNavigate();

  const user = authStore((s) => s.user);
  const token = authStore((s) => s.token);

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // safety: αν μπεις χωρίς AuthGuard
  if (!token || !user) return <Navigate to="/login" replace />;

  // ✅ Spec: ADMIN δεν έχει cinema-domain δικαιώματα. Άρα μόνο base USER δημιουργεί program.
  const baseRole = normalizeRole(user.role);
  const canCreateProgram = baseRole === BaseRole.USER;

  if (!canCreateProgram) return <Navigate to="/forbidden" replace />;

  const handleSubmit = async (values: ProgramFormValues) => {
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

      await programsApi.create({
        name,
        description,
        startDate,
        endDate,
      });

      nav("/programs", { replace: true });
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to create program."
      );
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto">
      <Card>
        <CardHeader title="Create program" />

        {error && (
          <div className="mb-3 text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
            {error}
          </div>
        )}

        <ProgramForm
          onSubmit={handleSubmit}
          submitLabel={busy ? "Creating…" : "Create program"}
          disabled={busy}
        />
      </Card>
    </div>
  );
}
