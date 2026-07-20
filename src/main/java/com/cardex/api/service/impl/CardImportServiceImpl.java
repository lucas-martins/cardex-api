package com.cardex.api.service.impl;

import com.cardex.api.dto.response.CardImportPreviewItemResponse;
import com.cardex.api.dto.response.CardImportPreviewResponse;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.exception.InvalidCsvFileException;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.service.CardImportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardImportServiceImpl implements CardImportService {

    private static final String EXTERNAL_ID = "External ID";
    private static final String NAME = "Name";
    private static final String COLLECTION_ID = "Collection ID";
    private static final String COLLECTION = "Collection";
    private static final String CARD_NUMBER = "Card Number";
    private static final String RARITY = "Rarity";
    private static final String QUANTITY = "Quantity";
    private static final String LANGUAGE = "Language";
    private static final String CONDITION = "Condition";
    private static final String FAVORITE = "Favorite";
    private static final String NOTES = "Notes";

    private final CardRepository cardRepository;

    @Override
    public CardImportPreviewResponse previewCsv(MultipartFile file) {
        validateFile(file);

        List<CardImportPreviewItemResponse> items = new ArrayList<>();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                BOMInputStream.builder()
                                        .setInputStream(file.getInputStream())
                                        .get(),
                                StandardCharsets.UTF_8
                        )
                );

                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {
            validateHeaders(parser);

            for (CSVRecord record : parser) {
                items.add(validateRecord(record));
            }
        } catch (InvalidCsvFileException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidCsvFileException(
                    "Could not read the CSV file.",
                    exception
            );
        }

        long validRows = items.stream()
                .filter(CardImportPreviewItemResponse::valid)
                .count();

        return new CardImportPreviewResponse(
                items.size(),
                validRows,
                items.size() - validRows,
                items
        );
    }

    private CardImportPreviewItemResponse validateRecord(CSVRecord record) {
        String externalId = record.get(EXTERNAL_ID);
        String name = record.get(NAME);
        String collectionName = record.get(COLLECTION);
        String cardNumber = record.get(CARD_NUMBER);

        try {
            validateRequired(externalId, EXTERNAL_ID);
            validateRequired(name, NAME);
            validateRequired(record.get(COLLECTION_ID), COLLECTION_ID);
            validateRequired(collectionName, COLLECTION);
            validateRequired(cardNumber, CARD_NUMBER);
            validateRequired(record.get(QUANTITY), QUANTITY);
            validateRequired(record.get(LANGUAGE), LANGUAGE);
            validateRequired(record.get(CONDITION), CONDITION);

            int quantity = Integer.parseInt(record.get(QUANTITY));

            if (quantity < 1) {
                throw new IllegalArgumentException(
                        "Quantity must be greater than zero."
                );
            }

            CardLanguage language =
                    CardLanguage.valueOf(record.get(LANGUAGE));

            CardCondition condition =
                    CardCondition.valueOf(record.get(CONDITION));

            parseBoolean(record.get(FAVORITE));

            boolean duplicate = cardRepository.existsByExternalIdAndLanguageAndCondition(
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
                    record.getRecordNumber() + 1,
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
                record.getRecordNumber() + 1,
                externalId,
                name,
                collectionName,
                cardNumber,
                quantity,
                false,
                error
        );
    }

    private int parseQuantitySafely(CSVRecord record) {
        try {
            return Integer.parseInt(record.get(QUANTITY));
        } catch (Exception exception) {
            return 0;
        }
    }

    private boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw new IllegalArgumentException(
                "Favorite must be true or false."
        );
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required."
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCsvFileException(
                    "The CSV file is required."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null
                || !filename.toLowerCase().endsWith(".csv")) {
            throw new InvalidCsvFileException(
                    "The file must have the .csv extension."
            );
        }
    }

    private void validateHeaders(CSVParser parser) {
        List<String> requiredHeaders = List.of(
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

        if (!parser.getHeaderMap().keySet().containsAll(requiredHeaders)) {
            throw new InvalidCsvFileException(
                    "The CSV file does not contain the expected CardDex columns."
            );
        }
    }
}