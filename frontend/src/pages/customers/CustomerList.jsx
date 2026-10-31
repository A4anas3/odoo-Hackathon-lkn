import React, { useState, useEffect } from 'react';
import { Plus, Search, Users, ChevronLeft, ChevronRight, Building, Mail, Phone, MapPin } from 'lucide-react';
import { customerApi } from '../../api/client';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export const CustomerList = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const { showToast } = useToast();

  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    companyName: '',
    contactName: '',
    email: '',
    phone: '',
    billingAddress: '',
    city: '',
    state: 'Uttar Pradesh',
    country: 'India',
    postalCode: '',
    gstin: '',
    customerType: 'BUSINESS',
  });
  const [submitting, setSubmitting] = useState(false);

  const fetchCustomers = async () => {
    setLoading(true);
    try {
      const res = await customerApi.list({
        search: search || undefined,
        page,
        size: 15,
      });
      if (res.data) {
        setCustomers(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setTotalElements(res.data.totalElements || 0);
      }
    } catch (err) {
      showToast(err.message || 'Failed to fetch customers', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCustomers();
  }, [page]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await customerApi.create(formData);
      showToast('Customer created successfully!', 'success');
      setModalOpen(false);
      setFormData({
        companyName: '',
        contactName: '',
        email: '',
        phone: '',
        billingAddress: '',
        city: '',
        state: 'Uttar Pradesh',
        country: 'India',
        postalCode: '',
        gstin: '',
        customerType: 'BUSINESS',
      });
      fetchCustomers();
    } catch (err) {
      showToast(err.message || 'Failed to create customer', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-black tracking-tight">Enterprise Customers</h1>
          <p className="text-sm text-slate-600 mt-1">
            Accounts, billing profiles, and GST tax registrations ({totalElements} total).
          </p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>New Customer</span>
        </button>
      </div>

      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
        <div className="relative max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                setPage(0);
                fetchCustomers();
              }
            }}
            placeholder="Search by company, contact, or email..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-50 border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67]"
          />
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs uppercase font-bold text-slate-500 tracking-wider">
              <tr>
                <th className="py-3 px-4">Customer / Organization</th>
                <th className="py-3 px-4">Contact Person</th>
                <th className="py-3 px-4">Email & Phone</th>
                <th className="py-3 px-4">Location</th>
                <th className="py-3 px-4">GSTIN</th>
                <th className="py-3 px-4 text-center">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-slate-500">
                    Loading customer directory...
                  </td>
                </tr>
              ) : customers.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-slate-500">
                    No customers found.
                  </td>
                </tr>
              ) : (
                customers.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50 transition-colors">
                    <td className="py-3.5 px-4 font-bold text-black">
                      <div className="flex items-center space-x-2">
                        <Building className="w-4 h-4 text-[#714B67]" />
                        <span>{c.companyName || c.contactName}</span>
                      </div>
                      <span className="text-[11px] text-slate-500 font-normal block pl-6">
                        Code: {c.customerCode}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-medium text-slate-800">{c.contactName}</td>
                    <td className="py-3.5 px-4">
                      <p className="text-black font-semibold">{c.email}</p>
                      {c.phone && <p className="text-xs text-slate-500">{c.phone}</p>}
                    </td>
                    <td className="py-3.5 px-4 text-xs text-slate-700">
                      {c.city ? `${c.city}, ${c.state || ''}` : c.state || 'India'}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-xs font-bold text-indigo-700">
                      {c.gstin || '-'}
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <span className="inline-block px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-950 border border-emerald-300">
                        {c.status}
                      </span>
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

      {/* Create Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Create New Enterprise Customer"
        maxWidth="max-w-xl"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Company Name</label>
              <input
                type="text"
                placeholder="Acme Technologies Ltd"
                value={formData.companyName}
                onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Contact Person</label>
              <input
                type="text"
                required
                placeholder="Jane Smith"
                value={formData.contactName}
                onChange={(e) => setFormData({ ...formData, contactName: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Email</label>
              <input
                type="email"
                required
                placeholder="billing@acme.com"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Phone</label>
              <input
                type="tel"
                placeholder="+91 98765 43210"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div className="sm:col-span-2">
              <label className="block text-xs font-bold text-black uppercase mb-1">Billing Address</label>
              <input
                type="text"
                placeholder="Floor 4, Cyber Tower, Sector 62"
                value={formData.billingAddress}
                onChange={(e) => setFormData({ ...formData, billingAddress: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">City</label>
              <input
                type="text"
                placeholder="Lucknow"
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">State</label>
              <input
                type="text"
                placeholder="Uttar Pradesh"
                value={formData.state}
                onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">GSTIN</label>
              <input
                type="text"
                placeholder="09AAACB1234F1Z8"
                value={formData.gstin}
                onChange={(e) => setFormData({ ...formData, gstin: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black uppercase font-mono"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Customer Type</label>
              <select
                value={formData.customerType}
                onChange={(e) => setFormData({ ...formData, customerType: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              >
                <option value="BUSINESS">Business Enterprise</option>
                <option value="INDIVIDUAL">Individual Consumer</option>
              </select>
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-slate-200">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-3 py-1.5 text-xs font-semibold rounded border border-slate-300 text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-1.5 text-xs font-semibold rounded bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs disabled:opacity-50"
            >
              {submitting ? 'Saving...' : 'Save Customer'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default CustomerList;
