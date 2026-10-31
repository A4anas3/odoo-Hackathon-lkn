import React, { useState, useEffect } from 'react';
import { ShieldCheck, Search, Filter, Clock } from 'lucide-react';
import { auditApi } from '../../api/client';
import { useToast } from '../../context/ToastContext';

export const AuditLogs = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [entityType, setEntityType] = useState('');
  const [action, setAction] = useState('');
  const { showToast } = useToast();

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const res = await auditApi.list({
        entityType: entityType || undefined,
        action: action || undefined,
        size: 50,
      });
      if (res.data) setLogs(res.data.content || []);
    } catch (err) {
      showToast(err.message || 'Failed to fetch audit logs', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [entityType, action]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-black tracking-tight">System Audit Trail</h1>
          <p className="text-sm text-slate-600 mt-1">
            Immutable audit log capturing user operations, security events, and entity lifecycle changes.
          </p>
        </div>
      </div>

      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-wrap gap-3">
        <select
          value={entityType}
          onChange={(e) => setEntityType(e.target.value)}
          className="px-3 py-1.5 bg-white border border-slate-300 rounded-lg text-xs font-semibold text-black"
        >
          <option value="">All Entity Types</option>
          <option value="SUBSCRIPTION">Subscription</option>
          <option value="INVOICE">Invoice</option>
          <option value="PAYMENT">Payment</option>
          <option value="QUOTATION">Quotation</option>
          <option value="CUSTOMER">Customer</option>
        </select>

        <select
          value={action}
          onChange={(e) => setAction(e.target.value)}
          className="px-3 py-1.5 bg-white border border-slate-300 rounded-lg text-xs font-semibold text-black"
        >
          <option value="">All Actions</option>
          <option value="CREATE">CREATE</option>
          <option value="UPDATE">UPDATE</option>
          <option value="STATUS_CHANGE">STATUS CHANGE</option>
          <option value="DELETE">DELETE</option>
        </select>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs uppercase font-bold text-slate-500 tracking-wider">
              <tr>
                <th className="py-3 px-4">Timestamp</th>
                <th className="py-3 px-4">Entity Type</th>
                <th className="py-3 px-4">Action</th>
                <th className="py-3 px-4">User</th>
                <th className="py-3 px-4">IP Address</th>
                <th className="py-3 px-4">Entity ID</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-slate-500">
                    Loading audit trail...
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-slate-500">
                    No audit records matching filter criteria.
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50">
                    <td className="py-3.5 px-4 font-mono text-xs text-slate-600">
                      {new Date(log.createdAt).toLocaleString()}
                    </td>
                    <td className="py-3.5 px-4 font-bold text-black">{log.entityType}</td>
                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-slate-100 text-slate-800 border border-slate-200">
                        {log.action}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-medium text-black">{log.userName || 'System Auto'}</td>
                    <td className="py-3.5 px-4 font-mono text-xs text-slate-500">{log.ipAddress || '-'}</td>
                    <td className="py-3.5 px-4 font-mono text-[11px] text-slate-400">{log.entityId}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AuditLogs;
