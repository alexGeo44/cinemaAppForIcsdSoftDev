import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { useEffect } from "react";
import { router } from "./app/router";
import "./styles/main.css";
import { authStore } from "./auth/auth.store";

function Bootstrap() {
  const loadMe = authStore((s) => s.loadMe);
  const bootstrapped = authStore((s) => s.bootstrapped);

  useEffect(() => {
    loadMe(); // ✅ πάντα, μια φορά στην εκκίνηση
  }, [loadMe]);

  if (!bootstrapped) {
    return (
      <div className="h-screen flex items-center justify-center text-sm text-slate-400 bg-slate-950">
        Loading...
      </div>
    );
  }

  return <RouterProvider router={router} />;
}

ReactDOM.createRoot(document.getElementById("root")!).render(<Bootstrap />);
