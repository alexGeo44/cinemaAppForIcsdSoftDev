import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../../api/authApi";

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: "",
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
      // Προσοχή: τα ονόματα πεδίων ΠΡΕΠΕΙ να ταιριάζουν με το backend
      await authApi.register({
        fullName: form.fullName,
        username: form.username,
        password: form.password
      });

      navigate("/login");
    } catch (err: any) {
      console.error(err);
      let msg = "Αποτυχία εγγραφής";

      // Πάρε μήνυμα από backend αν υπάρχει
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
        <h1>Register</h1>

        {error && <div className="error-box">{error}</div>}

        <div className="form-field">
          <label className="form-label" htmlFor="fullName">
            Full name
          </label>
          <input
            id="fullName"
            name="fullName"
            className="form-input"
            value={form.fullName}
            onChange={handleChange}
            required
          />
        </div>

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
          {loading ? "Registering..." : "Register"}
        </button>

        <p style={{ marginTop: "1rem" }}>
          Έχεις ήδη λογαριασμό;{" "}
          <Link to="/login">Login</Link>
        </p>
      </form>
    </div>
  );
};

export default RegisterPage;
