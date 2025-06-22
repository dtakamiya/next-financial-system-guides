-- 口座番号を採番するためのシーケンス
CREATE SEQUENCE IF NOT EXISTS account_number_seq
    START WITH 1000000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- accountsテーブル
CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    customer_name VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    version BIGINT NOT NULL
);

-- transfersテーブル
CREATE TABLE IF NOT EXISTS transfers (
    id UUID PRIMARY KEY,
    source_account_id UUID NOT NULL,
    destination_account_id UUID NOT NULL,
    money_amount DECIMAL(19, 2) NOT NULL,
    money_currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL,
    FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    FOREIGN KEY (destination_account_id) REFERENCES accounts(id)
); 