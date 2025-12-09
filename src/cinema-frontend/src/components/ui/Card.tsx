export function Card({ children }: { children: React.ReactNode }) {
  return (
    <div className="bg-slate-900/90 rounded-xl border border-slate-800 shadow-lg shadow-slate-950/60 p-4">
      {children}
    </div>
  );
}

export function CardHeader({
  title,
  subtitle,
}: {
  title: string;
  subtitle?: string;
}) {
  return (
    <div className="mb-3">
      <h2 className="text-lg font-semibold text-slate-50">{title}</h2>
      {subtitle && (
        <p className="text-xs text-slate-400 mt-1">{subtitle}</p>
      )}
    </div>
  );
}

export function CardSection({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className="mt-4">
      <div className="text-[11px] font-semibold text-slate-400 mb-1 uppercase tracking-wide">
        {title}
      </div>
      {children}
    </div>
  );
}
