-- =============================================================================
-- BRIGHTLOOP ERP - V2__seed_data.sql
-- =============================================================================

-- 1. SEED ROLES
INSERT INTO roles (id, name, description) VALUES
('11111111-1111-1111-1111-111111111101', 'ADMIN', 'System Administrator with full access'),
('11111111-1111-1111-1111-111111111102', 'STAFF', 'Operations and Sales Staff'),
('11111111-1111-1111-1111-111111111103', 'CUSTOMER', 'Customer Portal Access')
ON CONFLICT (name) DO NOTHING;

-- 2. SEED DEFAULT USERS (Password: Admin@1234, Staff@1234, Customer@1234)
INSERT INTO users (id, full_name, email, password_hash, role_id, status, email_verified) VALUES
('22222222-2222-2222-2222-222222222201', 'System Administrator', 'admin@brightloop.local', '$2a$10$31Ib.33XcB6YcuFXc1MVauOVLbBGZK2gGqlJfgB4gU4dls76DKj32', '11111111-1111-1111-1111-111111111101', 'ACTIVE', TRUE),
('22222222-2222-2222-2222-222222222202', 'Operations Staff', 'staff@brightloop.local', '$2a$10$6hW1phVVdfesNK85MQqorONU31uNFxwSgpT2YQQit0SSutnMkJJ5m', '11111111-1111-1111-1111-111111111102', 'ACTIVE', TRUE),
('22222222-2222-2222-2222-222222222203', 'Apex Solutions Admin', 'customer@brightloop.local', '$2a$10$tRS1KRXfu6eIk/EvUkvD/ejmYCYq2V6w0/aBu4.Sbp9zY93qwrH4C', '11111111-1111-1111-1111-111111111103', 'ACTIVE', TRUE)
ON CONFLICT (email) DO NOTHING;

-- 3. SEED CUSTOMERS
INSERT INTO customers (id, user_id, customer_code, customer_type, company_name, contact_name, email, phone, billing_address, city, state, country, postal_code, gstin, status) VALUES
('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222203', 'CUST-001', 'BUSINESS', 'Apex Global Solutions Pvt Ltd', 'Rajesh Sharma', 'customer@brightloop.local', '+91 98765 43210', '104 Tech Park, Sector 62', 'Noida', 'Uttar Pradesh', 'India', '201301', '09AAACA1234A1Z5', 'ACTIVE'),
('33333333-3333-3333-3333-333333333302', NULL, 'CUST-002', 'BUSINESS', 'Nexus Dynamics Corp', 'Priya Menon', 'priya@nexusdynamics.in', '+91 98111 22233', '45 Indiranagar Main Rd', 'Bengaluru', 'Karnataka', 'India', '560038', '29BBBCB5678B1Z2', 'ACTIVE'),
('33333333-3333-3333-3333-333333333303', NULL, 'CUST-003', 'INDIVIDUAL', NULL, 'Amitabh Verma', 'amitabh.verma@gmail.com', '+91 99222 33344', 'Flat 12B, Sky Heights', 'Mumbai', 'Maharashtra', 'India', '400001', NULL, 'ACTIVE'),
('33333333-3333-3333-3333-333333333304', NULL, 'CUST-004', 'BUSINESS', 'Starlight Logistics Ltd', 'Gurpreet Singh', 'billing@starlightlogistics.com', '+91 97333 44455', '88 Transport Nagar', 'Jaipur', 'Rajasthan', 'India', '302013', '08CCCC9012C1Z8', 'ACTIVE'),
('33333333-3333-3333-3333-333333333305', NULL, 'CUST-005', 'BUSINESS', 'Zenith Healthtech Pvt Ltd', 'Dr. Sunita Patel', 'accounts@zenithhealth.org', '+91 96444 55566', '12 Ring Road', 'Ahmedabad', 'Gujarat', 'India', '380015', '24DDDD3456D1Z1', 'ACTIVE')
ON CONFLICT (customer_code) DO NOTHING;

-- 4. SEED PRODUCTS
INSERT INTO products (id, product_code, name, description, product_type, sale_price, cost_price, currency, is_active) VALUES
('44444444-4444-4444-4444-444444444401', 'PROD-ERP-CORE', 'Brightloop ERP Core Engine', 'Full-suite cloud enterprise subscription management and billing system', 'SUBSCRIPTION', 4999.00, 1200.00, 'INR', TRUE),
('44444444-4444-4444-4444-444444444402', 'PROD-ANALYTICS', 'Advanced BI & Financial Reporting', 'Executive real-time cohort churn analysis, MRR/ARR dashboards and automated accounting sync', 'ADDON', 1499.00, 300.00, 'INR', TRUE),
('44444444-4444-4444-4444-444444444403', 'PROD-PAYMENT-SYNC', 'Multi-Gateway Reconciliation Bridge', 'Instant webhook deduplication, UPI auto-routing and ledger auto-matching', 'ADDON', 999.00, 150.00, 'INR', TRUE),
('44444444-4444-4444-4444-444444444404', 'PROD-SUPPORT-GOLD', '24x7 Priority SLA Support', 'Dedicated technical account manager with 15-minute guaranteed response time', 'SERVICE', 2999.00, 1800.00, 'INR', TRUE),
('44444444-4444-4444-4444-444444444405', 'PROD-WHATSAPP-BOT', 'WhatsApp & SMS Invoicing Bot', 'Automated bill distribution, payment link dispatch, and PDF receipt bot', 'ADDON', 799.00, 200.00, 'INR', TRUE)
ON CONFLICT (product_code) DO NOTHING;

