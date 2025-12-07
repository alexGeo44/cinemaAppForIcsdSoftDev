import { useForm } from "react-hook-form";
import { useParams, useNavigate } from "react-router-dom";
import type { CreateScreeningRequest } from "../types/api";
import { screeningsApi } from "../api/screeningsApi";
import { useAuth } from "../auth/AuthContext";

export default function CreateScreeningPage() {
  const { id: programId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const { register, handleSubmit } = useForm<CreateScreeningRequest>();

  const onSubmit = async (data: CreateScreeningRequest) => {
    if (!user || !programId) {
      alert("Πρέπει να είσαι συνδεδεμένος");
      return;
    }
    await screeningsApi.create(user.id, Number(programId), data);
    navigate(`/programs/${programId}`);
  };

  return (
    <div style={{ padding: "1rem" }}>
      <h2>Create Screening</h2>
      <form onSubmit={handleSubmit(onSubmit)}>
        <label>
          Title
          <input {...register("title", { required: true })} />
        </label>
        <br />
        <label>
          Genre
          <input {...register("genre", { required: true })} />
        </label>
        <br />
        <label>
          Description
          <textarea {...register("description", { required: true })} />
        </label>
        <br />
        <button type="submit">Create</button>
      </form>
    </div>
  );
}
