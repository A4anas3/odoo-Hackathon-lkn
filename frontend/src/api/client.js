import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle unauthenticated 401s
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401 && !window.location.pathname.includes('/login')) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred';
    return Promise.reject(new Error(message));
  }
);

export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (data) => api.post('/auth/register', data),
  getCurrentUser: () => api.get('/auth/me'),
  logout: () => api.post('/auth/logout'),
};

export const customerApi = {
  list: (params) => api.get('/customers', { params }),
  get: (id) => api.get(`/customers/${id}`),
  create: (data) => api.post('/customers', data),
  update: (id, data) => api.put(`/customers/${id}`, data),
  delete: (id) => api.delete(`/customers/${id}`),
};

export const productApi = {
  list: (params) => api.get('/products', { params }),
  get: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`),
};

export const planApi = {
  list: (params) => api.get('/plans', { params }),
  get: (id) => api.get(`/plans/${id}`),
  create: (data) => api.post('/plans', data),
  update: (id, data) => api.put(`/plans/${id}`, data),
  delete: (id) => api.delete(`/plans/${id}`),
};

export const quotationApi = {
  list: (params) => api.get('/quotations', { params }),
  get: (id) => api.get(`/quotations/${id}`),
  create: (data) => api.post('/quotations', data),
  update: (id, data) => api.put(`/quotations/${id}`, data),
  updateStatus: (id, status) => api.patch(`/quotations/${id}/status`, { status }),
  delete: (id) => api.delete(`/quotations/${id}`),
};

export const quotationTemplateApi = {
  list: (params) => api.get('/quotation-templates', { params }),
  getActive: () => api.get('/quotation-templates/active'),
  get: (id) => api.get(`/quotation-templates/${id}`),
  create: (data) => api.post('/quotation-templates', data),
  update: (id, data) => api.put(`/quotation-templates/${id}`, data),
  delete: (id) => api.delete(`/quotation-templates/${id}`),
};

export const subscriptionApi = {
  list: (params) => api.get('/subscriptions', { params }),
  get: (id) => api.get(`/subscriptions/${id}`),
  create: (data) => api.post('/subscriptions', data),
  createFromQuotation: (quotationId) => api.post(`/subscriptions/from-quotation/${quotationId}`),
  changeStatus: (id, status, reason) => api.post(`/subscriptions/${id}/status`, { status, reason }),
  changePlan: (id, newPlanId, reason) => api.post(`/subscriptions/${id}/change-plan`, { newPlanId, reason }),
  renew: (id, intervals = 1) => api.post(`/subscriptions/${id}/renew?intervals=${intervals}`),
  delete: (id) => api.delete(`/subscriptions/${id}`),
};

export const invoiceApi = {
  list: (params) => api.get('/invoices', { params }),
  get: (id) => api.get(`/invoices/${id}`),
  create: (data) => api.post('/invoices', data),
  update: (id, data) => api.put(`/invoices/${id}`, data),
  post: (id) => api.post(`/invoices/${id}/post`),
  cancel: (id) => api.post(`/invoices/${id}/cancel`),
  downloadPdf: (id) => {
    return axios.get(`/api/v1/invoices/${id}/pdf`, {
      responseType: 'blob',
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
    });
  },
  delete: (id) => api.delete(`/invoices/${id}`),
};

export const paymentApi = {
  list: (params) => api.get('/payments', { params }),
  get: (id) => api.get(`/payments/${id}`),
  create: (data) => api.post('/payments', data),
  refund: (id, reason) => api.post(`/payments/${id}/refund`, { reason }),
};

export const discountApi = {
  list: (params) => api.get('/discounts', { params }),
  get: (id) => api.get(`/discounts/${id}`),
  getByCode: (code) => api.get(`/discounts/code/${code}`),
  validate: (code, amount, customerId) => api.get('/discounts/validate', { params: { code, amount, customerId } }),
  create: (data) => api.post('/discounts', data),
  update: (id, data) => api.put(`/discounts/${id}`, data),
  delete: (id) => api.delete(`/discounts/${id}`),
};

export const taxApi = {
  list: (params) => api.get('/taxes', { params }),
  getActive: () => api.get('/taxes/active'),
  calculate: (amount, customerState) => api.get('/taxes/calculate', { params: { amount, customerState } }),
  get: (id) => api.get(`/taxes/${id}`),
  create: (data) => api.post('/taxes', data),
  update: (id, data) => api.put(`/taxes/${id}`, data),
  delete: (id) => api.delete(`/taxes/${id}`),
};

export const reportApi = {
  getDashboardMetrics: () => api.get('/reports/dashboard'),
  exportSubscriptions: () => axios.get('/api/v1/reports/export/subscriptions', {
    responseType: 'blob',
    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
  }),
  exportInvoices: () => axios.get('/api/v1/reports/export/invoices', {
    responseType: 'blob',
    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
  }),
  exportPayments: () => axios.get('/api/v1/reports/export/payments', {
    responseType: 'blob',
    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
  }),
};

export const billingApi = {
  run: (date) => api.post('/billing/run', null, { params: { date } }),
  records: (params) => api.get('/billing/records', { params }),
};

export const auditApi = {
  list: (params) => api.get('/audit-logs', { params }),
};

export default api;
