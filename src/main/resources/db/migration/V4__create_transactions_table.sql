-- V4__create_transactions_table.sql
-- Create transactions table for imported bank statements (CAMT.052, CSV, OFX)

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    tx_id VARCHAR(50) NOT NULL,
    tx_date DATE NOT NULL,
    amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    balance NUMERIC(19, 2),
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    description VARCHAR(500),
    counterparty VARCHAR(100),
    reference VARCHAR(200),
    category_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'imported' CHECK (status IN ('imported', 'categorized', 'archived')),
    notes VARCHAR(500),
    import_source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Unique constraint: tx_id is unique per account (deduplication)
ALTER TABLE transactions ADD CONSTRAINT uc_transactions_account_tx_id UNIQUE (account_id, tx_id);

-- Indexes for common queries
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(tx_date);
CREATE INDEX idx_transactions_counterparty ON transactions(counterparty);
CREATE INDEX idx_transactions_account_date ON transactions(account_id, tx_date);
CREATE INDEX idx_transactions_status ON transactions(status);

-- Optional: Foreign key for categories (when Phase 4 is implemented)
-- ALTER TABLE transactions ADD CONSTRAINT fk_transactions_category_id
--     FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;
