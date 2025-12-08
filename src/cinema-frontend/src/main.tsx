import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { router } from "./app/router";
import "./styles/main.css";


ReactDOM.createRoot(document.getElementById("root")!).render(
  <RouterProvider router={router} />
);
