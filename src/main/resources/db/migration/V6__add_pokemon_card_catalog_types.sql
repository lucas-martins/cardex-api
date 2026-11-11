ALTER TABLE pokemon_card_catalog
    ADD COLUMN IF NOT EXISTS types varchar(100);
