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

  const submit = async () => {
    setErrors([]);
    setServerError(null);

    if (!username || !fullName || !password || !confirm) {
      setErrors(["Συμπλήρωσε όλα τα πεδία."]);
      return;
    }

    if (password !== confirm) {
      setErrors(["Τα passwords δεν ταιριάζουν."]);
      return;
    }

    const result = validatePassword(password, username, fullName);
    if (!result.valid) {
      setErrors(result.violations.map((v) => v.message));
      return;
    }

    try {
      setSubmitting(true);
      await authApi.register(username, password, fullName);
      nav("/login");
    } catch (e: any) {
      setServerError(
        e?.response?.data?.message || "Αποτυχία εγγραφής. Δοκίμασε ξανά."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-100">
      <div className="w-full max-w-md bg-white rounded-lg shadow-md p-6 space-y-5">
        <h1 className="text-xl font-semibold text-slate-800">
          Δημιουργία λογαριασμού
        </h1>

        <div className="space-y-3">
          <Input
            label="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />

          <Input
            label="Ονοματεπώνυμο"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
          />

          <Input
            type="password"
            label="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <Input
            type="password"
            label="Επιβεβαίωση password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
          />
        </div>

        {errors.length > 0 && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-md p-3 space-y-1">
            {errors.map((e, i) => (
              <div key={i}>• {e}</div>
            ))}
          </div>
        )}

        {serverError && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-md p-3">
            {serverError}
          </div>
        )}

        <Button
          className="w-full"
          onClick={submit}
          disabled={submitting}
        >
          {submitting ? "Δημιουργία..." : "Create account"}
        </Button>

        <div className="text-xs text-slate-600 bg-slate-50 border rounded-md p-3 space-y-1">
          <div className="font-medium mb-1">Κανόνες password</div>
          <ul className="list-disc pl-4 space-y-0.5">
            <li>Ελάχιστο μήκος 10 χαρακτήρες</li>
            <li>
              Τουλάχιστον ένα κεφαλαίο, ένα μικρό, ένα ψηφίο, ένα σύμβολο
            </li>
            <li>Όχι ακολουθίες όπως 12345 ή abcde</li>
            <li>Όχι προφανή password (password, 123456, qwerty)</li>
            <li>
              Να μην περιέχει το username ή κομμάτια από το όνομά σου
            </li>
          </ul>
        </div>

        <div className="text-sm text-center text-slate-600">
          Έχεις ήδη λογαριασμό;{" "}
          <NavLink
            to="/login"
            className="text-blue-600 hover:underline font-medium"
          >
            Σύνδεση
          </NavLink>
        </div>
      </div>
    </div>
  );
}
