package com.cardex.api.component.cardimport;

import java.util.List;

public final class CardCsvHeaders {

    public static final String EXTERNAL_ID = "External ID";
    public static final String NAME = "Name";
    public static final String COLLECTION_ID = "Collection ID";
    public static final String COLLECTION = "Collection";
    public static final String CARD_NUMBER = "Card Number";
    public static final String RARITY = "Rarity";
    public static final String QUANTITY = "Quantity";
    public static final String LANGUAGE = "Language";
    public static final String CONDITION = "Condition";
    public static final String FAVORITE = "Favorite";
    public static final String NOTES = "Notes";

    private CardCsvHeaders() {
    }

    public static List<String> requiredHeaders() {
        return List.of(
                EXTERNAL_ID,
                NAME,
                COLLECTION_ID,
                COLLECTION,
                CARD_NUMBER,
                RARITY,
                QUANTITY,
                LANGUAGE,
                CONDITION,
                FAVORITE,
                NOTES
        );
    }
}