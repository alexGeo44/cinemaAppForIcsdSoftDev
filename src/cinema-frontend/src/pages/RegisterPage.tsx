import { useForm } from "react-hook-form";
import type { RegisterUserRequest } from "../types";
import { authApi } from "../api/authApi";
import { useNavigate } from "react-router-dom";

type FormValues = RegisterUserRequest & { confirmPassword: string };

export default function RegisterPage() {
  const { register, handleSubmit, watch } = useForm<FormValues>();
  const navigate = useNavigate();
  const password = watch("password");

  const onSubmit = async (data: FormValues) => {
    if (data.password !== data.confirmPassword) {
      alert("Passwords do not match");
      return;
    }
    try {
      await authApi.register({
        username: data.username,
        password: data.password,
        fullName: data.fullName,
      });
      alert("Registration successful, you may need ADMIN activation");
      navigate("/login");
    } catch {
      alert("Registration failed (username may exist)");
    }
  };

  return (
    <div className="card auth-card">
      <h2>Register</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="form">
        <label>
          Full name
          <input {...register("fullName", { required: true })} />
        </label>
        <label>
          Username
          <input {...register("username", { required: true })} />
        </label>
        <label>
          Password
          <input
            type="password"
            {...register("password", { required: true, minLength: 8 })}
          />
        </label>
        <label>
          Confirm password
          <input
            type="password"
            {...register("confirmPassword", { required: true })}
          />
        </label>
        <button type="submit">Register</button>
      </form>
    </div>
  );
}