-- 5. SEED PRODUCT VARIANTS (as specified in Master Prompt section 4.4)
INSERT INTO product_variants (id, product_id, attribute_name, attribute_value, additional_price, sku, is_active) VALUES
('55555555-5555-5555-5555-555555555501', '44444444-4444-4444-4444-444444444401', 'Brand Edition', 'Odoo Enterprise Connector', 560.00, 'VAR-ERP-ODOO', TRUE),
('55555555-5555-5555-5555-555555555502', '44444444-4444-4444-4444-444444444401', 'Brand Edition', 'Standard Native Suite', 0.00, 'VAR-ERP-STD', TRUE),
('55555555-5555-5555-5555-555555555503', '44444444-4444-4444-4444-444444444401', 'Deployment Model', 'Dedicated High-Speed Cloud Instance', 1200.00, 'VAR-ERP-DEDICATED', TRUE)
ON CONFLICT DO NOTHING;

-- 6. SEED PLANS
INSERT INTO plans (id, plan_code, name, description, billing_interval, interval_count, minimum_quantity, maximum_quantity, base_price, currency, available_from, available_until, auto_close, allow_early_close, allow_pause, allow_renewal, is_active) VALUES
('66666666-6666-6666-6666-666666666601', 'PLAN-PRO-MONTHLY', 'Growth Pro Monthly Plan', 'Monthly billing for scaling teams including core ERP and invoicing', 'MONTHLY', 1, 1, 100, 4999.00, 'INR', '2026-01-01', '2028-12-31', FALSE, TRUE, TRUE, TRUE, TRUE),
('66666666-6666-6666-6666-666666666602', 'PLAN-SCALE-QUARTERLY', 'Scale Quarterly Bundle', 'Billed every 3 months with included Priority Support & Analytics', 'MONTHLY', 3, 1, 200, 13499.00, 'INR', '2026-01-01', '2028-12-31', FALSE, TRUE, TRUE, TRUE, TRUE),
('66666666-6666-6666-6666-666666666603', 'PLAN-ENTERPRISE-YEARLY', 'Enterprise Unlimited Annual', 'Yearly billing with 2 months free discount and all add-ons enabled', 'YEARLY', 1, 1, 1000, 49990.00, 'INR', '2026-01-01', '2028-12-31', FALSE, TRUE, TRUE, TRUE, TRUE)
ON CONFLICT (plan_code) DO NOTHING;

-- PLAN PRODUCTS
INSERT INTO plan_products (id, plan_id, product_id, default_quantity, is_mandatory) VALUES
('77777777-7777-7777-7777-777777777701', '66666666-6666-6666-6666-666666666601', '44444444-4444-4444-4444-444444444401', 1, TRUE),
('77777777-7777-7777-7777-777777777702', '66666666-6666-6666-6666-666666666602', '44444444-4444-4444-4444-444444444401', 1, TRUE),
('77777777-7777-7777-7777-777777777703', '66666666-6666-6666-6666-666666666602', '44444444-4444-4444-4444-444444444402', 1, FALSE),
('77777777-7777-7777-7777-777777777704', '66666666-6666-6666-6666-666666666603', '44444444-4444-4444-4444-444444444401', 1, TRUE),
('77777777-7777-7777-7777-777777777705', '66666666-6666-6666-6666-666666666603', '44444444-4444-4444-4444-444444444402', 1, TRUE),
('77777777-7777-7777-7777-777777777706', '66666666-6666-6666-6666-666666666603', '44444444-4444-4444-4444-444444444404', 1, TRUE)
ON CONFLICT DO NOTHING;

-- 7. SEED TAXES (GST Structure as specified in Section 4.10)
INSERT INTO taxes (id, name, tax_type, rate, country, state, is_compound, is_active, valid_from) VALUES
('88888888-8888-8888-8888-888888888801', 'CGST @ 9%', 'CGST', 0.0900, 'India', 'Uttar Pradesh', FALSE, TRUE, '2026-01-01'),
('88888888-8888-8888-8888-888888888802', 'SGST @ 9%', 'SGST', 0.0900, 'India', 'Uttar Pradesh', FALSE, TRUE, '2026-01-01'),
('88888888-8888-8888-8888-888888888803', 'IGST @ 18%', 'IGST', 0.1800, 'India', NULL, FALSE, TRUE, '2026-01-01')
ON CONFLICT DO NOTHING;

