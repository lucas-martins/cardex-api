ALTER TABLE cards
    ADD COLUMN IF NOT EXISTS card_stamp varchar(30) NOT NULL DEFAULT 'NONE';

ALTER TABLE cards
    DROP CONSTRAINT IF EXISTS uk_cards_external_language_condition_finish;

DROP INDEX IF EXISTS uk_cards_external_language_condition_finish;

ALTER TABLE cards
    DROP CONSTRAINT IF EXISTS uk_cards_external_language_condition_finish_stamp;

DROP INDEX IF EXISTS uk_cards_external_language_condition_finish_stamp;

ALTER TABLE cards
    ADD CONSTRAINT uk_cards_external_language_condition_finish_stamp
        UNIQUE (user_id, external_id, language, card_condition, card_finish, card_stamp);
