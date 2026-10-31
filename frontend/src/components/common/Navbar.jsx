import React, { useState } from 'react';
import { Menu, RefreshCw, Shield, User } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { billingApi } from '../../api/client';
import { useToast } from '../../context/ToastContext';
import PermissionGuard from './PermissionGuard';

export const Navbar = ({ onOpenSidebar }) => {
  const { user, role, isAdmin, isCustomer } = useAuth();
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

  const getRoleDisplay = () => {
    switch (role) {
      case 'ADMIN':
        return {
          title: 'System Administrator',
          tag: 'Admin',
          badgeClass: 'bg-purple-50 text-[#714B67] border-purple-300',
        };
      case 'STAFF':
        return {
          title: 'Billing Operations Staff',
          tag: 'Staff',
          badgeClass: 'bg-blue-50 text-blue-700 border-blue-300',
        };
      case 'CUSTOMER':
      default:
        return {
          title: 'Customer Self-Service',
          tag: 'Customer',
          badgeClass: 'bg-emerald-50 text-emerald-700 border-emerald-300',
        };
    }
  };

  const roleMeta = getRoleDisplay();

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
            {isCustomer ? 'Customer Portal' : 'Brightloop Operations'}
          </span>
          <h2 className="text-base sm:text-lg font-bold text-black tracking-tight leading-none sm:leading-tight">
            {isCustomer ? 'My Account & Subscriptions' : 'Subscription Management Platform'}
          </h2>
        </div>
      </div>

      {/* Right: Quick Actions & Profile */}
      <div className="flex items-center space-x-2 sm:space-x-4">
        {/* Permission-guarded: Only Admins can manually trigger recurring billing engine */}
        <PermissionGuard roles={['ADMIN']}>
          <button
            onClick={handleRunBilling}
            disabled={runningBilling}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-[#714B67] hover:bg-[#5d3d54] text-white shadow-xs transition-all disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${runningBilling ? 'animate-spin' : ''}`} />
            <span className="hidden sm:inline">Run Billing Cycle</span>
          </button>
        </PermissionGuard>

        <div className="flex items-center space-x-2 pl-2 sm:pl-3 border-l border-slate-200">
          <div className="hidden sm:flex flex-col text-right">
            <span className="text-xs font-bold text-black">{user?.fullName || 'User'}</span>
            <span className="text-[10px] text-slate-500 font-medium">
              {roleMeta.title}
            </span>
          </div>
          <span
            className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${roleMeta.badgeClass}`}
          >
            <Shield className="w-3 h-3 mr-1" />
            {roleMeta.tag}
          </span>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
