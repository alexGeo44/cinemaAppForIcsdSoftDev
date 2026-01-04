import { useState } from "react";
import { useNavigate, NavLink } from "react-router-dom";
import { authApi } from "../../api/auth.api";
import { validatePassword } from "../../auth/passwordPolicy";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";

export default function RegisterPage() {
  const nav = useNavigate();

  const [username, setUsername] = useState("");
  const [fullName, setFullName] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");

  const [errors, setErrors] = useState<string[]>([]);
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const extractServerMessage = (e: any) => {
    const status = e?.response?.status;
    const msg =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.message ||
      "Αποτυχία εγγραφής. Δοκίμασε ξανά.";

    // προαιρετικό: κάνε πιο readable τα κοινά status
    if (status === 400) return `400 Bad Request: ${msg}`;
    if (status === 401) return `401 Unauthorized: ${msg}`;
    if (status === 403) return `403 Forbidden: ${msg}`;
    if (status === 409) return `409 Conflict: ${msg}`;
    return msg;
  };

  const submit = async () => {
    setErrors([]);
    setServerError(null);

    const u = username.trim();
    const fn = fullName.trim();

    if (!u || !fn || !password || !confirm) {
      setErrors(["Συμπλήρωσε όλα τα πεδία."]);
      return;
    }

    if (password !== confirm) {
      setErrors(["Τα passwords δεν ταιριάζουν."]);
      return;
    }

    const result = validatePassword(password, u, fn);
    if (!result.valid) {
      setErrors(result.violations.map((v) => v.message));
      return;
    }

    try {
      setSubmitting(true);

      // API call
      await authApi.register(u, password, fn);

      nav("/login", { replace: true });
    } catch (e: any) {
      setServerError(extractServerMessage(e));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950">
      <form
        className="w-full max-w-md bg-slate-900/80 border border-slate-800 rounded-xl shadow-xl shadow-slate-950 p-8 space-y-6 backdrop-blur"
        onSubmit={(e) => {
          e.preventDefault();
          if (!submitting) submit();
        }}
      >
        <div className="text-center space-y-1">
          <h1 className="text-2xl font-semibold text-slate-100">
            Δημιουργία λογαριασμού
          </h1>
          <p className="text-slate-400 text-sm">
            Συμπλήρωσε τα στοιχεία για να συνεχίσεις.
          </p>
        </div>

        <div className="space-y-4">
          <Input
            name="username"
            autoComplete="username"
            label="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={submitting}
          />
          <Input
            name="fullName"
            autoComplete="name"
            label="Ονοματεπώνυμο"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            disabled={submitting}
          />
          <Input
            name="password"
            autoComplete="new-password"
            type="password"
            label="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={submitting}
          />
          <Input
            name="confirmPassword"
            autoComplete="new-password"
            type="password"
            label="Επιβεβαίωση password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            disabled={submitting}
          />
        </div>

        {errors.length > 0 && (
          <div className="bg-rose-500/10 border border-rose-600/50 text-rose-300 text-sm rounded-md p-3 space-y-1">
            {errors.map((e, i) => (
              <div key={i}>• {e}</div>
            ))}
          </div>
        )}

        {serverError && (
          <div className="bg-rose-500/10 border border-rose-600/50 text-rose-300 text-sm rounded-md p-3">
            {serverError}
          </div>
        )}

        <Button
          className="w-full py-2.5 text-[15px]"
          type="submit"
          disabled={submitting}
        >
          {submitting ? "Δημιουργία..." : "Δημιουργία λογαριασμού"}
        </Button>

        <div className="text-xs text-slate-400 bg-slate-900/70 border border-slate-800 rounded-lg p-4 space-y-1">
          <div className="font-medium text-slate-300 mb-1">Κανόνες password</div>
          <ul className="list-disc pl-4 space-y-0.5">
            <li>Ελάχιστο μήκος 10 χαρακτήρες</li>
            <li>Τουλάχιστον ένα κεφαλαίο, μικρό, ψηφίο & σύμβολο</li>
            <li>Όχι ακολουθίες (12345, abcde)</li>
            <li>Όχι προφανή passwords (password, qwerty)</li>
            <li>Να μην περιέχει το username ή κομμάτια του ονόματος</li>
          </ul>
        </div>

        <div className="text-sm text-center text-slate-400">
          Έχεις ήδη λογαριασμό;{" "}
          <NavLink
            to="/login"
            className="text-sky-400 hover:underline"

            onClick={(e) => {
              if (submitting) e.preventDefault();
            }}
          >
            Σύνδεση
          </NavLink>
        </div>
      </form>
    </div>
  );
}
