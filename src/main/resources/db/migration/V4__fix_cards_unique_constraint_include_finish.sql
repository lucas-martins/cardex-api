-- Old unique key blocked multiple finishes of the same card.
-- Replace it with user + external + language + condition + finish.

ALTER TABLE cards
    DROP CONSTRAINT IF EXISTS uk_cards_external_language_condition;

DROP INDEX IF EXISTS uk_cards_external_language_condition;

ALTER TABLE cards
    DROP CONSTRAINT IF EXISTS uk_cards_external_language_condition_finish;

DROP INDEX IF EXISTS uk_cards_external_language_condition_finish;

ALTER TABLE cards
    ALTER COLUMN card_finish SET DEFAULT 'NORMAL';

UPDATE cards
SET card_finish = 'NORMAL'
WHERE card_finish IS NULL;

ALTER TABLE cards
    ALTER COLUMN card_finish SET NOT NULL;

ALTER TABLE cards
    ADD CONSTRAINT uk_cards_external_language_condition_finish
        UNIQUE (user_id, external_id, language, card_condition, card_finish);
