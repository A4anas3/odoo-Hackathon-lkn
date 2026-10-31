-- =============================================================================
-- BRIGHTLOOP ERP - V1__init_schema.sql
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. ROLES
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(30) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. USERS
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id UUID NOT NULL REFERENCES roles(id),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. PASSWORD RESET TOKENS
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. USER SESSIONS (Refresh Tokens)
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. CUSTOMERS
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    customer_code VARCHAR(50) UNIQUE NOT NULL,
    customer_type VARCHAR(30) NOT NULL DEFAULT 'BUSINESS', -- INDIVIDUAL, BUSINESS
    company_name VARCHAR(200),
    contact_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    billing_address TEXT,
    shipping_address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    postal_code VARCHAR(20),
    gstin VARCHAR(20),
    tax_registration_type VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, ARCHIVED
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 6. PRODUCTS
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_code VARCHAR(80) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    product_type VARCHAR(30) NOT NULL DEFAULT 'SUBSCRIPTION', -- SERVICE, SUBSCRIPTION, ADDON
    sale_price NUMERIC(19,2) NOT NULL DEFAULT 0.00 CHECK (sale_price >= 0),
    cost_price NUMERIC(19,2) NOT NULL DEFAULT 0.00 CHECK (cost_price >= 0),
    currency VARCHAR(3) DEFAULT 'INR',
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 7. PRODUCT VARIANTS
CREATE TABLE product_variants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_value VARCHAR(150) NOT NULL,
    additional_price NUMERIC(19,2) NOT NULL DEFAULT 0.00 CHECK (additional_price >= 0),
    sku VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. PLANS
CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plan_code VARCHAR(80) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    billing_interval VARCHAR(30) NOT NULL, -- DAILY, WEEKLY, MONTHLY, YEARLY
    interval_count INTEGER NOT NULL DEFAULT 1 CHECK (interval_count >= 1),
    minimum_quantity INTEGER NOT NULL DEFAULT 1 CHECK (minimum_quantity >= 1),
    maximum_quantity INTEGER,
    base_price NUMERIC(19,2) NOT NULL DEFAULT 0.00 CHECK (base_price >= 0),
    currency VARCHAR(3) DEFAULT 'INR',
    available_from DATE,
    available_until DATE,
    auto_close BOOLEAN DEFAULT FALSE,
    allow_early_close BOOLEAN DEFAULT TRUE,
    allow_pause BOOLEAN DEFAULT TRUE,
    allow_renewal BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. PLAN PRODUCTS
CREATE TABLE plan_products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    default_quantity INTEGER NOT NULL DEFAULT 1 CHECK (default_quantity >= 1),
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. TAXES
CREATE TABLE taxes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    tax_type VARCHAR(30) NOT NULL, -- GST, CGST, SGST, IGST, OTHER
    rate NUMERIC(8,4) NOT NULL CHECK (rate >= 0),
    country VARCHAR(100) DEFAULT 'India',
    state VARCHAR(100),
    is_compound BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    valid_from DATE,
    valid_until DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. DISCOUNTS
CREATE TABLE discounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    discount_type VARCHAR(30) NOT NULL, -- FIXED, PERCENTAGE
    discount_value NUMERIC(19,2) NOT NULL CHECK (discount_value > 0),
    minimum_purchase NUMERIC(19,2) DEFAULT 0.00,
    minimum_quantity INTEGER DEFAULT 1,
    maximum_discount NUMERIC(19,2),
    maximum_uses INTEGER,
    maximum_uses_per_customer INTEGER,
    usage_count INTEGER DEFAULT 0,
    applies_to VARCHAR(30) NOT NULL DEFAULT 'BOTH', -- PRODUCT, SUBSCRIPTION, BOTH
    valid_from DATE,
    valid_until DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE discount_products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    discount_id UUID NOT NULL REFERENCES discounts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE(discount_id, product_id)
);

