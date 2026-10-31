import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import AppLayout from './components/layout/AppLayout';

// Pages
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import Dashboard from './pages/dashboard/Dashboard';
import SubscriptionList from './pages/subscriptions/SubscriptionList';
import SubscriptionKanban from './pages/subscriptions/SubscriptionKanban';
import SubscriptionDetail from './pages/subscriptions/SubscriptionDetail';
import SubscriptionForm from './pages/subscriptions/SubscriptionForm';
import QuotationList from './pages/quotations/QuotationList';
import QuotationDetail from './pages/quotations/QuotationDetail';
import QuotationForm from './pages/quotations/QuotationForm';
import InvoiceList from './pages/invoices/InvoiceList';
import InvoiceDetail from './pages/invoices/InvoiceDetail';
import InvoiceForm from './pages/invoices/InvoiceForm';
import PaymentList from './pages/payments/PaymentList';
import CustomerList from './pages/customers/CustomerList';
import ProductList from './pages/products/ProductList';
import PlanList from './pages/plans/PlanList';
import DiscountList from './pages/discounts/DiscountList';
import TaxList from './pages/taxes/TaxList';
import Reports from './pages/reports/Reports';
import BillingRecords from './pages/billing/BillingRecords';
import AuditLogs from './pages/audit/AuditLogs';

// Role & Permission Protected Route
const ProtectedRoute = ({ children, roles }) => {
  const { user, loading, hasRole } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-[#714B67] border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Permission-based route blocking: if user does not possess required role, redirect to home
  if (roles && !hasRole(roles)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export const App = () => {
  return (
    <ToastProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* Public Auth Routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* Protected Enterprise ERP Suite */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <AppLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Dashboard />} />

              {/* Subscriptions (Admin, Staff, Customer) */}
              <Route path="subscriptions" element={<SubscriptionList />} />
              <Route path="subscriptions/kanban" element={<SubscriptionKanban />} />
              <Route
                path="subscriptions/new"
                element={
                  <ProtectedRoute roles={['ADMIN', 'STAFF']}>
                    <SubscriptionForm />
                  </ProtectedRoute>
                }
              />
              <Route path="subscriptions/:id" element={<SubscriptionDetail />} />

              {/* Quotations (Admin, Staff, Customer) */}
              <Route path="quotations" element={<QuotationList />} />
              <Route
                path="quotations/new"
                element={
                  <ProtectedRoute roles={['ADMIN', 'STAFF']}>
                    <QuotationForm />
                  </ProtectedRoute>
                }
              />
              <Route path="quotations/:id" element={<QuotationDetail />} />

              {/* Invoices (Admin, Staff, Customer) */}
              <Route path="invoices" element={<InvoiceList />} />
              <Route
                path="invoices/new"
                element={
                  <ProtectedRoute roles={['ADMIN', 'STAFF']}>
                    <InvoiceForm />
                  </ProtectedRoute>
                }
              />
              <Route path="invoices/:id" element={<InvoiceDetail />} />

              {/* Payments (Admin, Staff, Customer) */}
              <Route path="payments" element={<PaymentList />} />

              {/* CRM & Masters (Admin & Staff Only) */}
              <Route
                path="customers"
                element={
                  <ProtectedRoute roles={['ADMIN', 'STAFF']}>
                    <CustomerList />
                  </ProtectedRoute>
                }
              />
              <Route
                path="products"
                element={
                  <ProtectedRoute roles={['ADMIN', 'STAFF']}>
                    <ProductList />
                  </ProtectedRoute>
                }
              />
              <Route
                path="plans"
                element={
                  <ProtectedRoute roles={['ADMIN', 'STAFF']}>
                    <PlanList />
                  </ProtectedRoute>
                }
              />

              {/* Analytics & Reports (Admin & Staff Only) */}
              <Route
                path="reports"
                element={
                  <ProtectedRoute roles={['ADMIN', 'STAFF']}>
                    <Reports />
                  </ProtectedRoute>
                }
              />

              {/* Pricing Rules & Discounts (Admin Only) */}
              <Route
                path="discounts"
                element={
                  <ProtectedRoute roles={['ADMIN']}>
                    <DiscountList />
                  </ProtectedRoute>
                }
              />
              <Route
                path="taxes"
                element={
                  <ProtectedRoute roles={['ADMIN']}>
                    <TaxList />
                  </ProtectedRoute>
                }
              />

              {/* Automated Engine & Security Audit (Admin Only) */}
              <Route
                path="billing"
                element={
                  <ProtectedRoute roles={['ADMIN']}>
                    <BillingRecords />
                  </ProtectedRoute>
                }
              />
              <Route
                path="audit-logs"
                element={
                  <ProtectedRoute roles={['ADMIN']}>
                    <AuditLogs />
                  </ProtectedRoute>
                }
              />
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ToastProvider>
  );
};

export default App;
