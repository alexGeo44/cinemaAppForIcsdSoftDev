import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authStore } from "../../auth/auth.store";
import { validatePassword } from "../../auth/passwordPolicy";
import { meApi } from "../../api/me.api";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";

export default function AccountSettingsPage() {
  const user = authStore((s) => s.user);
  const logout = authStore((s) => s.logout);
  const nav = useNavigate();

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmNew, setConfirmNew] = useState("");

  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [changing, setChanging] = useState(false);
  const [deactivating, setDeactivating] = useState(false);
  const [deleting, setDeleting] = useState(false);

  if (!user) return <div className="text-slate-300">Must be logged in</div>;

  const busy = changing || deactivating || deleting;

  const resetAlerts = () => {
    setMessage(null);
    setError(null);
  };

  const handleChangePassword = async () => {
    resetAlerts();

    if (!currentPassword || !newPassword || !confirmNew) {
      setError("Συμπλήρωσε όλα τα πεδία.");
      return;
    }
    if (newPassword !== confirmNew) {
      setError("Το νέο password και το confirm δεν ταιριάζουν.");
      return;
    }

    const result = validatePassword(newPassword, user.userName, user.fullName);
    if (!result.valid) {
      setError(result.violations.map((v) => v.message).join(" | "));
      return;
    }

    try {
      setChanging(true);

      await meApi.changePassword({
        oldPassword: currentPassword,
        newPassword,
        newPasswordRepeat: confirmNew,
      });

      setMessage("Ο κωδικός άλλαξε με επιτυχία.");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmNew("");
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          "Αποτυχία αλλαγής κωδικού. Έλεγξε τα στοιχεία."
      );
    } finally {
      setChanging(false);
    }
  };

  const handleDeactivate = async () => {
    resetAlerts();
    if (!window.confirm("Σίγουρα θέλεις να απενεργοποιήσεις τον λογαριασμό σου;")) return;

    try {
      setDeactivating(true);
      await meApi.deactivate();
      logout();
      nav("/login", { replace: true });
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          "Αποτυχία απενεργοποίησης λογαριασμού."
      );
    } finally {
      setDeactivating(false);
    }
  };

  const handleDelete = async () => {
    resetAlerts();

    const phrase = "DELETE";
    const input = window.prompt(
      `Πληκτρολόγησε ${phrase} για να διαγράψεις οριστικά τον λογαριασμό σου`
    );
    if (input !== phrase) return;

    try {
      setDeleting(true);
      await meApi.deleteMe();
      logout();
      nav("/login", { replace: true });
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          "Αποτυχία διαγραφής λογαριασμού."
      );
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-4">
      <Card>
        <CardHeader title="Account settings" subtitle="Διαχείριση προφίλ & ασφάλειας" />

        {(message || error) && (
          <div className="mt-3 space-y-2">
            {message && (
              <div className="rounded-lg border border-emerald-500/30 bg-emerald-500/10 px-3 py-2 text-sm text-emerald-200">
                {message}
              </div>
            )}
            {error && (
              <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-200">
                {error}
              </div>
            )}
          </div>
        )}

        <CardSection title="Profile">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="rounded-lg border border-slate-800 bg-slate-950 px-3 py-2">
              <div className="text-[11px] uppercase tracking-wide text-slate-500">
                Username
              </div>
              <div className="text-sm text-slate-100 font-medium mt-1">
                {user.userName}
              </div>
            </div>

            <div className="rounded-lg border border-slate-800 bg-slate-950 px-3 py-2">
              <div className="text-[11px] uppercase tracking-wide text-slate-500">
                Full name
              </div>
              <div className="text-sm text-slate-100 font-medium mt-1">
                {user.fullName}
              </div>
            </div>

            <div className="rounded-lg border border-slate-800 bg-slate-950 px-3 py-2">
              <div className="text-[11px] uppercase tracking-wide text-slate-500">
                Role
              </div>
              <div className="text-sm text-slate-100 font-medium mt-1">
                {String(user.role)}
              </div>
            </div>
          </div>
        </CardSection>

        <CardSection title="Change password">
          <form
            className="space-y-3 max-w-xl"
            onSubmit={(e) => {
              e.preventDefault();
              if (!changing) handleChangePassword();
            }}
          >
            <Input
              type="password"
              label="Τρέχων κωδικός"
              placeholder="••••••••"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              disabled={busy}
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <Input
                type="password"
                label="Νέος κωδικός"
                placeholder="••••••••"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                disabled={busy}
              />
              <Input
                type="password"
                label="Επιβεβαίωση νέου κωδικού"
                placeholder="••••••••"
                value={confirmNew}
                onChange={(e) => setConfirmNew(e.target.value)}
                disabled={busy}
              />
            </div>

            <div className="flex items-center gap-2">
              <Button type="submit" disabled={busy} className="min-w-[160px]">
                {changing ? "Αλλαγή..." : "Αλλαγή κωδικού"}
              </Button>

              <Button
                type="button"
                variant="secondary"
                disabled={busy}
                onClick={() => {
                  setCurrentPassword("");
                  setNewPassword("");
                  setConfirmNew("");
                  resetAlerts();
                }}
              >
                Καθαρισμός
              </Button>
            </div>

            <div className="text-xs text-slate-400">
              Tip: Μετά την αλλαγή, το παλιό token μπορεί να ακυρωθεί (ανάλογα το backend).
            </div>
          </form>
        </CardSection>

        <CardSection title="Danger zone">
          <div className="rounded-xl border border-rose-500/25 bg-rose-500/5 p-4">
            <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
              <div>
                <div className="text-sm font-semibold text-slate-50">Λογαριασμός</div>
                <div className="text-xs text-slate-400 mt-1">
                  Απενεργοποίηση: μπορείς να επιστρέψεις με ενεργοποίηση από ADMIN. <br />
                  Διαγραφή: μόνιμη ενέργεια.
                </div>
              </div>

              <div className="flex flex-wrap gap-2">
                <Button
                  type="button"
                  variant="secondary"
                  disabled={busy}
                  onClick={handleDeactivate}
                >
                  {deactivating ? "Απενεργοποίηση..." : "Απενεργοποίηση"}
                </Button>

                <Button
                  type="button"
                  variant="danger"
                  disabled={busy}
                  onClick={handleDelete}
                >
                  {deleting ? "Διαγραφή..." : "Οριστική διαγραφή"}
                </Button>
              </div>
            </div>
          </div>
        </CardSection>
      </Card>
    </div>
  );
}
