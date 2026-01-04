import React, { useEffect, useMemo, useState } from "react";
import { Navigate, useNavigate, useParams } from "react-router-dom";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";
import { screeningsApi } from "../../api/screenings.api";
import type { Screening } from "../../domain/screenings/screening.types";
import { ScreeningState } from "../../domain/screenings/screening.enums";
import { authStore } from "../../auth/auth.store";

export default function ScreeningEditPage() {
  const { id } = useParams();
  const nav = useNavigate();
  const user = authStore((s) => s.user);

  const screeningId = useMemo(() => {
    const n = Number(id);
    return Number.isFinite(n) && n > 0 ? n : null;
  }, [id]);

  const [screening, setScreening] = useState<Screening | null>(null);
  const [loading, setLoading] = useState(true);

  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [description, setDescription] = useState("");

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!screeningId) return;

    let cancelled = false;

    (async () => {
      try {
        setLoading(true);
        setError(null);

        const res = await screeningsApi.view(screeningId);
        if (cancelled) return;

        setScreening(res.data);
        setTitle(res.data.title ?? "");
        setGenre(res.data.genre ?? "");
        setDescription(res.data.description ?? "");
      } catch (e: any) {
        if (!cancelled) {
          setError(
            e?.response?.data?.message ||
              e?.response?.data?.error ||
              e?.message ||
              "Failed to load screening."
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [screeningId]);

  if (!user) return <Navigate to="/login" replace />;

  if (!screeningId) {
    return (
      <div className="max-w-4xl mx-auto p-4 text-sm text-slate-300">
        Missing/invalid screening id.
      </div>
    );
  }

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto p-4 text-sm text-slate-300">
        Loading…
      </div>
    );
  }

  if (!screening) {
    return (
      <div className="max-w-4xl mx-auto p-4 text-sm text-slate-300">
        Screening not found.
      </div>
    );
  }

  // ✅ submitterId/state are optional (role-aware DTOs)
  const isOwner = screening.submitterId != null && user.id === screening.submitterId;
  const canEdit = isOwner && screening.state === ScreeningState.CREATED;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canEdit || busy) return;

    setError(null);

    if (!title.trim()) {
      setError("Title is required.");
      return;
    }

    try {
      setBusy(true);

      await screeningsApi.update(screening.id, {
        title: title.trim(),
        genre: genre.trim(),
        description: description.trim(),
      });

      nav(`/screenings/${screening.id}`, { replace: true });
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to update screening."
      );
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-4">
      <Card>
        <CardHeader
          title="Edit screening"
          subtitle={`#${screening.id} • Program #${screening.programId}`}
          extra={
            <Button
              variant="ghost"
              type="button"
              onClick={() => nav(`/screenings/${screening.id}`)}
              disabled={busy}
            >
              Back
            </Button>
          }
        />

        {!canEdit && (
          <div className="mb-3 text-xs text-amber-200 border border-amber-500/30 bg-amber-500/10 rounded-md px-3 py-2">
            Δεν μπορείς να κάνεις edit αυτό το screening. Επιτρέπεται μόνο στον submitter και μόνο όταν είναι{" "}
            <b>CREATED</b>.
          </div>
        )}

        {error && (
          <div className="mb-3 text-xs text-red-300 border border-red-500/30 bg-red-500/10 rounded-md px-3 py-2">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <CardSection title="Details">
            <div className="space-y-3">
              <Input
                label="Title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                disabled={busy || !canEdit}
              />

              <Input
                label="Genre"
                value={genre}
                onChange={(e) => setGenre(e.target.value)}
                disabled={busy || !canEdit}
              />

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-slate-400 uppercase tracking-wide">
                  Description
                </label>
                <textarea
                  className="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 outline-none focus:ring-2 focus:ring-sky-500/60 min-h-[120px] disabled:opacity-60"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  disabled={busy || !canEdit}
                />
              </div>
            </div>
          </CardSection>

          <CardSection title="Actions">
            <div className="flex flex-wrap gap-2 justify-end">
              <Button
                type="button"
                variant="secondary"
                onClick={() => nav(`/screenings/${screening.id}`)}
                disabled={busy}
              >
                Cancel
              </Button>

              <Button type="submit" disabled={busy || !canEdit}>
                {busy ? "Saving…" : "Save changes"}
              </Button>
            </div>
          </CardSection>
        </form>
      </Card>
    </div>
  );
}
