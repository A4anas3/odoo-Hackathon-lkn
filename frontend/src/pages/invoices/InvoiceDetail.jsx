import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Download,
  CreditCard,
  Send,
  XCircle,
  Receipt,
  Building,
  CheckCircle2,
  RefreshCw,
} from 'lucide-react';
import { invoiceApi, paymentApi } from '../../api/client';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export const InvoiceDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // Payment registration modal
  const [paymentModalOpen, setPaymentModalOpen] = useState(false);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('BANK_TRANSFER');
  const [refNumber, setRefNumber] = useState('');
  const [paymentNotes, setPaymentNotes] = useState('');

  const fetchInvoice = async () => {
    try {
      const res = await invoiceApi.get(id);
      if (res.data) {
        setInvoice(res.data);
        setPaymentAmount(res.data.balanceDue);
      }
    } catch (err) {
      showToast(err.message || 'Failed to fetch invoice', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoice();
  }, [id]);

  const handleDownloadPdf = async () => {
    try {
      const res = await invoiceApi.downloadPdf(id);
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${invoice.invoiceNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      showToast(`Downloaded PDF for ${invoice.invoiceNumber}`, 'success');
    } catch (err) {
      showToast('Error downloading invoice PDF: ' + err.message, 'error');
    }
  };

  const handlePostInvoice = async () => {
    setActionLoading(true);
    try {
      await invoiceApi.post(id);
      showToast('Invoice posted and finalized!', 'success');
      fetchInvoice();
    } catch (err) {
      showToast(err.message || 'Failed to post invoice', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelInvoice = async () => {
    setActionLoading(true);
    try {
      await invoiceApi.cancel(id);
      showToast('Invoice cancelled successfully', 'success');
      fetchInvoice();
    } catch (err) {
      showToast(err.message || 'Failed to cancel invoice', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRecordPayment = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    try {
      await paymentApi.create({
        customerId: invoice.customerId,
        amount: parseFloat(paymentAmount) || 0,
        paymentMethod,
        referenceNumber: refNumber,
        notes: paymentNotes,
        allocations: [
          {
            invoiceId: invoice.id,
            amount: parseFloat(paymentAmount) || 0,
          },
        ],
      });
      showToast('Payment recorded successfully!', 'success');
      setPaymentModalOpen(false);
      fetchInvoice();
    } catch (err) {
      showToast(err.message || 'Failed to record payment', 'error');
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

  if (!invoice) {
    return (
      <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center">
        <p className="text-black font-bold">Invoice not found</p>
        <button
          onClick={() => navigate('/invoices')}
          className="mt-4 px-4 py-2 bg-[#714B67] text-white rounded-lg text-sm"
        >
          Back to List
        </button>
      </div>
    );
  }

  const inv = invoice;

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => navigate('/invoices')}
            className="p-2 rounded-lg border border-slate-300 text-slate-600 hover:text-black hover:bg-slate-100 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-xl font-bold text-black tracking-tight">{inv.invoiceNumber}</h1>
              <StatusBadge status={inv.status} />
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Customer: <span className="font-semibold text-black">{inv.customerName}</span> ({inv.customerEmail})
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={handleDownloadPdf}
            className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-purple-50 hover:bg-purple-100 text-[#714B67] border border-purple-200 shadow-xs transition-colors"
          >
            <Download className="w-4 h-4" />
            <span>Download Official PDF</span>
          </button>

          {inv.status === 'DRAFT' && (
            <button
              onClick={handlePostInvoice}
              disabled={actionLoading}
              className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors"
            >
              <Send className="w-4 h-4" />
              <span>Post Invoice</span>
            </button>
          )}

          {['POSTED', 'PARTIALLY_PAID'].includes(inv.status) && (
            <button
              onClick={() => {
                setPaymentAmount(inv.balanceDue);
                setPaymentModalOpen(true);
              }}
              className="flex items-center space-x-1.5 px-4 py-2 rounded-lg text-xs font-bold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-md transition-all"
            >
              <CreditCard className="w-4 h-4" />
              <span>Register Payment</span>
            </button>
          )}

          {inv.status !== 'PAID' && inv.status !== 'CANCELLED' && (
            <button
              onClick={handleCancelInvoice}
              disabled={actionLoading}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 shadow-xs transition-colors"
            >
              <XCircle className="w-4 h-4" />
              <span>Cancel</span>
            </button>
          )}
        </div>
      </div>

      {/* Overview Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-2">Invoice Info</span>
          <div className="space-y-2 text-xs divide-y divide-slate-100">
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Issue Date:</span>
              <span className="font-semibold text-black">{inv.issueDate}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Due Date:</span>
              <span className="font-bold text-black">{inv.dueDate}</span>
            </div>
            <div className="flex justify-between pt-2">
              <span className="text-slate-500">Payment Terms:</span>
              <span className="font-medium text-black">{inv.paymentTerms || 'Net 15'}</span>
            </div>
            {inv.subscriptionNumber && (
              <div className="flex justify-between pt-2">
                <span className="text-slate-500">Subscription:</span>
                <Link
                  to={`/subscriptions/${inv.subscriptionId}`}
                  className="font-bold text-[#714B67] hover:underline"
                >
                  {inv.subscriptionNumber}
                </Link>
              </div>
            )}
            {inv.billingPeriodStart && (
              <div className="flex justify-between pt-2">
                <span className="text-slate-500">Billing Period:</span>
                <span className="text-slate-700 font-medium">
                  {inv.billingPeriodStart} to {inv.billingPeriodEnd}
                </span>
              </div>
            )}
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-2">Billed Customer</span>
          <h3 className="text-base font-bold text-black">{inv.customerName}</h3>
          <p className="text-xs text-slate-500">{inv.customerEmail}</p>
          {inv.customerGstin && (
            <p className="text-xs font-mono font-bold text-indigo-700 mt-2">
              GSTIN: {inv.customerGstin}
            </p>
          )}
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-2">Balance Due</span>
          <h3 className="text-3xl font-bold text-rose-700">{formatCurrency(inv.balanceDue)}</h3>
          <div className="mt-3 space-y-1.5 text-xs text-slate-600">
            <div className="flex justify-between">
              <span>Total Invoice Amount:</span>
              <span className="font-bold text-black">{formatCurrency(inv.totalAmount)}</span>
            </div>
            <div className="flex justify-between text-emerald-700">
              <span>Paid to date:</span>
              <span className="font-semibold">{formatCurrency(inv.paidAmount)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Items Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <h3 className="text-base font-bold text-black mb-4">Invoice Line Items</h3>
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
              {inv.lines?.map((line) => (
                <tr key={line.id} className="hover:bg-slate-50">
                  <td className="py-3 px-3 font-medium text-black">{line.description}</td>
                  <td className="py-3 px-3 text-center">{line.quantity}</td>
                  <td className="py-3 px-3 text-right font-medium text-black">{formatCurrency(line.unitPrice)}</td>
                  <td className="py-3 px-3 text-right text-slate-600">
                    {(Number(line.taxRate || 0) * 100).toFixed(1)}%
                  </td>
                  <td className="py-3 px-3 text-right font-bold text-black">{formatCurrency(line.lineTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Totals Summary */}
        <div className="mt-6 flex justify-end">
          <div className="w-full max-w-sm space-y-2 text-xs divide-y divide-slate-100">
            <div className="flex justify-between pt-1">
              <span className="text-slate-500">Subtotal:</span>
              <span className="font-semibold text-black">{formatCurrency(inv.subtotal)}</span>
            </div>
            {Number(inv.discountAmount) > 0 && (
              <div className="flex justify-between pt-1 text-rose-600">
                <span>Discount:</span>
                <span>- {formatCurrency(inv.discountAmount)}</span>
              </div>
            )}
            {inv.taxLines?.map((tl) => (
              <div key={tl.id} className="flex justify-between pt-1">
                <span className="text-slate-500">{tl.taxName}:</span>
                <span className="font-semibold text-black">{formatCurrency(tl.taxAmount)}</span>
              </div>
            ))}
            <div className="flex justify-between pt-2 text-sm font-bold border-t border-slate-300">
              <span>Total Amount:</span>
              <span className="text-black">{formatCurrency(inv.totalAmount)}</span>
            </div>
            <div className="flex justify-between pt-1 font-bold text-rose-700">
              <span>Balance Due:</span>
              <span>{formatCurrency(inv.balanceDue)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Payment Registration Modal */}
      <Modal
        isOpen={paymentModalOpen}
        onClose={() => setPaymentModalOpen(false)}
        title={`Register Payment for ${inv.invoiceNumber}`}
        maxWidth="max-w-md"
      >
        <form onSubmit={handleRecordPayment} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Payment Amount (₹)</label>
            <input
              type="number"
              step="0.01"
              required
              max={inv.balanceDue}
              value={paymentAmount}
              onChange={(e) => setPaymentAmount(e.target.value)}
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black font-bold focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
            <p className="text-[11px] text-slate-500 mt-1">Maximum payable: {formatCurrency(inv.balanceDue)}</p>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Payment Method</label>
            <select
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            >
              <option value="BANK_TRANSFER">Bank Transfer (NEFT/RTGS/IMPS)</option>
              <option value="UPI">UPI</option>
              <option value="CREDIT_CARD">Credit Card</option>
              <option value="CHEQUE">Cheque</option>
              <option value="CASH">Cash</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Reference / UTR Number</label>
            <input
              type="text"
              placeholder="e.g. UTR123456789"
              value={refNumber}
              onChange={(e) => setRefNumber(e.target.value)}
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setPaymentModalOpen(false)}
              className="px-3.5 py-2 text-xs font-semibold rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={actionLoading}
              className="px-4 py-2 text-xs font-semibold rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs disabled:opacity-50"
            >
              {actionLoading ? 'Recording...' : 'Record Payment'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default InvoiceDetail;
