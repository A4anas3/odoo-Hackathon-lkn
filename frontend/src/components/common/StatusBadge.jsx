import React from 'react';

const statusStyles = {
  ACTIVE: 'bg-emerald-50 text-emerald-950 border-emerald-300',
  PAUSED: 'bg-amber-50 text-amber-950 border-amber-300',
  CLOSED: 'bg-slate-100 text-slate-900 border-slate-300',
  EXPIRED: 'bg-rose-50 text-rose-950 border-rose-300',
  DRAFT: 'bg-slate-100 text-slate-800 border-slate-300',
  QUOTATION: 'bg-sky-50 text-sky-950 border-sky-300',
  CONFIRMED: 'bg-purple-50 text-purple-950 border-purple-300',
  POSTED: 'bg-indigo-50 text-indigo-950 border-indigo-300',
  PAID: 'bg-emerald-50 text-emerald-950 border-emerald-300',
  PARTIALLY_PAID: 'bg-amber-50 text-amber-950 border-amber-300',
  CANCELLED: 'bg-rose-50 text-rose-900 border-rose-300',
  COMPLETED: 'bg-emerald-50 text-emerald-950 border-emerald-300',
  PENDING: 'bg-amber-50 text-amber-950 border-amber-300',
  REFUNDED: 'bg-purple-50 text-purple-950 border-purple-300',
};

export const StatusBadge = ({ status, className = '' }) => {
  if (!status) return null;
  const upper = status.toUpperCase();
  const style = statusStyles[upper] || 'bg-slate-100 text-slate-900 border-slate-300';

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border shadow-xs tracking-wide uppercase ${style} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full bg-current mr-1.5 opacity-80" />
      {status.replace(/_/g, ' ')}
    </span>
  );
};

export default StatusBadge;
