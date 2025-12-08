interface BadgeProps {
  children: React.ReactNode;
  color?: "default" | "success" | "danger" | "warning";
}

export function Badge({ children, color = "default" }: BadgeProps) {
  const base = "inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium";
  const variant =
    color === "success"
      ? "bg-emerald-50 text-emerald-700"
      : color === "danger"
      ? "bg-red-50 text-red-700"
      : color === "warning"
      ? "bg-amber-50 text-amber-700"
      : "bg-slate-100 text-slate-700";

  return <span className={`${base} ${variant}`}>{children}</span>;
}
