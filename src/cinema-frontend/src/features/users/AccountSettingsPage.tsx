import { useState } from "react";
import { usersApi } from "../../api/users.api";
import { authStore } from "../../auth/auth.store";
import { useNavigate } from "react-router-dom";
import { validatePassword } from "../../auth/passwordPolicy";


export default function AccountSettingsPage() {
  const user = authStore((s) => s.user);
  const logout = authStore((s) => s.logout);
  const nav = useNavigate();

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmNew, setConfirmNew] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (!user) return <div>Must be logged in</div>;

  const handleChangePassword = async () => {
    setMessage(null);
    setError(null);

    if (!currentPassword || !newPassword) {
      setError("Συμπλήρωσε όλα τα πεδία.");
      return;
    }
    if (newPassword !== confirmNew) {
      setError("Το νέο password και το confirm δεν ταιριάζουν.");
      return;
    }

    // 🔐 FE validation ίδιο με backend
    const result = validatePassword(newPassword, user.userName, user.fullName);
     if (!result.valid) {
        setError(result.violations.map((v) => v.message).join(" | "));
        return;
     }

    try {
      await usersApi.changePassword(user.id, {
        oldPassword: currentPassword,
        newPassword,
      });
      setMessage("Ο κωδικός άλλαξε με επιτυχία.");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmNew("");
    } catch (e: any) {
      setError(
        e?.response?.data?.message || "Αποτυχία αλλαγής κωδικού. Έλεγξε τα στοιχεία."
      );
    }
  };

  const handleDeactivate = async () => {
    if (!window.confirm("Σίγουρα θέλεις να απενεργοποιήσεις τον λογαριασμό σου;"))
      return;
    try {
      await usersApi.deactivate(user.id);
      logout();
      nav("/login");
    } catch (e) {
      setError("Αποτυχία απενεργοποίησης λογαριασμού.");
    }
  };

  const handleDelete = async () => {
    const phrase = "DELETE";
    const input = window.prompt(
      `Πληκτρολόγησε ${phrase} για να διαγράψεις οριστικά τον λογαριασμό σου`
    );
    if (input !== phrase) return;

    try {
      await usersApi.delete(user.id);
      logout();
      nav("/login");
    } catch (e) {
      setError("Αποτυχία διαγραφής λογαριασμού.");
    }
  };

  return (
    <div>
      <h1>Account settings</h1>

      <section style={{ marginTop: "1rem" }}>
        <h2>Προφίλ</h2>
        <p>
          <b>Username:</b> {user.userName}
          <br />
          <b>Full name:</b> {user.fullName}
          <br />
          <b>Role:</b> {user.role}
        </p>
      </section>

      <section style={{ marginTop: "1.5rem" }}>
        <h2>Αλλαγή κωδικού</h2>

        <div>
          <input
            type="password"
            placeholder="Τρέχων κωδικός"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
          />
        </div>
        <div>
          <input
            type="password"
            placeholder="Νέος κωδικός"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
        </div>
        <div>
          <input
            type="password"
            placeholder="Επιβεβαίωση νέου κωδικού"
            value={confirmNew}
            onChange={(e) => setConfirmNew(e.target.value)}
          />
        </div>
        <button onClick={handleChangePassword}>Αλλαγή κωδικού</button>
      </section>

      <section style={{ marginTop: "1.5rem" }}>
        <h2>Κατάσταση λογαριασμού</h2>
        <button onClick={handleDeactivate}>Απενεργοποίηση λογαριασμού</button>
        <br />
        <button
          onClick={handleDelete}
          style={{ marginTop: "0.5rem", color: "red" }}
        >
          Οριστική διαγραφή λογαριασμού
        </button>
      </section>

      {message && (
        <div style={{ marginTop: "1rem", color: "green" }}>{message}</div>
      )}
      {error && <div style={{ marginTop: "1rem", color: "red" }}>{error}</div>}
    </div>
  );
}
