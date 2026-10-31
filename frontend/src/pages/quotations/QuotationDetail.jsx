import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Send, CheckCircle2, Repeat, XCircle, FileText, RefreshCw } from 'lucide-react';
import { quotationApi, subscriptionApi } from '../../api/client';
import StatusBadge from '../../components/common/StatusBadge';
import { useToast } from '../../context/ToastContext';

export const QuotationDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [quotation, setQuotation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchQuotation = async () => {
    try {
      const res = await quotationApi.get(id);
      if (res.data) setQuotation(res.data);
    } catch (err) {
      showToast(err.message || 'Failed to load quotation', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQuotation();
  }, [id]);

  const handleStatusChange = async (targetStatus) => {
    setActionLoading(true);
    try {
      await quotationApi.updateStatus(id, targetStatus);
      showToast(`Quotation status updated to ${targetStatus}`, 'success');
      fetchQuotation();
    } catch (err) {
      showToast(err.message || 'Failed to update quotation status', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const handleConvertToSubscription = async () => {
    setActionLoading(true);
    try {
      const res = await subscriptionApi.createFromQuotation(id);
      if (res.data) {
        showToast('Successfully converted quotation to active subscription!', 'success');
        navigate(`/subscriptions/${res.data.id}`);
      }
    } catch (err) {
      showToast(err.message || 'Conversion failed', 'error');
    } finally {
      setActionLoading(false);
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

  if (!quotation) {
    return (
      <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center">
        <p className="text-black font-bold">Quotation not found</p>
        <button
          onClick={() => navigate('/quotations')}
          className="mt-4 px-4 py-2 bg-[#714B67] text-white rounded-lg text-sm"
        >
          Back to List
        </button>
      </div>
    );
  }

  const q = quotation;

  return (
    <div className="space-y-6">
      {/* Header bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => navigate('/quotations')}
            className="p-2 rounded-lg border border-slate-300 text-slate-600 hover:text-black hover:bg-slate-100 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-xl font-bold text-black tracking-tight">{q.quotationNumber}</h1>
              <StatusBadge status={q.status} />
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Customer: <span className="font-semibold text-black">{q.customerName}</span> ({q.customerEmail})
            </p>
          </div>
        </div>

        {/* Status Actions */}
        <div className="flex flex-wrap items-center gap-2">
          {q.status === 'DRAFT' && (
            <button
              onClick={() => handleStatusChange('SENT')}
              disabled={actionLoading}
              className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-sky-600 hover:bg-sky-700 text-white shadow-xs transition-colors"
            >
              <Send className="w-4 h-4" />
              <span>Mark as Sent</span>
            </button>
          )}

          {['DRAFT', 'SENT'].includes(q.status) && (
            <button
              onClick={() => handleStatusChange('CONFIRMED')}
              disabled={actionLoading}
              className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Confirm Quotation</span>
            </button>
          )}

          {q.status === 'CONFIRMED' && (
            <button
              onClick={handleConvertToSubscription}
              disabled={actionLoading}
              className="flex items-center space-x-1.5 px-4 py-2 rounded-lg text-xs font-bold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-md transition-all animate-pulse"
            >
              <Repeat className="w-4 h-4" />
              <span>Convert to Subscription Contract</span>
            </button>
          )}

          {q.status !== 'CANCELLED' && q.status !== 'CONFIRMED' && (
            <button
              onClick={() => handleStatusChange('CANCELLED')}
              disabled={actionLoading}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-rose-600 hover:bg-rose-700 text-white shadow-xs transition-colors"
            >
              <XCircle className="w-4 h-4" />
              <span>Cancel</span>
            </button>
          )}
        </div>
      </div>

      {/* Details Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-2">Quotation Info</span>
          <div className="space-y-2 text-xs divide-y divide-slate-100">
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Order Date:</span>
              <span className="font-semibold text-black">{q.orderDate || '-'}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Valid Until:</span>
              <span className="font-semibold text-black">{q.validUntil || '-'}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Template Used:</span>
              <span className="font-medium text-black">{q.templateName || 'Custom'}</span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-2">Customer Profile</span>
          <h3 className="text-base font-bold text-black">{q.customerName}</h3>
          <p className="text-xs text-slate-500">{q.customerEmail}</p>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-2">Financial Totals</span>
          <h3 className="text-2xl font-bold text-black">{formatCurrency(q.totalAmount)}</h3>
          <div className="mt-3 space-y-1.5 text-xs text-slate-600">
            <div className="flex justify-between">
              <span>Subtotal:</span>
              <span className="font-semibold text-black">{formatCurrency(q.subtotal)}</span>
            </div>
            {Number(q.discountAmount) > 0 && (
              <div className="flex justify-between text-rose-600">
                <span>Discount:</span>
                <span>- {formatCurrency(q.discountAmount)}</span>
              </div>
            )}
            <div className="flex justify-between">
              <span>Tax (GST):</span>
              <span className="font-semibold text-black">{formatCurrency(q.taxAmount)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Items Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <h3 className="text-base font-bold text-black mb-4">Quoted Line Items</h3>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs uppercase font-bold text-slate-500">
              <tr>
                <th className="py-2.5 px-3">Item / Service</th>
                <th className="py-2.5 px-3 text-center">Qty</th>
                <th className="py-2.5 px-3 text-right">Unit Price</th>
                <th className="py-2.5 px-3 text-right">Discount</th>
                <th className="py-2.5 px-3 text-right">Line Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {q.lines?.map((line) => (
                <tr key={line.id} className="hover:bg-slate-50">
                  <td className="py-3 px-3 font-medium text-black">
                    {line.planName || line.productName || line.description || 'Quotation item'}
                  </td>
                  <td className="py-3 px-3 text-center">{line.quantity}</td>
                  <td className="py-3 px-3 text-right font-medium text-black">{formatCurrency(line.unitPrice)}</td>
                  <td className="py-3 px-3 text-right text-rose-600">
                    {Number(line.discountAmount) > 0 ? `- ${formatCurrency(line.discountAmount)}` : '-'}
                  </td>
                  <td className="py-3 px-3 text-right font-bold text-black">{formatCurrency(line.lineTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {q.termsAndConditions && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <h3 className="text-xs font-bold uppercase tracking-wider text-black mb-2">Terms & Conditions</h3>
          <p className="text-xs text-slate-700 whitespace-pre-wrap">{q.termsAndConditions}</p>
        </div>
      )}
    </div>
  );
};

export default QuotationDetail;
