-- Align card conditions with TCG grading labels used in the UI.
-- Drop unique constraint first so remapped values can collide safely, then merge.

ALTER TABLE cards
    DROP CONSTRAINT IF EXISTS uk_cards_external_language_condition_finish;

DROP INDEX IF EXISTS uk_cards_external_language_condition_finish;

UPDATE cards
SET card_condition = CASE card_condition
    WHEN 'MINT' THEN 'MINT'
    WHEN 'NEAR_MINT' THEN 'NEAR_MINT'
    WHEN 'EXCELLENT' THEN 'SLIGHTLY_PLAYED'
    WHEN 'LIGHTLY_PLAYED' THEN 'SLIGHTLY_PLAYED'
    WHEN 'GOOD' THEN 'MODERATELY_PLAYED'
    WHEN 'PLAYED' THEN 'MODERATELY_PLAYED'
    WHEN 'POOR' THEN 'DAMAGED'
    WHEN 'SLIGHTLY_PLAYED' THEN 'SLIGHTLY_PLAYED'
    WHEN 'MODERATELY_PLAYED' THEN 'MODERATELY_PLAYED'
    WHEN 'HEAVILY_PLAYED' THEN 'HEAVILY_PLAYED'
    WHEN 'DAMAGED' THEN 'DAMAGED'
    ELSE 'NEAR_MINT'
END;

UPDATE cards keeper
SET quantity = totals.total_quantity
FROM (
    SELECT
        MIN(id) AS keep_id,
        SUM(quantity) AS total_quantity
    FROM cards
    GROUP BY user_id, external_id, language, card_condition, card_finish
    HAVING COUNT(*) > 1
) totals
WHERE keeper.id = totals.keep_id;

DELETE FROM cards duplicate
USING cards keeper
WHERE duplicate.user_id = keeper.user_id
  AND duplicate.external_id = keeper.external_id
  AND duplicate.language = keeper.language
  AND duplicate.card_condition = keeper.card_condition
  AND duplicate.card_finish = keeper.card_finish
  AND duplicate.id > keeper.id;

ALTER TABLE cards
    ADD CONSTRAINT uk_cards_external_language_condition_finish
        UNIQUE (user_id, external_id, language, card_condition, card_finish);
