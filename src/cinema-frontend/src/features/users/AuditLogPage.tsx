import { useEffect, useMemo, useState } from "react";
import { auditApi } from "../../api/audit.api";
import type { AuditLog } from "../../domain/audit/audit.types";

type ActionFilter = "ALL" | "LOGIN" | "LOGOUT" | "ACTIVATE_USER" | "DEACTIVATE_USER";

export default function AuditLogPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");
  const [actionFilter, setActionFilter] = useState<ActionFilter>("ALL");

  const refresh = async () => {
    try {
      setLoading(true);
      const res = await auditApi.list();
      setLogs(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const filteredLogs = useMemo(() => {
    return logs.filter((l) => {
      const matchesAction =
        actionFilter === "ALL" || l.action === actionFilter;

      const text = `${l.actorUserId} ${l.action} ${l.target}`.toLowerCase();
      const matchesSearch =
        search.trim().length === 0 ||
        text.includes(search.trim().toLowerCase());

      return matchesAction && matchesSearch;
    });
  }, [logs, search, actionFilter]);

  return (
    <div className="min-h-full bg-slate-950/90 py-8">
      <div className="max-w-6xl mx-auto px-4">
        {/* Header */}
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between mb-6">
          <div>
            <h1 className="text-2xl font-semibold text-slate-50">
              Audit log
            </h1>
            <p className="text-sm text-slate-400">
              Καταγραφή ενεργειών συστήματος (login, logout, ενεργοποιήσεις κ.λπ.).
            </p>
          </div>

          <div className="flex items-center gap-3 text-sm text-slate-300">
            <span className="px-3 py-1 rounded-full bg-slate-800">
              Σύνολο εγγραφών:{" "}
              <span className="font-semibold text-sky-400">
                {logs.length}
              </span>
            </span>
          </div>
        </div>

        {/* Card */}
        <div className="bg-slate-900/80 border border-slate-800 rounded-xl shadow-xl shadow-slate-900/40 overflow-hidden">
          {/* Toolbar */}
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between px-4 py-3 border-b border-slate-800">
            <div className="relative w-full sm:w-80">
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Αναζήτηση σε action / target / user id..."
                className="w-full rounded-lg bg-slate-900 border border-slate-700 px-3 py-2 pl-9 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500/70 focus:border-sky-500"
              />
              <span className="pointer-events-none absolute left-2.5 top-2.5 text-slate-500 text-xs">
                🔍
              </span>
            </div>

            <div className="flex flex-wrap gap-2 text-xs">
              {(["ALL", "LOGIN", "LOGOUT", "ACTIVATE_USER", "DEACTIVATE_USER"] as ActionFilter[]).map(
                (a) => (
                  <button
                    key={a}
                    type="button"
                    onClick={() => setActionFilter(a)}
                    className={`px-3 py-1.5 rounded-full border ${
                      actionFilter === a
                        ? "border-sky-500 bg-sky-500/10 text-sky-300"
                        : "border-slate-700 bg-slate-900 text-slate-300 hover:border-slate-500"
                    }`}
                  >
                    {a === "ALL" ? "Όλα" : a}
                  </button>
                )
              )}
            </div>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-900/90 border-b border-slate-800 text-xs uppercase tracking-wide text-slate-400">
                <tr>
                  <th className="py-3 px-4 text-left">Time</th>
                  <th className="py-3 px-4 text-left">Actor user id</th>
                  <th className="py-3 px-4 text-left">Action</th>
                  <th className="py-3 px-4 text-left">Target</th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td
                      colSpan={4}
                      className="py-6 text-center text-slate-400"
                    >
                      Φόρτωση audit logs...
                    </td>
                  </tr>
                )}

                {!loading && filteredLogs.length === 0 && (
                  <tr>
                    <td
                      colSpan={4}
                      className="py-6 text-center text-slate-500"
                    >
                      Δεν υπάρχουν αποτελέσματα με τα τρέχοντα φίλτρα.
                    </td>
                  </tr>
                )}

                {!loading &&
                  filteredLogs.map((l, idx) => (
                    <tr
                      key={idx}
                      className="border-t border-slate-800/80 hover:bg-slate-900/70 transition-colors"
                    >
                      <td className="py-2.5 px-4 text-slate-200 whitespace-nowrap">
                        {new Date(l.timestamp).toLocaleString()}
                      </td>
                      <td className="py-2.5 px-4 text-slate-300">
                        #{l.actorUserId}
                      </td>
                      <td className="py-2.5 px-4">
                        <span className="inline-flex items-center rounded-full bg-slate-800/70 px-2.5 py-0.5 text-[11px] font-medium text-slate-100">
                          {l.action}
                        </span>
                      </td>
                      <td className="py-2.5 px-4 text-slate-200">
                        {l.target}
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
