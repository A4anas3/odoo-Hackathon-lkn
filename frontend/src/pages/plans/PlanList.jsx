import React, { useState, useEffect } from 'react';
import { Plus, Search, Calendar, Check, X, Shield } from 'lucide-react';
import { planApi } from '../../api/client';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export const PlanList = () => {
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    planCode: '',
    description: '',
    billingInterval: 'MONTHLY',
    intervalCount: 1,
    basePrice: '',
    allowPause: true,
    allowRenewal: true,
    autoClose: false,
  });
  const [submitting, setSubmitting] = useState(false);
  const { showToast } = useToast();

  const fetchPlans = async () => {
    setLoading(true);
    try {
      const res = await planApi.list({ search: search || undefined, size: 50 });
      if (res.data) setPlans(res.data.content || []);
    } catch (err) {
      showToast(err.message || 'Failed to fetch plans', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPlans();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await planApi.create({
        name: formData.name,
        planCode: formData.planCode.toUpperCase().trim(),
        description: formData.description,
        billingInterval: formData.billingInterval,
        intervalCount: parseInt(formData.intervalCount, 10) || 1,
        basePrice: parseFloat(formData.basePrice) || 0,
        allowPause: formData.allowPause,
        allowRenewal: formData.allowRenewal,
        autoClose: formData.autoClose,
      });
      showToast('Plan created successfully!', 'success');
      setModalOpen(false);
      fetchPlans();
    } catch (err) {
      showToast(err.message || 'Failed to create plan', 'error');
    } finally {
      setSubmitting(false);
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
          <h1 className="text-2xl font-bold text-black tracking-tight">Recurring Subscription Plans</h1>
          <p className="text-sm text-slate-600 mt-1">
            Interval definitions, recurring pricing formulas, and policy rules ({plans.length} plans).
          </p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>New Plan</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {plans.map((p) => (
          <div
            key={p.id}
            className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-purple-50 text-[#714B67] border border-purple-200 font-mono">
                  {p.planCode}
                </span>
                <span className="text-xs font-bold text-slate-700 uppercase">
                  {p.intervalCount > 1 ? `${p.intervalCount} ` : ''}
                  {p.billingInterval}
                </span>
              </div>

              <h3 className="text-lg font-bold text-black">{p.name}</h3>
              <p className="text-xs text-slate-600 mt-1 line-clamp-2">{p.description || 'Enterprise recurring plan.'}</p>

              <div className="mt-4 pt-4 border-t border-slate-100 space-y-2 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Allow Pause:</span>
                  {p.allowPause ? (
                    <span className="text-emerald-700 font-bold flex items-center">
                      <Check className="w-3.5 h-3.5 mr-0.5" /> Yes
                    </span>
                  ) : (
                    <span className="text-slate-400 flex items-center">
                      <X className="w-3.5 h-3.5 mr-0.5" /> No
                    </span>
                  )}
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Allow Renewal:</span>
                  {p.allowRenewal ? (
                    <span className="text-emerald-700 font-bold flex items-center">
                      <Check className="w-3.5 h-3.5 mr-0.5" /> Yes
                    </span>
                  ) : (
                    <span className="text-slate-400 flex items-center">
                      <X className="w-3.5 h-3.5 mr-0.5" /> No
                    </span>
                  )}
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Auto Close on End:</span>
                  {p.autoClose ? (
                    <span className="text-amber-700 font-bold">Automatic</span>
                  ) : (
                    <span className="text-slate-600">Manual review</span>
                  )}
                </div>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-100 flex items-baseline justify-between">
              <div>
                <span className="text-2xl font-bold text-black">{formatCurrency(p.basePrice)}</span>
                <span className="text-xs text-slate-500 ml-1">/ {p.billingInterval.toLowerCase()}</span>
              </div>
              <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                Active
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Create New Subscription Plan" maxWidth="max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Plan Name</label>
            <input
              type="text"
              required
              placeholder="Enterprise Growth"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Plan Code</label>
            <input
              type="text"
              required
              placeholder="PLAN-GROWTH-M"
              value={formData.planCode}
              onChange={(e) => setFormData({ ...formData, planCode: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black uppercase font-mono"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Billing Interval</label>
              <select
                value={formData.billingInterval}
                onChange={(e) => setFormData({ ...formData, billingInterval: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              >
                <option value="MONTHLY">Monthly</option>
                <option value="QUARTERLY">Quarterly</option>
                <option value="YEARLY">Yearly</option>
                <option value="WEEKLY">Weekly</option>
                <option value="DAILY">Daily</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Interval Multiplier</label>
              <input
                type="number"
                min="1"
                required
                value={formData.intervalCount}
                onChange={(e) => setFormData({ ...formData, intervalCount: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black font-bold"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Recurring Base Price (₹)</label>
            <input
              type="number"
              step="0.01"
              required
              placeholder="4999.00"
              value={formData.basePrice}
              onChange={(e) => setFormData({ ...formData, basePrice: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black font-bold"
            />
          </div>

          <div className="space-y-2 pt-2 border-t border-slate-200">
            <label className="flex items-center space-x-2 text-xs font-medium text-black">
              <input
                type="checkbox"
                checked={formData.allowPause}
                onChange={(e) => setFormData({ ...formData, allowPause: e.target.checked })}
                className="rounded text-[#714B67]"
              />
              <span>Allow subscriber to pause contract</span>
            </label>
            <label className="flex items-center space-x-2 text-xs font-medium text-black">
              <input
                type="checkbox"
                checked={formData.allowRenewal}
                onChange={(e) => setFormData({ ...formData, allowRenewal: e.target.checked })}
                className="rounded text-[#714B67]"
              />
              <span>Allow contract renewal</span>
            </label>
            <label className="flex items-center space-x-2 text-xs font-medium text-black">
              <input
                type="checkbox"
                checked={formData.autoClose}
                onChange={(e) => setFormData({ ...formData, autoClose: e.target.checked })}
                className="rounded text-[#714B67]"
              />
              <span>Auto-close when end date reached</span>
            </label>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-slate-200">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-3 py-1.5 text-xs font-semibold rounded border border-slate-300 text-slate-700"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-1.5 text-xs font-semibold rounded bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs"
            >
              {submitting ? 'Saving...' : 'Save Plan'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default PlanList;
