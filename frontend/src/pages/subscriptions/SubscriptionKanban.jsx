import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { List, Plus, RefreshCw, Repeat, Calendar, ArrowRight } from 'lucide-react';
import { subscriptionApi } from '../../api/client';
import StatusBadge from '../../components/common/StatusBadge';
import { useToast } from '../../context/ToastContext';

const KANBAN_COLUMNS = [
  { key: 'DRAFT', label: 'Draft', color: 'border-slate-300' },
  { key: 'QUOTATION', label: 'Quotation', color: 'border-sky-300' },
  { key: 'CONFIRMED', label: 'Confirmed', color: 'border-purple-300' },
  { key: 'ACTIVE', label: 'Active', color: 'border-emerald-300' },
  { key: 'PAUSED', label: 'Paused', color: 'border-amber-300' },
  { key: 'CLOSED', label: 'Closed', color: 'border-slate-300' },
  { key: 'EXPIRED', label: 'Expired', color: 'border-rose-300' },
];

export const SubscriptionKanban = () => {
  const [subscriptions, setSubscriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showToast } = useToast();

  const fetchSubscriptions = async () => {
    setLoading(true);
    try {
      const res = await subscriptionApi.list({ size: 100 });
      if (res.data) {
        setSubscriptions(res.data.content || []);
      }
    } catch (err) {
      showToast(err.message || 'Failed to fetch subscriptions', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubscriptions();
  }, []);

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹ 0.00';
    return `₹ ${Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-black tracking-tight">Subscription Pipeline</h1>
          <p className="text-sm text-slate-600 mt-1">
            Visual state board tracking contracts across lifecycle phases.
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Link
            to="/subscriptions"
            className="flex items-center space-x-1.5 px-3 py-2 rounded-lg text-xs font-semibold bg-slate-100 hover:bg-slate-200 text-slate-800 transition-colors"
          >
            <List className="w-4 h-4" />
            <span>List Table</span>
          </Link>
          <Link
            to="/subscriptions/new"
            className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>New Subscription</span>
          </Link>
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center p-12 bg-white rounded-2xl border border-slate-200">
          <RefreshCw className="w-6 h-6 text-[#714B67] animate-spin" />
        </div>
      ) : (
        /* Kanban Columns Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-7 gap-4 items-start overflow-x-auto pb-4">
          {KANBAN_COLUMNS.map((col) => {
            const items = subscriptions.filter((s) => s.status === col.key);
            return (
              <div
                key={col.key}
                className="bg-slate-100/70 rounded-xl border border-slate-200 p-3 min-w-[240px] flex flex-col space-y-3"
              >
                {/* Column header */}
                <div className="flex items-center justify-between px-1">
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-bold uppercase tracking-wider text-black">
                      {col.label}
                    </span>
                    <span className="text-xs font-bold px-1.5 py-0.5 rounded-full bg-white text-slate-700 border border-slate-200">
                      {items.length}
                    </span>
                  </div>
                </div>

                {/* Cards */}
                <div className="space-y-2.5">
                  {items.map((sub) => (
                    <Link
                      key={sub.id}
                      to={`/subscriptions/${sub.id}`}
                      className="block bg-white rounded-lg border border-slate-200 p-3.5 shadow-2xs hover:shadow-md hover:border-[#714B67] transition-all group"
                    >
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="text-xs font-bold text-[#714B67] group-hover:underline">
                          {sub.subscriptionNumber}
                        </span>
                        <StatusBadge status={sub.status} />
                      </div>
                      <p className="text-sm font-bold text-black truncate">{sub.customerName}</p>
                      <p className="text-xs text-slate-600 truncate mt-0.5">{sub.planName}</p>

                      <div className="mt-3 pt-2.5 border-t border-slate-100 flex items-center justify-between text-xs">
                        <span className="font-bold text-black">{formatCurrency(sub.totalAmount)}</span>
                        <span className="text-slate-500 text-[11px]">
                          {sub.nextBillingDate ? `Due: ${sub.nextBillingDate}` : 'No date'}
                        </span>
                      </div>
                    </Link>
                  ))}
                  {items.length === 0 && (
                    <div className="text-center py-6 text-xs text-slate-400 border border-dashed border-slate-200 rounded-lg">
                      No contracts in {col.label.toLowerCase()}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default SubscriptionKanban;
