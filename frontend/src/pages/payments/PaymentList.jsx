import React, { useState, useEffect } from 'react';
import { Plus, Search, Filter, CreditCard, ChevronLeft, ChevronRight, RotateCcw } from 'lucide-react';
import { paymentApi, customerApi } from '../../api/client';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export const PaymentList = () => {
  const [payments, setPayments] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [method, setMethod] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const { showToast } = useToast();

  // Create payment modal
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [customerId, setCustomerId] = useState('');
  const [amount, setAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('BANK_TRANSFER');
  const [referenceNumber, setReferenceNumber] = useState('');
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Refund modal
  const [refundModalOpen, setRefundModalOpen] = useState(false);
  const [selectedPaymentId, setSelectedPaymentId] = useState(null);
  const [refundReason, setRefundReason] = useState('');

  const fetchPayments = async () => {
    setLoading(true);
    try {
      const res = await paymentApi.list({
        search: search || undefined,
        method: method || undefined,
        page,
        size: 15,
      });
      if (res.data) {
        setPayments(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setTotalElements(res.data.totalElements || 0);
      }
    } catch (err) {
      showToast(err.message || 'Failed to fetch payments', 'error');
    } finally {
      setLoading(false);
    }
  };

  const fetchCustomers = async () => {
    try {
      const res = await customerApi.list({ size: 100 });
      if (res.data) {
        setCustomers(res.data.content || []);
        if (res.data.content?.length > 0) setCustomerId(res.data.content[0].id);
      }
    } catch (e) {
      // Ignore
    }
  };

  useEffect(() => {
    fetchPayments();
    fetchCustomers();
  }, [page, method]);

  const handleRecordPayment = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await paymentApi.create({
        customerId,
        amount: parseFloat(amount),
        paymentMethod,
        referenceNumber,
        notes,
      });
      showToast('Payment recorded and allocated FIFO across open invoices!', 'success');
      setCreateModalOpen(false);
      setAmount('');
      setReferenceNumber('');
      setNotes('');
      fetchPayments();
    } catch (err) {
      showToast(err.message || 'Failed to record payment', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRefund = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await paymentApi.refund(selectedPaymentId, refundReason);
      showToast('Payment refunded successfully', 'success');
      setRefundModalOpen(false);
      fetchPayments();
    } catch (err) {
      showToast(err.message || 'Failed to refund payment', 'error');
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
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-black tracking-tight">Payments & Receipts</h1>
          <p className="text-sm text-slate-600 mt-1">
            Collections, multi-invoice allocations, and payment reconciliation ({totalElements} total).
          </p>
        </div>
        <button
          onClick={() => setCreateModalOpen(true)}
          className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Record New Payment</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col md:flex-row gap-3 items-center justify-between">
        <div className="relative flex-1 w-full md:max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                setPage(0);
                fetchPayments();
              }
            }}
            placeholder="Search by payment #, customer, reference..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-50 border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67]"
          />
        </div>

        <div className="flex items-center space-x-2 w-full md:w-auto">
          <Filter className="w-4 h-4 text-slate-500" />
          <select
            value={method}
            onChange={(e) => {
              setMethod(e.target.value);
              setPage(0);
            }}
            className="px-3 py-1.5 bg-white border border-slate-300 rounded-lg text-xs font-semibold text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
          >
            <option value="">All Payment Methods</option>
            <option value="BANK_TRANSFER">Bank Transfer</option>
            <option value="UPI">UPI</option>
            <option value="CREDIT_CARD">Credit Card</option>
            <option value="CHEQUE">Cheque</option>
            <option value="CASH">Cash</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs uppercase font-bold text-slate-500 tracking-wider">
              <tr>
                <th className="py-3 px-4">Payment #</th>
                <th className="py-3 px-4">Customer</th>
                <th className="py-3 px-4">Date</th>
                <th className="py-3 px-4">Method & Ref</th>
                <th className="py-3 px-4 text-right">Amount</th>
                <th className="py-3 px-4">Allocated Invoices</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="8" className="py-8 text-center text-slate-500">
                    Loading payments...
                  </td>
                </tr>
              ) : payments.length === 0 ? (
                <tr>
                  <td colSpan="8" className="py-8 text-center text-slate-500">
                    No payment records found.
                  </td>
                </tr>
              ) : (
                payments.map((p) => (
                  <tr key={p.id} className="hover:bg-slate-50 transition-colors">
                    <td className="py-3.5 px-4 font-bold text-[#714B67]">{p.paymentNumber}</td>
                    <td className="py-3.5 px-4 font-semibold text-black">{p.customerName}</td>
                    <td className="py-3.5 px-4 text-slate-600">
                      {p.paymentDate ? new Date(p.paymentDate).toLocaleDateString() : '-'}
                    </td>
                    <td className="py-3.5 px-4">
                      <p className="font-semibold text-black">{p.paymentMethod.replace(/_/g, ' ')}</p>
                      {p.referenceNumber && (
                        <p className="text-[11px] font-mono text-slate-500">{p.referenceNumber}</p>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-right font-bold text-emerald-700">
                      {formatCurrency(p.amount)}
                    </td>
                    <td className="py-3.5 px-4">
                      {p.allocations && p.allocations.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {p.allocations.map((a) => (
                            <span
                              key={a.id}
                              className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 text-slate-800 border border-slate-300"
                            >
                              {a.invoiceNumber} ({formatCurrency(a.allocatedAmount)})
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-xs text-slate-400">Unallocated balance</span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <StatusBadge status={p.status} />
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      {p.status === 'COMPLETED' && (
                        <button
                          onClick={() => {
                            setSelectedPaymentId(p.id);
                            setRefundModalOpen(true);
                          }}
                          className="p-1.5 rounded bg-rose-50 hover:bg-rose-100 text-rose-700 transition-colors"
                          title="Refund"
                        >
                          <RotateCcw className="w-3.5 h-3.5" />
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-between px-6 py-3 border-t border-slate-200 bg-slate-50/50 text-xs text-slate-600">
            <span>
              Page {page + 1} of {totalPages} ({totalElements} items)
            </span>
            <div className="flex items-center space-x-1">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="p-1.5 rounded border border-slate-300 bg-white hover:bg-slate-100 disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                className="p-1.5 rounded border border-slate-300 bg-white hover:bg-slate-100 disabled:opacity-40"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Record Payment Modal */}
      <Modal
        isOpen={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="Record Received Payment"
        maxWidth="max-w-md"
      >
        <form onSubmit={handleRecordPayment} className="space-y-4">
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
            <label className="block text-xs font-bold text-black uppercase mb-1">Payment Amount (₹)</label>
            <input
              type="number"
              step="0.01"
              required
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black font-bold focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
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
            <label className="block text-xs font-bold text-black uppercase mb-1">Reference / UTR</label>
            <input
              type="text"
              placeholder="UTR12345678"
              value={referenceNumber}
              onChange={(e) => setReferenceNumber(e.target.value)}
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Notes</label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-xs text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setCreateModalOpen(false)}
              className="px-3.5 py-2 text-xs font-semibold rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-xs font-semibold rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs disabled:opacity-50"
            >
              {submitting ? 'Recording...' : 'Record & Allocate'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Refund Modal */}
      <Modal
        isOpen={refundModalOpen}
        onClose={() => setRefundModalOpen(false)}
        title="Refund Payment"
        maxWidth="max-w-md"
      >
        <form onSubmit={handleRefund} className="space-y-4">
          <p className="text-sm text-slate-700">
            Are you sure you want to refund this payment? Associated invoice allocations will be automatically reversed.
          </p>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Refund Reason</label>
            <textarea
              required
              rows={3}
              value={refundReason}
              onChange={(e) => setRefundReason(e.target.value)}
              placeholder="Provide reason for refund..."
              className="w-full p-2.5 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setRefundModalOpen(false)}
              className="px-3.5 py-2 text-xs font-semibold rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-xs font-semibold rounded-lg bg-rose-600 hover:bg-rose-700 text-white shadow-xs disabled:opacity-50"
            >
              {submitting ? 'Processing...' : 'Confirm Refund'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default PaymentList;
