-- ===============================================
-- Create table: bulk_payment_info
-- ===============================================
CREATE TABLE bulk_payment_info
(
    id              BIGSERIAL PRIMARY KEY,
    total_records   INTEGER            DEFAULT 0,
    valid_records   INTEGER            DEFAULT 0,
    invalid_records INTEGER            DEFAULT 0,
    total_amount    NUMERIC(18, 2)     DEFAULT 0,
    total_fee       NUMERIC(18, 2)     DEFAULT 0,
    source_account  VARCHAR(100),
    currency        VARCHAR(10),
    status          VARCHAR(50),
    remark          TEXT,
    effective_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    submitted_at    TIMESTAMP,
    completed_at    TIMESTAMP
);

CREATE INDEX idx_bulk_payment_info_status ON bulk_payment_info (status);

-- ===============================================
-- Create table: bulk_payment_data_staging
-- ===============================================
CREATE TABLE bulk_payment_data_staging
(
    id                   BIGSERIAL PRIMARY KEY,
    bulk_payment_info_id BIGINT         NOT NULL REFERENCES bulk_payment_info (id) ON DELETE CASCADE,
    beneficiary_account  VARCHAR(100)   NOT NULL,
    beneficiary_name     VARCHAR(255),
    currency             VARCHAR(10),
    amount               NUMERIC(18, 2) NOT NULL,
    fee                  NUMERIC(18, 2)          DEFAULT 0,
    failure_reason       TEXT,
    sequence_number      VARCHAR(50),
    created_at           TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP
);

CREATE INDEX idx_staging_bulk_payment_info_id ON bulk_payment_data_staging (bulk_payment_info_id);
CREATE INDEX idx_staging_sequence_number ON bulk_payment_data_staging (sequence_number);

-- ===============================================
-- Create table: bulk_payment_data_prod
-- ===============================================
CREATE TABLE bulk_payment_data_prod
(
    id                   BIGSERIAL PRIMARY KEY,
    bulk_payment_info_id BIGINT         NOT NULL REFERENCES bulk_payment_info (id) ON DELETE CASCADE,
    beneficiary_account  VARCHAR(100)   NOT NULL,
    beneficiary_name     VARCHAR(255),
    amount               NUMERIC(18, 2) NOT NULL,
    fee                  NUMERIC(18, 2)          DEFAULT 0,
    status               VARCHAR(50),
    failure_reason       TEXT,
    transaction_id       VARCHAR(100),
    payment_reference    VARCHAR(100),
    executed_at          TIMESTAMP,
    created_at           TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP
);

CREATE INDEX idx_prod_bulk_payment_info_id ON bulk_payment_data_prod (bulk_payment_info_id);
CREATE INDEX idx_prod_transaction_id ON bulk_payment_data_prod (transaction_id);
