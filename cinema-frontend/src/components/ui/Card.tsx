import React from "react";

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
  extra,
}: {
  title: string;
  subtitle?: string;
  extra?: React.ReactNode;
}) {
  return (
    <div className="mb-3 flex items-start justify-between gap-4">
      <div>
        <h2 className="text-lg font-semibold text-slate-50">{title}</h2>
        {subtitle && <p className="text-xs text-slate-400 mt-1">{subtitle}</p>}
      </div>

      {extra ? <div className="shrink-0">{extra}</div> : null}
    </div>
  );
}

export function CardSection({
  title,
  children,
}: {
  title?: string; // ✅ κάνουμε το title optional για να μη σπάει αν δεν το δώσεις
  children: React.ReactNode;
}) {
  return (
    <div className="mt-4">
      {title ? (
        <div className="text-[11px] font-semibold text-slate-400 mb-1 uppercase tracking-wide">
          {title}
        </div>
      ) : null}
      {children}
    </div>
  );
}