-- 12. QUOTATION TEMPLATES
CREATE TABLE quotation_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    validity_days INTEGER NOT NULL DEFAULT 30,
    default_plan_id UUID REFERENCES plans(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quotation_template_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    template_id UUID NOT NULL REFERENCES quotation_templates(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    tax_id UUID REFERENCES taxes(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 13. QUOTATIONS
CREATE TABLE quotations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quotation_number VARCHAR(80) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    template_id UUID REFERENCES quotation_templates(id) ON DELETE SET NULL,
    quotation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_until DATE NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    discount_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    tax_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    grand_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED, CONVERTED
    notes TEXT,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quotation_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quotation_id UUID NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    variant_id UUID REFERENCES product_variants(id) ON DELETE SET NULL,
    description TEXT,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    tax_rate NUMERIC(8,4) NOT NULL DEFAULT 0.00,
    tax_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    line_total NUMERIC(19,2) NOT NULL DEFAULT 0.00
);

-- 14. SUBSCRIPTIONS
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subscription_number VARCHAR(80) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE RESTRICT,
    quotation_id UUID REFERENCES quotations(id) ON DELETE SET NULL,
    start_date DATE NOT NULL,
    expiry_date DATE,
    next_billing_date DATE NOT NULL,
    billing_interval VARCHAR(30) NOT NULL, -- DAILY, WEEKLY, MONTHLY, YEARLY
    interval_count INTEGER NOT NULL DEFAULT 1,
    currency VARCHAR(3) DEFAULT 'INR',
    subtotal NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    discount_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    tax_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    payment_terms VARCHAR(100) DEFAULT 'Immediate',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, QUOTATION, CONFIRMED, ACTIVE, PAUSED, CLOSED, EXPIRED
    auto_renew BOOLEAN DEFAULT FALSE,
    auto_close BOOLEAN DEFAULT FALSE,
    allow_pause BOOLEAN DEFAULT TRUE,
    allow_early_close BOOLEAN DEFAULT TRUE,
    allow_renewal BOOLEAN DEFAULT TRUE,
    closed_at TIMESTAMP,
    paused_at TIMESTAMP,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscription_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    variant_id UUID REFERENCES product_variants(id) ON DELETE SET NULL,
    description TEXT,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    tax_rate NUMERIC(8,4) NOT NULL DEFAULT 0.00,
    tax_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    line_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscription_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    reason TEXT,
    changed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 15. INVOICES
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_number VARCHAR(80) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    subscription_id UUID REFERENCES subscriptions(id) ON DELETE SET NULL,
    invoice_date DATE NOT NULL DEFAULT CURRENT_DATE,
    billing_period_start DATE,
    billing_period_end DATE,
    due_date DATE NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    discount_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    tax_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    grand_total NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    amount_paid NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    balance_due NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, CONFIRMED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED
    notes TEXT,
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE invoice_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    variant_id UUID REFERENCES product_variants(id) ON DELETE SET NULL,
    description TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    tax_rate NUMERIC(8,4) NOT NULL DEFAULT 0.00,
    tax_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    line_subtotal NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    line_total NUMERIC(19,2) NOT NULL DEFAULT 0.00
);

CREATE TABLE invoice_tax_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    tax_id UUID REFERENCES taxes(id) ON DELETE RESTRICT,
    taxable_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    tax_rate NUMERIC(8,4) NOT NULL DEFAULT 0.00,
    tax_amount NUMERIC(19,2) NOT NULL DEFAULT 0.00
);

-- 16. PAYMENTS
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_number VARCHAR(80) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'BANK_TRANSFER', -- UPI, BANK_TRANSFER, CASH, CARD, GATEWAY, OTHER
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) DEFAULT 'INR',
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reference_number VARCHAR(150),
    gateway_transaction_id VARCHAR(200),
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- PENDING, SUCCESS, FAILED, REFUNDED
    notes TEXT,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payment_allocations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    allocated_amount NUMERIC(19,2) NOT NULL CHECK (allocated_amount > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 17. DISCOUNT USAGES
CREATE TABLE discount_usages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    discount_id UUID NOT NULL REFERENCES discounts(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    invoice_id UUID REFERENCES invoices(id) ON DELETE SET NULL,
    amount_saved NUMERIC(19,2) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 18. BILLING PERIOD TRACKING (Mandatory Anti-duplicate Billing)
CREATE TABLE billing_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    billing_period_start DATE NOT NULL,
    billing_period_end DATE NOT NULL,
    invoice_id UUID REFERENCES invoices(id) ON DELETE SET NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- PENDING, SUCCESS, FAILED
    attempts INTEGER DEFAULT 1,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(subscription_id, billing_period_start)
);

-- 19. DOCUMENT SEQUENCES
CREATE TABLE document_sequences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_type VARCHAR(30) NOT NULL, -- INVOICE, QUOTATION, SUBSCRIPTION, PAYMENT
    financial_year VARCHAR(20) NOT NULL,
    prefix VARCHAR(30) NOT NULL,
    next_number BIGINT NOT NULL DEFAULT 1,
    UNIQUE(document_type, financial_year)
);

-- 20. AUDIT LOGS
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    action VARCHAR(50) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 21. OUTBOX EVENTS (Reliable Async Notifications / Emails)
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- =============================================================================
-- PERFORMANCE INDEXES (Mandatory Section 14)
-- =============================================================================
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_billing_due ON subscriptions(next_billing_date, status);
CREATE INDEX idx_subscriptions_customer ON subscriptions(customer_id);
CREATE INDEX idx_invoices_customer ON invoices(customer_id);
CREATE INDEX idx_invoices_status_due ON invoices(status, due_date);
CREATE INDEX idx_payments_date ON payments(payment_date);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_products_code ON products(product_code);
CREATE INDEX idx_billing_records_sub_start ON billing_records(subscription_id, billing_period_start);
