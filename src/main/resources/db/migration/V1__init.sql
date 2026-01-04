-- ============================
-- Payouts
-- ============================

CREATE TABLE payouts
(
    id              UUID PRIMARY KEY,
    company_id      UUID                     NOT NULL,
    amount          NUMERIC(15, 2)           NOT NULL,
    currency        VARCHAR(3)               NOT NULL,
    status          VARCHAR(32)              NOT NULL,
    idempotency_key VARCHAR(128)             NOT NULL,

    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Idempotency by company
CREATE UNIQUE INDEX ux_payouts_company_idempotency
    ON payouts (company_id, idempotency_key);
