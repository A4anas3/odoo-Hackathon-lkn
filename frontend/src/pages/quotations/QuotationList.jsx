import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Search, Filter, FileText, ChevronLeft, ChevronRight, Eye } from 'lucide-react';
import { quotationApi } from '../../api/client';
import StatusBadge from '../../components/common/StatusBadge';
import { useToast } from '../../context/ToastContext';

export const QuotationList = () => {
  const [quotations, setQuotations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const { showToast } = useToast();

  const fetchQuotations = async () => {
    setLoading(true);
    try {
      const res = await quotationApi.list({
        search: search || undefined,
        status: status || undefined,
        page,
        size: 15,
      });
      if (res.data) {
        setQuotations(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setTotalElements(res.data.totalElements || 0);
      }
    } catch (err) {
      showToast(err.message || 'Failed to fetch quotations', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQuotations();
  }, [page, status]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchQuotations();
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
          <h1 className="text-2xl font-bold text-black tracking-tight">Quotations</h1>
          <p className="text-sm text-slate-600 mt-1">
            Proposals, contract quotations, and subscription onboarding ({totalElements} total).
          </p>
        </div>
        <Link
          to="/quotations/new"
          className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>New Quotation</span>
        </Link>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col md:flex-row gap-3 items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="relative flex-1 w-full md:max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search quotation #, customer..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-50 border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67]"
          />
        </form>

        <div className="flex items-center space-x-2 w-full md:w-auto">
          <Filter className="w-4 h-4 text-slate-500" />
          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
            className="px-3 py-1.5 bg-white border border-slate-300 rounded-lg text-xs font-semibold text-black focus:outline-none focus:ring-2 focus:ring-[#714B67]"
          >
            <option value="">All Statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="SENT">Sent</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs uppercase font-bold text-slate-500 tracking-wider">
              <tr>
                <th className="py-3 px-4">Quotation #</th>
                <th className="py-3 px-4">Customer</th>
                <th className="py-3 px-4">Order Date</th>
                <th className="py-3 px-4">Valid Until</th>
                <th className="py-3 px-4 text-right">Total Amount</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="7" className="py-8 text-center text-slate-500">
                    Loading quotations...
                  </td>
                </tr>
              ) : quotations.length === 0 ? (
                <tr>
                  <td colSpan="7" className="py-8 text-center text-slate-500">
                    No quotations found.
                  </td>
                </tr>
              ) : (
                quotations.map((q) => (
                  <tr key={q.id} className="hover:bg-slate-50 transition-colors">
                    <td className="py-3.5 px-4 font-bold text-[#714B67]">
                      <Link to={`/quotations/${q.id}`} className="hover:underline">
                        {q.quotationNumber}
                      </Link>
                    </td>
                    <td className="py-3.5 px-4 font-semibold text-black">{q.customerName}</td>
                    <td className="py-3.5 px-4 text-slate-600">{q.orderDate || '-'}</td>
                    <td className="py-3.5 px-4 text-slate-600">{q.validUntil || '-'}</td>
                    <td className="py-3.5 px-4 text-right font-bold text-black">
                      {formatCurrency(q.totalAmount)}
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <StatusBadge status={q.status} />
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <Link
                        to={`/quotations/${q.id}`}
                        className="inline-flex items-center space-x-1 px-2.5 py-1 rounded bg-slate-100 hover:bg-slate-200 text-xs font-semibold text-slate-800 transition-colors"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>View</span>
                      </Link>
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
    </div>
  );
};

export default QuotationList;
