import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, Tag, CheckCircle2, AlertCircle, Plus, Trash2 } from 'lucide-react';
import { subscriptionApi, customerApi, planApi, productApi, discountApi } from '../../api/client';
import { useToast } from '../../context/ToastContext';

export const SubscriptionForm = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [customers, setCustomers] = useState([]);
  const [plans, setPlans] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // Form State
  const [customerId, setCustomerId] = useState('');
  const [planId, setPlanId] = useState('');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [unitPrice, setUnitPrice] = useState('');
  const [paymentTerms, setPaymentTerms] = useState('Immediate');
  const [discountCode, setDiscountCode] = useState('');
  const [discountInfo, setDiscountInfo] = useState(null);
  const [validatingCode, setValidatingCode] = useState(false);

  // Additional line items
  const [lines, setLines] = useState([]);

  useEffect(() => {
    const loadPrerequisites = async () => {
      try {
        const [cRes, pRes, prRes] = await Promise.all([
          customerApi.list({ size: 100 }),
          planApi.list({ isActive: true, size: 50 }),
          productApi.list({ isActive: true, size: 50 }),
        ]);
        if (cRes.data) setCustomers(cRes.data.content || []);
        if (pRes.data) setPlans(pRes.data.content || []);
        if (prRes.data) setProducts(prRes.data.content || []);

        if (cRes.data?.content?.length > 0) setCustomerId(cRes.data.content[0].id);
        if (pRes.data?.content?.length > 0) {
          const firstPlan = pRes.data.content[0];
          setPlanId(firstPlan.id);
          setUnitPrice(firstPlan.basePrice);
        }
      } catch (err) {
        showToast('Error loading prerequisites: ' + err.message, 'error');
      } finally {
        setLoading(false);
      }
    };
    loadPrerequisites();
  }, []);

  const handlePlanSelect = (id) => {
    setPlanId(id);
    const selected = plans.find((p) => p.id === id);
    if (selected) {
      setUnitPrice(selected.basePrice);
    }
  };

  const handleValidateCoupon = async () => {
    if (!discountCode.trim()) return;
    setValidatingCode(true);
    try {
      const baseSubtotal = (parseFloat(unitPrice) || 0) * (parseInt(quantity, 10) || 1);
      const res = await discountApi.validate(discountCode.trim(), baseSubtotal, customerId || undefined);
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
    } finally {
      setValidatingCode(false);
    }
  };

  const addLineItem = () => {
    if (products.length === 0) return;
    const defaultProd = products[0];
    setLines([
      ...lines,
      {
        productId: defaultProd.id,
        description: defaultProd.name,
        quantity: 1,
        unitPrice: defaultProd.salePrice || 100,
        taxRate: 0.18,
      },
    ]);
  };

  const updateLineItem = (index, field, value) => {
    const updated = [...lines];
    updated[index][field] = value;
    if (field === 'productId') {
      const prod = products.find((p) => p.id === value);
      if (prod) {
        updated[index].description = prod.name;
        updated[index].unitPrice = prod.salePrice || 100;
      }
    }
    setLines(updated);
  };

  const removeLineItem = (index) => {
    setLines(lines.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!customerId || !planId) {
      showToast('Please select a customer and a plan', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        customerId,
        planId,
        startDate,
        endDate: endDate || null,
        quantity: parseInt(quantity, 10) || 1,
        unitPrice: parseFloat(unitPrice) || 0,
        paymentTerms,
        discountCode: discountCode.trim() || null,
        lines: lines.map((l) => ({
          productId: l.productId,
          description: l.description,
          quantity: parseInt(l.quantity, 10) || 1,
          unitPrice: parseFloat(l.unitPrice) || 0,
          taxRate: parseFloat(l.taxRate) || 0,
        })),
      };

      const res = await subscriptionApi.create(payload);
      if (res.data) {
        showToast('Subscription created successfully!', 'success');
        navigate(`/subscriptions/${res.data.id}`);
      }
    } catch (err) {
      showToast(err.message || 'Failed to create subscription', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="p-8 text-center text-sm font-semibold text-slate-500">Loading form...</div>;
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => navigate('/subscriptions')}
            className="p-2 rounded-lg border border-slate-300 text-slate-600 hover:text-black hover:bg-slate-100"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-black tracking-tight">Create Subscription</h1>
            <p className="text-xs text-slate-500 mt-0.5">Setup a recurring billing contract</p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Core Settings */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <h3 className="text-sm font-bold text-black uppercase tracking-wider">Contract Details</h3>

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
              <label className="block text-xs font-bold text-black uppercase mb-1">Subscription Plan</label>
              <select
                required
                value={planId}
                onChange={(e) => handlePlanSelect(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              >
                {plans.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} (₹{p.basePrice} / {p.billingInterval})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Start Date</label>
              <input
                type="date"
                required
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">End Date (Optional)</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Plan Unit Price (₹)</label>
              <input
                type="number"
                step="0.01"
                required
                value={unitPrice}
                onChange={(e) => setUnitPrice(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Contract Quantity</label>
              <input
                type="number"
                min="1"
                required
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>
          </div>
        </div>

        {/* Coupon Discount Section */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <h3 className="text-sm font-bold text-black uppercase tracking-wider">Promotional Discount</h3>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Tag className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                value={discountCode}
                onChange={(e) => setDiscountCode(e.target.value.toUpperCase())}
                placeholder="Enter coupon code (e.g. WELCOME10, ANNUAL20)"
                className="w-full pl-9 pr-3 py-2 bg-white border border-slate-300 rounded-lg text-sm uppercase text-black font-mono focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>
            <button
              type="button"
              onClick={handleValidateCoupon}
              disabled={validatingCode || !discountCode}
              className="px-4 py-2 bg-slate-900 hover:bg-black text-white text-xs font-bold rounded-lg disabled:opacity-50"
            >
              {validatingCode ? 'Checking...' : 'Apply Coupon'}
            </button>
          </div>
          {discountInfo && (
            <div className="flex items-center space-x-2 text-xs font-semibold text-emerald-800 bg-emerald-50 p-2.5 rounded-lg border border-emerald-200">
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
              <span>Applied {discountInfo.code}: -₹{discountInfo.calculatedDiscountAmount}</span>
            </div>
          )}
        </div>

        {/* Additional Line Items */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-black uppercase tracking-wider">Service Add-ons (Optional)</h3>
            <button
              type="button"
              onClick={addLineItem}
              className="flex items-center space-x-1 text-xs font-bold text-[#714B67] hover:underline"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Add-on Item</span>
            </button>
          </div>

          {lines.map((item, index) => (
            <div key={index} className="flex gap-3 items-center bg-slate-50 p-3 rounded-lg border border-slate-200">
              <select
                value={item.productId}
                onChange={(e) => updateLineItem(index, 'productId', e.target.value)}
                className="flex-1 p-2 bg-white border border-slate-300 rounded text-xs text-black"
              >
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} (₹{p.salePrice})
                  </option>
                ))}
              </select>
              <input
                type="number"
                min="1"
                value={item.quantity}
                onChange={(e) => updateLineItem(index, 'quantity', e.target.value)}
                className="w-16 p-2 bg-white border border-slate-300 rounded text-xs text-center font-bold"
              />
              <input
                type="number"
                step="0.01"
                value={item.unitPrice}
                onChange={(e) => updateLineItem(index, 'unitPrice', e.target.value)}
                className="w-24 p-2 bg-white border border-slate-300 rounded text-xs text-right font-bold"
              />
              <button
                type="button"
                onClick={() => removeLineItem(index)}
                className="p-1.5 text-rose-600 hover:bg-rose-50 rounded"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          ))}
        </div>

        {/* Submit Bar */}
        <div className="flex justify-end space-x-3">
          <button
            type="button"
            onClick={() => navigate('/subscriptions')}
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
            <span>{submitting ? 'Creating...' : 'Create Subscription'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};

export default SubscriptionForm;
