ALTER TABLE payouts
ADD CONSTRAINT uk_payouts_company_id_idempotency_key
UNIQUE (company_id, idempotency_key);