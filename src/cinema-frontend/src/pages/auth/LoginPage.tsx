import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation() as any;

  const [form, setForm] = useState({
    username: "",
    password: ""
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login({
        username: form.username,
        password: form.password
      });

      const redirectTo = location.state?.from?.pathname || "/";
      navigate(redirectTo, { replace: true });
    } catch (err: any) {
      console.error(err);
      let msg = "Αποτυχία σύνδεσης";

      if (err.response?.data) {
        const data = err.response.data;
        if (typeof data === "string") {
          msg = data;
        } else if (typeof data.message === "string") {
          msg = data.message;
        }
      }

      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h1>Login</h1>

        {error && <div className="error-box">{error}</div>}

        <div className="form-field">
          <label className="form-label" htmlFor="username">
            Username
          </label>
          <input
            id="username"
            name="username"
            className="form-input"
            value={form.username}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-field">
          <label className="form-label" htmlFor="password">
            Password
          </label>
          <input
            id="password"
            name="password"
            type="password"
            className="form-input"
            value={form.password}
            onChange={handleChange}
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>

        <p style={{ marginTop: "1rem" }}>
          Δεν έχεις λογαριασμό;{" "}
          <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  );
};

export default LoginPage;
