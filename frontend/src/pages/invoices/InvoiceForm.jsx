import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, Plus, Trash2 } from 'lucide-react';
import { invoiceApi, customerApi, productApi } from '../../api/client';
import { useToast } from '../../context/ToastContext';

export const InvoiceForm = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const [customerId, setCustomerId] = useState('');
  const [issueDate, setIssueDate] = useState(new Date().toISOString().split('T')[0]);
  const [dueDate, setDueDate] = useState('');
  const [paymentTerms, setPaymentTerms] = useState('Net 15');
  const [notes, setNotes] = useState('');

  const [lines, setLines] = useState([]);

  useEffect(() => {
    const loadPrerequisites = async () => {
      try {
        const [cRes, pRes] = await Promise.all([
          customerApi.list({ size: 100 }),
          productApi.list({ isActive: true, size: 50 }),
        ]);
        if (cRes.data) setCustomers(cRes.data.content || []);
        if (pRes.data) setProducts(pRes.data.content || []);

        if (cRes.data?.content?.length > 0) setCustomerId(cRes.data.content[0].id);
        if (pRes.data?.content?.length > 0) {
          const first = pRes.data.content[0];
          setLines([
            {
              productId: first.id,
              description: first.name,
              quantity: 1,
              unitPrice: first.salePrice || 100,
            },
          ]);
        }
      } catch (err) {
        showToast('Failed to load customers or products: ' + err.message, 'error');
      } finally {
        setLoading(false);
      }
    };
    loadPrerequisites();
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
    if (field === 'productId') {
      const prod = products.find((p) => p.id === value);
      if (prod) {
        updated[index].description = prod.name;
        updated[index].unitPrice = prod.salePrice || 100;
      }
    }
    setLines(updated);
  };

  const removeLine = (index) => {
    setLines(lines.filter((_, i) => i !== index));
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
        issueDate,
        dueDate: dueDate || null,
        paymentTerms,
        notes,
        lines: lines.map((l) => ({
          productId: l.productId,
          description: l.description,
          quantity: parseInt(l.quantity, 10) || 1,
          unitPrice: parseFloat(l.unitPrice) || 0,
        })),
      };

      const res = await invoiceApi.create(payload);
      if (res.data) {
        showToast('Invoice created successfully!', 'success');
        navigate(`/invoices/${res.data.id}`);
      }
    } catch (err) {
      showToast(err.message || 'Failed to create invoice', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="p-8 text-center text-sm font-semibold text-slate-500">Loading invoice form...</div>;
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => navigate('/invoices')}
            className="p-2 rounded-lg border border-slate-300 text-slate-600 hover:text-black hover:bg-slate-100"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-black tracking-tight">Create Invoice</h1>
            <p className="text-xs text-slate-500 mt-0.5">Generate a manual tax invoice</p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <h3 className="text-sm font-bold text-black uppercase tracking-wider">Invoice Header</h3>

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
              <label className="block text-xs font-bold text-black uppercase mb-1">Payment Terms</label>
              <select
                value={paymentTerms}
                onChange={(e) => setPaymentTerms(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              >
                <option value="Immediate">Immediate</option>
                <option value="Net 15">Net 15</option>
                <option value="Net 30">Net 30</option>
                <option value="Net 60">Net 60</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Issue Date</label>
              <input
                type="date"
                required
                value={issueDate}
                onChange={(e) => setIssueDate(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Due Date</label>
              <input
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
                className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
              />
            </div>
          </div>
        </div>

        {/* Lines */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-black uppercase tracking-wider">Line Items</h3>
            <button
              type="button"
              onClick={addLine}
              className="flex items-center space-x-1 text-xs font-bold text-[#714B67] hover:underline"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Line</span>
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

        {/* Actions */}
        <div className="flex justify-end space-x-3">
          <button
            type="button"
            onClick={() => navigate('/invoices')}
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
            <span>{submitting ? 'Creating...' : 'Create Invoice'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};

export default InvoiceForm;
