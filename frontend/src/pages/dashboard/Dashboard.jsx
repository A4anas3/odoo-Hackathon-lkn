import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  TrendingUp,
  Repeat,
  DollarSign,
  AlertCircle,
  Users,
  Receipt,
  PlusCircle,
  RefreshCw,
  FileText,
  ArrowRight,
  CreditCard,
  CheckCircle2,
  Clock,
  ExternalLink,
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts';
import { reportApi, billingApi } from '../../api/client';
import StatCard from '../../components/common/StatCard';
import StatusBadge from '../../components/common/StatusBadge';
import PermissionGuard from '../../components/common/PermissionGuard';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';

export const Dashboard = () => {
  const { user, role, isCustomer, isAdmin } = useAuth();
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [runningBilling, setRunningBilling] = useState(false);
  const { showToast } = useToast();

  const fetchMetrics = async () => {
    try {
      const res = await reportApi.getDashboardMetrics();
      if (res.data) {
        setMetrics(res.data);
      }
    } catch (err) {
      // For customers, reports API may not be relevant
      if (!isCustomer) {
        showToast('Failed to load dashboard metrics: ' + err.message, 'error');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMetrics();
  }, []);

  const handleRunBilling = async () => {
    setRunningBilling(true);
    try {
      const res = await billingApi.run();
      const count = res.data?.invoicesGenerated || 0;
      showToast(`Billing cycle finished! ${count} recurring invoice(s) generated.`, 'success');
      fetchMetrics();
    } catch (err) {
      showToast(err.message || 'Billing run failed', 'error');
    } finally {
      setRunningBilling(false);
    }
  };

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹ 0.00';
    return `₹ ${Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center space-y-3">
          <RefreshCw className="w-8 h-8 text-[#714B67] animate-spin" />
          <p className="text-sm font-semibold text-slate-600">Loading Workspace...</p>
        </div>
      </div>
    );
  }

  // =========================================================================
  // CUSTOMER PORTAL VIEW
  // =========================================================================
  if (isCustomer) {
    return (
      <div className="space-y-6">
        {/* Welcome Header for Customer */}
        <div className="bg-gradient-to-r from-purple-50 via-white to-slate-50 p-6 rounded-2xl border border-purple-100 shadow-xs">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <span className="text-xs uppercase tracking-wider font-bold text-[#714B67]">
                Customer Self-Service Portal
              </span>
              <h1 className="text-2xl font-bold text-black tracking-tight mt-1">
                Welcome back, {user?.fullName || 'Valued Customer'}!
              </h1>
              <p className="text-sm text-slate-600 mt-1">
                Manage your active subscriptions, view billing history, and download GST-compliant tax invoices.
              </p>
            </div>
            <div className="flex items-center space-x-2">
              <Link
                to="/subscriptions"
                className="px-4 py-2 rounded-lg bg-[#714B67] hover:bg-[#5d3d54] text-white text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition-all"
              >
                <Repeat className="w-4 h-4" />
                <span>My Subscriptions</span>
              </Link>
            </div>
          </div>
        </div>

        {/* Customer Quick Metric Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Active Plans</span>
              <div className="w-8 h-8 rounded-lg bg-purple-50 text-[#714B67] flex items-center justify-center">
                <Repeat className="w-4 h-4" />
              </div>
            </div>
            <p className="text-2xl font-bold text-black mt-2">{metrics?.activeSubscriptionsCount ?? '—'}</p>
            <p className="text-xs text-slate-500 mt-1">Recurring contracts in good standing</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Unpaid Balance</span>
              <div className="w-8 h-8 rounded-lg bg-amber-50 text-amber-700 flex items-center justify-center">
                <AlertCircle className="w-4 h-4" />
              </div>
            </div>
            <p className="text-2xl font-bold text-black mt-2">{formatCurrency(metrics?.outstandingBalance)}</p>
            <p className="text-xs text-slate-500 mt-1">Outstanding across open invoices</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Account Status</span>
              <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-700 flex items-center justify-center">
                <CheckCircle2 className="w-4 h-4" />
              </div>
            </div>
            <p className="text-2xl font-bold text-emerald-700 mt-2">Active</p>
            <p className="text-xs text-slate-500 mt-1">Verified enterprise customer</p>
          </div>
        </div>

        {/* Customer Portal Quick Navigation Tiles */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Link
            to="/subscriptions"
            className="group bg-white p-6 rounded-2xl border border-slate-200 hover:border-[#714B67] shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-purple-100 text-[#714B67] flex items-center justify-center mb-4">
                <Repeat className="w-5 h-5" />
              </div>
              <h3 className="text-base font-bold text-black group-hover:text-[#714B67] transition-colors">
                My Subscriptions
              </h3>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Review your current recurring packages, billing frequency, renewal dates, and terms.
              </p>
            </div>
            <div className="flex items-center text-xs font-bold text-[#714B67] mt-6 group-hover:translate-x-1 transition-transform">
              <span>View All Subscriptions</span>
              <ArrowRight className="w-4 h-4 ml-1" />
            </div>
          </Link>

          <Link
            to="/invoices"
            className="group bg-white p-6 rounded-2xl border border-slate-200 hover:border-[#714B67] shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center mb-4">
                <Receipt className="w-5 h-5" />
              </div>
              <h3 className="text-base font-bold text-black group-hover:text-[#714B67] transition-colors">
                Invoices & Tax Receipts
              </h3>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Inspect past invoice line items, tax breakdowns (GST), and download official PDF tax invoices.
              </p>
            </div>
            <div className="flex items-center text-xs font-bold text-emerald-700 mt-6 group-hover:translate-x-1 transition-transform">
              <span>Access Invoices</span>
              <ArrowRight className="w-4 h-4 ml-1" />
            </div>
          </Link>

          <Link
            to="/payments"
            className="group bg-white p-6 rounded-2xl border border-slate-200 hover:border-[#714B67] shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center mb-4">
                <CreditCard className="w-5 h-5" />
              </div>
              <h3 className="text-base font-bold text-black group-hover:text-[#714B67] transition-colors">
                Payment History
              </h3>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Check settlement records, FIFO invoice clearing statements, and transaction references.
              </p>
            </div>
            <div className="flex items-center text-xs font-bold text-blue-700 mt-6 group-hover:translate-x-1 transition-transform">
              <span>View Payment Receipts</span>
              <ArrowRight className="w-4 h-4 ml-1" />
            </div>
          </Link>
        </div>
      </div>
    );
  }

  // =========================================================================
  // ENTERPRISE ADMIN & STAFF VIEW
  // =========================================================================
  return (
    <div className="space-y-6">
      {/* Top Banner & Quick Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-black tracking-tight">Recurring Billing Overview</h1>
          <p className="text-sm text-slate-600 mt-1">
            Real-time subscription metrics, recurring cycles, and revenue collection health.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {/* PermissionGuard: Only Admins can trigger billing cycle runs */}
          <PermissionGuard roles={['ADMIN']}>
            <button
              onClick={handleRunBilling}
              disabled={runningBilling}
              className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-all disabled:opacity-50"
            >
              <RefreshCw className={`w-4 h-4 ${runningBilling ? 'animate-spin' : ''}`} />
              <span>Run Billing Cycle</span>
            </button>
          </PermissionGuard>

          {/* Creation links for Admin & Staff */}
          <PermissionGuard roles={['ADMIN', 'STAFF']}>
            <Link
              to="/subscriptions/new"
              className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-all"
            >
              <PlusCircle className="w-4 h-4" />
              <span>New Subscription</span>
            </Link>
            <Link
              to="/quotations/new"
              className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-slate-900 hover:bg-black text-white shadow-xs transition-all"
            >
              <FileText className="w-4 h-4" />
              <span>New Quotation</span>
            </Link>
          </PermissionGuard>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Monthly Recurring Revenue (MRR)"
          value={formatCurrency(metrics?.mrr)}
          subtitle="Normalized monthly recurring base"
          icon={TrendingUp}
          color="odoo"
          trend={{ value: '8.4%', positive: true, label: 'MoM growth' }}
        />
        <StatCard
          title="Annual Recurring Revenue (ARR)"
          value={formatCurrency(metrics?.arr)}
          subtitle="Annualized recurring projection"
          icon={DollarSign}
          color="emerald"
        />
        <StatCard
          title="Active Subscriptions"
          value={metrics?.activeSubscriptionsCount || 0}
          subtitle={`${metrics?.pausedSubscriptionsCount || 0} paused, ${metrics?.closedSubscriptionsCount || 0} closed`}
          icon={Repeat}
          color="indigo"
        />
        <StatCard
          title="Outstanding Receivables"
          value={formatCurrency(metrics?.outstandingBalance)}
          subtitle={`${metrics?.overdueInvoicesCount || 0} overdue invoices`}
          icon={AlertCircle}
          color="amber"
        />
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Revenue Trend Area Chart */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-base font-bold text-black">Revenue & Recurring Billed Trend</h3>
              <p className="text-xs text-slate-500">6-Month historical performance trajectory</p>
            </div>
            <div className="flex items-center space-x-4 text-xs font-medium">
              <div className="flex items-center space-x-1.5">
                <span className="w-3 h-3 rounded-full bg-[#714B67]" />
                <span className="text-slate-700">Total Revenue</span>
              </div>
              <div className="flex items-center space-x-1.5">
                <span className="w-3 h-3 rounded-full bg-emerald-500" />
                <span className="text-slate-700">Recurring Billed</span>
              </div>
            </div>
          </div>

          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={metrics?.revenueTrends || []} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorRev" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#714B67" stopOpacity={0.25} />
                    <stop offset="95%" stopColor="#714B67" stopOpacity={0.0} />
                  </linearGradient>
                  <linearGradient id="colorRec" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.25} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <XAxis dataKey="month" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis
                  stroke="#94a3b8"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(val) => `₹${val / 1000}k`}
                />
                <Tooltip
                  formatter={(value) => [formatCurrency(value), '']}
                  contentStyle={{
                    backgroundColor: '#ffffff',
                    border: '1px solid #e2e8f0',
                    borderRadius: '0.75rem',
                    boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)',
                    color: '#000',
                  }}
                />
                <Area type="monotone" dataKey="revenue" stroke="#714B67" strokeWidth={2.5} fillOpacity={1} fill="url(#colorRev)" />
                <Area type="monotone" dataKey="recurringBilled" stroke="#10b981" strokeWidth={2} fillOpacity={1} fill="url(#colorRec)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Subscription Status Lifecycle Breakdown */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs flex flex-col justify-between">
          <div>
            <h3 className="text-base font-bold text-black mb-1">Subscription State Funnel</h3>
            <p className="text-xs text-slate-500 mb-4">Current subscription lifecycle distribution</p>

            <div className="space-y-3">
              {metrics?.subscriptionsByStatus &&
                Object.entries(metrics.subscriptionsByStatus).map(([status, count]) => (
                  <div key={status} className="flex items-center justify-between p-2.5 rounded-lg bg-slate-50 border border-slate-200">
                    <StatusBadge status={status} />
                    <span className="text-sm font-bold text-black">{count}</span>
                  </div>
                ))}
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-slate-200">
            <Link
              to="/subscriptions/kanban"
              className="w-full flex items-center justify-center space-x-2 py-2 px-3 rounded-lg bg-purple-50 hover:bg-purple-100 text-[#714B67] text-xs font-bold transition-all border border-purple-200"
            >
              <span>Open Visual Kanban Board</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
      </div>

      {/* Plans & Top Customers Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Subscription Plan Distribution */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-base font-bold text-black">Plan Popularity & MRR Contribution</h3>
            <PermissionGuard roles={['ADMIN', 'STAFF']}>
              <Link to="/plans" className="text-xs font-bold text-[#714B67] hover:underline">
                View All Plans
              </Link>
            </PermissionGuard>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-xs uppercase font-bold text-slate-500">
                  <th className="pb-3">Plan Name</th>
                  <th className="pb-3 text-center">Active Subs</th>
                  <th className="pb-3 text-right">Monthly Value</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {metrics?.planDistribution?.map((p) => (
                  <tr key={p.planId} className="hover:bg-slate-50 transition-colors">
                    <td className="py-3 font-semibold text-black">{p.planName}</td>
                    <td className="py-3 text-center font-bold text-indigo-700">{p.count}</td>
                    <td className="py-3 text-right font-bold text-black">{formatCurrency(p.monthlyValue)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Top Enterprise Customers (Admin Only) */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-base font-bold text-black">Top Revenue Customers</h3>
            <PermissionGuard roles={['ADMIN', 'STAFF']}>
              <Link to="/customers" className="text-xs font-bold text-[#714B67] hover:underline">
                View All Customers
              </Link>
            </PermissionGuard>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-xs uppercase font-bold text-slate-500">
                  <th className="pb-3">Customer</th>
                  <th className="pb-3 text-center">Active Subs</th>
                  <th className="pb-3 text-right">Total Paid</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {metrics?.topCustomers?.map((c) => (
                  <tr key={c.customerId} className="hover:bg-slate-50 transition-colors">
                    <td className="py-3">
                      <p className="font-semibold text-black">{c.customerName}</p>
                      <p className="text-xs text-slate-500">{c.customerEmail}</p>
                    </td>
                    <td className="py-3 text-center font-bold text-emerald-700">{c.activeSubscriptions}</td>
                    <td className="py-3 text-right font-bold text-black">{formatCurrency(c.totalPaid)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
