interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  helperText?: string;
}

export function Input({
  label,
  helperText,
  className = "",
  ...props
}: InputProps) {
  return (
    <div className="space-y-1">
      {label && (
        <label className="block text-xs font-medium text-slate-200">
          {label}
        </label>
      )}
      <input
        className={`w-full rounded-md border border-slate-700 bg-slate-950/80 px-3 py-2 text-sm text-slate-50 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-sky-500 ${className}`}
        {...props}
      />
      {helperText && (
        <p className="text-[11px] text-slate-400">{helperText}</p>
      )}
    </div>
  );
}
