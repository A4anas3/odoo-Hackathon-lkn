import React, { useState, useEffect } from 'react';
import { Plus, Percent, Calculator, Check, ArrowRight } from 'lucide-react';
import { taxApi } from '../../api/client';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export const TaxList = () => {
  const [taxes, setTaxes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    taxType: 'GST',
    rate: '0.18',
    country: 'India',
    state: '',
    isCompound: false,
  });
  const [submitting, setSubmitting] = useState(false);
  const { showToast } = useToast();

  // Tax Calculator State
  const [calcAmount, setCalcAmount] = useState('10000');
  const [calcState, setCalcState] = useState('Uttar Pradesh');
  const [calcResult, setCalcResult] = useState(null);
  const [calculating, setCalculating] = useState(false);

  const fetchTaxes = async () => {
    setLoading(true);
    try {
      const res = await taxApi.getActive();
      if (res.data) setTaxes(res.data || []);
    } catch (err) {
      showToast(err.message || 'Failed to fetch taxes', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTaxes();
    handleCalculate();
  }, []);

  const handleCalculate = async () => {
    if (!calcAmount) return;
    setCalculating(true);
    try {
      const res = await taxApi.calculate(parseFloat(calcAmount) || 0, calcState);
      if (res.data) setCalcResult(res.data);
    } catch (err) {
      showToast(err.message || 'Tax calculation failed', 'error');
    } finally {
      setCalculating(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await taxApi.create({
        name: formData.name,
        taxType: formData.taxType,
        rate: parseFloat(formData.rate) || 0,
        country: formData.country,
        state: formData.state || null,
        isCompound: formData.isCompound,
        isActive: true,
      });
      showToast('Tax configuration created!', 'success');
      setModalOpen(false);
      fetchTaxes();
    } catch (err) {
      showToast(err.message || 'Failed to create tax', 'error');
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
          <h1 className="text-2xl font-bold text-black tracking-tight">Tax Configurations & GST Rates</h1>
          <p className="text-sm text-slate-600 mt-1">
            Indian GST fiscal positions, CGST, SGST, IGST rules, and tax calculators.
          </p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>New Tax Rule</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Taxes List */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs">
            <div className="p-4 border-b border-slate-200 bg-slate-50 font-bold text-xs uppercase tracking-wider text-black">
              Active Fiscal Tax Rules ({taxes.length})
            </div>
            <div className="divide-y divide-slate-100">
              {taxes.map((t) => (
                <div key={t.id} className="p-4 flex items-center justify-between hover:bg-slate-50 transition-colors">
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="font-bold text-sm text-black">{t.name}</span>
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-purple-50 text-[#714B67] border border-purple-200">
                        {t.taxType}
                      </span>
                    </div>
                    <p className="text-xs text-slate-500 mt-0.5">
                      Country: {t.country} {t.state ? `• State: ${t.state}` : '• All States'}
                    </p>
                  </div>
                  <div className="text-right">
                    <span className="text-lg font-bold text-black">{(Number(t.rate) * 100).toFixed(1)}%</span>
                    <span className="block text-[10px] text-emerald-700 font-bold uppercase">Active</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Interactive Tax Simulator */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center space-x-2 text-xs font-bold text-[#714B67] uppercase tracking-wider">
            <Calculator className="w-4 h-4" />
            <span>Live GST Tax Simulator</span>
          </div>
          <p className="text-xs text-slate-500">
            Tests intra-state (CGST + SGST) vs inter-state (IGST) split based on customer location.
          </p>

          <div className="space-y-3">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Taxable Amount (₹)</label>
              <input
                type="number"
                value={calcAmount}
                onChange={(e) => setCalcAmount(e.target.value)}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black font-bold"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Customer State</label>
              <select
                value={calcState}
                onChange={(e) => setCalcState(e.target.value)}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              >
                <option value="Uttar Pradesh">Uttar Pradesh (Intra-state CGST + SGST)</option>
                <option value="Maharashtra">Maharashtra (Inter-state IGST)</option>
                <option value="Karnataka">Karnataka (Inter-state IGST)</option>
                <option value="Delhi">Delhi (Inter-state IGST)</option>
              </select>
            </div>

            <button
              type="button"
              onClick={handleCalculate}
              disabled={calculating}
              className="w-full py-2 bg-slate-900 hover:bg-black text-white text-xs font-bold rounded-lg shadow-xs"
            >
              {calculating ? 'Calculating...' : 'Recalculate GST'}
            </button>
          </div>

          {calcResult && (
            <div className="mt-4 pt-4 border-t border-slate-100 space-y-2 text-xs">
              <div className="flex justify-between text-slate-600">
                <span>Taxable Amount:</span>
                <span className="font-semibold text-black">{formatCurrency(calcResult.taxableAmount)}</span>
              </div>
              {calcResult.breakdown?.map((b, i) => (
                <div key={i} className="flex justify-between text-[#714B67] font-medium">
                  <span>{b.name} ({(Number(b.rate) * 100).toFixed(0)}%):</span>
                  <span>{formatCurrency(b.taxAmount)}</span>
                </div>
              ))}
              <div className="flex justify-between pt-2 border-t border-slate-200 font-bold text-sm text-black">
                <span>Total Tax:</span>
                <span className="text-emerald-700">{formatCurrency(calcResult.totalTax)}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Create Tax Rule" maxWidth="max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Tax Name</label>
            <input
              type="text"
              required
              placeholder="GST 18% (Goods & Services)"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Tax Type</label>
              <select
                value={formData.taxType}
                onChange={(e) => setFormData({ ...formData, taxType: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              >
                <option value="GST">GST</option>
                <option value="CGST">CGST</option>
                <option value="SGST">SGST</option>
                <option value="IGST">IGST</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Rate (0.18 = 18%)</label>
              <input
                type="number"
                step="0.0001"
                required
                placeholder="0.18"
                value={formData.rate}
                onChange={(e) => setFormData({ ...formData, rate: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black font-bold"
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
              {submitting ? 'Saving...' : 'Save Tax'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default TaxList;
