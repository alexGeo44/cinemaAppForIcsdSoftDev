import { useForm } from "react-hook-form";
import type { LoginRequest } from "../types";
import { useAuth } from "../auth/AuthContext";
import { useLocation, useNavigate } from "react-router-dom";

export default function LoginPage() {
  const { register, handleSubmit } = useForm<LoginRequest>();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation() as any;
  const from = location.state?.from?.pathname || "/";

  const onSubmit = async (data: LoginRequest) => {
    try {
      await login(data);
      navigate(from, { replace: true });
    } catch {
      alert("Login failed");
    }
  };

  return (
    <div className="card auth-card">
      <h2>Login</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="form">
        <label>
          Username
          <input {...register("username", { required: true })} />
        </label>
        <label>
          Password
          <input
            type="password"
            {...register("password", { required: true })}
          />
        </label>
        <button type="submit">Login</button>
      </form>
    </div>
  );
}
