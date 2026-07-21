package com.cardex.api.component.cardimport;

import com.cardex.api.dto.response.CardImportPreviewItemResponse;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardImportValidator {

    private final CardRepository cardRepository;

    public CardImportPreviewItemResponse validate(CSVRecord record) {
        String externalId = record.get(CardCsvHeaders.EXTERNAL_ID);
        String name = record.get(CardCsvHeaders.NAME);
        String collectionName = record.get(CardCsvHeaders.COLLECTION);
        String cardNumber = record.get(CardCsvHeaders.CARD_NUMBER);

        try {
            validateRequired(
                    externalId,
                    CardCsvHeaders.EXTERNAL_ID
            );
            validateRequired(
                    name,
                    CardCsvHeaders.NAME
            );
            validateRequired(
                    record.get(CardCsvHeaders.COLLECTION_ID),
                    CardCsvHeaders.COLLECTION_ID
            );
            validateRequired(
                    collectionName,
                    CardCsvHeaders.COLLECTION
            );
            validateRequired(
                    cardNumber,
                    CardCsvHeaders.CARD_NUMBER
            );
            validateRequired(
                    record.get(CardCsvHeaders.QUANTITY),
                    CardCsvHeaders.QUANTITY
            );
            validateRequired(
                    record.get(CardCsvHeaders.LANGUAGE),
                    CardCsvHeaders.LANGUAGE
            );
            validateRequired(
                    record.get(CardCsvHeaders.CONDITION),
                    CardCsvHeaders.CONDITION
            );

            int quantity = parseQuantity(record);
            CardLanguage language = parseLanguage(record);
            CardCondition condition = parseCondition(record);

            validateFavorite(record);

            boolean duplicate =
                    cardRepository.existsByExternalIdAndLanguageAndCondition(
                            externalId,
                            language,
                            condition
                    );

            if (duplicate) {
                return invalidItem(
                        record,
                        externalId,
                        name,
                        collectionName,
                        cardNumber,
                        quantity,
                        "Card already exists in the collection."
                );
            }

            return new CardImportPreviewItemResponse(
                    getFileLine(record),
                    externalId,
                    name,
                    collectionName,
                    cardNumber,
                    quantity,
                    true,
                    null
            );
        } catch (Exception exception) {
            return invalidItem(
                    record,
                    externalId,
                    name,
                    collectionName,
                    cardNumber,
                    parseQuantitySafely(record),
                    exception.getMessage()
            );
        }
    }

    private int parseQuantity(CSVRecord record) {
        int quantity = Integer.parseInt(
                record.get(CardCsvHeaders.QUANTITY)
        );

        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        return quantity;
    }

    private CardLanguage parseLanguage(CSVRecord record) {
        return CardLanguage.valueOf(
                record.get(CardCsvHeaders.LANGUAGE)
        );
    }

    private CardCondition parseCondition(CSVRecord record) {
        return CardCondition.valueOf(
                record.get(CardCsvHeaders.CONDITION)
        );
    }

    private void validateFavorite(CSVRecord record) {
        String favorite = record.get(CardCsvHeaders.FAVORITE);

        if (!"true".equalsIgnoreCase(favorite)
                && !"false".equalsIgnoreCase(favorite)) {
            throw new IllegalArgumentException(
                    "Favorite must be true or false."
            );
        }
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required."
            );
        }
    }

    private int parseQuantitySafely(CSVRecord record) {
        try {
            return Integer.parseInt(
                    record.get(CardCsvHeaders.QUANTITY)
            );
        } catch (Exception exception) {
            return 0;
        }
    }

    private CardImportPreviewItemResponse invalidItem(
            CSVRecord record,
            String externalId,
            String name,
            String collectionName,
            String cardNumber,
            int quantity,
            String error
    ) {
        return new CardImportPreviewItemResponse(
                getFileLine(record),
                externalId,
                name,
                collectionName,
                cardNumber,
                quantity,
                false,
                error
        );
    }

    private long getFileLine(CSVRecord record) {
        return record.getRecordNumber() + 1;
    }
}