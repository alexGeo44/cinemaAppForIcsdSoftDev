import { useEffect, useState } from "react";
import { usersApi } from "../api/usersApi";
import type { UserResponse } from "../types";
import { useAuth } from "../auth/AuthContext";

export default function AdminUsersPage() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const { user } = useAuth();

  useEffect(() => {
    const load = async () => {
      try {
        const res = await usersApi.list();
        setUsers(res);
      } catch {
        // ίσως δεν έχεις GET /users
      }
    };
    load();
  }, []);

  if (!user || user.role !== "ADMIN") {
    return <div>Not authorized</div>;
  }

  const handleDeactivate = async (u: UserResponse) => {
    try {
      await usersApi.deactivate(u.id);
      setUsers((prev) =>
        prev.map((x) => (x.id === u.id ? { ...x } : x))
      );
      alert("Deactivated (ή toggled) – δες backend λογική");
    } catch {
      alert("Failed");
    }
  };

  const handleDelete = async (u: UserResponse) => {
    if (u.role === "ADMIN") {
      alert("Cannot delete ADMIN");
      return;
    }
    if (!window.confirm(`Delete user ${u.userName}?`)) return;
    try {
      await usersApi.delete(u.id);
      setUsers((prev) => prev.filter((x) => x.id !== u.id));
    } catch {
      alert("Delete failed");
    }
  };

  return (
    <div>
      <h2>Users (ADMIN)</h2>
      <table className="table">
        <thead>
          <tr>
            <th>Username</th>
            <th>Full name</th>
            <th>Role</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.userName}</td>
              <td>{u.fullName}</td>
              <td>{u.role}</td>
              <td>
                <button onClick={() => handleDeactivate(u)}>
                  Deactivate
                </button>
                <button className="danger" onClick={() => handleDelete(u)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {users.length === 0 && (
            <tr>
              <td colSpan={4}>No users / endpoint missing.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
