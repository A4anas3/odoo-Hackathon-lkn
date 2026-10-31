import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  PauseCircle,
  PlayCircle,
  XCircle,
  RotateCcw,
  Sliders,
  Calendar,
  CreditCard,
  User,
  Clock,
  RefreshCw,
  CheckCircle,
} from 'lucide-react';
import { subscriptionApi, planApi } from '../../api/client';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export const SubscriptionDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [subscription, setSubscription] = useState(null);
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // Modals
  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [targetStatus, setTargetStatus] = useState('');
  const [statusReason, setStatusReason] = useState('');

  const [planModalOpen, setPlanModalOpen] = useState(false);
  const [selectedPlanId, setSelectedPlanId] = useState('');
  const [planChangeReason, setPlanChangeReason] = useState('');

  const [renewModalOpen, setRenewModalOpen] = useState(false);
  const [renewIntervals, setRenewIntervals] = useState(1);

  const fetchSubscription = async () => {
    try {
      const res = await subscriptionApi.get(id);
      if (res.data) {
        setSubscription(res.data);
      }
    } catch (err) {
      showToast(err.message || 'Failed to load subscription details', 'error');
    } finally {
      setLoading(false);
    }
  };

  const fetchPlans = async () => {
    try {
      const res = await planApi.list({ isActive: true, size: 50 });
      if (res.data) {
        setPlans(res.data.content || []);
      }
    } catch (e) {
      // Ignore
    }
  };

  useEffect(() => {
    fetchSubscription();
    fetchPlans();
  }, [id]);

  const handleStatusChange = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    try {
      await subscriptionApi.changeStatus(id, targetStatus, statusReason);
      showToast(`Subscription status updated to ${targetStatus}`, 'success');
      setStatusModalOpen(false);
      setStatusReason('');
      fetchSubscription();
    } catch (err) {
      showToast(err.message || 'Failed to update status', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const handlePlanChange = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    try {
      await subscriptionApi.changePlan(id, selectedPlanId, planChangeReason);
      showToast('Subscription plan successfully changed', 'success');
      setPlanModalOpen(false);
      setPlanChangeReason('');
      fetchSubscription();
    } catch (err) {
      showToast(err.message || 'Failed to change plan', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRenew = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    try {
      await subscriptionApi.renew(id, renewIntervals);
      showToast(`Subscription renewed for ${renewIntervals} interval(s)`, 'success');
      setRenewModalOpen(false);
      fetchSubscription();
    } catch (err) {
      showToast(err.message || 'Failed to renew subscription', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const openStatusConfirm = (st) => {
    setTargetStatus(st);
    setStatusReason('');
    setStatusModalOpen(true);
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

  if (!subscription) {
    return (
      <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center">
        <p className="text-black font-bold">Subscription not found</p>
        <button
          onClick={() => navigate('/subscriptions')}
          className="mt-4 px-4 py-2 bg-[#714B67] text-white rounded-lg text-sm"
        >
          Back to List
        </button>
      </div>
    );
  }

  const s = subscription;

  return (
    <div className="space-y-6">
      {/* Top Breadcrumb & Actions Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => navigate('/subscriptions')}
            className="p-2 rounded-lg border border-slate-300 text-slate-600 hover:text-black hover:bg-slate-100 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-xl font-bold text-black tracking-tight">{s.subscriptionNumber}</h1>
              <StatusBadge status={s.status} />
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Customer: <span className="font-semibold text-black">{s.customerName}</span> ({s.customerEmail})
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center gap-2">
          {s.status === 'ACTIVE' && (
            <button
              onClick={() => openStatusConfirm('PAUSED')}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-amber-500 hover:bg-amber-600 text-black shadow-xs transition-colors"
            >
              <PauseCircle className="w-4 h-4" />
              <span>Pause Subscription</span>
            </button>
          )}

          {s.status === 'PAUSED' && (
            <button
              onClick={() => openStatusConfirm('ACTIVE')}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors"
            >
              <PlayCircle className="w-4 h-4" />
              <span>Resume Active</span>
            </button>
          )}

          {['ACTIVE', 'PAUSED'].includes(s.status) && (
            <button
              onClick={() => {
                setSelectedPlanId(s.planId || '');
                setPlanModalOpen(true);
              }}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-indigo-600 hover:bg-indigo-700 text-white shadow-xs transition-colors"
            >
              <Sliders className="w-4 h-4" />
              <span>Upgrade / Downgrade Plan</span>
            </button>
          )}

          {['CLOSED', 'EXPIRED'].includes(s.status) && (
            <button
              onClick={() => setRenewModalOpen(true)}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Renew Subscription</span>
            </button>
          )}

          {s.status !== 'CLOSED' && (
            <button
              onClick={() => openStatusConfirm('CLOSED')}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-rose-600 hover:bg-rose-700 text-white shadow-xs transition-colors"
            >
              <XCircle className="w-4 h-4" />
              <span>Close Contract</span>
            </button>
          )}
        </div>
      </div>

      {/* Contract Overview Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Plan Details Card */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center space-x-2 text-xs font-bold text-[#714B67] uppercase tracking-wider mb-3">
            <Calendar className="w-4 h-4" />
            <span>Plan & Schedule</span>
          </div>
          <h3 className="text-lg font-bold text-black">{s.planName}</h3>
          <p className="text-xs text-slate-500 font-mono mt-0.5">{s.planCode}</p>

          <div className="mt-4 space-y-2 text-xs divide-y divide-slate-100">
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Billing Interval:</span>
              <span className="font-bold text-black uppercase">
                {s.billingIntervalCount > 1 ? `${s.billingIntervalCount} ` : ''}
                {s.billingInterval}
              </span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Contract Start:</span>
              <span className="font-semibold text-black">{s.startDate || '-'}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Contract End:</span>
              <span className="font-semibold text-black">{s.endDate || 'Ongoing / Indefinite'}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Next Billing Date:</span>
              <span className="font-bold text-[#714B67]">{s.nextBillingDate || '-'}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Last Billed:</span>
              <span className="font-semibold text-black">{s.lastBillingDate || 'Not billed yet'}</span>
            </div>
          </div>
        </div>

        {/* Pricing & Billing Breakdown */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center space-x-2 text-xs font-bold text-emerald-700 uppercase tracking-wider mb-3">
            <CreditCard className="w-4 h-4" />
            <span>Recurring Financials</span>
          </div>
          <h3 className="text-2xl font-bold text-black">{formatCurrency(s.totalAmount)}</h3>
          <p className="text-xs text-slate-500 mt-0.5">Recurring billed amount per cycle</p>

          <div className="mt-4 space-y-2 text-xs divide-y divide-slate-100">
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Base Unit Price:</span>
              <span className="font-semibold text-black">{formatCurrency(s.unitPrice)}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Contract Quantity:</span>
              <span className="font-semibold text-black">{s.quantity}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Subtotal:</span>
              <span className="font-semibold text-black">{formatCurrency(s.subtotal)}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Discounts Applied:</span>
              <span className="font-semibold text-rose-600">- {formatCurrency(s.discountAmount)}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Tax Total:</span>
              <span className="font-semibold text-black">{formatCurrency(s.taxAmount)}</span>
            </div>
          </div>
        </div>

        {/* Customer Information */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center space-x-2 text-xs font-bold text-indigo-700 uppercase tracking-wider mb-3">
            <User className="w-4 h-4" />
            <span>Customer Details</span>
          </div>
          <h3 className="text-lg font-bold text-black">{s.customerName}</h3>
          <p className="text-xs text-slate-500 mt-0.5">{s.customerEmail}</p>

          <div className="mt-4 space-y-2 text-xs divide-y divide-slate-100">
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Payment Terms:</span>
              <span className="font-semibold text-black">{s.paymentTerms || 'Immediate'}</span>
            </div>
            {s.quotationId && (
              <div className="flex justify-between pt-2">
                <span className="text-slate-500">Origin Quotation:</span>
                <Link
                  to={`/quotations/${s.quotationId}`}
                  className="font-bold text-[#714B67] hover:underline"
                >
                  View Quotation
                </Link>
              </div>
            )}
            {s.autoCloseReason && (
              <div className="pt-2">
                <span className="text-slate-500 block">Close Reason:</span>
                <span className="text-rose-700 font-medium">{s.autoCloseReason}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Contract Line Items */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <h3 className="text-base font-bold text-black mb-4">Included Products & Service Add-ons</h3>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs uppercase font-bold text-slate-500">
              <tr>
                <th className="py-2.5 px-3">Description</th>
                <th className="py-2.5 px-3 text-center">Qty</th>
                <th className="py-2.5 px-3 text-right">Unit Price</th>
                <th className="py-2.5 px-3 text-right">Tax Rate</th>
                <th className="py-2.5 px-3 text-right">Line Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {s.lines && s.lines.length > 0 ? (
                s.lines.map((l) => (
                  <tr key={l.id} className="hover:bg-slate-50">
                    <td className="py-3 px-3 font-medium text-black">
                      {l.productName || l.description || 'Subscription Line'}
                    </td>
                    <td className="py-3 px-3 text-center">{l.quantity}</td>
                    <td className="py-3 px-3 text-right font-medium text-black">
                      {formatCurrency(l.unitPrice)}
                    </td>
                    <td className="py-3 px-3 text-right text-slate-600">
                      {(Number(l.taxRate || 0) * 100).toFixed(1)}%
                    </td>
                    <td className="py-3 px-3 text-right font-bold text-black">
                      {formatCurrency(l.lineTotal)}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5" className="py-4 text-center text-xs text-slate-500">
                    Standard plan line item ({s.planName} at {formatCurrency(s.unitPrice)})
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Lifecycle Audit History */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <div className="flex items-center space-x-2 text-xs font-bold text-black uppercase tracking-wider mb-4">
          <Clock className="w-4 h-4 text-slate-500" />
          <span>Lifecycle Transition History</span>
        </div>

        <div className="space-y-4">
          {s.history && s.history.length > 0 ? (
            s.history.map((h) => (
              <div key={h.id} className="flex items-start space-x-3 text-xs border-l-2 border-[#714B67] pl-3 py-1">
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    {h.fromStatus && (
                      <>
                        <StatusBadge status={h.fromStatus} />
                        <span className="text-slate-400">→</span>
                      </>
                    )}
                    <StatusBadge status={h.toStatus} />
                    <span className="text-slate-400">•</span>
                    <span className="text-slate-500 font-medium">
                      {new Date(h.createdAt).toLocaleString()}
                    </span>
                  </div>
                  {h.reason && <p className="text-slate-700 mt-1 font-medium">{h.reason}</p>}
                </div>
              </div>
            ))
          ) : (
            <p className="text-xs text-slate-500">No status changes recorded yet.</p>
          )}
        </div>
      </div>

      {/* Status Change Modal */}
      <Modal
        isOpen={statusModalOpen}
        onClose={() => setStatusModalOpen(false)}
        title={`Confirm Status Transition: ${targetStatus}`}
        maxWidth="max-w-md"
      >
        <form onSubmit={handleStatusChange} className="space-y-4">
          <p className="text-sm text-slate-700">
            Are you sure you want to transition subscription{' '}
            <span className="font-bold text-black">{s.subscriptionNumber}</span> to{' '}
            <span className="font-bold text-[#714B67]">{targetStatus}</span>?
          </p>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">
              Reason / Operational Notes
            </label>
            <textarea
              required
              rows={3}
              value={statusReason}
              onChange={(e) => setStatusReason(e.target.value)}
              placeholder="Provide reason for this state change..."
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setStatusModalOpen(false)}
              className="px-3.5 py-2 text-xs font-semibold rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={actionLoading}
              className="px-4 py-2 text-xs font-semibold rounded-lg bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs disabled:opacity-50"
            >
              {actionLoading ? 'Updating...' : 'Confirm Transition'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Upgrade / Downgrade Plan Modal */}
      <Modal
        isOpen={planModalOpen}
        onClose={() => setPlanModalOpen(false)}
        title="Upgrade / Downgrade Subscription Plan"
        maxWidth="max-w-md"
      >
        <form onSubmit={handlePlanChange} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">
              Select New Plan
            </label>
            <select
              required
              value={selectedPlanId}
              onChange={(e) => setSelectedPlanId(e.target.value)}
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            >
              {plans.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} — {formatCurrency(p.basePrice)} / {p.billingInterval}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">
              Reason for Plan Change
            </label>
            <textarea
              rows={2}
              value={planChangeReason}
              onChange={(e) => setPlanChangeReason(e.target.value)}
              placeholder="e.g. Customer requested upgrade to Enterprise tier"
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setPlanModalOpen(false)}
              className="px-3.5 py-2 text-xs font-semibold rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={actionLoading}
              className="px-4 py-2 text-xs font-semibold rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white shadow-xs disabled:opacity-50"
            >
              {actionLoading ? 'Updating...' : 'Apply Plan Change'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Renew Modal */}
      <Modal
        isOpen={renewModalOpen}
        onClose={() => setRenewModalOpen(false)}
        title="Renew Subscription Contract"
        maxWidth="max-w-md"
      >
        <form onSubmit={handleRenew} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">
              Number of Additional Billing Intervals
            </label>
            <input
              type="number"
              min="1"
              max="24"
              required
              value={renewIntervals}
              onChange={(e) => setRenewIntervals(parseInt(e.target.value, 10))}
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setRenewModalOpen(false)}
              className="px-3.5 py-2 text-xs font-semibold rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={actionLoading}
              className="px-4 py-2 text-xs font-semibold rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs disabled:opacity-50"
            >
              {actionLoading ? 'Renewing...' : 'Confirm Renewal'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default SubscriptionDetail;
