-- V3__create_accounts_table.sql
-- Create accounts table with proper indexing and constraints
-- Depends on: users table (V1), categories table (V2)

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    iban VARCHAR(34) NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'BANK_ACCOUNT',
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uc_accounts_user_iban UNIQUE (user_id, iban),
    CONSTRAINT uc_accounts_user_name UNIQUE (user_id, name),
    CONSTRAINT ck_accounts_status CHECK (status IN ('active', 'archived', 'closed')),
    CONSTRAINT ck_accounts_type CHECK (type IN ('BANK_ACCOUNT', 'CREDIT_CARD', 'CASH', 'SAVINGS', 'INVESTMENT')),
    CONSTRAINT ck_accounts_currency CHECK (currency ~ '^[A-Z]{3}$')
);

-- Create indexes for common queries
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_type ON accounts(type);

-- Composite index for user account lookup by status
CREATE INDEX idx_accounts_user_status ON accounts(user_id, status);

-- Comment for documentation
COMMENT ON TABLE accounts IS 'Financial accounts for users (bank, credit card, cash, savings, investment)';
COMMENT ON COLUMN accounts.iban IS 'International Bank Account Number - unique per user';
COMMENT ON COLUMN accounts.type IS 'Account type: BANK_ACCOUNT, CREDIT_CARD, CASH, SAVINGS, INVESTMENT';
COMMENT ON COLUMN accounts.currency IS 'ISO 4217 currency code (EUR, USD, GBP, etc.)';
COMMENT ON COLUMN accounts.status IS 'Account status: active, archived, or closed';
