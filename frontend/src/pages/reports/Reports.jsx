import React, { useState, useEffect } from 'react';
import { Download, FileSpreadsheet, BarChart3, TrendingUp, DollarSign, Repeat, RefreshCw } from 'lucide-react';
import { reportApi } from '../../api/client';
import StatCard from '../../components/common/StatCard';
import { useToast } from '../../context/ToastContext';

export const Reports = () => {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState('');
  const { showToast } = useToast();

  const fetchMetrics = async () => {
    try {
      const res = await reportApi.getDashboardMetrics();
      if (res.data) setMetrics(res.data);
    } catch (err) {
      showToast('Failed to load metrics: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMetrics();
  }, []);

  const handleExport = async (type) => {
    setExporting(type);
    try {
      let res;
      let filename;
      if (type === 'subscriptions') {
        res = await reportApi.exportSubscriptions();
        filename = 'subscriptions_export.csv';
      } else if (type === 'invoices') {
        res = await reportApi.exportInvoices();
        filename = 'invoices_export.csv';
      } else if (type === 'payments') {
        res = await reportApi.exportPayments();
        filename = 'payments_export.csv';
      }

      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      showToast(`Exported ${type} CSV successfully!`, 'success');
    } catch (err) {
      showToast(`Failed to export ${type}: ` + err.message, 'error');
    } finally {
      setExporting('');
    }
  };

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹ 0.00';
    return `₹ ${Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12">
        <RefreshCw className="w-8 h-8 text-[#714B67] animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <h1 className="text-2xl font-bold text-black tracking-tight">Executive Reports & Data Export</h1>
        <p className="text-sm text-slate-600 mt-1">
          Download real-time CSV exports for finance, auditing, tax accounting, and revenue forecasting.
        </p>
      </div>

      {/* CSV Export Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center text-[#714B67] mb-3">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-black">Subscriptions Master Export</h3>
            <p className="text-xs text-slate-500 mt-1">
              Complete subscriber database with intervals, renewal dates, MRR contribution, and contract statuses.
            </p>
          </div>
          <button
            onClick={() => handleExport('subscriptions')}
            disabled={exporting === 'subscriptions'}
            className="mt-5 w-full flex items-center justify-center space-x-2 py-2.5 px-3 rounded-lg bg-[#714B67] hover:bg-[#5d3d54] text-white text-xs font-bold transition-all shadow-xs disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            <span>{exporting === 'subscriptions' ? 'Generating...' : 'Export Subscriptions CSV'}</span>
          </button>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-700 mb-3">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-black">Tax Invoices Master Export</h3>
            <p className="text-xs text-slate-500 mt-1">
              All tax invoices, billing periods, GST amounts, paid totals, and outstanding aging balances.
            </p>
          </div>
          <button
            onClick={() => handleExport('invoices')}
            disabled={exporting === 'invoices'}
            className="mt-5 w-full flex items-center justify-center space-x-2 py-2.5 px-3 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold transition-all shadow-xs disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            <span>{exporting === 'invoices' ? 'Generating...' : 'Export Invoices CSV'}</span>
          </button>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center text-emerald-700 mb-3">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-black">Payments & Receipts Export</h3>
            <p className="text-xs text-slate-500 mt-1">
              Payment logs, bank transfer references, payment gateway allocations, and refund reconciliation.
            </p>
          </div>
          <button
            onClick={() => handleExport('payments')}
            disabled={exporting === 'payments'}
            className="mt-5 w-full flex items-center justify-center space-x-2 py-2.5 px-3 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold transition-all shadow-xs disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            <span>{exporting === 'payments' ? 'Generating...' : 'Export Payments CSV'}</span>
          </button>
        </div>
      </div>

      {/* Financial Health Snapshot */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <h3 className="text-base font-bold text-black mb-4">Financial Health Snapshot</h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
            <span className="text-[11px] font-bold text-slate-500 uppercase">Current MRR</span>
            <p className="text-xl font-bold text-black mt-1">{formatCurrency(metrics?.mrr)}</p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
            <span className="text-[11px] font-bold text-slate-500 uppercase">Annualized ARR</span>
            <p className="text-xl font-bold text-black mt-1">{formatCurrency(metrics?.arr)}</p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
            <span className="text-[11px] font-bold text-slate-500 uppercase">Total Revenue Collected</span>
            <p className="text-xl font-bold text-emerald-700 mt-1">{formatCurrency(metrics?.totalRevenue)}</p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
            <span className="text-[11px] font-bold text-slate-500 uppercase">Outstanding Receivables</span>
            <p className="text-xl font-bold text-rose-700 mt-1">{formatCurrency(metrics?.outstandingBalance)}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Reports;
