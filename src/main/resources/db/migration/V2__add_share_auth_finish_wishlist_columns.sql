-- Auth reset + public share fields
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_reset_token varchar(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_reset_expires_at timestamp;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS share_token varchar(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS share_enabled boolean NOT NULL DEFAULT false;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_share_token
    ON users (share_token);

-- Card finish / graded inventory fields
ALTER TABLE cards
    ADD COLUMN IF NOT EXISTS card_finish varchar(30) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE cards
    ADD COLUMN IF NOT EXISTS grading_company varchar(50);

ALTER TABLE cards
    ADD COLUMN IF NOT EXISTS grade varchar(20);

-- Wishlist purchase context
ALTER TABLE wishlist_cards
    ADD COLUMN IF NOT EXISTS notes varchar(1000);

ALTER TABLE wishlist_cards
    ADD COLUMN IF NOT EXISTS store_url varchar(1000);

ALTER TABLE wishlist_cards
    ADD COLUMN IF NOT EXISTS target_price_usd numeric(12, 2);
