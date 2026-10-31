import React, { useState, useEffect } from 'react';
import { RefreshCw, Play, Calendar, CheckCircle2, AlertCircle, Clock } from 'lucide-react';
import { billingApi } from '../../api/client';
import { useToast } from '../../context/ToastContext';

export const BillingRecords = () => {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);
  const [runDate, setRunDate] = useState(new Date().toISOString().split('T')[0]);
  const { showToast } = useToast();

  const fetchRecords = async () => {
    setLoading(true);
    try {
      const res = await billingApi.records({ size: 50 });
      if (res.data) setRecords(res.data.content || []);
    } catch (err) {
      showToast(err.message || 'Failed to fetch billing logs', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecords();
  }, []);

  const handleRunBilling = async () => {
    setRunning(true);
    try {
      const res = await billingApi.run(runDate);
      const data = res.data;
      showToast(
        `Cycle completed! Processed: ${data.subscriptionsProcessed}, Generated: ${data.invoicesGenerated}, Expired: ${data.expiredSubscriptions}`,
        'success'
      );
      fetchRecords();
    } catch (err) {
      showToast(err.message || 'Billing cycle trigger failed', 'error');
    } finally {
      setRunning(false);
    }
  };

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹ 0.00';
    return `₹ ${Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-black tracking-tight">Recurring Billing Engine Runs</h1>
          <p className="text-sm text-slate-600 mt-1">
            Audit history of automated cron billing cycles and manual invoice generation runs.
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <input
            type="date"
            value={runDate}
            onChange={(e) => setRunDate(e.target.value)}
            className="p-1.5 bg-slate-50 border border-slate-300 rounded text-xs text-black"
          />
          <button
            onClick={handleRunBilling}
            disabled={running}
            className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${running ? 'animate-spin' : ''}`} />
            <span>{running ? 'Processing...' : 'Run Cycle Now'}</span>
          </button>
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs uppercase font-bold text-slate-500 tracking-wider">
              <tr>
                <th className="py-3 px-4">Billing Date</th>
                <th className="py-3 px-4">Subscription</th>
                <th className="py-3 px-4">Generated Invoice</th>
                <th className="py-3 px-4">Billing Period</th>
                <th className="py-3 px-4 text-right">Amount Billed</th>
                <th className="py-3 px-4 text-center">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-slate-500">
                    Loading billing history...
                  </td>
                </tr>
              ) : records.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-slate-500">
                    No billing cycles logged yet.
                  </td>
                </tr>
              ) : (
                records.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-50">
                    <td className="py-3.5 px-4 font-semibold text-black">{r.billingDate}</td>
                    <td className="py-3.5 px-4 font-bold text-[#714B67]">{r.subscriptionNumber}</td>
                    <td className="py-3.5 px-4 font-medium text-black">{r.invoiceNumber || '-'}</td>
                    <td className="py-3.5 px-4 text-xs text-slate-600">
                      {r.periodStart} to {r.periodEnd}
                    </td>
                    <td className="py-3.5 px-4 text-right font-bold text-black">{formatCurrency(r.amount)}</td>
                    <td className="py-3.5 px-4 text-center">
                      <span
                        className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-bold border ${
                          r.status === 'SUCCESS'
                            ? 'bg-emerald-50 text-emerald-950 border-emerald-300'
                            : r.status === 'SKIPPED'
                            ? 'bg-amber-50 text-amber-950 border-amber-300'
                            : 'bg-rose-50 text-rose-950 border-rose-300'
                        }`}
                      >
                        {r.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default BillingRecords;
