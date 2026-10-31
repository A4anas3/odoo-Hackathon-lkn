-- V3: Schema Alignment for Entity Compatibility

-- 1. Subscriptions additions
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS last_billing_date DATE;
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS unit_price NUMERIC(19,2) DEFAULT 0.00;
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS quantity INTEGER DEFAULT 1;
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS auto_close_reason TEXT;

-- 2. Invoices additions
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(100);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS pdf_path VARCHAR(255);

-- 3. Quotations additions
ALTER TABLE quotations ADD COLUMN IF NOT EXISTS terms_and_conditions TEXT;

-- 4. Quotation lines additions
ALTER TABLE quotation_lines ADD COLUMN IF NOT EXISTS plan_id UUID REFERENCES plans(id) ON DELETE SET NULL;

-- 5. Discounts additions
ALTER TABLE discounts ADD COLUMN IF NOT EXISTS code VARCHAR(80);
UPDATE discounts SET code = name WHERE code IS NULL;
ALTER TABLE discounts ALTER COLUMN code SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_discounts_code ON discounts(code);

-- 6. Billing records additions
ALTER TABLE billing_records ADD COLUMN IF NOT EXISTS billing_date DATE DEFAULT CURRENT_DATE;
ALTER TABLE billing_records ADD COLUMN IF NOT EXISTS amount NUMERIC(19,2) DEFAULT 0.00;
ALTER TABLE billing_records ADD COLUMN IF NOT EXISTS error_message TEXT;

-- 7. Additional compatibility columns
ALTER TABLE payments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE plan_products ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE discount_products ADD COLUMN IF NOT EXISTS plan_id UUID REFERENCES plans(id) ON DELETE CASCADE;
ALTER TABLE invoice_tax_lines ADD COLUMN IF NOT EXISTS tax_name VARCHAR(100);
ALTER TABLE subscription_history ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE subscription_history ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