-- 8. SEED DISCOUNTS
INSERT INTO discounts (id, name, description, discount_type, discount_value, minimum_purchase, minimum_quantity, maximum_discount, maximum_uses, usage_count, applies_to, is_active) VALUES
('99999999-9999-9999-9999-999999999901', 'HACKATHON2026', 'Flat 10% Welcome Incentive on all software plans', 'PERCENTAGE', 10.00, 3000.00, 1, 2000.00, 500, 3, 'BOTH', TRUE),
('99999999-9999-9999-9999-999999999902', 'STARTUP_SAVINGS', 'Fixed ₹500 off on any subscription purchase', 'FIXED', 500.00, 2500.00, 1, 500.00, 100, 1, 'SUBSCRIPTION', TRUE)
ON CONFLICT DO NOTHING;

-- 9. SEED QUOTATION TEMPLATES
INSERT INTO quotation_templates (id, name, description, validity_days, default_plan_id, is_active) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa01', 'Standard SaaS Quotation', 'Standard monthly quotation with 30-day validity', 30, '66666666-6666-6666-6666-666666666601', TRUE),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02', 'Enterprise Annual Transformation', 'Comprehensive yearly package including dedicated support', 45, '66666666-6666-6666-6666-666666666603', TRUE)
ON CONFLICT DO NOTHING;

-- 10. SEED DOCUMENT SEQUENCES
INSERT INTO document_sequences (id, document_type, financial_year, prefix, next_number) VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01', 'INVOICE', '2026-27', 'INV/2026/', 105),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'QUOTATION', '2026-27', 'QTN/2026/', 103),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03', 'SUBSCRIPTION', '2026-27', 'SUB/2026/', 106),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb04', 'PAYMENT', '2026-27', 'PAY/2026/', 104)
ON CONFLICT (document_type, financial_year) DO NOTHING;

-- 11. SEED SUBSCRIPTIONS IN DIFFERENT STATUSES
INSERT INTO subscriptions (id, subscription_number, customer_id, plan_id, start_date, expiry_date, next_billing_date, billing_interval, interval_count, currency, subtotal, discount_total, tax_total, total_amount, status, created_by) VALUES
('cccccccc-cccc-cccc-cccc-cccccccccc01', 'SUB/2026/001', '33333333-3333-3333-3333-333333333301', '66666666-6666-6666-6666-666666666601', '2026-10-01', '2027-09-30', '2026-11-01', 'MONTHLY', 1, 'INR', 5559.00, 500.00, 910.62, 5969.62, 'ACTIVE', '22222222-2222-2222-2222-222222222201'),
('cccccccc-cccc-cccc-cccc-cccccccccc02', 'SUB/2026/002', '33333333-3333-3333-3333-333333333302', '66666666-6666-6666-6666-666666666602', '2026-09-15', '2027-09-14', '2026-12-15', 'MONTHLY', 3, 'INR', 13499.00, 0.00, 2429.82, 15928.82, 'ACTIVE', '22222222-2222-2222-2222-222222222202'),
('cccccccc-cccc-cccc-cccc-cccccccccc03', 'SUB/2026/003', '33333333-3333-3333-3333-333333333303', '66666666-6666-6666-6666-666666666601', '2026-10-10', '2027-10-09', '2026-11-10', 'MONTHLY', 1, 'INR', 4999.00, 0.00, 899.82, 5898.82, 'PAUSED', '22222222-2222-2222-2222-222222222201'),
('cccccccc-cccc-cccc-cccc-cccccccccc04', 'SUB/2026/004', '33333333-3333-3333-3333-333333333304', '66666666-6666-6666-6666-666666666603', '2026-11-01', '2027-10-31', '2026-11-01', 'YEARLY', 1, 'INR', 49990.00, 2000.00, 8638.20, 56628.20, 'CONFIRMED', '22222222-2222-2222-2222-222222222202'),
('cccccccc-cccc-cccc-cccc-cccccccccc05', 'SUB/2026/005', '33333333-3333-3333-3333-333333333305', '66666666-6666-6666-6666-666666666601', '2026-10-25', '2027-10-24', '2026-11-25', 'MONTHLY', 1, 'INR', 4999.00, 0.00, 899.82, 5898.82, 'DRAFT', '22222222-2222-2222-2222-222222222201')
ON CONFLICT (subscription_number) DO NOTHING;

