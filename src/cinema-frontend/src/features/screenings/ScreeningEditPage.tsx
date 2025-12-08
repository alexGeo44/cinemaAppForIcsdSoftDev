import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Card, CardHeader } from "../../components/ui/Card";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";
import { screeningsApi } from "../../api/screenings.api";
import { Screening } from "../../domain/screenings/screening.types";
import { authStore } from "../../auth/auth.store";

export default function ScreeningEditPage() {
  const { id } = useParams();
  const [screening, setScreening] = useState<Screening | null>(null);
  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);

  const user = authStore((s) => s.user);
  const nav = useNavigate();

  useEffect(() => {
    if (!id) return;
    screeningsApi
      .view(Number(id))
      .then((res) => {
        setScreening(res.data);
        setTitle(res.data.title);
        setGenre(res.data.genre || "");
        setDescription(res.data.description || "");
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (!user) return <div>Must be logged in</div>;
  if (loading) return <div>Loading…</div>;
  if (!screening) return <div>Screening not found.</div>;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!title.trim()) {
      setError("Title is required.");
      return;
    }

    try {
      await screeningsApi.update(user.id, screening.id, {
        title,
        genre,
        description,
      });
      nav(`/screenings/${screening.id}`);
    } catch (e) {
      setError("Failed to update screening.");
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <Card>
        <CardHeader title="Edit screening" />
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <Input
            label="Genre"
            value={genre}
            onChange={(e) => setGenre(e.target.value)}
          />
          <div className="space-y-1">
            <label className="block text-xs font-medium text-slate-600">
              Description
            </label>
            <textarea
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-slate-900 min-h-[80px]"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          {error && <div className="text-sm text-red-600">{error}</div>}

          <Button type="submit">Save changes</Button>
        </form>
      </Card>
    </div>
  );
}
