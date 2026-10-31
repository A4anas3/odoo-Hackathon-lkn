import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, Plus, Trash2, Tag, CheckCircle2 } from 'lucide-react';
import { quotationApi, quotationTemplateApi, customerApi, planApi, productApi, discountApi } from '../../api/client';
import { useToast } from '../../context/ToastContext';

export const QuotationForm = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [customers, setCustomers] = useState([]);
  const [plans, setPlans] = useState([]);
  const [products, setProducts] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // Form
  const [customerId, setCustomerId] = useState('');
  const [templateId, setTemplateId] = useState('');
  const [orderDate, setOrderDate] = useState(new Date().toISOString().split('T')[0]);
  const [validUntil, setValidUntil] = useState('');
  const [discountCode, setDiscountCode] = useState('');
  const [discountInfo, setDiscountInfo] = useState(null);
  const [terms, setTerms] = useState('Payment within 15 days of invoice date. Subscription renews automatically.');
  const [notes, setNotes] = useState('');

  const [lines, setLines] = useState([]);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [cRes, pRes, prRes, tRes] = await Promise.all([
          customerApi.list({ size: 100 }),
          planApi.list({ isActive: true, size: 50 }),
          productApi.list({ isActive: true, size: 50 }),
          quotationTemplateApi.getActive(),
        ]);
        if (cRes.data) setCustomers(cRes.data.content || []);
        if (pRes.data) setPlans(pRes.data.content || []);
        if (prRes.data) setProducts(prRes.data.content || []);
        if (tRes.data) setTemplates(tRes.data || []);

        if (cRes.data?.content?.length > 0) setCustomerId(cRes.data.content[0].id);

        // Pre-populate with first plan
        if (pRes.data?.content?.length > 0) {
          const firstPlan = pRes.data.content[0];
          setLines([
            {
              planId: firstPlan.id,
              description: firstPlan.name + ' Plan',
              quantity: 1,
              unitPrice: firstPlan.basePrice,
            },
          ]);
        }
      } catch (err) {
        showToast('Error loading prerequisites: ' + err.message, 'error');
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  const addLine = () => {
    if (products.length === 0) return;
    const prod = products[0];
    setLines([
      ...lines,
      {
        productId: prod.id,
        description: prod.name,
        quantity: 1,
        unitPrice: prod.salePrice || 100,
      },
    ]);
  };

  const updateLine = (index, field, value) => {
    const updated = [...lines];
    updated[index][field] = value;
    setLines(updated);
  };

  const removeLine = (index) => {
    setLines(lines.filter((_, i) => i !== index));
  };

  const handleValidateCoupon = async () => {
    if (!discountCode.trim()) return;
    try {
      const subtotal = lines.reduce((acc, l) => acc + (parseFloat(l.unitPrice) || 0) * (parseInt(l.quantity, 10) || 1), 0);
      const res = await discountApi.validate(discountCode.trim(), subtotal, customerId || undefined);
      if (res.data && res.data.valid) {
        setDiscountInfo(res.data);
        showToast('Coupon verified! Discount: ₹' + res.data.calculatedDiscountAmount, 'success');
      } else {
        setDiscountInfo(null);
        showToast(res.data?.message || 'Invalid coupon code', 'error');
      }
    } catch (err) {
      setDiscountInfo(null);
      showToast(err.message || 'Error validating coupon', 'error');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!customerId) {
      showToast('Please select a customer', 'error');
      return;
    }
    if (lines.length === 0) {
      showToast('Please add at least one line item', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        customerId,
        templateId: templateId || null,
        orderDate,
        validUntil: validUntil || null,
        termsAndConditions: terms,
        notes,
        discountCode: discountCode.trim() || null,
        lines: lines.map((l) => ({
          productId: l.productId || null,
          planId: l.planId || null,
          description: l.description,
          quantity: parseInt(l.quantity, 10) || 1,
          unitPrice: parseFloat(l.unitPrice) || 0,
        })),
      };

      const res = await quotationApi.create(payload);
      if (res.data) {
        showToast('Quotation created successfully!', 'success');
        navigate(`/quotations/${res.data.id}`);
      }
    } catch (err) {
      showToast(err.message || 'Failed to create quotation', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="p-8 text-center text-sm font-semibold text-slate-500">Loading quotation form...</div>;
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => navigate('/quotations')}
            className="p-2 rounded-lg border border-slate-300 text-slate-600 hover:text-black hover:bg-slate-100"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-black tracking-tight">Create Quotation Proposal</h1>
            <p className="text-xs text-slate-500 mt-0.5">Prepare a contract quotation for customer confirmation</p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <h3 className="text-sm font-bold text-black uppercase tracking-wider">Customer & Template</h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Customer</label>
              <select
                required
                value={customerId}
                onChange={(e) => setCustomerId(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              >
                {customers.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.companyName || c.contactName} ({c.email})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Quotation Template</label>
              <select
                value={templateId}
                onChange={(e) => setTemplateId(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              >
                <option value="">No Template (Custom)</option>
                {templates.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name} ({t.validityDays} days validity)
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Order Date</label>
              <input
                type="date"
                required
                value={orderDate}
                onChange={(e) => setOrderDate(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Valid Until</label>
              <input
                type="date"
                value={validUntil}
                onChange={(e) => setValidUntil(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>
          </div>
        </div>

        {/* Line Items */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-black uppercase tracking-wider">Quotation Line Items</h3>
            <button
              type="button"
              onClick={addLine}
              className="flex items-center space-x-1 text-xs font-bold text-[#714B67] hover:underline"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Item</span>
            </button>
          </div>

          <div className="space-y-3">
            {lines.map((l, index) => (
              <div key={index} className="flex gap-3 items-center bg-slate-50 p-3 rounded-lg border border-slate-200">
                <input
                  type="text"
                  required
                  placeholder="Item description"
                  value={l.description}
                  onChange={(e) => updateLine(index, 'description', e.target.value)}
                  className="flex-1 p-2 bg-white border border-slate-300 rounded text-xs text-black"
                />
                <input
                  type="number"
                  min="1"
                  required
                  value={l.quantity}
                  onChange={(e) => updateLine(index, 'quantity', e.target.value)}
                  className="w-16 p-2 bg-white border border-slate-300 rounded text-xs text-center font-bold"
                />
                <input
                  type="number"
                  step="0.01"
                  required
                  value={l.unitPrice}
                  onChange={(e) => updateLine(index, 'unitPrice', e.target.value)}
                  className="w-28 p-2 bg-white border border-slate-300 rounded text-xs text-right font-bold"
                />
                <button
                  type="button"
                  onClick={() => removeLine(index)}
                  className="p-1.5 text-rose-600 hover:bg-rose-50 rounded"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Coupon Discount */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-3">
          <h3 className="text-sm font-bold text-black uppercase tracking-wider">Discount Coupon</h3>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Tag className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                value={discountCode}
                onChange={(e) => setDiscountCode(e.target.value.toUpperCase())}
                placeholder="Enter coupon code (e.g. WELCOME10)"
                className="w-full pl-9 pr-3 py-2 bg-white border border-slate-300 rounded-lg text-sm uppercase text-black font-mono focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>
            <button
              type="button"
              onClick={handleValidateCoupon}
              className="px-4 py-2 bg-slate-900 hover:bg-black text-white text-xs font-bold rounded-lg"
            >
              Apply
            </button>
          </div>
          {discountInfo && (
            <div className="flex items-center space-x-2 text-xs font-semibold text-emerald-800 bg-emerald-50 p-2.5 rounded-lg border border-emerald-200">
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
              <span>Applied {discountInfo.code}: -₹{discountInfo.calculatedDiscountAmount}</span>
            </div>
          )}
        </div>

        {/* Terms */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-3">
          <label className="block text-xs font-bold text-black uppercase tracking-wider">
            Terms & Conditions
          </label>
          <textarea
            rows={3}
            value={terms}
            onChange={(e) => setTerms(e.target.value)}
            className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-xs text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
          />
        </div>

        {/* Actions */}
        <div className="flex justify-end space-x-3">
          <button
            type="button"
            onClick={() => navigate('/quotations')}
            className="px-5 py-2.5 rounded-lg border border-slate-300 text-xs font-bold text-slate-700 hover:bg-slate-100"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="flex items-center space-x-1.5 px-6 py-2.5 rounded-lg bg-[#714B67] hover:bg-[#5d3d54] text-white text-xs font-bold shadow-md disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            <span>{submitting ? 'Saving...' : 'Create Quotation'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};

export default QuotationForm;
