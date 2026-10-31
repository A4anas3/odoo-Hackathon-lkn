import React, { useState, useEffect } from 'react';
import { Plus, Search, Package, Layers, Tag } from 'lucide-react';
import { productApi } from '../../api/client';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    internalReference: '',
    description: '',
    salePrice: '',
    costPrice: '',
    taxRate: '0.18',
    isRecurring: true,
  });
  const [submitting, setSubmitting] = useState(false);
  const { showToast } = useToast();

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await productApi.list({ search: search || undefined, size: 50 });
      if (res.data) setProducts(res.data.content || []);
    } catch (err) {
      showToast(err.message || 'Failed to fetch products', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await productApi.create({
        name: formData.name,
        internalReference: formData.internalReference,
        description: formData.description,
        salePrice: parseFloat(formData.salePrice) || 0,
        costPrice: parseFloat(formData.costPrice) || 0,
        taxRate: parseFloat(formData.taxRate) || 0.18,
        isRecurring: formData.isRecurring,
      });
      showToast('Product created successfully!', 'success');
      setModalOpen(false);
      setFormData({
        name: '',
        internalReference: '',
        description: '',
        salePrice: '',
        costPrice: '',
        taxRate: '0.18',
        isRecurring: true,
      });
      fetchProducts();
    } catch (err) {
      showToast(err.message || 'Failed to create product', 'error');
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
          <h1 className="text-2xl font-bold text-black tracking-tight">Products & Services Catalog</h1>
          <p className="text-sm text-slate-600 mt-1">
            Subscription offerings, tangible items, and variant matrices ({products.length} items).
          </p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center space-x-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>New Product</span>
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
              if (e.key === 'Enter') fetchProducts();
            }}
            placeholder="Search products by title, sku..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-50 border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67]"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {products.map((p) => (
          <div
            key={p.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
          >
            <div>
              <div className="flex items-start justify-between">
                <div>
                  <span className="text-[10px] font-bold text-[#714B67] bg-purple-50 px-2 py-0.5 rounded border border-purple-200 uppercase tracking-wider">
                    {p.internalReference || 'SKU'}
                  </span>
                  <h3 className="text-base font-bold text-black mt-2">{p.name}</h3>
                </div>
                <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center text-slate-600">
                  <Package className="w-4 h-4" />
                </div>
              </div>
              <p className="text-xs text-slate-600 mt-2 line-clamp-2">
                {p.description || 'No detailed description provided.'}
              </p>

              {p.variants && p.variants.length > 0 && (
                <div className="mt-3 pt-3 border-t border-slate-100">
                  <div className="flex items-center space-x-1.5 text-xs text-slate-600 mb-1.5 font-semibold">
                    <Layers className="w-3.5 h-3.5" />
                    <span>Variants ({p.variants.length})</span>
                  </div>
                  <div className="flex flex-wrap gap-1">
                    {p.variants.map((v) => (
                      <span
                        key={v.id}
                        className="px-2 py-0.5 rounded text-[10px] bg-slate-100 text-slate-800 font-bold border border-slate-200"
                      >
                        {v.attributeValues ? Object.values(v.attributeValues).join('/') : v.sku}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
              <div>
                <span className="text-[10px] text-slate-500 uppercase font-semibold">Sale Price</span>
                <p className="text-lg font-bold text-black">{formatCurrency(p.salePrice)}</p>
              </div>
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-emerald-50 text-emerald-900 border border-emerald-300">
                GST: {(Number(p.taxRate || 0) * 100).toFixed(0)}%
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Create New Product" maxWidth="max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Product Name</label>
            <input
              type="text"
              required
              placeholder="Cloud Storage 100GB Addon"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">SKU / Internal Reference</label>
            <input
              type="text"
              placeholder="ADDON-STORAGE-100"
              value={formData.internalReference}
              onChange={(e) => setFormData({ ...formData, internalReference: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black font-mono"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Sale Price (₹)</label>
              <input
                type="number"
                step="0.01"
                required
                value={formData.salePrice}
                onChange={(e) => setFormData({ ...formData, salePrice: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black font-bold"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-black uppercase mb-1">Cost Price (₹)</label>
              <input
                type="number"
                step="0.01"
                value={formData.costPrice}
                onChange={(e) => setFormData({ ...formData, costPrice: e.target.value })}
                className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">GST Tax Rate</label>
            <select
              value={formData.taxRate}
              onChange={(e) => setFormData({ ...formData, taxRate: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
            >
              <option value="0.18">18% (Standard GST)</option>
              <option value="0.12">12%</option>
              <option value="0.05">5%</option>
              <option value="0.00">0% (Exempt)</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase mb-1">Description</label>
            <textarea
              rows={2}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full p-2 bg-white border border-slate-300 rounded text-xs text-black"
            />
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
              {submitting ? 'Saving...' : 'Save Product'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ProductList;
