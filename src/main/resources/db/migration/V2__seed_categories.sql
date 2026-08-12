-- liquibase formatted sql
-- changeset vilicus:002_seed_categories_dml
-- comment: Seed 15 predefined transaction categories

INSERT INTO categories (name, color, "order") VALUES
    ('Groceries', '#2ECC71', 1),
    ('Utilities', '#3498DB', 2),
    ('Entertainment', '#E74C3C', 3),
    ('Salary', '#27AE60', 4),
    ('Savings', '#16A085', 5),
    ('Transport', '#F39C12', 6),
    ('Insurance', '#8E44AD', 7),
    ('Healthcare', '#E67E22', 8),
    ('Dining', '#C0392B', 9),
    ('Shopping', '#D35400', 10),
    ('Subscriptions', '#34495E', 11),
    ('Transfers', '#7F8C8D', 12),
    ('Rent', '#C0392B', 13),
    ('Taxes', '#2C3E50', 14),
    ('Other', '#95A5A6', 15);
