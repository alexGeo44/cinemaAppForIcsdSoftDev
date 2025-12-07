import { useForm } from "react-hook-form";
import type{
  CreateScreeningRequest,
  ScreeningResponse,
  UpdateScreeningRequest,
} from "../types";
import { useNavigate, useParams } from "react-router-dom";
import { screeningsApi } from "../api/screeningsApi";
import { useEffect, useState } from "react";

type Mode = "create" | "edit";

export default function ScreeningFormPage() {
  const { id: programId, screeningId } = useParams();
  const mode: Mode = screeningId ? "edit" : "create";
  const navigate = useNavigate();
  const { register, handleSubmit, reset } = useForm<
    CreateScreeningRequest & UpdateScreeningRequest
  >();
  const [loading, setLoading] = useState(mode === "edit");

  useEffect(() => {
    const load = async () => {
      if (mode === "edit" && screeningId) {
        try {
          const s: ScreeningResponse = await screeningsApi.get(
            Number(screeningId)
          );
          reset({
            title: s.title,
            genre: s.genre,
            description: s.description,
          });
        } catch {
          alert("Failed to load screening");
        } finally {
          setLoading(false);
        }
      }
    };
    load();
  }, [mode, screeningId, reset]);

  const onSubmit = async (data: CreateScreeningRequest) => {
    if (!programId) return;
    try {
      if (mode === "create") {
        await screeningsApi.create(Number(programId), data);
      } else if (screeningId) {
        await screeningsApi.update(Number(screeningId), data);
      }
      navigate(`/programs/${programId}`);
    } catch {
      alert("Save failed");
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="card">
      <h2>{mode === "create" ? "Create screening" : "Edit screening"}</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="form">
        <label>
          Title
          <input {...register("title", { required: true })} />
        </label>
        <label>
          Genre
          <input {...register("genre", { required: true })} />
        </label>
        <label>
          Description
          <textarea {...register("description", { required: true })} />
        </label>
        <button type="submit">Save</button>
      </form>
    </div>
  );
}
