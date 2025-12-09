import { ReactNode } from "react";
import Navbar from "./Navbar";
import Sidebar from "./Sidebar";

export default function AppLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-50 flex flex-col">
      {/* Top bar */}
      <Navbar />

      {/* Content + sidebar */}
      <div className="flex flex-1">
        <Sidebar />

        <main className="flex-1 px-6 py-6 overflow-y-auto">
          <div className="max-w-6xl mx-auto">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
