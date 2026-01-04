import { useEffect, useMemo, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";

export type ProgramFormValues = {
  name: string;
  description: string;
  startDate: string; // yyyy-MM-dd (required)
  endDate: string;   // yyyy-MM-dd (required)
};

type Props = {
  onSubmit: (values: ProgramFormValues) => Promise<void> | void;
  submitLabel?: string;
  disabled?: boolean;

  // ✅ accept both names to avoid breaking pages
  initial?: Partial<ProgramFormValues>;
  initialValues?: Partial<ProgramFormValues>;
};

export function ProgramForm({
  onSubmit,
  submitLabel = "Save",
  disabled = false,
  initial,
  initialValues,
}: Props) {
  const seed = useMemo(() => {
    const src = initialValues ?? initial ?? {};
    return {
      name: src.name ?? "",
      description: src.description ?? "",
      startDate: src.startDate ?? "",
      endDate: src.endDate ?? "",
    };
  }, [initial, initialValues]);

  const [name, setName] = useState(seed.name);
  const [description, setDescription] = useState(seed.description);
  const [startDate, setStartDate] = useState(seed.startDate);
  const [endDate, setEndDate] = useState(seed.endDate);

  const [err, setErr] = useState<string | null>(null);

  // ✅ sync form when initial values arrive/ change (edit flow)
  useEffect(() => {
    setName(seed.name);
    setDescription(seed.description);
    setStartDate(seed.startDate);
    setEndDate(seed.endDate);
    setErr(null);
  }, [seed.name, seed.description, seed.startDate, seed.endDate]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (disabled) return;

    setErr(null);

    const n = name.trim();
    const d = description.trim();
    const s = startDate;
    const en = endDate;

    if (!n) return setErr("Το όνομα είναι υποχρεωτικό.");
    if (!d) return setErr("Η περιγραφή είναι υποχρεωτική.");
    if (!s) return setErr("Το start date είναι υποχρεωτικό.");
    if (!en) return setErr("Το end date είναι υποχρεωτικό.");

    if (s > en) return setErr("Το start date δεν μπορεί να είναι μετά το end date.");

    await onSubmit({
      name: n,
      description: d,
      startDate: s,
      endDate: en,
    });
  };

  const reset = () => {
    setName(seed.name);
    setDescription(seed.description);
    setStartDate(seed.startDate);
    setEndDate(seed.endDate);
    setErr(null);
  };

  return (
    <form onSubmit={submit} className="space-y-4">
      {err && (
        <div className="text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
          {err}
        </div>
      )}

      <Input
        label="Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        disabled={disabled}
      />

      <div>
        <div className="text-xs text-slate-400 mb-1">Description</div>
        <textarea
          className="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 outline-none focus:ring-2 focus:ring-sky-500/60 disabled:opacity-60"
          rows={6}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          disabled={disabled}
        />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <Input
          type="date"
          label="Start date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          disabled={disabled}
        />
        <Input
          type="date"
          label="End date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          disabled={disabled}
        />
      </div>

      <div className="flex gap-2 justify-end">
        <Button type="submit" disabled={disabled}>
          {submitLabel}
        </Button>

        <Button
          type="button"
          variant="secondary"
          disabled={disabled}
          onClick={reset}
        >
          Reset
        </Button>
      </div>
    </form>
  );
}