-- SUBSCRIPTION LINES
INSERT INTO subscription_lines (id, subscription_id, product_id, variant_id, description, quantity, unit_price, discount_amount, tax_rate, tax_amount, line_total) VALUES
('dddddddd-dddd-dddd-dddd-dddddddddd01', 'cccccccc-cccc-cccc-cccc-cccccccccc01', '44444444-4444-4444-4444-444444444401', '55555555-5555-5555-5555-555555555501', 'Brightloop ERP Core Engine (Odoo Enterprise Connector)', 1, 5559.00, 500.00, 0.1800, 910.62, 5969.62),
('dddddddd-dddd-dddd-dddd-dddddddddd02', 'cccccccc-cccc-cccc-cccc-cccccccccc02', '44444444-4444-4444-4444-444444444401', '55555555-5555-5555-5555-555555555502', 'Brightloop ERP Core Engine (Standard)', 1, 4999.00, 0.00, 0.1800, 899.82, 5898.82)
ON CONFLICT DO NOTHING;

-- 12. SEED INVOICES
INSERT INTO invoices (id, invoice_number, customer_id, subscription_id, invoice_date, billing_period_start, billing_period_end, due_date, subtotal, discount_total, tax_total, grand_total, amount_paid, balance_due, currency, status) VALUES
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01', 'INV/2026/001', '33333333-3333-3333-3333-333333333301', 'cccccccc-cccc-cccc-cccc-cccccccccc01', '2026-10-01', '2026-10-01', '2026-10-31', '2026-10-15', 5559.00, 500.00, 910.62, 5969.62, 5969.62, 0.00, 'INR', 'PAID'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02', 'INV/2026/002', '33333333-3333-3333-3333-333333333302', 'cccccccc-cccc-cccc-cccc-cccccccccc02', '2026-09-15', '2026-09-15', '2026-12-14', '2026-09-30', 13499.00, 0.00, 2429.82, 15928.82, 10000.00, 5928.82, 'INR', 'PARTIALLY_PAID'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee03', 'INV/2026/003', '33333333-3333-3333-3333-333333333303', 'cccccccc-cccc-cccc-cccc-cccccccccc03', '2026-10-10', '2026-10-10', '2026-11-09', '2026-10-24', 4999.00, 0.00, 899.82, 5898.82, 0.00, 5898.82, 'INR', 'OVERDUE'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee04', 'INV/2026/004', '33333333-3333-3333-3333-333333333304', NULL, '2026-10-20', '2026-10-20', '2026-11-19', '2026-11-05', 2999.00, 0.00, 539.82, 3538.82, 0.00, 3538.82, 'INR', 'CONFIRMED')
ON CONFLICT (invoice_number) DO NOTHING;

-- INVOICE LINES
INSERT INTO invoice_lines (id, invoice_id, product_id, variant_id, description, quantity, unit_price, discount_amount, tax_rate, tax_amount, line_subtotal, line_total) VALUES
('ffffffff-ffff-ffff-ffff-ffffffffff01', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01', '44444444-4444-4444-4444-444444444401', '55555555-5555-5555-5555-555555555501', 'Brightloop ERP Core Engine (Odoo Enterprise Connector)', 1, 5559.00, 500.00, 0.1800, 910.62, 5059.00, 5969.62),
('ffffffff-ffff-ffff-ffff-ffffffffff02', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02', '44444444-4444-4444-4444-444444444401', '55555555-5555-5555-5555-555555555502', 'Scale Quarterly Bundle', 1, 13499.00, 0.00, 0.1800, 2429.82, 13499.00, 15928.82)
ON CONFLICT DO NOTHING;

-- 13. SEED PAYMENTS
INSERT INTO payments (id, payment_number, customer_id, payment_method, amount, currency, payment_date, reference_number, status, created_by) VALUES
('12121212-1212-1212-1212-121212121201', 'PAY/2026/001', '33333333-3333-3333-3333-333333333301', 'UPI', 5969.62, 'INR', '2026-10-02 14:30:00', 'UPI-987456123011', 'SUCCESS', '22222222-2222-2222-2222-222222222201'),
('12121212-1212-1212-1212-121212121202', 'PAY/2026/002', '33333333-3333-3333-3333-333333333302', 'BANK_TRANSFER', 10000.00, 'INR', '2026-09-20 11:15:00', 'NEFT-AXIS99887711', 'SUCCESS', '22222222-2222-2222-2222-222222222202')
ON CONFLICT (payment_number) DO NOTHING;

-- PAYMENT ALLOCATIONS
INSERT INTO payment_allocations (id, payment_id, invoice_id, allocated_amount) VALUES
('13131313-1313-1313-1313-131313131301', '12121212-1212-1212-1212-121212121201', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01', 5969.62),
('13131313-1313-1313-1313-131313131302', '12121212-1212-1212-1212-121212121202', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02', 10000.00)
ON CONFLICT DO NOTHING;
