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

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
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

              {/* Subscriptions */}
              <Route path="subscriptions" element={<SubscriptionList />} />
              <Route path="subscriptions/kanban" element={<SubscriptionKanban />} />
              <Route path="subscriptions/new" element={<SubscriptionForm />} />
              <Route path="subscriptions/:id" element={<SubscriptionDetail />} />

              {/* Quotations */}
              <Route path="quotations" element={<QuotationList />} />
              <Route path="quotations/new" element={<QuotationForm />} />
              <Route path="quotations/:id" element={<QuotationDetail />} />

              {/* Invoices */}
              <Route path="invoices" element={<InvoiceList />} />
              <Route path="invoices/new" element={<InvoiceForm />} />
              <Route path="invoices/:id" element={<InvoiceDetail />} />

              {/* Payments */}
              <Route path="payments" element={<PaymentList />} />

              {/* Masters */}
              <Route path="customers" element={<CustomerList />} />
              <Route path="products" element={<ProductList />} />
              <Route path="plans" element={<PlanList />} />
              <Route path="discounts" element={<DiscountList />} />
              <Route path="taxes" element={<TaxList />} />

              {/* Reports & Operations */}
              <Route path="reports" element={<Reports />} />
              <Route path="billing" element={<BillingRecords />} />
              <Route path="audit-logs" element={<AuditLogs />} />
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
