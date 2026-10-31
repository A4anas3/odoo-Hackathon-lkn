import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Repeat,
  FileText,
  Receipt,
  CreditCard,
  Users,
  Package,
  Calendar,
  Tag,
  Percent,
  BarChart3,
  RefreshCw,
  ShieldCheck,
  LogOut,
  X,
  Shield,
  UserCheck,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const navigation = [
  // Core Modules (Accessible to all authenticated users)
  { name: 'Dashboard', href: '/', icon: LayoutDashboard, roles: ['ADMIN', 'STAFF', 'CUSTOMER'], section: 'Overview' },
  { name: 'Subscriptions', href: '/subscriptions', icon: Repeat, roles: ['ADMIN', 'STAFF', 'CUSTOMER'], section: 'Recurring Billing' },
  { name: 'Quotations', href: '/quotations', icon: FileText, roles: ['ADMIN', 'STAFF', 'CUSTOMER'], section: 'Recurring Billing' },
  { name: 'Invoices', href: '/invoices', icon: Receipt, roles: ['ADMIN', 'STAFF', 'CUSTOMER'], section: 'Recurring Billing' },
  { name: 'Payments', href: '/payments', icon: CreditCard, roles: ['ADMIN', 'STAFF', 'CUSTOMER'], section: 'Recurring Billing' },

  // Management Master Data (Admin & Staff only)
  { name: 'Customers', href: '/customers', icon: Users, roles: ['ADMIN', 'STAFF'], section: 'Master Data' },
  { name: 'Products', href: '/products', icon: Package, roles: ['ADMIN', 'STAFF'], section: 'Master Data' },
  { name: 'Plans', href: '/plans', icon: Calendar, roles: ['ADMIN', 'STAFF'], section: 'Master Data' },
  { name: 'Reports', href: '/reports', icon: BarChart3, roles: ['ADMIN', 'STAFF'], section: 'Analytics' },

  // Administrative Engine (Admin only)
  { name: 'Discounts', href: '/discounts', icon: Tag, roles: ['ADMIN'], section: 'Settings' },
  { name: 'Taxes', href: '/taxes', icon: Percent, roles: ['ADMIN'], section: 'Settings' },
  { name: 'Billing Runs', href: '/billing', icon: RefreshCw, roles: ['ADMIN'], section: 'Engine' },
  { name: 'Audit Logs', href: '/audit-logs', icon: ShieldCheck, roles: ['ADMIN'], section: 'Engine' },
];

export const Sidebar = ({ isOpen, onClose }) => {
  const { user, role, hasRole, logout } = useAuth();

  // Filter navigation items permission-wise
  const visibleNav = navigation.filter((item) => hasRole(item.roles));

  // Determine role display properties
  const getRoleBadge = () => {
    switch (role) {
      case 'ADMIN':
        return { label: 'Administrator', bg: 'bg-purple-100 text-[#714B67] border-purple-300' };
      case 'STAFF':
        return { label: 'Billing Staff', bg: 'bg-blue-100 text-blue-800 border-blue-300' };
      case 'CUSTOMER':
      default:
        return { label: 'Customer Portal', bg: 'bg-emerald-100 text-emerald-800 border-emerald-300' };
    }
  };

  const badge = getRoleBadge();

  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/40 lg:hidden backdrop-blur-xs"
          onClick={onClose}
        />
      )}

      {/* Sidebar container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-50 w-64 bg-white border-r border-slate-200 flex flex-col transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-slate-200 bg-slate-50/50">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-[#714B67] flex items-center justify-center text-white font-bold shadow-xs">
              <span className="text-lg">B</span>
            </div>
            <div>
              <span className="font-bold text-base text-black tracking-tight block">Brightloop</span>
              <span className="text-[10px] uppercase font-semibold tracking-wider text-[#714B67] block -mt-1">
                {role === 'CUSTOMER' ? 'Customer Portal' : 'ERP Subscription'}
              </span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-500 hover:text-black lg:hidden"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Links */}
        <div className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
          {visibleNav.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.name}
                to={item.href}
                onClick={() => {
                  if (window.innerWidth < 1024) onClose();
                }}
                className={({ isActive }) =>
                  `flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-purple-50 text-[#714B67] font-semibold border-l-4 border-[#714B67] shadow-xs'
                      : 'text-slate-700 hover:bg-slate-100 hover:text-black'
                  }`
                }
              >
                <Icon className="w-5 h-5 flex-shrink-0" />
                <span className="truncate">{item.name}</span>
              </NavLink>
            );
          })}
        </div>

        {/* User Card & Logout with Role Badge */}
        <div className="p-4 border-t border-slate-200 bg-slate-50/50 space-y-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3 truncate">
              <div className="w-8 h-8 rounded-full bg-[#714B67]/10 text-[#714B67] flex items-center justify-center text-xs font-bold uppercase">
                {user?.fullName ? user.fullName[0] : user?.email ? user.email[0] : 'U'}
              </div>
              <div className="truncate">
                <p className="text-xs font-bold text-black truncate">{user?.fullName || 'User'}</p>
                <p className="text-[10px] text-slate-500 truncate">{user?.email}</p>
              </div>
            </div>
            <button
              onClick={logout}
              title="Sign out"
              className="p-1.5 rounded-lg text-slate-500 hover:text-rose-700 hover:bg-rose-50 transition-colors"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
          <div className="pt-1">
            <span
              className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${badge.bg}`}
            >
              <Shield className="w-3 h-3 mr-1" />
              {badge.label}
            </span>
          </div>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
