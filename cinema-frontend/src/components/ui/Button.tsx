type Variant = "primary" | "secondary" | "ghost" | "danger";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
}

const base =
  "inline-flex items-center justify-center rounded-md px-3 py-2 text-sm font-medium transition focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-sky-500 focus:ring-offset-slate-950 disabled:opacity-60 disabled:cursor-not-allowed";

const variants: Record<Variant, string> = {
  primary: "bg-sky-500 text-slate-950 hover:bg-sky-400",
  secondary: "border border-slate-600 bg-slate-900 text-slate-100 hover:bg-slate-800",
  ghost: "text-slate-200 hover:bg-slate-800/70",
  danger: "bg-rose-600 text-white hover:bg-rose-500",
};

export function Button({
  variant = "primary",
  className = "",
  type = "button",          // ✅ CRITICAL
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}            // ✅ CRITICAL
      className={`${base} ${variants[variant]} ${className}`}
      {...props}
    />
  );
}
