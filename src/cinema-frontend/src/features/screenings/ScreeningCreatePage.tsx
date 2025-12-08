import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { screeningsApi } from "../../api/screenings.api";
import { authStore } from "../../auth/auth.store";

export default function ScreeningCreatePage() {
  const [search] = useSearchParams();
  const programIdParam = search.get("programId");
  const programId = programIdParam ? Number(programIdParam) : null;

  const user = authStore((s) => s.user);
  const nav = useNavigate();

  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [description, setDescription] = useState("");

  if (!user) return <div>Must be logged in</div>;
  if (!programId) return <div>Missing programId</div>;

  const submit = async () => {
    await screeningsApi.create(user.id, programId, { title, genre, description });
    nav(`/programs/${programId}`);
  };

  return (
    <div>
      <h1>Create Screening</h1>
      <div>
        <input
          placeholder="Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
      </div>
      <div>
        <input
          placeholder="Genre"
          value={genre}
          onChange={(e) => setGenre(e.target.value)}
        />
      </div>
      <div>
        <textarea
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
      </div>
      <button onClick={submit}>Create</button>
    </div>
  );
}
