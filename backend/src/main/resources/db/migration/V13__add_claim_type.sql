ALTER TABLE claims ADD COLUMN claim_type VARCHAR(20);

UPDATE claims
SET claim_type = (
    SELECT products.category
    FROM policies
    JOIN products ON products.id = policies.product_id
    WHERE policies.id = claims.policy_id
)
WHERE claim_type IS NULL;

ALTER TABLE claims ALTER COLUMN claim_type SET NOT NULL;
ALTER TABLE claims ADD CONSTRAINT ck_claims_type CHECK (claim_type IN ('HEALTH', 'LIFE', 'VEHICLE', 'TRAVEL'));

CREATE INDEX ix_claims_type ON claims (claim_type);