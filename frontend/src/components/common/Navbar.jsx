import React, { useState } from 'react';
import { Menu, RefreshCw, Shield, Sparkles } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { billingApi } from '../../api/client';
import { useToast } from '../../context/ToastContext';

export const Navbar = ({ onOpenSidebar }) => {
  const { user, isAdmin } = useAuth();
  const { showToast } = useToast();
  const [runningBilling, setRunningBilling] = useState(false);

  const handleRunBilling = async () => {
    setRunningBilling(true);
    try {
      const res = await billingApi.run();
      const count = res.data?.invoicesGenerated || 0;
      showToast(`Billing cycle completed: ${count} recurring invoice(s) generated.`, 'success');
    } catch (err) {
      showToast(err.message || 'Billing cycle trigger failed', 'error');
    } finally {
      setRunningBilling(false);
    }
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200 sticky top-0 z-30 flex items-center justify-between px-4 sm:px-6 lg:px-8 shadow-xs">
      {/* Left: Mobile Toggle & Page Title */}
      <div className="flex items-center space-x-3">
        <button
          onClick={onOpenSidebar}
          className="p-2 rounded-lg text-slate-600 hover:text-black hover:bg-slate-100 lg:hidden"
        >
          <Menu className="w-5 h-5" />
        </button>
        <div>
          <span className="text-xs font-semibold text-[#714B67] uppercase tracking-wider hidden sm:inline-block">
            Brightloop Operations
          </span>
          <h2 className="text-base sm:text-lg font-bold text-black tracking-tight leading-none sm:leading-tight">
            Subscription Management Platform
          </h2>
        </div>
      </div>

      {/* Right: Quick Actions & Profile */}
      <div className="flex items-center space-x-2 sm:space-x-4">
        {isAdmin && (
          <button
            onClick={handleRunBilling}
            disabled={runningBilling}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-all disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${runningBilling ? 'animate-spin' : ''}`} />
            <span className="hidden sm:inline">Run Billing Cycle</span>
          </button>
        )}

        <div className="flex items-center space-x-2 pl-2 sm:pl-3 border-l border-slate-200">
          <div className="hidden sm:flex flex-col text-right">
            <span className="text-xs font-bold text-black">{user?.fullName || 'User'}</span>
            <span className="text-[10px] text-slate-500 font-medium">
              {isAdmin ? 'System Administrator' : 'Staff Member'}
            </span>
          </div>
          <span
            className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${
              isAdmin
                ? 'bg-purple-50 text-[#714B67] border-purple-300'
                : 'bg-slate-100 text-slate-700 border-slate-300'
            }`}
          >
            <Shield className="w-3 h-3 mr-1" />
            {isAdmin ? 'Admin' : 'Staff'}
          </span>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
