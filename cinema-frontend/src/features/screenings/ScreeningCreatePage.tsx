import { FormEvent, useEffect, useMemo, useState } from "react";
import { Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { programsApi } from "../../api/programs.api";
import { authStore } from "../../auth/auth.store";
import { Card, CardHeader, CardSection } from "../../components/ui/Card";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";
import { isCreatorOfProgram, type ProgramLike } from "../../auth/permissions";

type ProgramExtra = {
  id?: number;
  state?: string;
  programmerIds?: Array<number | string>;
  programmers?: Array<number | string>;
  creatorUserId?: number | string;
};

export default function ScreeningCreatePage() {
  const [search] = useSearchParams();
  const nav = useNavigate();
  const user = authStore((s) => s.user);

  const programId = useMemo(() => {
    const p = search.get("programId");
    if (!p) return null;
    const n = Number(p);
    return Number.isFinite(n) && n > 0 ? n : null;
  }, [search]);

  const [program, setProgram] = useState<ProgramExtra | null>(null);
  const [checking, setChecking] = useState(true);

  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [description, setDescription] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!user) return <Navigate to="/login" replace />;

  // load program to enforce "CREATOR cannot create screening in own program"
  useEffect(() => {
    let alive = true;

    async function run() {
      if (!programId) {
        if (alive) setChecking(false);
        return;
      }

      try {
        setError(null);
        setChecking(true);
        const res = await programsApi.view(programId);
        if (!alive) return;
        setProgram(res.data as any);
      } catch (e: any) {
        if (!alive) return;
        setProgram(null);
        setError(
          e?.response?.data?.message ||
            e?.response?.data?.error ||
            e?.message ||
            "Failed to load program."
        );
      } finally {
        if (alive) setChecking(false);
      }
    }

    run();
    return () => {
      alive = false;
    };
  }, [programId]);

  // ✅ block ONLY if user is CREATOR of THIS program
  const isCreatorOfThisProgram = useMemo(() => {
    if (!user || !program) return false;

    const p: ProgramLike = {
      creatorUserId:
        program.creatorUserId == null ? undefined : Number(program.creatorUserId),
      state: program.state,
      // δεν χρειάζονται για το rule, αλλά τα αφήνω safe
      programmerIds: (program.programmerIds ?? [])
        .map((x) => Number(x))
        .filter(Number.isFinite) as number[],
      programmers: (program.programmers ?? [])
        .map((x) => Number(x))
        .filter(Number.isFinite) as number[],
    };

    return isCreatorOfProgram(p, user.id);
  }, [user?.id, program]);

  if (!programId) {
    return (
      <div className="max-w-3xl mx-auto p-4 text-slate-300">
        Missing/invalid <span className="text-slate-100 font-medium">programId</span>.
        <div className="mt-2 text-xs text-slate-500">
          Άνοιξε τη σελίδα ως:{" "}
          <span className="text-slate-200">/screenings/new?programId=123</span>
        </div>
      </div>
    );
  }

  if (checking) {
    return <div className="max-w-3xl mx-auto p-4 text-slate-300">Loading…</div>;
  }

  // ✅ Frontend guard: ONLY CREATOR cannot create screening in own program
  if (isCreatorOfThisProgram) {
    return (
      <div className="max-w-3xl mx-auto space-y-4">
        <Card>
          <CardHeader title="Create screening" subtitle={`Program #${programId}`} />
          <CardSection title="Not allowed">
            <div className="rounded-md border border-amber-400/30 bg-amber-500/10 p-3 text-sm text-amber-200">
              Ως <b>CREATOR</b> αυτού του προγράμματος δεν επιτρέπεται να υποβάλεις screenings στο ίδιο σου το program.
            </div>

            {process.env.NODE_ENV !== "production" && (
              <div className="mt-2 text-[11px] text-slate-500">
                debug: userId={user.id} creatorUserId={String(program?.creatorUserId)}
              </div>
            )}

            <div className="mt-3">
              <Button variant="secondary" onClick={() => nav(`/programs/${programId}`)}>
                Back to program
              </Button>
            </div>
          </CardSection>
        </Card>
      </div>
    );
  }

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!title.trim()) {
      setError("Title is required.");
      return;
    }

    try {
      setLoading(true);

      const res = await screeningsApi.create(programId, {
        title: title.trim(),
        genre: genre.trim() || "",
        description: description.trim() || "",
      });

      nav(`/screenings/${res.data.id}`);
    } catch (e: any) {
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to create screening."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-4">
      <Card>
        <CardHeader title="Create screening" subtitle={`Program #${programId}`} />

        <CardSection title="Details">
          <form onSubmit={submit} className="space-y-4">
            <Input
              label="Title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              disabled={loading}
            />
            <Input
              label="Genre"
              value={genre}
              onChange={(e) => setGenre(e.target.value)}
              disabled={loading}
            />

            <div className="space-y-1">
              <label className="block text-xs font-medium text-slate-300">
                Description
              </label>
              <textarea
                className="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 outline-none focus:ring-2 focus:ring-sky-500/60 min-h-[110px] disabled:opacity-60"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                disabled={loading}
              />
            </div>

            {error && (
              <div className="rounded-md border border-red-400/30 bg-red-500/10 p-3 text-sm text-red-200">
                {error}
              </div>
            )}

            <div className="flex gap-2">
              <Button type="submit" disabled={loading}>
                {loading ? "Creating…" : "Create"}
              </Button>
              <Button
                type="button"
                variant="secondary"
                onClick={() => nav(`/programs/${programId}`)}
                disabled={loading}
              >
                Back to program
              </Button>
            </div>
          </form>
        </CardSection>
      </Card>
    </div>
  );
}
