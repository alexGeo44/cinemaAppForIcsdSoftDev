import { useState, useEffect } from "react";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";
import { Program } from "../../domain/programs/program.types";

export interface ProgramFormValues {
  name: string;
  description: string;
  startDate: string;
  endDate: string;
}

export function ProgramForm({
  initial,
  onSubmit,
  submitLabel,
}: {
  initial?: Program | null;
  onSubmit: (values: ProgramFormValues) => Promise<void> | void;
  submitLabel: string;
}) {
  const [values, setValues] = useState<ProgramFormValues>({
    name: "",
    description: "",
    startDate: "",
    endDate: "",
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!initial) return;
    setValues({
      name: initial.name,
      description: initial.description || "",
      startDate: initial.startDate || "",
      endDate: initial.endDate || "",
    });
  }, [initial]);

  const handleChange =
    (field: keyof ProgramFormValues) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setValues((v) => ({ ...v, [field]: e.target.value }));
    };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!values.name.trim()) {
      setError("Το όνομα είναι υποχρεωτικό.");
      return;
    }
    if (values.startDate && values.endDate && values.endDate < values.startDate) {
      setError("End date δεν μπορεί να είναι πριν από start date.");
      return;
    }

    try {
      setLoading(true);
      await onSubmit(values);
    } catch (err: any) {
      setError("Κάτι πήγε στραβά. Δοκίμασε ξανά.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 max-w-xl">
      <Input
        label="Name"
        value={values.name}
        onChange={handleChange("name")}
      />
      <div className="space-y-1">
        <label className="block text-xs font-medium text-slate-600">
          Description
        </label>
        <textarea
          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-slate-900 min-h-[80px]"
          value={values.description}
          onChange={handleChange("description")}
        />
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <Input
          type="date"
          label="Start date"
          value={values.startDate}
          onChange={handleChange("startDate")}
        />
        <Input
          type="date"
          label="End date"
          value={values.endDate}
          onChange={handleChange("endDate")}
        />
      </div>

      {error && <div className="text-sm text-red-600">{error}</div>}

      <Button type="submit" disabled={loading}>
        {loading ? "Saving..." : submitLabel}
      </Button>
    </form>
  );
}
