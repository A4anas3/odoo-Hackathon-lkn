import React, { useState, useEffect } from 'react';
import { Plus, Search, Tag, Check, Calendar, Hash } from 'lucide-react';
import { discountApi } from '../../api/client';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export const DiscountList = () => {
  const [discounts, setDiscounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    discountType: 'PERCENTAGE',
    value: '',
    minOrderAmount: '',
    maxDiscountAmount: '',
    usageLimit: '',
    validFrom: new Date().toISOString().split('T')[0],
    validUntil: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const { showToast } = useToast();

  const fetchDiscounts = async () => {
    setLoading(true);
    try {
      const res = await discountApi.list({ search: search || undefined, size: 50 });
      if (res.data) setDiscounts(res.data.content || []);
    } catch (err) {
      showToast(err.message || 'Failed to fetch discounts', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDiscounts();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await discountApi.create({
        code: formData.code.toUpperCase().trim(),
        name: formData.name,
        discountType: formData.discountType,
        value: parseFloat(formData.value) || 0,
        minOrderAmount: formData.minOrderAmount ? parseFloat(formData.minOrderAmount) : null,
        maxDiscountAmount: formData.maxDiscountAmount ? parseFloat(formData.maxDiscountAmount) : null,
        usageLimit: formData.usageLimit ? parseInt(formData.usageLimit, 10) : null,
        validFrom: formData.validFrom || null,
        validUntil: formData.validUntil || null,
      });
      showToast('Discount coupon created!', 'success');
      setModalOpen(false);
      fetchDiscounts();
    } catch (err) {
      showToast(err.message || 'Failed to create discount', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-black tracking-tight">Discount Coupons & Offers</h1>
          <p className="text-sm text-slate-600 mt-1">
            Promotional codes, volume discounts, and redemption tracking ({discounts.length} coupons).
          </p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>New Coupon</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {discounts.map((d) => (
          <div
            key={d.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
          >
            <div>
              <div className="flex items-center justify-between">
                <span className="text-xs font-mono font-bold px-2.5 py-1 rounded-md bg-purple-50 text-[#714B67] border border-purple-200 uppercase tracking-wider">
                  {d.code}
                </span>
                <span className="text-xs font-bold px-2 py-0.5 rounded bg-emerald-50 text-emerald-800 border border-emerald-200">
                  {d.discountType === 'PERCENTAGE' ? `${d.value}% OFF` : `₹${d.value} FLAT`}
                </span>
              </div>

              <h3 className="text-base font-bold text-black mt-3">{d.name}</h3>

              <div className="mt-4 pt-3 border-t border-slate-100 space-y-1.5 text-xs text-slate-600">
                <div className="flex justify-between">
                  <span>Usage Count:</span>
                  <span className="font-bold text-black">
                    {d.usageCount} {d.usageLimit ? `/ ${d.usageLimit}` : 'redemptions'}
                  </span>
                </div>
                {d.minOrderAmount && (
                  <div className="flex justify-between">
                    <span>Min Order:</span>
                    <span className="font-semibold text-black">₹{d.minOrderAmount}</span>
                  </div>
                )}
                {d.validUntil && (
                  <div className="flex justify-between">
                    <span>Expires:</span>
                    <span className="font-semibold text-rose-700">{d.validUntil}</span>
                  </div>
                )}
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
              <span className="text-slate-500">{d.appliesTo?.replace(/_/g, ' ')}</span>
              <span className="font-bold text-emerald-700">Active</span>
            </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Create Discount Coupon" maxWidth="max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Coupon Code</label>
            <input
              type="text"
              required
              placeholder="FESTIVE25"
              value={formData.code}
              onChange={(e) => setFormData({ ...formData, code: e.target.value.toUpperCase() })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black uppercase font-mono font-bold"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Offer Title</label>
            <input
              type="text"
              required
              placeholder="Festive Season 25% Off"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Type</label>
              <select
                value={formData.discountType}
                onChange={(e) => setFormData({ ...formData, discountType: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              >
                <option value="PERCENTAGE">Percentage (%)</option>
                <option value="FIXED_AMOUNT">Fixed Amount (₹)</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Value</label>
              <input
                type="number"
                step="0.01"
                required
                placeholder="25"
                value={formData.value}
                onChange={(e) => setFormData({ ...formData, value: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black font-bold"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Min Order (₹)</label>
              <input
                type="number"
                placeholder="Optional"
                value={formData.minOrderAmount}
                onChange={(e) => setFormData({ ...formData, minOrderAmount: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Max Cap (₹)</label>
              <input
                type="number"
                placeholder="Optional"
                value={formData.maxDiscountAmount}
                onChange={(e) => setFormData({ ...formData, maxDiscountAmount: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Usage Limit</label>
              <input
                type="number"
                placeholder="e.g. 100"
                value={formData.usageLimit}
                onChange={(e) => setFormData({ ...formData, usageLimit: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Valid Until</label>
              <input
                type="date"
                value={formData.validUntil}
                onChange={(e) => setFormData({ ...formData, validUntil: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-2 border-t border-slate-200">
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
              {submitting ? 'Creating...' : 'Create Coupon'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default DiscountList;
