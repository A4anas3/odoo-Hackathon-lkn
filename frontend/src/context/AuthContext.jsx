import React, { createContext, useContext, useState, useEffect, useMemo } from 'react';
import { authApi } from '../api/client';
import { useToast } from './ToastContext';

const AuthContext = createContext(null);

const PERMISSION_ROLE_MAP = {
  MANAGE_BILLING: ['ADMIN'],
  VIEW_AUDITS: ['ADMIN'],
  MANAGE_TAXES: ['ADMIN'],
  MANAGE_DISCOUNTS: ['ADMIN'],
  MANAGE_CUSTOMERS: ['ADMIN', 'STAFF'],
  MANAGE_PRODUCTS: ['ADMIN', 'STAFF'],
  MANAGE_PLANS: ['ADMIN', 'STAFF'],
  VIEW_REPORTS: ['ADMIN', 'STAFF'],
  MANAGE_SUBSCRIPTIONS: ['ADMIN', 'STAFF'],
  VIEW_SUBSCRIPTIONS: ['ADMIN', 'STAFF', 'CUSTOMER'],
  VIEW_INVOICES: ['ADMIN', 'STAFF', 'CUSTOMER'],
  VIEW_QUOTATIONS: ['ADMIN', 'STAFF', 'CUSTOMER'],
  VIEW_PAYMENTS: ['ADMIN', 'STAFF', 'CUSTOMER'],
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(true);
  const { showToast } = useToast();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      authApi.getCurrentUser()
        .then((res) => {
          if (res.data) {
            setUser(res.data);
            localStorage.setItem('user', JSON.stringify(res.data));
          }
        })
        .catch(() => {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          setUser(null);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (email, password) => {
    try {
      const res = await authApi.login({ email, password });
      const token = res.data?.accessToken || res.data?.token;
      if (res.data && token) {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(res.data.user));
        setUser(res.data.user);
        showToast('Welcome back, ' + (res.data.user?.fullName || res.data.user?.email || 'User'), 'success');
        return true;
      }
      return false;
    } catch (err) {
      showToast(err.message || 'Login failed', 'error');
      throw err;
    }
  };

  const register = async (formData) => {
    try {
      const res = await authApi.register(formData);
      const token = res.data?.accessToken || res.data?.token;
      if (res.data && token) {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(res.data.user));
        setUser(res.data.user);
        showToast('Registration successful! Welcome to Brightloop ERP.', 'success');
        return true;
      }
      return false;
    } catch (err) {
      showToast(err.message || 'Registration failed', 'error');
      throw err;
    }
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (e) {
      // Ignore
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setUser(null);
      showToast('Logged out successfully', 'info');
    }
  };

  // Normalized current role (e.g. 'ADMIN', 'STAFF', 'CUSTOMER')
  const role = useMemo(() => {
    if (!user) return 'ANONYMOUS';
    const raw = user.role || (Array.isArray(user.roles) ? user.roles[0] : null) || 'CUSTOMER';
    return String(raw).toUpperCase().replace('ROLE_', '');
  }, [user]);

  const isAdmin = role === 'ADMIN';
  const isStaff = role === 'STAFF';
  const isCustomer = role === 'CUSTOMER';

  /**
   * Checks whether the current user has at least one of the specified roles.
   * @param {string|string[]} allowedRoles
   */
  const hasRole = (allowedRoles) => {
    if (!allowedRoles) return true;
    const list = Array.isArray(allowedRoles) ? allowedRoles : [allowedRoles];
    const normalized = list.map((r) => String(r).toUpperCase().replace('ROLE_', ''));
    return normalized.includes(role);
  };

  /**
   * Checks whether the current user has a specific named permission.
   * @param {string|string[]} permissions
   */
  const hasPermission = (permissions) => {
    if (!permissions) return true;
    const perms = Array.isArray(permissions) ? permissions : [permissions];
    return perms.some((p) => {
      const allowedRoles = PERMISSION_ROLE_MAP[p];
      return allowedRoles ? hasRole(allowedRoles) : false;
    });
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        role,
        loading,
        login,
        register,
        logout,
        isAdmin,
        isStaff,
        isCustomer,
        hasRole,
        hasPermission,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
