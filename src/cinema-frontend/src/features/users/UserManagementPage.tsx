import { useEffect, useState } from "react";
import { usersApi } from "../../api/users.api";
import { User, BaseRole } from "../../domain/auth/auth.types";
import { authStore } from "../../auth/auth.store";
import { Card, CardHeader } from "../../components/ui/Card";
import { Button } from "../../components/ui/Button";

export default function UserManagementPage() {
  const currentUser = authStore((s) => s.user);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // μόνο ADMIN
  if (!currentUser) return <div>Must be logged in.</div>;
  if (currentUser.role !== BaseRole.ADMIN) {
    return (
      <div className="max-w-3xl mx-auto mt-8 text-sm text-red-600">
        Δεν έχεις δικαίωμα πρόσβασης σε αυτή τη σελίδα (μόνο ADMIN).
      </div>
    );
  }

  useEffect(() => {
    const load = async () => {
      try {
        setError(null);
        setLoading(true);
        const res = await usersApi.list();
        setUsers(res.data);
      } catch (e) {
        setError("Αποτυχία φόρτωσης χρηστών.");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const refresh = async () => {
    const res = await usersApi.list();
    setUsers(res.data);
  };

  const handleDeactivate = async (user: User) => {
    const ok = window.confirm(
      `Να απενεργοποιηθεί ο χρήστης "${user.userName}" ;`
    );
    if (!ok) return;
    try {
      await usersApi.deactivate(user.id);
      await refresh();
    } catch {
      alert("Αποτυχία απενεργοποίησης.");
    }
  };

  const handleDelete = async (user: User) => {
    const ok = window.confirm(
      `Οριστική διαγραφή χρήστη "${user.userName}" ;`
    );
    if (!ok) return;
    try {
      await usersApi.delete(user.id);
      await refresh();
    } catch {
      alert("Αποτυχία διαγραφής.");
    }
  };

  return (
    <div className="max-w-5xl mx-auto">
      <Card>
        <CardHeader
          title="User management"
          subtitle="Διαχείριση χρηστών (μόνο ADMIN)."
        />

        {loading ? (
          <div className="text-sm text-slate-500">Loading users…</div>
        ) : error ? (
          <div className="text-sm text-red-600">{error}</div>
        ) : users.length === 0 ? (
          <div className="text-sm text-slate-500">
            Δεν υπάρχουν χρήστες.
          </div>
        ) : (
          <div className="overflow-x-auto mt-3">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b text-xs uppercase text-slate-500">
                  <th className="py-2 text-left px-2">ID</th>
                  <th className="py-2 text-left px-2">Username</th>
                  <th className="py-2 text-left px-2">Full name</th>
                  <th className="py-2 text-left px-2">Role</th>
                  <th className="py-2 text-right px-2">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id} className="border-b last:border-0">
                    <td className="py-2 px-2 text-xs text-slate-500">
                      {u.id}
                    </td>
                    <td className="py-2 px-2 font-mono">{u.userName}</td>
                    <td className="py-2 px-2">{u.fullName}</td>
                    <td className="py-2 px-2 text-xs uppercase">
                      {u.role}
                    </td>
                    <td className="py-2 px-2 text-right space-x-2">
                      <Button
                        variant="secondary"
                        onClick={() => handleDeactivate(u)}
                      >
                        Deactivate
                      </Button>
                      <Button
                        variant="danger"
                        onClick={() => handleDelete(u)}
                      >
                        Delete
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
